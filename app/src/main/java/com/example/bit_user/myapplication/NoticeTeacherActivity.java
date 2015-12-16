package com.example.bit_user.myapplication;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.os.PowerManager;
import android.text.format.Time;
import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bit_user.myapllication.core.JSONResult;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import static com.github.kevinsawicki.http.HttpRequest.post;

public class NoticeTeacherActivity extends Activity {

    public static final String TAG = "NoticeTeacherActivity";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    public ArrayList<String> idList = new ArrayList<String>();
    public ArrayList<Map> lessonList = new ArrayList<Map>();
    private ArrayList<String> arrayList = new ArrayList<String>();
    private ArrayAdapter<String>adapter;

    TextView checkLessonNotice;
    ListView check_list;
    EditText noticeTitle;
    EditText noticeMessage;
    Button makeNoticeButton;

    Double lessonNumber;
    String lessonName;
    String title;

    String id;
    Sender sender;
    String regId;
    String status;
    String data;
    Handler handler = new Handler();
    private Random random ;
    private int TTLTime = 60;
    private	int RETRY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_notice);

        Intent intent = getIntent();
        if (intent != null) {
            processIntent(intent);
        }

        Bundle bundleData = intent.getBundleExtra("ID_DATA");

        if(bundleData == null){
            Toast.makeText(this, "Bundle data is null!",Toast.LENGTH_LONG).show();
            return;
        }

        id = bundleData.getString("ID");
        Toast.makeText(this, "ID is "+id ,Toast.LENGTH_LONG).show();

        sender = new Sender(GCMInfo.GOOGLE_API_KEY);

        checkLessonNotice = (TextView)findViewById(R.id.check_lesson_notice);
        check_list=(ListView)findViewById(R.id.lesson_list_notice_teacher);
        noticeTitle = (EditText)findViewById(R.id.notice_Title);
        noticeMessage = (EditText)findViewById(R.id.notice_Message);
        makeNoticeButton = (Button)findViewById(R.id.make_notice_button);

        arrayList =new ArrayList<String>();

        adapter= new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1,arrayList);
        check_list.setAdapter(adapter);
        check_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        //수업 목록 가져오기
        GCMTask gcmTask = new GCMTask();
        gcmTask.execute();

        check_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lessonName = (String) adapter.getItem(position);
                Toast.makeText(getBaseContext(), lessonName, Toast.LENGTH_SHORT).show();

                //id 등록하기
                registerDevice();
                checkLessonNotice.setText("" + lessonName);
            }
        });

        makeNoticeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                DBTask dbTask = new DBTask();
                dbTask.execute();

                title = noticeTitle.getText().toString();
                data = noticeMessage.getText().toString();
                sendToDevice(data);
            }
        });
    }

    private void registerDevice() {
        RegisterThread registerObj = new RegisterThread();
        registerObj.start();
    }

    class RegisterThread extends Thread {
        public void run() {
            try {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                regId = gcm.register(GCMInfo.PROJECT_ID);
                Log.d("regId", "" + regId);

                for (int i = 0; i < lessonList.size(); i++) {
                    if (lessonName == lessonList.get(i).get("CLASS_NAME").toString()) {
                        //idList.add(lessonList.get(i).get("USER_PHONE_ID").toString());
                        idList = (ArrayList)lessonList.get(i).get("PHONE_ID_LIST");
                        println("ID LIST !!!!!!!!!!!!!!!! "+idList.toString());
                        lessonNumber = Double.valueOf(lessonList.get(i).get("CLASS_NO").toString());
                        println("!!!!!!!!!!!!!!"+lessonNumber);
                    }
                }

                //idList.clear();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class GCMTask extends AsyncTask<String, Void, String> {
        ArrayList<Map> arrayList = new ArrayList<Map>();

        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/class/student-phone-list");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json","UTF-8" );

                JSONObject params1 = new JSONObject();
                params1.put("userId", id);
                request.send( params1.toString() );

                int responseCode = request.code();
                if (HttpURLConnection.HTTP_OK != responseCode) {
                    Log.e("HTTP fail-->", "Http Response Fail:" + responseCode );
                    return  "오류";
                }else {
                    Log.e("HTTPRequest-->", "정상");
                }

                Reader reader = request.bufferedReader();
                JSONResultString result = GSON.fromJson(reader, JSONResultString.class);
                reader.close();

                arrayList = result.getData();
                addList(arrayList);

                status  = result.getResult();
                Log.d("---> ResponseResult-->", result.getResult() );  // "success"? or "fail"?
                return result.getResult();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null ;
        }

        public ArrayList<String> addAdapter(ArrayList<String> arrayList)
        {
            return null;
        }

        public void addList(final ArrayList<Map> arrList){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            for(int i=0; i<arrList.size();i++)
                            {
                                Log.d("addList", "addList--------------------->" + arrList.get(i) + "arrList.size()        " + arrList.size());
                                lessonList.add(arrList.get(i));
                                adapter.add(arrList.get(i).get("CLASS_NAME").toString());
                            }
                        }
                    });
                }
            }).start();
        }

        private class JSONResultString extends JSONResult<ArrayList<Map>> {
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(NoticeTeacherActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }

    private class DBTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/notice/insert");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                JSONObject params1 = new JSONObject();
                params1.put("userId", id);
                params1.put("className", lessonName);
                params1.put("classNo", lessonNumber);
                params1.put("message", data);
                params1.put("title", title);

                request.send(params1.toString());

                Log.d("GCM Data-->", params1.toString());

                int responseCode = request.code();
                if (HttpURLConnection.HTTP_OK != responseCode) {
                    Log.e("HTTP fail-->", "Http Response Fail:" + responseCode);
                    return "오류";
                } else {
                    Log.e("HTTPRequest-->", "정상");
                }

                Reader reader = request.bufferedReader();
                JSONResultString result = GSON.fromJson(reader, JSONResultString.class);
                reader.close();

                status = result.getResult();
                Log.d("---> ResponseResult-->", result.getResult());  // "success"? or "fail"?
                return result.getResult();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }
        private class JSONResultString extends JSONResult<String> {
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(NoticeTeacherActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }

    private void sendToDevice(String data) {
        SendThread thread = new SendThread(data);
        thread.start();
    }

    class SendThread extends Thread {
        String data;

        public SendThread(String inData) {
            data = inData;
        }

        public void run() {
            try {
                sendText(data);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        public void sendText(String msg)
                throws Exception
        {
            if( random == null){
                random = new Random(System.currentTimeMillis());
            }
            String messageCollapseKey = String.valueOf(Math.abs(random.nextInt()));
            try {
                Message.Builder gcmMessageBuilder = new Message.Builder();
                gcmMessageBuilder.collapseKey(messageCollapseKey).delayWhileIdle(true).timeToLive(TTLTime);
                gcmMessageBuilder.addData("type", "text");
                gcmMessageBuilder.addData("command", "show");
                gcmMessageBuilder.addData("class", "notice");
                gcmMessageBuilder.addData("data", URLEncoder.encode(data, "UTF-8"));

                Message gcmMessage = gcmMessageBuilder.build();
                MulticastResult resultMessage = sender.send(gcmMessage, idList, RETRY);

                println(resultMessage.toString());
                String output = "GCM6 => " + resultMessage.getMulticastId()
                        + "," + resultMessage.getRetryMulticastIds() + "," + resultMessage.getSuccess();
                println(output);
            } catch(Exception ex) {
                ex.printStackTrace();
                String output = "GCM7 : " + ex.toString();
                println(output);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent() called.");
        processIntent(intent);
        super.onNewIntent(intent);
    }

    private void processIntent(Intent intent) {
        String from = intent.getStringExtra("from");
        if (from == null) {
            Log.d(TAG, "*********from is null.");
            return;
        }
        String command = intent.getStringExtra("command");
        String type = intent.getStringExtra("type");
        data = intent.getStringExtra("data");
        Log.d(TAG, "from : " + from + ", command : " + command + ", type : " + type + ", data : " + data+"sender"+ regId);
    }

    private void println(String msg) {
        final String output = msg;
        handler.post(new Runnable() {
            public void run() {
                Log.d(TAG, output);
                Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
            }
        });
    }

    /*
    private static PowerManager.WakeLock wakeLock;
    public static void acquire(Context context, long timeout) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP         |
                        PowerManager.FULL_WAKE_LOCK         |
                        PowerManager.ON_AFTER_RELEASE
                , context.getClass().getName());
        if(timeout > 0)
            wakeLock.acquire(timeout);
        else
            wakeLock.acquire();
    }
    */
}