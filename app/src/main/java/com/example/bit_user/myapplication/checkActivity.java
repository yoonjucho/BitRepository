package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bit_user.myapllication.core.JSONResult;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.Reader;

import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import static com.github.kevinsawicki.http.HttpRequest.post;


public class checkActivity extends Activity {


    public static final String KEY_SIMPLE_DATA = "data";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private ArrayList<String> arrayList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String>arrayListClassNo;
    String lesson;
    String codeNum;
    EditText check_CodeNum;
    Button check_Btn;
    TextView check_day;
    TextView check_time;
    private ListView check_list;
    EditText check_lesson;
    Double longitude;
    Double latitude;
    String id;
    String strDay;
    String strTime;
    Long count_timer;
    String timer = "100";
    Long timer1;
    String classNo;
    TextView stu_count;
    Bundle bundleData;

    private CountDownTimer countDownTimer; // built in android class
    // CountDownTimer
    private long totalTimeCountInMilliseconds; // total count down time in
    // milliseconds
    private long timeBlinkInMilliseconds; // start time of start blinking
    private boolean blink; // controls the blinking .. on and off

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("ID_DATA");
    /*    Bundle bundle = intent.getBundleExtra("DATA_LIST");
        Log.e("tiemer","되나이게"+bundle.getString("DATA"));*/
        Log.e("login", "!!!!!!!sample" + bundleData.getString("ID"));

        if (bundleData == null) {
            Toast.makeText(this, "Bundle data is null!", Toast.LENGTH_LONG).show();
            return;
        }

        id = bundleData.getString("ID");
        Toast.makeText(this, "ID is " + id, Toast.LENGTH_LONG).show();
        System.out.print(id);

        check_day = (TextView) findViewById(R.id.check_day);
        check_time = (TextView) findViewById(R.id.check_time);
        check_Btn = (Button) findViewById(R.id.check_Btn);
        check_list = (ListView) findViewById(R.id.check_list);
        check_lesson = (EditText) findViewById(R.id.check_lesson);
        check_CodeNum = (EditText) findViewById(R.id.check_codeNum);
        stu_count = (TextView) findViewById(R.id.stu_count);
        codeNum = check_CodeNum.getText().toString();

