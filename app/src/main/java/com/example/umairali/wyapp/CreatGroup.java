package com.example.umairali.wyapp;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreatGroup extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    EditText mName, mDescription, mTag;
    Button mbtnUpload, mbtncreatechannel;
    CircleImageView circleImageView;
    String name, bio;
    private Uri picUri;
    private DatabaseReference UsersDatabase;
    private DatabaseReference mDatabase, mRootRef;

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mGroup_Id;
    private String mCurrentUser;
    private StorageReference imageStorage;
    private ProgressDialog mprogressDialog;
    //keep track of camera capture intent
    static final int CAMERA_CAPTURE = 1;
    //keep track of cropping intent
    final int PIC_CROP = 3;
    //keep track of gallery intent
    final int PICK_IMAGE_REQUEST = 2;
    //captured picture uri

    private final int CALL_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creat_group);
        toolbar = (Toolbar) findViewById(R.id.creatGroup);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Creat Group");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //---------------//
        mName = (EditText) findViewById(R.id.et_channelname);
        mDescription = (EditText) findViewById(R.id.tv_bio);
        mbtnUpload = (Button) findViewById(R.id.btn_upload);
        mbtncreatechannel = (Button) findViewById(R.id.btn_createchannel);
        circleImageView = (CircleImageView) findViewById(R.id.channel_image);

        //-=------------------------//
        UsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersDatabase.keepSynced(true);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Group_Metadata").push();
        mGroup_Id = mDatabase.getKey();
        mDatabase.keepSynced(true);
        imageStorage = FirebaseStorage.getInstance().getReference();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUser = mFirebaseUser.getUid();

        mbtnUpload.setOnClickListener(this);
        mbtncreatechannel.setOnClickListener(this);

        /////////////////----------------------///////////////
        mprogressDialog = new ProgressDialog(this);
        mprogressDialog.setMessage("Creating Chanel.....");
        mprogressDialog.setCanceledOnTouchOutside(false);



    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_createchannel:
                createchannel();
                break;
            case R.id.btn_upload:

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    callPermission();
                } else {
                    Uploadimage();
                }
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void callPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CALL_CODE);
    }


    private void Uploadimage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(CreatGroup.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    try {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        String imageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/picture.jpg";
                        File imageFile = new File(imageFilePath);
                        picUri = Uri.fromFile(imageFile); // convert path to Uri
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
                        startActivityForResult(takePictureIntent, CAMERA_CAPTURE);

                    } catch (ActivityNotFoundException anfe) {
                        //display an error message
                        String errorMessage = "Whoops - your device doesn't support capturing images!";
                        Toast.makeText(CreatGroup.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    // Start the Intent
                    startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }


    private void createchannel() {
        mprogressDialog.show();
        name = mName.getText().toString().trim();
        bio = mDescription.getText().toString().trim();

        if (name.isEmpty() || bio.isEmpty()) {
            Toast.makeText(CreatGroup.this,"Please Enter All Details",Toast.LENGTH_SHORT).show();
        } else {
            mDatabase.child("Created_At").setValue(ServerValue.TIMESTAMP);
            mDatabase.child("CreatedBy_UserID").setValue(mCurrentUser);
            mDatabase.child("Group_Id").setValue(mGroup_Id);
            mDatabase.child("Group_Name").setValue(name);
            mDatabase.child("Group_Description").setValue(bio);
            mDatabase.child("members").setValue(1);
            mDatabase.child("TypeOfGroup").setValue("private");
            mDatabase.child("group_members").child(mCurrentUser).child("null").setValue("");
            //finish();
            UsersDatabase.child(mCurrentUser).child("Groups").child(mGroup_Id).child("null").setValue("");
            if (picUri != null) {
                final StorageReference filapath = imageStorage.child("Group_Logo").child(mGroup_Id).child(mGroup_Id + ".jpg");
                filapath.putFile(picUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filapath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    mDatabase.child("Group_image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                mprogressDialog.dismiss();
                                                Toast.makeText(CreatGroup.this, "Image Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                                            } else {
                                                mprogressDialog.dismiss();
                                                Toast.makeText(CreatGroup.this, "Error In Uploading Image", Toast.LENGTH_SHORT).show();
                                                mDatabase.child("Group_image").setValue("default");
                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            mprogressDialog.dismiss();
                            mDatabase.child("Group_image").setValue("default");
                            Toast.makeText(CreatGroup.this, "Error In Uploading Image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                mDatabase.child("Group_image").setValue("default");
                mprogressDialog.dismiss();

            }
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //user is returning from capturing an image using the camera
            if (requestCode == CAMERA_CAPTURE) {
                //get the Uri for the captured image
                Uri uri = picUri;
                //carry out the crop operation
                performCrop();
                Log.d("picUri", uri.toString());

            } else if (requestCode == PICK_IMAGE_REQUEST) {
                picUri = data.getData();
                Log.d("uriGallery", picUri.toString());
                performCrop();
            }

            //user is returning from cropping the image
            else if (requestCode == PIC_CROP) {
                //get the returned data
                Bundle extras = data.getExtras();
                //get the cropped bitmap
                Bitmap thePic = (Bitmap) extras.get("data");
                //display the returned cropped image
                circleImageView.setImageBitmap(thePic);
            }

        }
    }

    private void performCrop() {
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        } catch (ActivityNotFoundException anfe) {
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
