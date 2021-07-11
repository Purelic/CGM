package net.purelic.cgm.listeners.participant;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.participant.MatchTeamEliminateEvent;
import net.purelic.cgm.events.participant.ParticipantEliminateEvent;
import net.purelic.cgm.kit.SpectatorKit;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.cgm.utils.SpawnUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ChatUtils;
import net.purelic.commons.utils.TaskUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ParticipantEliminate implements Listener {

    private final SpectatorKit spectatorKit;

    public ParticipantEliminate() {
        this.spectatorKit = new SpectatorKit();
    }

    @EventHandler
    public void onParticipantEliminate(ParticipantEliminateEvent event) {
        Player player = event.getPlayer();
        MatchTeam team = MatchTeam.getTeam(player);

        ScoreboardManager.updateTeamBoard();
        ScoreboardManager.updateSoloBoard();

        player.getWorld().strikeLightningEffect(player.getLocation());
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.spigot().setCollidesWithEntities(false);
        player.spigot().setAffectsSpawning(false);

        if (player.getLocation().getY() <= 0) {
            SpawnUtils.teleportObsSpawn(player);
        }

        if (MatchUtils.isTeamEliminated(team)) {
            Commons.callEvent(new MatchTeamEliminateEvent(team));
        }

        if (CGM.get().getMatchManager().allEliminated()) {
            Commons.callEvent(new RoundEndEvent());
        } else {
            if (!event.isCombatLog()) {
                ChatUtils.sendTitle(player, ChatColor.RED + "Eliminated", MatchUtils.hasRounds() ? "You will respawn next round" : "");
            }
        }

        // TODO somewhere i think inventories are getting cleared via a task so it's
        // applying the kit but then somewhere the player's inv is getting cleared.
        // This condition checks that we still want to schedule to apply this kit to the player since
        // the match could edit from some logic checking above
        if (MatchState.isState(MatchState.STARTED)) {
            TaskUtils.runLater(() -> this.spectatorKit.apply(player), 30L);
        }
    }

}
