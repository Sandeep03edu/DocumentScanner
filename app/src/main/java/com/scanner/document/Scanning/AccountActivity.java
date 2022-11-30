package com.scanner.document.Scanning;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.scanner.document.R;

public class AccountActivity extends AppCompatActivity {
    TextView accountNumber;
    LinearLayout shareApp;
    ImageView home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        _init();

        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            String mobNum = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
            accountNumber.setText("Account : " + mobNum);
        }

        shareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 Intent intent = new Intent();
                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 intent.setAction(Intent.ACTION_SEND);
                 intent.putExtra(Intent.EXTRA_TEXT, "Download Scanning app from PlayStore");
                 intent.setType("text/plain");
                 Intent shareIntent = Intent.createChooser(intent, "Share via");
                 shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(shareIntent);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void _init() {
        accountNumber = findViewById(R.id.account_number);
        shareApp = findViewById(R.id.account_share);
        home = findViewById(R.id.account_home);
    }
}