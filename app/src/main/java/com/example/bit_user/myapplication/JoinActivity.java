package com.example.bit_user.myapplication;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.bit_user.myapllication.core.JSONResult;
import com.google.android.gcm.server.Sender;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static com.github.kevinsawicki.http.HttpRequest.get;
import static com.github.kevinsawicki.http.HttpRequest.post;

public class JoinActivity extends Activity {

    public static final String TAG = "JoinActivity";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    String requestURL = "http://192.168.1.13:8088/testserver2/list";
    String id;
    String password;
    String name;
    String phoneId;
    Button registerButton;
    EditText messageInput;
    TextView messageOutput;
    EditText Edit_id;
    EditText Edit_password;
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

        //페이지 스크롤 만들기~
   /*     page = (RelativeLayout)findViewById(R.id.page);
        page.setMovementMethod(new ScrollingMovementMethod());
*/
        // 서버 : GOOGLE_API_KEY를 이용해 Sender 초기화
        sender = new Sender(GCMInfo.GOOGLE_API_KEY);

        // 회원가입 버튼-> 단말기 등록
        registerButton = (Button) findViewById(R.id.joinbutton);
        Edit_id = (EditText) findViewById(R.id.Edit_id);
        Edit_password = (EditText) findViewById(R.id.Edit_password);
        Edit_name = (EditText) findViewById(R.id.Edit_name);
        radio_position = (RadioGroup) findViewById(R.id.radio_position);
        Phoneid = (RadioButton) findViewById(R.id.Phoneid);
     /*   String id = Edit_id.toString();
        String password = Edit_password.toString();
        String name = Edit_name.toString();*/

        radio_position.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int isChecked) {
                if (radioGroup == radio_position) {
                    if (isChecked == R.id.teacher) {
                        position = "teacher";
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
                    // 단말 등록하고 등록 ID 받기
                    registerDevice();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });



        registerButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    //

                    WebTask asyncT = new WebTask();
                    asyncT.execute();
                    chechSuccess(status);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("---> ", "Http Response Fail");
                }


            }
        });

    }

    private class WebTask extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... params) {

            try {

                HttpRequest request = post("http://192.168.1.13:8088/spring-ajax/api/example1");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType( "application/json", "UTF-8" );

                // 데이터 세팅
                JSONObject params1 = new JSONObject();
                params1.put("id",Edit_id.getText().toString());
                params1.put("password", Edit_password.getText().toString());
                params1.put("name", Edit_name.getText().toString());
                params1.put("type",radio_position.getCheckedRadioButtonId());
                params1.put("phoneId",phoneId.toString());

                Log.d("JoinData-->", params1.toString());


                // 요청
                request.send( params1.toString() );

                // query striing 으로 보내기
                //request.send(  "id=" + Edit_name.getText().toString() + "&password=" + Edit_name.getText().toString() + "&name=" +  Edit_name.getText().toString() );




                //HttpRequest request = HttpRequest.get("http://192.168.1.13:8088/testserver2/list?id=asdf");
                // 1. 타임 아웃 설정
                //request.connectTimeout(2000).readTimeout(2000);
                // 2. header 세팅
                //request.accept(HttpRequest.CONTENT_TYPE_JSON);

                // 3. 요청
                int responseCode = request.code();
                if (HttpURLConnection.HTTP_OK != responseCode) {
                    Log.e("HTTP fail-->", "Http Response Fail:" + responseCode );
                    return "오류";
                }else {
                    Log.e("HTTPRequest-->", "정상");

                }

                //4. JSON 파싱
                Reader reader = request.bufferedReader();
                //Log.d("Reader",reader);
                JSONResultString result = GSON.fromJson(reader, JSONResultString.class);
                reader.close();

                //5. 사용하기
                Log.d("---> ResponseResult-->", result.getResult() );  // "success"? or "fail"?

               /* Log.d("---> guestbook", result.getMessage() );*/

               /* Log.d("---> guestbook", result.getData() );*/
                status  = result.getResult();

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

                // 등록 ID 리스트에 추가 (현재는 1개만)
                //idList.clear();
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
        if(status.equals("success")) {
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
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
            return;

        }

    }
    private class JSONResultString extends JSONResult<String> {


    }
}