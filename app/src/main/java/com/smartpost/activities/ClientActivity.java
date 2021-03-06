package com.smartpost.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.smartpost.entities.ConsignmentStatus;
import com.smartpost.entities.PostManClientMap;
import com.smartpost.R;
import com.smartpost.core.ApplicationSetting;
import com.smartpost.entities.ReceiverDetails;
import com.smartpost.services.LogoutService;
import com.smartpost.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientActivity extends AppCompatActivity implements LogoutService {

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

        listView.setEmptyView(findViewById(R.id.empty_list_item_client));
        adapter = new CustomListAdapter(this,R.layout.list_item,mapList);
        listView.setAdapter(adapter);
        addListeners();
        //pd.show();
        //Toast.makeText(this,"In Client Activity",Toast.LENGTH_SHORT).show();
    }

    private  void addListeners(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PostManClientMap p = mapList.get(i);
                //got uuid of postman//
                if(!p.getDetails().getStatus().equalsIgnoreCase(ConsignmentStatus.DELIVERED.toString()))
                    openMapsActivity(p.getUuid(),p.getAddress(),p.getConsignmentId(),p.getPhone());
                else
                    Toast.makeText(ClientActivity.this,"Can not track already delivered consignment !",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMapsActivity(String uuid,String address,String id,String phone){
        Intent intent = new Intent(this,MapsActivity.class);
        intent.putExtra(Constants.FIREBASE_UUID,uuid);
        intent.putExtra(Constants.FIREBASE_ADDRSS,address);
        intent.putExtra(Constants.ACTOR,Constants.ACTOR_CLIENT);
        intent.putExtra(Constants.CONSIG_NAME,id);
        intent.putExtra(Constants.POSTMAN_PHONE,phone);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
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

    private void dismissPD(){
        if(pd != null && pd.isShowing())
            pd.dismiss();
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
                dismissPD();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PostManClientMap p = dataSnapshot.getValue(PostManClientMap.class);
                if(p.isBelongToClient(emailId)) {
                    map.put(dataSnapshot.getKey(),p);
                    updateMapList();
                }
                dismissPD();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                PostManClientMap p = dataSnapshot.getValue(PostManClientMap.class);
                if(p.isBelongToClient(ApplicationSetting.getInstance().getUserEmail())) {
                    map.remove(dataSnapshot.getKey());
                    updateMapList();
                }
                dismissPD();
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
    public void onBackPressed() {
        //super.onBackPressed();
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
        databaseReference.child(Constants.FIREBASE_EMAIL).setValue(ApplicationSetting.getInstance().getUserEmail());


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout :
                logout();
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
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private  void deleteFirebaseData(){
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CLIENT_KEY).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
        }
    }

    @Override
    public void logout() {
        pd.show();

        //clear token

        String uuId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CLIENT_KEY).child(uuId);
        databaseReference.getRef().removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                pd.dismiss();
                FirebaseAuth.getInstance().signOut();
                openLoginActivity();
            }
        });
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
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row  = inflater.inflate(layoutResourceId,parent,false);
            holder = new MapHolder();
            holder.textView1 = row.findViewById(R.id.textView1);
            holder.textView2 = row.findViewById(R.id.textView2);
            holder.textView3 = row.findViewById(R.id.textView3);
            holder.textView4 = row.findViewById(R.id.textView4);
            holder.receivedByLayout = row.findViewById(R.id.receivedByLayout);
            row.setTag(holder);


            PostManClientMap p = list.get(position);
            Log.d(TAG, "getView: "+p.toString());
            holder.textView1.setText(p.getConsignmentId());
            holder.textView2.setText(p.getAddress());
            String status = p.getDetails().getStatus();
            holder.textView3.setText(status);

            ReceiverDetails d = p.getDetails();
            if(status.equalsIgnoreCase(ConsignmentStatus.DELIVERED.toString())) {
                holder.receivedByLayout.setVisibility(View.VISIBLE);
                holder.textView4.setText(d.getName());
                holder.textView3.setTextColor(Color.parseColor("#058933"));
            }else if(status.equalsIgnoreCase(ConsignmentStatus.POSTMAN_ASSIGNED.toString())){
                holder.textView3.setTextColor(Color.parseColor("#eddf1c"));
            }else{
                holder.textView3.setTextColor(Color.parseColor("#ce0a31"));
            }
           return row;
        }


        class MapHolder {
            TextView textView1;
            TextView textView2;
            TextView textView3;
            TextView textView4;
            LinearLayout receivedByLayout;
        }


        public void setList(List<PostManClientMap> list){
            this.list.clear();
            this.list.addAll(list);
            this.notifyDataSetChanged();
        }


    }

    @Override
    protected void onDestroy() {
        //deleteFirebaseData();
        FirebaseAuth.getInstance().signOut();
        super.onDestroy();

    }
}
