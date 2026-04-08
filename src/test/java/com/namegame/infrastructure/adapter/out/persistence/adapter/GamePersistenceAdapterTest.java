package com.namegame.infrastructure.adapter.out.persistence.adapter;

import com.namegame.infrastructure.adapter.out.persistence.entity.GameEntity;
import com.namegame.infrastructure.adapter.out.persistence.repository.GameJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.namegame.domain.model.Game;
import com.namegame.domain.enums.GameStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamePersistenceAdapterTest {

    @Mock
    private GameJpaRepository jpaRepository;

    private GamePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GamePersistenceAdapter(jpaRepository);
    }

    @Test
    void save_ShouldMapToEntitySaveAndReturnDomain() {
        // Arrange
        UUID gameId = UUID.randomUUID();

        // Usamos un objeto real o un mock con comportamiento definido
        Game gameDomain = mock(Game.class);
        when(gameDomain.getId()).thenReturn(gameId);
        when(gameDomain.getStatus()).thenReturn(GameStatus.IN_PROGRESS); // Evita el primer NPE
        when(gameDomain.getTotalRounds()).thenReturn(10);
        when(gameDomain.getFacesPerRound()).thenReturn(4);

        GameEntity savedEntity = new GameEntity();
        savedEntity.setId(gameId);
        savedEntity.setStatus(GameStatus.IN_PROGRESS.toString());
        // Rellenar otros campos obligatorios que el mapper use (ej. createdAt)

        when(jpaRepository.save(any(GameEntity.class))).thenReturn(savedEntity);

        // Act
        Game result = adapter.save(gameDomain);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gameId);
        verify(jpaRepository).save(any(GameEntity.class));
    }

    @Test
    void findById_WhenExists_ShouldReturnOptionalWithDomain() {
        // Arrange
        UUID id = UUID.randomUUID();
        GameEntity entity = new GameEntity();
        entity.setId(id);
        entity.setStatus(GameStatus.IN_PROGRESS.toString());
        // El error "Name is null" sugiere que tu mapper o GameStatus.valueOf() falló.
        // Asegúrate de que el entity tenga los datos mínimos:
        // entity.set...

        when(jpaRepository.findById(id)).thenReturn(Optional.of(entity));

        // Act
        Optional<Game> result = adapter.findById(id);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
    }
}
