package com.sfaxi19.mioglobal_test;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by sfaxi19 on 14.07.16.
 */
public class MyViewPager extends ViewPager {

    private boolean active = false;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return active ? super.onInterceptTouchEvent(ev) : false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return active ? super.onTouchEvent(ev) : false;
    }

    public void setActive(boolean active){
        this.active = active;
    }
}
