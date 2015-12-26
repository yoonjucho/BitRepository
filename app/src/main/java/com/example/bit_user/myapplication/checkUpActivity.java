package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bit_user.NavigationDrawerFragment;
import com.example.bit_user.myapllication.core.JSONResult;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.Reader;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.kevinsawicki.http.HttpRequest.post;


public class checkUpActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks  {
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    public static final String KEY_SIMPLE_DATA = "data";
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    private ArrayList<String> arrayList;
    private ArrayAdapter<String> adapter;
    private ArrayList<HashMap> lessonList;
    ArrayList<String> datalist;
    String status;
    String id;
    String className;
    String codenum;
    String timer;
    String classNo;
    TextView check_day;
    TextView check_time;
    EditText count_timer;
    Spinner lesson_list;
    Button check_up_btn;
    TextView select_lesson;
    String strDay;
    String strTime;
    Bundle bundleData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_up);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("ID_DATA");
        Log.e("login", "!!!!!!!sample" + bundleData.getString("ID"));

        if (bundleData == null) {
            Toast.makeText(this, "Bundle data is null!", Toast.LENGTH_LONG).show();
            return;
        }
        id = bundleData.getString("ID");
        Toast.makeText(this, "ID is " + id, Toast.LENGTH_LONG).show();
        System.out.print(id);


        //  list_sp = (Spinner)findViewById(R.id.list_sp);
        lesson_list = (Spinner) findViewById(R.id.lesson_list);
        count_timer = (EditText) findViewById(R.id.count_timer);
        check_up_btn = (Button) findViewById(R.id.check_up_Btn);
        select_lesson = (TextView) findViewById(R.id.select_lesson);
        check_day = (TextView) findViewById(R.id.check_day);
        check_time = (TextView) findViewById(R.id.check_time);

        Date date = new Date();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", java.util.Locale.getDefault());

        strTime = dateFormat.format(date);
        strDay = dateformat.format(date);


        check_day.setText(strDay);
        check_time.setText("TIME" + strTime);


        arrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, arrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setListViewData();

        lesson_list.setPrompt("강의");
        lesson_list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                className = (String) adapter.getItem(position);
                Toast.makeText(getBaseContext(), className, Toast.LENGTH_SHORT).show();
                select_lesson.setText("" + className);
                //  refresh  refresh

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        check_up_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                CheckUpTask checkUpTask = new CheckUpTask();
                checkUpTask.execute();
                timer = count_timer.getText().toString();
            }
        });

    }
    public void setListViewData(){
        LessonListTask lessonListTask = new LessonListTask();
        lessonListTask.execute();
    }
    public void addList(final ArrayList<HashMap> arrList){
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        // 해당 작업을 처리함
                        for (int i = 0; i < arrList.size(); i++)

                        {
                            Log.d("addList", "addList--------------------->" + arrList.get(i) + "arrList.size()        " + arrList.size());
                            // lessonList.add(arrList.get(i));
                            adapter.add(arrList.get(i).get("CLASS_NAME").toString());
                        }
                        lesson_list.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();



    }
    //   public void
    public ArrayList<String> addAdapter(ArrayList<String> arrayList)
    {
        return null;
    }

    //출석(강의)리스트 구현
    private class LessonListTask extends AsyncTask<String, Void, ArrayList<HashMap>> {


        protected ArrayList<HashMap> doInBackground(String... params) {
            JSONResultString result;
            ArrayList<HashMap> arrayList1 = new ArrayList<HashMap>();
            try {

                HttpRequest request = post("http://192.168.1.13:8088/bitin/api/class/class-name-and-no");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");

                // 데이터 세팅
                JSONObject params1 = new JSONObject();
                params1.put("userId",id.toString());

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
                /*for( int i=0 ; i< result.getData().size() ; i++) {
                    arrayList.add(result.getData().toString());
                }*/

                addList(arrayList1);

                return result.getData();

            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }

        private class JSONResultString extends JSONResult<ArrayList<HashMap>> {

        }

    }
    //출석체크 업로드
    private class CheckUpTask extends AsyncTask<String, Void,String> {


        protected String doInBackground(String... params) {
            JSONResultString result;
            //JSONResultString1 result1;
            try {

                HttpRequest request = post("http://192.168.1.13:8088/bitin/api/class/start-class");

                // reiquest 설정
                request.connectTimeout(2000).readTimeout(2000);
                // JSON  포맷으로 보내기  => POST 방식
                request.acceptCharset("UTF-8");
                request.acceptJson();
                request.accept(HttpRequest.CONTENT_TYPE_JSON);
                request.contentType("application/json", "UTF-8");


                // 데이터 세팅
                JSONObject params1 = new JSONObject();     params1.put("userId", id.toString());
                params1.put("className",className.toString());

                Log.d("JoinData-->", params1.toString());

                // 요청
                request.send(params1.toString());

                int responseCode = request.code();
                if (HttpURLConnection.HTTP_OK != responseCode) {
                    Log.e("HTTP fail-->", "Http Response Fail:" + responseCode);
                    return null;
                } else {
                    Log.e("HTTPRequest-->", "정상");

                }

                //4. JSON 파싱
                Reader reader = request.bufferedReader();

                result = GSON.fromJson(reader, JSONResultString.class);
                //result1 = GSON.fromJson(reader,JSONResultString1.class);
                reader.close();

                //5. 사용하기
                Log.d("---> ResponseResult-->", result.getResult());  // "success"? or "fail"?
                Log.d("--->ResponseResult-->",result.getData());
                Log.d("--->ResponseResult",result.getData2());

                codenum = result.getData();
                classNo= result.getData2();
                Log.d("Code",codenum);
                Log.d("Code",classNo);
                status  = result.getResult();
                Log.d("결과",status);

                if(status !=null && status.equals("success")) {
                    datalist = new ArrayList<String>();
                    datalist.add(id);
                    datalist.add(timer);
                    datalist.add(codenum);
                    datalist.add(className);
                    datalist.add(classNo);



                    Log.d("정보","ID:"+ id +",타이머:"+ timer +",인증번호:"+ codenum +",강의명:"+className);

                    Intent intent = new Intent(getBaseContext(), checkNowActivity.class);
                    Bundle bundleData = new Bundle();
                    bundleData.putStringArrayList("DATA_LIST", datalist);
                    intent.putExtra("DATA", bundleData);
                    Log.d("데이터리스트", datalist.toString());
                    startActivity(intent);
                    finish();

                }else
                    Log.d("오류:","정확하게 입력하세요");

                return result.getData()+result.getData2();

            } catch (Exception e3) {
                e3.printStackTrace();
            }
            return null;
        }

        private class JSONResultString extends JSONResult<String> {}

    }
    private void refresh( String lesson_list ) {
        adapter.add( lesson_list ) ;
        adapter.notifyDataSetChanged() ;
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
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
            case 6:
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
            ((checkUpActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}