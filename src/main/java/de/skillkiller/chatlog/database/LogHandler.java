package de.skillkiller.chatlog.database;

import de.skillkiller.chatlog.main.Core;
import org.bukkit.Bukkit;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

public class LogHandler {

    private Sql database;
    private Logger logger;
    private final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private SecureRandom rnd = new SecureRandom();


    public LogHandler(Sql database, Core core) {
        this.database = database;
        this.logger = core.logger;
        createDefaultTable();
    }

    public void createDefaultTable() {

        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement("SHOW TABLES;")) {
            ResultSet resultSet = preparedStatement.executeQuery();
            int i = 0;
            while (resultSet.next() && i <= 3) {
                switch (resultSet.getString(1)) {
                    case "messages":
                    case "restartcode":
                    case "timecode":
                    case "config":
                        i++;
                }
            }

            if (i == 4) {
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        database.executeQuery("create table if not exists config\n" +
                "(\n" +
                "  server     varchar(32) not null,\n" +
                "  timebefore bigint      not null,\n" +
                "  timeafter  bigint      not null\n" +
                ");");

        database.executeQuery("create table if not exists messages\n" +
                "(\n" +
                "  id        bigint auto_increment\n" +
                "    primary key,\n" +
                "  time      timestamp default CURRENT_TIMESTAMP not null,\n" +
                "  server    varchar(32)                         not null,\n" +
                "  restartnr int                                 not null,\n" +
                "  uuid      varchar(36)                         not null,\n" +
                "  message   varchar(350)                        not null\n" +
                ");");

        database.executeQuery("create index restartnr\n" +
                "  on messages (restartnr);");

        database.executeQuery("create index server\n" +
                "  on messages (server);");

        database.executeQuery("create table if not exists restartcode\n" +
                "(\n" +
                "  code      varchar(32)                         not null\n" +
                "    primary key,\n" +
                "  server    varchar(32)                         not null,\n" +
                "  restartnr int                                 not null,\n" +
                "  created   timestamp default CURRENT_TIMESTAMP not null,\n" +
                "  constraint restartcode_ibfk_1\n" +
                "  foreign key (restartnr) references messages (restartnr)\n" +
                "    on update cascade,\n" +
                "  constraint restartcode_ibfk_2\n" +
                "  foreign key (server) references messages (server)\n" +
                ");\n");

        database.executeQuery("create index restartnr\n" +
                "  on restartcode (restartnr);");

        database.executeQuery("create index server\n" +
                "  on restartcode (server);");

        database.executeQuery("create table if not exists timecode\n" +
                "(\n" +
                "  code     varchar(32)                             not null\n" +
                "    primary key,\n" +
                "  server   varchar(32)                             not null,\n" +
                "  fromTime timestamp default '0000-00-00 00:00:00' not null,\n" +
                "  toTime   timestamp default '0000-00-00 00:00:00' not null,\n" +
                "  created  timestamp default CURRENT_TIMESTAMP     not null,\n" +
                "  constraint timecode_ibfk_1\n" +
                "  foreign key (server) references messages (server)\n" +
                "    on update cascade\n" +
                ");");

        database.executeQuery("create index server\n" +
                "  on timecode (server);");
    }

    public void addMessage(UUID uuid, String message) {
        try {
            PreparedStatement preparedStatement = database.getConnection().prepareStatement("INSERT INTO `messages` (`time`, `server`, `restartnr`, `uuid`, `message`) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?);");
            preparedStatement.setString(1, Bukkit.getServerName());
            preparedStatement.setInt(2, Core.restartNr);
            preparedStatement.setString(3, uuid.toString());
            preparedStatement.setString(4, message);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addPluginMessage(String message) {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        addMessage(uuid, message);
    }

    public String createRestartCode() {
        int counter = 0;
        boolean found = false;
        String code = null;

        while (!found && counter < 10) {
            code = "R" + randomString(5);
            found = !existCode(code);
        }

        if (!found) {
            logger.warning("Es konnte kein Code gefunden werden!");
            return null;
        }
        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement("INSERT INTO restartcode (code, server, restartnr) VALUES (?, ?, ?);")) {
            preparedStatement.setString(1, code);
            preparedStatement.setString(2, Bukkit.getServerName());
            preparedStatement.setInt(3, Core.restartNr);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }

    private boolean existCode(String code) {
        try {
            PreparedStatement preparedStatement;
            if (code.startsWith("T")) {
                preparedStatement = database.getConnection().prepareStatement("SELECT code FROM timecode WHERE code = ?;");
            } else {
                preparedStatement = database.getConnection().prepareStatement("SELECT code FROM restartcode WHERE code = ?;");
            }

            preparedStatement.setString(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.last();
            return resultSet.getRow() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    public boolean needTimeCode() {
        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement("SELECT * FROM config WHERE server = ?")){
            preparedStatement.setString(1, Bukkit.getServerName());
            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.last();

            return resultSet.getRow() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String createTimeCode(Date fromTime, Date toTime) {
        int counter = 0;
        boolean found = false;
        String code = null;

        while (!found && counter < 10) {
            code = "T" + randomString(5);
            found = !existCode(code);
        }

        if (!found) {
            logger.warning("Es konnte kein Code gefunden werden!");
            return null;
        }
        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement("INSERT INTO `timecode` (`code`, `server`, `fromTime`, `toTime`) VALUES (?, ?, ?, ?);")) {
            preparedStatement.setString(1, code);
            preparedStatement.setString(2, Bukkit.getServerName());
            preparedStatement.setTimestamp(3, new Timestamp(fromTime.getTime()));
            preparedStatement.setTimestamp(4, new Timestamp(toTime.getTime()));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;

    }

    public String[] getTimeShift() {
        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement("SELECT * FROM config WHERE server = ?")){
            preparedStatement.setString(1, Bukkit.getServerName());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.first();
            String s = resultSet.getString("timebefore") + ":" + resultSet.getString("timeafter");
            return s.split(":");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
