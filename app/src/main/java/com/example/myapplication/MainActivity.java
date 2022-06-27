package com.example.myapplication;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.ImageView;

import com.example.myapplication.databinding.FragmentGalleryBinding;
import com.example.myapplication.service.BackgroundService;
import com.example.myapplication.service.OpenCVMatchTemplate;
import com.example.myapplication.ui.gallery.GalleryFragment;
import com.example.myapplication.ui.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ActivityMainBinding;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private MainViewModel viewModel;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow).setOpenableLayout(drawer).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        ImageView imageView = (ImageView) root.findViewById(R.id.imageView2);
        imageView.setOnClickListener(v -> {
            Log.d(TAG,"OnClickListener " + v);

            viewModel.getWebView().evaluateJavascript("Blockly.Lua.workspaceToCode(workspace);", (value -> {
                Log.d(TAG,"getLuaCode " + value);

                Intent intent = new Intent(this, BackgroundService.class);
                intent.putExtra("luaCode", value);
                startForegroundService(intent);
            }));

        });

        if(OpenCVLoader.initDebug()){
            Log.d(TAG,"OpenCv configured successfully");
        }else{
            Log.d(TAG,"OpenCv doesn't configured successfully");
        }

        Log.d(TAG, "onCreate: " + OpenCVLoader.OPENCV_VERSION);
        Log.d(TAG, "onCreate: " + this.getExternalFilesDirs(Environment.DIRECTORY_PICTURES)[0]);
        Log.d(TAG, "onCreate: " + this.getExternalFilesDirs(Environment.DIRECTORY_PICTURES)[1]);
        Uri aa = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Log.d(TAG, "onCreate: " + aa);
        String[] files = this.fileList();
        Log.d(TAG, "onCreate: " + files.length);
        //Log.d(TAG, "onCreate: " + files[0]);
        //File file = new File(this.getFilesDir(), files[0]);
        //Log.d(TAG, "onCreate: " + file.toURI());
        //Log.d(TAG, "onCreate: " + file.getPath());

        //Bitmap bmp = BitmapFactory.decodeFile(file.getPath());

        OpenCVMatchTemplate opencv = new OpenCVMatchTemplate();
        //opencv.run(file.getPath(), file.getPath(), file.getPath(), 80);
        //opencv.run(file.toURI().toString(), file.toURI().toString(), file.toURI().toString(), 80);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }





}