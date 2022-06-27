package com.example.myapplication.ui.slideshow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.MainViewModel;
import com.example.myapplication.databinding.FragmentSlideshowBinding;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class SlideshowFragment extends Fragment {

    private MainViewModel viewModel;
    private FragmentSlideshowBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        ImageView imageView = new ImageView(getContext());

        String[] files = getContext().fileList();
        File file = new File(getContext().getFilesDir(), files[0]);
        Bitmap bmp = BitmapFactory.decodeFile(file.getPath());

        bmp = rgb2gray(bmp);

        imageView.setImageBitmap(bmp);

        return imageView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //RGB→Gray
    private Bitmap rgb2gray(Bitmap image) {
        Mat img_Mat = new Mat();
        Utils.bitmapToMat(image, img_Mat);
        Imgproc.cvtColor(img_Mat, img_Mat, Imgproc.COLOR_BGR2GRAY);
        Bitmap result = image.copy(Bitmap.Config.ARGB_8888, true);
        Utils.matToBitmap(img_Mat, result);
        return result;
    }
}