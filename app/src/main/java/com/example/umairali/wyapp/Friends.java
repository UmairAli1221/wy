package com.example.umairali.wyapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class Friends extends AppCompatActivity {

    private Toolbar mTooltbar;

    private View mView;
    private RecyclerView mUsersList;
    private LinearLayoutManager mLayoutManager;
    private DatabaseReference UsersDatabase, mRootRef;
    private FirebaseAuth mAuth;
    String mCurrentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        mTooltbar=(Toolbar)findViewById(R.id.AddFriends_toolbar);
        setSupportActionBar(mTooltbar);
        getSupportActionBar().setTitle("Add Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        UsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersDatabase.keepSynced(true);
        mUsersList = (RecyclerView)findViewById(R.id.contactlist);
        mLayoutManager = new LinearLayoutManager(this);
        mUsersList.setHasFixedSize(true);
        mAuth = FirebaseAuth.getInstance();
        mUsersList.setLayoutManager(mLayoutManager);
        mCurrentUser = mAuth.getCurrentUser().getUid();
        mRootRef = FirebaseDatabase.getInstance().getReference();
    }
    @Override
    public void onStart() {
        super.onStart();
        UsersDatabase.child(mCurrentUser).child("online").setValue("true");

        FirebaseRecyclerAdapter<Users, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, FriendsViewHolder>(
                Users.class,
                R.layout.user_single_layout3,
                FriendsViewHolder.class,
                UsersDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, final Users model, int position) {

                viewHolder.setName(model.getEmail());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(model.getProfile_image(), Friends.this);

                final String user_id = getRef(position).getKey();
                if (user_id != null) {
                    UsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userName = dataSnapshot.child("email").getValue().toString();
                            String userThumb = dataSnapshot.child("profile_image").getValue().toString();

                            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent chatIntent = new Intent(Friends.this, Profile.class);
                                    chatIntent.putExtra("user_id", user_id);
                                    chatIntent.putExtra("user_name", userName);
                                    startActivity(chatIntent);
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
    }
    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View view;

        public FriendsViewHolder(View itemView) {
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);

        if (searchItem != null) {
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    //some operation
                    return false;
                }
            });
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //some operation
                }
            });
            EditText searchPlate = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
            searchPlate.setHint("Search...");
            View searchPlateView = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
            searchPlateView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            // use this method for search process
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // use this method when query submitted
                    Toast.makeText(Friends.this, query, Toast.LENGTH_SHORT).show();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // use this method for auto complete search process
                    Query Q = UsersDatabase.orderByChild("email").startAt(newText);

                    FirebaseRecyclerAdapter<Users, FriendsViewHolder> firebaseRecyclerAdapter22 = new FirebaseRecyclerAdapter<Users, FriendsViewHolder>(
                            Users.class, R.layout.user_single_layout3, FriendsViewHolder.class, Q) {
                        @Override
                        protected void populateViewHolder(final FriendsViewHolder viewHolder, final Users model, int position) {

                            viewHolder.setName(model.getEmail());
                            viewHolder.setImage(model.getProfile_image(),Friends.this);
                            final String user_id = getRef(position).getKey();
                            viewHolder.view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Intent chatIntent = new Intent(Friends.this, Profile.class);
                                    chatIntent.putExtra("user_id", user_id);
                                    startActivity(chatIntent);
                                }
                            });

                        }

                    };
                    mUsersList.setAdapter(firebaseRecyclerAdapter22);
                    return false;
                }
            });
            SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.logout){
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }else if (item.getItemId()==R.id.settings){
            Intent startIntent=new Intent(Friends.this,Settings.class);
            startActivity(startIntent);

        }else if (item.getItemId()==R.id.newGroup){
            Intent startIntent=new Intent(Friends.this,CreatGroup.class);
            startActivity(startIntent);

        }
        return true;
    }

    private void sendToStart() {
        Intent startIntent=new Intent(Friends.this,LoginActivity.class);
        startActivity(startIntent);
        finish();
    }
}
