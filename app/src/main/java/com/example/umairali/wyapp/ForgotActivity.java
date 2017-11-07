package com.example.umairali.wyapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotActivity extends AppCompatActivity {

    private EditText Email;
    private Button resetPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog mprogressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        //Getting Refernces
        Email = (EditText) findViewById(R.id.femail);
        resetPassword = (Button) findViewById(R.id.ForgotPassword);

        //Progress Dialoge
        mprogressDialog = new ProgressDialog(this);
        mprogressDialog.setTitle("Requesting...");
        mprogressDialog.setMessage("Sending Email.....");

        //Firebase Authentication
        mAuth = FirebaseAuth.getInstance();


        //Toolabr BackArrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //ResetButton
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mprogressDialog.show();
                String email = Email.getText().toString();
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mprogressDialog.dismiss();
                        Toast.makeText(ForgotActivity.this, "Link Has Sent To Your Email For Reset Password", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(ForgotActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    }
                });

            }
        });

    }
}
