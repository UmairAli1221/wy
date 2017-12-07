package com.example.umairali.wyapp;

import android.*;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupDetails extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView rvmembers, rvblock;
    ImageView mimgedit;
    CircleImageView circleImageView;
    GridLayoutManager gridLayoutManager;
    //image chooser
    private static int IMG_RESULT = 1;
    String ImageDecode;
    Intent intent;
    private final int CALL_CODE = 3;
    private String mGroupId;
    private DatabaseReference UsersDatabase, mRootRef, UserDatabase, mDatabase, mMembersDatabase,mBlockmembers,mblock,mUnblock;
    private FirebaseAuth mAuth;
    String mCurrentUser;
    private TextView mDesc, mMember;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);


        mGroupId = getIntent().getStringExtra("user_id");
        //Firebase References
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();
        UserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser);
        UsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser).child("Groups");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Group_Metadata");
        mDatabase.keepSynced(true);
        UsersDatabase.keepSynced(true);
        mMembersDatabase = FirebaseDatabase.getInstance().getReference().child("Group_Metadata").child(mGroupId).child("group_members");
        mMembersDatabase.keepSynced(true);
        mBlockmembers = FirebaseDatabase.getInstance().getReference().child("Group_Metadata").child(mGroupId).child("block_members");
        mBlockmembers.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        circleImageView = (CircleImageView) findViewById(R.id.user_image);
        mimgedit = (ImageView) findViewById(R.id.img_edit_profile);
        mDesc = (TextView) findViewById(R.id.tv_description);
        mDatabase.child(mGroupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String groupDesc = dataSnapshot.child("Group_Description").getValue().toString();
                String groupImage = dataSnapshot.child("Group_image").getValue().toString();
                mDesc.setText("Description: " + groupDesc);
                Picasso.with(getApplication()).load(groupImage)
                        .placeholder(R.drawable.default_avatar).into(circleImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(getApplication()).load(R.drawable.default_avatar).into(circleImageView);
                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//rvmembers adaptor
        rvmembers = (RecyclerView) findViewById(R.id.rv_channel_mebers);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        rvmembers.setHasFixedSize(true);
        rvmembers.setNestedScrollingEnabled(false);
        rvmembers.setLayoutManager(gridLayoutManager);



        //block members adaptor
        rvblock = (RecyclerView) findViewById(R.id.rv_channel_blockmebers);
        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        rvblock.setHasFixedSize(true);
        rvblock.setNestedScrollingEnabled(false);
        rvblock.setLayoutManager(gridLayoutManager);



        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.scrol);
        scrollView.getParent().requestChildFocus(scrollView, scrollView);




        mimgedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    callPermission();
                } else {
                    Uploadimage();
                }
            }
        });
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void callPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, CALL_CODE);
    }


    private void Uploadimage() {
        intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, IMG_RESULT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

            if (requestCode == IMG_RESULT && resultCode == RESULT_OK
                    && null != data) {


                Uri URI = data.getData();
                String[] FILE = {MediaStore.Images.Media.DATA};


                Cursor cursor = getContentResolver().query(URI,
                        FILE, null, null, null);

                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(FILE[0]);
                ImageDecode = cursor.getString(columnIndex);
                cursor.close();


                circleImageView.setImageBitmap(BitmapFactory
                        .decodeFile(ImageDecode));

            }
        } catch (Exception e) {

            Toast.makeText(this, "Please try again", Toast.LENGTH_LONG)
                    .show();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        UserDatabase.child("online").setValue("true");
        super.onStart();
        FirebaseRecyclerAdapter<Users, vrViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, vrViewHolder>(
                Users.class,
                R.layout.members,
                vrViewHolder.class,
                mMembersDatabase
        ) {
            @Override
            protected void populateViewHolder(final vrViewHolder viewHolder, Users model, int position) {
                final String user_id = getRef(position).getKey();
                if (user_id != null) {
                    mRootRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String groupName = dataSnapshot.child("name").getValue().toString();
                            viewHolder.setName(groupName);
                            String groupImage = dataSnapshot.child("profile_image").getValue().toString();
                            viewHolder.setImage(groupImage, GroupDetails.this);
                            mblock=mMembersDatabase.child(user_id).child("null");
                            mUnblock=mMembersDatabase.child(user_id).child("null");
                            mDatabase.child(mGroupId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String group_admin=dataSnapshot.child("CreatedBy_UserID").getValue().toString();
                                    if (group_admin.equals(mCurrentUser)){
                                        ImageView makeadmin=(ImageView)viewHolder.view.findViewById(R.id.ic_admin);
                                        ImageView block=(ImageView)viewHolder.view.findViewById(R.id.ic_block);
                                        makeadmin.setVisibility(View.VISIBLE);
                                        block.setVisibility(View.VISIBLE);
                                        block.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mDatabase.child(mGroupId).child("block_members").child(user_id).child("null").setValue("null").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        mDatabase.child(mGroupId).child("group_members").child(user_id).child("null").setValue(null);
                                                    }
                                                });
                                            }
                                        });
                                        makeadmin.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        };
        FirebaseRecyclerAdapter<Users, vrViewHolder> firebaseRecyclerAdapter2 = new FirebaseRecyclerAdapter<Users, vrViewHolder>(
                Users.class,
                R.layout.members,
                vrViewHolder.class,
                mBlockmembers
        ) {
            @Override
            protected void populateViewHolder(final vrViewHolder viewHolder, Users model, int position) {
                final String user_id = getRef(position).getKey();
                if (user_id != null) {
                    mRootRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String groupName = dataSnapshot.child("name").getValue().toString();
                            viewHolder.setName(groupName);
                            String groupImage = dataSnapshot.child("profile_image").getValue().toString();
                            viewHolder.setImage(groupImage, GroupDetails.this);
                            mDatabase.child(mGroupId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String group_admin=dataSnapshot.child("CreatedBy_UserID").getValue().toString();
                                    if (group_admin.equals(mCurrentUser)){
                                        ImageView unblock=(ImageView)viewHolder.view.findViewById(R.id.ic_add_to_group);
                                        unblock.setVisibility(View.VISIBLE);
                                        unblock.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                mDatabase.child(mGroupId).child("group_members").child(user_id).child("null").setValue("null").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mDatabase.child(mGroupId).child("block_members").child(user_id).child("null").setValue(null);
                                                    }
                                                });
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        };

        rvmembers.setAdapter(firebaseRecyclerAdapter);
        rvblock.setAdapter(firebaseRecyclerAdapter2);
    }
    public static class vrViewHolder extends RecyclerView.ViewHolder {
        View view;

        public vrViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            TextView usersNameView = (TextView) view.findViewById(R.id.user_name);
            usersNameView.setText(name);
        }

        public void setImage(String image, final Context context) {
            final CircleImageView Image = (CircleImageView) view.findViewById(R.id.user_image);
            Picasso.with(context).load(image)
                    .placeholder(R.drawable.default_avatar).into(Image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(R.drawable.default_avatar).into(Image);
                }
            });

        }
    }
}
