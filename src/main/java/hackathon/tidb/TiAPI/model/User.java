package hackathon.tidb.TiAPI.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/* create table user(id int auto_increment primary key, username varchar(255) not null, password varchar(255) not null); */
@lombok.Data
@lombok.RequiredArgsConstructor
@Table("user")
public class User {
    @Id
    private int id;
    private final String username;
    private final String password;
}
