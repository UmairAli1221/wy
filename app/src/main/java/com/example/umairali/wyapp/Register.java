package com.example.umairali.wyapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class Register extends AppCompatActivity {

    private EditText memailEditText, mpasswordEditText,mnameEditText;
    private Button mregisterButton;
    private ProgressDialog mprogressDialog;
    private DatabaseReference mFirebasedatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //EditTexts Refernces
        memailEditText=(EditText)findViewById(R.id.emailEditText);
        mpasswordEditText=(EditText)findViewById(R.id.passwordEditText);
        mnameEditText=(EditText)findViewById(R.id.nameEditText);
        //Login Button References
        mregisterButton=(Button)findViewById(R.id.RegistrationButton);

        //Toolabr BackArrow
      //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Progress Dialoge
        mprogressDialog = new ProgressDialog(this);
        mprogressDialog.setTitle("Registring...");
        mprogressDialog.setMessage("Please Wait.....");
        mprogressDialog.setCanceledOnTouchOutside(false);

        //Firebase References
        mFirebasedatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //Registration Button
        mregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = memailEditText.getText().toString();
                String Password = mpasswordEditText.getText().toString();
                final String name=mnameEditText.getText().toString();
                if (email.length() == 0) {
                    memailEditText.requestFocus();
                    memailEditText.setError("Field Cannot Be Empty");
                }else if (Password.length() == 0) {
                    mpasswordEditText.requestFocus();
                    mpasswordEditText.setError("Field Cannot Be Empty");
                } else if(name.length()==0){
                    mnameEditText.requestFocus();
                    mnameEditText.setError("Field Cannot Be Empty");
                }else {
                    mprogressDialog.show();
                    mAuth.createUserWithEmailAndPassword(email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                String mUserID=mAuth.getCurrentUser().getUid();
                                String device_token= FirebaseInstanceId.getInstance().getToken();
                                mFirebasedatabase.child("Users").child(mUserID).child("status").setValue("WhereYou Are!!!");
                                mFirebasedatabase.child("Users").child(mUserID).child("Uid").setValue(mUserID);
                                mFirebasedatabase.child("Users").child(mUserID).child("profile_image").setValue("default");
                                mFirebasedatabase.child("Users").child(mUserID).child("device_token").setValue(device_token);
                                mFirebasedatabase.child("Users").child(mUserID).child("email").setValue(email);
                                mFirebasedatabase.child("Users").child(mUserID).child("name").setValue(name);
                                Intent mainIntent=new Intent(Register.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                mprogressDialog.dismiss();
                                startActivity(mainIntent);
                                finish();
                            }else {
                                mprogressDialog.dismiss();
                                try {
                                    throw task.getException();
                                } catch(FirebaseAuthWeakPasswordException e) {
                                    mpasswordEditText.setError("Weak Password");
                                    mpasswordEditText.requestFocus();
                                } catch(FirebaseAuthInvalidCredentialsException e) {
                                    memailEditText.setError("Invalid Email");
                                    memailEditText.requestFocus();
                                } catch(FirebaseAuthUserCollisionException e) {
                                    memailEditText.setError("User Already Exist");
                                    memailEditText.requestFocus();
                                } catch(FirebaseNetworkException e) {
                                    Toast.makeText(Register.this,"User Already Exist",Toast.LENGTH_LONG).show();
                                }
                                catch(Exception e) {
                                    Toast.makeText(Register.this,"You Got Some Network Error.",Toast.LENGTH_LONG).show();
                                }

                            }
                        }
                    });
                }
            }
        });
    }

}
