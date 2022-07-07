package com.example.myapplication.service;


import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.drawable.Icon;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;

import com.example.myapplication.Utility;
import com.example.myapplication.lua.CustomDebugLib;
import com.example.myapplication.lua.MyLua2Java;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class BackgroundService extends Service implements MyLua2Java.LuaListener {
    private static final String TAG = "BackgroundService";

    protected @Nullable Context mContext = null;
    private Handler mHandler = null;
    private Thread mLuaThread;
    private CustomDebugLib mCustomDebugLib;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private ImageReader mImageReader;

    private int mWidth;
    private int mHeight;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand start");

        mContext = this;
        mHandler = new Handler(Looper.getMainLooper());

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mWidth = metrics.widthPixels;
        mHeight = metrics.heightPixels;
        int density = metrics.densityDpi;

        String channelId = "blockly";
        String channelName = "service channel";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle("実行中...")
                .setContentText("タップすると自動操作が中断されます。")
                .setSmallIcon(R.drawable.ic_baseline_play_circle_outline_24)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                .build();
        startForeground(1, notification);

        int resultCode = intent.getIntExtra("resultCode", 0);
        Intent resultData = intent.getParcelableExtra("resultData");
        mMediaProjectionManager = (MediaProjectionManager)this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, resultData);


        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture", mWidth, mHeight, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);

        mLuaThread = new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "Thread start");

                String luaCode = intent.getStringExtra("luaCode");
                luaCode = Utility.trim(luaCode, '"');
                luaCode = luaCode.replace("\\n", " ");
                luaCode = luaCode.replace("end", "end ");
                Log.d(TAG,"luaCode " + luaCode);

                Globals globals = JsePlatform.standardGlobals();
                MyLua2Java lua2Java = new MyLua2Java(mContext);
                globals.load(lua2Java);
                mCustomDebugLib = new CustomDebugLib();
                globals.load(mCustomDebugLib);
                LuaValue chunk = globals.load(luaCode);

                try {
                    chunk.call();
                } catch (RuntimeException e) { }

                stopSelf();
                Log.d(TAG, "Thread end");
            }
        };
        mLuaThread.start();

        Log.d(TAG, "onStartCommand end");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy start");
        mCustomDebugLib.interrupted = true;
        mLuaThread.interrupt();
        Log.d(TAG,"onDestroy end");
    }


    @Override
    public void ScreenCapture() {
        Bitmap screenshot = getScreenshot();
        try {
            File file = new File(getFilesDir().getPath() + "/test.png");
            FileOutputStream outStream = new FileOutputStream(file);
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getScreenshot() {
        Image image = mImageReader.acquireLatestImage();
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * mWidth;

        // バッファからBitmapを生成
        Bitmap bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();

        return bitmap;
    }
}
