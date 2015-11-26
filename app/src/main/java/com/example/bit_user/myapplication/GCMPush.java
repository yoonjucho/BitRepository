package com.example.bit_user.myapplication;

        import android.app.Activity;
        import android.content.Intent;
        import android.os.Bundle;
        import android.os.Handler;

        import android.util.Log;

        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;


        import com.google.android.gcm.server.Message;
        import com.google.android.gcm.server.MulticastResult;
        import com.google.android.gcm.server.Sender;
        import com.google.android.gms.gcm.GoogleCloudMessaging;

        import java.net.URLEncoder;
        import java.util.ArrayList;
        import java.util.Random;


public class GCMPush extends Activity {
    public static final String TAG = "GCMPush";

    EditText messageInput;
    TextView messageOutput;
    Sender sender;
    String regId;
    Handler handler = new Handler();

    private Random random ;

    private int TTLTime = 60;

    private	int RETRY = 3;

    ArrayList<String> idList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gcmpush);
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

        Intent intent = getIntent();
        if (intent != null) {
            processIntent(intent);
        }
    }

    private void registerDevice() {
        RegisterThread registerObj = new RegisterThread();
        registerObj.start();
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
                idList.add(regId);
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

        Log.d(TAG, "from : " + from + ", command : " + command + ", type : " + type + ", data : " + data+"sender"+ regId);
        //println("DATA : " + command + ", " + type + ", " + data+ "from"+ regId);
        messageOutput.setText("Message from [" + from + "] : " + data);
    }
}
