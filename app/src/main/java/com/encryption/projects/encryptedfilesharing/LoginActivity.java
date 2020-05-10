package com.encryption.projects.encryptedfilesharing;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.encryption.projects.encryptedfilesharing.webservices.JSONPARSE;
import com.encryption.projects.encryptedfilesharing.webservices.RestAPI;
import com.encryption.projects.encryptedfilesharing.webservices.Utility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    protected EditText Mobile, Password;
    protected Button SignIn, SignUp;
    protected RelativeLayout relativeLayout;
    SharedPreferences pref;
    Dialog mDialog;
    String previousMobileNumber = "";
    private String UserId;

    private DecimalFormat df = new DecimalFormat("#.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mDialog = new Dialog(LoginActivity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        pref = getSharedPreferences("EncryptedFileSharing", Context.MODE_PRIVATE);
        String mobileNo_pref = pref.getString("UserId", "");

        if (mobileNo_pref.compareTo("") != 0) {
            String mobile = pref.getString("Mobile", "");
            Log.d(TAG, "onCreate: Length String  Code = " + mobile + "\n" + mobile.getBytes(StandardCharsets.UTF_8).length);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        }
        getSupportActionBar().hide();
        init();

    }

    protected void init() {


        Mobile = findViewById(R.id.loginUserName);
        Password = findViewById(R.id.loginPassword);
        SignIn = findViewById(R.id.loginButton);
        SignUp = findViewById(R.id.signUp);

        relativeLayout = findViewById(R.id.activity_login);

        findViewById(R.id.forgot_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPassword.class));
            }
        });

        Mobile.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: " + hasFocus);
                if (!hasFocus) {
                    if (Mobile.getText().toString().length() != 0) {
                        if (previousMobileNumber.length() == 0) {
                            previousMobileNumber = Mobile.getText().toString();
                            callUserIdAPI();
                        } else {
                            if (!(previousMobileNumber.equalsIgnoreCase(Mobile.getText().toString()))) {
                                new GetUserTask().execute(Mobile.getText().toString());
                            }
                        }
                    }

                }
            }
        });

        SignUp.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                          startActivity(intent);
                                      }
                                  }
        );

        SignIn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (Mobile.getText().toString().equals("")) {
                            Snackbar.make(v, "Mobile Number is required", Snackbar.LENGTH_SHORT).show();
                            Mobile.requestFocus();
                        } else if (Mobile.getText().toString().length() != 10) {
                            Snackbar.make(v, "Please Fallow Mobile Number Standards", Snackbar.LENGTH_SHORT).show();
                            Mobile.requestFocus();
                        } else if (Password.getText().toString().equals("")) {
                            Snackbar.make(v, "Password is required", Snackbar.LENGTH_LONG).show();
                            Password.requestFocus();
                        } else {
                            Authentication authentication = new Authentication();
                            String password = Password.getText().toString();
                            password = authentication.getSecurePassword(password
                                    , UserId);
                            new LoginTask().execute(Mobile.getText().toString()
                                    , password);
                        }
                    }
                }
        );


    }

    private void callUserIdAPI() {
        new GetUserTask().execute(Mobile.getText().toString());
    }

    public class LoginTask extends AsyncTask<String, JSONObject, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.login(params[0], params[1]);
                JSONPARSE jp = new JSONPARSE();
                a = jp.parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.dismiss();
            Log.d(TAG, "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> message = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(LoginActivity.this, message.first, message.second
                        , false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("ok")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("Data");
                        JSONObject object = jsonArray.getJSONObject(0);

                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("Mobile", Mobile.getText().toString());
                        editor.putString("UserId", object.getString("data0"));
                        editor.apply();
                        editor.commit();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } else if (jsonObject.getString("status").equalsIgnoreCase("false")) {

                        Snackbar.make(relativeLayout, "Invalid Credential", Snackbar.LENGTH_LONG).show();

                    } else {
                        String error = jsonObject.getString("Error");
                        Log.d(TAG, "onPostExecute: Error " + error);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class GetUserTask extends AsyncTask<String, JSONObject, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            Password.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.GetUserId(params[0]);
                JSONPARSE jp = new JSONPARSE();
                a = jp.parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.dismiss();
            Password.setEnabled(true);
            Log.d(TAG, "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> message = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(LoginActivity.this, message.first, message.second
                        , false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("ok")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("Data");
                        JSONObject object = jsonArray.getJSONObject(0);
                        UserId = object.getString("data0");

                    } else if (jsonObject.getString("status").equalsIgnoreCase("false")) {

                        Mobile.setText("");
                        Snackbar.make(relativeLayout, "Could not find user. You have entered invalid details."
                                , Snackbar.LENGTH_LONG).show();

                    } else {
                        String error = jsonObject.getString("Error");
                        Log.d(TAG, "onPostExecute: Error " + error);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}




