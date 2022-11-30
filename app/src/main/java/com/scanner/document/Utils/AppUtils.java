package com.scanner.document.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.scanner.document.R;
import com.scanner.document.Scanning.PdfAction;

import org.checkerframework.checker.units.qual.A;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AppUtils {

    private static final String TAG = "AppUtilsTag";

    /**
     * Rotating a Bitmap
     *
     * @param orgBitmap - Given Bitmap
     * @param degree    - Degree of rotation
     * @return - Rotated bitmap
     */
    public static Bitmap rotateBitmap(Bitmap orgBitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(orgBitmap, orgBitmap.getWidth(), orgBitmap.getHeight(), true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }

    /**
     * Method to convert Bitmap to Uri by saving it to "Cache" directory
     *
     * @param context - Context - Activity context
     * @param bitmap  - Bitmap - imageBitmap
     * @return - Uri - Bitmap converted uri
     */
    public static Uri saveBitmapToCache(Context context, Bitmap bitmap) {
        bitmap = ImageResizer.reduceBitmapSize(bitmap, 240000);
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("image", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".png";
        File mypath = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            return Uri.fromFile(mypath);
        } catch (Exception e) {
            Log.e("SAVE_IMAGE", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Saving Bitmap to a directory "dirName"
     *
     * @param context - Activity context
     * @param bitmap  - ImageBitmap
     * @param dirName - Folder name
     * @return - Image Uri
     */
    public Uri saveImageToFolder(Context context, Bitmap bitmap, String dirName) {
        String resultPath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) +
                File.separator +
                context.getString(R.string.app_name) +
                File.separator +
                dirName +
                File.separator +
                System.currentTimeMillis() +
                ".jpg";

        new File(resultPath).getParentFile().mkdir();

        Uri uri;

        if (Build.VERSION.SDK_INT < 29) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "Photo");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Edited");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            values.put("_data", resultPath);

            ContentResolver cr = context.getContentResolver();
            uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            try {
                OutputStream fileOutputStream = new FileOutputStream(resultPath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }

        } else {
            OutputStream fos = null;
            File file = new File(resultPath);

            final String relativeLocation = Environment.DIRECTORY_PICTURES;
            final ContentValues contentValues = new ContentValues();

            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation + File.separator + context.getString(R.string.app_name) + File.separator + dirName);
            contentValues.put(MediaStore.MediaColumns.TITLE, "Photo");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
            contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
            contentValues.put(MediaStore.MediaColumns.BUCKET_ID, file.toString().toLowerCase(Locale.US).hashCode());
            contentValues.put(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(Locale.US));

            final ContentResolver resolver = context.getContentResolver();
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, contentValues);

            try {
                fos = resolver.openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return uri;
    }

    /**
     * Method to convert Uri to Bitmap
     *
     * @param context - Activity Context
     * @param uri     - Image Uri
     * @return - Image Bitmap
     */
    public static Bitmap Uri2Bitmap(Context context, Uri uri) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            bitmap = ImageResizer.reduceBitmapSize(bitmap, 240000);
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converting milliseconds to date
     *
     * @param milliSec - Given time in milliseconds
     * @return - Formatted string of date
     */
    public static String formattedDate(long milliSec) {
        DateFormat simple = new SimpleDateFormat("dd MMM yyyy");
        Date result = new Date(milliSec);
        return simple.format(result);
    }

    public static double fileSizekiloBytes(String uri, Context context) {
        /*
        Zero size only
         */
//        File file = new File(uri);
//        double fileSize = file.length();
//        fileSize /= 1024;

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(uri), "r");
            long fileSize = fileDescriptor.getLength();
            fileSize /= 1024;
            return fileSize;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String[] projection = new String[]{MediaStore.Images.Media.DATA, OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
        Cursor cursor = context.getContentResolver().query(Uri.parse(uri), projection, null, null, null);
        double size = 0;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                // Try extracting content size
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex);
                    size /= 1024;
                }
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return size;
    }

    public static byte[] Bitmap2Bytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap Bytes2Bitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static ArrayList<String> copyImages(Context context, ArrayList<Uri> uriArrayList) {
        ArrayList<String> copyList = new ArrayList<>();
        for (Uri uri : uriArrayList) {
            Bitmap bitmap = Uri2Bitmap(context, uri);
            Uri newUri = saveBitmapToCache(context, bitmap);
            copyList.add(String.valueOf(newUri));
        }

        return copyList;
    }

    public static String CopyTextFromBitmap(Context context, Bitmap bitmap) {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        if(!textRecognizer.isOperational()){
            Log.d(TAG, "CopyTextFromBitmap: Not possible");
            textRecognizer.release();
            if(!textRecognizer.isOperational())
                return null;
        }
//        TextRecognizer textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
//
//        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
//        textRecognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
//                    @Override
//                    public void onSuccess(Text text) {
//                        if(text==null) return;
//                        String te = text.getText();
//                        Log.d(TAG, "onSuccess: text: " + te);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d(TAG, "onFailure: Error: " +e.getMessage());
//                    }
//                });

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<TextBlock> sparseArray = textRecognizer.detect(frame);
        StringBuilder stringBuilder = new StringBuilder();

        for(int i=0; i<sparseArray.size(); ++i){
            stringBuilder.append(sparseArray.valueAt(i).getValue());
            stringBuilder.append("\n");
        }

        Log.d(TAG, "CopyTextFromBitmap: String: " + stringBuilder.toString());
        textRecognizer.release();
        return stringBuilder.toString().trim();
    }

}
