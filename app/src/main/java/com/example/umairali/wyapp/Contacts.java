package com.example.umairali.wyapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class Contacts extends Fragment {

    private RecyclerView mUsersList;
    private View mView;
    private DatabaseReference UsersDatabase, mRootRef, mFriendsDatabase;
    private LinearLayoutManager mLayoutManager;
    private FirebaseAuth mAuth;
    String mCurrentUser;
    private ImageButton mButton;
    private Query mphoneQuery;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_contacts, container, false);
        //------------Firebase Referencing--------//
        UsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersDatabase.keepSynced(true);
        mUsersList = (RecyclerView) mView.findViewById(R.id.userlist);


        mLayoutManager = new LinearLayoutManager(getContext());
        mUsersList.setHasFixedSize(true);
        mAuth = FirebaseAuth.getInstance();
        mUsersList.setLayoutManager(mLayoutManager);
        mCurrentUser = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUser);
        mFriendsDatabase.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
       /* */
        final FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.user_single_layout3,
                UsersViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, final Users model, final int position) {
                final String user_id = getRef(position).getKey();
                if (user_id != null) {
                    UsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            viewHolder.setName(userName);

                            final String userStatus = dataSnapshot.child("status").getValue().toString();
                            viewHolder.setStatus(userStatus);
                            String userThumb = dataSnapshot.child("profile_image").getValue().toString();
                            viewHolder.setImage(userThumb, getContext());
                            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Select Options");
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                Intent chatIntent = new Intent(getContext(), Profile.class);
                                                chatIntent.putExtra("user_id", user_id);
                                                chatIntent.putExtra("user_name", userName);
                                                startActivity(chatIntent);
                                            }
                                            if (which == 1) {
                                               /* Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                chatIntent.putExtra("user_id", user_id);
                                                chatIntent.putExtra("user_name", userName);
                                                startActivity(chatIntent);*/
                                            }
                                        }
                                    });
                                    builder.show();
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
        mUsersList.setAdapter(firebaseRecyclerAdapter);

        UsersDatabase.child(mCurrentUser).child("online").setValue("true");

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {
        View view;

        public UsersViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setName(String name) {
            TextView usersNameView = (TextView) view.findViewById(R.id.user_single_name);
            usersNameView.setText(name);
        }

        public void setStatus(String status) {
            TextView usersStatusView = (TextView) view.findViewById(R.id.messageTextView);
            usersStatusView.setText(status);
        }

        public void setImage(String image, Context context) {
            CircleImageView Image = (CircleImageView) view.findViewById(R.id.user_single_image);
            Picasso.with(context).load(image).placeholder(R.drawable.avatar).into(Image);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        UsersDatabase.child(mCurrentUser).child("online").setValue(ServerValue.TIMESTAMP);
    }

}
