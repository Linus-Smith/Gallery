package com.android.gallery3d.mediaCore.Utils;

/**
 * Created by linus.yang on 2016/12/12.
 */

public class Utils {

    public static void  checkNull(Object obj , String str) {
        if(obj == null) throw new NullPointerException(str);
    }


    /**
     * @return the current time in milliseconds since January 1, 1970 00:00:00.0 UTC.
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    // Returns the input value x clamped to the range [min, max].
    public static float clamp(float x, float min, float max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }
}
