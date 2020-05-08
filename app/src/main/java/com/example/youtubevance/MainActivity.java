package com.example.youtubevance;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    private WebView wbvMain;
    private String url = "https://m.youtube.com/";
    private Intent intent ;
    private CustomWebChromeClient chromeWebClient;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = getIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askPermission();
        }else{
            if(intent!=null){
                if (intent.getBooleanExtra("expand",true)) {
                    Intent intent = new Intent(MainActivity.this, FloatingViewService.class);
                    intent.putExtra("url", url);
                    startForegroundService(intent);
                    startService(intent);
                    finish();
                }
            }
        }

        initView();
        initData();
        initListener();

        if (savedInstanceState == null) {
            wbvMain.restoreState(savedInstanceState);
        }
    }

    void initView(){
        wbvMain = findViewById(R.id.webView);
        chromeWebClient = new CustomWebChromeClient();
        wbvMain.setWebChromeClient(chromeWebClient);
        // Navigate everywhere you want, this classes have only been tested on YouTube's mobile site
        wbvMain.loadUrl("http://m.youtube.com");
    }

    void initData(){
        if(intent!=null){
            if(intent.getStringExtra("url")!=null){
                url = intent.getStringExtra("url");
                Log.e("url", url);
            }
        }

        WebSettings webSettings = wbvMain.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        // 設置緩存模式
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 啟用緩存
        webSettings.setAppCacheEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);

        wbvMain.loadUrl(url);
        Toast.makeText(MainActivity.this, "按下返回鍵後，可縮小應用並在背景播放影片", Toast.LENGTH_SHORT).show();
    }

    void initListener(){

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//捕捉返回鍵
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            //if(wbvMain.canGoBack()){
            //    wbvMain.goBack();
            //}else{
                Intent intent = new Intent(MainActivity.this, FloatingViewService.class);
                intent.putExtra("url", wbvMain.getUrl());
                startForegroundService(intent);
                startService(intent);
                finish();
            //}
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION) {
                Intent intent = new Intent(MainActivity.this, FloatingViewService.class);
                intent.putExtra("url", wbvMain.getUrl());
                startForegroundService(intent);
                startService(intent);
                finish();
            }
        }
    }

    public class CustomWebChromeClient extends WebChromeClient {
        Dialog dialog ;
        @Override
        public void onShowCustomView(View view, final CustomViewCallback callback) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            dialog = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            dialog.setContentView(view);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    callback.onCustomViewHidden();
                    chromeWebClient.onHideCustomView();
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
                }
            });
            dialog.show();
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            dialog.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        wbvMain.saveState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        wbvMain.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        wbvMain.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        wbvMain.stopLoading();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wbvMain.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        }
        else {

        }
    }
}
