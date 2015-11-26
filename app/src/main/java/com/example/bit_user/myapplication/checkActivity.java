package com.example.bit_user.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.Reader;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.github.kevinsawicki.http.HttpRequest.post;


public class checkActivity extends Activity  {

    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private ArrayList<String> arrayList;
    private ArrayAdapter<String>adapter;
    String[] data = {"수업목록"};
    public String lesson;
    Button check_Btn;
    ListView check_list;
    EditText check_lesson;
    public Double longitude;
    public Double latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        check_Btn  = (Button)findViewById(R.id.check_Btn);
        check_list=(ListView)findViewById(R.id.check_list);
        check_lesson=(EditText)findViewById(R.id.check_lesson);
        //출석리스트 만들기

        arrayList =new ArrayList<String>();
        arrayList.add("안녕");
        arrayList.add("하이");

        adapter= new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1,arrayList);

        check_list.setAdapter(adapter);

        check_list.setAdapter(adapter);
        check_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        check_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lesson = adapter.getItem(position).toString();
                Toast.makeText(getBaseContext(),lesson,Toast.LENGTH_SHORT).show();
                check_lesson.setText(""+lesson);
            }
        });
        check_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //str.toString();
                startLocationService();

                WebTask asyncT = new WebTask();
                asyncT.execute();

                finish();
            }
        });
    }
    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 위치 정보를 받을 리스너 생성
        GPSListener gpsListener = new GPSListener();

        long minTime = 10000;
        float minDistance = 0;

        // GPS를 이용한 위치 요청
        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        // 네트워크를 이용한 위치 요청
        manager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                gpsListener);

        // 위치 확인이 안되는 경우에도 최근에 확인된 위치 정보 먼저 확인
        try {
            Toast.makeText(getApplicationContext(), "위치 확인이 시작되었습니다. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();
            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                latitude = lastLocation.getLatitude();
                longitude = lastLocation.getLongitude();
                Toast.makeText(getApplicationContext(), "Last Known Location : " + "Latitude : "+ latitude + " Longitude:"+ longitude, Toast.LENGTH_LONG).show();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 리스너 클래스 정의 ....
     */
    private class GPSListener implements LocationListener {
        /**
         * 위치 정보가 확인될 때 자동 호출되는 메소드
         */
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            String msg = "Latitude : "+ latitude + "\nLongitude:"+ longitude;
            Log.i("GPSListener", msg);
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }

        public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    //출석리스트 구현


    private class WebTask extends AsyncTask<String, Void, String> {


        protected String doInBackground(String... params) {

            try {

                HttpRequest request = post("http://192.168.1.13:8088/testserver2/api/user/check");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                Log.d("latitude",latitude.toString());
                Log.d("longitude",longitude.toString());
                // 데이터 세팅
                JSONObject params1 = new JSONObject();
                params1.put("latitude",latitude);
                params1.put("longitude",longitude);
                params1.put("lesson",lesson);

                Log.d("JoinData-->", params1.toString());

                // 요청
                request.send(params1.toString());

                // query string 으로 보내기
                //request.send(  "id=" + Edit_name.getText().toString() + "&password=" + Edit_name.getText().toString() + "&name=" +  Edit_name.getText().toString() );
                //HttpRequest request = HttpRequest.get("http://192.168.1.13:8088/testserver2/list?id=asdf");
                // 1. 타임 아웃 설정
                //request.connectTimeout(2000).readTimeout(2000);
                // 2. header 세팅
                //request.accept(HttpRequest.CONTENT_TYPE_JSON);
                // 3. 요청
                int responseCode = request.code();
                if (HttpURLConnection.HTTP_OK != responseCode) {
                    Log.e("HTTP fail-->", "Http Response Fail:" + responseCode);
                    return "오류";
                } else {
                    Log.e("HTTPRequest-->", "정상");
                }

                //4. JSON 파싱
                Reader reader = request.bufferedReader();
                //Log.d("Reader",reader);
                JSONResultString result = GSON.fromJson(reader, JSONResultString.class);
                reader.close();

                //5. 사용하기
                Log.d("---> ResponseResult-->", result.getResult());  // "success"? or "fail"?
                //Log.d("-->data",result.getData());//데이터받아오기

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