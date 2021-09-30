package net.purelic.cgm.commands.discord;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.commands.toggles.ToggleAutoJoinCommand;
import net.purelic.cgm.commands.toggles.ToggleAutoStartCommand;
import net.purelic.cgm.commands.toggles.ToggleJoinLockCommand;
import net.purelic.commons.Commons;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.IOException;

public class RumbleCommand implements CustomCommand {

    private final String webhook;
    private boolean used;

    public RumbleCommand(Configuration config) {
        this.webhook = config.getString("alerts_webhook");
        this.used = false;
    }

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("rumble")
            .senderType(Player.class)
            .permission(Permission.isMod())
            .handler(c -> {
                Player player = (Player) c.getSender();

                // if (Permission.notPremium(c)) return;

                if (!Commons.isOwner(player)) {
                    CommandUtils.sendErrorMessage(player, "Only the server owner can start a rumble!");
                    return;
                }

                if (this.used) {
                    CommandUtils.sendErrorMessage(player, "You can only use this command once per server!");
                    return;
                }

                this.used = true;
                this.sendDiscordNotification(player);
                Commons.sendSpringMessage(player, "StartRumble", ServerUtils.getId());
                CommandUtils.sendSuccessMessage(player, "You alerted the \"Rumblers\" role in Discord! (#alerts)");

                ToggleJoinLockCommand.joinlock = true;
                ToggleAutoJoinCommand.autoJoin = false;
                ToggleAutoStartCommand.autostart = false;

                if (Bukkit.hasWhitelist()) {
                    ServerUtils.setWhitelisted(false);
                    CommandUtils.sendSuccessMessage(player, "You turned the whitelist off!");
                }
            });
    }

    private void sendDiscordNotification(Player sender) {
        String uuid;

        try {
            uuid = NickUtils.isNicked(sender) ? Fetcher.getMinecraftUser(NickUtils.getNick(sender)).getId().toString() : sender.getUniqueId().toString();
        } catch (IOException e) {
            uuid = sender.getUniqueId().toString();
        }

        DiscordWebhook webhook = new DiscordWebhook(this.webhook, "Purelic");
        webhook.setContent("<@&890320975294513162>");
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
            .setColor(Color.GREEN)
            .setDescription(sender.getName() + " is looking to Rumble!")
            .addField("Server", "/server " + ServerUtils.getName(), false)
            .addField("Players Online", "" + Bukkit.getOnlinePlayers().size(), false)
            .setAuthor(sender.getName(), "https://purelic.net/players/" + sender.getName(), "https://crafatar.com/renders/head/" + uuid + "?size=128&overlay")
        );
        webhook.execute();
    }

}
