package com.example.bit_user.myapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bit_user.myapllication.core.JSONResult;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Date;
import org.json.JSONObject;

import java.io.Reader;
import java.net.HttpURLConnection;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.github.kevinsawicki.http.HttpRequest.post;

public class check_up_insertActivity extends Activity implements View.OnClickListener{

    public static final String KEY_SIMPLE_DATA = "data";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();


    TextView nowdate;
    Button data_btn;
    ListView lesson_list;
    Button insert_btn;
    String id;
    Bundle bundleData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_up_insert);

        nowdate = (TextView)findViewById(R.id.date);
        String format = new String("yyyy.MM.dd HH:mm");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format,Locale.KOREA);
        nowdate.setText(simpleDateFormat.format(new Date()));


        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("ID_DATA");
        Log.e("login", "!!!!!!!sample" + bundleData.getString("ID"));

        if(bundleData == null){
            Toast.makeText(this, "Bundle data is null!", Toast.LENGTH_LONG).show();
            return;
        }
        id = bundleData.getString("ID");
        Toast.makeText(this, "ID is "+id ,Toast.LENGTH_LONG).show();
        System.out.print(id);
       // data_btn = (Button)findViewById(R.id.data_btn);
        lesson_list= (ListView)findViewById(R.id.lesson_list);
        insert_btn = (Button)findViewById(R.id.insert_btn);

        data_btn.setOnClickListener(this);
     //   lesson_list.setOnItemClickListener(this);

        insert_btn.setOnClickListener(this);
    }
    public void onClick(View v) {
        switch (v.getId()){
       /*     case R.id.data_btn:

                break;*/
            case R.id.insert_btn:
                CheckinsertTask asyncT = new CheckinsertTask();
                asyncT.execute();
                Intent intent = new Intent(this,check_up_newActivity.class);
                startActivity(intent);
                break;
        }
    }

    //출석체크
    private class CheckinsertTask extends AsyncTask<String, Void,String> {


        protected String doInBackground(String... params) {
            JSONResultString result;

            try {

                HttpRequest request = post("http://192.168.1.13:8088/testserver2/api/user/check");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                // 데이터 세팅(날짜,강의명,Id,)
                JSONObject params1 = new JSONObject();
                params1.put("userId", id.toString());
              //  params1.put("lesson",lesson_list.toString());
             //   params1.put("date", data_btn.toString());


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

}