        Date date = new Date();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());


        strDay = dateformat.format(date);
        strTime = dateFormat.format(date);

        check_day.setText(strDay);
        check_time.setText("TIME" + strTime);
        View header = (View) getLayoutInflater().inflate(R.layout.stu_check_lesson_header, null);
        check_list.addHeaderView(header);


        //출석리스트 만들기
        arrayList = new ArrayList<String>();
        arrayListClassNo = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        setListViewData();


        check_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);


        check_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("position", "" + position);
                Log.d("getICount", "" + adapter.getCount());
                    if (adapter != null && position != 0) {
                    lesson = (String) adapter.getItem(position - 1);
                    classNo = (String)adapter.getItem(position-1);
                    Toast.makeText(getBaseContext(), lesson, Toast.LENGTH_SHORT).show();
                    check_lesson.setText("" + lesson);
                        TimerTask timerTask = new TimerTask();
                        timerTask.execute();
                        Test();
                        if (timer1 > 0) {
                            setTimer();
                            startTimer();
                        } else {
                            stu_count.setText("출석체크 끝!");
                        }
                } else {
                    Log.d("null 이다 ", "null 이다");
                }
            }
        });

        check_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ss", "ss");
                Test();
    /*            //str.toString();
                startLocationService();

                if(longitude==null && latitude==null){
                    Toast.makeText(getApplicationContext(), "GPS를 동의해주세요.", Toast.LENGTH_SHORT).show();
                }

                CheckTask asyncT = new CheckTask();
                asyncT.execute();

                 finish();*/
            }
        });
    }

    public void Test() {
        //시간 설정
        String start = "2015-12-18 오후 12:00:00";
        Calendar tempcal = Calendar.getInstance();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss");
        Date startday = sf.parse(start, new ParsePosition(0));

        long startTime = startday.getTime();

        //현재의 시간 설정
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        long endTime = endDate.getTime();

        long mills = endTime - startTime;
        String saaa = Long.toString(mills);
        Log.d("시간",endDate.toString());
        Log.d("시간2",Long.toString(endTime));

        Log.d("ddd", saaa);
        //분으로 변환
        long min = mills / 60000;

        StringBuffer diffTime = new StringBuffer();

        diffTime.append("시간의 차이는").append(min).append("분 입니다.");
        Log.d(diffTime.toString(), Long.toString(min));

        timer1 = Long.parseLong(timer) - min;
        Log.d("타이마", timer1.toString());

    }


    public void setListViewData() {
        LessonListTask listTask = new LessonListTask();
        listTask.execute();
     /*   Test();
        if (timer1 > 0) {
            setTimer();
            startTimer();
        } else {
            stu_count.setText("출석체크 끝!");
        }*/

    }

    public void addList(final List<HashMap> arrList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 해당 작업을 처리함

                        for (int i = 0; i < arrList.size(); i++) {
                            Log.d("addList", "addList--------------------->" + arrList.get(i) + "arrList.size()" + arrList.size());
                            adapter.add(arrList.get(i).get("CLASSNAME").toString() +
                                    arrList.get(i).get("STARTTIME").toString());
                            arrayListClassNo.add(arrList.get(i).get("classNo").toString());

                        }
                        check_list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    public ArrayList<String> addAdapter(ArrayList<String> arrayList) {
        return null;
    }

    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 위치 정보를 받을 리스너 생성
        GPSListener gpsListener = new GPSListener();

        long minTime = 10000;
        float minDistance = 0;

        // GPS를 이용한 위치 요청

        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        // 네트워크를 이용한 위치 요청
        manager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        // 위치 확인이 안되는 경우에도 최근에 확인된 위치 정보 먼저 확인
        try {
            Toast.makeText(getApplicationContext(), "위치 확인이 시작되었습니다. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();
            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        onStop();
    }

    /**
     * 리스너 클래스 정의 ....
     */
    private class GPSListener implements LocationListener {

        /**
         * 위치 정보가 확인될 때 자동 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            String msg = "두번째Latitude 22: " + latitude + "\nLongitude:22" + longitude;
            Log.i("GPSListener", msg);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

        }


        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    //출석리스트 불러오기
    private class LessonListTask extends AsyncTask<String, Void, List<HashMap>> {


        protected List<HashMap> doInBackground(String... params) {
            JSONResultString result;
            List<HashMap> arrayList1 = new ArrayList<HashMap>();
            try {

                HttpRequest request = post("http://192.168.1.13:8088/bitin/api/class/classlist");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                // 데이터 세팅
                JSONObject params1 = new JSONObject();
                params1.put("userId", id.toString());
                params1.put("nowDate", strDay.toString());


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
                Log.d("-->data", result.getData().toString());//데이터받아오기
                arrayList1 = result.getData();

                Log.d("ar", arrayList1.toString());

                addList(arrayList1);

                return result.getData();

            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }

        private class JSONResultString extends JSONResult<List<HashMap>> {

        }

    }

    //타이머시간 불러오기
    private class TimerTask extends AsyncTask<String, Void, List<HashMap>> {


        protected List<HashMap> doInBackground(String... params) {
            JSONResultString result;
            List<HashMap> arrayList1 = new ArrayList<HashMap>();
            try {

                HttpRequest request = post("http://192.168.1.13:8088/bitin/api/class/classlist");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                // 데이터 세팅
                JSONObject params1 = new JSONObject();
                params1.put("userId", id.toString());
                params1.put("classNo",classNo);



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
                Log.d("-->data", result.getData().toString());//데이터받아오기
                arrayList1 = result.getData();

                Log.d("ar", arrayList1.toString());

                addList(arrayList1);

                return result.getData();

            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }

        private class JSONResultString extends JSONResult<List<HashMap>> {

        }

    }

    //출석체크
    private class CheckTask extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... params) {
            JSONResultString result;

            try {

                HttpRequest request = post("http://192.168.1.13:8088/bitin/api/user/check");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                // 데이터 세팅
                JSONObject params1 = new JSONObject();
                params1.put("userId", id.toString());
                params1.put("codeNum", codeNum);
                params1.put("longitude", longitude);
                params1.put("latitude", latitude);


                Log.d("JoinData-->", params1.toString());


                // 요청
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

                result = GSON.fromJson(reader, JSONResultString.class);
                reader.close();

                //5. 사용하기
                Log.d("---> ResponseResult-->", result.getResult());  // "success"? or "fail"?


                return result.getResult();

            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }

        private class JSONResultString extends JSONResult<String> {
        }
    }


        public void setTimer () {
            count_timer = 0L;
            if (!timer1.toString().equals("")) {
                count_timer = timer1;

            } else
                Toast.makeText(checkActivity.this, "Please Enter Minutes...",
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


