package com.example.bit_user.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bit-user on 2015-11-23.
 */
public class BoardActivity extends Activity {

    public static final String TAG = "BoardActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_board);

        //페이지 스크롤 만들기~
   /*     page = (RelativeLayout)findViewById(R.id.page);
        page.setMovementMethod(new ScrollingMovementMethod());
*/

        EditText content;
        String strDate;
        TextView Datepick;
        Date date = new Date();
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy.MM.dd HH:mm",java.util.Locale.getDefault());
        strDate = dateformat.format(date);
        content = (EditText) findViewById(R.id.board_content);
        Datepick = (TextView) findViewById(R.id.board_date);
        Datepick.setText(strDate);

        String contents = content.getText().toString();
        contents=contents.replace("'","''");
    }
}
