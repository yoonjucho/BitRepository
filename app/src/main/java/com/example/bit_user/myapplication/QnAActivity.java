package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bit_user.myapllication.core.JSONResult;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gcm.GCMRegistrar;
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

public class QnAActivity extends Activity {

    public static final String TAG = "QnAActivity";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    public ArrayList<String> idList = new ArrayList<String>();
    public ArrayList<Map> lessonList = new ArrayList<Map>();
    private ArrayList<String> arrayList = new ArrayList<String>();
    private ArrayAdapter<String>adapter;

    public String bundleId;
    EditText ppt_number;
    String pptNo;
    ListView check_list;
    String id;
    public String teacherId;
    EditText messageInput;
    TextView messageOutput;
    Sender sender;
    String lessonName;
    TextView check_lesson;
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
        setContentView(R.layout.activity_qn_a);

        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("ID_DATA");

        if(bundleData == null){
            Toast.makeText(this, "Bundle data is null!",Toast.LENGTH_LONG).show();
            return;
        }

        id = bundleData.getString("ID");
        Toast.makeText(this, "ID is "+id ,Toast.LENGTH_LONG).show();

        sender = new Sender(GCMInfo.GOOGLE_API_KEY);
        messageInput = (EditText) findViewById(R.id.ask_Message);
        messageOutput = (TextView) findViewById(R.id.messageOutput);
        ppt_number = (EditText) findViewById(R.id.ppt_number);

        check_list=(ListView)findViewById(R.id.qna_teacher);
        check_lesson=(TextView)findViewById(R.id.check_lesson_qna);

        arrayList =new ArrayList<String>();
        arrayList.add(getString(R.string.lessonCheck));

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
                for (int i = 0; i < lessonList.size(); i++) {
                    if (lessonName == lessonList.get(i).get("CLASS_NAME").toString()) {
                        idList.clear();
                        //idList.add(lessonList.get(i).get("USER_PHONE_ID").toString());
                        teacherId = lessonList.get(i).get("USER_PHONE_ID").toString();
                        bundleId = lessonList.get(i).get("USER_ID").toString();
                        idList.add(teacherId);
                    }
                }

                //id 등록하기
                registerDevice();
                check_lesson.setText("" + lessonName);
            }
        });

        Button askButton = (Button) findViewById(R.id.ask_btn);
        askButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                data = messageInput.getText().toString();
                pptNo = ppt_number.getText().toString();
                DBTask dbTask = new DBTask();
                dbTask.execute();

                sendToDevice(data);
            }
        });

        if (intent != null) {
            processIntent(intent);
        }
    }

    private void registerDevice() {
        RegisterThread registerObj = new RegisterThread();
        registerObj.start();
    }

    class RegisterThread extends Thread {
        public void run() {
            try {

                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                GCMRegistrar.checkDevice(getApplicationContext());
                GCMRegistrar.checkManifest(getApplicationContext());
                regId = gcm.register(GCMInfo.PROJECT_ID);
                GCMRegistrar.register(getApplicationContext(), GCMIntentService.SEND_ID);
                println("------------>ID : " + regId);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class GCMTask extends AsyncTask<String, Void, String> {
        ArrayList<Map> arrayList = new ArrayList<Map>();

        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/class/classinfo-by-userid");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json","UTF-8" );

                JSONObject params1 = new JSONObject();
                params1.put("userId", id);
                Log.d("GCM Data-->", params1.toString());

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
            Toast.makeText(QnAActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }

    private class DBTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/qna/create-q");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                JSONObject params1 = new JSONObject();
                params1.put("senderId", id);
                params1.put("receiverId", teacherId);
                params1.put("message", data);
                params1.put("pptNo", pptNo);
                params1.put("lesson", lessonName);

                Log.d("GCM Data-->", params1.toString());

                request.send(params1.toString());

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
            Toast.makeText(QnAActivity.this, result, Toast.LENGTH_LONG).show();
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
                sendText("질문이 도착했습니다.");
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
                gcmMessageBuilder.addData("class", "qna");
                gcmMessageBuilder.addData("bundleId", bundleId);
                gcmMessageBuilder.addData("data", URLEncoder.encode(data, "UTF-8"));

                Log.d(TAG,idList.toString());
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
            return;
        }
        String command = intent.getStringExtra("command");
        String type = intent.getStringExtra("type");
        data = intent.getStringExtra("data");
        Log.d(TAG, "from : " + from + ", command : " + command + ", type : " + type + ", data : " + data+ "sender : " + regId);

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
}