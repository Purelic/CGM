package net.purelic.cgm.core.runnables;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.utils.BedUtils;
import net.purelic.cgm.utils.PlayerUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerTracker extends BukkitRunnable {

    @Override
    public void run() {
        if (!MatchState.isState(MatchState.STARTED)) {
            this.cancel();
            return;
        }

        for (Participant participant : MatchManager.getParticipants()) {
            Player player = participant.getPlayer();
            Player target = PlayerUtils.getClosestEnemy(player);

            if (target == null
                    || (EnumSetting.GAME_TYPE.is(GameType.BED_WARS) && !BedUtils.canUseTracker(player))) {
                player.setCompassTarget(player.getLocation());
            } else {
                player.setCompassTarget(target.getLocation());
            }
        }
    }

}
