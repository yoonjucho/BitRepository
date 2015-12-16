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

public class doVoteActivity extends Activity {
    public static final String TAG = "doVoteActivity";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

    public ArrayList<String> voteList = new ArrayList<String>();
    public ArrayList<String> arrayList = new ArrayList<String>();
    public ArrayAdapter<String> listadapter;

    private ArrayList<VoteList> listReturn = new ArrayList<VoteList>();

    TextView voteTitle;
    TextView checkVote;
    ListView checkList;
    Button voteButton;

    String status;
    public String checkedVote;
    public String id;
    public double voteNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);

        this.voteTitle = (TextView) findViewById(R.id.vote_title);
        this.checkVote = (TextView) findViewById(R.id.check_vote);
        this.checkList = (ListView)findViewById(R.id.vote_list);
        this.voteButton = (Button) findViewById(R.id.vote_btn);

        Intent intent = getIntent();

        Bundle bundleData = intent.getBundleExtra("ID_DATA");
        if(bundleData == null){
            Toast.makeText(this, "Bundle data is null!",Toast.LENGTH_LONG).show();
            return;
        }

        id = bundleData.getString("ID");
        voteNumber = bundleData.getDouble("VOTENUMBER");

        listadapter= new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1,arrayList);
        checkList.setAdapter(listadapter);
        checkList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        VoteTask vTask = new VoteTask();
        vTask.execute();

        checkList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkedVote = (String) listadapter.getItem(position);
                Toast.makeText(getBaseContext(), checkedVote, Toast.LENGTH_SHORT).show();
                checkVote.setText("" + checkedVote);
            }
        });

        voteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //디비에 추가하기

                DBTask dTask = new DBTask();
                dTask.execute();

                Intent gointent = new Intent(getBaseContext(),VoteListStudent.class);
                Bundle bundleData = new Bundle();
                bundleData.putString("ID", id);
                gointent.putExtra("ID_DATA", bundleData);
                startActivity(gointent);
                finish();
            }
        });
    }

    private class VoteTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            try {
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/vote/votelistbyvoteno");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                JSONObject params1 = new JSONObject();
                params1.put("voteNumber",voteNumber);
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
                                voteList.add(arrList.get(i));
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
                HttpRequest request = post("http://192.168.1.32:8088/bitin/api/vote/voting");
                request.connectTimeout(2000).readTimeout(2000);

                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                JSONObject params1 = new JSONObject();
                params1.put("userId", id);
                params1.put("voteNumber",voteNumber);
                params1.put("voteContent", checkedVote);

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
}
