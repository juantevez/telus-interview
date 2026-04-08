# The Name Game — Backend Design Document

## 1. Visión General

Aplicación tipo quiz donde se muestran N rostros de personas (hombres y mujeres) junto con su nombre.
El jugador debe identificar qué rostro corresponde al nombre indicado, hacer click en la imagen y luego presionar "Continue" para validar su elección. El jugador tiene tantos intentos como rostros hay: si hay 4 imágenes, de 1 a 4 opciones; si hay 6 rostros, de 1 a 6 opciones.

Cuando acierta, se muestra un resumen con porcentaje de aciertos/errores y el tiempo que tardó en encontrar el rostro.

**Los datos de las personas (nombre, foto, cargo) provienen de la API externa de WillowTree:**
`GET https://namegame.willowtreeapps.com/api/v1.0/profiles`

---

## 2. Estructura Real del Person (API externa)

```json
{
  "id": "3WCYqVR963Q4hB7pH9YVxe",
  "type": "people",
  "slug": "al-zoubi",
  "firstName": "Ameir",
  "lastName": "Al-Zoubi",
  "jobTitle": "Staff Software Engineer",
  "bio": "...",
  "socialLinks": [],
  "headshot": {
    "id": "79PkYrx56H2EhTJDmIxBTk",
    "type": "image",
    "mimeType": "image/jpeg",
    "url": "https://namegame.willowtreeapps.com/images/ameir.jpeg",
    "alt": "ameir al-zoubi",
    "width": 500,
    "height": 500
  }
}
```

**Campos clave para el juego:**
| Campo | Uso |
|-------|-----|
| `id` | Identificador único (String, no UUID) |
| `firstName` + `lastName` | Nombre mostrado al jugador |
| `headshot.url` | URL directa de la foto (sin S3 propio) |
| `jobTitle` | Dato adicional / subtítulo |

**Campos no disponibles en la API:** género (`gender`), estado activo (`active`).

---

## 3. Arquitectura Hexagonal

```
┌──────────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE LAYER                        │
│                                                                  │
│  ┌───────────────┐  ┌──────────────────┐  ┌───────────────────┐  │
│  │  REST API     │  │  JPA Adapters    │  │  WillowTree API   │  │
│  │  (Controllers)│  │  (Repositories)  │  │  (HTTP Client)    │  │
│  └──────┬────────┘  └────────┬─────────┘  └─────────┬─────────┘  │
│         │                   │                      │             │
├─────────┼───────────────────┼──────────────────────┼─────────────┤
│         │          APPLICATION LAYER                │            │
│         ▼                   ▼                      ▼             │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │              USE CASES (Application Services)            │    │
│  │                                                          │    │
│  │  • StartGameUseCase                                      │    │
│  │  • GetRoundUseCase                                       │    │
│  │  • SubmitAnswerUseCase                                   │    │
│  │  • GetGameResultsUseCase                                 │    │
│  └────────────────────────┬─────────────────────────────────┘    │
│                           │                                      │
├───────────────────────────┼──────────────────────────────────────┤
│                    DOMAIN LAYER                                  │
│                           ▼                                      │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │                   DOMAIN MODEL                           │    │
│  │                                                          │    │
│  │  Entities:  Game, Round, Person                          │    │
│  │  Value Objects: GameResult, RoundAnswer                  │    │
│  │  Enums: GameStatus, RoundStatus                          │    │
│  │                                                          │    │
│  │  PORTS (interfaces):                                     │    │
│  │    in:  StartGamePort, SubmitAnswerPort,                 │    │
│  │         GetRoundPort, GetGameResultsPort                 │    │
│  │    out: GameRepositoryPort, PersonApiPort                │    │
│  └──────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────┘
```

---

## 4. Estructura de Paquetes

