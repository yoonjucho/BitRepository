package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.os.PowerManager;
import android.text.format.Time;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

public class NoticeStudentActivity extends Activity {
    public static final String TAG = "NoticeStudentActivity";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    public ArrayList<Map> arrayList = new ArrayList<Map>();
    private CusromAdapter adapter;
    private ArrayList<Map> listReturn = new ArrayList<Map>();

    ListView checkList;
    TextView noticeLesson;
    TextView noticeTitle;
    TextView noticeTime;
    TextView noticeMessage;

    public String MESSAGE;
    String id;
    Sender sender;

    String status;
    Handler handler = new Handler();
    private Random random ;
    private int TTLTime = 60;
    private	int RETRY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_notice);

        this.sender = new Sender(GCMInfo.GOOGLE_API_KEY);
        this.checkList = (ListView)findViewById(R.id.notice_list_student);
        this.noticeTitle = (TextView)findViewById(R.id.notice_Title);
        this.noticeTime = (TextView)findViewById(R.id.notice_time);
        this.noticeMessage = (TextView)findViewById(R.id.notice_Message);
        this.noticeLesson = (TextView)findViewById(R.id.notice_lesson);

        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("ID_DATA");
        if(bundleData == null){
            Toast.makeText(this, "Bundle data is null!",Toast.LENGTH_LONG).show();
            return;
        }

        id = bundleData.getString("ID");
        Toast.makeText(this, "ID is "+id ,Toast.LENGTH_LONG).show();

        adapter = new CusromAdapter(this, 0, listReturn );
        this.checkList.setAdapter(adapter);
        checkList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        this.adapter.notifyDataSetChanged();

        LessonListTask lTask = new LessonListTask();
        lTask.execute();
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
                v = vi.inflate(R.layout.notice_list, null);
            }

            TextView noticeLesson = (TextView)v.findViewById(R.id.noticelist_lessonname);
            TextView noticeTitle = (TextView)v.findViewById(R.id.noticelist_title);
            TextView noticeTime = (TextView)v.findViewById(R.id.noticelist_time);

            Button click_vote_teacher = (Button)v.findViewById(R.id.noticelist_check_notice);

            Map dataItem = m_listItem.get(position);
            noticeLesson.setText(dataItem.get("className").toString());
            noticeTitle.setText(dataItem.get("title").toString());
            noticeTime.setText(dataItem.get("createdDate").toString());

            click_vote_teacher.setTag(position);
            click_vote_teacher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout itemParent = (LinearLayout)v.getParent();
                    int nPosition = (int) v.getTag();
                    NoticeStudentActivity.this.getViewData(nPosition);
                }
            });
            return v;
        }
    }//end class CusromAdapter

    public void getViewData(int nPosition)
    {

        String name = listReturn.get(nPosition).get("className").toString();
        String title = listReturn.get(nPosition).get("title").toString();
        String time = listReturn.get(nPosition).get("createdDate").toString();
        String message = listReturn.get(nPosition).get("message").toString();

        noticeLesson.setText(name);
        noticeTitle.setText(title);
        noticeTime.setText(time);
        noticeMessage.setText(message);

        this.adapter.notifyDataSetChanged();
    }

    private class LessonListTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/notice/list");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                JSONObject params1 = new JSONObject();
                params1.put("userId", id);
                Log.d("LessonList Data-->", params1.toString());

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

                arrayList = result.getData();
                addList(arrayList);

                status = result.getResult();
                Log.d("---> ResponseResult-->", result.getResult());  // "success"? or "fail"?
                return result.getResult();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }

        public ArrayList<Map> addAdapter(ArrayList<Map> arrayList) {
            return null;
        }

        public void addList(final ArrayList<Map> arrList) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < arrList.size(); i++) {
                                Log.d("addList", "addList--------------------->" + arrList.get(i) + "arrList.size()        " + arrList.size());
                                adapter.add(arrList.get(i));
                            }
                        }
                    });
                }
            }).start();
        }

        private class JSONResultString extends JSONResult<ArrayList<Map>> {
        }
    }

}
