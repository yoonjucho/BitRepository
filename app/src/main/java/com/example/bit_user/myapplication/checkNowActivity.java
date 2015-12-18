package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bit_user.myapllication.core.JSONResult;
import com.example.bit_user.myapllication.core.SafeAsyncTask;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.Reader;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.github.kevinsawicki.http.HttpRequest.post;


public class  checkNowActivity extends Activity implements OnClickListener {
    public static final String KEY_SIMPLE_DATA = "data";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    int i=-1;
    String id;
    String lesson;
    Long endTime;
    Date endDate;
    int count_timer;
    String codenum;
    String classNo;
    String timer;
    Bundle bundleData;
    TextView code_num;
    TextView count;
    Button start_btn;
    ProgressBar mProgressBar;
    ArrayList<String> datalist;
    private ArrayList<String> arrayList;
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

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss");
        Calendar cal = Calendar.getInstance();
        endDate = cal.getTime();
        endTime = endDate.getTime();


        code_num = (TextView) findViewById(R.id.code_num);
        count = (TextView) findViewById(R.id.count);
        start_btn = (Button) findViewById(R.id.start_btn);

        arrayList = new ArrayList<String>();

        id = datalist.get(0);
        Log.d("ID",id);


        codenum = datalist.get(2);//앞 엑티비티에서 인증번호 값 받아오기
        code_num.setText("" + codenum);//TextView에 인증번호 띄우기

        classNo = datalist.get(4);
        Log.d("classNo",classNo);

        timer = datalist.get(1);//앞 엑티비티에서 타이머 값 받아오기
        //count.setText(timer);
        count.setText(timer + "분");
        start_btn.setOnClickListener(this);

    }
    public void onClick(View v) {
        TimerData timerData = new TimerData();
        timerData.execute();

        if(v.getId() == R.id.start_btn){
            setTimer();

            InputMethodManager imm = (InputMethodManager)getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            start_btn.setVisibility(View.GONE);
            startTimer();
        }
    }

    public void setTimer() {
        count_timer = 0;
        if (!timer.toString().equals("")) {
            count_timer = Integer.parseInt(timer);
        } else
            Toast.makeText(checkNowActivity.this, "Please Enter Minutes...",
                    Toast.LENGTH_LONG).show();
        totalTimeCountInMilliseconds = 60 * count_timer * 1000;
        timeBlinkInMilliseconds = 30 * 1000;
    }

    public void startTimer() {
        countDownTimer = new CountDownTimer(totalTimeCountInMilliseconds, 500) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                long seconds = leftTimeInMilliseconds / 1000;
                if (leftTimeInMilliseconds < timeBlinkInMilliseconds) {
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

            }

            @Override
            public void onFinish() {
                count.setText("Time up!");
                count.setVisibility(View.VISIBLE);
                start_btn.setVisibility(View.VISIBLE);

                if(count.toString().equals("Time up!")) {

                    Intent intent = new Intent(getBaseContext(), checkListActivity.class);
                    Bundle bundleData = new Bundle();
                    bundleData.putString("ID", id);
                    intent.putExtra("ID_DATA", bundleData);
                    startActivity(intent);
                    finish();

                }

            }

        }.start();

    }

    public class TimerData extends SafeAsyncTask<ArrayList<String>> {
        @Override
        //1.data 통신
        public ArrayList<String>  call() throws Exception {
            JSONResultString result = null;
            ArrayList<String> arrayList1 = new ArrayList<String>();
            try {

                HttpRequest request = post("http://192.168.1.13:8088/bitin/api/class/start-attd ");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                // 데이터 세팅
                JSONObject params1 = new JSONObject();
                params1.put("classNo",classNo);
                params1.put("startTime",endDate);
                params1.put("timer",count_timer);


                Log.d("JoinData-->", params1.toString());
                request.send(params1.toString());

                // 3. 요청
                int responseCode = request.code();
                if (HttpURLConnection.HTTP_OK != responseCode) {
                    Log.e("HTTP fail-->", "Http Response Fail:" + responseCode);
                    return null;
                } else {
                    Log.e("HTTPRequest-->", "정상");

                }

                //4. JSON 파싱
                Reader reader = request.bufferedReader();
                //Log.d("Reader",reader);
                result = GSON.fromJson(reader, JSONResultString.class);
                reader.close();

                //5. 사용하기
                Log.d("---> ResponseResult-->", result.getResult());  // "success"? or "fail"?
                Log.d("--->Data-->",result.getData().toString());

                return result.getData();

            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return result.getData();

        }
        //2.오류시
        @Override
        protected void onException(Exception e) throws RuntimeException {
            super.onException(e);
        }

        protected void onSuccess(ArrayList<String> String) throws Exception {

            super.onSuccess(String);

            for(int i = 0; i<String.size(); i++){
                arrayList.add(String.get(i).toString());
                Log.d(" arrayList",arrayList.toString());
            }


        }
        private class JSONResultString extends JSONResult<ArrayList<String>> {

        }

    }
}
