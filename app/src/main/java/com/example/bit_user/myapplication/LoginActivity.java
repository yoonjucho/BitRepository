package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.bit_user.myapllication.core.JSONResult;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gcm.server.Sender;
import com.google.android.gms.nearby.connection.Connections;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Random;

import static com.github.kevinsawicki.http.HttpRequest.post;

public class LoginActivity extends Activity {

    public static final String TAG = "LoginActivity";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    //String requestURL = "http://192.168.1.13:8088/testserver2/list";

    Button loginButton;
    EditText Login_id;
    EditText Login_password;
    Handler handler = new Handler();
    Button joinform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        joinform=(Button) findViewById(R.id.joinform);
        loginButton = (Button) findViewById(R.id.loginbutton);
        Login_id = (EditText) findViewById(R.id.Login_id);
        Login_password = (EditText) findViewById(R.id.Login_password);

        joinform.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),JoinActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    WebTask asyncT = new WebTask();
                    asyncT.execute();
                    Log.e("---> ", "Http Response");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("---> ", "Http Response Fail");
                }

                Intent intent = new Intent(getBaseContext(),MenuActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private class WebTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {

            try {
                //HttpClient client = new DefaultHttpClient();
                HttpRequest request = post("http://192.168.1.13:8088/testserver2/api/user/login");
                request.connectTimeout(2000).readTimeout(2000);

                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType( "application/json", "UTF-8" );

                //json params
                JSONObject params1 = new JSONObject();
                params1.put("userId",Login_id.getText().toString());
                params1.put("userPassword", Login_password.getText().toString());
                //Log.d("--->Login :", GSON.toJson(UserVo));

                // 요청
                request.send(params1.toString());

                int responseCode = request.code();
                if (HttpURLConnection.HTTP_OK != responseCode) {
                    Log.e("--->Login :", "Http Response Fail:" + responseCode );
                    return "오류";
                }else {
                    Log.e("정상", "정상");
                }

                Reader reader = request.bufferedReader();
                JSONResultString result = GSON.fromJson(reader, JSONResultString.class);
                reader.close();

                //5. 사용하기
                Log.d("---> Login", result.getResult() );

                return result.getResult();

            } catch (Exception e3) {
                e3.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
// JSON 결과확인

            super.onPostExecute(result);
            Toast.makeText(LoginActivity.this, result, Toast.LENGTH_LONG).show();
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


    private class JSONResultString extends JSONResult<String> {

    }
}