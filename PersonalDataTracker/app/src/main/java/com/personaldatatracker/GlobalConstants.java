package com.personaldatatracker;

import android.content.Context;

import com.personaldatatracker.engine.CategoryGroup;

public class GlobalConstants {

    public static CategoryGroup categoryGroup = null;

    public static void init(Context context) {
        categoryGroup = new CategoryGroup(context);
    }


    public static String[] gif_paths = {
            "https://cookieattack.files.wordpress.com/2015/04/tumblr_n0v2cccqar1qc4uvwo1_250.gif",
            "https://media.giphy.com/media/xT9DPPqwOCoxi3ASWc/giphy.gif",
            "https://media.giphy.com/media/cuA6BiPOI0gXS/giphy.gif"
    };
}
