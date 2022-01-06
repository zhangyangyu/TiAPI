package hackathon.tidb.TiAPI.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import dev.miku.r2dbc.mysql.constant.SslMode;
import hackathon.tidb.TiAPI.dao.TiDBRepository;
import hackathon.tidb.TiAPI.dao.UserToTiDBRepository;
import hackathon.tidb.TiAPI.model.ExecuteSQLRequest;
import hackathon.tidb.TiAPI.model.ExecuteSQLResponse;
import hackathon.tidb.TiAPI.model.TiDB;
import hackathon.tidb.TiAPI.model.UserToTiDB;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.R2dbcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class TiDBService {

    @Autowired
    private TiDBRepository tiDBRepository;

    @Autowired
    private UserToTiDBRepository userToTiDBRepository;

    private final Cache<String, Connection> connections = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(Duration.ofSeconds(1800)) // same as session inactive interval
            .build();

    private Mono<Connection> getTiDBAdminConnection(TiDB tiDB) {
        return Mono.just(MySqlConnectionConfiguration.builder()
                .host(tiDB.getHost())
                .port(tiDB.getPort())
                .user(tiDB.getAdminUsername())
                .password(tiDB.getAdminPassword())
                .sslMode(SslMode.DISABLED)
                .build())
                .map(MySqlConnectionFactory::from)
                .flatMap(MySqlConnectionFactory::create);
    }

    private Mono<Connection> getTiDBUserConnection(UserToTiDB userToTiDB, String database) {
        return Mono.justOrEmpty(connections.getIfPresent(userToTiDB.getUsername()))
                .switchIfEmpty(
                        tiDBRepository.findById(userToTiDB.getTidbId())
                                .map(tiDB -> MySqlConnectionConfiguration.builder()
                                        .host(tiDB.getHost())
                                        .port(tiDB.getPort())
                                        .user(userToTiDB.getUsername())
                                        .database(userToTiDB.getUsername() + "_" + database)
                                        .sslMode(SslMode.DISABLED)
                                        .build())
                                .map(MySqlConnectionFactory::from)
                                .flatMap(MySqlConnectionFactory::create)
                                .doOnNext(mySqlConnection -> connections.put(userToTiDB.getUsername(), mySqlConnection))
                );
    }

    public Mono<UserToTiDB> bindTiDB(String username, String database) {
        return userToTiDBRepository.findByUsername(username)
                .switchIfEmpty(tiDBRepository.findAll()
                        .collectList()
                        .map(tiDBS -> tiDBS.get(new Random().nextInt(tiDBS.size()))) // randomly choose one
                        .map(tiDB -> new UserToTiDB(username, tiDB.getId()))
                        .flatMap(userToTiDB -> userToTiDBRepository.save(userToTiDB))
                ).flatMap(userToTiDB -> {
                    if (!userToTiDB.isCreated()) {
                        return tiDBRepository.findById(userToTiDB.getTidbId())
                                .flatMap(this::getTiDBAdminConnection)
                                .flatMapMany(connection -> Flux.from(connection.createBatch()
                                        .add("CREATE USER " + username)
                                        .add("GRANT ALL PRIVILEGES ON `" + username + "_%` . * TO '" + username + "'@'%'")
                                        .add("CREATE DATABASE `" + username + "_" + database + "`")
                                        .execute()))
                                .then(Mono.fromCallable(() -> {
                                    userToTiDB.setCreated(true);
                                    return userToTiDB;
                                }).flatMap(userToTiDBRepository::save));
                    }
                    return Mono.just(userToTiDB);
                });
    }

    private Mono<ExecuteSQLResponse> executeSQLWithConnection(Connection connection, String statement) {
        return Flux.from(connection.createStatement(statement).execute())
                .flatMap(result -> result.map((row, rowMetadata) -> {
                    List<Object> array = new ArrayList<>(rowMetadata.getColumnNames().size());
                    for (int i = 0; i < rowMetadata.getColumnNames().size(); i++) {
                        array.add(row.get(i));
                    }
                    return array;
                }))
                .collectList()
                .map(result -> {
                    ExecuteSQLResponse response = new ExecuteSQLResponse();
                    response.setData(result);
                    return response;
                });
    }

    public Mono<ExecuteSQLResponse> executeSQL(WebSession webSession, ExecuteSQLRequest executeSQLRequest) {
        return userToTiDBRepository.findByUsername(webSession.getAttribute("username"))
                .flatMap(userToTiDB -> this.getTiDBUserConnection(userToTiDB, webSession.getAttribute("database")))
                .flatMap(connection -> executeSQLWithConnection(connection, executeSQLRequest.getStatement()))
                .onErrorResume(R2dbcException.class, error -> Mono.error(new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, error.getMessage())));
    }
}
