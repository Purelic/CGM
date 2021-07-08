package net.purelic.cgm.utils;

public class YamlUtils {

    public static double[] getCoords(String[] args) {
        double[] coords = new double[3];

        for (int i = 0; i < args.length; i++) {
            coords[i] = Double.parseDouble(args[i]);
        }

        return coords;
    }

}
