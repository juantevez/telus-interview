package com.namegame.domain.port.out;

import com.namegame.domain.model.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepositoryPort {

    List<Person> findRandomActive(int count);

    Optional<Person> findById(UUID id);

    List<Person> findAllByIds(List<UUID> ids);

    long countActive();
}
