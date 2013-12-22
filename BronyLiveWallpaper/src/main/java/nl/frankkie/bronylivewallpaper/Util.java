/*
 * Copyright (C) 2013 FrankkieNL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.frankkie.bronylivewallpaper;

/**
 * Created by FrankkieNL on 3-8-13.
 */
public class Util {

    public static final String LOCATION_ASSETS = "assets";
    public static final String LOCATION_SDCARD = "/sdcard/Ponies/";

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

    public static boolean isInMane6(String ponyName){
        String[] mane6 = new String[]{"Applejack", "Rainbow Dash", "Fluttershy", "Rarity", "Pinkie Pie", "Princess Twilight Sparkle"};
        for (String name : mane6){
            if (ponyName.equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }
}
