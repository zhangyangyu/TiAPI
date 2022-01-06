package hackathon.tidb.TiAPI.controller;

import hackathon.tidb.TiAPI.model.ExecuteSQLRequest;
import hackathon.tidb.TiAPI.model.ExecuteSQLResponse;
import hackathon.tidb.TiAPI.service.TiDBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class SqlController implements SqlApi {

    @Autowired
    private TiDBService tiDBService;

    @Override
    public Mono<ResponseEntity<ExecuteSQLResponse>> executeSQL(Mono<ExecuteSQLRequest> executeSQLRequest, ServerWebExchange exchange) {
        return exchange.getSession().flatMap(webSession -> {
            if (!webSession.isStarted()) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "unauthorized access"));
            }
            return Mono.just(webSession);
        })
                .flatMap(webSession -> executeSQLRequest.flatMap(request -> tiDBService.executeSQL(webSession, request)))
                .map(executeSQLResponse -> new ResponseEntity<>(executeSQLResponse, HttpStatus.OK));
    }
}