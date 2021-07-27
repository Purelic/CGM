package net.purelic.cgm.commands.info;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.gamemodes.CustomGameMode;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.Fetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ocpsoft.prettytime.PrettyTime;
import xyz.upperlevel.spigot.book.BookUtil;

import java.text.SimpleDateFormat;

public class GameModeCommand implements CustomCommand {

    private final PrettyTime pt = new PrettyTime();
    private final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    private final int authorLimit = 3;

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("gm")
            .senderType(Player.class)
            .argument(StringArgument.of("game mode", StringArgument.StringMode.GREEDY))
            .handler(c -> {
                Player player = (Player) c.getSender();
                String gmArg = c.get("game mode");

                CustomGameMode gameMode = CGM.getPlaylist().getGameMode(gmArg);

                if (gameMode == null) {
                    CommandUtils.sendErrorMessage(player, "Could not find game mode \"" + gmArg + "\"!");
                    return;
                }

                BookUtil.PageBuilder pageBuilder = new BookUtil.PageBuilder()
                    .add(new ComponentBuilder("⬅ Back")
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/gamemodes").italic(true).color(ChatColor.GRAY).create()))
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gamemodes"))
                        .bold(true)
                        .create()).newLine().newLine()
                    .add("Name: ")
                    .add(gameMode.getName()).newLine()
                    .add("Alias: ")
                    .add(gameMode.getAlias()).newLine().newLine();

                String name = Fetcher.getNameOf(gameMode.getAuthor());
                Player author = Bukkit.getPlayer(name);

                if (author != null) pageBuilder.add("Author:").newLine().add(Fetcher.getFancyName(player));
                else pageBuilder.add("Author:").newLine().add(ChatColor.DARK_AQUA + name);

                pageBuilder.newLine().newLine()
                    .add("Description:").newLine()
                    .add(gameMode.getDescription());

                ItemStack book = BookUtil.writtenBook().pages(pageBuilder.build()).build();
                BookUtil.openPlayer(player, book);
            });
    }

}
