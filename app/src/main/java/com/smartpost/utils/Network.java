package com.smartpost.utils;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by GS-1480 on 27-04-2017.
 */

public class Network {

    private Context context;

    private AsyncHttpClient client = new AsyncHttpClient();

    public Network(Context context) {
        this.context = context;
    }

    //for POST request with JSON input
    public void postWithJSON(String url, JSONObject params, AsyncHttpResponseHandler responseHandler) {

     //   if(lfApp.isNetworkAvailable() && lfApp.isNetworkConnected()) {


            StringEntity entity;
            try {
                entity = new StringEntity(params.toString());

                //   client.addHeader("x-auth-token",lfApp.getUser().getAuthString());

                // client.addHeader("Content-Type", "application/json");
                client.addHeader("Authorization","key="+"AAAAoOtLGA8:APA91bFeQgpgt7Lx2jZodp_VNSue7DJjUDZ5X5IaANUhcFdfc3l5IqmgSbFOULnxypuyH60wl9lPkcXwRN7E8rgyg1aH9zowJ1sbOdXcr-EJuy-QtDFk_0N1G1uJ9Ee_BJx5qiFlQYpF");

                client.post(context, url, entity, "application/json", responseHandler);

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

       // }
//        else {
//
//            Toast.makeText(context, NO_INTERNET, Toast.LENGTH_SHORT).show();
//        }


    }
}
