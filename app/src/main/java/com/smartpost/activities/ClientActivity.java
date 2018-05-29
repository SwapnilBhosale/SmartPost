package com.smartpost.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.smartpost.entities.PostManClientMap;
import com.smartpost.LoginActivity;
import com.smartpost.R;
import com.smartpost.core.ApplicationSetting;
import com.smartpost.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientActivity extends AppCompatActivity {

    private static final String TAG = ClientActivity.class.getSimpleName();

    private ProgressDialog pd = null;
    private DatabaseReference mapListener;

    private Map<String,PostManClientMap> map = new HashMap();

    private List<PostManClientMap> mapList = new ArrayList();

    private  ChildEventListener eventListener;

    private ListView listView;
    private CustomListAdapter adapter;

    private String emailId = ApplicationSetting.getInstance().getUserEmail();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        initUi();
        initSpinnerDialogue();

        adapter = new CustomListAdapter(this,R.layout.list_item,mapList);
        listView.setAdapter(adapter);
        addListeners();
        //Toast.makeText(this,"In Client Activity",Toast.LENGTH_SHORT).show();
    }

    private  void addListeners(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PostManClientMap p = mapList.get(i);
                //got uuid of postman//
                openMapsActivity(p.getUuid(),p.getAddress(),p.getConsignmentId());
            }
        });
    }

    private void openMapsActivity(String uuid,String address,String id){
        Intent intent = new Intent(this,MapsActivity.class);
        intent.putExtra(Constants.FIREBASE_UUID,uuid);
        intent.putExtra(Constants.FIREBASE_ADDRSS,address);
        intent.putExtra(Constants.ACTOR,Constants.ACTOR_CLIENT);
        intent.putExtra(Constants.CONSIG_NAME,id);
        startActivity(intent);
    }

    private void initUi(){
        listView = findViewById(R.id.listView);
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
        uploadDataToServer();
        addConsignmentListener();
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    private void addConsignmentListener(){
        mapListener = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN__CONSIGNMENT_MAP);
        eventListener = mapListener.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PostManClientMap p = dataSnapshot.getValue(PostManClientMap.class);
                Log.d(TAG, "onChildAdded: "+p.toString());
                if(p.isBelongToClient(emailId)) {
                    Log.d(TAG, "onChildAdded: Belong");
                    map.put(dataSnapshot.getKey(),p);
                    updateMapList();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PostManClientMap p = dataSnapshot.getValue(PostManClientMap.class);
                if(p.isBelongToClient(emailId)) {
                    map.put(dataSnapshot.getKey(),p);
                    updateMapList();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                PostManClientMap p = dataSnapshot.getValue(PostManClientMap.class);
                if(p.isBelongToClient(ApplicationSetting.getInstance().getUserEmail())) {
                    map.remove(dataSnapshot.getKey());
                    updateMapList();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapListener.removeEventListener(eventListener);
    }

    private void updateMapList(){
        mapList.clear();
        adapter.clear();
        mapList = new ArrayList(map.values());
        Log.d(TAG, "updateMapList: Size : "+mapList.size());
        adapter.setList(mapList);
        adapter.notifyDataSetChanged();

    }


    /*
     * upload sip extension and firebase token to our database
     * */
    private void uploadDataToServer() {

        String uuId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CLIENT_KEY).child(uuId);


        // push token
        databaseReference.child(Constants.FIREBASE_TOKEN).setValue(FirebaseInstanceId.getInstance().getToken());
        databaseReference.child(Constants.FIREBASE_LAT).setValue(0.0);
        databaseReference.child(Constants.FIREBASE_LONG).setValue(0.0);
        databaseReference.child(Constants.FIREBASE_EMAIL).setValue(ApplicationSetting.getInstance().getUserEmail());


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout :
                pd.show();

                //clear token

                String uuId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CLIENT_KEY).child(uuId);
                databaseReference.getRef().removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        pd.dismiss();
                        openLoginActivity();
                    }
                });
                break;
        }
        super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    private void openLoginActivity(){
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private  void deleteFirebaseData(){
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN_KEY).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        }
    }


    public class CustomListAdapter extends ArrayAdapter<PostManClientMap>{

        @Override
        public int getCount() {
            return list.size();
        }

        @Nullable
        @Override
        public PostManClientMap getItem(int position) {
            return this.list.get(position);
        }

        private Context context;
        private List<PostManClientMap> list;
        int layoutResourceId;



        public CustomListAdapter(Context context,int layoutResourceId,List<PostManClientMap> list){
            super(context,layoutResourceId,list);
            this.layoutResourceId = layoutResourceId;
            this.list = list;
            this.context = context;
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            MapHolder holder = null;
            if(row == null){
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row  = inflater.inflate(layoutResourceId,parent,false);
                holder = new MapHolder();
                holder.textView1 = row.findViewById(R.id.textView1);
                holder.textView2 = row.findViewById(R.id.textView2);
                row.setTag(holder);
            }else{
                holder = (MapHolder)row.getTag();
            }
            PostManClientMap p = list.get(position);
            Log.d(TAG, "getView: "+p.toString());
            holder.textView1.setText("Consignement Id : "+p.getConsignmentId());
            holder.textView2.setText("Delivery Address : "+p.getAddress());
           return row;
        }


        class MapHolder {
            TextView textView1;
            TextView textView2;
        }


        public void setList(List<PostManClientMap> list){
            this.list.clear();
            this.list.addAll(list);
            this.notifyDataSetChanged();
        }


    }

    @Override
    protected void onDestroy() {
        deleteFirebaseData();
        super.onDestroy();

    }
}
