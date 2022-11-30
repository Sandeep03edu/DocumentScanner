package com.scanner.document.Scanning;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.scanner.document.Model.Document;
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
import java.util.Collections;

public class DisplayActivity extends AppCompatActivity {

    private static final String TAG = "DisplayActivityTag";
    ImageView backArrow;
    TextView fileName;
    RecyclerView imageGridRcv;
    ViewPager2 imageDisplayPager;
    GenericDocument genericDocument;
    Document document;
    OcrDocument ocrDocument;
    GenericDocumentModel genericDocumentModel;
    DisplayDocumentAdapter displayDocumentAdapter;
    DisplayOcrAdapter displayOcrAdapter;
    DisplayGridAdapter gridAdapter;
    RelativeLayout displayRl;

    LinearLayout reorder, crop, rotate, filter, delete;
    ImageView more, reorderIcon;
    private ProgressDialog progressDialog;
    ArrayList<String> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        _init();

        // Get Intent data
        GetData();

        // Set Data
        SetRcvData();

        // Set Document DisplayRcv Data
        SetDisplayDocumentRcvData();

        SetDisplayOcrRcvData();

        // Reorder Action
        ReorderAction();

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SetClickAction();

        SetMoreAction();
    }

    private void SetMoreAction() {
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(DisplayActivity.this, more);
                popupMenu.getMenuInflater().inflate(R.menu.display_more, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        switch (id) {
                            case R.id.add_pages:
                                // Add more pages
                                new AlertDialog.Builder(DisplayActivity.this)
                                        .setTitle("Add more pages")
                                        .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();

                                                Intent intent = new Intent(DisplayActivity.this, ScanActivity.class);
                                                intent.putExtra(Constants.GENERIC_DOCUMENT, new Gson().toJson(genericDocument));
                                                startActivityForResult(intent, Constants.UPDATE_GENERIC_DOC);
                                            }
                                        })
                                        .setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent();
                                                intent.setType("image/*");
                                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                                if (genericDocument.getDocument() != null) {
                                                    startActivityForResult(intent, Constants.PICK_GALLERY_IMAGE);
                                                } else {
                                                    startActivityForResult(intent, Constants.PICK_GALLERY_OCR_IMAGE);
                                                }

                                            }
                                        }).show();

                                return true;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });
    }

    private void SetDisplayOcrRcvData() {
        if (genericDocument.getOcrDocument() == null) return;

        displayOcrAdapter = new DisplayOcrAdapter(this, genericDocument);
        imageDisplayPager.setAdapter(displayOcrAdapter);
    }

    private void SetClickAction() {
        if (images.size() == 1) {
            reorderIcon.setColorFilter(ContextCompat.getColor(this, R.color.gray2), android.graphics.PorterDuff.Mode.SRC_IN);
            reorder.setEnabled(false);
        } else {
            reorderIcon.setColorFilter(ContextCompat.getColor(DisplayActivity.this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
            reorder.setEnabled(true);
        }

        reorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayGrid();
                Toast.makeText(DisplayActivity.this, "Long press and drag images to change order", Toast.LENGTH_LONG).show();
            }
        });

        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = imageDisplayPager.getCurrentItem();
                StartRotateWork(pos);
            }
        });

        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = imageDisplayPager.getCurrentItem();
                StartCropWork(pos);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = imageDisplayPager.getCurrentItem();
                StartDeleteProcess(pos);
            }
        });

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = imageDisplayPager.getCurrentItem();
                StartFilterProcess(pos);
            }
        });
    }

    private void SetDisplayDocumentRcvData() {
        if (document == null || document.getFilePhotos() == null) {
            if (ocrDocument == null || ocrDocument.getFilePhotos() == null) {
                return;
            }
        }
        ArrayList<String> images = new ArrayList<>();
        if (document != null) images = document.getFilePhotos();
        else {
            return;
        }
        displayDocumentAdapter = new DisplayDocumentAdapter(this, images, new DisplayDocumentAdapter.ClickAction() {
            @Override
            public void onReorder(int pos) {
                DisplayGrid();
                Toast.makeText(DisplayActivity.this, "Long press and drag images to change order", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCrop(int pos) {
                StartCropWork(pos);
            }

            @Override
            public void onRotate(int pos) {
                StartRotateWork(pos);
            }

            @Override
            public void onFilter(int pos) {
                StartFilterProcess(pos);
            }

            @Override
            public void onDelete(int pos) {
                StartDeleteProcess(pos);
            }
        });

//        imageDisplayRcv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageDisplayPager.setAdapter(displayDocumentAdapter);
    }

    private void StartDeleteProcess(int pos) {
        new AlertDialog.Builder(DisplayActivity.this)
                .setTitle("Are you sure?")
                .setMessage("Deleting image can't be undone")
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();

                        if (displayDocumentAdapter != null)
                            displayDocumentAdapter.notifyItemRemoved(pos);
                        if (displayOcrAdapter != null)
                            displayOcrAdapter.notifyItemRemoved(pos);
                        if (gridAdapter != null) gridAdapter.notifyItemRemoved(pos);

                        if (document != null) {
                            document.getFilePhotos().remove(pos);
                            genericDocument.setDocument(document);
                        }
                        if (ocrDocument != null) {
                            ocrDocument.getFilePhotos().remove(pos);
                            ocrDocument.getFileTexts().remove(pos);
                            genericDocument.setOcrDocument(ocrDocument);
                        }
                        genericDocumentModel.update(genericDocument);

                        if (document != null) {
                            if (document.getFilePhotos().size() == 0) {
                                genericDocumentModel.delete(genericDocument);
                                finish();
                            }
                            if(document.getFilePhotos().size()==1){
                                reorderIcon.setColorFilter(ContextCompat.getColor(DisplayActivity.this, R.color.gray2), android.graphics.PorterDuff.Mode.SRC_IN);
                                reorder.setEnabled(false);
                            }
                        }
                        if (ocrDocument != null) {
                            if (ocrDocument.getFilePhotos().size() == 0) {
                                genericDocumentModel.delete(genericDocument);
                                finish();
                            }
                            if(ocrDocument.getFilePhotos().size()==1){
                                reorderIcon.setColorFilter(ContextCompat.getColor(DisplayActivity.this, R.color.gray2), android.graphics.PorterDuff.Mode.SRC_IN);
                                reorder.setEnabled(false);
                            }
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

    private void StartFilterProcess(int pos) {
        Bitmap bitmap = null;
        if (document != null) {
            bitmap = AppUtils.Uri2Bitmap(DisplayActivity.this, Uri.parse(document.getFilePhotos().get(pos)));
        }
        if (ocrDocument != null) {
            bitmap = AppUtils.Uri2Bitmap(DisplayActivity.this, Uri.parse(ocrDocument.getFilePhotos().get(pos)));
        }
        if (bitmap == null) return;

        bitmap = ImageResizer.reduceBitmapSize(bitmap, 240000);
        Intent intent = new Intent(DisplayActivity.this, FilterImage.class);
        intent.putExtra(Constants.IMAGE_POS, pos);
        byte[] bytes = AppUtils.Bitmap2Bytes(bitmap);
        intent.putExtra(Constants.ImageBitmapBytes, bytes);
        startActivityForResult(intent, Constants.FilterRequest);
    }

    private void StartRotateWork(int pos) {
        Bitmap bitmap = null;
        if (document != null) {
            bitmap = AppUtils.Uri2Bitmap(DisplayActivity.this, Uri.parse(document.getFilePhotos().get(pos)));
        }
        if (ocrDocument != null) {
            bitmap = AppUtils.Uri2Bitmap(DisplayActivity.this, Uri.parse(ocrDocument.getFilePhotos().get(pos)));
        }
        if (bitmap == null) return;

        bitmap = AppUtils.rotateBitmap(bitmap, 90);
        Uri rotatedUri = AppUtils.saveBitmapToCache(DisplayActivity.this, bitmap);
        if (document != null) {
            document.getFilePhotos().set(pos, String.valueOf(rotatedUri));
            genericDocument.setDocument(document);
        }
        if (ocrDocument != null) {
            ocrDocument.getFilePhotos().set(pos, String.valueOf(rotatedUri));
            Bitmap finalBitmap = bitmap;
            new BackgroundWork(this) {
                @Override
                public void doInBackground() {
                    super.doInBackground();
                    ocrDocument.getFileTexts().set(pos, AppUtils.CopyTextFromBitmap(DisplayActivity.this, finalBitmap));
                }

                @Override
                public void onPostExecute() {
                    super.onPostExecute();
                    genericDocument.setOcrDocument(ocrDocument);
                    genericDocumentModel.update(genericDocument);

                    if (displayOcrAdapter != null) displayOcrAdapter.notifyItemChanged(pos);
                    if (displayDocumentAdapter != null)
                        displayDocumentAdapter.notifyItemChanged(pos);
                    if (gridAdapter != null) gridAdapter.notifyItemChanged(pos);
                }
            }.execute();
        }
    }

    private void StartCropWork(int pos) {
        Bitmap bitmap = null;
        if (document != null) {
            bitmap = AppUtils.Uri2Bitmap(DisplayActivity.this, Uri.parse(document.getFilePhotos().get(pos)));
        }
        if (ocrDocument != null) {
            bitmap = AppUtils.Uri2Bitmap(DisplayActivity.this, Uri.parse(ocrDocument.getFilePhotos().get(pos)));
        }

        if (bitmap == null) return;

        // Sending activity for cropping image
        Intent cropImagesIntent = new Intent(getApplicationContext(), CropImagesActivity.class);
        Bundle imagesBundle = new Bundle();

        ArrayList<Uri> uriArrayList = new ArrayList<>();
        uriArrayList.add(AppUtils.saveBitmapToCache(DisplayActivity.this, bitmap));
        imagesBundle.putSerializable(Constants.IMAGES, uriArrayList);
        cropImagesIntent.putExtra(Constants.IMAGES_BUNDLE, imagesBundle);
        cropImagesIntent.putExtra(Constants.DISABLE_ASPECT_CROP, true);
        cropImagesIntent.putExtra(Constants.IMAGE_POS, pos);
        startActivityForResult(cropImagesIntent, Constants.CROP_IMAGE_REQUEST_CODE);
    }

    private void SetRcvData() {
        if (document == null || document.getFilePhotos() == null) {
            if (ocrDocument == null || ocrDocument.getFilePhotos() == null) {
                return;
            }
        }

        String name = "";
        if (document != null) name = document.getFileName();
        if (ocrDocument != null) name = ocrDocument.getFileName();

        fileName.setText(name);

        if (document != null) images = document.getFilePhotos();
        if (ocrDocument != null) images = ocrDocument.getFilePhotos();

        Log.d(TAG, "SetRcvData: Images Size: " + images.size());
        gridAdapter = new DisplayGridAdapter(this, images, new DisplayGridAdapter.ClickAction() {
            @Override
            public void onItemClick(int pos) {
                imageDisplayPager.setCurrentItem(pos, false);
                DisplayDisplay();
            }
        });

        imageGridRcv.setAdapter(gridAdapter);
        imageGridRcv.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void swap(int pos1, int pos2) {
        if (gridAdapter != null)
            gridAdapter.notifyItemMoved(pos1, pos2);
        if (displayOcrAdapter != null)
            displayOcrAdapter.notifyItemMoved(pos1, pos2);
        if (displayDocumentAdapter != null)
            displayDocumentAdapter.notifyItemMoved(pos1, pos2);
    }

    private void GetData() {
        if (getIntent() != null && getIntent().hasExtra(Constants.DOCUMENT)) {
            String gson = getIntent().getStringExtra(Constants.DOCUMENT);
            genericDocument = new Gson().fromJson(gson, GenericDocument.class);
            document = genericDocument.getDocument();
            ocrDocument = genericDocument.getOcrDocument();
        } else {
            finish();
        }

        if (getIntent().hasExtra(Constants.REORDER)) {
            boolean reorder = getIntent().getBooleanExtra(Constants.REORDER, false);
            if (reorder) {
                Toast.makeText(this, "Long press and drag images to change order", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void ReorderAction() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(imageGridRcv);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPos = viewHolder.getAdapterPosition();
            int toPos = target.getAdapterPosition();
            Log.d(TAG, "onMove: " + fromPos + "->" + toPos);

            if (document != null) {
                Collections.swap(document.getFilePhotos(), fromPos, toPos);
//                Objects.requireNonNull(recyclerView.getAdapter()).notifyItemMoved(fromPos, toPos);
                genericDocument.setDocument(document);
                Log.d(TAG, "onMove: Moved....");
                swap(fromPos, toPos);
            }
            if (ocrDocument != null) {
                Collections.swap(ocrDocument.getFilePhotos(), fromPos, toPos);
                Collections.swap(ocrDocument.getFileTexts(), fromPos, toPos);
//                Objects.requireNonNull(recyclerView.getAdapter()).notifyItemMoved(fromPos, toPos);
                genericDocument.setOcrDocument(ocrDocument);
                swap(fromPos, toPos);
            }
            genericDocumentModel.update(genericDocument);

            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };

    private void _init() {
        backArrow = findViewById(R.id.display_back_arrow);
        fileName = findViewById(R.id.display_file_name);

        delete = findViewById(R.id.display_delete_page);
        reorder = findViewById(R.id.display_reorder_page);
        rotate = findViewById(R.id.display_rotate_page);
        crop = findViewById(R.id.display_crop_page);
        filter = findViewById(R.id.display_filter_page);
        more = findViewById(R.id.display_more);

        reorderIcon = findViewById(R.id.display_reorder_icon);

        imageGridRcv = findViewById(R.id.display_rcv);
        imageGridRcv.setLayoutManager(new GridLayoutManager(this, 2));

        displayRl = findViewById(R.id.display_display_rl);
        imageDisplayPager = findViewById(R.id.display_display_rcv);

        genericDocumentModel = ViewModelProviders.of(this).get(GenericDocumentModel.class);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);


        DisplayGrid();
    }

    private void DisplayGrid() {
        imageGridRcv.setVisibility(View.VISIBLE);
        displayRl.setVisibility(View.GONE);
    }

    private void DisplayDisplay() {
        imageGridRcv.setVisibility(View.GONE);
        displayRl.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (displayRl.getVisibility() == View.VISIBLE) {
            DisplayGrid();
        } else {
            super.onBackPressed();
        }
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
                    String currStr = String.valueOf(currUri);
                    int pos = data.getIntExtra(Constants.IMAGE_POS, -1);

                    if (pos != -1) {
                        if (document != null) {
                            document.getFilePhotos().set(pos, currStr);
                            genericDocument.setDocument(document);
                        }
                        if (ocrDocument != null) {
                            ocrDocument.getFilePhotos().set(pos, currStr);

                            new BackgroundWork(this) {
                                @Override
                                public void doInBackground() {
                                    super.doInBackground();
                                    ocrDocument.getFileTexts().set(pos, AppUtils.CopyTextFromBitmap(DisplayActivity.this, AppUtils.Uri2Bitmap(DisplayActivity.this, currUri)));
                                }

                                @Override
                                public void onPostExecute() {
                                    super.onPostExecute();
                                    genericDocument.setOcrDocument(ocrDocument);
                                    genericDocumentModel.update(genericDocument);

                                    if (displayOcrAdapter != null)
                                        displayOcrAdapter.notifyItemChanged(pos);
                                    if (displayDocumentAdapter != null)
                                        displayDocumentAdapter.notifyItemChanged(pos);
                                    if (gridAdapter != null) gridAdapter.notifyItemChanged(pos);
                                }
                            }.execute();
                        }
                    }
                }
            }

            if (requestCode == Constants.FilterRequest && data != null && data.hasExtra(Constants.ImageBitmapBytes) && data.hasExtra(Constants.IMAGE_POS)) {
                int pos = data.getIntExtra(Constants.IMAGE_POS, -1);
                byte[] bytes = data.getByteArrayExtra(Constants.ImageBitmapBytes);
                Bitmap bitmap = AppUtils.Bytes2Bitmap(bytes);
                Uri uri = AppUtils.saveBitmapToCache(getApplicationContext(), bitmap);
                String currUri = String.valueOf(uri);

                if (pos != -1) {
                    if (document != null) {
                        document.getFilePhotos().set(pos, currUri);
                        genericDocument.setDocument(document);
                    }
                    if (ocrDocument != null) {
                        ocrDocument.getFilePhotos().set(pos, currUri);

                        new BackgroundWork(this) {
                            @Override
                            public void doInBackground() {
                                super.doInBackground();
                                ocrDocument.getFileTexts().set(pos, AppUtils.CopyTextFromBitmap(DisplayActivity.this, bitmap));
                            }

                            @Override
                            public void onPostExecute() {
                                super.onPostExecute();
                                genericDocument.setOcrDocument(ocrDocument);
                                genericDocumentModel.update(genericDocument);

                                if (displayOcrAdapter != null)
                                    displayOcrAdapter.notifyItemChanged(pos);
                                if (displayDocumentAdapter != null)
                                    displayDocumentAdapter.notifyItemChanged(pos);
                                if (gridAdapter != null) gridAdapter.notifyItemChanged(pos);
                            }
                        }.execute();

                    }
                }
            }

            if (requestCode == Constants.UPDATE_GENERIC_DOC) {
                Log.d(TAG, "onActivityResult: Updating generic Doc");
                if (data != null) {
                    genericDocument = new Gson().fromJson(data.getStringExtra(Constants.GENERIC_DOCUMENT), GenericDocument.class);
                    Intent intent = new Intent(DisplayActivity.this, DisplayActivity.class);
                    intent.putExtra(Constants.DOCUMENT, new Gson().toJson(genericDocument));
                    startActivity(intent);
                }

                finish();
            }

            if (requestCode == Constants.PICK_GALLERY_IMAGE) {
                if (data != null) {
                    ArrayList<Uri> uriArrayList = new ArrayList<>();
                    if (data.getData() != null) {
                        uriArrayList.add(data.getData());
                    }

                    if (data.getClipData() != null && data.getClipData().getItemCount() + genericDocument.getDocument().getFilePhotos().size() > 100) {
                        Toast.makeText(this, "Maximum 100 photos can be taken at a time!!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ShowDialog();
                    if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); ++i) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            uriArrayList.add(uri);
                        }
                    }
                    dismissDialog();
                    Intent intent = new Intent(this, GalleryEditActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.IMAGES, uriArrayList);
                    intent.putExtra(Constants.IMAGES_BUNDLE, bundle);
                    intent.putExtra(Constants.GENERIC_DOCUMENT, new Gson().toJson(genericDocument));
                    startActivityForResult(intent, Constants.UPDATE_GENERIC_DOC);
                }
            } else if (requestCode == Constants.PICK_GALLERY_OCR_IMAGE) {
                if (data != null) {
                    ArrayList<Uri> uriArrayList = new ArrayList<>();
                    if (data.getData() != null) {
                        uriArrayList.add(data.getData());
                    }

                    if (data.getClipData() != null && data.getClipData().getItemCount() > 100) {
                        Toast.makeText(this, "Maximum 100 photos can be taken at a time!!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ShowDialog();
                    if (data.getClipData() != null) {
                        for (int i = 0; i < data.getClipData().getItemCount(); ++i) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            uriArrayList.add(uri);
                        }
                    }
                    dismissDialog();

                    Intent intent = new Intent(this, GalleryOcrActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Constants.IMAGES, uriArrayList);
                    intent.putExtra(Constants.IMAGES_BUNDLE, bundle);
                    intent.putExtra(Constants.GENERIC_DOCUMENT, new Gson().toJson(genericDocument));
                    startActivityForResult(intent, Constants.UPDATE_GENERIC_DOC);
                }
            }
        }
    }

    private void ShowDialog() {
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

    private void dismissDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}