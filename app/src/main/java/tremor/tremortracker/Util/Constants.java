package tremor.tremortracker.Util;

import java.util.Random;

/**
 * Created by sonal on 04-03-2018.
 */

public class Constants {
    public static final String URL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.geojson";
    public static final int LIMIT=60;

    public static int randomInt(int max, int min) {
        return new Random().nextInt(max - min) + min;

    }

}

