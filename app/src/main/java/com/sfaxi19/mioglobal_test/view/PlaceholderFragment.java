package com.sfaxi19.mioglobal_test.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sfaxi19.mioglobal_test.MainActivity;
import com.sfaxi19.mioglobal_test.R;
import com.sfaxi19.mioglobal_test.sections.DiscoverSection;
import com.sfaxi19.mioglobal_test.sections.ISection;
import com.sfaxi19.mioglobal_test.sections.MainSection;
import com.sfaxi19.mioglobal_test.sections.SettingsSection;

/**
 * Created by sfaxi19 on 29.06.16.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private MainActivity activity;
    private final static String TEST_TAG ="Create and Destroy";

    public PlaceholderFragment() {
    }

    public void initFragment(int section_number, MainActivity activity){
        this.activity = activity;
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, section_number);
        this.setArguments(args);
    }

    public void link(MainActivity mainActivity){
        Log.d(TEST_TAG, "link fragment " + this.hashCode() + " to activity: " + mainActivity.hashCode());
        activity = mainActivity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TEST_TAG, "onCreateView section fragment: " + this.hashCode());// + " into " + activity.hashCode());
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        View rootSettingsView = inflater.inflate(R.layout.settings, container, false);
        //if(activity==null)return rootView;
        switch(getArguments().getInt(ARG_SECTION_NUMBER)) {
            case 1:
                ISection settingsSection = new SettingsSection(activity,rootSettingsView);
                settingsSection.view();
                return rootSettingsView;
            case 2:
                ISection mainSection = new MainSection(activity, rootView);
                mainSection.view();
                return rootView;
            case 3:
                ISection discoverSection = new DiscoverSection(activity, rootView);
                discoverSection.view();
                return rootView;
        }
        return null;
    }


    @Override
    public void onDestroyView() {
        Log.d(TEST_TAG, "onDestroyView section fragment: " + this.hashCode());
        super.onDestroyView();
    }
}