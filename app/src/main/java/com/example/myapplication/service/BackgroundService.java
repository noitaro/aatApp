package com.example.myapplication.service;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import com.example.myapplication.R;

import com.example.myapplication.Utility;
import com.example.myapplication.lua.MyLua2Java;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

public class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";

    protected @Nullable Context mContext = null;
    private Handler mHandler = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand start");

        mContext = getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());

        String channelId = "blockly";
        String channelName = "service channel";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);


        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new Notification.Builder(this, channelId)
                        .setContentTitle("notification_title")
                        .setContentText("notification_message")
                        .setSmallIcon(R.drawable.ic_baseline_play_circle_outline_24)
                        .setContentIntent(pendingIntent)
                        .build();
        startForeground(1, notification);


        //mHandler.post(new Runnable() {
        Thread t = new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "Thread start");

                String luaCode = intent.getStringExtra("luaCode");
                luaCode = Utility.trim(luaCode, '"');
                luaCode = luaCode.replace("\\n", "");
                Log.d("【onHandleIntent】luaCode", luaCode);

                Globals globals = JsePlatform.standardGlobals();
                globals.load(new MyLua2Java(mContext));
                LuaValue chunk = globals.load(luaCode);
                chunk.call();

                stopSelf();
                Log.d(TAG, "Thread end");
            }
        };
        t.start();

        Log.d(TAG, "onStartCommand end");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("onDestroy", "start");
        Log.d("onDestroy", "end");
    }













}
