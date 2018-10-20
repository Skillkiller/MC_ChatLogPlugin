package de.skillkiller.chatlog.database;

import de.skillkiller.chatlog.main.Core;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Sql {

    private String username, password, host, database, port;
    private Connection connection;
    private Logger logger;
    private Core core;

    public Sql(String username, String password, String host, String database, String port, Core core) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.database = database;
        this.port = port;
        this.logger = core.logger;
        this.core = core;
        openConnection();
    }

    public Connection getConnection() {
        try {
            if (connection.isValid(5)) {
                return connection;
            } else {
                openConnection();
                return connection;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void openConnection() {
        try {
            connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s", host, port, database), username, password);
        } catch (SQLException e) {
            Bukkit.getPluginManager().disablePlugin(core);
            e.printStackTrace();
        }
        logger.info("Verbindung hergestellt");

    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            logger.info("Verbindung geschlossen");
        }
    }

    public void executeQuery(String query) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement(query);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executePreparedStatement(PreparedStatement preparedStatement) {
        try {
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
