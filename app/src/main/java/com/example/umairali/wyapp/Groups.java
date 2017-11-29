package com.example.umairali.wyapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import de.hdodenhof.circleimageview.CircleImageView;


public class Groups extends Fragment {

    RecyclerView mMyChannels;
    ImageButton floatingActionButton;
    private DatabaseReference UsersDatabase, mRootRef, UserDatabase, database;
    private FirebaseAuth mAuth;
    String mCurrentUser;
    private LinearLayoutManager mLayoutManager;
    private Query messageQuery;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mychannels = inflater.inflate(R.layout.fragment_groups, container, false);
        mMyChannels = (RecyclerView) mychannels.findViewById(R.id.mychannels);
        mLayoutManager = new LinearLayoutManager(getContext());
        mMyChannels.setHasFixedSize(true);
        mMyChannels.setLayoutManager(mLayoutManager);

        //Firebase References
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();
        UserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser);
        UsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser).child("Groups");
        messageQuery = UsersDatabase.orderByChild("last_message_time");
        UsersDatabase.keepSynced(true);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        floatingActionButton = (ImageButton) mychannels.findViewById(R.id.fabCreatGroup);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CreatGroup.class));
            }
        });
        return mychannels;
    }

    @Override
    public void onStart() {
        UserDatabase.child("online").setValue("true");
        super.onStart();
        FirebaseRecyclerAdapter<Users, AllGroupsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, AllGroupsViewHolder>(
                Users.class,
                R.layout.user_single_layout3,
                AllGroupsViewHolder.class,
                UsersDatabase
        ) {
            @Override
            protected void populateViewHolder(final AllGroupsViewHolder viewHolder, Users model, int position) {
                final String user_id = getRef(position).getKey();
                if (user_id != null) {
                    mRootRef.child("Group_Metadata").child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String groupName = dataSnapshot.child("Group_Name").getValue().toString();
                            viewHolder.setName(groupName);
                            if (dataSnapshot.hasChild("Group_image")) {
                                String groupImage = dataSnapshot.child("Group_image").getValue().toString();
                                viewHolder.setImage(groupImage, getContext());
                            }
                            if (dataSnapshot.hasChild("last_message") && dataSnapshot.hasChild("last_message_time")) {
                                String lastMessage = dataSnapshot.child("last_message").getValue().toString();
                                viewHolder.setStatus(lastMessage);
                                String lasttime = dataSnapshot.child("last_message_time").getValue().toString();
                                long lastTime = Long.parseLong(lasttime);
                                viewHolder.setTime(lastTime);
                            }else {
                                String des = dataSnapshot.child("Group_Description").getValue().toString();
                                viewHolder.setStatus(des);
                            }
                            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent newIntent = new Intent(getContext(), GroupChatActivity.class);
                                    newIntent.putExtra("user_id", user_id);
                                    newIntent.putExtra("Group_Name", groupName);
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
        mMyChannels.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        UserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
    }

    public static class AllGroupsViewHolder extends RecyclerView.ViewHolder {
        View view;

        public AllGroupsViewHolder(View itemView) {
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

        public void setImage(String image, final Context context) {
            final CircleImageView Image = (CircleImageView) view.findViewById(R.id.user_single_image);
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

        public void setTime(long lastTime) {
            TextView lastMessagetime = (TextView) view.findViewById(R.id.timeTextView);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            String formattedDate = formatter.format(lastTime);
            SimpleDateFormat todayformater = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String today = todayformater.format(lastTime);

            StringTokenizer tk = new StringTokenizer(formattedDate);
            String date = tk.nextToken();
            String time = tk.nextToken();

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            dateFormat.format(cal.getTime());

            Calendar calender = Calendar.getInstance();

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String currentdate = df.format(calender.getTime());

            if (formattedDate.equals(dateFormat)){
                lastMessagetime.setText("Yesterday");
            }else if (today.equals(currentdate)){
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
                Date dt;
                try {
                    dt = sdf.parse(time);
                    lastMessagetime.setText(sdfs.format(dt));
                    // <-- I got result here
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }else {
                lastMessagetime.setText(today);
            }
        }
    }

}
