package com.namegame.application.usecase;

import com.namegame.application.dto.RoundResponse;
import com.namegame.domain.enums.Gender;
import com.namegame.domain.enums.RoundStatus;
import com.namegame.domain.exception.GameNotFoundException;
import com.namegame.domain.model.*;
import com.namegame.domain.port.out.GameRepositoryPort;
import com.namegame.domain.port.out.PersonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetRoundUseCaseTest {

    @Mock
    private GameRepositoryPort gameRepository;

    @Mock
    private PersonRepositoryPort personRepository;

    @InjectMocks
    private GetRoundUseCase useCase;

    private UUID gameId;
    private Game mockGame;
    private Round mockRound;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        mockGame = mock(Game.class);
        mockRound = new Round(); // Usamos objeto real para testear setters
        mockRound.setRoundNumber(1);
        mockRound.setStatus(RoundStatus.PENDING);
    }

    @Test
    @DisplayName("Should set presentedAt and save game when accessing round for the first time")
    void getRound_FirstAccess_SetsPresentedAtAndSaves() {
        // GIVEN
        mockRound.setPresentedAt(null);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(mockGame.getRoundByNumber(1)).thenReturn(mockRound);

        // WHEN
        Round result = useCase.getRound(gameId, 1);

        // THEN
        assertThat(result.getPresentedAt()).isNotNull();
        verify(gameRepository, times(1)).save(mockGame);
    }

    @Test
    @DisplayName("Should NOT save game if presentedAt is already set (Idempotency)")
    void getRound_SecondAccess_DoesNotSaveAgain() {
        // GIVEN
        Instant alreadySet = Instant.now().minusSeconds(10);
        mockRound.setPresentedAt(alreadySet);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(mockGame.getRoundByNumber(1)).thenReturn(mockRound);

        // WHEN
        Round result = useCase.getRound(gameId, 1);

        // THEN
        assertThat(result.getPresentedAt()).isEqualTo(alreadySet);
        verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return a valid RoundResponse with mapped faces and names")
    void getRoundResponse_Success() {
        // GIVEN
        UUID targetId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        List<UUID> options = List.of(targetId, otherId);

        mockRound.setTargetPersonId(targetId);
        mockRound.setOptionPersonIds(options);
        mockRound.setPresentedAt(Instant.now());

        Person targetPerson = new Person(targetId, "John", "Doe", Gender.MALE, "url1", true, Instant.now());
        Person otherPerson = new Person(otherId, "Jane", "Smith", Gender.FEMALE, "url2", true, Instant.now());

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(mockGame.getRoundByNumber(1)).thenReturn(mockRound);
        when(mockGame.getTotalRounds()).thenReturn(10);
        when(personRepository.findAllByIds(options)).thenReturn(List.of(targetPerson, otherPerson));

        // WHEN
        RoundResponse response = useCase.getRoundResponse(gameId, 1);

        // THEN
        assertThat(response.gameId()).isEqualTo(gameId);
        assertThat(response.targetName()).isEqualTo("John Doe");
        assertThat(response.totalRounds()).isEqualTo(10);
        assertThat(response.faces()).hasSize(2);

        // Verificar que las URLs de las fotos están en el DTO
        assertThat(response.faces()).extracting("photoUrl")
                .containsExactlyInAnyOrder("url1", "url2");

        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("Should throw GameNotFoundException when ID does not exist")
    void getRound_GameNotFound_ThrowsException() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getRound(gameId, 1))
                .isInstanceOf(GameNotFoundException.class);
    }
}
