package com.example.youtubevance;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class FloatingViewService extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View mFloatingView;
    private View collapsedView;
    private View expandedView;
    private WebView wbvYoutube;

    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            startForeground(1,new Notification());
        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            startForeground();
        }

        //getting the widget layout from xml using layout inflater
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        wbvYoutube = mFloatingView.findViewById(R.id.wbv_youtube);
        WebSettings webSettings = wbvYoutube.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        // 設置緩存模式
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        // 啟用緩存
        webSettings.setAppCacheEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        wbvYoutube.setWebViewClient(new WebViewClient() {
            // autoplay when finished loading via javascript injection
            public void onPageFinished(WebView view, String url) {
                wbvYoutube.loadUrl("javascript:(function() { document.getElementsByTagName('video')[0].play(); })()");
                wbvYoutube.loadUrl("javascript:(function() { document.getElementsByTagName('video')[0].muted=false; })()");
            }
        });
        wbvYoutube.setWebChromeClient(new WebChromeClient());

        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        //getting the collapsed and expanded view from the floating view
        collapsedView = mFloatingView.findViewById(R.id.layoutCollapsed);
        expandedView = mFloatingView.findViewById(R.id.layoutExpanded);

        //adding click listener to close button and expanded view
        mFloatingView.findViewById(R.id.imb_cancel).setOnClickListener(this);
        mFloatingView.findViewById(R.id.imb_cancel2).setOnClickListener(this);
        mFloatingView.findViewById(R.id.btn_expanded).setOnClickListener(this);

        expandedView.setOnClickListener(this);

        //adding an touchlistener to make drag movement of the floating widget
        mFloatingView.findViewById(R.id.relativeLayoutParent).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        //when the drag is ended switching the state of the widget
                        if(event.getRawX()==initialTouchX && event.getRawY()==initialTouchY){
                            collapsedView.setVisibility(View.GONE);
                            expandedView.setVisibility(View.VISIBLE);
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @TargetApi(26)
    private void startForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.mds.youtuber";
        String channelName = FloatingViewService.class.getSimpleName();
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("MDS TEST XDDDDDDDD")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, startId, startId);
        Bundle extras = intent.getExtras();
        String url = extras.getString("url");
        wbvYoutube.loadUrl(url);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layoutExpanded:
                //switching views
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
                break;

            case R.id.imb_cancel:
                stopSelf();
                break;

            case R.id.btn_expanded:
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(getApplicationContext(), MainActivity.class);
                intent.putExtra("url", wbvYoutube.getUrl());
                intent.putExtra("expand", false);

                startActivity(intent);
                stopSelf();
                break;

            case R.id.imb_cancel2:
                collapsedView.setVisibility(View.VISIBLE);
                expandedView.setVisibility(View.GONE);
                break;
        }
    }
}