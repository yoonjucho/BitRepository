package com.example.bit_user.myapplication;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.os.PowerManager;
import android.text.format.Time;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.Map;
import java.util.Random;

import static com.github.kevinsawicki.http.HttpRequest.post;

public class MessageTeacher extends Activity {

    public static final String TAG = "MessageTeacher";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    public ArrayList<String> idList = new ArrayList<String>();
    public ArrayList<Map> qnaList = new ArrayList<Map>();
    private ArrayList<Map> listReturn = new ArrayList<Map>();
    private ArrayList<String> arrayList = new ArrayList<String>();
    private CusromAdapter adapter;

    public double qnaNumber;
    TextView qna_lesson;
    String qnaLesson;
    TextView qna_ppt;
    String qnaPpt;
    TextView qna_name;
    String qnaName;
    TextView qna_time;
    String qnaTime;
    TextView ask_message;
    String askMessage;

    ListView check_list;
    String id;

    Sender sender;

    String regId;
    String status;
    String data;
    Handler handler = new Handler();
    private Random random ;
    private int TTLTime = 60;
    private	int RETRY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_message);

        Intent intent = getIntent();

        Bundle bundleData = intent.getBundleExtra("ID_DATA");

        if(bundleData == null){
            Toast.makeText(this, "Bundle data is null!",Toast.LENGTH_LONG).show();
            return;
        }

        id = bundleData.getString("ID");
        Toast.makeText(this, "ID is "+id ,Toast.LENGTH_LONG).show();

        sender = new Sender(GCMInfo.GOOGLE_API_KEY);

        qna_lesson = (TextView) findViewById(R.id.QnA_lesson);
        qna_ppt = (TextView) findViewById(R.id.ppt_number);
        qna_name = (TextView) findViewById(R.id.QnA_name);
        qna_time = (TextView) findViewById(R.id.qnaTime);
        ask_message = (TextView) findViewById(R.id.ask_Message);
        check_list=(ListView)findViewById(R.id.qna_teacher);

        adapter = new CusromAdapter(this, 0, listReturn );
        this.check_list.setAdapter(adapter);
        check_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        this.adapter.notifyDataSetChanged();

        //질문 목록 가져오기
        qnaTask qTask = new qnaTask();
        qTask.execute();

    }

    private class CusromAdapter extends ArrayAdapter<Map>
    {
        private ArrayList<Map> m_listItem;

        public CusromAdapter(Context context, int textViewResourceId, ArrayList<Map> objects )
        {
            super(context, textViewResourceId, objects);
            this.m_listItem = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v = convertView;
            if( null == v)
            {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.qna_list, null);
            }

            TextView qna_lesson_name = (TextView)v.findViewById(R.id.qnalist_lessonname);
            TextView qna_student_name = (TextView)v.findViewById(R.id.qnalist_studentname);
            TextView qna_ppt_number = (TextView)v.findViewById(R.id.qnalist_pptnumber);

            Button qna_check_btn = (Button)v.findViewById(R.id.qnalist_check_qna);
            Button qna_remove_btn = (Button)v.findViewById(R.id.qnalist_remove_btn);

            Map dataItem = m_listItem.get(position);
            qna_lesson_name.setText(dataItem.get("className").toString());
            qna_student_name.setText(dataItem.get("userName").toString());

            try{
            if (qnaList.get(position).get("pptNo") == null) {
                qna_ppt_number.setText("ppt");
            } else {
                qna_ppt_number.setText(dataItem.get("pptNo").toString());
            }
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            qna_check_btn.setTag(position);
            qna_check_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //쪽지 아래에 보이게 하기

                    int nPosition = (int) v.getTag();

                    //될지 안될지 모르겟???
                    qna_lesson.setText(listReturn.get(nPosition).get("className").toString());

                    try{
                        if (qnaList.get(nPosition).get("pptNo") == null) {
                            qna_ppt.setText("ppt");
                        } else {
                            qna_ppt.setText(listReturn.get(nPosition).get("pptNo").toString());
                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }

                    qna_name.setText(listReturn.get(nPosition).get("userName").toString());
                    qna_time.setText(listReturn.get(nPosition).get("createdDate").toString());
                    ask_message.setText(listReturn.get(nPosition).get("qMessage").toString());
                }
            });

            qna_remove_btn.setTag(position);
            qna_remove_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout itemParent = (LinearLayout)v.getParent();
                    int nPosition = (int) v.getTag();
                    MessageTeacher.this.RemoveData(nPosition);
                }
            });
            return v;
        }
    }//end class CusromAdapter

    public void RemoveData(int nPosition)
    {

        //qnaNumber = Integer.valueOf(qnaList.get(nPosition).get("qnaQNo").toString());

        qnaLesson = listReturn.get(nPosition).get("className").toString();
        qnaTime = listReturn.get(nPosition).get("createdDate").toString();

        for (int i = 0; i < qnaList.size(); i++) {
            if (qnaLesson == qnaList.get(i).get("className").toString()) {
                    if (qnaTime == qnaList.get(i).get("createdDate").toString()) {
                        qnaNumber = (Double) qnaList.get(i).get("qnaQNo");
                }
            }
        }

        this.adapter.remove(this.listReturn.get(nPosition));

        RemoveTask rTask = new RemoveTask();
        rTask.execute();
        //어댑터 초기화
        this.adapter.notifyDataSetChanged();
    }


    //질문 목록 가져오기
    private class qnaTask extends AsyncTask<String, Void, String> {
        ArrayList<Map> arrayList = new ArrayList<Map>();

        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/qna/list-q");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json","UTF-8" );

                JSONObject params1 = new JSONObject();
                params1.put("userId", id);
                Log.d("QNA Data-->", params1.toString());

                request.send( params1.toString() );

                int responseCode = request.code();
                if (HttpURLConnection.HTTP_OK != responseCode) {
                    Log.e("HTTP fail-->", "Http Response Fail:" + responseCode );
                    return  "오류";
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

        public ArrayList<String> addAdapter(ArrayList<String> arrayList)
        {
            return null;
        }

        public void addList(final ArrayList<Map> arrList){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            for(int i=0; i<arrList.size();i++)
                            {
                                Log.d("addList", "addList--------------------->" + arrList.get(i) + "arrList.size()        " + arrList.size());
                                qnaList.add(arrList.get(i));
                                adapter.add(arrList.get(i));
                            }
                        }
                    });
                }
            }).start();
        }

        private class JSONResultString extends JSONResult<ArrayList<Map>> {
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(MessageTeacher.this, result, Toast.LENGTH_LONG).show();
        }
    }

    private class RemoveTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/qna/delete-q");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                JSONObject params1 = new JSONObject();
                params1.put("qnaQNo", (long)qnaNumber);

                Log.d("GCM Data-->", params1.toString());

                request.send(params1.toString());

                int responseCode = request.code();
                if (HttpURLConnection.HTTP_OK != responseCode) {
                    Log.e("HTTP fail-->", "Http Response Fail:" + responseCode);
                    return "오류";
                } else {
                    Log.e("HTTPRequest-->", "정상");
                }

                Reader reader = request.bufferedReader();
                JSONResultString result = GSON.fromJson(reader, JSONResultString.class);
                reader.close();

                status = result.getResult();
                Log.d("---> ResponseResult-->", result.getResult());  // "success"? or "fail"?
                return result.getResult();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }
        private class JSONResultString extends JSONResult<String> {
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(MessageTeacher.this, result, Toast.LENGTH_LONG).show();
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
}