package net.purelic.cgm.commands.preferences;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.purelic.cgm.utils.ColorConverter;
import net.purelic.commons.Commons;
import net.purelic.commons.commands.parsers.CustomCommand;
import net.purelic.commons.commands.parsers.Permission;
import net.purelic.commons.profile.Preference;
import net.purelic.commons.profile.Profile;
import net.purelic.commons.profile.Rank;
import net.purelic.commons.profile.preferences.ArmorColor;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.CommandUtils;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorCommand implements CustomCommand {

    private final String hexRegex = "^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$";
    private final Pattern pattern = Pattern.compile(hexRegex);

    @Override
    public Command.Builder<CommandSender> getCommandBuilder(BukkitCommandManager<CommandSender> mgr) {
        return mgr.commandBuilder("color")
                .senderType(Player.class)
                .argument(StringArgument.optional("color"))
                .handler(c -> {
                    Player player = (Player) c.getSender();
                    Optional<String> colorArg = c.getOptional("color");

                    if (!colorArg.isPresent()) {
                        this.printColors(player);
                        return;
                    }

                    String colorStr = colorArg.get();
                    Profile profile = Commons.getProfile(player);

                    if (colorStr.equalsIgnoreCase("reset") || colorStr.equalsIgnoreCase("none")) {
                        profile.updatePreference(Preference.ARMOR_COLOR, null);
                        CommandUtils.sendSuccessMessage(player, "Your armor color has been reset to default!");
                    } else if (this.isValidColor(colorStr)) {
                        ArmorColor armorColor = ArmorColor.valueOf(colorStr.toUpperCase());

                        if (armorColor.isPremium() && Permission.notPremium(c, "Only Premium players can use this armor color!")) return;

                        Color color = ColorConverter.convert(armorColor.getColor());
                        profile.updatePreference(Preference.ARMOR_COLOR, (long) color.asRGB());
                        CommandUtils.sendSuccessMessage(player, "Armor color updated to " + armorColor.getName() + "!");
                    } else if (this.isValidColorCode(colorStr)) {
                        if (Permission.notPremium(c, "Only Premium players can use custom armor colors!")) return;

                        java.awt.Color color = java.awt.Color.decode(colorStr);
                        int r = color.getRed();
                        int g = color.getGreen();
                        int b = color.getBlue();
                        profile.updatePreference(Preference.ARMOR_COLOR, (long) Color.fromRGB(r, g, b).asRGB());
                        CommandUtils.sendSuccessMessage(player, "Armor color updated to " + colorStr + "!");
                    } else {
                        CommandUtils.sendErrorMessage(player, "Unknown color \"" + colorStr + "\"! Use /colors for a list of valid colors");
                    }
                });
    }

    private boolean isValidColor(String color) {
        return ArmorColor.contains(color);
    }

    private boolean isValidColorCode(final String colorCode) {
        Matcher matcher = this.pattern.matcher(colorCode);
        return matcher.matches();
    }

    private void printColors(Player player) {
        ComponentBuilder builder = new ComponentBuilder("");

        boolean first = true;

        for (ArmorColor color : ArmorColor.values()) {
            builder.append((first ? "" : "\n") + " • ").color(ChatColor.GRAY)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/color " + color.name().toLowerCase()).create()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/color " + color.name().toLowerCase()))
                    .append("⬛").color(color.getColor())
                    .append(" " + color.getName()).color(ChatColor.WHITE);

            if (color.isPremium()) {
                builder.append(" ")
                    .append(Rank.PREMIUM.getFlair())
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
                            Rank.PREMIUM.getFlair() + " " +
                                Rank.PREMIUM.getName(true) +
                                Rank.PREMIUM.getColor() + " Only Color").create()));
            }

            first = false;
        }

        ChatUtils.sendMessage(player, ChatUtils.getHeader("Select a Color"));
        ChatUtils.sendMessage(player, builder);
    }

}
