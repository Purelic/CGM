package net.purelic.cgm.match;

import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.NumberSetting;
import net.purelic.cgm.core.gamemodes.constants.GameType;
import net.purelic.cgm.match.constants.ParticipantState;
import net.purelic.cgm.match.stats.ParticipantStats;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class MatchParticipant {

    private final Player player;
    private ParticipantState state;
    private ParticipantStats stats;
    private int lives;

    public MatchParticipant(Player player) {
        this.player = player;
        this.state = ParticipantState.QUEUED;
        this.stats = new ParticipantStats();
        this.lives = 0;
    }

    public Player getPlayer() {
        return this.player;
    }

    public boolean isState(ParticipantState... states) {
        return Arrays.asList(states).contains(this.state);
    }

    public void setState(ParticipantState state) {
        this.state = state;
    }

    public ParticipantStats getStats() {
        return this.stats;
    }

    public int getLives() {
        return this.lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void resetLives() {
        if (EnumSetting.GAME_TYPE.is(GameType.BED_WARS)) this.lives = -1;
        else this.lives = NumberSetting.LIVES_PER_ROUND.value();
    }

}
