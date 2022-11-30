package com.scanner.document;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.vision.text.TextRecognizer;
import com.google.gson.Gson;
import com.scanner.document.Model.GenericDocument;

import com.scanner.document.Saving.GenericDocumentModel;
import com.scanner.document.Scanning.AccountActivity;
import com.scanner.document.Scanning.GalleryEditActivity;
import com.scanner.document.Scanning.GalleryOcrActivity;
import com.scanner.document.Scanning.PdfAction;
import com.scanner.document.Scanning.DisplayActivity;
import com.scanner.document.Scanning.GenericDocumentAdapter;
import com.scanner.document.Scanning.ScanActivity;
import com.scanner.document.Utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivityTag";
    LinearLayout emptyView;
    RelativeLayout rcvRelativeLayout;
    RecyclerView recyclerView;
    GenericDocumentAdapter genericDocumentAdapter;
    GenericDocumentModel genericDocumentModel;

    ImageView gallery1, gallery2, camera1, camera2, galleryOcr1, galleryOcr2;
    CardView profile, back;
    LinearLayout gallery3, camera3, galleryOcr3;
    ProgressDialog progressDialog;

    EditText searchView;
    private List<GenericDocument> completeList = new ArrayList<>();

    /**
     * Requesting users permission
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: Per Requested");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            Log.d(TAG, "onCreate: Per granted");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _init();

        gallery1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryPickOption();
            }
        });

        gallery2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryPickOption();
            }
        });

        gallery3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryPickOption();
            }
        });

        camera1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPickOption();
            }
        });

        camera2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPickOption();
            }
        });

        camera3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPickOption();
            }
        });

        galleryOcr1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryPickOcrOption();
            }
        });

        galleryOcr2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryPickOcrOption();
            }
        });

        galleryOcr3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GalleryPickOcrOption();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent proIntent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(proIntent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Display List activity
        DisplayFile();

        SetSearchAction();
    }

    private void SetSearchAction() {
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence query, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String query = editable.toString();
                query = query.toLowerCase().trim();
                List<GenericDocument> queryDoc = new ArrayList<>();
                for (GenericDocument genericDocument : completeList) {
                    String fileName = "";
                    if (genericDocument.getDocument() != null) {
                        fileName = genericDocument.getDocument().getFileName();
                    }
                    if (genericDocument.getOcrDocument() != null) {
                        fileName = genericDocument.getOcrDocument().getFileName();
                    }
                    fileName = fileName.toLowerCase().trim();
                    if (!fileName.trim().isEmpty() && fileName.startsWith(query)) {
                        queryDoc.add(genericDocument);
                    }
                }

                setDocumentsData(queryDoc);
            }
        });

    }

    private void GalleryPickOcrOption() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, Constants.PICK_GALLERY_OCR_IMAGE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    private void DisplayFile() {
        genericDocumentAdapter.setOnItemClickListener(new GenericDocumentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GenericDocument genericDocument) {
                Intent intent = new Intent(MainActivity.this, DisplayActivity.class);
                intent.putExtra(Constants.DOCUMENT, new Gson().toJson(genericDocument));
                startActivity(intent);
            }
        });

        genericDocumentAdapter.setShareListener(new GenericDocumentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GenericDocument genericDocument) {
                if (genericDocument.getDocument() != null) {
                    PdfAction.shareDocumentPdf(MainActivity.this, genericDocument.getDocument());
                }
                if (genericDocument.getOcrDocument() != null) {
                    PdfAction.shareOcrDocumentPdf(MainActivity.this, genericDocument.getOcrDocument());
                }
            }
        });

        genericDocumentAdapter.setMoreListener(new GenericDocumentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GenericDocument genericDocument) {
                PdfAction.openBottomSheet(MainActivity.this, genericDocument, genericDocumentModel, genericDocumentAdapter);
            }
        });
    }

    private void CameraPickOption() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
            return;
        }

        Intent scanIntent = new Intent(this, ScanActivity.class);
        startActivity(scanIntent);
    }

    private void GalleryPickOption() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            startActivityForResult(intent, Constants.PICK_GALLERY_IMAGE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.PICK_GALLERY_IMAGE) {
                if (data != null) {
                    ArrayList<Uri> uriArrayList = new ArrayList<>();
                    if (data.getData() != null) {
                        uriArrayList.add(data.getData());
                    }

                    if (data.getClipData() != null && data.getClipData().getItemCount() > 100) {
                        Toast.makeText(this, "Maximum 100 photos can be taken at a time!!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showDialog();
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
                    startActivity(intent);
                }
            } else if (requestCode == Constants.PICK_GALLERY_OCR_IMAGE && data != null) {
                ArrayList<Uri> uriArrayList = new ArrayList<>();
                if (data.getData() != null) {
                    uriArrayList.add(data.getData());
                }

                if (data.getClipData() != null && data.getClipData().getItemCount() > 100) {
                    Toast.makeText(this, "Maximum 100 photos can be taken at a time!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                showDialog();
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
                startActivity(intent);
            }
        }
    }

    /**
     * Displaying ProgressDialog if not null
     */
    private void showDialog() {
        if (progressDialog != null) {
            progressDialog.show();
        }
    }

        /**
     * Dismissing ProgressDialog if not null
     */
    private void dismissDialog() {

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * Initialising layout views
     */
    private void _init() {

        profile = findViewById(R.id.main_profile);
        back = findViewById(R.id.main_back);

        gallery1 = findViewById(R.id.main_gallery_1);
        gallery2 = findViewById(R.id.main_gallery_2);
        gallery3 = findViewById(R.id.main_gallery_3);

        galleryOcr1 = findViewById(R.id.main_ocr_scan_1);
        galleryOcr2 = findViewById(R.id.main_ocr_scan_2);
        galleryOcr3 = findViewById(R.id.main_ocr_scan_3);

        camera1 = findViewById(R.id.main_camera_1);
        camera2 = findViewById(R.id.main_camera_2);
        camera3 = findViewById(R.id.main_camera_3);

        emptyView = findViewById(R.id.main_empty_view);
        rcvRelativeLayout = findViewById(R.id.main_rcv_rl);
        genericDocumentAdapter = new GenericDocumentAdapter();
        genericDocumentModel = ViewModelProviders.of(this).get(GenericDocumentModel.class);

        searchView = findViewById(R.id.main_search_view);

        recyclerView = findViewById(R.id.main_rcv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(genericDocumentAdapter);
        recyclerView.setHasFixedSize(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        SetData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SetData();
    }

    private void SetData() {
        genericDocumentModel = ViewModelProviders.of(this).get(GenericDocumentModel.class);
        genericDocumentModel.getAllGenericDocuments().observe(this, new Observer<List<GenericDocument>>() {
            @Override
            public void onChanged(List<GenericDocument> documents) {
                setDocumentsData(documents);
                completeList = documents;
            }
        });
    }

    private void setDocumentsData(List<GenericDocument> documents) {
        if (documents.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            rcvRelativeLayout.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            rcvRelativeLayout.setVisibility(View.VISIBLE);
        }
        genericDocumentAdapter.submitList(documents);
        genericDocumentAdapter.notifyDataSetChanged();
    }
}