package com.example.myapplication.service;


import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.myapplication.models.Template;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.Str;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

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

    private Mat mScreenImage;
    private ArrayList<Template> mTemplateList;

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

        // 通知の作成と登録
        Notification notification = new Notification.Builder(this, channelId)
                .setContentTitle("実行中...")
                .setContentText("タップすると自動操作が中断されます。")
                .setSmallIcon(R.drawable.ic_baseline_play_circle_outline_24)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                .build();
        startForeground(1, notification);

        // スクリーンショットの準備
        int resultCode = intent.getIntExtra("resultCode", 0);
        Intent resultData = intent.getParcelableExtra("resultData");
        mMediaProjectionManager = (MediaProjectionManager)this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, resultData);
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture", mWidth, mHeight, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mImageReader.getSurface(), null, null);

        // OpenCVの準備
        if(OpenCVLoader.initDebug()){
            Log.d(TAG,"OpenCv configured successfully");
        }else{
            Log.d(TAG,"OpenCv doesn't configured successfully");
        }

        // テンプレート画像の読み込み
        mTemplateList = new ArrayList<>();
        String workspaceName = intent.getStringExtra("workspaceName");
        String[] files = this.fileList();
        for (int i = 0; i < files.length; i++) {
            if (files[i].contains(workspaceName) && files[i].contains(".png")) {
                Log.d(TAG, "onStartCommand: "+files[i]);

                try {
                    // 画像読み込み
                    File file = new File(getFilesDir().getPath() + "/" + files[i]);
                    FileInputStream inputStream = new FileInputStream(file);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    // Mat変換
                    Mat matImage = new Mat();
                    Utils.bitmapToMat(bitmap, matImage);

                    // グレースケール変換
                    Imgproc.cvtColor(matImage, matImage, Imgproc.COLOR_RGB2GRAY);

                    // テンプレートリスト追加
                    Template template = new Template();
                    String name = files[i].replace(".png","");
                    name = name.replace(workspaceName+"_","");
                    template.Name = name;
                    template.Image = matImage;
                    mTemplateList.add(template);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }

        // Luaスクリプトの開始
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

                // メインアクティビティが終了するまで少し待つ
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // スクリーンショット（初回は捨てる）
                //ScreenCapture();

                // Luaスクリプト実行
                try {
                    chunk.call();
                } catch (RuntimeException e) { }

                // バックグラウンドサービス終了
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
        Log.d(TAG, "ScreenCapture1: ");

        Bitmap screenshot = getScreenshot();
        // 画面に変更がない場合、NULL
        if (screenshot == null) {
            return;
        }

        Mat matImage = new Mat();
        Utils.bitmapToMat(screenshot, matImage);

        // グレースケール変換
        Imgproc.cvtColor(matImage, matImage, Imgproc.COLOR_RGB2GRAY);

        /*
        File file = new File(getFilesDir().getPath() + "/test4.png");
        Imgcodecs.imwrite(file.toString(), matImage);

        try {
            FileOutputStream outStream = new FileOutputStream(file);
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        Log.d(TAG, "ScreenCapture2: ");
        mScreenImage = matImage;
    }

    @Override
    public Point MatchTemplate(String templateName) {
        Log.d(TAG, "MatchTemplate1: " + templateName);

        Template template = null;
        for (int i = 0; i < mTemplateList.size(); i++) {
            Template t = mTemplateList.get(i);
            if (t.Name.equals(templateName)) {
                template = t;
                break;
            }
        }

        if (template == null) {
            Log.e(TAG, "テンプレート画像が読み込めませんでした。");
            return null;
        }

        Point matchLoc = matchTemplate(mScreenImage, template.Image, 0.8);
        return matchLoc;
    }

    private void savePng(Mat mat, String name) {
        String FileName = name + "_" + System.currentTimeMillis() + ".png";
        Log.d(TAG, "savePng: " + FileName);
        File file = new File(getFilesDir().getPath() + "/" + FileName);
        Imgcodecs.imwrite(file.toString(), mat);
    }

    private Bitmap getScreenshot() {
        Log.d(TAG, "getScreenshot1: ");


        Image image = mImageReader.acquireLatestImage();
        // 画面に変更がない場合、NULL
        if (image == null) {
            return null;
        }

        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();

        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * mWidth;

        // バッファからBitmapを生成
        Bitmap bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();

        Log.d(TAG, "getScreenshot2: ");
        return bitmap;
    }

    public Point matchTemplate(Mat inImage, Mat templateImage, double threshold) {
        Log.d(TAG, "Running Template Matching");

        //Mat img = Imgcodecs.imread(inFile);
        //Mat temple = Imgcodecs.imread(templateFile);

        // Create the result matrix
        int result_cols = inImage.cols() - templateImage.cols() + 1;
        int result_rows = inImage.rows() - templateImage.rows() + 1;
        //Mat result = new Mat(result_rows, result_cols, CvType.CV_8UC1);
        Mat result = new Mat();

        // Do the Matching and Normalize
        Imgproc.matchTemplate(inImage, templateImage, result, Imgproc.TM_CCOEFF_NORMED);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        // Localizing the best match with minMaxLoc
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        // ルイギド確認
        if (threshold > mmr.maxVal) {
            return null;
        }

        Point matchLoc = mmr.maxLoc;

        Mat outImage = new Mat();
        inImage.copyTo(outImage);

        // Show me what you got
        Imgproc.rectangle(outImage, matchLoc, new Point(matchLoc.x + templateImage.cols(),matchLoc.y + templateImage.rows()), new Scalar(0, 255, 0));

        // Save the visualized detection.
        //savePng(outImage, "test");

        // 一致した中央ポイントを返却
        Point centerLoc = new Point(matchLoc.x + (templateImage.cols()/2), matchLoc.y + (templateImage.rows())); // なぜかYがずれる
        Log.d(TAG, "matchTemplate1: " + matchLoc.x + ", " + matchLoc.y);
        Log.d(TAG, "matchTemplate2: " + templateImage.cols() + ", " + templateImage.rows());
        Log.d(TAG, "matchTemplate3: " + centerLoc.x + ", " + centerLoc.y);
        return centerLoc;
    }

}
