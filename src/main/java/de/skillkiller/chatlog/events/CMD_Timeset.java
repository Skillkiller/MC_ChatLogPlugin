package de.skillkiller.chatlog.events;

import de.skillkiller.chatlog.database.LogHandler;
import de.skillkiller.chatlog.main.Core;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class CMD_Timeset implements CommandExecutor {

    Logger logger;
    LogHandler logHandler;

    public CMD_Timeset(Core core) {
        this.logger = core.logger;
        logHandler = core.logHandler;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length != 2) return false;
        if(!commandSender.hasPermission("chatlog.time")) return true;

        try {
            long timebefore = Long.parseLong(args[0]);
            long timeafter = Long.parseLong(args[1]);

            if (timebefore == 0 && timeafter == 0) {
                logHandler.removeTimeShift();
                commandSender.sendMessage("Timeshift entfernt. Restartcodes werden verwendet");
            } else {
                logHandler.setTimeShift(timebefore, timeafter);
                commandSender.sendMessage("TimeBefore: " + timebefore);
                commandSender.sendMessage("TimeAfter: " + timeafter);
                commandSender.sendMessage("Time set!");
            }
        } catch (NumberFormatException e) {
            commandSender.sendMessage("Es dürfen nur Ganzzahlen angeben werden!");
        }
        return true;
    }
}
