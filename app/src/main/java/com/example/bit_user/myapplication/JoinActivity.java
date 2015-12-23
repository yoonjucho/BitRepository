package com.example.bit_user.myapplication;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.bit_user.myapllication.core.JSONResult;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gcm.server.Sender;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.Reader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Random;

import static com.github.kevinsawicki.http.HttpRequest.post;

public class JoinActivity extends Activity {

    public static final String TAG = "JoinActivity";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    //String requestURL = "http://192.168.1.13:8088/testserver2/list";
    String id;
    String password;
    String name;
    String phoneId;
    Button registerButton;
    String phoneNumber;
    String email;
    EditText Edit_id;
    EditText Edit_password;
    EditText Edit_email;
    EditText Edit_phone;
    EditText Edit_name;
    RadioGroup radio_position;
    String position;
    RadioButton Phoneid;
    Sender sender; //서버 : Sender 객체 선언
    Handler handler = new Handler();
    RelativeLayout page;
    String status;
    private Random random; //collapseKey 설정을 위한 Random 객체
    private int TTLTime = 60; //구글 서버에 메시지 보관하는 기간(초단위로 4주까지 가능)
    private int RETRY = 3; //단말기에 메시지 전송 재시도 횟수
    ArrayList<String> idList = new ArrayList<String>(); //등록된 ID 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // 서버 : GOOGLE_API_KEY를 이용해 Sender 초기화
        sender = new Sender(GCMInfo.GOOGLE_API_KEY);

        // 회원가입 버튼-> 단말기 등록
        registerButton = (Button) findViewById(R.id.joinbutton);
        Edit_id = (EditText) findViewById(R.id.Edit_id);
        Edit_password = (EditText) findViewById(R.id.Edit_password);
        Edit_email = (EditText) findViewById(R.id.Edit_email);
        Edit_phone = (EditText) findViewById(R.id.Edit_phone);
        Edit_name = (EditText) findViewById(R.id.Edit_name);
        radio_position = (RadioGroup) findViewById(R.id.radio_position);
        Phoneid = (RadioButton) findViewById(R.id.Phoneid);

        radio_position.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int isChecked) {
                if (radioGroup == radio_position) {
                    if (isChecked == R.id.teacher) {
                        position = "teacher";
                        Log.d(TAG, "eeeeeeeeeeeeeeeeeeee"+position);
                    } else if (isChecked == R.id.student) {
                        position = "student";
                    } else if (isChecked == R.id.employee) {
                        position = "employee";
                    } else {
                        position = "";
                    }
                }
            }
        });

        Phoneid.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    registerDevice();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        registerButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                id = Edit_id.getText().toString();
                password = Edit_password.getText().toString();
                name = Edit_name.getText().toString();
                email = Edit_email.getText().toString();
                phoneNumber = Edit_phone.getText().toString();

                if(position.isEmpty()){
                    Toast.makeText(JoinActivity.this, "타입을 설정해주세요.", Toast.LENGTH_LONG).show();
                }else if(id.isEmpty()){
                    Toast.makeText(JoinActivity.this, "id를 입력해주세요.", Toast.LENGTH_LONG).show();
                }else if(password.isEmpty()){
                    Toast.makeText(JoinActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show();
                }else if(name.isEmpty()){
                    Toast.makeText(JoinActivity.this, "이름을 입력해주세요.", Toast.LENGTH_LONG).show();
                }else if(email.isEmpty()){
                    Toast.makeText(JoinActivity.this, "이메일을 입력해주세요.", Toast.LENGTH_LONG).show();
                }else if(phoneNumber.isEmpty()){
                    Toast.makeText(JoinActivity.this, "휴대폰 번호를 입력해주세요.", Toast.LENGTH_LONG).show();
                }
                else {
                    try {
                        WebTask asyncT = new WebTask();
                        asyncT.execute();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.e("---> ", "Http Response Fail");
                    }
                }
            }
        });

    }

    private class WebTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {

            try {

                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/user/join");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType( "application/json", "UTF-8" );

                // 데이터 세팅
                JSONObject params1 = new JSONObject();
                params1.put("userId",id);
                params1.put("userPassword", password);
                params1.put("userName", name);
                params1.put("userPhone", phoneNumber);
                params1.put("userEmail", email);
                params1.put("userType",position);
                params1.put("phoneId",phoneId.toString());

                Log.d("JoinData-->", params1.toString());

                request.send(params1.toString());

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

                //5. 사용하기
                Log.d("---> ResponseResult-->", result.getResult() );  // "success"? or "fail"?

                status  = result.getResult();
                chechSuccess(status);

                return result.getResult();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null ;
        }

        @Override
        protected void onPostExecute(String result) {
        // JSON 결과확인
            super.onPostExecute(result);
            Toast.makeText(JoinActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }

    private void registerDevice() { //단말 등록
        RegisterThread registerObj = new RegisterThread();
        registerObj.start();
    }

    class RegisterThread extends Thread {
        public void run() {
            try {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                phoneId = gcm.register(GCMInfo.PROJECT_ID);
                println("푸시 서비스를 위해 단말을 등록했습니다.");
                println("등록 ID : " + phoneId);
                idList.add(phoneId);
            } catch (Exception ex) {
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

    public void chechSuccess(String status){
        if(status !=null && status.equals("success")) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }else if(status != null && status.equals("fail")){
            ProgressDialog dialog = null;
            dialog.dismiss();
            AlertDialog.Builder builder3 = new AlertDialog.Builder(JoinActivity.this);
            builder3.setMessage( "오류.").setCancelable(false)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,int id) {
                        }
                    });
            AlertDialog alert = builder3.create();
            alert.show();
            //return;
        }
    }

    private class JSONResultString extends JSONResult<String> {
    }
}