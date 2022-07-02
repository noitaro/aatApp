package com.example.myapplication.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MainViewModel;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentGalleryBinding;
import com.example.myapplication.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private MainViewModel viewModel;
    private FragmentHomeBinding binding;
    protected WebView mWebView = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mWebView = (WebView) root.findViewById(R.id.webView1);

        mWebView.setWebChromeClient(new WebChromeClient() {
            boolean isCompleted = false;
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    isCompleted = false;
                    Log.d(TAG,"onProgressChanged " + newProgress);
                } else {
                    if (!isCompleted) {
                        isCompleted = true;
                        Log.d(TAG,"onProgressChanged " + newProgress);

                        String xml = viewModel.mWebViewOnWorkspaceXml;
                        if (!xml.equals("")) {
                            String script =
                                "let xml = Blockly.Xml.textToDom('"+ xml.replace("\"", "\\\"") +"');\n " +
                                "Blockly.Xml.domToWorkspace(xml, workspace);";
                            Log.d(TAG, "onProgressChanged: " + script);
                            view.evaluateJavascript(script, null);
                        }

                    }
                }
            }
        });
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        WebAppInterface webAppInterface = new WebAppInterface(inflater.getContext());
        mWebView.addJavascriptInterface(webAppInterface, "Android");
        mWebView.loadUrl("file:///android_asset/blockly/webview.html");

        viewModel.setWebView(mWebView);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");



    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG,"onDestroyView");
        binding = null;
    }














}