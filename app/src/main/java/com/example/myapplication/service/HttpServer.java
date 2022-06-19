package com.example.myapplication.service;

import android.view.KeyEvent;

import androidx.core.view.InputDeviceCompat;

import com.example.myapplication.models.InjectEvent;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    public static void main(String[] args) {
        System.out.println("HttpServer: start");

        ServerSocket server = null;

        try {
            Gson gson = new Gson();
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(8081));
            System.out.println("Server listening on port 8081...");
            InputService inputService = new InputService();

            while (true) {
                Socket socket = server.accept();// 接続まで待機
                System.out.println("Connected");

                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

                while (true) {
                    String line = input.readLine();
                    System.out.println(line);

                    if (line == null) {
                        break;
                    }

                    InjectEvent inject = gson.fromJson(line, InjectEvent.class);

                    if (inject.event.equals("key")) {
                        System.out.println("inject.event: key");
                        try {
                            inputService.injectKeyEvent(new KeyEvent(inject.action, inject.code));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else if (inject.event.equals("motion")) {
                        System.out.println("inject.event: motion");
                        try {
                            inputService.injectMotionEvent(InputDeviceCompat.SOURCE_TOUCHSCREEN, inject.action, inject.x, inject.y);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                input.close();
                socket.close();

                System.out.println("Connect: end");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (server != null) {
                try {
                    server.close();
                    server = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("HttpServer: end");
    }
}
