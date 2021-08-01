package net.purelic.cgm.utils;

public class PlaceUtils {

    public static String getPlace(int place) {
        return place + getPlaceSuffix(place) + " Place";
    }

    public static String getPlaceSuffix(int place) {
        if (place == 1 || (place % 10 == 1 && place > 20)) return "st";
        else if (place == 2 || (place % 10 == 2 && place > 20)) return "nd";
        else if (place == 3 || (place % 10 == 3 && place > 20)) return "rd";
        else return "th";
    }

}
