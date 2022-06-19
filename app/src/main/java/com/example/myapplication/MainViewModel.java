package com.example.myapplication;

import android.webkit.WebView;

import androidx.lifecycle.ViewModel;

import com.example.myapplication.models.Workspace;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends ViewModel {

    private final BackgroundService backgroundService;

    public MainViewModel() {
        this.backgroundService = new BackgroundService();


        for (int i = 0; i < 60; i++) {
            Workspace workspace = new Workspace();
            workspace.text = "This is element #" + i;
            workspace.xml = "<xml xmlns=\\\"https://developers.google.com/blockly/xml\\\" id=\\\"workspaceBlocks\\\" style=\\\"display: none\\\"><block type=\\\"text\\\" id=\\\"[QfadHl!T2bzs6Te0Gt.\\\" x=\\\"63\\\" y=\\\"63\\\"><field name=\\\"TEXT\\\"></field></block></xml>";
            mWorkspaceList.add(workspace);
        }

    }


    private List<Workspace> mWorkspaceList = new ArrayList<Workspace>();
    public List<Workspace> getWorkspaceList() {
        return mWorkspaceList;
    }


    private int mSelectedPosition = -1;
    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
    }


    public String getXml() {
        if (mSelectedPosition == -1) {
            return "";
        } else {
            return mWorkspaceList.get(mSelectedPosition).xml;
        }
    }

    private WebView mWebView;
    public void setWebView(WebView webView) {
        mWebView = webView;
    }

    public WebView getWebView() {
        return mWebView;
    }
}