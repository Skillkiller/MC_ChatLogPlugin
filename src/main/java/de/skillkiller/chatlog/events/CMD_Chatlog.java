package de.skillkiller.chatlog.events;

import de.skillkiller.chatlog.database.LogHandler;
import de.skillkiller.chatlog.main.Core;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.logging.Logger;

public class CMD_Chatlog implements CommandExecutor {

    Logger logger;
    LogHandler logHandler;

    public CMD_Chatlog(Core core) {
        this.logger = core.logger;
        logHandler = core.logHandler;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] args) {

        if (commandSender instanceof Player) {
            logHandler.addMessage(((Player) commandSender).getUniqueId(), "/chatlog");
        } else {
            logHandler.addPluginMessage("/chatlog");
        }
        if (!logHandler.needTimeCode()) {
            commandSender.sendMessage("Code: " + logHandler.createRestartCode());
        } else {

            String shift[] = logHandler.getTimeShift();
            long lbefor, lafter;
            lbefor = Long.parseLong(shift[0]);
            lafter = Long.parseLong(shift[1]);
            Date now = new Date();

            Date fromTime = new Date(now.getTime() - lbefor * 1000);
            Date toTime = new Date(now.getTime() + lafter * 1000);

            commandSender.sendMessage("Code: " + logHandler.createTimeCode(fromTime, toTime));
        }
        return true;
    }
}
