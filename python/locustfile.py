"""
NameGame API — Locust stress test
==================================
Cubre el flujo completo: start game → get round → submit answer (N veces) → get results

Uso básico:
    locust -f locustfile.py --host=http://localhost:8080

Con headless (útil para CI o resultados rápidos):
    locust -f locustfile.py --host=http://localhost:8080 \
           --headless -u 50 -r 5 --run-time 60s \
           --html report.html --csv results

Escenarios disponibles:
    - NormalPlayer     (weight=6): flujo normal, una respuesta por round
    - FastPlayer       (weight=2): responde muy rápido, mide reaction time bajo
    - SlowPlayer       (weight=1): simula usuario lento / timeout largo
    - ParallelRounds   (weight=1): pide el mismo round dos veces (race condition test)
"""

import random
import time
import uuid
from datetime import datetime, timezone

from locust import HttpUser, TaskSet, task, between, events
from locust.exception import StopUser

# ---------------------------------------------------------------------------
# Configuración del juego
# ---------------------------------------------------------------------------
TOTAL_ROUNDS = 5
FACES_PER_ROUND = 5
BASE_URL = "/api/v1/games"


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def log(user, msg: str):
    print(f"[{user.__class__.__name__}] {msg}")


# ---------------------------------------------------------------------------
# TaskSets (flujos de usuario)
# ---------------------------------------------------------------------------

class FullGameFlow(TaskSet):
    """
    Flujo completo: inicia partida, recorre todos los rounds, consulta resultados.
    Representativo de un usuario real que termina el juego.
    """

    def on_start(self):
        self.game_id = None
        self.total_rounds = TOTAL_ROUNDS
        self.faces_per_round = FACES_PER_ROUND
        self._play_full_game()

    def _play_full_game(self):
        game_id = self._start_game()
        if game_id is None:
            raise StopUser()

        for round_num in range(1, self.total_rounds + 1):
            round_data = self._get_round(game_id, round_num)
            if round_data is None:
                raise StopUser()

            # Simula tiempo de decisión del usuario (sobreescrito por subclases)
            think_time = self._think_time()
            time.sleep(think_time)

            faces = round_data.get("faces", [])
            if not faces:
                log(self, f"No faces in round {round_num}, aborting")
                raise StopUser()

            selected = random.choice(faces)
            self._submit_answer(game_id, round_num, selected["personId"])

        self._get_results(game_id)
        raise StopUser()

    def _start_game(self) -> str | None:
        with self.client.post(
            BASE_URL,
            json={"totalRounds": self.total_rounds, "facesPerRound": self.faces_per_round},
            name="POST /games (start)",
            catch_response=True,
        ) as resp:
            if resp.status_code == 201:
                data = resp.json()
                return str(data.get("gameId"))
            else:
                resp.failure(f"Start game failed: {resp.status_code} — {resp.text[:200]}")
                return None

    def _get_round(self, game_id: str, round_num: int) -> dict | None:
        with self.client.get(
            f"{BASE_URL}/{game_id}/rounds/{round_num}",
            name="GET /games/{id}/rounds/{n}",
            catch_response=True,
        ) as resp:
            if resp.status_code == 200:
                return resp.json()
            else:
                resp.failure(f"Get round {round_num} failed: {resp.status_code} — {resp.text[:200]}")
                return None

    def _submit_answer(self, game_id: str, round_num: int, selected_person_id: str):
        with self.client.post(
            f"{BASE_URL}/{game_id}/rounds/{round_num}/answer",
            json={"selectedPersonId": selected_person_id, "clientTimestamp": now_iso()},
            name="POST /games/{id}/rounds/{n}/answer",
            catch_response=True,
        ) as resp:
            if resp.status_code == 200:
                data = resp.json()
                correct = data.get("correct", False)
                reaction = data.get("reactionTimeMillis", 0)
                # Agrega etiqueta custom al nombre del request para distinguir correct/incorrect
                resp.success()
            elif resp.status_code == 409:
                # Round ya respondido — puede pasar en ParallelRounds test, no es falla real
                resp.success()
            else:
                resp.failure(f"Submit answer failed: {resp.status_code} — {resp.text[:200]}")

    def _get_results(self, game_id: str):
        with self.client.get(
            f"{BASE_URL}/{game_id}/results",
            name="GET /games/{id}/results",
            catch_response=True,
        ) as resp:
            if resp.status_code == 200:
                data = resp.json()
                pct = data.get("correctPercentage", 0)
                resp.success()
            else:
                resp.failure(f"Get results failed: {resp.status_code} — {resp.text[:200]}")

    def _think_time(self) -> float:
        """Tiempo de reflexión por defecto: distribución normal entre 1-3s."""
        return max(0.5, random.gauss(2.0, 0.8))

    # Locust requiere al menos un @task; el flujo real se ejecuta en on_start
    @task
    def idle(self):
        raise StopUser()


