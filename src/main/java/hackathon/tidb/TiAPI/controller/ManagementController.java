package hackathon.tidb.TiAPI.controller;

import hackathon.tidb.TiAPI.dao.TiDBRepository;
import hackathon.tidb.TiAPI.dao.UserRepository;
import hackathon.tidb.TiAPI.dao.UserToTiDBRepository;
import hackathon.tidb.TiAPI.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@RestController
public class ManagementController implements ManagementApi {

    @Autowired
    private TiDBRepository tiDBRepository;

    @Autowired
    private UserToTiDBRepository userToTiDBRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Mono<ResponseEntity<ListTiDBResponse>> listTiDB(ServerWebExchange exchange) {
        return tiDBRepository.findAll()
                .map(tiDB -> {
                    TiDBInstance instance = new TiDBInstance();
                    instance.setHost(tiDB.getHost());
                    instance.setPort(tiDB.getPort());
                    instance.setAdminUsername(tiDB.getAdminUsername());
                    instance.setAdminPassword(tiDB.getAdminPassword());
                    return instance;
                })
                .collectList()
                .map(tiDBS -> {
                    ListTiDBResponse response = new ListTiDBResponse();
                    response.setValue(tiDBS);
                    return response;
                })
                .map(listTiDBResponse -> new ResponseEntity<>(listTiDBResponse, HttpStatus.OK));
    }

    @Override
    public Mono<ResponseEntity<Void>> addTiDB(Mono<TiDBInstance> tiDBInstance, ServerWebExchange exchange) {
        return tiDBInstance.map(instance -> {
            TiDB tiDB = new TiDB();
            tiDB.setHost(instance.getHost());
            tiDB.setPort(instance.getPort());
            tiDB.setAdminUsername(instance.getAdminUsername());
            tiDB.setAdminPassword(instance.getAdminPassword());
            return tiDB;
        })
                .flatMap(tiDBRepository::save)
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
    }

    @Override
    public Mono<ResponseEntity<ListUserToTiDBResponse>> listUserToTiDB(ServerWebExchange exchange) {
        return userToTiDBRepository.findAll()
                .flatMap(userToTiDB -> tiDBRepository.findById(userToTiDB.getTidbId()).map(tiDB -> Tuples.of(userToTiDB, tiDB)))
                .map(t -> {
                    UserToTiDB userToTiDB = t.getT1();
                    TiDB tiDB = t.getT2();
                    UserToTiDBInstance userToTiDBInstance = new UserToTiDBInstance();
                    userToTiDBInstance.setUsername(userToTiDB.getUsername());
                    userToTiDBInstance.setCreated(userToTiDB.isCreated());
                    TiDBInstance tiDBInstance = new TiDBInstance();
                    tiDBInstance.setHost(tiDB.getHost());
                    tiDBInstance.setPort(tiDB.getPort());
                    tiDBInstance.setAdminUsername(tiDB.getAdminUsername());
                    tiDBInstance.setAdminPassword(tiDB.getAdminPassword());
                    userToTiDBInstance.setTidb(tiDBInstance);
                    return userToTiDBInstance;
                })
                .collectList()
                .map(userToTiDBInstances -> {
                    ListUserToTiDBResponse listUserToTiDBResponse = new ListUserToTiDBResponse();
                    listUserToTiDBResponse.setValue(userToTiDBInstances);
                    return listUserToTiDBResponse;
                })
                .map(listUserToTiDBResponse -> new ResponseEntity<>(listUserToTiDBResponse, HttpStatus.OK));
    }

    @Override
    public Mono<ResponseEntity<ListUserResponse>> listUser(ServerWebExchange exchange) {
        return userRepository.findAll()
                .map(user -> {
                    UserInstance instance = new UserInstance();
                    instance.setUsername(user.getUsername());
                    instance.setPassword(user.getPassword());
                    return instance;
                })
                .collectList()
                .map(userInstances -> {
                    ListUserResponse response = new ListUserResponse();
                    response.setValue(userInstances);
                    return response;
                })
                .map(listUserResponse -> new ResponseEntity<>(listUserResponse, HttpStatus.OK));
    }
}
