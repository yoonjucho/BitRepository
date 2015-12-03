package com.example.bit_user.myapplication;

        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.Handler;

        import android.os.PowerManager;
        import android.util.Log;

        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
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
        import java.util.Random;

        import static com.github.kevinsawicki.http.HttpRequest.post;

public class GCMPush extends Activity {
    public static final String TAG = "GCMPush";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    ArrayList<String> idList = new ArrayList<String>();

    String id;
    EditText messageInput;
    TextView messageOutput;
    Sender sender;
    String receiver[];
    String regId;
    String status;
    Handler handler = new Handler();
    private Random random ;
    private int TTLTime = 60;
    private	int RETRY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcmpush);

        Intent intent = getIntent();
        if (intent != null) {
            processIntent(intent);
        }

        Bundle bundleData = intent.getBundleExtra("ID_DATA");
        Log.e("login", "!!!!!!!sample" + bundleData.getString("ID"));

        if(bundleData == null){
            Toast.makeText(this, "Bundle data is null!",Toast.LENGTH_LONG).show();
            return;
        }

        id = bundleData.getString("ID");
        Toast.makeText(this, "ID is "+id ,Toast.LENGTH_LONG).show();
        System.out.print(id);

        sender = new Sender(GCMInfo.GOOGLE_API_KEY);
        messageInput = (EditText) findViewById(R.id.messageInput);
        messageOutput = (TextView) findViewById(R.id.messageOutput);

        registerDevice();

        Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String data = messageInput.getText().toString();
                sendToDevice(data);
            }
        });
    }

    public void addList(ArrayList<String> arrList){
        for(int i=0; i<arrList.size();i++)
        {
            Log.d("addList","addList--------------------->"+arrList.get(i) +"arrList.size()        " + arrList.size()  );
            idList.add(arrList.get(i).toString());
        }
    }

    private void registerDevice() {
        GCMTask gcmTask = new GCMTask();
        gcmTask.execute();

        RegisterThread registerObj = new RegisterThread();
        registerObj.start();
    }

    private class GCMTask extends AsyncTask<String, Void, String> {
        ArrayList<String> arrayList = new ArrayList<String>();

        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.13:8088/testserver2/api/user/phoneidlist-by-groupno");
                request.connectTimeout(2000).readTimeout(2000);


                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType( "application/json", "UTF-8" );


                JSONObject params1 = new JSONObject();
                //params1.put("id", id);
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                //일단 project_id 를 보내주는거로 함!! 고쳐줘야함
                params1.put("id", gcm.register(GCMInfo.PROJECT_ID));
                Log.d("GCM Data-->", params1.toString());

                request.send( params1.toString() );

                int responseCode = request.code();
                if (HttpURLConnection.HTTP_OK != responseCode) {
                    Log.e("HTTP fail-->", "Http Response Fail:" + responseCode );
                    return "오류";
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

        private class JSONResultString extends JSONResult<ArrayList<String>> {
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(GCMPush.this, result, Toast.LENGTH_LONG).show();
        }
    }

    private void sendToDevice(String data) {
        SendThread thread = new SendThread(data);
        thread.start();
    }

    class RegisterThread extends Thread {
        public void run() {
            try {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                regId = gcm.register(GCMInfo.PROJECT_ID);
                println("ID : " + regId);
                //idList.clear();
                //idList.add(regId);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
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
                gcmMessageBuilder.addData("type","text");
                gcmMessageBuilder.addData("command", "show");
                gcmMessageBuilder.addData("class","gcm");
                gcmMessageBuilder.addData("data", URLEncoder.encode(data, "UTF-8"));

                Message gcmMessage = gcmMessageBuilder.build();
                MulticastResult resultMessage = sender.send(gcmMessage, idList, RETRY);

                String output = "GCM3 => " + resultMessage.getMulticastId()
                        + "," + resultMessage.getRetryMulticastIds() + "," + resultMessage.getSuccess();
                println(output);

            } catch(Exception ex) {
                ex.printStackTrace();
                String output = "GCM1 : " + ex.toString();
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
        String data = intent.getStringExtra("data");
        Log.d(TAG, "from : " + from + ", command : " + command + ", type : " + type + ", data : " + data + "sender" + regId);
        messageOutput.setText("Message from [" + from + "] : " + data);
    }

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
}
