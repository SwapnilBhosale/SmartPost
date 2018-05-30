

package com.smartpost.activities;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartpost.entities.PostMan;
import com.smartpost.R;

import java.util.List;
import java.util.Locale;

public class MyRecyclerViewAdapter extends RecyclerView
        .Adapter<MyRecyclerViewAdapter
        .DataObjectHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private List<PostMan> mDataset;
    private static MyClickListener myClickListener;
    private Context mcontext;

    private static String TAG = MyRecyclerViewAdapter.class.getSimpleName();

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {

        TextView postmanDetail;
        TextView postmanLatLng;
        TextView currLocation;
       // TextView diagnosis;

        public DataObjectHolder(View itemView) {
            super(itemView);
            postmanDetail = (TextView) itemView.findViewById(R.id.postmanDetail);
            postmanLatLng = (TextView) itemView.findViewById(R.id.postmanLatLng);
            currLocation = (TextView) itemView.findViewById(R.id.currLocation);

            Log.i(LOG_TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public void setPostmanList(List<PostMan> list){

        this.mDataset.clear();
        this.mDataset.addAll(list);
        notifyDataSetChanged();
    }


    public MyRecyclerViewAdapter(List<PostMan> myDataset,Context context) {
        mDataset = myDataset;
        this.mcontext = context;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.postman_row, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, final int position) {

        PostMan postman = mDataset.get(position);
        holder.postmanDetail.setText(postman.getEmail());
        holder.postmanLatLng.setText(String.valueOf(postman.getLatitude()).concat(" : ").concat(String.valueOf(postman.getLongitude())));
        holder.currLocation.setText(reverseGeoCode(postman.getLatitude(),postman.getLongitude()));
    }

    public void addItem(PostMan dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }


    public String reverseGeoCode(Double latitude,Double longitude){
        Geocoder geocoder = new Geocoder(mcontext, Locale.getDefault());
        String result = null;
        List<Address> addresses = null;
        try {

            // at some weird location coordinates, this addreses array can come empty

            addresses = geocoder.getFromLocation(latitude,longitude, 1);
            if (!addresses.isEmpty()) {
                String feutureName = addresses.get(0).getFeatureName();
                String stateName = addresses.get(0).getAddressLine(1);

                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append(", ");
                }
                sb.append(address.getLocality()).append(",");
                sb.append(address.getAdminArea()).append(", ");
                sb.append(address.getPostalCode()).append(" ");
                // sb.append(address.getPostalCode()).append("\n");
                sb.append(address.getCountryName());

                result = sb.toString();
                Log.d(TAG, "reverseGeoCode: "+result);
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "reverseGeoCode: ",e );
        }
        return result;
    }
}