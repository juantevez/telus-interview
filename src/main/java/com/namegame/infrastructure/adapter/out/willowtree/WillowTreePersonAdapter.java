package com.namegame.infrastructure.adapter.out.willowtree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.namegame.domain.model.Person;
import com.namegame.domain.port.out.PersonRepositoryPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class WillowTreePersonAdapter implements PersonRepositoryPort {

    private final RestClient restClient;
    private final String profilesUrl;
    private volatile List<Person> cache;

    public WillowTreePersonAdapter(RestClient restClient, String profilesUrl) {
        this.restClient = restClient;
        this.profilesUrl = profilesUrl;
    }

    @Override
    public List<Person> findRandomActive(int count) {
        List<Person> shuffled = new ArrayList<>(getProfiles());
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(count, shuffled.size()));
    }

    @Override
    public Optional<Person> findById(UUID id) {
        return getProfiles().stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    @Override
    public List<Person> findAllByIds(List<UUID> ids) {
        Set<UUID> idSet = new HashSet<>(ids);
        return getProfiles().stream().filter(p -> idSet.contains(p.getId())).toList();
    }

    @Override
    public long countActive() {
        return getProfiles().size();
    }

    private synchronized List<Person> getProfiles() {
        if (cache == null) {
            List<WillowTreeProfile> profiles = restClient.get()
                    .uri(profilesUrl)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            cache = profiles == null ? List.of() : profiles.stream()
                    .filter(p -> p.id() != null && p.firstName() != null)
                    .map(this::toPerson)
                    .toList();
        }
        return cache;
    }

    private Person toPerson(WillowTreeProfile p) {
        UUID id = UUID.nameUUIDFromBytes(p.id().getBytes(StandardCharsets.UTF_8));
        String photoUrl = p.headshot() != null ? p.headshot().url() : null;
        return new Person(id, p.firstName(), p.lastName(), null, photoUrl, true, null);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record WillowTreeProfile(String id, String firstName, String lastName, Headshot headshot) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        record Headshot(String url) {}
    }
}
