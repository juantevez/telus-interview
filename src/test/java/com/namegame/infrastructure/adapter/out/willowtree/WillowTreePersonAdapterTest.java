package com.namegame.infrastructure.adapter.out.willowtree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namegame.domain.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WillowTreePersonAdapterTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RestClient restClient;

    private final String profilesUrl = "http://api.test/profiles";
    private WillowTreePersonAdapter adapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        adapter = new WillowTreePersonAdapter(restClient, profilesUrl);
    }

    @Test
    void findRandomActive_ShouldReturnRequestedCountAndUseCache() throws JsonProcessingException {
        // Arrange
        String jsonResponse = objectMapper.writeValueAsString(List.of(
                new WillowTreeProfileStub("1", "John", "Doe"),
                new WillowTreeProfileStub("2", "Jane", "Smith")
        ));

        // CAMBIO AQUÍ: Usamos doReturn para que no cuente la llamada durante la configuración
        // Como es un Deep Stub, necesitamos obtener el mock intermedio o simplificar:

        var uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        var headersSpec = mock(RestClient.RequestHeadersSpec.class);
        var responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(profilesUrl)).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn(jsonResponse);

        // Limpiamos cualquier invocación accidental durante el setup
        clearInvocations(restClient);

        // Act
        adapter.findRandomActive(2); // Primera llamada (llena cache)
        adapter.findRandomActive(1); // Segunda llamada (usa cache)

        // Assert
        verify(restClient, times(1)).get();
    }

    @Test
    void findById_ShouldReturnCorrectPerson() throws JsonProcessingException {
        // Arrange
        String idRaw = "unique-id-123";
        // Calculamos el UUID tal cual lo hace el adaptador internamente
        UUID expectedUuid = UUID.nameUUIDFromBytes(idRaw.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        String jsonResponse = objectMapper.writeValueAsString(List.of(
                new WillowTreeProfileStub(idRaw, "Target", "Person")
        ));

        when(restClient.get().uri(profilesUrl).retrieve().body(String.class)).thenReturn(jsonResponse);

        // Act
        Optional<Person> result = adapter.findById(expectedUuid);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("Target");
        assertThat(result.get().getId()).isEqualTo(expectedUuid);
    }

    @Test
    void findAllByIds_ShouldReturnFilteredList() throws JsonProcessingException {
        // Arrange
        String jsonResponse = objectMapper.writeValueAsString(List.of(
                new WillowTreeProfileStub("id1", "A", "A"),
                new WillowTreeProfileStub("id2", "B", "B"),
                new WillowTreeProfileStub("id3", "C", "C")
        ));
        when(restClient.get().uri(profilesUrl).retrieve().body(String.class)).thenReturn(jsonResponse);

        UUID uuid1 = UUID.nameUUIDFromBytes("id1".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        UUID uuid3 = UUID.nameUUIDFromBytes("id3".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // Act
        List<Person> result = adapter.findAllByIds(List.of(uuid1, uuid3));

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Person::getFirstName).containsExactlyInAnyOrder("A", "C");
    }

    // Stub para generar el JSON fácilmente en el test
    private record WillowTreeProfileStub(String id, String firstName, String lastName) {}
}
