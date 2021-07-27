package net.purelic.cgm.core.runnables;

import net.purelic.cgm.core.constants.MatchState;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.core.managers.MatchManager;
import net.purelic.cgm.core.maps.flag.Flag;
import net.purelic.cgm.core.maps.hill.Hill;
import net.purelic.cgm.core.match.Participant;
import net.purelic.cgm.utils.FlagUtils;
import net.purelic.cgm.utils.HillUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ObjectiveTracker extends BukkitRunnable {

    private final GameType gameType;

    public ObjectiveTracker() {
        this(EnumSetting.GAME_TYPE.get());
    }

    public ObjectiveTracker(GameType gameType) {
        this.gameType = gameType;
    }

    @Override
    public void run() {
        if (!MatchState.isState(MatchState.STARTED)) {
            this.cancel();
            return;
        }

        for (Participant participant : MatchManager.getParticipants()) {
            Player player = participant.getPlayer();
            Location targetLoc = null;

            if (this.gameType == GameType.CAPTURE_THE_FLAG) {
                Flag flag = FlagUtils.getClosestFlag(player);
                targetLoc = flag != null ? FlagUtils.getCurrentFlagLocation(flag) : null;
            } else if (this.gameType == GameType.KING_OF_THE_HILL
                || this.gameType == GameType.HEAD_HUNTER) {
                Hill hill = HillUtils.getClosestHill(player);
                targetLoc = hill != null ? hill.getCenter() : null;
            }

            if (targetLoc == null) {
                player.setCompassTarget(player.getLocation());
            } else {
                player.setCompassTarget(targetLoc);
            }
        }
    }

}
