package hackathon.tidb.TiAPI.controller;

import hackathon.tidb.TiAPI.dao.UserRepository;
import hackathon.tidb.TiAPI.model.AuthUserRequest;
import hackathon.tidb.TiAPI.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@RestController
public class UserController implements UserApi {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Mono<ResponseEntity<Void>> authUser(Mono<AuthUserRequest> authUserRequest, ServerWebExchange exchange) {
        return authUserRequest.flatMap(request ->
                        userRepository.findByUsername(request.getUsername())
                                .flatMap(user -> {
                                    if (!user.getPassword().equals(request.getPassword())) {
                                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong username or password"));
                                    }
                                    return Mono.just(Tuples.of(user, false));
                                })
                                .switchIfEmpty(userRepository.save(new User(request.getUsername(), request.getPassword())).map(user -> Tuples.of(user, true)))
                                .flatMap(tuple -> exchange.getSession()
                                        .doOnNext(webSession -> webSession.getAttributes().put("user", tuple.getT1().getUsername()))
                                        .doOnNext(webSession -> webSession.getAttributes().put("database", request.getDatabase()))
                                        .thenReturn(tuple.getT2())
                                )
                                .map(created -> created ? HttpStatus.CREATED : HttpStatus.OK)
                                .map(ResponseEntity::new)
                );
    }
}