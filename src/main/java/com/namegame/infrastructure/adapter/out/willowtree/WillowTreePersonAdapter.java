package com.namegame.infrastructure.adapter.out.willowtree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namegame.domain.model.Person;
import com.namegame.domain.port.out.PersonRepositoryPort;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class WillowTreePersonAdapter implements PersonRepositoryPort {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RestClient restClient;
    private final String profilesUrl;

    private final AtomicReference<List<Person>> cacheRef = new AtomicReference<>();
    private final AtomicReference<Map<UUID, Person>> indexRef = new AtomicReference<>();

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
        return Optional.ofNullable(getIndex().get(id));
    }

    @Override
    public List<Person> findAllByIds(List<UUID> ids) {
        Map<UUID, Person> index = getIndex();
        return ids.stream()
                .map(index::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public long countActive() {
        return getProfiles().size();
    }

    private List<Person> getProfiles() {
        List<Person> cached = cacheRef.get();
        if (cached != null) return cached;

        synchronized (this) {
            cached = cacheRef.get();           // segunda lectura dentro del lock
            if (cached != null) return cached;

            cached = loadFromApi();
            cacheRef.set(cached);

            Map<UUID, Person> index = new HashMap<>(cached.size() * 2);
            for (Person p : cached) index.put(p.getId(), p);
            indexRef.set(Collections.unmodifiableMap(index));

            return cached;
        }
    }

    private Map<UUID, Person> getIndex() {
        Map<UUID, Person> index = indexRef.get();
        if (index != null) return index;
        getProfiles();                         // dispara la carga si todavía no ocurrió
        return indexRef.get();
    }

    // --- extracción de lo que ya estaba en getProfiles() ---

    private List<Person> loadFromApi() {
        String json = restClient.get()
                .uri(profilesUrl)
                .retrieve()
                .body(String.class);

        try {
            List<WillowTreeProfile> profiles = json == null ? List.of()
                    : MAPPER.readValue(json, new TypeReference<>() {});
            return profiles.stream()
                    .filter(p -> p.id() != null && p.firstName() != null)
                    .map(this::toPerson)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse WillowTree profiles response", e);
        }
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
