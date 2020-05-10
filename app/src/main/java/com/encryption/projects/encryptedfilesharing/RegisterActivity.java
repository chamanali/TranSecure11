package com.encryption.projects.encryptedfilesharing;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.encryption.projects.encryptedfilesharing.webservices.JSONPARSE;
import com.encryption.projects.encryptedfilesharing.webservices.RestAPI;
import com.encryption.projects.encryptedfilesharing.webservices.Utility;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    protected EditText name, phoneNumber, emailID, password, confirmPassword;
    protected Button register;
    protected RelativeLayout relativeLayout;
    String AesKey, BfKey;
    Dialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        mDialog = new Dialog(RegisterActivity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Register");
        init();

        genrateKey();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    protected void init() {


        name = findViewById(R.id.userName);
        phoneNumber = findViewById(R.id.phoneNumber);
        emailID = findViewById(R.id.emailID);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        relativeLayout = findViewById(R.id.activity_registration);
        register = findViewById(R.id.registerButton);

        register.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkCriteria()) {

                            String match = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                            if (name.getText().toString().equals("")) {
                                Snackbar.make(relativeLayout, "Name is required", Snackbar.LENGTH_SHORT).show();
                                name.requestFocus();
                            } else if (phoneNumber.getText().toString().equals("")) {
                                Snackbar.make(relativeLayout, "Phone number is required", Snackbar.LENGTH_SHORT).show();
                                phoneNumber.requestFocus();
                            } else if (phoneNumber.getText().toString().length() != 10) {
                                Snackbar.make(relativeLayout, "Invalid Number,Must Be 10 Digits", Snackbar.LENGTH_SHORT).show();
                                phoneNumber.requestFocus();
                            } else if (emailID.getText().toString().equals("")) {
                                Snackbar.make(relativeLayout, "Email Id is required", Snackbar.LENGTH_SHORT).show();
                                emailID.requestFocus();
                            } else if (!emailID.getText().toString().matches(match)) {
                                Snackbar.make(relativeLayout, "Please Follow Email Standards", Snackbar.LENGTH_SHORT).show();
                                emailID.requestFocus();
                            } else if (password.getText().toString().equals("")) {
                                Snackbar.make(relativeLayout, "Password is required", Snackbar.LENGTH_SHORT).show();
                                password.requestFocus();
                            } else if (confirmPassword.getText().toString().equals("")) {
                                confirmPassword.requestFocus();
                                Snackbar.make(relativeLayout, " Confirm Password is required", Snackbar.LENGTH_SHORT).show();
                            } else if (!password.getText().toString().equals(confirmPassword.getText().toString())) {
                                Snackbar.make(relativeLayout, "Password Does Not Match", Snackbar.LENGTH_SHORT).show();
                                password.requestFocus();
                                password.setText("");
                                confirmPassword.setText("");
                            } else {

//                                String name,String mobile,String email,String pass,String aeskey,String ,String bfkey
                                new RegisterTask().execute(name.getText().toString(), phoneNumber.getText().toString(), emailID.getText().toString(), password.getText().toString(), AesKey, BfKey);

                            }
                        } else {
                            new AlertDialog.Builder(RegisterActivity.this)
                                    .setMessage("All fields are mandatary. Please enter all details")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    }
                }
        );

    }

    protected boolean checkCriteria() {
        boolean b = true;
        if ((name.getText().toString()).equals("")) {
            b = false;
        }
        return b;
    }

    public void genrateKey() {

        AesKey = geta() + getn() + geta() + getn() + geta() + getn() + geta() + getn() + geta() + getn() + geta() + getn() + geta() + getn() + geta() + getn();

        BfKey = geta() + geta() + getn() + getn() + geta() + geta() + getn() + getn() + geta();

    }

    public String geta() {

        String[] alphaArray = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        Random random = new Random();
        return alphaArray[random.nextInt(alphaArray.length)];
    }

    public String getn() {

        String[] numberArray = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        Random random = new Random();
        return numberArray[random.nextInt(numberArray.length)];
    }

    public class RegisterTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.register(params[0], params[1], params[2]);
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

            Log.d("Register", "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                mDialog.dismiss();
                Pair<String, String> message = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(RegisterActivity.this, message.first, message.second
                        , false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("ok")) {
                        JSONArray json = jsonObject.getJSONArray("Data");
                        JSONObject jsonObject1 = json.getJSONObject(0);
                        //Call New API to Add Password
                        Authentication authentication = new Authentication();
                        String password = confirmPassword.getText().toString();
                        password = authentication.getSecurePassword(password
                                , jsonObject1.getString("data0"));
                        new PasswordTask().execute(jsonObject1.getString("data0"), password);

                    } else if (jsonObject.getString("status").equalsIgnoreCase("already")) {
                        mDialog.dismiss();
                        phoneNumber.setText("");
                        Snackbar.make(relativeLayout
                                , "Enter details is already registered,Please Check Your Mobile Number"
                                , Snackbar.LENGTH_LONG).show();
                    } else {
                        mDialog.dismiss();
                        String error = jsonObject.getString("Error");
                        Log.d("Register", "onPostExecute: Error " + error);
                    }
                } catch (Exception e) {
                    mDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }
    }

    public class PasswordTask extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.AddPassword(params[0], params[1]);
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
            Log.d("Register", "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> message = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(RegisterActivity.this, message.first, message.second
                        , false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("true")) {
                        Toast.makeText(RegisterActivity.this, "Registered Successfully" +
                                        ", Login to Continue"
                                , Toast.LENGTH_SHORT).show();
                    } else {
                        String error = jsonObject.getString("Error");
                        Log.d("Register", "onPostExecute: Error " + error);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}

