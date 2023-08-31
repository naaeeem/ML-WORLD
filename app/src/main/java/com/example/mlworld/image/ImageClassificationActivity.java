package com.example.mlworld.image;

import com.example.mlworld.helper.ImageHelperActivity;

import java.util.List;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

public class ImageClassificationActivity extends ImageHelperActivity {
    private ImageLabeler imageLabeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build());

        imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build());
    }

    @Override
    protected void runClassification(Bitmap bitmap) {
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        imageLabeler.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
            @Override
            public void onSuccess(List<ImageLabel> imageLabels) {
                if (imageLabels.size() > 0) {
                    StringBuilder builder = new StringBuilder();
                    for(ImageLabel label : imageLabels) {
                        builder.append(label.getText()).append(" : ").append(label.getConfidence()).append("\n");
                    }
                    getOutputTextView().setText(builder);
                } else {
                    getOutputTextView().setText("Could not Classify!!");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }
}