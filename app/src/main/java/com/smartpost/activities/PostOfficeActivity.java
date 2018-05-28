package com.smartpost.activities;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.smartpost.Entity.PostMan;
import com.smartpost.Entity.PostOffice;
import com.smartpost.Entity.User;
import com.smartpost.R;
import com.smartpost.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostOfficeActivity extends AppCompatActivity {

    private ProgressDialog pd = null;

    //Dynamically showing the no of ambulance in city
    private RecyclerView mRecyclerView;
    private MyRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private User user = null;

    private static final String TAG = PostOfficeActivity.class.getSimpleName();

    private Map<String,PostMan> postmans = new HashMap();
    private List<PostMan> list = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_office);

        initSpinnerDialogue();

        user = new PostOffice(FirebaseInstanceId.getInstance().getToken());
        //initializing recycler view
        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerViewAdapter(list, this);
        mRecyclerView.setAdapter(mAdapter);
        uploadDataToServer();

        addPostmanListener();
    }

    private void addPostmanListener(){

        final DatabaseReference postmanListener = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN_KEY);

        //add all postmans
        postmanListener.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                PostMan postman = dataSnapshot.getValue(PostMan.class);
                Log.d(TAG, "onChildAdded: "+postman);
                postmans.put(dataSnapshot.getKey(),postman);
                updatePostmanList();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                postmans.put(dataSnapshot.getKey(),dataSnapshot.getValue(PostMan.class));
                updatePostmanList();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                postmans.remove(dataSnapshot.getKey());
                updatePostmanList();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updatePostmanList(){
        list.clear();

        list = new ArrayList<>(postmans.values());

        mAdapter.setPostmanList(list);
        mAdapter.notifyDataSetChanged();
        Log.d(TAG, "updatePostmanList: "+mAdapter.getItemCount());
    }

    private void uploadDataToServer() {
        String uuId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POST_OFFICE_KEY).child(uuId);

        databaseReference.child(Constants.FIREBASE_TOKEN).setValue(user.getToken());
    }

    private void initSpinnerDialogue() {
        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("Loading...");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.setOnItemClickListener(new MyRecyclerViewAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                //list.get(position).getConsignments();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout :
                pd.show();

                //clear token

                String uuId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN_KEY).child(uuId);

                // push hospital extension
                databaseReference.child(Constants.FIREBASE_TOKEN).setValue("");
                break;
        }
        super.onOptionsItemSelected(item);
        return true;
    }

    private  void deleteFirebaseData(){
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN_KEY).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        }
    }
}