```
com.namegame
├── domain
│   ├── model
│   │   ├── Game.java
│   │   ├── Round.java
│   │   ├── Person.java
│   │   ├── GameResult.java          (Value Object)
│   │   └── RoundAnswer.java         (Value Object)
│   ├── enums
│   │   ├── GameStatus.java          (CREATED, IN_PROGRESS, FINISHED)
│   │   └── RoundStatus.java         (PENDING, ANSWERED)
│   ├── exception
│   │   ├── GameNotFoundException.java
│   │   ├── GameAlreadyFinishedException.java
│   │   ├── RoundNotFoundException.java
│   │   ├── RoundAlreadyAnsweredException.java
│   │   └── InsufficientPeopleException.java
│   └── port
│       ├── in
│       │   ├── StartGamePort.java
│       │   ├── GetRoundPort.java
│       │   ├── SubmitAnswerPort.java
│       │   └── GetGameResultsPort.java
│       └── out
│           ├── GameRepositoryPort.java
│           └── PersonApiPort.java
│
├── application
│   ├── usecase
│   │   ├── StartGameUseCase.java
│   │   ├── GetRoundUseCase.java
│   │   ├── SubmitAnswerUseCase.java
│   │   └── GetGameResultsUseCase.java
│   └── dto
│       ├── StartGameCommand.java
│       ├── SubmitAnswerCommand.java
│       ├── RoundResponse.java
│       └── GameResultResponse.java
│
└── infrastructure
    ├── adapter
    │   ├── in
    │   │   └── rest
    │   │       ├── GameController.java
    │   │       └── mapper
    │   │           └── GameRestMapper.java
    │   └── out
    │       ├── persistence
    │       │   ├── entity
    │       │   │   ├── GameEntity.java
    │       │   │   └── RoundEntity.java
    │       │   ├── repository
    │       │   │   ├── GameJpaRepository.java
    │       │   │   └── RoundJpaRepository.java
    │       │   ├── mapper
    │       │   │   └── PersistenceMapper.java
    │       │   └── adapter
    │       │       └── GamePersistenceAdapter.java
    │       └── external
    │           ├── WillowTreePersonAdapter.java   ← implementa PersonApiPort
    │           └── dto
    │               ├── WillowTreePersonDto.java
    │               └── WillowTreeHeadshotDto.java
    └── config
        ├── BeanConfig.java
        ├── CorsConfig.java
        └── WebClientConfig.java
```

---

## 5. Modelo de Dominio

### 5.1 Game (Aggregate Root)

```java
public class Game {
    private UUID id;
    private Integer totalRounds;
    private Integer facesPerRound;       // N rostros mostrados por ronda
    private GameStatus status;
    private List<Round> rounds;
    private Instant createdAt;
    private Instant finishedAt;

    public Round getCurrentRound() { ... }
    public void advanceToNextRound() { ... }
    public void finish() { ... }
    public GameResult calculateResult() { ... }
}
```

### 5.2 Round (Entity)

```java
public class Round {
    private UUID id;
    private Integer roundNumber;
    private String targetPersonId;           // ID string de la API externa
    private List<String> optionPersonIds;    // IDs de los rostros ofrecidos
    private RoundStatus status;
    private String selectedPersonId;         // rostro elegido por el jugador
    private Boolean correct;
    private Instant presentedAt;
    private Instant answeredAt;
    private Long reactionTimeMillis;

    public void submitAnswer(String selectedPersonId, Instant answeredAt) {
        this.selectedPersonId = selectedPersonId;
        this.answeredAt = answeredAt;
        this.reactionTimeMillis = Duration.between(presentedAt, answeredAt).toMillis();
        this.correct = targetPersonId.equals(selectedPersonId);
        this.status = RoundStatus.ANSWERED;
    }
}
```

### 5.3 Person (Value Object — datos de la API externa)

```java
public record Person(
    String id,
    String firstName,
    String lastName,
    String jobTitle,
    String photoUrl     // headshot.url
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}
```

### 5.4 GameResult (Value Object)

```java
public record GameResult(
    UUID gameId,
    int totalRounds,
    int correctAnswers,
    int incorrectAnswers,
    double correctPercentage,
    double incorrectPercentage,
    List<RoundAnswer> roundDetails,
    long totalTimeMillis,
    double averageReactionTimeMillis
) {}
```

### 5.5 RoundAnswer (Value Object)

```java
public record RoundAnswer(
    int roundNumber,
    String targetName,
    String selectedName,
    boolean correct,
    long reactionTimeMillis
) {}
```

---

## 6. Tablas PostgreSQL

Solo se persisten Game y Round. Los datos de Person se obtienen on-demand de la API externa.

### 6.1 Diagrama ER

```
┌──────────────┐       ┌───────────────────────┐
│    games     │       │       rounds          │
├──────────────┤       ├───────────────────────┤
│ id (PK)      │◄──────│ game_id (FK)          │
│ total_rounds │       │ id (PK)               │
│ faces_per_   │       │ round_number          │
│   round      │       │ target_person_id      │  ← String ID de API externa
│ status       │       │ option_person_ids     │  ← TEXT[] con IDs
│ created_at   │       │ selected_person_id    │  ← String ID de API externa
│ finished_at  │       │ status                │
│ updated_at   │       │ correct               │
└──────────────┘       │ presented_at          │
                       │ answered_at           │
                       │ reaction_time_millis  │
                       │ created_at            │
                       └───────────────────────┘
```

### 6.2 DDL (Flyway Migration)

