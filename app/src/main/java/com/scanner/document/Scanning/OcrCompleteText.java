package com.scanner.document.Scanning;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import com.scanner.document.R;
import com.scanner.document.Utils.Constants;

public class OcrCompleteText extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_complete_text);

        _init();

        if(getIntent()==null || !getIntent().hasExtra(Constants.TEXT)){
            Toast.makeText(this, "An error occurred!!", Toast.LENGTH_SHORT).show();
            finish();
        }
        String ocr = getIntent().getStringExtra(Constants.TEXT);
        if(ocr==null || ocr.trim().isEmpty()){
            ocr = "No text found!!";
        }
        textView.setText(ocr);
    }

    private void _init() {
        textView = findViewById(R.id.ocr_complete_text_tv);
        textView.setMovementMethod(new ScrollingMovementMethod());
    }
}