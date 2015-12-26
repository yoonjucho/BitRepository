package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.example.bit_user.myapllication.core.JSONResult;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.Reader;
import java.net.HttpURLConnection;

import static com.github.kevinsawicki.http.HttpRequest.post;

/**
 * Created by bit-user on 2015-11-23.
 */

public class BoardActivity extends Activity {
    private static final Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
    public static final String KEY_SIMPLE_DATA = "data";
    private WebView webView;

    String id;
    String position;
    String password;
    String data;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_board);
        SharedPreferences preferences = getSharedPreferences("Setting",0);
        id = preferences.getString("id","");
        position =preferences.getString("postion","");
        password = preferences.getString("password","");
;

        WebView infoWebView = (WebView)findViewById(R.id.webview);

        infoWebView.setWebViewClient(new InfoWebViewClient());			// 내꺼 webview 사용 명시

        infoWebView.getSettings().setDomStorageEnabled(true);
        infoWebView.getSettings().setJavaScriptEnabled(true);			// 자바 스크립스 사용

        infoWebView.loadUrl("http://192.168.1.120:8088/bitin/index?id="+id);					// Load URL

            }

    public class InfoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

    }


}
