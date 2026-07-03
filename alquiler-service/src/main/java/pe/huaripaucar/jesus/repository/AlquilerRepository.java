package pe.huaripaucar.jesus.repository;

import pe.huaripaucar.jesus.model.Alquiler;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface AlquilerRepository extends ReactiveMongoRepository<Alquiler, String> {

    Flux<Alquiler> findByClienteId(String clienteId);

    Flux<Alquiler> findByVehiculoId(String vehiculoId);
}
