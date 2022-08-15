package com.example.myapplication.ui.gallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.MainViewModel;
import com.example.myapplication.R;
import com.example.myapplication.databinding.FragmentGalleryBinding;
import com.example.myapplication.models.Workspace;

import org.luaj.vm2.ast.Str;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GalleryFragment extends Fragment implements CustomAdapter.OnAdapterListener {

    private static final String TAG = "GalleryFragment";
    private MainViewModel viewModel;
    private FragmentGalleryBinding binding;
    private Context _context;

    protected RecyclerView mRecyclerView;
    protected CustomAdapter mAdapter;
    protected List<Workspace> mWorkspaceList = new ArrayList<Workspace>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        _context = getContext();

        // ワークスペースリスト作成
        createWorkspaceList();

        mAdapter = new CustomAdapter(mWorkspaceList);
        mAdapter.setOnAdapterListener(this);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");

        Button button = (Button)getActivity().findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/zip");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"application/zip"});
                resultLauncher.launch(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG,"onDestroyView");
        binding = null;
    }

    ActivityResultLauncher resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent resultData = result.getData();
            if (resultData != null) {
                Uri uri = resultData.getData();
                Log.d("ActivityResultLauncher",uri.toString());

                AppCompatEditText editText = new AppCompatEditText(getContext());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("ワークスペース名入力");
                builder.setMessage("保存するワークスペース名を入力してください。");
                builder.setView(editText);
                builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String wsName = editText.getText().toString();
                        if (!wsName.equals("")) {
                            unzip(uri, wsName);

                            // ワークスペースリスト作成
                            createWorkspaceList();
                        }
                    }
                });
                builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create();
                builder.show();
            }
        }
    });

    private void unzip(Uri uri, String wsName) {
        try {
            InputStream stream = _context.getContentResolver().openInputStream(uri);
            ZipInputStream is = new ZipInputStream(stream);
            while (true) {
                ZipEntry zipEntry = is.getNextEntry();
                if (zipEntry == null) {
                    break;
                }
                Log.d("zipEntryName",zipEntry.getName());

                FileOutputStream os = _context.openFileOutput(wsName + "_" + zipEntry.getName(), _context.MODE_PRIVATE);

                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = is.read(buffer))>0) {
                    os.write(buffer, 0, length);
                }
                os.close();
                is.closeEntry();
            }

            is.close();
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnButtonPressed(Workspace workspace) {
        Log.d(TAG,"OnButtonPressed " + workspace.Name);
        StringBuffer workspaceXml = new StringBuffer();

        try {
            File file = new File(_context.getFilesDir(), workspace.Name+"_workspace.xml");

            FileInputStream is = _context.openFileInput(file.getName());
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String str;
            while ((str = reader.readLine()) != null) {
                workspaceXml.append(str);
            }

            reader.close();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "OnButtonPressed: " + workspaceXml.toString());
        viewModel.mWorkspaceName = workspace.Name;
        viewModel.mWebViewOnWorkspaceXml = workspaceXml.toString();

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("com.example.myapp.workspace_name", viewModel.mWorkspaceName);
        editor.putString("com.example.myapp.workspace_xml", viewModel.mWebViewOnWorkspaceXml);
        editor.apply();

        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_gallery_to_home);
    }

    private void createWorkspaceList() {

        mWorkspaceList.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] fileNames = _context.fileList();
                for (String fileName: fileNames){
                    if (fileName.contains("workspace.xml")) {
                        Workspace workspace = new Workspace();
                        workspace.Name = fileName.replace("_workspace.xml","");
                        mWorkspaceList.add(workspace);
                    }
                }
            }
        }).start();

    }












}