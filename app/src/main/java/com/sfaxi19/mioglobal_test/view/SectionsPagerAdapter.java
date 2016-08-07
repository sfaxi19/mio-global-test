package com.sfaxi19.mioglobal_test.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.sfaxi19.mioglobal_test.MainActivity;
import com.sfaxi19.mioglobal_test.view.PlaceholderFragment;

/**
 * Created by sfaxi19 on 29.06.16.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {


    private static final String ARG_SECTION_NUMBER = "section_number";
    private MainActivity activity;
    private int pageCount=3;
    private final static String TEST_TAG ="Create and Destroy";

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void link(MainActivity mainActivity){
        Log.d(TEST_TAG, "link section adapter " + this.hashCode() + " to activity: " + mainActivity.hashCode());
        activity = mainActivity;
        if(fragment!=null) fragment.link(mainActivity);
    }

    public void unlink(){
        Log.d(TEST_TAG, "unlink section adapter " + this.hashCode() + " with activity: " + activity.hashCode());
        activity = null;
    }

    PlaceholderFragment fragment;

    @Override
    public Fragment getItem(int position) {
        Log.d(TEST_TAG, "section adapter get Item");
        fragment = new PlaceholderFragment();
        fragment.initFragment(position + 1, activity);
        return fragment;
    }


    @Override
    public int getCount() {
        return pageCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "SECTION 1";
            case 1:
                return "SECTION 2";
            case 2:
                return "SECTION 3";
        }
        return null;
    }

}


