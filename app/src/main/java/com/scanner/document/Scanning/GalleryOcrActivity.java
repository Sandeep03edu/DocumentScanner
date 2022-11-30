package com.scanner.document.Scanning;

import static java.lang.Thread.sleep;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.scanner.document.Model.GenericDocument;
import com.scanner.document.Model.OcrDocument;
import com.scanner.document.R;
import com.scanner.document.Saving.GenericDocumentModel;
import com.scanner.document.Utils.AppUtils;
import com.scanner.document.Utils.BackgroundWork;
import com.scanner.document.Utils.Constants;
import com.scanner.document.Utils.CropImagesActivity;
import com.scanner.document.Utils.ImageResizer;

import java.util.ArrayList;

public class GalleryOcrActivity extends AppCompatActivity {

    private static final String TAG = "GalleryOcrTag";
    ViewPager2 viewPager2;
    LinearLayout reorder, crop, filter, delete, rotate, addPage;
    GalleryPagerAdapter galleryPagerAdapter;
    private ArrayList<Uri> images = new ArrayList<>();
    ImageView back;
    TextView check, imageText;
    EditText fileName;
    ProgressDialog progressDialog;
    GenericDocumentModel genericDocumentModel;
    ArrayList<String> finalImagesTexts = new ArrayList<>();
    private ArrayList<String> finalImages = new ArrayList<>();
    boolean updateGenDoc = false;
    GenericDocument genericDocument = new GenericDocument();
    OcrDocument ocrDocument = new OcrDocument();
    String[] text = {""};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_ocr);

        _init();

        // Set Intent Data
        SetPagerData();

        // Set Click Actions
        SetClickActions();
    }

    private void SetClickActions() {
        imageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectCurrOcrText();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = viewPager2.getCurrentItem();
                Uri uri = galleryPagerAdapter.getUri(pos);
                Bitmap bitmap = AppUtils.Uri2Bitmap(GalleryOcrActivity.this, uri);
                bitmap = AppUtils.rotateBitmap(bitmap, 90);
                uri = AppUtils.saveBitmapToCache(GalleryOcrActivity.this, bitmap);
                images.set(pos, uri);
                galleryPagerAdapter.notifyItemChanged(pos);

//                SetOcrText(bitmap);
            }
        });

        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = viewPager2.getCurrentItem();
                Uri uri = galleryPagerAdapter.getUri(pos);

                ArrayList<Uri> uriArrayList = new ArrayList<>();
                uriArrayList.add(uri);

                Intent cropImagesIntent = new Intent(getApplicationContext(), CropImagesActivity.class);
                Bundle imagesBundle = new Bundle();

                imagesBundle.putSerializable(Constants.IMAGES, uriArrayList);
                cropImagesIntent.putExtra(Constants.IMAGES_BUNDLE, imagesBundle);
                cropImagesIntent.putExtra(Constants.DISABLE_ASPECT_CROP, true);
                cropImagesIntent.putExtra(Constants.IMAGE_POS, pos);
                startActivityForResult(cropImagesIntent, Constants.CROP_IMAGE_REQUEST_CODE);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(GalleryOcrActivity.this)
                        .setTitle("Are you sure?")
                        .setMessage("Deleting image can't be undone")
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int pos = viewPager2.getCurrentItem();
                                images.remove(pos);
                                galleryPagerAdapter.notifyItemRemoved(pos);
                                dialogInterface.dismiss();

                                if (images.size() == 0) {
                                    finish();
                                }
                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });


        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = viewPager2.getCurrentItem();
                Uri uri = galleryPagerAdapter.getUri(pos);
                Bitmap bitmap = AppUtils.Uri2Bitmap(GalleryOcrActivity.this, uri);
                if (bitmap == null) return;

                bitmap = ImageResizer.reduceBitmapSize(bitmap, 240000);
                if (bitmap == null) return;
                bitmap = ImageResizer.reduceBitmapSize(bitmap, 240000);

                Intent intent = new Intent(GalleryOcrActivity.this, FilterImage.class);
                intent.putExtra(Constants.IMAGE_POS, pos);
                byte[] bytes = AppUtils.Bitmap2Bytes(bitmap);
                intent.putExtra(Constants.ImageBitmapBytes, bytes);
                startActivityForResult(intent, Constants.FilterRequest);
            }
        });

        addPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(GalleryOcrActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    startActivityForResult(intent, Constants.PICK_GALLERY_IMAGE);
                } else {
                    ActivityCompat.requestPermissions(GalleryOcrActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            }
        });

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetOcrResult();
            }
        });
    }

    private void SetOcrResult() {
        String name = fileName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(GalleryOcrActivity.this, "Enter file name!!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setTitle("Saving document");
        showDialog();

        new BackgroundWork(this) {
            @Override
            public void doInBackground() {
                super.doInBackground();
                finalImages = AppUtils.copyImages(GalleryOcrActivity.this, images);
                getImageText(0);
            }

            @Override
            public void onPostExecute() {
                super.onPostExecute();
            }
        }.execute();
    }

    private void SaveOcr() {
        String name = fileName.getText().toString().trim();
        new BackgroundWork(this) {
            @Override
            public void doInBackground() {
                super.doInBackground();
                ocrDocument.setFileName(name);
//                ocrDocument.setFileTexts(finalImagesTexts);
//                ocrDocument.setFilePhotos(finalImages);
                ocrDocument.getFilePhotos().addAll(finalImages);
                ocrDocument.getFileTexts().addAll(finalImagesTexts);
                genericDocument.setOcrDocument(ocrDocument);
                if(updateGenDoc){
                    genericDocumentModel.update(genericDocument);
                }
                else {
                    genericDocumentModel.insert(genericDocument);
                }
            }

            @Override
            public void onPostExecute() {
                super.onPostExecute();
                Toast.makeText(GalleryOcrActivity.this, "Saved Successfully!!", Toast.LENGTH_SHORT).show();
                dismissDialog();
                if(updateGenDoc){
                    Intent intent = new Intent();
                    intent.putExtra(Constants.GENERIC_DOCUMENT, new Gson().toJson(genericDocument));
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        }.execute();

    }

    private void getImageText(int pos) {
        if (pos >= images.size()) {
            SaveOcr();
            return;
        }

        Uri uri = images.get(pos);
        Bitmap bitmap = AppUtils.Uri2Bitmap(GalleryOcrActivity.this, uri);

        final String[] text = {""};
        new BackgroundWork(GalleryOcrActivity.this) {
            @Override
            public void doInBackground() {
                super.doInBackground();
                text[0] = AppUtils.CopyTextFromBitmap(GalleryOcrActivity.this, bitmap);
                if (text[0] == null) {
                    text[0] = "";
                }
            }

            @Override
            public void onPostExecute() {
                super.onPostExecute();
                finalImagesTexts.add(text[0]);
                getImageText(pos + 1);
            }
        }.execute();
    }

    private void SetPagerData() {
        images = new ArrayList<>();
        if (getIntent() != null && getIntent().hasExtra(Constants.IMAGES_BUNDLE)) {
            Bundle bundle = getIntent().getBundleExtra(Constants.IMAGES_BUNDLE);
            images = (ArrayList<Uri>) bundle.getSerializable(Constants.IMAGES);
        }

        if (getIntent().hasExtra(Constants.GENERIC_DOCUMENT)) {
            genericDocument = new Gson().fromJson(getIntent().getStringExtra(Constants.GENERIC_DOCUMENT), GenericDocument.class);
            if (genericDocument.getOcrDocument() != null) {
                ocrDocument = genericDocument.getOcrDocument();
                fileName.setText(ocrDocument.getFileName());
            }
            updateGenDoc = true;
        }

        if (images.size() == 0) {
            Toast.makeText(this, "No image found!!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        galleryPagerAdapter = new GalleryPagerAdapter(this, images);
        viewPager2.setAdapter(galleryPagerAdapter);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d(TAG, "onPageSelected: PageNumber: " + position);
//                SetOcrText(AppUtils.Uri2Bitmap(GalleryOcrActivity.this, images.get(position)));
            }
        });
    }

    private void detectCurrOcrText() {
        int position = viewPager2.getCurrentItem();
        Bitmap bitmap = AppUtils.Uri2Bitmap(GalleryOcrActivity.this, images.get(position));
        text[0] = "";
        progressDialog.setTitle("Detecting text");
        progressDialog.setMessage("Please wait...");
        showDialog();
        new BackgroundWork(GalleryOcrActivity.this) {
            @Override
            public void doInBackground() {
                super.doInBackground();
                text[0] = AppUtils.CopyTextFromBitmap(GalleryOcrActivity.this, bitmap);
                if (text[0] == null) {
                    text[0] = "";
                }
            }

            @Override
            public void onPostExecute() {
                super.onPostExecute();
//                imageText.setText(text[0]);
                dismissDialog();
                Intent intent = new Intent(GalleryOcrActivity.this, OcrCompleteText.class);
                intent.putExtra(Constants.TEXT, text[0]);
                startActivity(intent);
            }
        }.execute();
    }


    /**
     * Initialising layout views
     */
    private void _init() {

        viewPager2 = findViewById(R.id.gallery_ocr_view_pager);
        reorder = findViewById(R.id.gallery_ocr_reorder_page);
        crop = findViewById(R.id.gallery_ocr_crop_page);
        filter = findViewById(R.id.gallery_ocr_filter_page);
        delete = findViewById(R.id.gallery_ocr_delete_page);
        rotate = findViewById(R.id.gallery_ocr_rotate_page);
        addPage = findViewById(R.id.gallery_ocr_add_page);
        back = findViewById(R.id.gallery_ocr_back);
        check = findViewById(R.id.gallery_ocr_check);
        fileName = findViewById(R.id.gallery_ocr_name);
        imageText = findViewById(R.id.gallery_ocr_image_text);

        genericDocumentModel = ViewModelProviders.of(this).get(GenericDocumentModel.class);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing Text");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.CROP_IMAGE_REQUEST_CODE && data != null && data.hasExtra(Constants.IMAGES_BUNDLE) & data.hasExtra(Constants.IMAGE_POS)) {
                Bundle imageBundle = data.getBundleExtra(Constants.IMAGES_BUNDLE);
                ArrayList<Uri> croppedUriArrayList = (ArrayList<Uri>) imageBundle.getSerializable(Constants.IMAGES);
                if (croppedUriArrayList.size() > 0) {
                    Uri currUri = croppedUriArrayList.get(0);

                    int pos = data.getIntExtra(Constants.IMAGE_POS, -1);
                    if (pos != -1) {
//                        SetOcrText(AppUtils.Uri2Bitmap(GalleryOcrActivity.this, currUri));
                        images.set(pos, currUri);
                        galleryPagerAdapter.notifyItemChanged(pos);
                    }
                }
            }

            if (requestCode == Constants.FilterRequest && data != null && data.hasExtra(Constants.ImageBitmapBytes) && data.hasExtra(Constants.IMAGE_POS)) {
                int pos = data.getIntExtra(Constants.IMAGE_POS, -1);
                byte[] bytes = data.getByteArrayExtra(Constants.ImageBitmapBytes);
                Bitmap bitmap = AppUtils.Bytes2Bitmap(bytes);
                Uri uri = AppUtils.saveBitmapToCache(getApplicationContext(), bitmap);

                if (pos != -1) {
                    images.set(pos, uri);
                    galleryPagerAdapter.notifyItemChanged(pos);
//                    SetOcrText(AppUtils.Uri2Bitmap(GalleryOcrActivity.this, uri));
                }
            }

            if (requestCode == Constants.PICK_GALLERY_IMAGE && data != null) {
                ArrayList<Uri> newImages = new ArrayList<>();
                if (data.getData() != null) {
                    newImages.add(data.getData());
                }
                int alreadySize = 0;
                if (ocrDocument != null && ocrDocument.getFilePhotos() != null) {
                    alreadySize = ocrDocument.getFilePhotos().size();
                }

                if (data.getClipData() != null && data.getClipData().getItemCount() + images.size() + alreadySize > 100) {
                    Toast.makeText(this, "Maximum 100 photos can be taken at a time!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (data.getClipData() != null) {
                    for (int i = 0; i < data.getClipData().getItemCount(); ++i) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        newImages.add(uri);
                    }
                }

                if (newImages.size() != 0) {
                    images.addAll(newImages);
                    galleryPagerAdapter.notifyItemRangeInserted(images.size() - newImages.size(), newImages.size());
                }
            }
        }
    }

    public void dismissDialog() {
        if (progressDialog != null) progressDialog.dismiss();
    }

    public void showDialog() {
        if (progressDialog != null) progressDialog.show();
    }
}