package com.namegame.infrastructure.config;

import com.namegame.application.usecase.GetGameResultsUseCase;
import com.namegame.application.usecase.GetRoundUseCase;
import com.namegame.application.usecase.StartGameUseCase;
import com.namegame.application.usecase.SubmitAnswerUseCase;
import com.namegame.domain.port.out.GameRepositoryPort;
import com.namegame.domain.port.out.PersonRepositoryPort;
import com.namegame.infrastructure.adapter.in.rest.mapper.GameRestMapper;
import com.namegame.infrastructure.adapter.out.persistence.adapter.GamePersistenceAdapter;
import com.namegame.infrastructure.adapter.out.persistence.repository.GameJpaRepository;
import com.namegame.infrastructure.adapter.out.willowtree.WillowTreePersonAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BeanConfig {

    // --- Output port adapters ---

    @Bean
    public GameRepositoryPort gameRepositoryPort(GameJpaRepository jpaRepository) {
        return new GamePersistenceAdapter(jpaRepository);
    }

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean
    public PersonRepositoryPort personRepositoryPort(
            RestClient restClient,
            @Value("${willowtree.api.profiles-url}") String profilesUrl) {
        return new WillowTreePersonAdapter(restClient, profilesUrl);
    }

    // --- Use cases (input port implementations) ---

    @Bean
    public StartGameUseCase startGameUseCase(GameRepositoryPort gameRepository,
                                              PersonRepositoryPort personRepository) {
        return new StartGameUseCase(gameRepository, personRepository);
    }

    @Bean
    public GetRoundUseCase getRoundUseCase(GameRepositoryPort gameRepository,
                                            PersonRepositoryPort personRepository) {
        return new GetRoundUseCase(gameRepository, personRepository);
    }

    @Bean
    public SubmitAnswerUseCase submitAnswerUseCase(GameRepositoryPort gameRepository,
                                                    PersonRepositoryPort personRepository) {
        return new SubmitAnswerUseCase(gameRepository, personRepository);
    }

    @Bean
    public GetGameResultsUseCase getGameResultsUseCase(GameRepositoryPort gameRepository,
                                                        PersonRepositoryPort personRepository) {
        return new GetGameResultsUseCase(gameRepository, personRepository);
    }

    // --- REST layer ---

    @Bean
    public GameRestMapper gameRestMapper() {
        return new GameRestMapper();
    }
}
