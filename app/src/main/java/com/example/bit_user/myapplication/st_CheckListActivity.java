package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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

import static com.github.kevinsawicki.http.HttpRequest.post;


public class st_CheckListActivity extends Activity implements DatePicker.OnDateChangedListener,View.OnClickListener{
    public static final String KEY_SIMPLE_DATA = "data";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private ArrayList<String> arrayList;
    private ArrayAdapter<String>adapter;

    private DatePicker stu_date;
    TextView dateText;
    ListView stu_check_in_list;
    Button stu_select_btn;
    String select_date;
    String year1;
    String month;
    String day;
    String id;
    String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stu_check_list);

        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("ID_DATA");
        id = bundleData.getString("ID");
        type = bundleData.getString("type");

        stu_date =(DatePicker)findViewById(R.id.stu_date);
        dateText = (TextView)findViewById(R.id.dateText);
        stu_check_in_list = (ListView)findViewById(R.id.stu_check_in_list);
        stu_select_btn = (Button)findViewById(R.id.stu_select_btn);

        stu_date.init(2015,12,1,this);

        arrayList = new ArrayList<String>();

        View header = (View)getLayoutInflater().inflate(R.layout.st_check_list_header,null);
        stu_check_in_list.addHeaderView(header);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,arrayList);
        stu_check_in_list.setAdapter(adapter);

        stu_select_btn.setOnClickListener(this);

        stu_check_in_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

    }
    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        dateText.setText(year+"년"+(monthOfYear+1)+"월"+dayOfMonth+"일");
        year1 = String.valueOf(year);
        month = String.valueOf(monthOfYear+1);
        day = String.valueOf(dayOfMonth);
        select_date = year1+"/"+month+"/"+day;
        Log.d("날짜",select_date);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.stu_select_btn:
               StuCheckList stuCheckList = new StuCheckList();
                stuCheckList.execute();
                break;
        }

    }

    public class StuCheckList extends SafeAsyncTask<ArrayList<HashMap>>{
        @Override
        //1.data 통신
        public ArrayList<HashMap> call() throws Exception {
            JSONResultString result = null;
            ArrayList<HashMap> arrayList1 = new ArrayList<HashMap>();
            try {

                HttpRequest request = post("http://192.168.1.13:8088/bitin/api/attd/by-userno");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                // 데이터 세팅
                JSONObject params1 = new JSONObject();
                params1.put("checkDay",select_date);
                params1.put("userId",id);
                params1.put("type",type);

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
                Log.d("-->data",result.getData().toString());//데이터받아오기
                arrayList1 = result.getData();
                // arrayList.add(result.getData().toString());
                Log.d("ar",arrayList1.toString());


                return result.getData();

            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return result.getData();

        }
        //2.오류시
        @Override
        protected void onException(Exception e) throws RuntimeException {
            super.onException(e);
        }
        //3.thead
        protected void onSuccess(ArrayList<HashMap> hashMaps) throws Exception {
            super.onSuccess(hashMaps);

            for(int i=0; i<hashMaps.size(); i++ )
            {
                arrayList.add("       "+
                      hashMaps.get(i).get("CLASS_NAME").toString()+"     "+
                      hashMaps.get(i).get("START_TIME").toString()+"       "+
                      hashMaps.get(i).get("STATUS").toString());


            }
            adapter.notifyDataSetChanged();

        }
        private class JSONResultString extends JSONResult<ArrayList<HashMap>> {


        }

    }

}
