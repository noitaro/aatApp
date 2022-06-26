package com.example.myapplication.service;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class OpenCVMatchTemplate {
    public void run(String inFile, String templateFile, String outFile, int match_method) {
        System.out.println("\nRunning Template Matching");

        Mat img = Imgcodecs.imread(inFile);
        Mat temple = Imgcodecs.imread(templateFile);

        // / Create the result matrix
        int result_cols = img.cols() - temple.cols() + 1;
        int result_rows = img.rows() - temple.rows() + 1;
        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        // / Do the Matching and Normalize
        Imgproc.matchTemplate(img, temple, result, match_method);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        // / Localizing the best match with minMaxLoc
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        Point matchLoc;
        if (match_method == Imgproc.TM_SQDIFF
                || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
        } else {
            matchLoc = mmr.maxLoc;
        }

        // / Show me what you got
        Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + temple.cols(),matchLoc.y + temple.rows()), new Scalar(0, 255, 0));

        // Save the visualized detection.
        System.out.println("Writing " + outFile);
        Imgcodecs.imwrite(outFile, img);

    }
}
