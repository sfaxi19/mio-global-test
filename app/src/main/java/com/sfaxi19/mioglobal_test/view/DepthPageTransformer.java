package com.sfaxi19.mioglobal_test.view;

import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import java.util.HashMap;

/**
 * Created by sfaxi19 on 06.07.16.
 */
public class DepthPageTransformer implements ViewPager.PageTransformer {


    private static final float MIN_SCALE = 0.75f;
    private static final String TRANSFORM_TAG = "transform log";

    public void transformPage(View view, float position) {

        int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            Log.d(TRANSFORM_TAG,"[..,-1]");
            view.setAlpha(0);
        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            Log.d(TRANSFORM_TAG,"[-1,0]");
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            Log.d(TRANSFORM_TAG,"(0,1]");
            view.setAlpha(1-position);

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
            Log.d(TRANSFORM_TAG,"Scale:[" + scaleFactor + "]");

            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            Log.d(TRANSFORM_TAG,"[1,..]");
            view.setAlpha(0);
        }
    }
}