```sql
-- V1__create_games_table.sql

CREATE TABLE games (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    total_rounds    INTEGER      NOT NULL CHECK (total_rounds > 0),
    faces_per_round INTEGER      NOT NULL CHECK (faces_per_round >= 2),
    status          VARCHAR(20)  NOT NULL DEFAULT 'CREATED'
                    CHECK (status IN ('CREATED', 'IN_PROGRESS', 'FINISHED')),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    finished_at     TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_games_status ON games (status);


-- V2__create_rounds_table.sql

CREATE TABLE rounds (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id              UUID        NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    round_number         INTEGER     NOT NULL CHECK (round_number > 0),
    target_person_id     TEXT        NOT NULL,   -- ID string de la API externa
    option_person_ids    TEXT[]      NOT NULL,   -- array de IDs string
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING', 'ANSWERED')),
    selected_person_id   TEXT,
    correct              BOOLEAN,
    presented_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    answered_at          TIMESTAMPTZ,
    reaction_time_millis BIGINT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_game_round UNIQUE (game_id, round_number)
);

CREATE INDEX idx_rounds_game_id ON rounds (game_id);
CREATE INDEX idx_rounds_status  ON rounds (status);
```

---

## 7. Ports (Interfaces del Dominio)

### 7.1 Input Ports

```java
public interface StartGamePort {
    Game startGame(int totalRounds, int facesPerRound);
}

public interface GetRoundPort {
    Round getRound(UUID gameId, int roundNumber);
}

public interface SubmitAnswerPort {
    Round submitAnswer(UUID gameId, int roundNumber,
                       String selectedPersonId, Instant clientTimestamp);
}

public interface GetGameResultsPort {
    GameResult getResults(UUID gameId);
}
```

### 7.2 Output Ports

```java
public interface GameRepositoryPort {
    Game save(Game game);
    Optional<Game> findById(UUID id);
}

// Consume la API externa de WillowTree
public interface PersonApiPort {
    List<Person> fetchAll();                         // GET /api/v1.0/profiles
    Optional<Person> findById(String personId);
    List<Person> findAllByIds(List<String> ids);
}
```

---

## 8. REST API — Endpoints

### Base Path: `/api/v1`

---

### 8.1 POST `/api/v1/games` — Iniciar partida

**Request:**
```json
{
  "totalRounds": 6,
  "facesPerRound": 6
}
```

**Response 201 Created:**
```json
{
  "gameId": "a1b2c3d4-...",
  "totalRounds": 6,
  "facesPerRound": 6,
  "status": "IN_PROGRESS",
  "currentRound": 1,
  "createdAt": "2026-04-07T15:00:00Z"
}
```

**Lógica del Use Case:**
1. Llamar a la API externa (`PersonApiPort.fetchAll()`).
2. Validar que existan suficientes personas (`>= facesPerRound`).
3. Para cada ronda: seleccionar aleatoriamente N personas, elegir una como `target`.
4. Persistir Game + Rounds (solo los IDs de personas, no los datos).
5. Retornar gameId y metadata.

---

### 8.2 GET `/api/v1/games/{gameId}/rounds/{roundNumber}` — Obtener ronda

**Response 200 OK:**
```json
{
  "gameId": "a1b2c3d4-...",
  "roundNumber": 1,
  "totalRounds": 6,
  "targetName": "Ameir Al-Zoubi",
  "faces": [
    {
      "personId": "3WCYqVR963Q4hB7pH9YVxe",
      "photoUrl": "https://namegame.willowtreeapps.com/images/ameir.jpeg",
      "alt": "ameir al-zoubi"
    },
    {
      "personId": "pBcwRVa0782lEA34jjQKn",
      "photoUrl": "https://namegame.willowtreeapps.com/images/amer_josh.png",
      "alt": "Josh Amer"
    }
  ],
  "status": "PENDING",
  "presentedAt": "2026-04-07T15:00:05Z"
}
```

**Lógica:** Cargar Round de BD, enriquecer `faces` con datos de la API externa usando los `option_person_ids` almacenados.

---

### 8.3 POST `/api/v1/games/{gameId}/rounds/{roundNumber}/answer` — Enviar respuesta

**Request:**
```json
{
  "selectedPersonId": "3WCYqVR963Q4hB7pH9YVxe",
  "clientTimestamp": "2026-04-07T15:00:12.345Z"
}
```

**Response 200 OK:**
```json
{
  "roundNumber": 1,
  "correct": true,
  "targetPersonId": "3WCYqVR963Q4hB7pH9YVxe",
  "targetName": "Ameir Al-Zoubi",
  "selectedPersonId": "3WCYqVR963Q4hB7pH9YVxe",
  "selectedName": "Ameir Al-Zoubi",
  "reactionTimeMillis": 7345,
  "hasNextRound": true,
  "nextRoundNumber": 2
}
```

**Lógica:**
1. Validar que la ronda esté en estado PENDING.
2. Calcular `reactionTimeMillis` server-side (`NOW() - presentedAt`).
3. Marcar Round como ANSWERED.
4. Si es la última ronda, marcar Game como FINISHED.

