package de.skillkiller.chatlog.main;

import de.skillkiller.chatlog.database.LogHandler;
import de.skillkiller.chatlog.database.Sql;
import de.skillkiller.chatlog.events.Command;
import de.skillkiller.chatlog.events.PlayerChat;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Core extends JavaPlugin {

    public Logger logger = getLogger();
    public Sql database;
    public LogHandler logHandler;

    public static int restartNr;

    @Override
    public void onEnable() {
        logger.info("Chatlog wird gestartet");
        database = getSql();
        logHandler = new LogHandler(database, this);
        setRestartNr();
        logHandler.addPluginMessage("Server gestartet");

        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerChat(this), this);
        this.getCommand("chatlog").setExecutor(new Command(this));
    }

    @Override
    public void onDisable() {
        logHandler.addPluginMessage("Server stoppt");
        database.closeConnection();
    }

    private Sql getSql() {
        File file = new File(getDataFolder(), "mysql.yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        cfg.options().header("Die Datenbank wird benötigt um alle WildCard und Spieler speichern zu können");
        String db = "database.";
        cfg.addDefault(db + "host", "localhost");
        cfg.addDefault(db + "port", Integer.valueOf(3306));
        cfg.addDefault(db + "user", "USER");
        cfg.addDefault(db + "password", "PASSWORD");
        cfg.addDefault(db + "database", "DATABASE");
        cfg.options().copyDefaults(true);
        try
        {
            cfg.save(file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        String host, user, password, database;
        int port;

        host = cfg.getString(db + "host");
        port = cfg.getInt(db + "port");
        user = cfg.getString(db + "user");
        password = (cfg.getString(db + "password").equals("PASSWORD")) ? "" : cfg.getString(db + "password");
        database = cfg.getString(db + "database");
        return new Sql(user, password, host, database, String.valueOf(port), this);
    }

    private void setRestartNr() {
        try (PreparedStatement preparedStatement = database.getConnection().prepareStatement("SELECT restartnr FROM messages WHERE server = ? ORDER BY restartnr desc LIMIT 1;");){
            preparedStatement.setString(1, Bukkit.getServerName());
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.last();
            if (resultSet.getRow() == 0) {
                restartNr = 1;
            } else {
                resultSet.first();
                restartNr = resultSet.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
