package com.example.bit_user.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

public class VoteResultActivity extends Activity {

    LinearLayout linearChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_result);
        linearChart = (LinearLayout) findViewById(R.id.linearChart);
        drawChart(5);
    }

    public void drawChart(int count) {
        System.out.println(count);
        for (int k = 1; k <= count; k++) {
            View view = new View(this);
            view.setBackgroundColor(Color.parseColor("#ff6233"));
            view.setLayoutParams(new LinearLayout.LayoutParams(30, 400));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
            params.setMargins(5, 0, 0, 0); // substitute parameters for left,top, right, bottom
            view.setLayoutParams(params);
            linearChart.addView(view);
        }
    }
}
