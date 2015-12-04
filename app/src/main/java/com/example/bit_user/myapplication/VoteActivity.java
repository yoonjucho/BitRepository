package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import android.os.PowerManager;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import java.util.Random;

import static com.github.kevinsawicki.http.HttpRequest.post;

public class VoteActivity extends Activity {
    public static final String TAG = "VoteActivity";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    ArrayList<String> idList = new ArrayList<String>();
    private CusromAdapter adapter;

    private ArrayList<VoteList> listReturn = new ArrayList<VoteList>();

    Button plusButton;
    Button voteButton;
    String id;
    EditText title;
    EditText plusVote;
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

        sender = new Sender(GCMInfo.GOOGLE_API_KEY);
        this.title = (EditText) findViewById(R.id.vote_title);
        this.plusVote = (EditText) findViewById(R.id.plus_vote);
        this.voteView = (ListView)findViewById(R.id.vote_list);
        plusButton = (Button) findViewById(R.id.vote_plus_btn);
        voteButton = (Button) findViewById(R.id.vote_btn);

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

        registerDevice();

        adapter = new CusromAdapter(this, 0, listReturn );
        this.voteView.setAdapter(adapter);

        //this.listReturn.add(new VoteList("d","d"));
        this.adapter.notifyDataSetChanged();

        plusButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String plus = plusVote.getText().toString();
                //투표 리스트에 추가하기
            }
        });

        voteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //디비에 추가하기
                //sendToDevice(data);
            }
        });
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
                //v = vi.inflate(R.layout.listview_item_nobutton, null);
            }

            //위젯 찾기
            TextView txtData1 = (TextView)v.findViewById(R.id.txtData1);
            Button btnEdit = (Button)v.findViewById(R.id.btnEdit);

            //위젯에 데이터를 넣는다.
            VoteList dataItem = m_listItem.get(position);
            //txtData1.setText(dataItem.voteList.get(0));

            //포지션 입력
            btnEdit.setTag(position);
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout itemParent = (LinearLayout)v.getParent();
                    int nPosition = (int) v.getTag();
                    VoteActivity.this.RemoveData(nPosition);
                }
            });

            return v;
        }

    }//end class CusromAdapter

    public void  onClick_btnDataAdd_Custom(View v)
    {   //커스텀 Add

        this.adapter.add(new VoteList(this.txtData1.getText().toString()));
        this.adapter.notifyDataSetChanged();
    }

    public void RemoveData(int nPosition)
    {
        this.adapter.remove(this.listReturn.get(nPosition));
    }

}
