package net.purelic.cgm.listeners.match;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.TabManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.match.RoundResult;
import net.purelic.cgm.core.match.constants.ParticipantState;
import net.purelic.cgm.core.runnables.MatchCountdown;
import net.purelic.cgm.core.runnables.RoundCountdown;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.utils.PlayerUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RoundEnd implements Listener {

    @EventHandler
    public void onRoundEnd(RoundEndEvent event) {
        RoundResult result = event.getResult();
        MatchTeam winningTeam = result.getWinningTeam();
        Participant winningParticipant = result.getWinningParticipant();

        this.resetParticipantStates();

        if (EnumSetting.TEAM_TYPE.is(TeamType.SOLO) && winningParticipant != null) {
            MatchManager.setRoundWinner(MatchTeam.SOLO);
            winningParticipant.addRoundWin();
        } else {
            MatchManager.setRoundWinner(winningTeam);
        }

        MatchCountdown.getCountdown().cancel();
        TabManager.updateRounds();
        PlayerUtils.clearEffectsAll();

        if ((!NumberSetting.ROUNDS.isDefault() && winningTeam != null && winningTeam.getRoundsWon() > (NumberSetting.ROUNDS.value() / 2))
            || (NumberSetting.ROUNDS.value() <= MatchManager.getRound())) {
            MatchState.setState(MatchState.ENDED);
        } else {
            new RoundCountdown().runTaskTimer(CGM.get(), 0, 20);

            for (Player player : Bukkit.getOnlinePlayers()) {
                TaskUtils.run(() -> player.setAllowFlight(true));
                PlayerUtils.clearInventory(player);

                if (result.isDraw()) {
                    this.sendPlacement(player, "Round Over", ChatColor.YELLOW + "Draw!");
                } else if (result.isWinner(player)) {
                    this.sendPlacement(player, ChatColor.GREEN + "Round Won", "You won the round!");
                    SoundUtils.SFX.CRAB_RAVE.play(player);
                } else {
                    String title = MatchTeam.getTeam(player) == MatchTeam.OBS ? "Round Over" : ChatColor.RED + "Round Lost";
                    this.sendPlacement(player, title, result.getWinner() + ChatColor.RESET + " won the round!");
                }
            }
        }
    }

    // Set all participant states back to ALIVE on round end
    // This is mainly important for updating player visibility
    // e.g. RESPAWNING participants might not get shown to others
    private void resetParticipantStates() {
        for (Participant participant : MatchManager.getParticipants()) {
            participant.setState(ParticipantState.ALIVE);
        }
    }

    private void sendPlacement(Player player, String title, String subtitle) {
        ChatUtils.sendTitle(player, title, subtitle);

        ChatUtils.sendMessage(
            player,
            new ComponentBuilder("\n")
                .append(" " + title.toUpperCase() + " » ").color(ChatColor.WHITE).bold(true)
                .append(subtitle).reset()
                .append("\n").reset()
        );
    }

}
