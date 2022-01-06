package hackathon.tidb.TiAPI.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/* create table tidb(id int auto_increment primary key, host varchar(255) not null, port int not null, admin_username varchar(255) not null, admin_password varchar(255) not null default ''); */

@lombok.Data
@Table("tidb")
public class TiDB {
    @Id
    private int id;
    private String host;
    private int port;
    private String adminUsername;
    private String adminPassword;
}
