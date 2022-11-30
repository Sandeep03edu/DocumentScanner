package com.scanner.document.Scanning;

import static android.icu.text.DateTimePatternGenerator.PatternInfo.OK;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;
import com.scanner.document.Model.Document;
import com.scanner.document.R;
import com.scanner.document.Utils.AppUtils;
import com.scanner.document.Utils.Constants;
import com.scanner.document.Utils.CropImagesActivity;
import com.scanner.document.Utils.ImageResizer;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

public class ConfigureScan extends AppCompatActivity {

    private static final String TAG = "ConfigureScanTag";
    TextView cancel, savePdf;
    EditText fileName;
    CropImageView imageView;

    LinearLayout addPage, reorder, crop, rotate, filter, delete;
    Bitmap imageBitmap = null;
    Document document = null;
    int imgPos = -1;
    private Bitmap croppedBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_scan);

        // Initialising views
        _init();

        // Setting intent data
        SetIntentData();

        // Set Layout click actions
        SetClickAction();
    }

    private void SetClickAction() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        savePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileName.getText().toString().trim().isEmpty()) {
                    Toast.makeText(ConfigureScan.this, "Please enter a valid file name", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                imageBitmap = croppedBitmap;
                if (imageBitmap != null) {
                    Uri imageUri = AppUtils.saveBitmapToCache(ConfigureScan.this, imageBitmap);
                    if (imageUri != null) {
                        intent.putExtra(Constants.ImageUri, String.valueOf(imageUri));
                    }
                }
                intent.putExtra(Constants.FolderName, fileName.getText().toString().trim());
                intent.putExtra(Constants.ConfigureAction, Constants.ConfigureSavePdf);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        addPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                imageBitmap = croppedBitmap;
                if (imageBitmap != null) {
                    Uri imageUri = AppUtils.saveBitmapToCache(ConfigureScan.this, imageBitmap);
                    if (imageUri != null) {
                        intent.putExtra(Constants.ImageUri, String.valueOf(imageUri));
                    }
                }
                intent.putExtra(Constants.FolderName, fileName.getText().toString().trim());

                setResult(RESULT_OK, intent);
                finish();
            }
        });

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageBitmap == null) {
                    return;
                }
                imageBitmap = AppUtils.rotateBitmap(imageBitmap, 90);
                if (imageBitmap == null) {
                    return;
                }
                imageView.setImageBitmap(imageBitmap);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (document == null || imgPos == -1) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.ConfigureAction, Constants.ConfigureDeletePdf);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    // TODO : Open Image in full screen
                }
            }
        });

        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sending activity for cropping image
                Intent cropImagesIntent = new Intent(getApplicationContext(), CropImagesActivity.class);
                Bundle imagesBundle = new Bundle();

                ArrayList<Uri> uriArrayList = new ArrayList<>();
                uriArrayList.add(AppUtils.saveBitmapToCache(ConfigureScan.this, imageBitmap));
                imagesBundle.putSerializable(Constants.IMAGES, uriArrayList);
                cropImagesIntent.putExtra(Constants.IMAGES_BUNDLE, imagesBundle);
                cropImagesIntent.putExtra(Constants.DISABLE_ASPECT_CROP, true);
                startActivityForResult(cropImagesIntent, Constants.CROP_IMAGE_REQUEST_CODE);
            }
        });

        reorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                imageBitmap = croppedBitmap;
                if (imageBitmap != null) {
                    Uri imageUri = AppUtils.saveBitmapToCache(ConfigureScan.this, imageBitmap);
                    if (imageUri != null) {
                        intent.putExtra(Constants.ImageUri, String.valueOf(imageUri));
                    }
                }
                intent.putExtra(Constants.FolderName, fileName.getText().toString().trim());
                intent.putExtra(Constants.ConfigureAction, Constants.ConfigureReorderPdf);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfigureScan.this, FilterImage.class);
                if(imageBitmap==null) return;

                imageBitmap = ImageResizer.reduceBitmapSize(imageBitmap, 240000);
                byte[] bytes = AppUtils.Bitmap2Bytes(imageBitmap);
                intent.putExtra(Constants.ImageBitmapBytes, bytes);
                startActivityForResult(intent, Constants.FilterRequest);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.CROP_IMAGE_REQUEST_CODE && data != null) {
                Bundle imageBundle = data.getBundleExtra(Constants.IMAGES_BUNDLE);
                ArrayList<Uri> croppedUriArrayList = (ArrayList<Uri>) imageBundle.getSerializable(Constants.IMAGES);
                if (croppedUriArrayList.size() > 0) {
                    Uri currUri = croppedUriArrayList.get(0);
                    imageBitmap = AppUtils.Uri2Bitmap(this, currUri);
                    imageView.setImageBitmap(imageBitmap);
                }
            }

            if(requestCode==Constants.FilterRequest && data!=null && data.hasExtra(Constants.ImageBitmapBytes)){
                byte[] bytes = data.getByteArrayExtra(Constants.ImageBitmapBytes);
                imageBitmap = AppUtils.Bytes2Bitmap(bytes);
                imageView.setImageBitmap(imageBitmap);
            }
        }
    }

    private void SetIntentData() {
        if (getIntent() == null) {
            return;
        }

        if (getIntent().hasExtra(Constants.DOCUMENT)) {
            document = new Gson().fromJson(getIntent().getStringExtra(Constants.DOCUMENT), Document.class);
        }
        if (getIntent().hasExtra(Constants.IMAGE_POS)) {
            imgPos = getIntent().getIntExtra(Constants.IMAGE_POS, -1);
        }

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.FolderName)) {
            String name = intent.getStringExtra(Constants.FolderName);
            fileName.setText(name);
        }

        if (intent.hasExtra(Constants.ImageBitmapBytes)) {
            byte[] bytes = intent.getByteArrayExtra(Constants.ImageBitmapBytes);
            imageBitmap = AppUtils.Bytes2Bitmap(bytes);
            croppedBitmap = imageBitmap;

            setBitmapRotated();

            if (imageBitmap != null) {
                imageView.setImageBitmap(imageBitmap);
            }
        }
    }

    private void setBitmapRotated() {
        InputImage inputImage = InputImage.fromBitmap(imageBitmap, 0);

        // Multiple object detection in static images
        ObjectDetectorOptions options =
                new ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()  // Optional
                        .build();

        ObjectDetector objectDetector = ObjectDetection.getClient(options);

        Rect rect = new Rect();
        rect.set(0, 0, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);

        objectDetector.process(inputImage)
                .addOnSuccessListener(
                        new OnSuccessListener<List<DetectedObject>>() {
                            @Override
                            public void onSuccess(List<DetectedObject> detectedObjects) {
                                int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE, bottom = Integer.MIN_VALUE, right = Integer.MIN_VALUE;

                                for (DetectedObject detectedObject : detectedObjects) {
                                    Rect box = detectedObject.getBoundingBox();
                                    left = Math.min(left, box.left);
                                    top = Math.min(top, box.top);
                                    bottom = Math.max(bottom, box.bottom);
                                    right = Math.max(right, box.right);
                                }

                                if(left!=Integer.MAX_VALUE && top!=Integer.MAX_VALUE && bottom!=Integer.MIN_VALUE && right!=Integer.MIN_VALUE){
                                    rect.set(left, top, right, bottom);
                                    imageView.setCropRect(rect);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });

        imageView.setOnCropWindowChangedListener(new CropImageView.OnSetCropWindowChangeListener() {
            @Override
            public void onCropWindowChanged() {
                croppedBitmap = imageView.getCroppedImage();
                Log.d(TAG, "onCropImageComplete: Changing window crop");
            }
        });

    }

    /**
     * Initialising views
     */
    private void _init() {
        cancel = findViewById(R.id.configure_scan_cancel);
        savePdf = findViewById(R.id.configure_scan_save_pdf);
        fileName = findViewById(R.id.configure_scan_file_name_hint);

        imageView = findViewById(R.id.configure_scan_page_image);

        addPage = findViewById(R.id.configure_scan_add_page);
        reorder = findViewById(R.id.configure_scan_reorder_page);
        crop = findViewById(R.id.configure_scan_crop_page);
        rotate = findViewById(R.id.configure_scan_rotate_page);
        filter = findViewById(R.id.configure_scan_filter_page);
        delete = findViewById(R.id.configure_scan_delete_page);
    }
}