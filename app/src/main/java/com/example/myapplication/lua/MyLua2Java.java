package com.example.myapplication.lua;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.DataOutputStream;
import java.net.Socket;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.models.InjectEvent;
import com.example.myapplication.models.Template;
import com.google.gson.Gson;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.LibFunction;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyLua2Java extends LibFunction {
    private static final String TAG = "MyLua2Java";
    private Context _context = null;

    LuaListener listener;
    public interface LuaListener {
        void ScreenCapture();
        Point MatchTemplate(String templateName);
    }

    public MyLua2Java(Context context) {
        _context = context;
        listener = (LuaListener)context;
    }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("screenCapture", new screenCapture());
        library.set("imageTap", new imageTap());
        library.set("log", new javaLog());
        library.set("sleep", new javaSleep());
        library.set("deviceTap", new deviceTap());
        library.set("toast", new javaToast());
        library.set("deviceKey", new deviceKey());
        env.set("MyLua2Java", library);
        return library;
    }

    class screenCapture extends OneArgFunction {
        public LuaValue call(LuaValue value) {
            Log.d(TAG, "screenCapture: " + value.toString());
            listener.ScreenCapture();
            return null;
        }
    }

    class imageTap extends OneArgFunction {
        public LuaValue call(LuaValue value) {
            Log.d(TAG, "imageTap: " + value.toString());
            Point matchLoc = listener.MatchTemplate(value.toString());




            return null;
        }
    }

    class javaLog extends OneArgFunction {
        public LuaValue call(LuaValue value) {
            Log.d(TAG, "javaLog: " + value.toString());
            return null;
        }
    }

    class javaSleep extends OneArgFunction {
        public LuaValue call(LuaValue value) {
            Log.d(TAG, "javaSleep: " + value.toString());
            try {
                int millis = value.toint() * 1000;
                Thread.sleep(millis);
            } catch (InterruptedException e) { }
            return null;
        }
    }

    class deviceTap extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue luaValueX, LuaValue luaValueY) {
            Log.d(TAG, "deviceTap: " + luaValueX.toString() + ", y=" + luaValueY.toString());
            InjectEvent motionEvent = new InjectEvent();
            motionEvent.event = "motion";
            motionEvent.action = 0;
            motionEvent.x = luaValueX.tofloat();
            motionEvent.y = luaValueY.tofloat();
            Gson gson = new Gson();
            String json = gson.toJson(motionEvent);
            Send(json);

            motionEvent.action = 1;
            json = gson.toJson(motionEvent);
            Send(json);
            return null;
        }
    }

    class javaToast extends OneArgFunction {
        public LuaValue call(LuaValue value) {
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(_context , value.toString(), Toast.LENGTH_LONG).show();
                }
            });
            return null;
        }
    }

    class deviceKey extends OneArgFunction {
        public LuaValue call(LuaValue value) {
            Log.d(TAG, "deviceKey: " + value.toString());
            String url = "https://localhost:8081/KeyEvent/";
            InjectEvent keyEvent = new InjectEvent();
            keyEvent.event = "key";
            keyEvent.action = 0;
            keyEvent.code = value.toint();
            Gson gson = new Gson();
            String json = gson.toJson(keyEvent);
            Send(json);

            keyEvent.action = 1;
            json = gson.toJson(keyEvent);
            Send(json);
            return null;
        }
    }

    private void Send(String json) {
        try {
            Log.d(TAG, "Send: " + json);

            Socket socket = new Socket("localhost", 8081);
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            output.write(json.getBytes("UTF-8"));

            output.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
