package com.smartpost.core;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.smartpost.utils.Network;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class SmartPostApplication extends Application {

    private static final String TAG = SmartPostApplication.class.getSimpleName();
    public static Context appContext = null;
    private Network client;



    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        client = new Network(this);
    }


    public static Context getContext(){
        return appContext;
    }

    public Network getClient() {
        return client;
    }


    public void sendCloudNotification(JSONObject obj){
        client.postWithJSON("https://fcm.googleapis.com/fcm/send", obj, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Toast.makeText(appContext, "Notification sent !", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(appContext, "Notification sending failed !", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
