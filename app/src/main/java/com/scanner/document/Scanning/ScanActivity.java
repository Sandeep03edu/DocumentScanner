package com.scanner.document.Scanning;

import static java.lang.Math.abs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
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
import com.scanner.document.Model.Document;
import com.scanner.document.Model.GenericDocument;
import com.scanner.document.Model.OcrDocument;
import com.scanner.document.R;

import com.scanner.document.Saving.GenericDocumentModel;
import com.scanner.document.Utils.AppUtils;
import com.scanner.document.Utils.BackgroundWork;
import com.scanner.document.Utils.Constants;
import com.scanner.document.Utils.ImageResizer;
import com.scanner.document.Utils.LayoutUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.List;

public class ScanActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "ScanActivityTag";
    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    int activeBackCamera = CameraBridgeViewBase.CAMERA_ID_BACK;
    Mat mat;
    Mat frame = null;
    ImageView clickImage, lastImage, homeImage;

    TextView ocrScan, docScan, cardScan;
    int scanType;
    Rect rect = null;
    GenericDocument genericDocument;
    boolean updateGenDoc = false;


    Document document = null;
    GenericDocumentModel genericDocumentModel;
    private GenericDocument ocrGenericDocument= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        _init();

        _initBaseLoader();

        ScanTypeClickAction();

        ClickImageAction();

        SetLastImageClickAction();

        cameraBridgeViewBase.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "onTouch: ");
