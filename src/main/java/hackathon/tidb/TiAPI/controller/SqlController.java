package hackathon.tidb.TiAPI.controller;

import hackathon.tidb.TiAPI.connection.ConnectionUtil;
import hackathon.tidb.TiAPI.model.ExecuteSQLRequest;
import hackathon.tidb.TiAPI.model.ExecuteSQLResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class SqlController implements SqlApi {

    @Override
    public Mono<ResponseEntity<ExecuteSQLResponse>> executeSQL(Mono<ExecuteSQLRequest> executeSQLRequest, ServerWebExchange exchange) {
        return ConnectionUtil.get("database")
                .flatMap(connection ->
                        SqlApi.super.executeSQL(executeSQLRequest, exchange)
                );
    }
}