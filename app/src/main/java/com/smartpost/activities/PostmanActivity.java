package com.smartpost.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.smartpost.core.SmartPostApplication;
import com.smartpost.entities.Client;
import com.smartpost.entities.ConsignmentStatus;
import com.smartpost.entities.PostManClientMap;
import com.smartpost.R;
import com.smartpost.core.ApplicationSetting;
import com.smartpost.entities.PostOffice;
import com.smartpost.entities.ReceiverDetails;
import com.smartpost.services.LocationService;
import com.smartpost.services.LogoutService;
import com.smartpost.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostmanActivity extends AppCompatActivity implements LocationListener,LogoutService {

    private ProgressDialog pd = null;
    private LocationService locationService;
    private Intent serviceIntent;

    private static final String TAG = PostmanActivity.class.getSimpleName();

    private Button showConsignments;
    private Button endJourney;
    private Button scanQR;

    private IntentIntegrator qrScan;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    private LocationManager locationManager;
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1; // 1 minute
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private boolean canGetLocation = false;
    private Location location;
    private double latitude;
    private double longitude;
    private DatabaseReference mapListener;

    private Map<String,PostManClientMap> map = new HashMap();

    private Map<String,PostManClientMap> allMap = new HashMap();

    private List<PostManClientMap> mapList = new ArrayList();

    private ListView listViewPostman;
    private CustomListAdapter adapter;

    private  ChildEventListener eventListener;

    private String emailId = ApplicationSetting.getInstance().getUserEmail();

    private final int MY_PERMISSIONS_REQUEST_GPS = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postman);
        initUi();
        initSpinnerDialogue();

        //endJourney.setEnabled(false);

        listViewPostman.setEmptyView(findViewById(R.id.empty_list_item_postman));
        adapter = new CustomListAdapter(this,R.layout.list_item,mapList);
        listViewPostman.setAdapter(adapter);
        addListeners();
        //intializing scan object
        qrScan = new IntentIntegrator(this);

        location = fetchCurrentLocation();
        //pd.show();
    }

    private  void addListeners(){
        listViewPostman.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PostManClientMap p = mapList.get(i);
                if(p.getDetails().getStatus().equalsIgnoreCase(ConsignmentStatus.DELIVERED.toString()))
                    Toast.makeText(PostmanActivity.this,"Consignment is already Delivered !",Toast.LENGTH_SHORT).show();
                else {
                    openMapsActivity(p.getUuid(), p.getAddress(), p.getConsignmentId());
                }
            }
        });
    }

    private void openMapsActivity(String uuid,String address,String id){
        Intent intent = new Intent(this,MapsActivity.class);
        intent.putExtra(Constants.FIREBASE_UUID,uuid);

        intent.putExtra(Constants.ACTOR,Constants.ACTOR_POSTMAM);
        intent.putExtra(Constants.CONSIG_INDEX,id);

        int consignmentCount =mapList.size();
        intent.putExtra(Constants.CONSIG_LEN,consignmentCount);
        for(int i=1 ; i <= consignmentCount; i++){
            PostManClientMap p = mapList.get((i-1));
            intent.putExtra(Constants.FIREBASE_ADDRSS.concat("_"+i),p.getAddress());
            intent.putExtra(Constants.CONSIG_NAME.concat("_"+i),p.getConsignmentId());
        }

        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void dismissPD(){
        if(pd != null && pd.isShowing())
            pd.dismiss();
    }




    private void addPostManConsignementListener(){
       mapListener = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN__CONSIGNMENT_MAP);
       final String uuId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventListener =  mapListener.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PostManClientMap p = dataSnapshot.getValue(PostManClientMap.class);
                allMap.put(dataSnapshot.getKey(), p);
                if(p.isBelongToPostman(uuId)) {
                    map.put(dataSnapshot.getKey(), p);
                    updateMapList();
                }
                dismissPD();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                PostManClientMap p = dataSnapshot.getValue(PostManClientMap.class);
                allMap.put(dataSnapshot.getKey(), p);
                if(p.isBelongToPostman(uuId)) {
                    map.put(dataSnapshot.getKey(), p);
                    updateMapList();
                }
                dismissPD();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                PostManClientMap p = dataSnapshot.getValue(PostManClientMap.class);
                allMap.remove(dataSnapshot.getKey());
                if(p.isBelongToPostman(uuId)) {
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
    protected void onPause() {
        super.onPause();
        mapListener.removeEventListener(eventListener);
        //clientReference.removeEventListener(clientListener);
        //postOfficeReference.removeEventListener(postOfficeListener);

    }

    private void updateMapList(){
        mapList.clear();
        adapter.clear();
        mapList = new ArrayList(map.values());
        Log.d(TAG, "updateMapList: Size : "+mapList.size());
        adapter.setList(mapList);
        //adapter.notifyDataSetChanged();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_GPS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Request granted to GPS");
                } else{
                    Log.d(TAG, "onRequestPermissionsResult: Permission is not granted");
                }
             }
             return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /*fetch current location of patient*/
    public Location fetchCurrentLocation() {

        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            // first request location updates from both the providers
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_GPS);
                return null;
            }else{
                Log.d(TAG, "fetchCurrentLocation: Already have permission");
            }
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Toast.makeText(this, "Please turn on GPS", Toast.LENGTH_SHORT).show();
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {

                    // resetting updates
                    locationManager.removeUpdates(this);
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }


            }

        } catch (Exception e) {
            e.printStackTrace();

            // stopSelf();
        }


        return location;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu1, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout :
                logout();
                break;
            case R.id.action_scan_qr :
                qrScan.initiateScan();
                break;
        }
        super.onOptionsItemSelected(item);
        return true;
    }

    private void openLoginActivity(){
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public void onBackPressed() {
//        /super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        //deleteFirebaseData();
        this.stopService(serviceIntent);
        FirebaseAuth.getInstance().signOut();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationService = new LocationService();
        serviceIntent = new Intent(this,LocationService.class);
        this.startService(serviceIntent);
        uploadDataToServer();
        addPostManConsignementListener();
       // addClientListener();
        //addPostOfficeListener();
    }


    private void uploadConsignmentDate(PostManClientMap map){
        final DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN__CONSIGNMENT_MAP);
        String key = databaseReference.push().getKey();
        final DatabaseReference newRef = databaseReference.child(key);
        Log.d(TAG, "uploadConsignmentDate: adding new entry : "+map.getUuid()+" email : "+map.getEmailId());
        newRef.setValue(map);

    }

    /*
     * upload sip extension and firebase token to our database
     * */
    private void uploadDataToServer() {

        String uuId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN_KEY).child(uuId);


        // push token
        databaseReference.child(Constants.FIREBASE_TOKEN).setValue(FirebaseInstanceId.getInstance().getToken());

        //push lat long
        //databaseReference.child(Constants.FIREBASE_LAT).setValue(0.0);
        //databaseReference.child(Constants.FIREBASE_LONG).setValue(0.0);
        databaseReference.child(Constants.FIREBASE_EMAIL).setValue(ApplicationSetting.getInstance().getUserEmail());
        /*databaseReference.child(Constant.FIREBASE_ACTOR_DATA).setValue(ambulance.getUserData());
        databaseReference.child(Constant.FIREBASE_AMBULANCE_STATUS).setValue(Constant.Ambulance_Status.Free.toString());
        databaseReference.child(Constant.FIREBASE_UUID).setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.child(Constant.FIREBASE_HAS_PICKED_UP_PATIENT).setValue("NO");

        databaseReference.child(Constant.FIREBASE_IS_TRACK_FINISHED).setValue("NO");
        databaseReference.child(Constant.FIREBASE_AMBULANCE_STATUS).setValue("FREE");*/


    }


    private void initSpinnerDialogue() {
        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("Loading...");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    }

    private void initUi(){
        /*showConsignments = findViewById(R.id.showConsignments);
        endJourney = findViewById(R.id.endJourney);
        scanQR = findViewById(R.id.scanQR);*/

        listViewPostman = findViewById(R.id.listViewPostman);
        qrScan = new IntentIntegrator(PostmanActivity.this);
       /* scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PostmanActivity.this,"Clicked on scan QR",Toast.LENGTH_SHORT).show();
                //intializing scan object


            }
        });*/
    }

    private void removeOldAssignmentIfAssigned(String id){

        Log.d(TAG, "removeOldAssignmentIfAssigned: id "+id);

        for (final Map.Entry<String, PostManClientMap> entry : allMap.entrySet()) {
            PostManClientMap p = entry.getValue();
            Log.d(TAG, "removeOldAssignmentIfAssigned: MapId "+p.getConsignmentId());
            if(p.getConsignmentId().equalsIgnoreCase(id)){
                FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN__CONSIGNMENT_MAP).child(entry.getKey()).getRef().removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Log.d(TAG, "onComplete: Data removed for key :"+entry.getKey());
                    }
                });
                break;
            }
            // ...
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan QR failed", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                try {


                    //converting the data to json
                    JSONObject obj = new JSONObject(result.getContents());
                    //Toast.makeText(this, obj.toString(), Toast.LENGTH_LONG).show();
                    PostManClientMap postManClientMap = new PostManClientMap(obj.getString("email"),FirebaseAuth.getInstance().getCurrentUser().getUid());
                    postManClientMap.setAddress(obj.getString("address"));

                    String consignmentId = obj.getString("consignmentId");
                    postManClientMap.setConsignmentId(consignmentId);


                    removeOldAssignmentIfAssigned(consignmentId);
                    //set default status to assigned when rq code is scanned
                    ReceiverDetails details = new ReceiverDetails();
                    details.setStatus(ConsignmentStatus.POSTMAN_ASSIGNED.toString());
                    postManClientMap.setDetails(details);
                    uploadConsignmentDate(postManClientMap);
                    Log.d(TAG, "onActivityResult: consignment data uploaded");

                    //sendCloudPush(postManClientMap.getEmailId());

                } catch (JSONException e) {
                    e.printStackTrace();
                    //if control comes here
                    //that means the encoded format not matches
                    //in this case you can display whatever data is available on the qrcode
                    //to a toast
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void sendCloudPush(String email) throws JSONException{
        JSONObject parahJson = new JSONObject();
        JSONObject dataObject = new JSONObject();
        JSONArray tokenJsonArray = new JSONArray();

        String token = null;
        for (Map.Entry<String, Client> entry : clientMap.entrySet()) {
            Client client = entry.getValue();
            if(client.getEmail().equalsIgnoreCase(email)){
                token = client.getToken();
            }
        }
        if(token == null)
            return;
        tokenJsonArray.put(token);
        dataObject.put(Constants.FIREBASE_NOTFN_ACTION, Constants.FIREBASE_ACTION_CONSG_ASSIGNED);

        JSONObject notificationJsonObject = new JSONObject();
        notificationJsonObject.put("title","Help Me");
        notificationJsonObject.put("body","Help Me");

        notificationJsonObject.put("sound", "default");

        parahJson.put("notification", notificationJsonObject);
        parahJson.put("data", dataObject);
        parahJson.put("registration_ids", tokenJsonArray);

        ((SmartPostApplication)getApplication()).sendCloudNotification(parahJson);


    }

    @Override
    public void onLocationChanged(Location location) {
        uploadPostmanLocation(location);
    }

    private void uploadPostmanLocation(Location location) {


        /*patient.setLatitude(location.getLatitude());
        patient.setLongitude(location.getLongitude());*/

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase fbDatabase = FirebaseDatabase.getInstance();

        fbDatabase.getReference().child(Constants.FIREBASE_POSTMAN_KEY).child(user.getUid())
                .child(Constants.FIREBASE_LAT).setValue(location.getLatitude());

        fbDatabase.getReference().child(Constants.FIREBASE_POSTMAN_KEY).child(user.getUid())
                .child(Constants.FIREBASE_LONG).setValue(location.getLongitude());


        // we dont want continuous updates. remove it once you get
        //locationManager.removeUpdates(this);


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void logout() {
        pd.show();

        //clear token

        String uuId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Do we need to remove the mapping ?
        for (Map.Entry<String, PostManClientMap> entry : map.entrySet()) {
            PostManClientMap p = entry.getValue();
            if(p.isBelongToPostman(uuId)){
                FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN__CONSIGNMENT_MAP).child(entry.getKey()).getRef().removeValue();
            }
            // ...
        }
        DatabaseReference databaseReference =  FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN_KEY).child(uuId);

        databaseReference.getRef().removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                pd.dismiss();
                FirebaseAuth.getInstance().signOut();
                openLoginActivity();
            }
        });
    }

    private DatabaseReference clientReference;
    private ChildEventListener clientListener;

    private Map<String,Client> clientMap = new HashMap<>();

    private void addClientListener(){
        clientReference = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CLIENT_KEY);
        clientListener =  clientReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                clientMap.put(dataSnapshot.getKey(),dataSnapshot.getValue(Client.class));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                clientMap.put(dataSnapshot.getKey(),dataSnapshot.getValue(Client.class));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                clientMap.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private DatabaseReference postOfficeReference;
    private ChildEventListener postOfficeListener;

    private Map<String,PostOffice> postOfficeMap = new HashMap<>();

    private void addPostOfficeListener(){
        postOfficeReference = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POST_OFFICE_KEY);
        postOfficeListener =  postOfficeReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                postOfficeMap.put(dataSnapshot.getKey(),dataSnapshot.getValue(PostOffice.class));
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                postOfficeMap.put(dataSnapshot.getKey(),dataSnapshot.getValue(PostOffice.class));
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                postOfficeMap.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public class CustomListAdapter extends ArrayAdapter<PostManClientMap> {

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


        public CustomListAdapter(Context context, int layoutResourceId, List<PostManClientMap> list) {
            super(context, layoutResourceId, list);
            this.layoutResourceId = layoutResourceId;
            this.list = list;
            this.context = context;
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
           MapHolder holder = null;
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new MapHolder();
            holder.textView1 = row.findViewById(R.id.textView1);
            holder.textView2 = row.findViewById(R.id.textView2);
            holder.textView3 = row.findViewById(R.id.textView3);
            holder.textView4 = row.findViewById(R.id.textView4);
            holder.receivedByLayout = row.findViewById(R.id.receivedByLayout);

            row.setTag(holder);

            PostManClientMap p = list.get(position);
            holder.textView1.setText(p.getConsignmentId());
            holder.textView2.setText(p.getAddress());
            ReceiverDetails details = p.getDetails();
            String status = details.getStatus();
            holder.textView3.setText(status);
            String s = p.getDetails().getStatus();
            if(s.equalsIgnoreCase(ConsignmentStatus.DELIVERED.toString())){

                Log.d(TAG, "getView: Set to visible");
                    holder.receivedByLayout.setVisibility(View.VISIBLE);
                    //holder.receivedByLayout.invalidate();
                    holder.textView4.setText("Name : "+details.getName()+"\nAadhar UID : "+details.getUid()+"\nDOB : "+details.getDob());
                    holder.textView3.setTextColor(Color.parseColor("#058933"));
            }else if(s.equalsIgnoreCase(ConsignmentStatus.POSTMAN_ASSIGNED.toString())){
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


        public void setList(List<PostManClientMap> list) {
            this.list.clear();
            this.list.addAll(list);
            this.notifyDataSetChanged();
        }


    }
}
