package net.purelic.cgm.core.maps.hill.runnables;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.ToggleSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.maps.hill.events.HillEvent;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.core.match.constants.ParticipantState;
import net.purelic.cgm.core.rewards.RewardBuilder;
import net.purelic.cgm.listeners.modules.HeadModule;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.cgm.utils.SoundUtils;
import net.purelic.cgm.utils.SpawnUtils;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HillChecker extends BukkitRunnable {

    private final Hill hill;
    private final boolean teleport;

    public HillChecker(Hill hill) {
        this.hill = hill;
        this.teleport = ToggleSetting.CAPTURED_HILLS_TELEPORT.isEnabled()
            || EnumSetting.GAME_TYPE.is(GameType.DEATHMATCH);
    }

    @Override
    public void run() {
        if (this.hill.isLocked()) return;

        for (Participant participant : MatchManager.getParticipants()) {
            Player player = participant.getPlayer();
            boolean inside = participant.isState(ParticipantState.ALIVE) && this.hill.isInside(player.getLocation());
            boolean collect = inside && this.hill.getCapturedBy() == MatchTeam.getTeam(player);

            if (collect) this.collectObjectives(participant);

            if (collect && this.teleport) {
                SpawnUtils.teleportRandom(player, false);
                SoundUtils.SFX.TELEPORT.play(player);
            } else if (!ToggleSetting.PERMANENT_HILLS.isEnabled()) {
                if (inside) this.hill.enter(player);
                else this.hill.exit(player);
            }
        }
    }

    private void collectObjectives(Participant participant) {
        GameType gameType = EnumSetting.GAME_TYPE.get();
        Player player = participant.getPlayer();

        if (gameType == GameType.HEAD_HUNTER) {
            HeadModule.scoreHeads(participant);
        } else if (gameType == GameType.CAPTURE_THE_FLAG) {
            FlagUtils.captureFlag(participant);
        } else if (gameType == GameType.DEATHMATCH) {
            int points = NumberSetting.HILL_CAPTURE_POINTS.value();
            new RewardBuilder(player, points, "Entered Scorebox").reward();
            Bukkit.broadcastMessage(HillEvent.BROADCAST_PREFIX + " " + NickUtils.getDisplayName(player) +
                " scored " + ChatColor.AQUA + points + ChatColor.RESET + " points from " + this.hill.getName() + "!");
            participant.addScore(points);
        }
    }

}
