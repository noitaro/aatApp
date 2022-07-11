package com.example.myapplication;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.databinding.FragmentGalleryBinding;
import com.example.myapplication.models.Workspace;
import com.example.myapplication.service.BackgroundService;
import com.example.myapplication.ui.dialog.ProgressDialogFragment;
import com.example.myapplication.ui.gallery.GalleryFragment;
import com.example.myapplication.ui.home.HomeFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private ProgressDialogFragment progressDialog;

    private MediaProjectionManager mMediaProjectionManager;

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

        ImageView imageView2 = (ImageView) root.findViewById(R.id.imageView2);
        imageView2.setOnClickListener(v -> {

            mMediaProjectionManager = (MediaProjectionManager)this.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mStartForResult.launch(mMediaProjectionManager.createScreenCaptureIntent());
        });
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


    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");

        Intent intent = new Intent(this, BackgroundService.class);
        stopService(intent);

        if (progressDialog != null) {
            progressDialog.dismissAllowingStateLoss();
        }

        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Handle the Intent
                    Log.i(TAG, "Starting screen capture");

                    progressDialog = new ProgressDialogFragment();
                    progressDialog.show(getSupportFragmentManager(), TAG);

                    // Luaスクリプトを中断させるため
                    String javaScript =
                            "Blockly.Lua.STATEMENT_PREFIX = 'MyLua2Java.sleep(0);';" +
                                    "Blockly.Lua.workspaceToCode(workspace);";

                    viewModel.getWebView().evaluateJavascript(javaScript, value -> {
                        //Log.d(TAG,"Blockly.Lua.workspaceToCode " + value);

                        Intent intent = new Intent(getApplication(), BackgroundService.class);
                        intent.putExtra("luaCode", value);
                        intent.putExtra("resultCode", result.getResultCode());
                        intent.putExtra("resultData", result.getData());
                        intent.putExtra("workspaceName", viewModel.mWorkspaceName);
                        startForegroundService(intent);
                        finish();
                    });
                } else {
                    Log.i(TAG, "User cancelled");
                    Toast.makeText(getApplicationContext(), "画面共有を拒否しました。", Toast.LENGTH_SHORT).show();
                }
            }
        }
    );




}