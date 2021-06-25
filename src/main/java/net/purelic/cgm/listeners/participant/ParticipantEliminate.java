package net.purelic.cgm.listeners.participant;

import net.md_5.bungee.api.ChatColor;
import net.purelic.cgm.CGM;
import net.purelic.cgm.commands.toggles.ToggleSpectatorsCommand;
import net.purelic.cgm.core.managers.ScoreboardManager;
import net.purelic.cgm.events.match.RoundEndEvent;
import net.purelic.cgm.events.participant.MatchTeamEliminateEvent;
import net.purelic.cgm.events.participant.ParticipantEliminateEvent;
import net.purelic.cgm.match.MatchTeam;
import net.purelic.cgm.utils.MatchUtils;
import net.purelic.cgm.utils.SpawnUtils;
import net.purelic.commons.Commons;
import net.purelic.commons.utils.ChatUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ParticipantEliminate implements Listener {

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
        player.getInventory().addItem(ToggleSpectatorsCommand.getToggleItem(player));

        if (player.getLocation().getY() <= 0) {
            SpawnUtils.teleportObsSpawn(player);
        }

        if (team.isEliminated()) {
            Commons.callEvent(new MatchTeamEliminateEvent(team));
        }

        if (CGM.get().getMatchManager().allEliminated()) {
            Commons.callEvent(new RoundEndEvent());
        } else {
            if (!event.isCombatLog()) {
                ChatUtils.sendTitle(player, ChatColor.RED + "Eliminated", CGM.getMatchManager2().getCurrentMatch().isRoundBased() ? "You will respawn next round" : "");
            }
        }
    }

}
