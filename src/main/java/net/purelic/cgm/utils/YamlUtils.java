package net.purelic.cgm.utils;

public class YamlUtils {

    public static int[] getCoords(String[] args) {
        int[] coords = new int[3];

        for (int i = 0; i < args.length; i++) {
            coords[i] = Integer.parseInt(args[i]);
        }

        return coords;
    }

}
