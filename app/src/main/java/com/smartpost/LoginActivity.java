package com.smartpost;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smartpost.Entity.User;
import com.smartpost.activities.ClientActivity;
import com.smartpost.activities.PostOfficeActivity;
import com.smartpost.activities.PostmanActivity;
import com.smartpost.core.ApplicationSetting;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements  View.OnClickListener,RadioGroup.OnCheckedChangeListener{


    private static final String TAG = LoginActivity.class.getSimpleName();

    private Button btnLogin;
    private TextView sipExtension;
    private RadioGroup roleGroup;
    private RadioButton lastRadioButton;
    private LinearLayout configLayout;

    private LoginReceiver loginReceiver;
    private ProgressDialog pd = null;
    private FirebaseAuth mAuth;
    private EditText email,userLat,userLon;
    private Spinner spinner;
    public static FirebaseUser user;
    private String emailId,actorData;
    private User appUser;
    private boolean REGISTER_LOGIN_RCEIEVER = false;
    List<String> dropdownList = new ArrayList<>();
    List<String> clientList = new ArrayList();
    List<String> postmanList = new ArrayList();
    List<String> postOffList = new ArrayList<>();
    ArrayAdapter<String> dataAdapter;



    private UserRole userRole = UserRole.Client;
    public enum UserRole {
        PostMan {
            @Override
            Class getActivityClass() {
                return PostmanActivity.class;
            }
        },
        PostOffice {
            @Override
            Class getActivityClass() {
                return PostOfficeActivity.class;
            }
        },
        Client {
            @Override
            Class getActivityClass() {
                return ClientActivity.class;
            }
        };

        abstract  Class getActivityClass();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginReceiver = new LoginReceiver();
        initSpinner();
        mAuth = FirebaseAuth.getInstance();
        initUi();
        addDropDown();
    }

    private void addDropDown(){

        clientList.add("smartpost_client_1@gmail.com");
        clientList.add("smartpost_client_2@gmail.com");
        clientList.add("smartpost_client_3@gmail.com");
        clientList.add("smartpost_client_4@gmail.com");

        postmanList.add("smartpost_postman_1@gmail.com");
        postmanList.add("smartpost_postman_2@gmail.com");
        postmanList.add("smartpost_postman_3@gmail.com");
        postmanList.add("smartpost_postman_4@gmail.com");

        postOffList.add("smartpost_postoffice@gmail.com");


        dropdownList.clear();
        dropdownList.addAll(clientList);
        dataAdapter  = new ArrayAdapter<String>(this,R.layout.support_simple_spinner_dropdown_item,dropdownList);

        dataAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    private void initSpinner(){
        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("Logging in ...");
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }


    private void initUi() {
        btnLogin = (Button) findViewById(R.id.btn_login);
        roleGroup = (RadioGroup) findViewById(R.id.role_layout);
        lastRadioButton = (RadioButton) findViewById(R.id.postman);

        //sipExtension = (TextView) findViewById(R.id.text_sip_end);
        configLayout = (LinearLayout) findViewById(R.id.loginActivity);
        btnLogin.setOnClickListener(this);
        roleGroup.setOnCheckedChangeListener(this);

       // email = (EditText) findViewById(R.id.emailId);
        spinner = (Spinner) findViewById(R.id.text_email_login);

        /*userLon = (EditText) findViewById(R.id.userLon);
        userLat = (EditText) findViewById(R.id.userLat);*/

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                emailId = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                pd.show();
                Log.d(TAG, "onClick: emailId : "+emailId+" userEole:"+userRole);
                Toast.makeText(this,"UserRole : "+userRole+" emailId : "+emailId,Toast.LENGTH_SHORT).show();
                ApplicationSetting.getInstance().setUserEmail(emailId);
                signInWithFirebase();
             break;
             default:
                 Log.d(TAG, "onClick: Default case");
                 break;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    private void signInWithFirebase() {
        mAuth.signInWithEmailAndPassword(emailId,"123456").addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                pd.dismiss();
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    user = mAuth.getCurrentUser();



                    // succefully looged in in firebase
                    openActivityWithRole();

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    restartActivity();
                    //updateUI(null);
                }


            }
        });
    }


    public void restartActivity(){
        ApplicationSetting.getInstance().setUserEmail(emailId);
        //SDKManager.getInstance(this).shutdownClient();
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void openActivityWithRole(){
        Log.d(TAG, "openActivityWithRole: "+userRole.toString());
        Intent userIntent = new Intent(this, userRole.getActivityClass());
        /*switch (userRole){
            case Client:
                userIntent = new Intent(this, LoginActivity.class);
                break;
            case PostMan:
                userIntent = new Intent(this, LoginActivity.class);
                break;
            case PostOffice:
                userIntent = new Intent(this, LoginActivity.class);
               *//* if(actorAttribute.getText().toString()!=null){
                    hospitalName = actorAttribute.getText().toString();
                }
                userIntent.putExtra("HsopitalName",hospitalName);
*//*
                break;
            default:
                Log.d(TAG, "onClick: Invalid Enum value "+userRole.toString());
        }*/
        startActivity(userIntent);
        finish();
    }

    public boolean checkIfUserRoleSelected(){
        if (roleGroup.getCheckedRadioButtonId() == -1){
            lastRadioButton.setError(getResources().getString(R.string.selectRadioError));
            lastRadioButton.requestFocus();
            return false;
        }
        return true;
    }

    private void updateDropDownList (){
        dropdownList.clear();
        switch (userRole){
            case Client:
                dropdownList.addAll(clientList);
                break;
            case PostMan:
                dropdownList.addAll(postmanList);
                break;
            case PostOffice:
                dropdownList.addAll(postOffList);
                break;
        }
        dataAdapter.notifyDataSetChanged();
    }
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i){
            case R.id.postman:
                userRole = UserRole.PostMan;
                break;
            case R.id.postOffice:
                userRole = UserRole.PostOffice;
                break;
            case R.id.client:
                userRole = UserRole.Client;
                break;
        }
        updateDropDownList();
       // showActorField();
    }


    class LoginReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}

