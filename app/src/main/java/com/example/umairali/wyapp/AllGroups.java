package com.example.umairali.wyapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllGroups extends AppCompatActivity {

    Toolbar toolbar;
    private DatabaseReference UsersDatabase, mRootRef, UserDatabase,mMembersDatabase, mBlockmembers;
    private FirebaseAuth mAuth;
    String mCurrentUser;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mAllGroupsLIst;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_groups);
        toolbar = (Toolbar) findViewById(R.id.allgroups);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Groups");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //---------------------//
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();
        UserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser);
        UserDatabase.keepSynced(true);
        UsersDatabase = FirebaseDatabase.getInstance().getReference().child("Group_Metadata");
        UsersDatabase.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mRootRef.keepSynced(true);
        //-----------------------//
        mAllGroupsLIst = (RecyclerView)findViewById(R.id.groupsList);
        mAllGroupsLIst.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(AllGroups.this);
        mAllGroupsLIst.setLayoutManager(mLayoutManager);


    }
    @Override
    public void onStart() {
        UserDatabase.child("online").setValue("true");
        super.onStart();
        FirebaseRecyclerAdapter<Users, TrendingGroupsViewHolder> firebaseRecyclerAdapter2 = new FirebaseRecyclerAdapter<Users, TrendingGroupsViewHolder>(
                Users.class,
                R.layout.user_single_layout3,
                TrendingGroupsViewHolder.class,
                UsersDatabase
        ) {
            @Override
            protected void populateViewHolder(final TrendingGroupsViewHolder viewHolder2, Users model, int position) {
                final String user_id = getRef(position).getKey();
                if (user_id != null) {
                    mMembersDatabase = FirebaseDatabase.getInstance().getReference().child("Group_Metadata").child(user_id).child("group_members");
                    mMembersDatabase.keepSynced(true);
                    mBlockmembers = FirebaseDatabase.getInstance().getReference().child("Group_Metadata").child(user_id).child("block_members");
                    mBlockmembers.keepSynced(true);
                    UsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("Group_Name")&& dataSnapshot.hasChild("Group_Description")&&dataSnapshot.hasChild("CreatedBy_UserID")){
                                String dec=dataSnapshot.child("Group_Description").getValue().toString();
                                viewHolder2.setStatus(dec);
                                final String CreatedBy_UserID = dataSnapshot.child("CreatedBy_UserID").getValue().toString();
                                ImageView imageView=(ImageView)viewHolder2.view.findViewById(R.id.delete);
                                final String groupName = dataSnapshot.child("Group_Name").getValue().toString();
                                viewHolder2.setName(groupName);
                                if (CreatedBy_UserID.equals(mCurrentUser)){

                                    imageView.setVisibility(View.VISIBLE);
                                }
                            }
                            if (dataSnapshot.hasChild("Group_image")){
                                String groupImage = dataSnapshot.child("Group_image").getValue().toString();
                                viewHolder2.setImage(groupImage, AllGroups.this);
                            }
                            ImageView imageView=(ImageView)viewHolder2.view.findViewById(R.id.delete);
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //Toast.makeText(GroupProfile.this, "hello", Toast.LENGTH_SHORT).show();
                                    UsersDatabase.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(final DataSnapshot dataSnapshot) {
                                            mMembersDatabase.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(final DataSnapshot dataSnapshot1) {
                                                    for (DataSnapshot data : dataSnapshot1.getChildren()) {
                                                        // Toast.makeText(GroupProfile.this,""+data.getKey(),Toast.LENGTH_SHORT).show();
                                                        mRootRef.child(data.getKey()).child("Groups").child(user_id).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (dataSnapshot.hasChild("block_members")) {
                                                                    mBlockmembers.addValueEventListener(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            for (DataSnapshot data1 : dataSnapshot.getChildren()) {
                                                                                //Toast.makeText(GroupProfile.this,""+data1.getKey(),Toast.LENGTH_SHORT).show();
                                                                                mRootRef.child("Users").child(data1.getKey()).child("Groups").child(user_id).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {

                                                                                        UsersDatabase.child(user_id).setValue(null);
                                                                                    }
                                                                                });
                                                                            }

                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                                } else {
                                                                    UsersDatabase.child(user_id).setValue(null);
                                                                }
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
                            });
                            viewHolder2.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent newIntent = new Intent(AllGroups.this, GroupProfile.class);
                                    newIntent.putExtra("user_id", user_id);
                                    startActivity(newIntent);
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

        mAllGroupsLIst.setAdapter(firebaseRecyclerAdapter2);
    }

    @Override
    public void onStop() {
        super.onStop();
        UserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
    }
    public static class TrendingGroupsViewHolder extends RecyclerView.ViewHolder {
        View view;

        public TrendingGroupsViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            TextView usersNameView = (TextView)view.findViewById(R.id.user_single_name);
            usersNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView usersStatusView = (TextView)view.findViewById(R.id.messageTextView);
            usersStatusView.setText(status);
        }

        public void setImage(String image, final Context context) {
            final CircleImageView Image = (CircleImageView)view.findViewById(R.id.user_single_image);
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
