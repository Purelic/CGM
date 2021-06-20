package net.purelic.cgm.commands.info;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.CustomMap;
import net.purelic.cgm.core.maps.MapYaml;
import net.purelic.commons.Commons;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.utils.CommandUtils;
import net.purelic.commons.utils.Fetcher;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ocpsoft.prettytime.PrettyTime;
import xyz.upperlevel.spigot.book.BookUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MapCommand implements CustomCommand {

    private final PrettyTime pt = new PrettyTime();
    private final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    private final int authorLimit = 3;

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("map")
                .senderType(Player.class)
                .argument(StringArgument.optional("map", StringArgument.StringMode.GREEDY))
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    Optional<String> mapArg = c.getOptional("map");
                    String mapName = "";

                    if (mapArg.isPresent()) {
                        mapName = mapArg.get();
                    } else {
                        CustomMap map = MatchManager.getCurrentMap();

                        if (map == null) {
                            CommandUtils.sendErrorMessage(player, "Please specify a map!");
                            return;
                        } else {
                            mapName = map.getName();
                        }
                    }

                    // TODO move /sn map logic into this method
                    CustomMap map = CGM.getPlaylist().getMap(mapName);

                    if (map == null) {
                        CommandUtils.sendErrorMessage(player, "Could not find map \"" + mapName + "\"!");
                        return;
                    }

                    MapYaml yaml = map.getYaml();
                    List<String> authors = new ArrayList<>();
                    yaml.getAuthors().forEach(author -> authors.add(Fetcher.getNameOf(author)));

                    BookUtil.PageBuilder pageBuilder = new BookUtil.PageBuilder()
                        .add(new ComponentBuilder("⬅ Back")
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/maps").italic(true).color(ChatColor.GRAY).create()))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/maps"))
                            .bold(true)
                            .create()).newLine().newLine()
                        .add("Name: ")
                        .add(map.getName()).newLine().newLine()
                        .add("Created: ")
                        .add(new ComponentBuilder(this.pt.format(yaml.getCreated()))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.df.format(yaml.getCreated())).create()))
                            .create()).newLine().newLine()
                        .add("Updated: ")
                        .add(new ComponentBuilder(this.pt.format(yaml.getUpdated()))
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(this.df.format(yaml.getUpdated())).create()))
                            .create()).newLine().newLine();

                    if (authors.size() == 1) {
                        String name = authors.get(0);
                        Player author = Bukkit.getPlayer(name);
                        if (author != null) pageBuilder.add("Author:").newLine().add(Fetcher.getFancyName(author));
                        else pageBuilder.add("Author:").newLine().add(ChatColor.DARK_AQUA + name);
                    } else {
                        pageBuilder.add("Authors:");

                        boolean compact = authors.size() > this.authorLimit;

                        int l = 0;
                        for (String name : authors) {
                            if (compact && l == this.authorLimit - 1) break;
                            l++;
                            pageBuilder.newLine().add(" • ").add(this.getFancyAuthorComponent(name));
                        }

                        if (compact) {
                            String hover = "";

                            for (int i = this.authorLimit - 1; i < authors.size(); i++) {
                                if (i != this.authorLimit - 1) hover += "\n";
                                hover += " • " + this.getAuthorComponent(authors.get(i));
                            }

                            pageBuilder.newLine().add(new ComponentBuilder(" • and " + (authors.size() - 2) + " others")
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()))
                                    .create());
                        }
                    }

                    ItemStack book = BookUtil.writtenBook().pages(pageBuilder.build()).build();
                    BookUtil.openPlayer(player, book);
                });
    }

    private TextComponent getFancyAuthorComponent(String name) {
        Player author = Bukkit.getPlayer(name);
        if (author != null) return Fetcher.getFancyName(author);
        else return new TextComponent(ChatColor.DARK_AQUA + name);
    }

    private String getAuthorComponent(String name) {
        Player author = Bukkit.getPlayer(name);
        if (author != null) return Commons.getProfile(author).getFlairs() + NickUtils.getDisplayName(author);
        else return ChatColor.DARK_AQUA + name;
    }

}
