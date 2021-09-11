package net.purelic.cgm.commands.controls;

import cloud.commandframework.Command;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.purelic.cgm.CGM;
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

public class LTPCommand implements CustomCommand {

    private final String webhook;
    private final double cooldown = 3600.0D; // 1 hour in seconds
    private Long lastUsed = 0L;

    public LTPCommand(Configuration config) {
        this.webhook = config.getString("alerts_webhook");
    }

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("lookingtoplay", "ltp")
            .senderType(Player.class)
            .handler(c -> {
                Player player = (Player) c.getSender();

                if (Permission.notPremium(c)) return;

                if (!Commons.isOwner(player)) {
                    CommandUtils.sendErrorMessage(player, "Only the server owner can promote their server!");
                    return;
                }

                if (this.lastUsed > 0L) {
                    double timeLeft = (this.lastUsed + this.cooldown * 1000L) - System.currentTimeMillis();

                    if (timeLeft > 0) {
                        CommandUtils.sendErrorMessage(player, "You can only use this command once every hour!");
                        return;
                    }
                }

                this.lastUsed = System.currentTimeMillis();
                this.sendDiscordNotification(player);
                CommandUtils.sendSuccessMessage(player, "You alerted the \"Looking to Play\" role in Discord! (#alerts)");
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
        webhook.setContent("<@&830673260952944660>");
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
            .setColor(Color.GREEN)
            .setDescription(sender.getName() + " is looking to play " + CGM.getPlaylist().getName() + "!")
            .addField("Server", "/server " + ServerUtils.getName(), false)
            .addField("Players Online", "" + Bukkit.getOnlinePlayers().size(), false)
            .setAuthor(sender.getName(), "https://purelic.net/players/" + sender.getName(), "https://crafatar.com/renders/head/" + uuid + "?size=128&overlay")
        );
        webhook.execute();
    }

}
