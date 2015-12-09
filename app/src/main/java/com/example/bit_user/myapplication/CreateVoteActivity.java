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

public class CreateVoteActivity extends Activity {
    public static final String TAG = "CreateVoteActivity";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    ArrayList<String> idList = new ArrayList<String>();

    public ArrayList<String> lessonList = new ArrayList<String>();
    public ArrayList<String> arrayList = new ArrayList<String>();
    public ArrayList<String> voteList = new ArrayList<String>();
    public ArrayAdapter<String> listadapter;

    private CusromAdapter adapter;
    private ArrayList<VoteList> listReturn = new ArrayList<VoteList>();

    String voteTitle;
    String lessonName;
    EditText check_lesson_;
    ListView lesson_list;
    Button plusButton;
    Button voteButton;
    String id;
    EditText title;
    EditText txtVoteData;
    Sender sender;
    private ListView voteView;

    String receiver[];
    String regId;
    String status;
    Handler handler = new Handler();
    private Random random ;
    private int TTLTime = 60;
    private	int RETRY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_vote);

        this.sender = new Sender(GCMInfo.GOOGLE_API_KEY);
        this.title = (EditText) findViewById(R.id.vote_title);
        this.txtVoteData = (EditText) findViewById(R.id.plus_vote);
        this.lesson_list = (ListView)findViewById(R.id.lessonlist_vote);
        this.voteView = (ListView)findViewById(R.id.vote_list);
        this.plusButton = (Button) findViewById(R.id.vote_plus_btn);
        this.voteButton = (Button) findViewById(R.id.vote_btn);
        this.check_lesson_ = (EditText) findViewById(R.id.check_lesson_);
        Intent intent = getIntent();
        if (intent != null) {
            processIntent(intent);
        }

        Bundle bundleData = intent.getBundleExtra("ID_DATA");
        if(bundleData == null){
            Toast.makeText(this, "Bundle data is null!",Toast.LENGTH_LONG).show();
            return;
        }

        id = bundleData.getString("ID");
        Toast.makeText(this, "ID is "+id ,Toast.LENGTH_LONG).show();
        System.out.print(id);

        //registerDevice();

        adapter = new CusromAdapter(this, 0, listReturn );
        this.voteView.setAdapter(adapter);
        this.adapter.notifyDataSetChanged();

        listadapter= new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1,arrayList);
        lesson_list.setAdapter(listadapter);
        lesson_list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        LessonListTask lTask = new LessonListTask();
        lTask.execute();

        lesson_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lessonName = (String) listadapter.getItem(position);
                Toast.makeText(getBaseContext(), lessonName, Toast.LENGTH_SHORT).show();

                //학생들 idlist 받아오기

                //id 등록하기
                //registerDevice();

                check_lesson_.setText("" + lessonName);
            }
        });

        plusButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String plus = txtVoteData.getText().toString();
                voteList.add(plus);
                onClick_vote_plus_btn_Custom(v);
                //투표 리스트에 추가하기
            }
        });

        voteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //디비에 추가하기
                voteTitle = title.getText().toString();
                DBTask dTask = new DBTask();
                dTask.execute();

                //sendToDevice(data);
            }
        });
    }

    public void  onClick_vote_plus_btn_Custom(View v)
    {
        String plusVote;
        plusVote = this.txtVoteData.getText().toString();
        if(plusVote.isEmpty()){
            Toast.makeText(this, "vote list can not be null",Toast.LENGTH_LONG).show();
        }
        else {
            this.adapter.add(new VoteList(plusVote));
            this.adapter.notifyDataSetChanged();
            txtVoteData.setText(null);
        }
    }

    private class CusromAdapter extends ArrayAdapter<VoteList>
    {
        //리스트에 사용할 데이터
        private ArrayList<VoteList> m_listItem;

        public CusromAdapter(Context context, int textViewResourceId, ArrayList<VoteList> objects )
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
                v = vi.inflate(R.layout.votelist_item, null);
            }

            //위젯 찾기
            TextView txtData1 = (TextView)v.findViewById(R.id.txtData1);
            Button btnEdit = (Button)v.findViewById(R.id.btnEdit);

            //위젯에 데이터를 넣는다.
            VoteList dataItem = m_listItem.get(position);
            txtData1.setText(dataItem.voteData);

            //포지션 입력
            btnEdit.setTag(position);
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout itemParent = (LinearLayout)v.getParent();
                    int nPosition = (int) v.getTag();
                    CreateVoteActivity.this.RemoveData(nPosition);
                }
            });

            return v;
        }

    }//end class CusromAdapter

    public void RemoveData(int nPosition)
    {
        this.adapter.remove(this.listReturn.get(nPosition));
    }

    private class LessonListTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/user/classname-by-teacherid");
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

        public ArrayList<String> addAdapter(ArrayList<String> arrayList) {
            return null;
        }

        public void addList(final ArrayList<String> arrList) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < arrList.size(); i++) {
                                Log.d("addList", "addList--------------------->" + arrList.get(i) + "arrList.size()        " + arrList.size());
                                lessonList.add(arrList.get(i));
                                listadapter.add(arrList.get(i));
                            }
                        }
                    });
                }
            }).start();
        }

        private class JSONResultString extends JSONResult<ArrayList<String>> {
        }
    }

    private class DBTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/vote/enroll");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                JSONObject params1 = new JSONObject();
                params1.put("userId", id);
                params1.put("className", lessonName);
                params1.put("voteTitle", voteTitle);
                params1.put("voteContent", voteList);

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

        private class JSONResultString extends JSONResult<ArrayList<String>> {
        }

    }

    private void processIntent(Intent intent) {
        /*
        String from = intent.getStringExtra("from");
        if (from == null) {
            Log.d(TAG, "*********from is null.");
            return;
        }
        String command = intent.getStringExtra("command");
        String type = intent.getStringExtra("type");
        data = intent.getStringExtra("data");
        Log.d(TAG, "from : " + from + ", command : " + command + ", type : " + type + ", data : " + data+"sender"+ regId);

        DBTask dbTask = new DBTask();
        dbTask.execute();

        dd.setText("Message from [" + from + "] : " + data);
        */
    }
}
