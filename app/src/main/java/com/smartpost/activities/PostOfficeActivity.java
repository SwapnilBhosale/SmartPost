package com.smartpost.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.smartpost.LoginActivity;
import com.smartpost.entities.ConsignmentStatus;
import com.smartpost.entities.PostMan;
import com.smartpost.entities.PostManClientMap;
import com.smartpost.entities.PostOffice;
import com.smartpost.entities.ReceiverDetails;
import com.smartpost.entities.User;
import com.smartpost.R;
import com.smartpost.services.LogoutService;
import com.smartpost.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PostOfficeActivity extends AppCompatActivity implements LogoutService{

    private ProgressDialog pd = null;

    //Dynamically showing the no of ambulance in city
    private RecyclerView mRecyclerView;
    private MyRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private User user = null;

    private static final String TAG = PostOfficeActivity.class.getSimpleName();

    private Map<String,PostMan> postmans = new LinkedHashMap();
    private List<PostMan> list = new ArrayList();

    DatabaseReference postmanRefernce;
    ChildEventListener postmanListener;

    DatabaseReference mapReference;
    ChildEventListener mapLisener;

    private Map<String,PostManClientMap> map = new HashMap();

    private List<PostManClientMap> mapList = new ArrayList();



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
        addPostManConsignementListener();

        mAdapter.setOnItemClickListener(new MyRecyclerViewAdapter.MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                //list.get(position);
                final String uuid = String.valueOf(getPostmanUUID(position));
                mapList.clear();
                for (Map.Entry<String, PostManClientMap> entry : map.entrySet()) {
                    PostManClientMap p = entry.getValue();

                    if(p.isBelongToPostman(uuid)){
                        updateMapList(p);
                    }
                    openMapsActivity(uuid);
                }
            }
        });
    }

    private Object getPostmanUUID(int index){
       return postmans.keySet().toArray()[index];
    }

    private void updateMapList(PostManClientMap p){
        mapList.add(p);
    }


    private void addPostManConsignementListener(){
        mapReference = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN__CONSIGNMENT_MAP);
        mapLisener =  mapReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                map.put(dataSnapshot.getKey(), dataSnapshot.getValue(PostManClientMap.class));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                map.put(dataSnapshot.getKey(), dataSnapshot.getValue(PostManClientMap.class));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                map.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void addPostmanListener(){

        postmanRefernce = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN_KEY);

        //add all postmans
        postmanListener =  postmanRefernce.addChildEventListener(new ChildEventListener() {
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


    private void openMapsActivity(String uuid){
        Intent intent = new Intent(this,MapsActivity.class);
        intent.putExtra(Constants.FIREBASE_UUID,uuid);

        intent.putExtra(Constants.ACTOR,Constants.ACTOR_POSTOFFICE);

        int consignmentCount =mapList.size();
        intent.putExtra(Constants.CONSIG_LEN,consignmentCount);
        for(int i=1 ; i <= consignmentCount; i++){
            PostManClientMap p = mapList.get((i-1));
            intent.putExtra(Constants.FIREBASE_ADDRSS.concat("_"+i),p.getAddress());
            //intent.putExtra(Constants.CONSIG_NAME.concat("_"+i),p.getConsignmentId());
            ReceiverDetails details = p.getDetails();
            String status = details.getStatus();
            StringBuilder sb = new StringBuilder();
            sb.append("Id : ")
                    .append(p.getConsignmentId())
                    .append(" status : ")
                    .append(status);
            if(status.equalsIgnoreCase(ConsignmentStatus.DELIVERED.toString())){
                sb.append(" Receiver Name : ")
                        .append(details.getName())
                        .append(" Receiver UID : ")
                        .append(details.getUid());
            }
            intent.putExtra(Constants.CONSIG_DETAILS.concat("_"+i),sb.toString());
        }

        startActivity(intent);
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

    }

    @Override
    protected void onDestroy() {
        postmanRefernce.removeEventListener(postmanListener);
        mapReference.removeEventListener(mapLisener);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
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

    @Override
    public void logout() {
        pd.show();

        //clear token

        String uuId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POST_OFFICE_KEY).child(uuId);
        databaseReference.getRef().removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                pd.dismiss();
                openLoginActivity();
            }
        });
    }

    private void openLoginActivity(){
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
