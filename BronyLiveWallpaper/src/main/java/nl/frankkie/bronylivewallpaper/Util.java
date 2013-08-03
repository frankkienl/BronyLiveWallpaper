package nl.frankkie.bronylivewallpaper;

/**
 * Created by FrankkieNL on 3-8-13.
 */
public class Util {

    public static float pointDirection(float x1, float y1, float x2, float y2) {
        //http://wiki.yoyogames.com/index.php/Point_direction
        //radtodeg(arctan2(y1-y2,x2-x1));
        return (float) Math.toDegrees(Math.atan2(y1 - y2, x2 - x1));
    }

    public static float pointDistance(float x1, float y1, float x2, float y2) {
        //http://wiki.yoyogames.com/index.php/Point_distance
        //dist=sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1))
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }
}
