package com.scanner.document.Scanning;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;
import com.scanner.document.Model.Document;
import com.scanner.document.Model.GenericDocument;
import com.scanner.document.Model.OcrDocument;
import com.scanner.document.R;

import com.scanner.document.Saving.GenericDocumentModel;
import com.scanner.document.Utils.AppUtils;
import com.scanner.document.Utils.ImageResizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class PdfAction {

    private static final String TAG = "ConvertPdfTag";

    public static void document2Pdf(Context context, Document document) {
        Log.d(TAG, "document2Pdf: Started pdf formation");

        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("pdf", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }

        String fileName = document.getTimeStamp() + ".pdf";
        File myPdfFile = new File(directory, fileName);
        Log.d(TAG, "document2Pdf: Folder name = " + fileName);

        com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document();
        try {
            PdfWriter.getInstance(pdfDocument, new FileOutputStream(myPdfFile));
            pdfDocument.open();

            Log.d(TAG, "document2Pdf: Inside try :)");
            ArrayList<String> images = document.getFilePhotos();
            for (String str : images) {
                Uri uri = Uri.parse(str);
                Bitmap bitmap = AppUtils.Uri2Bitmap(context, uri);
                if (bitmap == null) {
                    return;
                }

                bitmap = ImageResizer.reduceBitmapSize(bitmap, 240000);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Image image = Image.getInstance(stream.toByteArray());

                // Scaling image to full screen
                float scaler = ((pdfDocument.getPageSize().getWidth() - pdfDocument.leftMargin()
                        - pdfDocument.rightMargin() - 0) / image.getWidth()) * 100;
                image.scalePercent(scaler);

                pdfDocument.add(image);
            }

            Log.d(TAG, "document2Pdf: All images added :)");
        } catch (Exception e) {
            Log.d(TAG, "document2Pdf: Catch :/ " + e.getMessage());
            e.printStackTrace();
        } finally {
            pdfDocument.close();

            if (myPdfFile.exists()) {
                Uri filePath = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", myPdfFile);
                String filePathStr = String.valueOf(filePath);
                document.setSavedUri(filePathStr);
            } else {
                Log.d(TAG, "document2Pdf: File Doesn't exist");
            }

        }
    }

    public static Bitmap textAsBitmap(Context context, String text) {
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
                | Paint.LINEAR_TEXT_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(16.0F);

        int width = context.getResources().getDisplayMetrics().widthPixels;
        StaticLayout mTextLayout = new StaticLayout(text, textPaint,
                width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        // Create bitmap and canvas to draw to
        Bitmap b = Bitmap.createBitmap(370, mTextLayout.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);

        // Draw background
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG
                | Paint.LINEAR_TEXT_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        c.drawPaint(paint);

        // Draw text
        c.save();
        c.translate(0, 0);
        mTextLayout.draw(c);
        c.restore();

        return b;
    }

    public static void ocrDocument2Pdf(Context context, OcrDocument ocrDocument) {
        Log.d(TAG, "ocrDocument2Pdf: Started pdf formation");

        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("ocr_pdf", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }

        String fileName = ocrDocument.getTimeStamp() + ".pdf";
        File myPdfFile = new File(directory, fileName);
        Log.d(TAG, "ocrDocument2Pdf: Folder name = " + fileName);

        com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document();
        try {
            PdfWriter.getInstance(pdfDocument, new FileOutputStream(myPdfFile));
            pdfDocument.open();

            Log.d(TAG, "document2Pdf: Inside try :)");
            ArrayList<String> images = ocrDocument.getFilePhotos();
            ArrayList<String> ocrText = ocrDocument.getFileTexts();
            int size = images.size();

            for (int i = 0; i < size; ++i) {
                String str = images.get(i);
                String text = ocrText.get(i);

                // Adding Image
                Uri uri = Uri.parse(str);
                Bitmap bitmap = AppUtils.Uri2Bitmap(context, uri);
                if (bitmap == null) {
                    return;
                }
                Image image = getImage(bitmap, pdfDocument);
                pdfDocument.add(image);

                // Adding Picture text
                bitmap = textAsBitmap(context, text);
                if (bitmap == null) {
                    return;
                }
                Image image1 = getImage(bitmap, pdfDocument);
                pdfDocument.add(image1);

            }
            Log.d(TAG, "ocrDocument2Pdf: All images added :)");
        } catch (Exception e) {
            Log.d(TAG, "ocrDocument2Pdf: Catch :/ " + e.getMessage());
            e.printStackTrace();
        } finally {
            pdfDocument.close();

            if (myPdfFile.exists()) {
//                Uri filePath = Uri.fromFile(myPdfFile);
                Uri filePath = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", myPdfFile);
                String filePathStr = String.valueOf(filePath);
                ocrDocument.setSavedUri(filePathStr);
            } else {
                Log.d(TAG, "document2Pdf: File Doesn't exist");
            }

        }
    }

    private static Image getImage(Bitmap bitmap, com.itextpdf.text.Document pdfDocument) {
        bitmap = ImageResizer.reduceBitmapSize(bitmap, 240000);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image image = null;
        try {
            image = Image.getInstance(stream.toByteArray());
        } catch (BadElementException | IOException e) {
            e.printStackTrace();
        }

        // Scaling image to full screen
        float scaler = ((pdfDocument.getPageSize().getWidth() - pdfDocument.leftMargin()
                - pdfDocument.rightMargin() - 0) / image.getWidth()) * 100;
        image.scalePercent(scaler);
        return image;
    }

    public static void shareDocumentPdf(Context context, Document document) {
        if (document == null || document.getSavedUri() == null) {
            Toast.makeText(context, "An error occurred!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (document.getSavedUri().trim().isEmpty()) {
            document2Pdf(context, document);
        }

        if (document.getSavedUri().trim().isEmpty()) {
            Toast.makeText(context, "An error occurred!!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileUri = document.getSavedUri();
        Uri uri = Uri.parse(fileUri);

        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setType("application/pdf");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(share, "Share file"));
    }

    public static void shareOcrDocumentPdf(Context context, OcrDocument ocrDocument) {
        if (ocrDocument == null || ocrDocument.getSavedUri() == null) {
            Toast.makeText(context, "An error occurred!!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ocrDocument.getSavedUri().trim().isEmpty()) {
            ocrDocument2Pdf(context, ocrDocument);
        }

        if (ocrDocument.getSavedUri().trim().isEmpty()) {
            Toast.makeText(context, "An error occurred!!", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileUri = ocrDocument.getSavedUri();
        Uri uri = Uri.parse(fileUri);

        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.setType("application/pdf");
        share.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(share, "Share file"));
    }

    public static void openBottomSheet(Context context, GenericDocument genericDocument, GenericDocumentModel genericDocumentModel, GenericDocumentAdapter adapter) {
        Document document = genericDocument.getDocument();
        OcrDocument ocrDocument = genericDocument.getOcrDocument();
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        View bottomView = LayoutInflater.from(context).inflate(R.layout.more_bottom_nav, null);

        TextView fileName, fileSize, export, delete, rename;
        ImageView fileThumb;

        fileName = bottomView.findViewById(R.id.more_bottom_nav_file_name);
        fileSize = bottomView.findViewById(R.id.more_bottom_nav_file_size);
        fileThumb = bottomView.findViewById(R.id.more_bottom_nav_thumbnail);

        export = bottomView.findViewById(R.id.more_bottom_nav_export);
        delete = bottomView.findViewById(R.id.more_bottom_nav_delete);
        rename = bottomView.findViewById(R.id.more_bottom_nav_rename);

        if (document != null) fileThumb.setImageURI(Uri.parse(document.getFilePhotos().get(0)));
        if (ocrDocument != null)
            fileThumb.setImageURI(Uri.parse(ocrDocument.getFilePhotos().get(0)));

        if (document != null)
            fileName.setText(document.getFileName() + " " + AppUtils.formattedDate(document.getTimeStamp()));
        if (ocrDocument != null)
            fileName.setText(ocrDocument.getFileName() + " " + AppUtils.formattedDate(ocrDocument.getTimeStamp()));
        if (document != null) {
            double size = AppUtils.fileSizekiloBytes(document.getSavedUri(), context);
            fileSize.setText(String.format("File Size: %.2f kb", size));
            if (size > 1024) {
                size /= 1024;
                fileSize.setText(String.format("File Size: %.2f mb", size));
            }

            if (size == 0) {
                document2Pdf(context, document);
            }

            size = AppUtils.fileSizekiloBytes(document.getSavedUri(), context);
            fileSize.setText(String.format("File Size: %.2f kb", size));
            if (size > 1024) {
                size /= 1024;
                fileSize.setText(String.format("File Size: %.2f mb", size));
            }
        }
        if (ocrDocument != null) {
            double size = AppUtils.fileSizekiloBytes(ocrDocument.getSavedUri(), context);
            fileSize.setText(String.format("File Size: %.2f kb", size));
            if (size > 1024) {
                size /= 1024;
                fileSize.setText(String.format("File Size: %.2f mb", size));
            }

            if (size == 0) {
                ocrDocument2Pdf(context, ocrDocument);
            }

            size = AppUtils.fileSizekiloBytes(ocrDocument.getSavedUri(), context);
            fileSize.setText(String.format("File Size: %.2f kb", size));
            if (size > 1024) {
                size /= 1024;
                fileSize.setText(String.format("File Size: %.2f mb", size));
            }
        }

        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (document != null) shareDocumentPdf(context, document);
                if (ocrDocument != null) shareOcrDocumentPdf(context, ocrDocument);
                bottomSheetDialog.dismiss();
            }
        });

        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                PopupWindow popupWindow;
                View popupView;
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                popupView = layoutInflater.inflate(R.layout.cart_rename_pdf, null);

                int width = (Resources.getSystem().getDisplayMetrics().widthPixels * 3) / 4;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;

                boolean focusable = true;
                popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.setElevation(20);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                CardView save = popupView.findViewById(R.id.cart_rename_pdf_save);
                EditText fileEt = popupView.findViewById(R.id.cart_rename_pdf_edittext);

                if (document != null) fileEt.setText(document.getFileName());
                if (ocrDocument != null) fileEt.setText(ocrDocument.getFileName());
                fileEt.setSelection(fileEt.getText().toString().trim().length());

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = fileEt.getText().toString().trim();
                        if (name.isEmpty()) {
                            Toast.makeText(context, "Enter file name!!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (document != null) document.setFileName(name);
                        if (ocrDocument != null) ocrDocument.setFileName(name);
                        genericDocument.setDocument(document);
                        genericDocument.setOcrDocument(ocrDocument);
                        genericDocumentModel.update(genericDocument);
                        popupWindow.dismiss();
                    }
                });
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Are you sure to delete this pdf??")
                        .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                genericDocumentModel.delete(genericDocument);
                                dialogInterface.dismiss();
                                bottomSheetDialog.dismiss();
                            }
                        })
                        .setPositiveButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                bottomSheetDialog.dismiss();
                            }
                        }).show();
            }
        });

        bottomSheetDialog.setContentView(bottomView);
        bottomSheetDialog.show();
    }
}
