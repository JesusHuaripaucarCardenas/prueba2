package pe.huaripaucar.jesus.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Component
public class ClienteClient {

    private final WebClient webClient;

    public ClienteClient(@Value("${clientes.service.url}") String clienteServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(clienteServiceUrl).build();
    }

    public Mono<ClienteDTO> obtenerCliente(String clienteId) {
        return webClient.get()
                .uri("/api/clientes/{id}", clienteId)
                .retrieve()
                .bodyToMono(ClienteDTO.class)
                .onErrorMap(ex -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente no encontrado o servicio no disponible: " + clienteId));
    }
}
