package com.example.mlworld.audio;

import com.example.mlworld.helper.AudioHelperActivity;

import android.media.AudioRecord;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

public class AudioClassificationActivity extends AudioHelperActivity {

    String modelPath = "yamnet_classification.tflite"; // https://tfhub.dev/google/lite-model/yamnet/classification/tflite/1
    float probabilityThreshold = 0.3f;

    AudioClassifier classifier;
    private TensorAudio tensorAudio;

    private AudioRecord record;
    private TimerTask timerTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onStartRecording(View view) {
        super.onStartRecording(view);

        // Loading the model from the assets folder
        try {
            classifier = AudioClassifier.createFromFile(this, modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Creating an audio recorder
        tensorAudio = classifier.createInputTensorAudio();

        // showing the audio recorder specification
        TensorAudio.TensorAudioFormat format = classifier.getRequiredTensorAudioFormat();
        String specs = "Number of channels: " + format.getChannels() + "\n"
                + "Sample Rate: " + format.getSampleRate();
        specsTextView.setText(specs);

        // Creating and start recording
        record = classifier.createAudioRecord();
        record.startRecording();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                // Classifying audio data
                // val numberOfSamples = tensor.load(record)
                // val output = classifier.classify(tensor)
                int numberOfSamples = tensorAudio.load(record);
                List<Classifications> output = classifier.classify(tensorAudio);

                // Filtering out classifications with low probability
                List<Category> finalOutput = new ArrayList<>();
                for (Classifications classifications : output) {
                    for (Category category : classifications.getCategories()) {
                        if (category.getScore() > probabilityThreshold) {
                            finalOutput.add(category);
                        }
                    }
                }

                // Sorting the results
                Collections.sort(finalOutput, (o1, o2) -> (int) (o1.getScore() - o2.getScore()));

                // Creating a multiline string with the filtered results
                StringBuilder outputStr = new StringBuilder();
                for (Category category : finalOutput) {
                    outputStr.append(category.getLabel())
                            .append(": ").append(category.getScore()).append("\n");
                }

                // Updating the UI, because it's running inside a timer thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalOutput.isEmpty()) {
                            outputTextView.setText("Could not classify");
                        } else {
                            outputTextView.setText(outputStr.toString());
                        }
                    }
                });
            }
        };

        new Timer().scheduleAtFixedRate(timerTask, 1, 500);
    }


    public void onStopRecording(View view) {
        super.onStopRecording(view);

        timerTask.cancel();
        record.stop();
    }
}