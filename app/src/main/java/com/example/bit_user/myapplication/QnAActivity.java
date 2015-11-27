package com.example.bit_user.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;

public class QnAActivity extends Activity implements View.OnClickListener {
    EditText ask_Message;
    Button ask_btn;
    Sender sender;
    String regId;
    Handler handler = new android.os.Handler();
    private Random random ;
    private int TTLTime = 60;
    private	int RETRY = 3;
    ArrayList<String> idList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qn_a);
        sender = new Sender(GCMInfo.GOOGLE_API_KEY);
        ask_Message = (EditText)findViewById(R.id.ask_Message);
        ask_btn = (Button)findViewById(R.id.ask_btn);
        registerDevice();

       ask_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
            String data = ask_Message.getText().toString();
            sendToDevice(data);
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
                Log.e("ID : ",  regId);

                //idList.clear();
                idList.add(regId);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
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
                Log.d("output",output);

            } catch(Exception ex) {
                ex.printStackTrace();
                String output = "GCM1 : " + ex.toString();
                Log.d("output", output);
            }
        }
    }
}
