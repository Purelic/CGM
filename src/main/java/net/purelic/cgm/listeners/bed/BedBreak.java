package net.purelic.cgm.listeners.bed;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.core.maps.bed.Bed;
import net.purelic.cgm.core.maps.bed.events.BedBreakEvent;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.rewards.RewardBuilder;
import net.purelic.cgm.core.runnables.MatchCountdown;
import net.purelic.cgm.events.match.MatchQuitEvent;
import net.purelic.cgm.events.match.RoundStartEvent;
import net.purelic.cgm.utils.BedUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.VersionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.github.paperspigot.Title;

import java.util.List;

public class BedBreak implements Listener {

    @EventHandler
    public void onBedBreak(BedBreakEvent event) {
        Player player = event.getPlayer();
        Bed bed = event.getBed();
        MatchTeam owner = bed.getOwner();

        // destroy the bed
        bed.destroy();

        if (MatchCountdown.getElapsed() > 0) {
            event.broadcast();
        }

        // update scoreboard
        ScoreboardManager.updateTeamBoard();
        // ScoreboardManager.updateTeamBoard(owner);

        if (player != null || event.isSuddenDeath()) {
            MatchTeam breakerTeam = MatchTeam.getTeam(player);

            if (player != null) {
                // reward player
                new RewardBuilder(player, 1, "Bed", "Bed Break").reward();
                MatchManager.getParticipant(player).getStats().addBed();
            }

            // play bed destroy sfx
            TeamType teamType = EnumSetting.TEAM_TYPE.get();
            for (MatchTeam team : teamType.getTeams()) {
                if (team == owner) SoundUtils.SFX.BED_DESTROYED.play(team);
                else if (team == breakerTeam) SoundUtils.SFX.TEAM_DESTROYED_BED.play(team);
                else SoundUtils.SFX.ENEMY_BED_DESTROYED.play(team);
            }

            // set lives remaining post-bed break
            this.setLives(owner.getPlayers());
        }
    }

    private void setLives(List<Player> players) {
        int lives = NumberSetting.LIVES_PER_ROUND.value();

        for (Player player : players) {
            Participant participant = MatchManager.getParticipant(player);
            participant.setLives(lives);

            String title = ChatColor.RED + "Bed Destroyed!";
            String subtitle = lives == 0 ? "" : ChatColor.AQUA + "" + lives + ChatColor.RESET + " " + (lives == 1 ? "Life" : "Lives") + " Remaining";

            if (VersionUtils.isLegacy(player)) {
                player.sendMessage(ChatColor.RED + "Your bed was destroyed! " + subtitle);
            } else {
                player.sendTitle(new Title(
                        new TextComponent(title),
                        new TextComponent(subtitle),
                        5,
                        20,
                        5
                ));
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onRoundStart(RoundStartEvent event) {
        BedUtils.getBeds().stream()
                .filter(bed -> bed.getOwner().getPlayers().size() == 0)
                .forEach(bed -> Commons.callEvent(new BedBreakEvent(bed, false)));
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onMatchQuit(MatchQuitEvent event) {
        if (!MatchState.isState(MatchState.STARTED)) return;

        BedUtils.getBeds().stream()
                .filter(bed -> !bed.isDestroyed())
                .filter(bed -> bed.getOwner().getPlayers().size() == 0)
                .forEach(bed -> Commons.callEvent(new BedBreakEvent(bed, false)));
    }

}
