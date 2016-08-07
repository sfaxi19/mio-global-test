package com.sfaxi19.mioglobal_test;

import com.sfaxi19.mioglobal_test.sections.DiscoverSection;

/**
 * Created by sfaxi19 on 06.07.16.
 */
public class MyCallback {

    DiscoverSection discoverSection;

    public MyCallback(DiscoverSection discoverSection) {
        this.discoverSection = discoverSection;
    }

    public MyCallback(){

    }
    public void viewDevice(String name, String address){
        discoverSection.listView.writeButton(name, address);
    }

    public void finishDiscover(int devCount){
        discoverSection.scanBtn.setText("Повторить");
        discoverSection.scanBtn.setEnabled(true);
        if(devCount>0){
            discoverSection.connectButtonView();
        }
    }
}
