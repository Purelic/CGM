package net.purelic.cgm.core.match;

import net.purelic.cgm.core.constants.MatchTeam;
import net.purelic.cgm.core.gamemodes.EnumSetting;
import net.purelic.cgm.core.gamemodes.constants.TeamType;
import net.purelic.commons.utils.NickUtils;
import org.bukkit.entity.Player;

public class RoundResult {

    private final MatchTeam winningTeam;
    private final Participant winningParticipant;
    private final boolean soloGameMode;

    public RoundResult(MatchTeam winningTeam) {
        this(winningTeam, null);
    }

    public RoundResult(Participant winningParticipant) {
        this(winningParticipant == null ? null : winningParticipant.getTeam(), winningParticipant);
    }

    private RoundResult(MatchTeam winningTeam, Participant winningParticipant) {
        this.winningTeam = winningTeam;
        this.winningParticipant = winningParticipant;
        this.soloGameMode = EnumSetting.TEAM_TYPE.is(TeamType.SOLO);
    }

    public boolean isDraw() {
        return this.soloGameMode ? this.winningParticipant == null : this.winningTeam == null;
    }

    public Participant getWinningParticipant() {
        return this.winningParticipant;
    }

    public MatchTeam getWinningTeam() {
        return this.winningTeam;
    }

    public boolean isWinner(Player player) {
        if (this.isDraw()) return false;
        return this.soloGameMode ? this.winningParticipant.getPlayer() == player : MatchTeam.getTeam(player) == this.winningTeam;
    }

    public String getWinner() {
        if (this.isDraw()) return "";
        return this.soloGameMode ? NickUtils.getDisplayName(this.winningParticipant.getPlayer()) : this.winningTeam.getColoredName();
    }

}
