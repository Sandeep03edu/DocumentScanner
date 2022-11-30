package com.scanner.document.Scanning;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.scanner.document.R;
import com.scanner.document.Utils.AppUtils;
import com.scanner.document.Utils.Constants;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;

import java.util.Collections;
import java.util.List;

public class FilterImage extends AppCompatActivity {

    private static final String TAG = "FilterImageTag";
    ImageView back, imageView, check;
    RecyclerView filterRcv;
    boolean tapped = false;


    static {
        System.loadLibrary("NativeImageProcessor");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_image);

        _init();

        // SetData
        SetData();

        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendResult();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(!tapped) {
            finish();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Apply the selected filter?")
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SendResult();
                    }
                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).show();
    }

    private void SetData() {
        if (getIntent() == null || !getIntent().hasExtra(Constants.ImageBitmapBytes)) {
            Toast.makeText(this, "An error occurred!!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        byte[] bytes = getIntent().getByteArrayExtra(Constants.ImageBitmapBytes);
        Bitmap bitmap = AppUtils.Bytes2Bitmap(bytes);
        imageView.setImageBitmap(bitmap);

        // Setting RecyclerView
        List<Filter> filters = FilterPack.getFilterPack(FilterImage.this);
        filters.add(null);
        Collections.swap(filters, 0, filters.size() - 1);

        FilterAdapter adapter = new FilterAdapter(this, bitmap, filters, new FilterAdapter.FilterActionListener() {
            @Override
            public void setFilter(Filter filter) {
                tapped = true;
                Bitmap bitmap1 = bitmap;
                bitmap1 = bitmap1.copy(Bitmap.Config.ARGB_8888, true);
                if (filter != null) {
                    bitmap1 = filter.processFilter(bitmap1);
                }
                imageView.setImageBitmap(bitmap1);
            }
        });

        filterRcv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        filterRcv.setAdapter(adapter);
    }

    private void SendResult() {
        Intent intent = new Intent();
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap filteredBitmap = drawable.getBitmap();
        byte[] bytes = AppUtils.Bitmap2Bytes(filteredBitmap);

        intent.putExtra(Constants.ImageBitmapBytes, bytes);
        if(getIntent()!=null && getIntent().hasExtra(Constants.IMAGE_POS)){
            intent.putExtra(Constants.IMAGE_POS, getIntent().getIntExtra(Constants.IMAGE_POS, -1));
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    private void _init() {
        back = findViewById(R.id.filter_image_back);
        imageView = findViewById(R.id.filter_image_imageview);
        check = findViewById(R.id.filter_image_check);

        filterRcv = findViewById(R.id.filter_image_rcv);
        tapped = false;
    }
}