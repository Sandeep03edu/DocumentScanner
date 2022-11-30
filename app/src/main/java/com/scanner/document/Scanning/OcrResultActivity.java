package com.scanner.document.Scanning;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.scanner.document.R;
import com.scanner.document.Utils.AppUtils;
import com.scanner.document.Utils.Constants;
import com.theartofdev.edmodo.cropper.CropImageView;

public class OcrResultActivity extends AppCompatActivity {

    private static final String TAG = "OcrResultActivityTag";
    CropImageView resultImage;
    CardView copyText, retake, continueImage, scanMore;
    private String copiedText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_result);

        // Initialising views
        _init();

        // Set intent data
        SetIntentData();

        copyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = resultImage.getCroppedImage();
                copiedText = AppUtils.CopyTextFromBitmap(OcrResultActivity.this, bitmap);

                if (copiedText == null) {
                    Toast.makeText(OcrResultActivity.this, "No text found", Toast.LENGTH_SHORT).show();
                    return;
                }

                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Ocr Text", copiedText);
                clipboard.setPrimaryClip(clip);

                String toastText = "Copied: \n";

                if (copiedText.length() > 100) {
                    copiedText = copiedText.substring(0, 100);
                    copiedText += "...";
                }

                toastText += copiedText;

                Toast.makeText(OcrResultActivity.this, toastText, Toast.LENGTH_SHORT).show();

            }
        });

        retake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(Constants.OCRAction, Constants.OCR_RETAKE);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        continueImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri imageUri = AppUtils.saveBitmapToCache(OcrResultActivity.this, resultImage.getCroppedImage());

                if (copiedText == null) {
                    copiedText = "";
                }
                Log.d(TAG, "onClick: ScanActivityTag : Going back to scan");
                Intent intent = new Intent();
                intent.putExtra(Constants.ImageUri, String.valueOf(imageUri));
                intent.putExtra(Constants.OCRAction, Constants.OCR_SAVE_RESULT);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        scanMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri imageUri = AppUtils.saveBitmapToCache(OcrResultActivity.this, resultImage.getCroppedImage());

                if (copiedText == null) {
                    copiedText = "";
                }
                Log.d(TAG, "onClick: ScanActivityTag : Going back to scan");
                Intent intent = new Intent();
                intent.putExtra(Constants.ImageUri, String.valueOf(imageUri));
                intent.putExtra(Constants.OCRAction, Constants.OCR_SCAN_MORE);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    private void SetIntentData() {
        if (getIntent() != null) {
            if (getIntent().hasExtra(Constants.ImageUri)) {
                String imageUri = getIntent().getStringExtra(Constants.ImageUri);
                Uri uri = Uri.parse(imageUri);
                resultImage.setImageUriAsync(uri);
            }
        }
    }


    private void _init() {
        resultImage = findViewById(R.id.ocr_result_image);
        copyText = findViewById(R.id.ocr_result_copy_text);
        continueImage = findViewById(R.id.ocr_result_continue);
        retake = findViewById(R.id.ocr_result_retake);
        scanMore = findViewById(R.id.ocr_result_scan_more);
    }
}