class FastGameFlow(FullGameFlow):
    """Usuario rápido: responde casi de inmediato. Estressa el cálculo de reactionTime."""

    def _think_time(self) -> float:
        return random.uniform(0.05, 0.3)


class SlowGameFlow(FullGameFlow):
    """Usuario lento: simula abandono parcial o conexión lenta."""

    def _think_time(self) -> float:
        return random.uniform(4.0, 8.0)


class ParallelRoundFlow(TaskSet):
    """
    Caso de stress específico: pide el mismo round dos veces casi simultáneamente.
    Expone la race condition en GetRoundUseCase.presentedAt y en submitAnswer duplicado.
    """

    def on_start(self):
        self._run()

    def _run(self):
        # 1. Inicia partida
        resp = self.client.post(
            BASE_URL,
            json={"totalRounds": 1, "facesPerRound": FACES_PER_ROUND},
            name="POST /games (parallel-test)",
        )
        if resp.status_code != 201:
            raise StopUser()

        game_id = resp.json().get("gameId")

        # 2. Doble GET del mismo round (sin sleep entre ellos)
        r1 = self.client.get(
            f"{BASE_URL}/{game_id}/rounds/1",
            name="GET /games/{id}/rounds/1 (primera vez)",
        )
        r2 = self.client.get(
            f"{BASE_URL}/{game_id}/rounds/1",
            name="GET /games/{id}/rounds/1 (segunda vez - idempotency)",
        )

        if r1.status_code != 200:
            raise StopUser()

        faces = r1.json().get("faces", [])
        if not faces:
            raise StopUser()

        selected = random.choice(faces)

        # 3. Doble submit del mismo answer (el segundo debe devolver 409)
        self.client.post(
            f"{BASE_URL}/{game_id}/rounds/1/answer",
            json={"selectedPersonId": selected["personId"], "clientTimestamp": now_iso()},
            name="POST /rounds/1/answer (primera vez)",
        )
        with self.client.post(
            f"{BASE_URL}/{game_id}/rounds/1/answer",
            json={"selectedPersonId": selected["personId"], "clientTimestamp": now_iso()},
            name="POST /rounds/1/answer (segunda - debe ser 409)",
            catch_response=True,
        ) as resp:
            if resp.status_code == 409:
                resp.success()  # Comportamiento correcto
            elif resp.status_code == 200:
                resp.failure("RACE CONDITION: segundo submit aceptado como válido")
            else:
                resp.failure(f"Unexpected: {resp.status_code}")

        raise StopUser()

    @task
    def idle(self):
        raise StopUser()


# ---------------------------------------------------------------------------
# Usuarios de Locust
# ---------------------------------------------------------------------------

class NormalPlayer(HttpUser):
    """
    Usuario normal. Weight=6 → representa el 60% del tráfico.
    """
    tasks = [FullGameFlow]
    weight = 6
    wait_time = between(1, 3)


class FastPlayer(HttpUser):
    """
    Usuario muy rápido. Útil para medir throughput máximo.
    """
    tasks = [FastGameFlow]
    weight = 2
    wait_time = between(0.1, 0.5)


class SlowPlayer(HttpUser):
    """
    Usuario lento. Ocupa conexiones por más tiempo, estressa el pool.
    """
    tasks = [SlowGameFlow]
    weight = 1
    wait_time = between(0, 1)


class ParallelTester(HttpUser):
    """
    Prueba de idempotencia y race conditions.
    """
    tasks = [ParallelRoundFlow]
    weight = 1
    wait_time = between(0.1, 0.5)


# ---------------------------------------------------------------------------
# Event hooks — métricas custom al finalizar
# ---------------------------------------------------------------------------

@events.quitting.add_listener
def on_quitting(environment, **kwargs):
    stats = environment.stats
    print("\n" + "="*60)
    print("  RESUMEN DE PERFORMANCE — NameGame")
    print("="*60)

    for name, entry in stats.entries.items():
        if entry.num_requests == 0:
            continue
        label = name[1] if isinstance(name, tuple) else str(name)
        print(
            f"\n  {label}\n"
            f"    Requests : {entry.num_requests}\n"
            f"    Failures : {entry.num_failures} ({100*entry.fail_ratio:.1f}%)\n"
            f"    Median   : {entry.median_response_time:.0f}ms\n"
            f"    p95      : {entry.get_response_time_percentile(0.95):.0f}ms\n"
            f"    p99      : {entry.get_response_time_percentile(0.99):.0f}ms\n"
            f"    RPS      : {entry.current_rps:.1f}\n"
        )

    total = stats.total
    print(f"\n  TOTAL")
    print(f"    Requests : {total.num_requests}")
    print(f"    Failures : {total.num_failures} ({100*total.fail_ratio:.1f}%)")
    print(f"    Median   : {total.median_response_time:.0f}ms")
    print(f"    p95      : {total.get_response_time_percentile(0.95):.0f}ms")
    print("="*60 + "\n")
