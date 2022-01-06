package hackathon.tidb.TiAPI.dao;

import hackathon.tidb.TiAPI.model.UserToTiDB;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserToTiDBRepository extends ReactiveCrudRepository<UserToTiDB, Integer> {
    Mono<UserToTiDB> findByUsername(String username);
}
