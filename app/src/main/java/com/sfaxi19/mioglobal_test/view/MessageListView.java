package com.sfaxi19.mioglobal_test.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by sfaxi19 on 28.06.16.
 */
public class MessageListView extends LinearLayout implements View.OnClickListener{

    private ArrayList<String> addressList=null;
    private int deviceNum=-1;
    private int id=0;
    private Button focusButton=null;

    public MessageListView(Context context, ViewGroup parent) {
        super(context);
        this.setOrientation(LinearLayout.VERTICAL);
        this.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        ScrollView scrollView = new ScrollView(getContext());
        scrollView.addView(this);
        parent.addView(scrollView);
        addressList = new ArrayList<>();
        //LinearLayout parentLayout = (LinearLayout)view;
       // parentLayout.addView(this);
    }

    public String getAddress(){
        if(deviceNum!=-1) {
            return addressList.get(deviceNum);
        }else return null;
    }

    public void writeText(String text){
        TextView text_view = new TextView(getContext());
        text_view.setText(text);
        this.addView(text_view);
    }

    public void writeButton(String name, String address){
        Button btn = new Button(getContext());
        btn.setText(name + "\n" + address);
        btn.setId(id);
        addressList.add(id, address);
        btn.setOnClickListener(this);
        this.addView(btn);
        id++;
    }

    @Override
    public void onClick(View v) {
        Button button = (Button)v;
        deviceNum = button.getId();
        button.setTextColor(Color.BLUE);
        if(focusButton!=null && focusButton!=button)focusButton.setTextColor(Color.BLACK);
        focusButton = button;
    }
}
