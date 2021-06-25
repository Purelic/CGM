package net.purelic.cgm.match.constants;

public enum RoundResult {

    INCOMPLETE("\u2b1c"), // ⬜
    DRAW("\u2592"), // ▒
    COMPLETE("\u2b1b"), // ⬛
    ;

    private final String symbol;

    RoundResult(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return this.symbol;
    }

}
