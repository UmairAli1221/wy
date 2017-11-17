package com.example.umairali.wyapp;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private Toolbar mChatToolbar;

    private DatabaseReference mRootRef;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private TextView mTyingText;
    private CircleImageView mProfileImage;
    private String mCurrentUserId;
    private EmojiconEditText mChatMessageView;
    View rootView;
    private ProgressDialog mprogressDialog;
    private EmojIconActions emojIcon;
    private SwipeRefreshLayout mRefreshLayout;
    private DatabaseReference mNotificationDatabase;

    private RecyclerView mMessagesList;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private DatabaseReference mUserRef;
    private FirebaseAuth mAuth;
    private static final int  TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    //keep track of camera capture intent
    static final int CAMERA_CAPTURE = 1;
    //keep track of cropping intent
    final int PIC_CROP = 3;
    //keep track of gallery intent
    final int PICK_IMAGE_REQUEST = 2;
    //captured picture uri
    private Uri picUri;
    private Bitmap bitmap;
    private int itemPos=0;
    private String mLastKey="";
    private String mPrevKey="";
    private StorageReference imageStorage;
    LinearLayout mContainerImg;

    ImageView mSendMessage, mAddImg, mEmojiBtn, mDeleteImg, mPreviewImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("Notifications");
        imageStorage = FirebaseStorage.getInstance().getReference();


        mChatUser = getIntent().getStringExtra("user_id");
        final String userName = getIntent().getStringExtra("user_name");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        // ---- Custom Action bar Items ----

        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        mAddImg=(ImageView)findViewById(R.id.addImg);
        mContainerImg=(LinearLayout)findViewById(R.id.container_img);
        mDeleteImg=(ImageView)findViewById(R.id.deleteImg);
        mEmojiBtn=(ImageView)findViewById(R.id.emojiBtn);
        mContainerImg.setVisibility(View.GONE);
        mPreviewImg=(ImageView)findViewById(R.id.previewImg);

        //Progress Dialoge
        mprogressDialog = new ProgressDialog(this);
        mprogressDialog.setTitle("Sending...");
        mprogressDialog.setMessage("Please Wait.....");
        mprogressDialog.setCanceledOnTouchOutside(false);

        rootView = findViewById(R.id.root_view);
        mSendMessage = (ImageView) findViewById(R.id.sendMessage);
        mChatMessageView = (EmojiconEditText) findViewById(R.id.messageText);
        emojIcon = new EmojIconActions(this, rootView, mChatMessageView, mEmojiBtn);
        emojIcon.ShowEmojIcon();
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                mChatMessageView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            }

            @Override
            public void onKeyboardClose() {
            }
        });


        mDeleteImg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                picUri = null;

                mContainerImg.setVisibility(View.GONE);
            }
        });
        mAddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_layout_swip);

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }
        });
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos=0;
                loadMoreMessages();
            }
        });

        loadMessages();

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView) findViewById(R.id.listView);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);


        mMessagesList.setAdapter(mAdapter);





        mTitleView.setText(userName);
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                String image = dataSnapshot.child("profile_image").getValue().toString();
                // String name=dataSnapshot.child("name").getValue().toString();
                // mTitleView.setText(name);

                if (online.equals("true")) {

                    mLastSeenView.setText("Online");


                } else {

                    //---------------User Last Online Status---------//
                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastSeenView.setText(lastSeenTime);
                    Picasso.with(ChatActivity.this).load(image)
                            .placeholder(R.drawable.default_avatar).into(mProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(mProfileImage.getContext()).load(R.drawable.default_avatar).into(mProfileImage);
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    protected void onStart() {
        super.onStart();


        mUserRef.child("online").setValue("true");




    }


    private void SelectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
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
                        Toast.makeText(ChatActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
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
                mContainerImg.setVisibility(View.VISIBLE);
                mPreviewImg.setImageBitmap(thePic);

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


    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("messages");
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);

                String messageKey=dataSnapshot.getKey();
                if (!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++,message);
                }else {
                    mPrevKey=mLastKey;
                }

                if (itemPos==1){

                    mLastKey=messageKey;
                }


                mAdapter.notifyDataSetChanged();
                // mMessagesList.scrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("messages");
        Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                itemPos++;
                if (itemPos==1){
                    String messageKey=dataSnapshot.getKey();
                    mLastKey=messageKey;
                    mPrevKey=messageKey;
                }
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {

        mprogressDialog.show();
        mChatMessageView = (EmojiconEditText) findViewById(R.id.messageText);

        final String message = mChatMessageView.getText().toString();
        mChatMessageView.clearFocus();

            final String current_user_ref = "Chat/" + mCurrentUserId + "/" + mChatUser+"/messages";
            final String chat_user_ref = "Chat/" + mChatUser + "/" + mCurrentUserId+"/messages";

            DatabaseReference user_message_push = mRootRef.child("Chat")
                    .child(mCurrentUserId).child(mChatUser).child("messages").push();

            final String push_id = user_message_push.getKey();


            Map messageMap = new HashMap();

            if (picUri!=null){
                final StorageReference filapath = imageStorage.child("message_images").child(push_id + ".jpg");
                filapath.putFile(picUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filapath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    Map messageMap = new HashMap();
                                    if (TextUtils.isEmpty(message)){
                                        messageMap.put("message","default");
                                    }else {
                                        messageMap.put("message",message);
                                    }
                                    messageMap.put("seen", false);
                                    messageMap.put("messageImage", uri.toString());
                                    messageMap.put("time", ServerValue.TIMESTAMP);
                                    messageMap.put("from", mCurrentUserId);

                                    Map messageUserMap = new HashMap();
                                    messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                                    messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                                    mChatMessageView.setText("");

                                    mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                            if (databaseError != null) {

                                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                                mprogressDialog.dismiss();

                                            }else {
                                                mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("last_message").setValue("Photo!");
                                                mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("last_message_time").setValue(ServerValue.TIMESTAMP);
                                                mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("last_message").setValue("Photo!");
                                                mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("last_message_time").setValue(ServerValue.TIMESTAMP);
                                                final Map notification = new HashMap<>();
                                                notification.put("from",mCurrentUserId );
                                                notification.put("type","Single_message");
                                                notification.put("message", "Photo!");
                                                mNotificationDatabase.child(mChatUser).push().setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("CHAT_LOG","Done");
                                                        picUri=null;
                                                        mContainerImg.setVisibility(View.GONE);
                                                        mprogressDialog.dismiss();
                                                    }
                                                });
                                            }

                                        }
                                    });

                                }
                            });
                        }else {
                            mprogressDialog.dismiss();
                            Toast.makeText(ChatActivity.this, "Error In Uploading Image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else if(!TextUtils.isEmpty(message) && picUri==null) {
                messageMap.put("message", message);
                messageMap.put("seen", false);
                messageMap.put("messageImage", "default");
                messageMap.put("time", ServerValue.TIMESTAMP);
                messageMap.put("from", mCurrentUserId);
                Map messageUserMap = new HashMap();
                messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                mChatMessageView.setText("");

                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if (databaseError != null) {

                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            mprogressDialog.dismiss();

                        }else {
                            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("last_message").setValue(message);
                            mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("last_message_time").setValue(ServerValue.TIMESTAMP);
                            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("last_message").setValue(message);
                            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("last_message_time").setValue(ServerValue.TIMESTAMP);
                            final Map notification = new HashMap<>();
                            notification.put("from",mCurrentUserId );
                            notification.put("type","Single_message");
                            notification.put("message", message);
                            mNotificationDatabase.child(mChatUser).push().setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("CHAT_LOG","Done");
                                    mprogressDialog.dismiss();
                                }
                            });
                        }

                    }
                });
            }else {
                Toast.makeText(ChatActivity.this, "Write Something", Toast.LENGTH_SHORT).show();
                mprogressDialog.dismiss();
            }
        }



    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }

    }
}