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

public class VoteListStudent extends Activity {
    public static final String TAG = "VoteListStudent";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    public ArrayList<Map> arrayList = new ArrayList<Map>();
    public ArrayList<String> voteList = new ArrayList<String>();
    private CusromAdapter adapter;
    private ArrayList<Map> listReturn = new ArrayList<Map>();

    public double voteNumber;

    ListView voteListStudent;
    String id;
    Sender sender;
    private ListView voteView;
    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_votelist_student);

        this.sender = new Sender(GCMInfo.GOOGLE_API_KEY);
        this.voteListStudent = (ListView)findViewById(R.id.vote_list_student);

        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("ID_DATA");
        if(bundleData == null){
            Toast.makeText(this, "Bundle data is null!",Toast.LENGTH_LONG).show();
            return;
        }

        id = bundleData.getString("ID");
        Toast.makeText(this, "ID is "+id ,Toast.LENGTH_LONG).show();

        adapter = new CusromAdapter(this, 0, listReturn );
        voteListStudent.setAdapter(adapter);
        voteListStudent.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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
                v = vi.inflate(R.layout.student_votelist_items, null);
            }

            TextView lesson_name_student = (TextView)v.findViewById(R.id.votelist_lesson_name);
            TextView vote_title_student = (TextView)v.findViewById(R.id.votelist_vote_title);
            TextView vote_time_student = (TextView)v.findViewById(R.id.votelist_vote_time);

            Button vote_student = (Button)v.findViewById(R.id.btnVote);

            Map dataItem = m_listItem.get(position);
            lesson_name_student.setText(dataItem.get("className").toString());
            vote_title_student.setText(dataItem.get("voteTitle").toString());
            vote_time_student.setText(dataItem.get("createdDate").toString());

            vote_student.setTag(position);
            vote_student.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout itemParent = (LinearLayout)v.getParent();
                    int nPosition = (int) v.getTag();
                    VoteListStudent.this.goNext(nPosition);

                    Intent gointent = new Intent(getBaseContext(),doVoteActivity.class);
                    Bundle bundleData = new Bundle();
                    bundleData.putString("ID", id);
                    bundleData.putDouble("VOTENUMBER",voteNumber);
                    gointent.putExtra("ID_DATA", bundleData);
                    startActivity(gointent);
                    finish();
                }
            });
            return v;
        }
    }//end class CusromAdapter

    public void goNext(int nPosition)
    {
        voteNumber =(double) listReturn.get(nPosition).get("voteNumber");
    }

    private class LessonListTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/vote/list");
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
