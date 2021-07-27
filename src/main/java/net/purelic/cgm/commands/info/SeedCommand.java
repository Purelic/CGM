package net.purelic.cgm.commands.info;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.purelic.commons.commands.parsers.CustomCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SeedCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("seed")
            .senderType(Player.class)
            .handler(c -> {
                Player sender = (Player) c.getSender();

                new ComponentBuilder("Seed: ").color(ChatColor.GREEN)
                    .append(sender.getWorld().getSeed() + "").color(ChatColor.WHITE).underlined(true)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to Copy").create()))
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, sender.getWorld().getSeed() + ""));
            });
    }

}
