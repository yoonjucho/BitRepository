package com.example.bit_user.myapplication;


import android.app.Activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;


public class checkUpActivity extends Activity {

    // 탭의 추가/수정/제거 등은 모두 TabHost를 통해 이뤄짐.
    private TabHost mTabHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_up);

        // 실질적으로 우리가 보는 화면의 TabHost 인자를 넘겨받음.
      //  mTabHost = getTabHost();

        mTabHost.addTab(mTabHost.newTabSpec("tab1").setContent(new Intent(this,check_up_insertActivity.class))
                .setIndicator("Tab1"));

        mTabHost.addTab(mTabHost.newTabSpec("tab2").setContent(new Intent(this,check_up_listActivity.class))
                .setIndicator("Tab2"));

        mTabHost.addTab(mTabHost.newTabSpec("tab3").setContent(new Intent(this,check_up_newActivity.class))
                .setIndicator("Tab3"));

    }
    }



