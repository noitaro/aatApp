package com.example.myapplication;

import android.webkit.WebView;

import androidx.lifecycle.ViewModel;

import com.example.myapplication.models.Workspace;
import com.example.myapplication.service.BackgroundService;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {

    public MainViewModel() { }

    public String mWorkspaceName = "";
    public String mWebViewOnWorkspaceXml = "";

    private WebView mWebView;
    public void setWebView(WebView webView) {
        mWebView = webView;
    }
    public WebView getWebView() {
        return mWebView;
    }
}