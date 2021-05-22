package net.purelic.cgm.core.stats.constants;

public enum MatchResult {

    WIN,
    LOSS,
    DRAW,
    ;

    public static MatchResult getResult(boolean hasWinner, int place, boolean topWin) {
        if (topWin) {
            if (place <= 3) return MatchResult.WIN;
            else return MatchResult.LOSS;
        } else if (place == 1) {
            if (!hasWinner) return MatchResult.DRAW;
            else return MatchResult.WIN;
        } else {
            return MatchResult.LOSS;
        }
    }

}
