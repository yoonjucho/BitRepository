package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bit_user.myapllication.core.JSONResult;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gcm.server.Sender;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.Reader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import static com.github.kevinsawicki.http.HttpRequest.post;

public class VoteListTeacher extends Activity {
    public static final String TAG = "VoteListTeacher";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    public ArrayList<Map> arrayList = new ArrayList<Map>();
    public ArrayList<Map> voteList = new ArrayList<Map>();
    private CusromAdapter adapter;
    private ArrayList<Map> listReturn = new ArrayList<Map>();
    public double removeNumber;

    ListView VotelistTeacher;
    Button makeButton;
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
        setContentView(R.layout.activity_votelist_teacher);

        this.sender = new Sender(GCMInfo.GOOGLE_API_KEY);
        this.VotelistTeacher = (ListView)findViewById(R.id.vote_list_teacher);
        this.makeButton = (Button) findViewById(R.id.make_vote_btn);

        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("ID_DATA");
        if(bundleData == null){
            Toast.makeText(this, "Bundle data is null!",Toast.LENGTH_LONG).show();
            return;
        }

        id = bundleData.getString("ID");
        Toast.makeText(this, "ID is "+id ,Toast.LENGTH_LONG).show();

        adapter = new CusromAdapter(this, 0, listReturn );
        this.VotelistTeacher.setAdapter(adapter);
        VotelistTeacher.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        this.adapter.notifyDataSetChanged();

        LessonListTask lTask = new LessonListTask();
        lTask.execute();

        makeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent gointent = new Intent(getBaseContext(),CreateVoteActivity.class);
                Bundle bundleData = new Bundle();
                bundleData.putString("ID", id);
                gointent.putExtra("ID_DATA", bundleData);
                startActivity(gointent);
                finish();
            }
        });
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
                v = vi.inflate(R.layout.vote_list_teacher, null);
            }

            TextView lesson_name_teacher = (TextView)v.findViewById(R.id.lesson_name_teacher);
            TextView vote_title_teacher = (TextView)v.findViewById(R.id.vote_title_teacher);
            TextView vote_time_teacher = (TextView)v.findViewById(R.id.vote_time_teacher);

            Button click_vote_teacher = (Button)v.findViewById(R.id.click_vote_teacher);
            Button remove_vote_teacher = (Button)v.findViewById(R.id.remove_vote_teacher);

            Map dataItem = m_listItem.get(position);
            lesson_name_teacher.setText(dataItem.get("className").toString());
            vote_title_teacher.setText(dataItem.get("voteTitle").toString());
            vote_time_teacher.setText(dataItem.get("createdDate").toString());

            click_vote_teacher.setTag(position);
            click_vote_teacher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout itemParent = (LinearLayout)v.getParent();
                    int nPosition = (int) v.getTag();

                    Intent gointent = new Intent(getBaseContext(), VoteResultActivity.class);
                    Bundle bundleData = new Bundle();
                    bundleData.putString("ID", id);
                    bundleData.putDouble("VOTENUMBER",(Double) voteList.get(nPosition).get("voteNumber"));
                    gointent.putExtra("ID_DATA", bundleData);
                    startActivity(gointent);
                    finish();
                }
            });

            remove_vote_teacher.setTag(position);
            remove_vote_teacher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout itemParent = (LinearLayout)v.getParent();
                    int nPosition = (int) v.getTag();
                    VoteListTeacher.this.RemoveData(nPosition);
                }
            });
            return v;
        }
    }//end class CusromAdapter

    public void RemoveData(int nPosition)
    {

        String name = listReturn.get(nPosition).get("className").toString();
        String title = listReturn.get(nPosition).get("voteTitle").toString();
        String time = listReturn.get(nPosition).get("createdDate").toString();

        for (int i = 0; i < voteList.size(); i++) {
            if (name == voteList.get(i).get("className").toString()) {
                if (title == voteList.get(i).get("voteTitle").toString()) {
                    if (time == voteList.get(i).get("createdDate").toString()) {
                        removeNumber =(double)  voteList.get(i).get("voteNumber");
                    }
                }
            }
        }

        this.adapter.remove(this.listReturn.get(nPosition));

        RemoveTask rTask = new RemoveTask();
        rTask.execute();
        //어댑터 초기화
        this.adapter.notifyDataSetChanged();
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
                                voteList.add(arrList.get(i));
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

    private class RemoveTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/vote/delete");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                JSONObject params1 = new JSONObject();
                params1.put("voteNumber",removeNumber);
                removeNumber = 0; // initailize
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

                status = result.getResult();
                Log.d("---> ResponseResult-->", result.getResult());  // "success"? or "fail"?
                return result.getResult();
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }

        private class JSONResultString extends JSONResult<ArrayList<Map>> {
        }
    }
}
