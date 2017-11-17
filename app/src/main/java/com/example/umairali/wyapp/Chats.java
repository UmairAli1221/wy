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


/**
 * A simple {@link Fragment} subclass.
 */
public class Chats extends Fragment {

    private RecyclerView mUsersList;
    private View mView;
    private DatabaseReference UsersDatabase, mRootRef;
    private LinearLayoutManager mLayoutManager;
    private FirebaseAuth mAuth;
    private Query messageQuery;
    String mCurrentUser;
    private ImageButton mButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_chats, container, false);
        mUsersList = (RecyclerView) mView.findViewById(R.id.chatlist);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mUsersList.setHasFixedSize(true);
        mAuth = FirebaseAuth.getInstance();
        mUsersList.setLayoutManager(mLayoutManager);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);
        mCurrentUser = mAuth.getCurrentUser().getUid();

        UsersDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrentUser);
        messageQuery = UsersDatabase.orderByChild("last_message_time");
        UsersDatabase.keepSynced(true);
        return mView;
    }
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(
                Users.class,
                R.layout.user_single_layout3,
                UsersViewHolder.class,
                messageQuery
        ) {
            @Override
            protected void populateViewHolder(final UsersViewHolder viewHolder, final Users model, int position) {


                // viewHolder.setStatus(model.getStatus());
                //viewHolder.setImage(model.getProfile_image(), getContext());
                CircleImageView Image;
                final String user_id;
                user_id = getRef(position).getKey();
                if (user_id != null) {
                    mRootRef.child("Users").child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("name").getValue().toString();
                            viewHolder.setName(userName);
                            String userThumb = dataSnapshot.child("profile_image").getValue().toString();
                            viewHolder.setImage(userThumb, getContext());
                            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //mRootRef.child("Chat").child(mCurrentUser).child(user_id).child("typing").setValue(false);
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("user_id", user_id);
                                    chatIntent.putExtra("user_name", userName);
                                    startActivity(chatIntent);
                                }

                            });
                            mRootRef.child("Chat").child(mCurrentUser).child(user_id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String lastMessage = dataSnapshot.child("last_message").getValue().toString();
                                    viewHolder.setStatus(lastMessage);
                                    String lasttime = dataSnapshot.child("last_message_time").getValue().toString();
                                    long lastTime = Long.parseLong(lasttime);
                                    viewHolder.setTime(lastTime);

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
                    Image = (CircleImageView)viewHolder.view.findViewById(R.id.user_single_image);
                    Image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(viewHolder.view.getContext(),Profile.class);
                            intent.putExtra("user_id", user_id);
                            // intent.putExtra("user_name", userName);
                            viewHolder.view.getContext().startActivity(intent);
                        }
                    });

                }
            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
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


        public void setTime(long sfd) {

            TextView lastMessagetime = (TextView) view.findViewById(R.id.timeTextView);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            String formattedDate = formatter.format(sfd);
            SimpleDateFormat todayformater = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String today = todayformater.format(sfd);

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
