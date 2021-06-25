package net.purelic.cgm.match;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.match.constants.RoundResult;
import net.purelic.cgm.match.stats.RoundStats;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class MatchRound {

    private final RoundStats stats; // todo still not sure this is the best way to get round placements
    private RoundResult result;
    private MatchTeam winningTeam;
    private MatchParticipant winningPlayer;

    public MatchRound() {
        this.stats = new RoundStats();
        this.result = RoundResult.INCOMPLETE;
    }

    public RoundStats getStats() {
        return this.stats;
    }

    public void complete() {
        if (MatchUtils.isElimination()) {
            if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                // stats get last participant alive, if exists they're the winner
                // other wise fallback to ordered participants based on score
                // we should avoid draws and always keep the player who reached the higher score first
                // there could be really rare edge cases, but we can probably ignore them (tied scores/no scoring gms)
                // winning team will always be solo
            } else {
                // right now we just take the last team alive if there is one
                // we could probably also go buy 1) higher round score and then 2) most players alive
            }
        } else {
            if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                // top individual round score
            } else {
                // top team round score
            }
        }
    }

    private ChatColor getColor() {
        if (this.result == RoundResult.COMPLETE) return this.stats.getTeams().get(0).getColor();
        else if (this.result == RoundResult.DRAW) return ChatColor.GRAY;
        else return ChatColor.WHITE;
    }

    public String getSymbol() {
        return this.getColor() + this.result.getSymbol() + ChatColor.RESET;
    }

    public void sendResults() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean playing = CGM.getMatchManager2().isPlaying(player);

            if (this.result == RoundResult.DRAW) {
                this.sendResult(player, "Round Over", "Draw!", ChatColor.WHITE, ChatColor.YELLOW);
            } else if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO)) {
                if (!playing) {
                    this.sendResult(player, NickUtils.getDisplayName(this.winningPlayer.getPlayer()) + " won the round!");
                } else if (this.winningPlayer.getPlayer() == player) {
                    this.sendWonResult(player);
                    SoundUtils.SFX.CRAB_RAVE.play(player);
                } else {
                    this.sendLostResult(player, NickUtils.getDisplayName(this.winningPlayer.getPlayer()) + " won the round!");
                }
            } else {
                if (!playing) {
                    this.sendResult(player, this.winningTeam.getColoredName() + " won the round!");
                } else if (this.winningTeam.has(player)) {
                    this.sendWonResult(player);
                    SoundUtils.SFX.CRAB_RAVE.play(player);
                } else {
                    this.sendLostResult(player, this.winningTeam.getColoredName() + " won the round!");
                }
            }
        }
    }

    private void sendResult(Player player, String subtitle) {
        this.sendResult(player, "Round Over", subtitle, ChatColor.WHITE, ChatColor.WHITE);
    }

    private void sendWonResult(Player player) {
        this.sendResult(player, "Round Won", "You won the round!", ChatColor.GREEN, ChatColor.WHITE);
    }

    private void sendLostResult(Player player, String subtitle) {
        this.sendResult(player, "Round Lost", subtitle, ChatColor.RED, ChatColor.WHITE);
    }

    private void sendResult(Player player, String title, String subtitle, ChatColor titleColor, ChatColor subtitleColor) {
        ChatUtils.sendTitle(player, titleColor + title, subtitle);

        ChatUtils.sendMessage(player,
            new ComponentBuilder("\n")
                .append(" " + title.toUpperCase() + " » ").color(titleColor).bold(true)
                .append(subtitle).reset().color(subtitleColor)
                .append("\n").reset()
        );
    }

}