---

### 8.4 GET `/api/v1/games/{gameId}/results` — Resultados finales

**Response 200 OK:**
```json
{
  "gameId": "a1b2c3d4-...",
  "totalRounds": 6,
  "correctAnswers": 4,
  "incorrectAnswers": 2,
  "correctPercentage": 66.67,
  "incorrectPercentage": 33.33,
  "totalTimeMillis": 42150,
  "averageReactionTimeMillis": 7025.0,
  "rounds": [
    {
      "roundNumber": 1,
      "targetName": "Ameir Al-Zoubi",
      "selectedName": "Ameir Al-Zoubi",
      "correct": true,
      "reactionTimeMillis": 7345
    },
    {
      "roundNumber": 2,
      "targetName": "Josh Amer",
      "selectedName": "Daniel Atwood",
      "correct": false,
      "reactionTimeMillis": 5200
    }
  ],
  "finishedAt": "2026-04-07T15:02:30Z"
}
```

**Validación:** Solo disponible cuando `status = FINISHED`.

---

## 9. Flujo Completo del Juego

```
                    FRONTEND                              BACKEND
                    ───────                              ───────
    1. Click "Start Game"
       ───────────────────────────►  POST /api/v1/games
                                     { totalRounds: 6, facesPerRound: 6 }
                                          │
                                          ▼
                                     Llama WillowTree API
                                     GET /api/v1.0/profiles
                                     Selecciona personas al azar
                                     Persiste Game + Rounds
       ◄───────────────────────────  201 { gameId, currentRound: 1 }

    2. Carga ronda 1
       ───────────────────────────►  GET /games/{id}/rounds/1
                                          │
                                          ▼
                                     Lee Round de BD
                                     Enriquece con datos de API externa
       ◄───────────────────────────  200 { targetName, faces[], presentedAt }

    3. ⏱ El jugador observa los rostros...
       Click en un rostro → selecciona personId
       Click "Continue"
       ───────────────────────────►  POST /games/{id}/rounds/1/answer
                                     { selectedPersonId, clientTimestamp }
       ◄───────────────────────────  200 { correct, reactionTimeMillis,
                                           hasNextRound: true, nextRound: 2 }

    4. Repetir pasos 2-3 para rondas 2..N

    5. Última ronda respondida (hasNextRound: false)
       ───────────────────────────►  GET /games/{id}/results
       ◄───────────────────────────  200 { correctPercentage, incorrectPercentage,
                                           rounds[].reactionTimeMillis, ... }

    6. Frontend muestra resumen final
```

---

## 10. Decisiones Técnicas

**Stack:** Java 17, Spring Boot 3.x, PostgreSQL 15+, Flyway, Docker Compose, WebClient (Spring WebFlux).

**Fuente de personas:** La lista de personas viene de la API pública de WillowTree (`https://namegame.willowtreeapps.com/api/v1.0/profiles`). El backend la consume al iniciar cada partida. No se gestiona CRUD propio de personas.

**IDs de personas:** Son Strings opacos (ej. `"3WCYqVR963Q4hB7pH9YVxe"`), no UUIDs. Se almacenan como `TEXT` en PostgreSQL.

**Fotos:** Las URLs de las fotos provienen directamente del campo `headshot.url` de la API externa. No se usa S3 propio.

**Tiempo de reacción:** Se calcula server-side (`NOW() - presentedAt`) para evitar manipulación del cliente. El `clientTimestamp` se guarda como dato de auditoría.

**Selección aleatoria de personas:** Se obtiene la lista completa de la API externa y se hace shuffle en memoria (la lista es pequeña, del orden de decenas/cientos de personas).

**Array de IDs en Rounds:** Se usa `TEXT[]` de PostgreSQL para `option_person_ids`, evitando una tabla intermedia innecesaria.

**Idempotencia en `GET /rounds/{n}`:** Si `presentedAt` ya fue seteado, no se sobreescribe en llamadas subsiguientes (el jugador puede refrescar la página sin afectar el timer).

**Cache de la API externa:** Se puede agregar un cache en memoria (Caffeine) con TTL de 5-10 minutos para evitar llamar a la API de WillowTree en cada `startGame`.

---

## 11. Consideraciones Futuras

- **Cache Redis:** Para la lista de personas de WillowTree en entornos distribuidos.
- **Autenticación y ranking:** Agregar `user_id` al Game para trackear jugadores y armar leaderboards.
- **Dificultad dinámica:** Variar `facesPerRound` según el nivel o score acumulado.
- **Modo por jobTitle:** Filtrar personas por cargo (ej. solo "Software Engineer").
- **WebSocket:** Notificaciones en tiempo real para modo multijugador.
