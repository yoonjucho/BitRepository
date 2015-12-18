package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bit_user.myapllication.core.JSONResult;
import com.example.bit_user.myapllication.core.SafeAsyncTask;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.Reader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.github.kevinsawicki.http.HttpRequest.post;


public class CheckListDataActivity extends Activity {
    String checkNo;
    String lessonName;
    Bundle bundleData;
    TextView lesson_name;
    ListView check_data_list;
    private ArrayList<String> arrayList;
    private ArrayAdapter<String> adapter;
    public static final String KEY_SIMPLE_DATA = "data";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list_data);

        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("checkNo_date");
        Log.d("checkNo여기는checkListData", bundleData.getString("checkNo"));
        checkNo =  bundleData.getString("checkNo");

        lesson_name = (TextView) findViewById(R.id.lesson_name);
        check_data_list = (ListView) findViewById(R.id.check_data_list);

       /* lessonName = bundleData.getString("checkNo");
        lesson_name.setText(lessonName);*/

        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        check_data_list.setAdapter(adapter);

        setListViewData();


    }

    public void setListViewData() {
        checkListData checkListData = new checkListData();
        checkListData.execute();
    }

    public class checkListData extends SafeAsyncTask<ArrayList<HashMap>> {
        @Override
        public ArrayList<HashMap> call() throws Exception {
            JSONResultString result = null;
            ArrayList<HashMap> arrayList1 = new ArrayList<HashMap>();
            try {

                HttpRequest request = post("http://192.168.1.13:8088/bitin/api/attd/by-attdno");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                // 데이터 세팅
                JSONObject params1 = new JSONObject();
                params1.put("attdNo",checkNo);
                Log.d("JoinData-->", params1.toString());
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
                //Log.d("Reader",reader);
                result = GSON.fromJson(reader, JSONResultString.class);
                reader.close();

                //5. 사용하기
                Log.d("---> ResponseResult-->", result.getResult());  // "success"? or "fail"?
                Log.d("-->data", result.getData().toString());//데이터받아오기
                arrayList1 = result.getData();
                Log.d("ar", arrayList1.toString());

                return result.getData();

            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return result.getData();

        }


        @Override
        protected void onException(Exception e) throws RuntimeException {
            super.onException(e);
        }

        @Override
        protected void onSuccess(ArrayList<HashMap> hashMaps) throws Exception {
            super.onSuccess(hashMaps);

            for (int i = 0; i < hashMaps.size(); i++) {

                arrayList.add(hashMaps.get(i).get("USERNAME").toString()+"         "+
                                hashMaps.get(i).get("STATUS"));


            }

            adapter.notifyDataSetChanged();
        }


        private class JSONResultString extends JSONResult<ArrayList<HashMap>> {

        }
    }

}