package com.example.bit_user.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by bit-user on 2015-11-23.
 */
public class BoardActivity extends Activity {

    private WebView webView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_board);


        WebView infoWebView = (WebView)findViewById(R.id.webview);

        infoWebView.setWebViewClient(new InfoWebViewClient());			// 내꺼 webview 사용 명시
       /* WebSettings settings = webView.getSettings();
        settings.setDomStorageEnabled(true);*/

        infoWebView.getSettings().setDomStorageEnabled(true);
        infoWebView.getSettings().setJavaScriptEnabled(true);			// 자바 스크립스 사용
   /*     WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
*/
        infoWebView.loadUrl("http://192.168.1.121:8088/bitin/index");					// Load URL
    }

    public class InfoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

    }
}
