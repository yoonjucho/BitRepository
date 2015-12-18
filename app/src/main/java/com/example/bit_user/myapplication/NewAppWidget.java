package com.example.bit_user.myapplication;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.example.bit_user.myapplication.checkActivity;


/**
 * Implementation of App Widget functionality.
 */
public class NewAppWidget extends AppWidgetProvider {
    Button refresh;
    private CountDownTimer countDownTimer; // built in android class
    // CountDownTimer
    private long totalTimeCountInMilliseconds; // total count down time in
    // milliseconds
    private long timeBlinkInMilliseconds; // start time of start blinking
    private boolean blink; // controls the blinking .. on and off
    String timer = "100";
    Long timer1;
    TextView stu_count;

       @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
/*
        startTimer();*/
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
        // views.setTextViewText(R.id.appwidget_text, widgetText);
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setComponent(new ComponentName(context, MainActivity.class));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, i, 0);
        views.setOnClickPendingIntent(R.id.startApp_btn, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);


        //   views.setOnClickPendingIntent(R.id.refresh_btn, pendingIntent);



    }
    public void startTimer() {

        checkActivity check = new checkActivity();
        check.Test();
        check.setTimer();
        countDownTimer = new CountDownTimer(totalTimeCountInMilliseconds, 500) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                if (leftTimeInMilliseconds < timeBlinkInMilliseconds) {
                    if (blink) {
                        stu_count.setVisibility(View.VISIBLE);
                        // if blink is true, textview will be visible
                    } else {
                        stu_count.setVisibility(View.INVISIBLE);
                    }

                    blink = !blink; // toggle the value of blink
                }

                stu_count.setText(String.format("%02d", seconds / 60)
                        + ":" + String.format("%02d", seconds % 60));

            }

            @Override
            public void onFinish() {
                stu_count.setText("Time up!");
                stu_count.setVisibility(View.VISIBLE);
                //start_btn.setVisibility(View.VISIBLE);

        /*        if(stu_count.toString().equals("Time up!")) {

                    Intent intent = new Intent(getBaseContext(), checkListActivity.class);
                    Bundle bundleData = new Bundle();
                    bundleData.putString("ID", id);
                    intent.putExtra("ID_DATA", bundleData);
                    startActivity(intent);
                    finish();

                }*/

            }

        }.start();

    }



    }







