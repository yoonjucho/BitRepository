package com.example.bit_user.myapplication;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;
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
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.github.kevinsawicki.http.HttpRequest.post;


/**
 * Implementation of App Widget functionality.
 */

public class NewAppWidget extends AppWidgetProvider {


    Context mContext ;
    public static final String KEY_SIMPLE_DATA = "data";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    public static ArrayList<String> arrayList;
    public static HashMap<String,Object>arrayList1;
    private HashMap<String,Object>arrayListClassNo;
    Long timer;
    Long timer1;
    public static String lesson;
    public static String notice;
    String classNo;
    String start;
    String id;
    String strDay;
    public static ComponentName mService = null;
    public static boolean endServiceFlag = false;
    public static Long count_timer;
    static SharedPreferences setting;
    RemoteViews updateViews;

       @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
           updateViews = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);
          setting = context.getSharedPreferences("Setting", context.MODE_PRIVATE);
          id =  setting.getString("id","");
          mContext = context;
          Date date = new Date();
          SimpleDateFormat dateformat = new SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault());
          strDay = dateformat.format(date);
         Log.d("wedget!!!!!",strDay);
         Log.d("wedget!!!!!",id);

           TimerGet timerGet = new TimerGet();
             timerGet.execute();
           NoticeGet noticeGet =new NoticeGet();
           noticeGet.execute();

           final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);

            AppWidgetManager widgetMgr = AppWidgetManager.getInstance(context);
            NewAppWidget.updateWidget(context,widgetMgr,appWidgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        }

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




    }

    public class NoticeGet extends SafeAsyncTask<ArrayList<HashMap>> {
        @Override
        public ArrayList<HashMap> call() throws Exception {
            JSONResultString result = null;
            ArrayList<HashMap> arrayList1 = new ArrayList<HashMap>();
            try {


                HttpRequest request = post("http://192.168.1.32:8088/bitin//api/notice/list");


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

                return result.getData();

            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return result.getData();

        }


        @Override
        protected void onException(Exception e) throws RuntimeException {
            super.onException(e);
        }

        @Override
        protected void onSuccess(ArrayList<HashMap> hashMaps) throws Exception {
            super.onSuccess(hashMaps);
            arrayList1 = new HashMap<String,Object>();
            for (int i = 0; i < hashMaps.size(); i++) {
                arrayList1.put("className",hashMaps.get(i).get("className").toString());
                arrayList1.put("message",hashMaps.get(i).get("message"));
            }
            notice = arrayList1.get("className").toString()+":"+arrayList1.get("message").toString();

        }
        private class JSONResultString extends JSONResult<ArrayList<HashMap>> {

        }
    }

    public class TimerGet extends SafeAsyncTask<ArrayList<HashMap>> {
        @Override
        public ArrayList<HashMap> call() throws Exception {
            JSONResultString result = null;
            ArrayList<HashMap> arrayList1 = new ArrayList<HashMap>();
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

                return result.getData();

            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return result.getData();

        }


        @Override
        protected void onException(Exception e) throws RuntimeException {
            super.onException(e);
        }

        @Override
        protected void onSuccess(ArrayList<HashMap> hashMaps) throws Exception {
            super.onSuccess(hashMaps);
            arrayList = new ArrayList<String>();
            arrayListClassNo = new HashMap<String,Object>();
            for (int i = 0; i < hashMaps.size(); i++) {
                arrayListClassNo.put("CLASSNAME",hashMaps.get(i).get("CLASSNAME").toString());
                arrayListClassNo.put("CLASSNO",hashMaps.get(i).get("CLASSNO").toString());
                arrayListClassNo.put("ATTDTIME",hashMaps.get(i).get("ATTDTIME").toString());
                arrayListClassNo.put("TIMERMIN",hashMaps.get(i).get("TIMERMIN"));
            }
            Log.d("ara",arrayListClassNo.toString());
            lesson = (String) arrayListClassNo.get("CLASSNAME");
            classNo = (String)arrayListClassNo.get("CLASSNO");
            timer = ((Double)arrayListClassNo.get("TIMERMIN")).longValue();
            start = (String)arrayListClassNo.get("ATTDTIME");

                Calendar tempcal = Calendar.getInstance();
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd a hh:mm:ss");
                Log.d("startday",start.toString());

                Date startday = sf.parse(start, new ParsePosition(0));

                Log.d("startday",startday.toString());

                long startTime = startday.getTime();
                Log.d("startTime",Long.toString(startTime));

                //현재의 시간 설정
                Calendar cal = Calendar.getInstance();
                Date endDate = cal.getTime();
                long endTime = endDate.getTime();

                long mills = endTime - startTime;
                String saaa = Long.toString(mills);
                Log.d("시간",endDate.toString());
                Log.d("시간2",Long.toString(endTime));


                //분으로 변환
                long min = mills / 60000;

                StringBuffer diffTime = new StringBuffer();

                diffTime.append("시간의 차이는").append(min).append("분 입니다.");
                //Log.d("타이머",timer);
                //  timer = Long.parseLong(timemin.toString());

                timer1 =  timer- min;
                Log.d("타이머", timer1.toString());


                setTimer();

        }
        private class JSONResultString extends JSONResult<ArrayList<HashMap>> {

        }
    }
    static void updateWidget(Context context,
                             AppWidgetManager appWidgetManager,
                             int appWidgetId){

        endServiceFlag = false;
        mService = context.startService(new Intent(context, UpdateService.class));
    }

    /*
	 * 위젯의 remoteView 갱신 시간은 1.6이상부터 기본 30분으로 지정되어있음.
	 * 따라서 30분 이하의 갱신을 원할 경우에는 Service를 상속받은 내부 클래스를 선언하여 시간 조절.
	 */
    public static class UpdateService extends Service implements Runnable {

        private Handler mHandler;
        private static final int TIMER_PERIOD = 1 * 1000;
        long totalTimeCountInMilliseconds; // total count down time in
        // milliseconds
        long timeBlinkInMilliseconds; // start time of start blinking
        public CountDownTimer countDownTimer; // built in android class
        // CountDownTimer
        private boolean blink; // controls the blinking .. on and off

        long leftTimeInMilliseconds;
        @Override
        public void onCreate(){
            mHandler = new Handler();
        }

        @Override
        public void onStart(Intent intent, int startId){
            count_timer = System.currentTimeMillis();// - DAY_TIME;
            mHandler.postDelayed(this, 1000);
        }

        @Override
        public IBinder onBind(Intent intent) {

            return null;
        }

        /*
         * 위젯 화면 업데이트를 주기적으로 하기위해 run 함수 호출.
         */
        @Override
        public void run() {
            Long day = count_timer / (60 * 60 * 24);
            Long hour = (count_timer - day * 60 * 60 * 24) / (60 * 60);
            Long minute = (count_timer - day * 60 * 60 * 24 - hour * 3600) / 60;
            Long second = count_timer % 60;

           String pre_count = hour+":"+minute+":"+second;

            //여기서는 버튼의 글자를 갱신한다.
            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.new_app_widget);

             views.setTextViewText(R.id.stu_count, ""+pre_count);
            views.setTextViewText(R.id.lessonName,lesson);
            views.setTextViewText(R.id.notice_view,notice);

            /* views.setTextViewText(R.id.stu_count,String.format("%02d", seconds / 60)
                        + ":" + String.format("%02d", seconds % 60));*/
            count_timer--;

            //위젯이 업데이트 되었음을 알림.
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            ComponentName testWidge = new ComponentName(this, NewAppWidget.class);
            appWidgetManager.updateAppWidget(testWidge, views);

            if(endServiceFlag){
                return;
            }else{
                mHandler.postDelayed(this, TIMER_PERIOD);
            }
        } //run end!!!

    }// class end

    //timer섫정
    public void setTimer () {
        count_timer = 0L;
        if (!timer1.toString().equals("")) {
            count_timer = timer1;

        }

    }
}













