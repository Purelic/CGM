package net.purelic.cgm.core.rewards;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.purelic.cgm.core.managers.MatchManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;

public class RewardBuilder {

    private final Player player;
    private final int amount;
    private final String type;
    private final String reason;
    private final boolean excludeSuffix;
    private final LinkedHashMap<Medal, Integer> medals;

    public RewardBuilder(Player player, int amount, String reason) {
        this(player, amount, "Point", reason);
    }

    public RewardBuilder(Player player, int amount, String type, String reason) {
        this(player, amount, type, reason, false);
    }

    public RewardBuilder(Player player, int amount, String type, String reason, boolean excludeSuffix) {
        this.player = player;
        this.amount = amount;
        this.type = type;
        this.reason = reason;
        this.excludeSuffix = excludeSuffix;
        this.medals = new LinkedHashMap<>();
    }

    public RewardBuilder addMedals(List<Medal> medals) {
        medals.forEach(this::addMedal);
        return this;
    }

    public RewardBuilder addMedal(Medal medal) {
        return this.addMedal(medal, 1);
    }

    public RewardBuilder addMedal(Medal medal, int amount) {
        if (medal == null || amount == 0) return this;
        this.medals.putIfAbsent(medal, 0);
        this.medals.put(medal, this.medals.get(medal) + 1);
        return this.addMedal(medal, amount - 1);
    }

    public void reward() {
        this.reward(Sound.LEVEL_UP);
    }

    public void reward(Sound sound) {
        boolean positive = this.amount >= 0;

        ComponentBuilder message =
                new ComponentBuilder((positive ? "+" : "") + this.amount + " " + this.type + (Math.abs(this.amount) == 1 || this.excludeSuffix ? "" : "s"))
                            .color(positive ? ChatColor.GREEN : ChatColor.RED).bold(true)
                        .append(" | ").reset().color(ChatColor.DARK_GRAY)
                        .append(this.reason).reset();

        for (Medal medal : this.medals.keySet()) {
            this.addMedal(message, medal, this.medals.get(medal));
        }

        this.player.sendMessage(message.create());
        this.player.playSound(this.player.getLocation(), sound, 10.0F, 2.0F);

        MatchManager.getParticipant(this.player).getStats().addMedals(this.medals);
    }

    private void addMedal(ComponentBuilder builder, Medal medal, int amount) {
        builder.append(", ").reset()
                .append(medal.getName()).color(medal.getColor())
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(medal.getDescription()).color(medal.getColor()).create()));
        if (amount > 1) builder.append(" (x" + amount + ")").color(ChatColor.GRAY);
    }

}
