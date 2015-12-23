package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class  checkNowActivity extends Activity implements OnClickListener {

    int i=-1;
    String id;
    String lesson;
    int count_timer;
    String codenum;
    String timer;
    Bundle bundleData;
    TextView code_num;
    TextView count;
    Button start_btn;
    ProgressBar mProgressBar;
    ArrayList<String> datalist;
    private ArrayAdapter<String> adapter;
    private CountDownTimer countDownTimer; // built in android class
    // CountDownTimer
    private long totalTimeCountInMilliseconds; // total count down time in
    // milliseconds
    private long timeBlinkInMilliseconds; // start time of start blinking
    private boolean blink; // controls the blinking .. on and off

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_now);
        Intent intent = getIntent();
        datalist = new ArrayList<String>();
        bundleData = intent.getBundleExtra("DATA");
        datalist = bundleData.getStringArrayList("DATA_LIST");
        Log.d("dd", datalist.toString());

        code_num = (TextView) findViewById(R.id.code_num);
        count = (TextView) findViewById(R.id.count);
        start_btn = (Button) findViewById(R.id.start_btn);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        mProgressBar.setMax(60*count_timer);

        codenum = datalist.get(2);//앞 엑티비티에서 인증번호 값 받아오기
        code_num.setText("" + codenum);//TextView에 인증번호 띄우기

        timer = datalist.get(1);//앞 엑티비티에서 타이머 값 받아오기
        //count.setText(timer);
        count.setText(timer + "분");
        start_btn.setOnClickListener(this);

    }
    public void onClick(View v) {
        if(v.getId() == R.id.start_btn){
            setTimer();

            //Hides the Keyboard

            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
          //imm.hideSoftInputFromWindow(timer.getWindowToken(), 0);

            //buttonStopTime.setVisibility(View.VISIBLE);
            start_btn.setVisibility(View.GONE);
           // edtTimerValue.setVisibility(View.GONE);
            //edtTimerValue.setText("");

            startTimer();
        }
    }

    private void setTimer() {
        count_timer = 0;
        if (!timer.toString().equals("")) {
            count_timer = Integer.parseInt(timer);
        } else
            Toast.makeText(checkNowActivity.this, "Please Enter Minutes...",
                    Toast.LENGTH_LONG).show();

        totalTimeCountInMilliseconds = 60 * count_timer * 1000;
        timeBlinkInMilliseconds = 30 * 1000;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(totalTimeCountInMilliseconds, 500) {
            // 500 means, onTick function will be called at every 500
            // milliseconds

            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                //i++;
                //Setting the Progress Bar to decrease wih the timer
                mProgressBar.setProgress((int) (leftTimeInMilliseconds / 1000));
                // textViewShowTime.setTextAppearance(getApplicationContext(),);

                if (leftTimeInMilliseconds < timeBlinkInMilliseconds) {
                    //   textViewShowTime.setTextAppearance(getApplicationContext(),
                    //   R.style.blinkText);
                    // change the style of the textview .. giving a red
                    // alert style

                    if (blink) {
                        count.setVisibility(View.VISIBLE);
                        // if blink is true, textview will be visible
                    } else {
                        count.setVisibility(View.INVISIBLE);
                    }

                    blink = !blink; // toggle the value of blink
                }

                count.setText(String.format("%02d", seconds / 60)
                        + ":" + String.format("%02d", seconds % 60));
                // format the textview to show the easily readable format

            }

            @Override
            public void onFinish() {
                // this function will be called when the timecount is finished
                count.setText("Time up!");
                count.setVisibility(View.VISIBLE);
                start_btn.setVisibility(View.VISIBLE);
               // buttonStopTime.setVisibility(View.GONE);
               // timer.setVisibility(View.VISIBLE);

            }

        }.start();

    }

}