//                SetBoundary();
                return false;
            }
        });

    }

    private void SetBoundary() {
        Log.d(TAG, "SetBoundary: ");
        if (mat != null && mat.cols() > 0 && mat.rows() > 0) {
            Log.d(TAG, "SetBoundary: Mat notNull");
            Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(mat, bitmap);
            bitmap = AppUtils.rotateBitmap(bitmap, 90);
            bitmap = ImageResizer.reduceBitmapSize(bitmap, 240000);

            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

            // Multiple object detection in static images
            ObjectDetectorOptions options =
                    new ObjectDetectorOptions.Builder()
                            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                            .enableMultipleObjects()
                            .enableClassification()  // Optional
                            .build();

            ObjectDetector objectDetector = ObjectDetection.getClient(options);

            Bitmap finalBitmap = bitmap;
            objectDetector.process(inputImage)
                    .addOnSuccessListener(
                            new OnSuccessListener<List<DetectedObject>>() {
                                @Override
                                public void onSuccess(List<DetectedObject> detectedObjects) {
                                    int left = Integer.MAX_VALUE, top = Integer.MAX_VALUE, bottom = Integer.MIN_VALUE, right = Integer.MIN_VALUE;

                                    for (DetectedObject detectedObject : detectedObjects) {
                                        Rect box = detectedObject.getBoundingBox();

                                        box = rotateRect(box);

                                        left = Math.min(left, box.left);
                                        top = Math.min(top, box.top);
                                        bottom = Math.max(bottom, box.bottom);
                                        right = Math.max(right, box.right);
                                    }

                                    if (left != Integer.MAX_VALUE && top != Integer.MAX_VALUE && bottom != Integer.MIN_VALUE && right != Integer.MIN_VALUE) {
                                        if (rect == null) {
                                            rect = new Rect();
                                        }
                                        rect.set(left, top, right, bottom);
                                        Log.d(TAG, "onSuccess: Setting boundary");



                                        /*
                                        Mat objMat = new Mat();
                                        Bitmap objBmp = Bitmap.createBitmap(finalBitmap, rect.left, rect.top,rect.right-rect.left, rect.bottom- rect.top);
                                        
                                        if(objBmp==null){
                                            Log.d(TAG, "onSuccess: Null obj bitmap");
                                            return;
                                        }

                                        Utils.bitmapToMat(objBmp, objMat);

                                        if(objMat.rows() <= 0 || objMat.cols() <= 0){
                                            Log.d(TAG, "onSuccess: Invalid obj Mat");
                                            return;
                                        }

                                        Imgproc.rectangle(
                                                objMat,
//                                                new Point(rect.left, rect.top),
//                                                new Point(rect.right, rect.bottom ),
                                                new Point(0,0),
                                                new Point(100, 100),
                                                new Scalar(255, 0, 255),
                                                30
                                        );

                                         */
                                    }
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: Error fetching obj: " + e.getMessage());
                                    // Task failed with an exception
                                    // ...
                                }
                            });

        }
    }

    private Rect rotateRect(Rect box) {
        Rect rect;

        int cx = box.centerX(), cy = box.centerY();
        int l = box.left, r = box.right, t = box.top, b = box.bottom;

        int w = b - t, h = r - l;

        int t1 = cy - h / 2, b1 = cy + h / 2;
        int l1 = cx - w / 2, r1 = cx + w / 2;

        rect = new Rect(l1, t1, r1, b1);

        return rect;
    }

    private void SetLastImageClickAction() {
        lastImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastImage.getVisibility() == View.VISIBLE) {
                    GenericDocument genericDocument = new GenericDocument();
                    genericDocument.setDocument(document);
                    Intent intent = new Intent(ScanActivity.this, DisplayActivity.class);
                    intent.putExtra(Constants.DOCUMENT, new Gson().toJson(genericDocument));
                    startActivity(intent);
                }
            }
        });
    }

    private void ScanTypeClickAction() {
        ocrScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanType = 0;
                setOption();
            }
        });

        docScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanType = 1;
                setOption();
            }
        });

        cardScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanType = 2;
                setOption();
            }
        });
    }

    private void ClickImageAction() {
        clickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (scanType) {
                    case 0:
                        // OCR Scan
                        SendingImageForOcr();
                        break;
                    case 1:
                        // Document Scan
                    case 2:
                        // Card Scan
                        SendImageForConfiguration();
                        break;
                }
            }
        });
    }

    /**
     * Sending image to ConfigureScan.java for configuring changes
     */
    private void SendImageForConfiguration() {
        if (mat != null && mat.cols() > 0 && mat.rows() > 0) {
            Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(mat, bitmap);
            bitmap = AppUtils.rotateBitmap(bitmap, 90);
            bitmap = ImageResizer.reduceBitmapSize(bitmap, 240000);

            byte[] bytes = AppUtils.Bitmap2Bytes(bitmap);

            Intent configureIntent = new Intent(this, ConfigureScan.class);
            configureIntent.putExtra(Constants.ImageBitmapBytes, bytes);
            String tempName = "Scan_" + document.getTimeStamp();
            if(!document.getFileName().equalsIgnoreCase(tempName)) {
                configureIntent.putExtra(Constants.FolderName, document.getFileName());
            }
            startActivityForResult(configureIntent, Constants.ConfigureRequest);
        }
    }

    /**
     * Sending image to OcrResultActivity for Ocr scanning
     */
    private void SendingImageForOcr() {
        if (mat != null && mat.cols() > 0 && mat.rows() > 0) {
            Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(mat, bitmap);
            bitmap = AppUtils.rotateBitmap(bitmap, 90);

            Uri imageUri = AppUtils.saveBitmapToCache(this, bitmap);

            // Moving to OCR screen
            Intent ocrResIntent = new Intent(this, OcrResultActivity.class);
            ocrResIntent.putExtra(Constants.ImageUri, String.valueOf(imageUri));
            startActivityForResult(ocrResIntent, Constants.OcrRequest);
        }
    }

    /**
     * Initialising baseLoaderCallBack
     */
    private void _initBaseLoader() {
        // Initializing BaseLoaderCallBack
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);
                switch (status) {
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        mat = new Mat();
                        frame = null;
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
    }

    private void setOption() {
        TextView target;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int fullHeight = screenWidth;
        int cardHeight = (screenWidth) / 16;

        switch (scanType) {
            case 0:
                target = ocrScan;
                cameraBridgeViewBase.setMaxFrameSize(screenWidth, fullHeight);
                RestartCamera();
                break;
            case 1:
                target = docScan;
                cameraBridgeViewBase.setMaxFrameSize(screenWidth, fullHeight);
                RestartCamera();
                break;
            case 2:
                target = cardScan;
                cameraBridgeViewBase.setMaxFrameSize(screenWidth, cardHeight);
                RestartCamera();
                break;
            default:
                target = null;
        }
        if (target == null) {
            return;
        }

        LayoutUtils.setTextColor(this, new TextView[]{ocrScan, docScan, cardScan}, target, R.color.card_bkg, R.color.white);
    }

    private void RestartCamera() {
        cameraBridgeViewBase.disableView();
        cameraBridgeViewBase.enableView();
    }

    /**
     * Initialing views
     */
    private void _init() {
        cameraBridgeViewBase = findViewById(R.id.scan_camera_view);
        clickImage = findViewById(R.id.scan_click_image);
        lastImage = findViewById(R.id.scan_last_image);
        lastImage.setVisibility(View.GONE);
        homeImage = findViewById(R.id.scan_home);

        ocrScan = findViewById(R.id.scan_ocr_option);
        docScan = findViewById(R.id.scan_document_option);
        cardScan = findViewById(R.id.scan_card_option);

        genericDocument = new GenericDocument();
        ocrGenericDocument= null;
        genericDocumentModel = ViewModelProviders.of(this).get(GenericDocumentModel.class);

        if (ContextCompat.checkSelfPermission((ScanActivity.this), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ScanActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
        } else {
            initializeCamera((CameraBridgeViewBase) cameraBridgeViewBase, activeBackCamera);
        }

        scanType = 1;

        setOption();

        document = new Document();

        homeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        if(getIntent().hasExtra(Constants.GENERIC_DOCUMENT)){
            genericDocument = new Gson().fromJson(getIntent().getStringExtra(Constants.GENERIC_DOCUMENT), GenericDocument.class);
            document = genericDocument.getDocument();
            updateGenDoc = true;

            if(document!=null){
                scanType = 1;
                setOption();

                docScan.setVisibility(View.VISIBLE);
                ocrScan.setVisibility(View.INVISIBLE);
                cardScan.setVisibility(View.INVISIBLE);
            }
            else{
                scanType = 0;
                setOption();

                ocrGenericDocument = genericDocument;

                docScan.setVisibility(View.INVISIBLE);
                ocrScan.setVisibility(View.VISIBLE);
                cardScan.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        boolean hasOcr = ocrGenericDocument!=null;
        boolean hasDoc = document!=null && document.getFilePhotos()!=null && document.getFilePhotos().size()!=0;

        if(!hasDoc && !hasOcr){
            finish();
            return;
        }

        String mess1 ="",mess2 = "Discard all changes", mess3 = "No";

        if(hasOcr){
            mess1 = "Save Ocr";
            if(hasDoc){
                mess1 += " & discard changes";
            }
        }


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScanActivity.this)
                .setTitle("Discard this scan?")
                .setMessage("If you close app now, the scan will be discarded");

        if(!mess1.trim().isEmpty()){
            alertDialog.setPositiveButton(mess1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(ocrGenericDocument!=null) {
                        if(!updateGenDoc) {
                            genericDocumentModel.insert(ocrGenericDocument);
                        }
                        else{
                            genericDocumentModel.update(ocrGenericDocument);
                            Intent intent = new Intent();
                            intent.putExtra(Constants.GENERIC_DOCUMENT, new Gson().toJson(genericDocument));
                            setResult(RESULT_OK, intent);
                        }
                    }
                    dialogInterface.dismiss();
                    finish();
                }
            });
        }


                alertDialog.setNegativeButton(mess2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                })
                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });


        alertDialog.show();
    }

    /**
     * Initialising CameraView
     */
    private void initializeCamera(CameraBridgeViewBase javaCameraView, int activeCamera) {
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCameraIndex(activeCamera);
        javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }


    /**
     * @param requestCode  - 0 for camera
     * @param permissions  - Camera permission
     * @param grantResults Initialising CameraView after getting camera permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeCamera((CameraBridgeViewBase) cameraBridgeViewBase, activeBackCamera);
            } else {
                Toast.makeText(this, "Permission not granted!!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.ConfigureRequest) {
                if (data != null) {
                    if (data.hasExtra(Constants.ImageUri)) {
                        String newImageUri = data.getStringExtra(Constants.ImageUri);
                        document.insertImage(newImageUri);
                        lastImage.setImageURI(Uri.parse(newImageUri));
                        lastImage.setVisibility(View.VISIBLE);
                    }

                    if (data.hasExtra(Constants.FolderName)) {
                        String folderName = data.getStringExtra(Constants.FolderName);
                        document.setFileName(folderName);
                    }

                    if (data.hasExtra(Constants.ConfigureAction)) {
                        int action = data.getIntExtra(Constants.ConfigureAction, -1);

                        switch (action) {
                            case Constants.ConfigureDeletePdf:
                                document = new Document();
                                lastImage.setVisibility(View.INVISIBLE);
                                break;
                            case Constants.ConfigureSavePdf:
                                genericDocument.setDocument(document);
                                if(updateGenDoc){
                                    genericDocumentModel.update(genericDocument);
                                }
                                else {
                                    genericDocumentModel.insert(genericDocument);
                                }
                                PdfAction.document2Pdf(this, document);
                                genericDocument.setDocument(document);
                                genericDocumentModel.update(genericDocument);

                                if(updateGenDoc){
                                    Intent intent = new Intent();
                                    intent.putExtra(Constants.GENERIC_DOCUMENT, new Gson().toJson(genericDocument));
                                    setResult(RESULT_OK, intent);
                                }
                                finish();
                                break;
                            case Constants.ConfigureReorderPdf:
                                GenericDocument genericDocument1 = new GenericDocument();
                                genericDocument1.setDocument(document);
                                Intent intent = new Intent(this, DisplayActivity.class);
                                intent.putExtra(Constants.DOCUMENT, new Gson().toJson(genericDocument1));
                                intent.putExtra(Constants.REORDER, true);
                                startActivity(intent);
                                break;
                        }
                    }

                }
            }

            if (requestCode == Constants.OcrRequest && data != null) {

                if (data.hasExtra(Constants.ImageUri)) {
                    String newImageUri = data.getStringExtra(Constants.ImageUri);
                    boolean addFiles = false;
                    OcrDocument ocrDocument = new OcrDocument();
                    if(ocrGenericDocument==null) {
                        ocrGenericDocument = new GenericDocument();
                    }
                    else{
                        ocrDocument = ocrGenericDocument.getOcrDocument();
                        addFiles = true;
                    }
                    ocrDocument.getFilePhotos().add(newImageUri);

                    boolean finalAddFiles = addFiles;
                    OcrDocument finalOcrDocument = ocrDocument;

                    new BackgroundWork(this){
                        @Override
                        public void doInBackground() {
                            super.doInBackground();
                            finalOcrDocument.getFileTexts().add(AppUtils.CopyTextFromBitmap(ScanActivity.this, AppUtils.Uri2Bitmap(ScanActivity.this, Uri.parse(newImageUri))));
                        }

                        @Override
                        public void onPostExecute() {
                            super.onPostExecute();
                            ocrGenericDocument.setOcrDocument(finalOcrDocument);
                            if(finalAddFiles){
//                                genericDocumentModel.update(ocrGenericDocument);
                            }
                            else{
//                                genericDocumentModel.insert(ocrGenericDocument);
                            }
                            int action = data.getIntExtra(Constants.OCRAction, -1);
                            switch (action) {
                                case Constants.OCR_RETAKE:
                                case Constants.OCR_SCAN_MORE:
                                    if(!updateGenDoc) {
                                        scanType = 0;
                                        setOption();
                                    }
                                    break;
                                case Constants.OCR_SAVE_RESULT:
                                    if(ocrGenericDocument!=null) {
                                        if(!updateGenDoc) {
                                            genericDocumentModel.insert(ocrGenericDocument);
                                        }
                                        else{
                                            genericDocumentModel.update(ocrGenericDocument);
                                            Intent intent = new Intent();
                                            intent.putExtra(Constants.GENERIC_DOCUMENT, new Gson().toJson(genericDocument));
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }
                                    }
                                    ocrGenericDocument = null;
                                    if(!updateGenDoc) {
                                        scanType = 1;
                                        setOption();
                                    }
                                    Toast.makeText(ScanActivity.this, "OCR Saved Successfully!!", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }.execute();

                }
                else if (data.hasExtra(Constants.OCRAction)) {
                    int action = data.getIntExtra(Constants.OCRAction, -1);

                    switch (action) {
                        case Constants.OCR_RETAKE:
                        case Constants.OCR_SCAN_MORE:
                            if(!updateGenDoc) {
                                scanType = 0;
                                setOption();
                            }
                            break;
                        case Constants.OCR_SAVE_RESULT:
                            ocrGenericDocument = null;
                            if(!updateGenDoc) {
                                scanType = 1;
                                setOption();
                            }
                            Toast.makeText(ScanActivity.this, "OCR Saved Successfully!!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }

            }
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mat = new Mat(width, height, CvType.CV_8UC4);
        frame = new Mat(width, height, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        if (mat != null) {
            mat.release();
        }
        if (frame != null) {
            frame.release();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "_init: Not init");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseLoaderCallback);
        } else if (baseLoaderCallback != null) {
            Log.d(TAG, "onStart: Baseloader called");
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "_init: Not init");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, baseLoaderCallback);
        } else if (baseLoaderCallback != null) {
            Log.d(TAG, "onResume: Baseloader called");
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
        if (mat != null) {
            mat.release();
        }
        if (frame != null) {
            frame.release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
        if (mat != null) {
            mat.release();
        }
        if (frame != null) {
            frame.release();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        frame = inputFrame.rgba();
        mat = frame;
        return frame;
    }
}