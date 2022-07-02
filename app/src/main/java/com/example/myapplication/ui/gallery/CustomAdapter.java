package com.example.myapplication.ui.gallery;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.Workspace;
import com.example.myapplication.R;

import java.io.FileNotFoundException;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> implements CustomViewHolder.OnViewHolderListener {
    private static final String TAG = "CustomAdapter";

    public List<Workspace> mWorkspaceList;

    public CustomAdapter(List<Workspace> list) {
        mWorkspaceList = list;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workspace_item, parent, false);

        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        Workspace workspace = mWorkspaceList.get(position);
        holder.setWorkspace(workspace);
        holder.setOnViewHolderListener(this);
    }

    @Override
    public int getItemCount() {
        return mWorkspaceList.size();
    }


    @Override
    public void OnButtonPressed(Workspace workspace) {
        Log.d(TAG,"OnButtonPressed " + workspace.Name);
        callback.OnButtonPressed(workspace);
    }

    OnAdapterListener callback;
    public void setOnAdapterListener(OnAdapterListener callback) {
        this.callback = callback;
    }

    interface OnAdapterListener {
        public void OnButtonPressed(Workspace workspace);
    }













}
