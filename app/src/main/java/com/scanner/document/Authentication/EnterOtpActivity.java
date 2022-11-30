package com.scanner.document.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.scanner.document.MainActivity;
import com.scanner.document.R;
import com.scanner.document.Utils.Constants;

/**
 * Activity to enter otp sent by Firebase auth
 */

public class EnterOtpActivity extends AppCompatActivity {

    EditText[] otpText;
    EditText otpValueText;
    CardView register;
    MaterialCardView changeNumber;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_otp);

        _init();

        // Finishing activity to change number
        changeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckOtp();
            }
        });

        // Set Otp motion
        SetOtpMotion();
    }

    /**
     * Function to proceed edittext to next edittext after entering number
     */
    private void SetOtpMotion() {
        for (int i = 0; i < 6; ++i) {
            EditText editText = otpText[i];
            int finalI = i;
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable.length() == 1 && finalI != 5) {
                        int next = (finalI + 1) % 6;
                        otpText[finalI].clearFocus();
                        otpText[next].requestFocus();
                    }
                }
            });
        }
    }


    /**
     * Function to check entered OTP
     */
    private void CheckOtp() {
        if (getIntent() == null || !getIntent().hasExtra(Constants.VERIFICATION_ID)) {
            finish();
            return;
        }

        String verificationId = getIntent().getStringExtra(Constants.VERIFICATION_ID);
        if (verificationId.trim().isEmpty()) {
            finish();
            return;
        }

        String otp = "";
        otp = otpValueText.getText().toString().trim();

        if (otp.length() != 6) {
            Toast.makeText(this, "Please enter a valid OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Using Firebase Auth to login user with otp and verificationId
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithCredential(credential);
    }


    /**
     * Function to sign in user with PhoneAuthCredential
     *
     * @param credential - Firebase credential to authenticate user
     */
    private void signInWithCredential(PhoneAuthCredential credential) {
        showDialog();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        dismissDialog();
                        Intent intent = new Intent(EnterOtpActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dismissDialog();
                        Toast.makeText(EnterOtpActivity.this, "An error occurred!!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Initialising layout views
     */
    private void _init() {

        otpValueText = findViewById(R.id.enter_otp_value);
        otpText = new EditText[]{
                findViewById(R.id.enter_otp_1),
                findViewById(R.id.enter_otp_2),
                findViewById(R.id.enter_otp_3),
                findViewById(R.id.enter_otp_4),
                findViewById(R.id.enter_otp_5),
                findViewById(R.id.enter_otp_6),
        };

        register = findViewById(R.id.enter_otp_register);
        changeNumber = findViewById(R.id.enter_otp_enter_phone_number);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Verifying OTP");
        progressDialog.setMessage("PLease wait...");
        progressDialog.setCancelable(false);
    }

    /**
     * Displaying ProgressDialog if not null
     */
    private void showDialog() {
        if (progressDialog != null) {
            progressDialog.show();
            ;
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
}