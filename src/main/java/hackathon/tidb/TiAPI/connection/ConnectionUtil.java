package hackathon.tidb.TiAPI.connection;

import reactor.core.publisher.Mono;

public class ConnectionUtil {
    public static Mono<Connection> get(String database) {
        return getConnectionPool(database)
                .flatMap(ConnectionUtil::getConnection);
    }

    private static Mono<ConnectionPool> getConnectionPool(String database) {
        return Mono.empty();
    }

    private static Mono<Connection> getConnection(ConnectionPool connectionPool) {
        return Mono.empty();
    }
}
