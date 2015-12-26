package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.example.bit_user.NavigationDrawerFragment;
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


public class checkListActivity extends ActionBarActivity implements DatePicker.OnDateChangedListener,View.OnClickListener,NavigationDrawerFragment.NavigationDrawerCallbacks{

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    public static final String KEY_SIMPLE_DATA = "data";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    String id="iron";
    String type;
    private DatePicker date;
    ListView checkin_list;
    TextView dateText;
    TextView test;
    Button select_btn;
    String list;
    private ArrayList<String> arrayList;
    private ArrayList<String> arrayListNo;
    private ArrayAdapter<String> adapter;
    String checkNo;
    String select_date;
    String year1;
    String month;
    String day;
    Bundle bundleData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("ID_DATA");
        id = bundleData.getString("ID");
        type = bundleData.getString("type");


        Log.d("type",type);

        date =(DatePicker)findViewById(R.id.date);
        checkin_list=(ListView)findViewById(R.id.checkin_list);
        dateText = (TextView)findViewById(R.id.dateText);
        select_btn = (Button)findViewById(R.id.select_btn);
        test = (TextView)findViewById(R.id.test);

        date.init(2015,12,1,this);

        arrayList = new ArrayList<String>();
        arrayListNo =  new ArrayList<String>();

        View header = (View)getLayoutInflater().inflate(R.layout.th_check_list_header,null);
        checkin_list.addHeaderView(header);
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arrayList);
        checkin_list.setAdapter(adapter);


        checkin_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d("getICount",""+adapter.getCount());
                Log.d("getICount",""+adapter.getCount());

                if(adapter != null && position !=0){
                    // checkNo  =(String)adapter.getItem(position-1);
                    checkNo  =(String)arrayListNo.get(position-1);
                    Log.d("checkNo여기는 checkList",checkNo);
                    Intent intent = new Intent(getBaseContext(),CheckListDataActivity.class);
                    Bundle bundleData = new Bundle();
                    bundleData.putString("checkNo",checkNo);
                    intent.putExtra("checkNo_date",bundleData);
                    startActivity(intent);
                    finish();

                }
            }
        });

        select_btn.setOnClickListener(this);

    }
    //date
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

        switch (v.getId())
        {
            case R.id.select_btn:
                Checklist checklist = new Checklist();
                checklist.execute();
                break;
        }

    }

    public class Checklist extends SafeAsyncTask<ArrayList<HashMap>>{

        @Override
        public ArrayList<HashMap> call() throws Exception {
            JSONResultString result = null;
           /* ArrayList<HashMap> arrayList1 = new ArrayList<HashMap>();*/
            try {

                HttpRequest request = post("http://192.168.1.13::8088/bitin/api/attd/classattd-by-date-and-user");

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
               /* arrayList1 = result.getData();
                Log.d("ar",arrayList1.toString());*/

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


            for(int i=0; i<hashMaps.size(); i++ )
            {

                arrayList.add("       "
                                +hashMaps.get(i).get("CLASS_NAME").toString()+"     "
                                +hashMaps.get(i).get("START_TIME").toString()+"TIME       "
                                +hashMaps.get(i).get("TOTAL_USER").toString()+"/"
                                +hashMaps.get(i).get("ATTD_TOTAL_USER").toString()+"      "
                );
                arrayListNo.add(hashMaps.get(i).get("ATTD_NO").toString());
            }

            adapter.notifyDataSetChanged();
        }

        private class JSONResultString extends JSONResult<ArrayList<HashMap>> {

        }
    }
    @Override
    public void  onNavigationDrawerItemSelected(int position1) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (position1) {
            case 0:
                Log.d("position",Integer.toString(position1));
                //  Log.d("position",position);
/*              Intent intent1 = new Intent(this,MenuActivity.class);
                bundleData = new Bundle();
                bundleData.putString("ID",id);
                bundleData.putString("position",position);
                intent1.putExtra("ID_DATA", bundleData);
                startActivity(intent1);*/
                break;
            case 1:
                //Settings
                Log.d("position",Integer.toString(position1));
                Intent intent2 = new Intent(this, checkUpActivity.class);
                bundleData = new Bundle();
                bundleData.putString("ID",id);
                intent2.putExtra("ID_DATA", bundleData);
                startActivity(intent2);
                break;
            case 2:
                Log.d("position",Integer.toString(position1));
                Intent intent3 = new Intent(this, checkNowActivity.class);
                bundleData = new Bundle();
                bundleData.putString("ID",id);
                intent3.putExtra("ID_DATA", bundleData);
                startActivity(intent3);
                break;
            case 3:
                Log.d("position",Integer.toString(position1));
                Intent intent4 = new Intent(this, checkListActivity.class);
                bundleData = new Bundle();
                bundleData.putString("ID",id);
                intent4.putExtra("ID_DATA", bundleData);
                startActivity(intent4);
                break;

            default:
        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position1 + 1))
                .commit();

        return ;
    }

    public void onSectionAttached(int position) {
        switch (position) {
            case 1:
                mTitle = getString(R.string.title_section1);
           /*    Intent intent71 = new Intent(this,MenuActivity.class);
                startActivity(intent71);*/
                break;
            case 2:
                mTitle = getString(R.string.title_section4);
             /*  Intent intent72 = new Intent(this,checkActivity.class);
                startActivity(intent72);*/
                break;
            case 3:
                mTitle = getString(R.string.title_section5);
             /*   Intent intent73 = new Intent(this,st_CheckListActivity.class);
                startActivity(intent73);*/
                break;
            case 4:
                mTitle = getString(R.string.title_section6);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main2, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main2, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((checkListActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }



}
