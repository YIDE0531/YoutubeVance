package com.example.youtubevance;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;
    private WebView wbvMain;
    private String url = "https://m.youtube.com/";
    private Intent intent ;
    private CustomWebChromeClient chromeWebClient;
    private final int REQUEST_EXTERNAL_STORAGE = 102;
    private boolean isDebug = false ;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isDebug){
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[] {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_EXTERNAL_STORAGE
                );
            }else{
                Debugeth db = new Debugeth();
                db.start();
            }
        }

        intent = getIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            askPermission();
        }else{
            if(intent!=null){
                if (intent.getBooleanExtra("expand",true)) {
                    Intent intent = new Intent(MainActivity.this, FloatingViewService.class);
                    intent.putExtra("url", url);
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
                        startForegroundService(intent);
                    }
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
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
                startForegroundService(intent);
            }
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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
                    startForegroundService(intent);
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Debugeth db = new Debugeth();
                    db.start();
                } else {
                    //使用者拒絕權限，停用檔案存取功能
                }
                break;
        }
    }

    class Debugeth extends Thread { //用來Debug的程式碼將所有Logcat的資訊寫入手機文字檔中，方便debug。
        @Override
        public void run() {
            try {
                java.lang.Process p = Runtime.getRuntime().exec("logcat");
                final InputStream is = p.getInputStream();
                new Thread() {
                    @Override
                    public void run() {
                        FileOutputStream os = null;
                        try {
                            String url = Environment.getExternalStorageDirectory().getAbsolutePath() ;
                            Log.d("writelog", "read logcat process failed. message: " + url);

                            File dirFile = new File(url + "/Youtuber/");
                            if (!dirFile.exists()) {// 如果資料夾不存在
                                dirFile.mkdir();// 建立資料夾
                            }
                            os = new FileOutputStream(url+"/Youtuber/writelogt", false);
                            int len = 0;
                            byte[] buf = new byte[1024];
                            while (-1 != (len = is.read(buf))) {
                                os.write(buf, 0, len);
                                os.flush();
                            }
                        } catch (Exception e) {
                            Log.d( "/Youtuber/writelog", "read logcat process failed. message: " + e.getMessage());
                        } finally {
                            if (null != os) {
                                try {
                                    os.close(); os = null;
                                } catch (IOException e) {
                                    // Do nothing
                                }
                            }
                        }
                    }
                }.start();
            } catch (Exception e) {
                Log.d("writelog", "open logcat process failed. message: " + e.getMessage());
            }
        }
    }

}
