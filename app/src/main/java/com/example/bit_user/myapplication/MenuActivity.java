package com.example.bit_user.myapplication;



import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;



public class MenuActivity extends Activity implements  View.OnClickListener {


    String id;
    String position;
    Bundle bundleData;
    public static final String KEY_SIMPLE_DATA = "data";
    ImageButton menu01;
    ImageButton menu02;
    ImageButton menu03;
    ImageButton menu04;
    ImageButton menu05;
    ImageButton menu06;
    TextView userName;
    TextView userType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_menu);

        //Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("ID_DATA");

        if(bundleData == null){
            Toast.makeText(this, "Bundle data is null!",Toast.LENGTH_LONG).show();
            return;
        }
   /*     id = bundleData.getString("ID");*/

        SharedPreferences preferences = getSharedPreferences("Setting",0);
        id = preferences.getString("id","");
        position =preferences.getString("postion","");


        //Toast.makeText(this, "ID is "+id ,Toast.LENGTH_LONG).show();

        menu01 =(ImageButton)findViewById(R.id.menu01);
        menu02 = (ImageButton)findViewById(R.id.menu02);
        menu03=(ImageButton)findViewById(R.id.menu03);
        menu04=(ImageButton)findViewById(R.id.menu04);
        menu05=(ImageButton)findViewById(R.id.menu05);
        menu06=(ImageButton)findViewById(R.id.menu06);
        userName = (TextView)findViewById(R.id.userName);
        userType = (TextView)findViewById(R.id.userType);

        userName.setText(id+"님  ");
        userType.setText(position);

        menu01.setOnClickListener(this);
        menu02.setOnClickListener(this);
        menu03.setOnClickListener(this);
        menu04.setOnClickListener(this);
        menu05.setOnClickListener(this);
        menu06.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.menu01: //강의실
                if(position.equals("teacher")) {
                    Intent intent11 = new Intent(this, BoardActivity.class);
                    bundleData = new Bundle();
                    bundleData.putString("ID",id);
                    intent11.putExtra("ID_DATA", bundleData);
                    startActivity(intent11);
                }
                else if (position.equals("student")){
                    Intent intent12 = new Intent(this, BoardActivity.class);
                    bundleData = new Bundle();
                    bundleData.putString("ID",id);
                    intent12.putExtra("ID_DATA", bundleData);
                    startActivity(intent12);
                }
                break;
            case R.id.menu02: //출석체크
                if(position.equals("teacher")) {
                    Intent intent21 = new Intent(this,checkUpActivity.class);
                    bundleData = new Bundle();
                    bundleData.putString("ID",id);
                    bundleData.putString("position",position);
                    intent21.putExtra("ID_DATA", bundleData);
                    startActivity(intent21);
                }
                else if (position.equals("student")) {
                    Intent intent22 = new Intent(this,checkActivity.class);
                    bundleData = new Bundle();
                    bundleData.putString("ID",id);
                    bundleData.putString("position",position);
                    intent22.putExtra("ID_DATA", bundleData);

                    startActivity(intent22);
                }
                break;
            case R.id.menu03: //투표하기
                if(position.equals("teacher")) {
                    Intent intent31 = new Intent(this,VoteListTeacher.class);
                    bundleData = new Bundle();
                    bundleData.putString("ID",id);
                    intent31.putExtra("ID_DATA", bundleData);
                    startActivity(intent31);
                }
                else if (position.equals("student")) {
                    Intent intent32 = new Intent(this,VoteListStudent.class);
                    bundleData = new Bundle();
                    bundleData.putString("ID",id);
                    intent32.putExtra("ID_DATA", bundleData);
                    startActivity(intent32);
                }
                break;
            case R.id.menu04: //쪽지보내기
                if(position.equals("teacher")) {
                    Intent intent41 = new Intent(this,MessageTeacher.class);
                    bundleData = new Bundle();
                    bundleData.putString("ID",id);
                    intent41.putExtra("ID_DATA", bundleData);
                    startActivity(intent41);
                }
                else if (position.equals("student")) {
                    Intent intent41 = new Intent(this,QnAActivity.class);
                    bundleData = new Bundle();
                    bundleData.putString("ID",id);
                    intent41.putExtra("ID_DATA", bundleData);
                    startActivity(intent41);
                }
                break;
            case R.id.menu05: //공지사항
                if(position.equals("teacher")) {
                    Intent intent51 = new Intent(this,NoticeTeacherActivity.class);
                    bundleData = new Bundle();
                    bundleData.putString("ID",id);
                    intent51.putExtra("ID_DATA", bundleData);
                    startActivity(intent51);
                }
                else if (position.equals("student")) {
                    Intent intent52 = new Intent(this,NoticeStudentActivity.class);
                    bundleData = new Bundle();
                    bundleData.putString("ID",id);
                    intent52.putExtra("ID_DATA", bundleData);
                    startActivity(intent52);
                }

                break;
            case R.id.menu06: //마이페이지
                Intent intent61 = new Intent(this,MypageActivity.class);
                bundleData = new Bundle();
                bundleData.putString("ID",id);
                intent61.putExtra("ID_DATA", bundleData);
                startActivity(intent61);

                /*
                if(position.equals("teacher")) {

                }
                else if (position.equals("student")) {

                }
                */
                break;
            default:
                break;
        }
    }
}
