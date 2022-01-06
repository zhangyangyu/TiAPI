package hackathon.tidb.TiAPI.dao;

import hackathon.tidb.TiAPI.model.TiDB;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TiDBRepository extends ReactiveCrudRepository<TiDB, Integer> {
}
