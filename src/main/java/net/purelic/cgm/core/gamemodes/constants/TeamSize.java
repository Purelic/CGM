package net.purelic.cgm.core.gamemodes.constants;

public enum TeamSize {

    SINGLES,
    DOUBLES,
    TRIOS,
    MINI,
    NORMAL,
    BIG,
    MEGA,
    ;

    public static int maxPlayers(TeamSize teamSize, TeamType teamType) {
        if (teamSize == TeamSize.SINGLES) {
            return 1;
        } else if (teamSize == TeamSize.DOUBLES) {
            return 2;
        } else if (teamSize == TeamSize.TRIOS) {
            return 3;
        } else if (teamSize == TeamSize.MINI) {
            return teamType.getSize() / 2;
        } else if (teamSize == TeamSize.BIG) {
            return teamType.getSize() * 2;
        } else if (teamSize == TeamSize.MEGA) {
            return teamType.getSize() * 4;
        } else {
            return teamType.getSize();
        }
    }

}
