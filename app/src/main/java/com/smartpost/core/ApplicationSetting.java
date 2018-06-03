package com.smartpost.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;



/**
 * Created by imran on 6/10/16.
 */
public class ApplicationSetting {
    private static final String TAG = "ApplicationSetting";
    private static ApplicationSetting singleton = null;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // Keys
    private final static String KEY_USER_EMAIL_ID = "KEY_USER_EMAIL_ID";
    private final static String KEY_PHONE = "KEY_PHONE";



    private ApplicationSetting() {
        sharedPreferences = SmartPostApplication.getContext().getSharedPreferences("com.smartpost.sharedpreferences",Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized ApplicationSetting getInstance() {
        if (singleton == null) {
            singleton = new ApplicationSetting();
        }
        return singleton;
    }

    public void setUserEmail(String emailId) {
        editor.putString(KEY_USER_EMAIL_ID, emailId);
        editor.commit();
    }

    public String getUserEmail() {
        String emailId = sharedPreferences.getString(KEY_USER_EMAIL_ID, "");
        Log.d(TAG, "getUserEmail: "+emailId);
        return emailId;
    }

    public void setPhone(String phone) {
        editor.putString(KEY_PHONE, phone);
        editor.commit();
    }

    public String getPhone() {
        String phone = sharedPreferences.getString(KEY_PHONE, "");
        Log.d(TAG, "getUserEmail: "+phone);
        return phone;
    }


    /*public void setActorAttribute(String data) {
        editor.putString(KEY_ACTOR_ATTRIBUTE, data);
        editor.commit();
    }

    public String getActorAttribute() {
        String data = sharedPreferences.getString(KEY_ACTOR_ATTRIBUTE, "");
        Log.d(TAG, "getActorAttribute: "+data);
        return data;
    }*/

  /*  public boolean setUserConfigurationSaved(boolean configurationSaved) {
        Log.d(TAG, "setUserConfigurationSaved() called with: " + "configurationSaved = [" + configurationSaved + "]");
        editor.putBoolean(KEY_IS_USER_SETTING_SAVED, configurationSaved);
        boolean results = editor.commit();
        Log.d(TAG, "setUserConfigurationSaved() returned: " + results);
        return results;
    }

    public boolean isUserConfigurationSaved() {
        Log.d(TAG, "isUserConfigurationSaved() called");
        boolean results = false;
        results = sharedPreferences.getBoolean(KEY_IS_USER_SETTING_SAVED, false);
        Log.d(TAG, "isUserConfigurationSaved() returned: " + results);
        return results;
    }
*/
}
