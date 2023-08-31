package com.example.mlworld.helper;

import com.example.mlworld.R;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public abstract class ImageHelperActivity extends AppCompatActivity {

    private String TAG = ImageHelperActivity.class.getSimpleName();
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1000;
    public final static int PICK_IMAGE_ACTIVITY_REQUEST_CODE = 1064;
    public final static int REQUEST_READ_EXTERNAL_STORAGE = 2031;

    private File photoFile;
    private ImageView inputImageView;
    private TextView outputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_helper);

        takePermissions();
        inputImageView = findViewById(R.id.imageView);
        outputTextView = findViewById(R.id.textView);
        findViewById(R.id.buttonPickPhoto).setOnClickListener(this::onPickImage);
        findViewById(R.id.buttonStartCamera).setOnClickListener(this::onStartCamera);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap bitmap = getCapturedImage();
                rotateIfRequired(bitmap);
                inputImageView.setImageBitmap(bitmap);
                runClassification(bitmap);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PICK_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = loadFromUri(data.getData());
                inputImageView.setImageBitmap(takenImage);
                runClassification(takenImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't selected!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onPickImage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public void onStartCamera(View view) {
        // Create a file to share with camera
        photoFile = getPhotoFileUri();

        // wrap File object into a content provider, required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(this, "com.codepath.fileprovider", photoFile);

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    /**
     * Create a File to share with camera where captured image will stored in Internal memory
     * Returns the File for a photo stored on disk given the fileName
     * */
    public File getPhotoFileUri() {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }

    /**
     * getCapturedImage(): Decodes and crops the captured image from camera.
     * To downgrade image size!
     */
    private Bitmap getCapturedImage() {
        // Get the dimensions of the View
        int targetW = inputImageView.getWidth();
        int targetH = inputImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;
        int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH /targetH));

        bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inMutable = true;
        return BitmapFactory.decodeFile(photoFile.getAbsolutePath(), bmOptions);
    }

    private void rotateIfRequired(Bitmap bitmap) {
        try {
            ExifInterface exifInterface = new ExifInterface(photoFile.getAbsolutePath());
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
            );

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                rotateImage(bitmap, 90f);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                rotateImage(bitmap, 180f);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                rotateImage(bitmap, 270f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rotate the given bitmap.
     */
    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true
        );
    }

    private Bitmap loadFromUri(Uri uri) {
        Bitmap bitmap = null;
        try {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                // modern way of converting bitmap for for SDK > 27
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            } else {
                // older way
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * Draw bounding boxes around objects together with the object's name.
     */
    protected Bitmap drawDetectionResult(Bitmap bitmap, List<BoxWithTitle> detectionResults) {
        Bitmap outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(outputBitmap);

        Paint penRect = new Paint();
        penRect.setTextAlign(Paint.Align.LEFT);
        penRect.setColor(Color.RED);
        penRect.setStrokeWidth(8F);
        penRect.setStyle(Paint.Style.STROKE);

        Paint penTitle = new Paint();
        penTitle.setStyle(Paint.Style.FILL_AND_STROKE);
        penTitle.setColor(Color.YELLOW);
        penTitle.setStrokeWidth(2F);
        penTitle.setTextSize(96F);

        for (BoxWithTitle box : detectionResults) {
            // draw bounding box
            canvas.drawRect(box.rect, penRect);

            // calculate the right font size
            Rect titleRect = new Rect(0, 0, 0, 0);
            penTitle.getTextBounds(box.title, 0, box.title.length(), titleRect);
            float fontSize = penTitle.getTextSize() * box.rect.width() / titleRect.width();

            // adjust the font size so texts are inside the bounding box
            if (penTitle.getTextSize() > fontSize) penTitle.setTextSize(fontSize);

            float margin = (box.rect.width() - titleRect.width()) / 2.0F;
            if (margin < 0F) margin = 0F;
            canvas.drawText(box.title, box.rect.left + margin, box.rect.top + titleRect.height(), penTitle);
        }
        return outputBitmap;
    }


    protected TextView getOutputTextView() {
        return outputTextView;
    }

    protected ImageView getInputImageView() {
        return inputImageView;
    }

    protected abstract void runClassification(Bitmap bitmap);

    private void takePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        }
    }
}