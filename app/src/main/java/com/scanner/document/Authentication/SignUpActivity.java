package com.scanner.document.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.scanner.document.R;
import com.scanner.document.Utils.Constants;

import java.util.concurrent.TimeUnit;

/**
 * Activity to Signup user
 */
public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivityTag";
    EditText mobileNumEt;
    CardView register;
    FirebaseAuth auth;
    String verificationId = "";
    CountryCodePicker countryCodePicker;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        _init();

        // Registration action
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mobileNumEt.getText() == null || mobileNumEt.getText().toString().trim().isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Enter a valid mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                RegisterUser();
            }
        });

    }

    /**
     * Registering user
     */
    private void RegisterUser() {
        String phoneNumber = mobileNumEt.getText().toString().trim();

        showDialog();
        String countryCode = countryCodePicker.getSelectedCountryCodeWithPlus();
        phoneNumber = countryCode + phoneNumber;

        /**
         * Authenticating user with PhoneAuthOptions
         */
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)       // Activity (for callback binding)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                Log.d(TAG, "onVerificationCompleted: ");
                                dismissDialog();
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Log.e(TAG, "onVerificationFailed: ", e);
                                // Listener updating onFailure
                                Toast.makeText(SignUpActivity.this, "An error occurred!!", Toast.LENGTH_SHORT).show();
                                dismissDialog();
                            }

                            @Override
                            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(s, forceResendingToken);
                                Log.d(TAG, "onCodeSent: String: " + s);
                                verificationId = s;
                                // Listener updating onCodeSent
                                dismissDialog();
                                MoveToOtpPage();
                            }
                        }).build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }

    /**
     * Moving to Otp page after sending otp successfully
     */
    private void MoveToOtpPage() {
        dismissDialog();
        Intent intent = new Intent(this, EnterOtpActivity.class);
        intent.putExtra(Constants.VERIFICATION_ID, verificationId);
        startActivity(intent);
    }

    /**
     * Initialising layout views
     */
    private void _init() {

        mobileNumEt = findViewById(R.id.sign_up_phone_num_et);
        register = findViewById(R.id.sign_up_register);
        countryCodePicker = findViewById(R.id.sign_up_country_code);

        auth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Sending OTP");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    /**
     * Displaying ProgressDialog if not null
     */
    private void showDialog(){
        if(progressDialog!=null){
            progressDialog.show();
        }
    }

    /**
     * Dismissing ProgressDialog if not null
     */
    private void dismissDialog() {
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}