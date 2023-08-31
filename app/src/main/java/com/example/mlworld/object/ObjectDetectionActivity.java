package com.example.mlworld.object;

import com.example.mlworld.helper.BoxWithTitle;
import com.example.mlworld.helper.ImageHelperActivity;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;


public class ObjectDetectionActivity extends ImageHelperActivity {

    private ObjectDetector objectDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Multiple object detection in static images
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build();

        objectDetector = ObjectDetection.getClient(options);
    }

    @Override
    protected void runClassification(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        objectDetector.process(image).addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
            @Override
            public void onSuccess(List<DetectedObject> detectedObjects) {
                if (detectedObjects.isEmpty()) {
                    getOutputTextView().setText("Could not detect!!");
                } else {
                    // Task completed successfully
                    StringBuilder sb = new StringBuilder();
                    List<BoxWithTitle> list = new ArrayList<>();
                    int index = 0;
                    for (DetectedObject object : detectedObjects) {
                        if (object.getLabels().isEmpty()) {
                            list.add(new BoxWithTitle(++index + ". Unknown", object.getBoundingBox()));
                        } else {
                            for (DetectedObject.Label label : object.getLabels()) {
                                sb.append(label.getText()).append(" : ").append(label.getConfidence()).append("\n");
                            }
                            list.add(new BoxWithTitle(++index + ". " + object.getLabels().get(0).getText(), object.getBoundingBox()));
                        }
                    }

                    getInputImageView().setImageBitmap(drawDetectionResult(bitmap, list));
                    getOutputTextView().setText(sb.toString());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Task failed with an exception
                // ...
                e.printStackTrace();
            }
        });

    }
}
