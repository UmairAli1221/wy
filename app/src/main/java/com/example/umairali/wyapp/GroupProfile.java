package com.example.umairali.wyapp;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class GroupProfile extends AppCompatActivity {

    private CircleImageView mImage;
    RecyclerView rvmembers;

    Bundle bundle;
    String from;
    TextView mDesc, mMember;
    Button mJoinclan;
    private DatabaseReference UsersDatabase, mRootRef, UserDatabase, mDatabase, mMembersDatabase,mBlockmembers;
    private FirebaseAuth mAuth;
    String mCurrentUser;
    private String mGroupId, mGroupName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);

        mDesc = (TextView) findViewById(R.id.tv_description);
        mMember = (TextView) findViewById(R.id.tv_member);
        mJoinclan = (Button) findViewById(R.id.btn_join);
        mImage = (CircleImageView) findViewById(R.id.user_image);


        //----------------//
        mGroupId = getIntent().getStringExtra("user_id");
        mGroupName = getIntent().getStringExtra("Group_Name");

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
        mDatabase.child(mGroupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String groupDesc = dataSnapshot.child("Group_Description").getValue().toString();
                String groupImage = dataSnapshot.child("Group_image").getValue().toString();
                mDesc.setText("Description: " + groupDesc);
                Picasso.with(getApplication()).load(groupImage)
                        .placeholder(R.drawable.default_avatar).into(mImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(getApplication()).load(R.drawable.default_avatar).into(mImage);
                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //rvmembers adaptor
        rvmembers = (RecyclerView) findViewById(R.id.rv_channel_mebers);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        rvmembers.setHasFixedSize(true);
        rvmembers.setNestedScrollingEnabled(false);
        rvmembers.setLayoutManager(gridLayoutManager);

        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.scrol);
        scrollView.getParent().requestChildFocus(scrollView, scrollView);
        UsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mGroupId)){
                    mJoinclan.setEnabled(false);
                }else {
                    mJoinclan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mDatabase.child(mGroupId).child("group_members").child(mCurrentUser).child("null").setValue("").addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mDatabase.child(mGroupId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            UsersDatabase.child(mGroupId).child("null").setValue("");
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                    mDatabase.child(mGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String membersAdded =dataSnapshot.child("members").getValue().toString();
                                            int members= Integer.parseInt(membersAdded);
                                            members = members+1;
                                            mDatabase.child(mGroupId).child("members").setValue(String.valueOf(members));
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });

                            // mDatabase.child(mGroupId).child("members").setValue(members+1);
                            //String mmm= String.valueOf(members);
                            //Toast.makeText(Profile.this,""+members,Toast.LENGTH_LONG).show();



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
    protected void onStart() {
        super.onStart();
        UserDatabase.child("online").setValue("true");
        super.onStart();
        FirebaseRecyclerAdapter<Users, AllUsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, AllUsersViewHolder>(
                Users.class,
                R.layout.chat_item,
                AllUsersViewHolder.class,
                mMembersDatabase
        ) {
            @Override
            protected void populateViewHolder(final AllUsersViewHolder viewHolder, Users model, int position) {
                final String user_id = getRef(position).getKey();
                if (user_id != null) {
                    mRootRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String groupName = dataSnapshot.child("name").getValue().toString();
                            viewHolder.setName(groupName);
                            String groupImage = dataSnapshot.child("profile_image").getValue().toString();
                            viewHolder.setImage(groupImage, GroupProfile.this);
                            mDatabase.child(mGroupId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String group_admin=dataSnapshot.child("CreatedBy_UserID").getValue().toString();
                                    if (group_admin.equals(user_id)){
                                        TextView groupadmin=(TextView)viewHolder.view.findViewById(R.id.admin);
                                        groupadmin.setVisibility(View.VISIBLE);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent newIntent = new Intent(GroupProfile.this, Profile.class);
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
        rvmembers.setAdapter(firebaseRecyclerAdapter);
    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder {
        View view;

        public AllUsersViewHolder(View itemView) {
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

        public void setAdmin() {
            TextView groupadmin=(TextView)view.findViewById(R.id.admin);
            groupadmin.setVisibility(View.VISIBLE);
        }
    }
}
