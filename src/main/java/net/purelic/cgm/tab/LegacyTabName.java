package net.purelic.cgm.tab;

import org.bukkit.ChatColor;

public enum LegacyTabName {

    CHAR01("!"),
    CHAR02("@"),
    CHAR03("#"),
    CHAR04("$"),
    CHAR05("%"),
    CHAR06("^"),
    CHAR07("&"),
    CHAR08("*"),
    CHAR09("("),
    CHAR10(")"),
    CHAR11("_"),
    CHAR12("-"),
    CHAR13("+"),
    CHAR14("="),
    CHAR15("{"),
    CHAR16("}"),
    CHAR17("["),
    CHAR18("]"),
    CHAR19("'"),
    CHAR20("."),
    CHAR21("<"),
    CHAR22(">"),
    CHAR23("/"),
    CHAR24("?"),
    CHAR25(":"),
    CHAR26(";"),
    CHAR27("\""),
    CHAR28("|"),
    CHAR29("\u00B3"),
    CHAR30("\u00BB"),
    CHAR31("\u00B4"),
    CHAR32("A"),
    CHAR33("B"),
    CHAR34("D"),
    CHAR35("E"),
    CHAR36("F"),
    CHAR37("g"),
    CHAR38("G"),
    CHAR39("h"),
    CHAR40("H"),
    CHAR41("i"),
    CHAR42("I"),
    CHAR43("j"),
    CHAR44("J"),
    CHAR45("K"),
    CHAR46("L"),
    CHAR47("M"),
    CHAR48("N"),
    CHAR49("O"),
    CHAR50("p"),
    CHAR51("P"),
    CHAR52("q"),
    CHAR53("Q"),
    CHAR54("R"),
    CHAR55("s"),
    CHAR56("S"),
    CHAR57("t"),
    CHAR58("T"),
    CHAR59("u"),
    CHAR60("U"),
    CHAR61("v"),
    CHAR62("V"),
    CHAR63("w"),
    CHAR64("W"),
    CHAR65("x"),
    CHAR66("X"),
    CHAR67("y"),
    CHAR68("Y"),
    CHAR69("z"),
    CHAR70("Z"),
    CHAR71("\u00A1"),
    CHAR72("\u00A2"),
    CHAR73("\u00A3"),
    CHAR74("\u00A4"),
    CHAR75("\u00A5"),
    CHAR76("\u00AE"),
    CHAR77("\u00B0"),
    CHAR78("\u00A8"),
    CHAR79("\u00A9"),
    CHAR80("\u00AA"),
    ;

    private final String value;

    LegacyTabName(String value) {
        this.value = ChatColor.COLOR_CHAR + value + ChatColor.RESET;
    }

    @Override
    public String toString() {
        return this.value;
    }

}
