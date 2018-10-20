package de.skillkiller.chatlog.events;

import de.skillkiller.chatlog.main.Core;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChat implements Listener {

    Core core;

    public PlayerChat(Core core) {
        this.core = core;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent chatEvent) {
        core.logHandler.addMessage(chatEvent.getPlayer().getUniqueId(), chatEvent.getMessage());
        System.out.println(chatEvent.getPlayer().getUniqueId().toString());
    }

}
