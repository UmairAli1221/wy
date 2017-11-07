package com.example.umairali.wyapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText memailEditText, mpasswordEditText;
    private Button mloginButton;
    private TextView mregisterButton, mforgotButton;
    private ProgressDialog mprogressDialog;
    private DatabaseReference mFirebasedatabase;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //EditTexts Refernces
        memailEditText=(EditText)findViewById(R.id.emailEditText);
        mpasswordEditText=(EditText)findViewById(R.id.passwordEditText);
        //Login Button References
        mloginButton=(Button)findViewById(R.id.loginButton);
        //TextView Refernces
        mregisterButton=(TextView)findViewById(R.id.registerButton);
        mforgotButton=(TextView)findViewById(R.id.forgotButton);

        //Progress Dialoge
        mprogressDialog = new ProgressDialog(this);
        mprogressDialog.setTitle("Login...");
        mprogressDialog.setMessage("Please Wait.....");
        mprogressDialog.setCanceledOnTouchOutside(false);

        //Firebase References
        mFirebasedatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //Login Function
        mloginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Email = memailEditText.getText().toString();
                String Password = mpasswordEditText.getText().toString();
                if (TextUtils.isEmpty(Email)){
                    memailEditText.setError("Email Cannot Be Empty");
                    memailEditText.requestFocus();
                }else if (TextUtils.isEmpty(Password)){
                    mpasswordEditText.setError("Password Cannot Be Empty");
                    mpasswordEditText.requestFocus();
                }else {
                    mprogressDialog.show();
                    mAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String mUserID = mAuth.getCurrentUser().getUid();
                                String device_token = FirebaseInstanceId.getInstance().getToken();
                                mFirebasedatabase.child("Users").child(mUserID).child("device_token").setValue(device_token);
                                mFirebasedatabase.child("Users").child(mUserID).child("Uid").setValue(mUserID);
                                mprogressDialog.dismiss();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                mprogressDialog.dismiss();
                                try {
                                    throw task.getException();
                                } catch (FirebaseNetworkException e) {
                                    Toast.makeText(LoginActivity.this, "No Network.", Toast.LENGTH_LONG).show();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    mpasswordEditText.setError("Weak Password");
                                    mpasswordEditText.requestFocus();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    memailEditText.setError("Invalid Username or Password");
                                    memailEditText.requestFocus();
                                    mpasswordEditText.setError("Inavalid Username or Pasword");
                                    mpasswordEditText.requestFocus();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    memailEditText.setError("User Already Exist");
                                    memailEditText.requestFocus();
                                } catch (Exception e) {
                                    Toast.makeText(LoginActivity.this, "You Got Some Network Error.", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });

                }


            }
        });

        //Registration Button
        mregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, Register.class);
                startActivity(intent);
            }
        });

        //ForgotPassword Button
        mforgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotActivity.class);
                startActivity(intent);
            }
        });

    }
}
