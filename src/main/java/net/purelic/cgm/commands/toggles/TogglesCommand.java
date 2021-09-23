package net.purelic.cgm.commands.toggles;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.purelic.cgm.CGM;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TogglesCommand implements CustomCommand {

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("toggles")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();
                player.sendMessage(ChatUtils.getHeader("Toggles"));
                player.sendMessage(getToggle("Voting", CGM.getVotingManager().isEnabled(), "voting").create());
                player.sendMessage(getToggle("Auto-Start", ToggleAutoStartCommand.autostart, "autostart").create());
                player.sendMessage(getToggle("Auto-Join", ToggleAutoJoinCommand.autoJoin, "autojoin").create());
                player.sendMessage(getToggle("Join-Lock", ToggleJoinLockCommand.joinlock, "joinlock").create());
                player.sendMessage(getToggle("Friendly Fire", ToggleFriendlyFireCommand.friendlyFire, "friendly_fire").create());
            });
    }

    private static ComponentBuilder getToggle(String label, boolean status, String command) {
        command = "/toggle " + command;

        return new ComponentBuilder(ChatColor.GRAY + " • " + ChatColor.RESET + label + " is " + getStatus(status) + " - " + command)
            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to " + getStatus(!status) + " " + label).create()));
    }

    public static String getStatus(boolean value) {
        return (value ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled") + ChatColor.RESET;
    }

}
