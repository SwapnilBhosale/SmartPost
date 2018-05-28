package com.smartpost.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smartpost.Entity.PostMan;
import com.smartpost.R;
import com.smartpost.utils.Constants;
import com.smartpost.utils.HttpConnection;
import com.smartpost.utils.PathJsonParser;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener,OnMapReadyCallback,ValueEventListener {


    private Marker consignmentMarker;
    private GoogleMap mMap;
    private LatLng fromPosition = null;
    private LatLng toPosition = null;
    private LatLng currentLatLong ;
    private LatLng postmanLatLng;
    private String ambulanceName;
    private static String TAG = MapsActivity.class.getSimpleName();
    private Marker marker;
    private Marker patientmarker;
    private  String ambulanceUUID;
    private  String patientUUID;
    private String p_ambulanceUUID;
    private Marker postmanMarker;
    private Button ambulanceBtn;


    private static final LatLng LOWER_MANHATTAN = new LatLng(40.722543,
            -73.998585);
    private static final LatLng BROOKLYN_BRIDGE = new LatLng(40.7057, -73.9964);
    private static final LatLng WALL_STREET = new LatLng(40.7064, -74.0094);
    private LatLng patientLatlng;
    private Polyline polyLine;
    private LatLng hospitalLatLong;
    private Marker hospitalMarker;

    private TextView bottomText;
    private Button conferenceCallBtn;
    private Button endJourneyBtn;

    private String Actor;
    private String PATIENT_ACTOR = "patient";
    private String AMBULANCE_ACTOR = "ambulance";
    private String HOSPITAL_ACTOR = "hospital";
    private String TRAFFIC_ACTOR = "traffic";

    private LatLng postManLatLng;

    private LatLng consignmentLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        bottomText = (TextView) findViewById(R.id.maps_bottom_text);
        ambulanceBtn = (Button) findViewById(R.id.ambulance_btn);
        conferenceCallBtn = (Button) findViewById(R.id.btn_conference_hosp);
        endJourneyBtn = (Button) findViewById(R.id.btn_end_journey);


        addGoogleMap();

     //   TrafficControlActivity.trafficInstance.getTrafficRef(this);


    }

    private void addMarker(LatLng latLng,String name){
        Log.d(TAG, "addMarker Consignment: "+latLng.latitude+" name : "+name);
      mMap.addMarker(new MarkerOptions().position(latLng).title(name));
    }

    private void addMarkers() {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(BROOKLYN_BRIDGE)
                    .title("First Point"));
            mMap.addMarker(new MarkerOptions().position(LOWER_MANHATTAN)
                    .title("Second Point"));
            mMap.addMarker(new MarkerOptions().position(WALL_STREET)
                    .title("Third Point"));
        }
    }

    private void checkIntent() {
        Intent intent = getIntent();



        if(getIntent().hasExtra(Constants.ACTOR))
        {


            String actor = intent.getStringExtra(Constants.ACTOR);
           // do the patient related stuff in this if else
            if(actor.equalsIgnoreCase(Constants.ACTOR_POSTMAM))
           {
              /* Actor = PATIENT_ACTOR;

               bottomText.setVisibility(View.VISIBLE);
               bottomText.setText("Waiting For Ambulance");

               patientUUID=intent.getStringExtra(Constant.FIREBASE_UUID);
               System.out.println("UUID"+patientUUID);
               // trackAmbulance(patientUUID);
               FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_PATIENT_KEY).child(patientUUID).addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {

                       Patient post = dataSnapshot.getValue(Patient.class);
                       System.out.println("ActorData"+post.getActorData());

                       p_ambulanceUUID=post.getassignedAmbulanceUUID();

                       // track Patient
                       trackPatient(post.getLatitude(),post.getLongitude(),post.getUserData());

                       if(p_ambulanceUUID!=null) {
                           System.out.println("POST"+p_ambulanceUUID);
                           trackAmbulance(p_ambulanceUUID);


                       }

                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                    }
                });
*/

            }  else if (actor.equalsIgnoreCase(Constants.ACTOR_POSTOFFICE)) { //// do the traffic related stuff in this if else
              /*  ambulanceUUID = intent.getStringExtra(Constant.FIREBASE_UUID);
                trackAmbulance(ambulanceUUID);

                trackPatient(intent.getExtras().getDouble("TrPatientlat"),intent.getExtras().getDouble("TrPatientlong"),intent.getExtras().getString("TrPateintname"));

                Actor = TRAFFIC_ACTOR;*/

           }
           /* // do the ambulance related stuff in this if else
           else if(actor.equalsIgnoreCase(Constant.ACTOR_AMBULANCE))
            {

                Actor = AMBULANCE_ACTOR;

               // endJourneyBtn.setVisibility(View.VISIBLE);

                ambulanceBtn.setVisibility(View.VISIBLE);
                trackAmbulance(FirebaseAuth.getInstance().getCurrentUser().getUid());
                trackPatient(intent.getExtras().getDouble("patientlat"),intent.getExtras().getDouble("patientlong"),intent.getExtras().getString("patientName"));
                // btn when actor is ambulance
                ambulanceBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_AMBULANCE_KEY)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constant.FIREBASE_HAS_PICKED_UP_PATIENT)
                                .setValue("YES");

                        Button btn = (Button) v;
                        btn.setVisibility(View.GONE);

                    }
                });

                endJourneyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                     // remove
                        FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_AMBULANCE_KEY)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                child(Constant.FIREBASE_ASSIGNED_PATIENT_UUID).removeValue();

                        FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_AMBULANCE_KEY)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                child(Constant.FIREBASE_ASSIGNED_HOSPITAL_UUID).
                                removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_AMBULANCE_KEY)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                        child(Constant.FIREBASE_AMBULANCE_STATUS).setValue("FREE");

                                // end the journey
                                FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_AMBULANCE_KEY)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(Constant.FIREBASE_IS_TRACK_FINISHED)
                                        .setValue("YES");

                            }
                        });


                        if(patientUUID!=null) {
                            FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_PATIENT_KEY)
                                    .child(patientUUID).
                                    child(Constant.FIREBASE_ASSIGNED_AMBULANCE_UUID).removeValue();
                        }





                    }
                });



            }*/
            //// do the client related stuff in this if else
           else if (actor.equalsIgnoreCase(Constants.ACTOR_CLIENT)) {
                //ambulanceUUID = intent.getStringExtra(Constant.AMBULANCE_UUID);
                //patientUUID = intent.getStringExtra(Constant.PATIENT_UUID);

                String postmanUUID = intent.getStringExtra(Constants.FIREBASE_UUID);
                String address = intent.getStringExtra(Constants.FIREBASE_ADDRSS);
                String id = intent.getStringExtra(Constants.CONSIG_NAME);
                Log.d(TAG, "checkIntent: postmanUUID : "+postmanUUID);
                addConsignmentMarker(address,id);
                fetchPostmanDetail(postmanUUID);

               //addPatientMarker(ambulanceUUID);





                /*conferenceCallBtn.setVisibility(View.VISIBLE);
                conferenceCallBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(trackedAmbulance!=null)
                        {
                            //hard coded traffic police ext on lab146
                            conferenceCall(trackedAmbulance.getExtension(),""+4578);
                        }

                    }
                });*/
                //trackAmbulance(ambulanceUUID);


            }
        }
    }

   /* private void getPatientDataAndAddMarker(final String patientID,final String ambulanceUUID){

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_PATIENT_KEY).child(patientID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Patient patient = dataSnapshot.getValue(Patient.class);
                Log.d(TAG, "onDataChange: Patient Data : "+patient.toString());
                String patientData = patient.getActorData();
                trackPatient(patient.getLatitude(),patient.getLongitude(),patientData);
                trackAmbulance(ambulanceUUID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }*/

    private void fetchPostmanDetail(String uuid){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_POSTMAN_KEY).child(uuid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                PostMan p = dataSnapshot.getValue(PostMan.class);
                Log.d(TAG, "onDataChange: PostMan Data : "+p.toString());
                postManLatLng = new LatLng(p.getLatitude(),p.getLongitude());
                trackPostman(p.getLatitude(),p.getLongitude(),p.getEmail());
                drawPathFromOriginToDestination(postManLatLng,consignmentLatLng);

                //trackAmbulance(ambulanceUUID);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void addConsignmentMarker(String address,String id){
        consignmentLatLng= getLocationFromAddress(this,address);
        addMarker(consignmentLatLng,id);
    }

    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        Log.d(TAG, "getLocationFromAddress: "+strAddress+"  latlong : "+p1);

        return p1;
    }


    /*private void addPatientMarker(final String ambulanceUUID){

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_AMBULANCE_KEY).child(ambulanceUUID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

              try {
                  Ambulance ambulance = dataSnapshot.getValue(Ambulance.class);
                  Log.d(TAG, "onDataChange: Ambulance Data : " + ambulance);
                  getPatientDataAndAddMarker(ambulance.getAssignedPatientUUID(), ambulanceUUID);
              }
              catch(Exception ex)
                {

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }*/


    private void trackPostman(Double latitude,Double longitude,String data) {

        postmanLatLng = new LatLng(latitude,longitude);

        if(postmanMarker!=null)
        {
            postmanMarker.remove();
        }

       // BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ambulance);


        postmanMarker = mMap.addMarker(new MarkerOptions().position(postmanLatLng).title(data));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postmanLatLng,14));
    }

    /*private void trackAmbulance(String UUID) {

        FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_AMBULANCE_KEY).child(UUID).addValueEventListener(this);
    }*/

    private void addGoogleMap(){
        if(mMap == null) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        checkIntent();
    }

    public void drawPathFromOriginToDestination(LatLng origin,LatLng destination)
    {
        String url = getMapsApiDirectionsUrl(origin,destination);
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin,
                13));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.i("GoogleMapActivity", "onMarkerClick");
        Toast.makeText(getApplicationContext(),
                "Marker Clicked: " + marker.getTitle(), Toast.LENGTH_LONG)
                .show();
        return false;
    }


    public void newLocation(Double lat,Double lon){
        //got new lat long
        if(marker == null){
            mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lon)).title(ambulanceName));
        }else{
            marker.setPosition(new LatLng(lat,lon));
        }
    }

    @Override
    public void onBackPressed() {

//        TrafficControlActivity.trafficInstance.setTrafficRefToNull();
//        super.onBackPressed();

        super.onBackPressed();

        goBackToHomeScreen();

       // finish();
    }

    @Override
    protected void onDestroy() {

//        if(TrafficControlActivity.trafficInstance.isTrafficReffNull())
//           TrafficControlActivity.trafficInstance.setTrafficRefToNull();


      /*  if(ambulanceUUID!=null) {

            FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_AMBULANCE_KEY).child(ambulanceUUID).removeEventListener(this);
        }*/
        mMap.clear();
        super.onDestroy();
    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        /*// for patient actor
        bottomText.setText("Ambulance Alloted");

        try {
         trackedAmbulance = dataSnapshot.getValue(Ambulance.class);


    LatLng ambulanceLatLng = new LatLng(Double.parseDouble(dataSnapshot.child(Constant.FIREBASE_LAT).getValue().toString()),

            Double.parseDouble(dataSnapshot.child(Constant.FIREBASE_LONG).getValue().toString()));

    if (marker != null)
        marker.remove();

    String title = dataSnapshot.child(Constant.FIREBASE_ACTOR_DATA).getValue().toString();

    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ambulance);





        marker = mMap.addMarker(new MarkerOptions().position(ambulanceLatLng).title(title).icon(icon));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ambulanceLatLng,14));



    //    if(dataSnapshot.child(Constant.FIREBASE_HAS_PICKED_UP_PATIENT).getValue().toString().equalsIgnoreCase("YES")) {

            // get the details of hospital assigned
            if (dataSnapshot.hasChild(Constant.FIREBASE_ASSIGNED_HOSPITAL_UUID)) {
                String hospitalUUID = dataSnapshot.child(Constant.FIREBASE_ASSIGNED_HOSPITAL_UUID).getValue().toString();

                if(hospitalLatLong==null) {
                    trackHospital(hospitalUUID);
                }
                else
                {
                    drawPathFromOriginToDestination(ambulanceLatLng,hospitalLatLong);
                }
            }

      // if patient has been picked up
        if(dataSnapshot.child(Constant.FIREBASE_HAS_PICKED_UP_PATIENT).getValue().toString().equalsIgnoreCase("YES")) {

            drawPathFromOriginToDestination(ambulanceLatLng,hospitalLatLong);

        }
        else
        {
            // draw paths from ambulance to patient
            drawPathFromOriginToDestination(ambulanceLatLng,patientLatlng);
        }


        // end journey
        if(dataSnapshot.hasChild(Constant.FIREBASE_IS_TRACK_FINISHED))
        {
            if(dataSnapshot.child(Constant.FIREBASE_IS_TRACK_FINISHED).getValue().toString().equalsIgnoreCase("YES"))
            {
                finish();
            }
        }

}
        catch(Exception ex)
        {

        }
*/

    }

    private void trackHospital(String hospitalUUID)
    {

      /*  FirebaseDatabase.getInstance().getReference().child(Constant.FIREBASE_HOSPITAL_KEY)
                .child(hospitalUUID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                          trackedHospital = dataSnapshot.getValue(Hospital.class);
                         hospitalLatLong = new LatLng(trackedHospital.getLatitude(),trackedHospital.getLongitude());

                        hospitalMarker = mMap.addMarker(new MarkerOptions().position(hospitalLatLong).title(trackedHospital.getActorData()));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hospitalLatLong,14));


                        //drawPathFromOriginToDestination(patientLatlng,hospitalLatLong);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

     //  Marker  hospitalMarker = mMap.addMarker(new MarkerOptions().position(ambulanceLatLng).title(title).icon(icon));
*/

    }


    @Override
    public void onCancelled(DatabaseError databaseError) {

    }



    /*Drawing paths*/
    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJsonParser parser = new PathJsonParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

         

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();


                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                
                polyLineOptions.width(10);
                polyLineOptions.color(Color.BLUE);
            }

           if(polyLineOptions!=null) {

               if(polyLine!=null)
               {
                   polyLine.remove();
               }

              polyLine =  mMap.addPolyline(polyLineOptions);
           }
        }
    }


    private String getMapsApiDirectionsUrl(LatLng origin, LatLng dest) {


        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }



    void goBackToHomeScreen()
    {
        /*Intent goBackIntent = null;
        if(Actor.equalsIgnoreCase(PATIENT_ACTOR))
        {
           goBackIntent = new Intent(MapsActivity.this,MainActivity.class);
        }
        else
            if(Actor.equalsIgnoreCase(HOSPITAL_ACTOR))
            {
               // goBackIntent = new Intent(MapsActivity.this,HospitalActivity.class);
                finish();
            }

            else
            if(Actor.equalsIgnoreCase(AMBULANCE_ACTOR))
            {
                goBackIntent = new Intent(MapsActivity.this,AmbulanceActivity.class);
            }
            else
            if(Actor.equalsIgnoreCase(TRAFFIC_ACTOR))
            {
                goBackIntent = new Intent(MapsActivity.this,TrafficControlActivity.class);
            }

            if(goBackIntent!=null) {

                goBackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(goBackIntent);
            }
*/






    }





}
