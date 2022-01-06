package hackathon.tidb.TiAPI.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/* create table usertotidb(id int auto_increment primary key, username varchar(255) not null, tidb_id int not null, created bool); */

@lombok.Data
@lombok.RequiredArgsConstructor
@Table("usertotidb")
public class UserToTiDB {
    @Id
    private int id;
    final private String username;
    final private int tidbId;
    boolean created;
}
