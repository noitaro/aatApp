package com.example.myapplication.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
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

import org.luaj.vm2.ast.Str;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private MainViewModel viewModel;
    private FragmentHomeBinding binding;
    protected WebView mWebView = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView1");

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mWebView = (WebView) root.findViewById(R.id.webView1);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String workspaceName = sharedPref.getString("com.example.myapp.workspace_name", "");
        Log.d(TAG,"onCreateView2: " + workspaceName);
        viewModel.mWorkspaceName = workspaceName;

        String workspaceXml = sharedPref.getString("com.example.myapp.workspace_xml", "");
        Log.d(TAG,"onCreateView3: " + workspaceXml);
        viewModel.mWebViewOnWorkspaceXml = workspaceXml;

        mWebView.setWebChromeClient(new WebChromeClient() {
            boolean isCompleted = false;
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    isCompleted = false;
                    Log.d(TAG,"onProgressChanged " + newProgress);
                } else {
                    if (!isCompleted) {
                        // webview.html を読み込み終わった。
                        isCompleted = true;
                        Log.d(TAG,"onProgressChanged " + newProgress);

                        String xml = viewModel.mWebViewOnWorkspaceXml;
                        if (!xml.equals("")) {
                            String workspaceXml = xml.replace("\"", "\\\"");
                            Log.d(TAG, "onProgressChanged: workspace-> " + workspaceXml);
                            String script =
                                    "let xml = Blockly.Xml.textToDom('"+ workspaceXml +"');\n " +
                                            "Blockly.Xml.domToWorkspace(xml, workspace);";
                            view.evaluateJavascript(script, null);
                        }

                    }
                }
            }
        });
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        WebAppInterface webAppInterface = new WebAppInterface(this.getContext());
        mWebView.addJavascriptInterface(webAppInterface, "Android");
        viewModel.setWebView(mWebView);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
        mWebView.loadUrl("file:///android_asset/blockly/webview.html");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");

        String script =
                "let xmlDom = Blockly.Xml.workspaceToDom(workspace);\n" +
                        "Blockly.Xml.domToPrettyText(xmlDom);";
        mWebView.evaluateJavascript(script, value -> {
            Log.d(TAG, "onStop1: after-> " + value);
            if (value != "\"null\"") {
                value = value.replace("\\n", "");
                value = value.replace("\\\"", "\"");
                value = value.replace("\\u003C", "<");
                // 先頭と末尾のダブルクオート削除
                value = value.substring(1, value.length()-1);
                Log.d(TAG, "onStop2: before-> " + viewModel.mWebViewOnWorkspaceXml);
                viewModel.mWebViewOnWorkspaceXml = value;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG,"onDestroyView1");

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        Log.d(TAG,"onDestroyView2: " + viewModel.mWorkspaceName);
        editor.putString("com.example.myapp.workspace_name", viewModel.mWorkspaceName);
        Log.d(TAG,"onDestroyView2: " + viewModel.mWebViewOnWorkspaceXml);
        editor.putString("com.example.myapp.workspace_xml", viewModel.mWebViewOnWorkspaceXml);
        editor.apply();

        binding = null;
    }














}