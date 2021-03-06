package com.example.umairali.wyapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Umair Ali on 11/29/2017.
 */

class MessageAdapter2 extends RecyclerView.Adapter<MessageAdapter2.MessageViewHolder> {
    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private final static int Outgoing_VIEW = 0;
    private final static int Ingoing_VIEW = 1;

    public MessageAdapter2(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutRes = 0;
        switch (viewType) {
            case Outgoing_VIEW:
                layoutRes = R.layout.message_single_layout;
                break;
            case Ingoing_VIEW:
                layoutRes = R.layout.message_single_layout2;
                break;
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes,parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText,messangerName,messageTimeText;
        public CircleImageView profileImage;
        public ImageView mMessageImage;
        public TextView messagetime;
        public TextView sent;


        public MessageViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.left_message);
            profileImage = (CircleImageView) view.findViewById(R.id.left_fromUser);
            mMessageImage = (ResizableImageView) view.findViewById(R.id.left_img);
            messagetime = (TextView) view.findViewById(R.id.left_timeAgo);
            messangerName=(TextView)view.findViewById(R.id.name);
            sent=(TextView)view.findViewById(R.id.sentIndecator);


        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);
        mAuth=FirebaseAuth.getInstance();
        String CurrentUser=mAuth.getCurrentUser().getUid();

        String from_user = c.getFrom();
        String message_type=c.getType();
        Long lastTime = c.getTime();


        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.keepSynced(true);
        if (i==Ingoing_VIEW){
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    viewHolder.messangerName.setText(name);
                    final String image = dataSnapshot.child("profile_image").getValue().toString();

                    Picasso.with(viewHolder.profileImage.getContext()).load(image)
                            .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(viewHolder.profileImage.getContext()).load(R.drawable.default_avatar).into(viewHolder.profileImage);
                        }
                    });
                    viewHolder.sent.setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else {
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    viewHolder.messangerName.setText(name);
                    final String image = dataSnapshot.child("profile_image").getValue().toString();


                    Picasso.with(viewHolder.profileImage.getContext()).load(image)
                            .placeholder(R.drawable.default_avatar).into(viewHolder.profileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(viewHolder.profileImage.getContext()).load(R.drawable.default_avatar).into(viewHolder.profileImage);
                        }
                    });
                    viewHolder.sent.setVisibility(View.VISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if (message_type.equals("text")){
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.mMessageImage.setVisibility(View.GONE);
        }else if(message_type.equals("image")) {
            viewHolder.messageText.setVisibility(View.GONE);
            viewHolder.mMessageImage.setVisibility(View.VISIBLE);
            Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.default_avatar).into(viewHolder.mMessageImage);
        }

        //-------------Timing Logic-----------//
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
            viewHolder.messagetime.setText("Yesterday");
        }else if (today.equals(currentdate)){
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
            SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm a");
            Date dt;
            try {
                dt = sdf.parse(time);
                viewHolder.messagetime.setText(sdfs.format(dt));
                // <-- I got result here
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else {
            viewHolder.messagetime.setText(today);
        }


    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Messages c = mMessageList.get(position);
        mAuth=FirebaseAuth.getInstance();
        String CurrentUser=mAuth.getCurrentUser().getUid();

        String from_user = c.getFrom();
        if (from_user.equals(CurrentUser)){
            return Ingoing_VIEW;
        }else {
            return Outgoing_VIEW;
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}

