package net.purelic.cgm.core.maps;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.core.match.Participant;
import net.purelic.commons.utils.ChatUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Collection;

public class ProgressBar {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    private static final ChatColor BRACKET_COLOR = ChatColor.DARK_GRAY;
    private static final ChatColor COMPLETE_COLOR = ChatColor.GREEN;
    private static final ChatColor INCOMPLETE_COLOR = ChatColor.GRAY;
    private static final ChatColor PROGRESS_COLOR = ChatColor.AQUA;
    private static final String PROGRESS_BAR_SYMBOL = "|";
    private static final int TOTAL_PROGRESS_BARS = 40;

    private final String bar;
    private final boolean sound;
    private final float pitch;

    public ProgressBar(String title, float percent, boolean sound) {
        final int progressBars = (int) (TOTAL_PROGRESS_BARS * percent);
        final String progress = DECIMAL_FORMAT.format(Math.min(percent, 1) * 100);

        this.bar = title + " " + BRACKET_COLOR + "[" + ChatColor.RESET +
            StringUtils.repeat(COMPLETE_COLOR + PROGRESS_BAR_SYMBOL, progressBars) +
            StringUtils.repeat(INCOMPLETE_COLOR + PROGRESS_BAR_SYMBOL, TOTAL_PROGRESS_BARS - progressBars) +
            BRACKET_COLOR + "]" + PROGRESS_COLOR + " " + progress + (progress.contains(".") ? "" : ".0") + "%";
        this.sound = sound;
        this.pitch = (1.5F * percent) + 0.5F;
    }

    public void sendBar(Collection<? extends Participant> participants) {
        participants.forEach(participant -> this.sendBar(participant.getPlayer()));
    }

    public void sendBar(Participant participant) {
        this.sendBar(participant.getPlayer());
    }

    public void sendBar(Player player) {
        ChatUtils.sendActionBar(player, this.bar);
        if (this.sound) player.playSound(player.getLocation(), Sound.CLICK, 1F, this.pitch);
    }

}
