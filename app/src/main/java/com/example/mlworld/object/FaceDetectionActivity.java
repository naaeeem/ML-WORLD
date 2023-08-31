package com.example.mlworld.object;

import com.example.mlworld.helper.BoxWithTitle;
import com.example.mlworld.helper.ImageHelperActivity;

import android.graphics.Bitmap;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

public class FaceDetectionActivity extends ImageHelperActivity {

    private FaceDetector faceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // High-accuracy landmark detection and face classification
        FaceDetectorOptions highAccuracyOpts =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .enableTracking()
                        .build();

        faceDetector = FaceDetection.getClient(highAccuracyOpts);
    }

    @Override
    protected void runClassification(Bitmap bitmap) {
        Bitmap finalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        InputImage image = InputImage.fromBitmap(finalBitmap, 0);

        faceDetector.process(image)
                .addOnSuccessListener(faces -> {
                    if (faces.isEmpty()) {
                        getOutputTextView().setText("No faces detected");
                    } else {
                        getOutputTextView().setText(String.format("%d faces detected", faces.size()));
                        List<BoxWithTitle> boxes = new ArrayList();
                        for (Face face : faces) {
                            boxes.add(new BoxWithTitle(face.getTrackingId() + "", face.getBoundingBox()));
                        }
                        getInputImageView().setImageBitmap(drawDetectionResult(finalBitmap, boxes));
                    }
                })
                .addOnFailureListener(error -> {
                    error.printStackTrace();
                })
        ;
    }
}