package pe.huaripaucar.jesus.repository;

import pe.huaripaucar.jesus.model.Cliente;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ClienteRepository extends ReactiveMongoRepository<Cliente, String> {

    Mono<Cliente> findByDni(String dni);

    Mono<Boolean> existsByDni(String dni);
}
