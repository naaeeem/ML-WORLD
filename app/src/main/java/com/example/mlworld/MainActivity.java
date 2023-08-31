package com.example.mlworld;

import com.example.mlworld.audio.AudioClassificationActivity;
import com.example.mlworld.audio.BirdSoundDetectorActivity;
import com.example.mlworld.image.FlowerIdentificationActivity;
import com.example.mlworld.image.ImageClassificationActivity;
import com.example.mlworld.object.FaceDetectionActivity;
import com.example.mlworld.object.ObjectDetectionActivity;
import com.example.mlworld.text.SpamTextDetectionActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(this::onGoToImageActivity);
        findViewById(R.id.button_flower).setOnClickListener(this::onGoToFlowerIdentificationActivity);
        findViewById(R.id.button_object_detection).setOnClickListener(this::onGoToObjectDetection);
        findViewById(R.id.button_face_detection).setOnClickListener(this::onGoToFaceDetection);
        findViewById(R.id.button_audio_classification).setOnClickListener(this::onGoToAudioClassification);
        findViewById(R.id.button_bird_sound_detection).setOnClickListener(this::onGoToBirdSoundDetection);
        findViewById(R.id.button_spam_text_detection).setOnClickListener(this::onGoToSpamTextDetection);
    }

    public void onGoToImageActivity(View view) {
        Intent intent = new Intent(this, ImageClassificationActivity.class);
        startActivity(intent);
    }

    public void onGoToFlowerIdentificationActivity(View view) {
        Intent intent = new Intent(this, FlowerIdentificationActivity.class);
        startActivity(intent);
    }

    public void onGoToObjectDetection(View view) {
        Intent intent = new Intent(this, ObjectDetectionActivity.class);
        startActivity(intent);
    }

    public void onGoToFaceDetection(View view) {
        Intent intent = new Intent(this, FaceDetectionActivity.class);
        startActivity(intent);
    }

    public void onGoToAudioClassification(View view) {
        Intent intent = new Intent(this, AudioClassificationActivity.class);
        startActivity(intent);
    }

    public void onGoToBirdSoundDetection(View view) {
        Intent intent = new Intent(this, BirdSoundDetectorActivity.class);
        startActivity(intent);
    }

    public void onGoToSpamTextDetection(View view) {
        Intent intent = new Intent(this, SpamTextDetectionActivity.class);
        startActivity(intent);
    }

}