package com.encryption.projects.encryptedfilesharing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.encryption.projects.encryptedfilesharing.helper.AuthPreference;
import com.encryption.projects.encryptedfilesharing.helper.MyReceiver;
import com.encryption.projects.encryptedfilesharing.helper.PhoneAuthentication;
import com.encryption.projects.encryptedfilesharing.webservices.JSONPARSE;
import com.encryption.projects.encryptedfilesharing.webservices.RestAPI;
import com.encryption.projects.encryptedfilesharing.webservices.Utility;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

public class ForgotPassword extends AppCompatActivity implements MyReceiver.MessageListener{
    private static final String TAG = "VERIFY";
    private Context appContext;
    private EditText mMobileNumber, mVerifyCode, newPassword;
    private TextView mHintView;
    private LinearLayout mMobileLayout, mVerifyLayout, mChangeLayout;
    private MaterialButton mMobileProceed, mVerifyProceed, mUpdatePassword;

    private PhoneAuthentication.PhoneAuthInterface phoneAuthInterface = new PhoneAuthentication.PhoneAuthInterface() {
        @Override
        public void onResponse(String response) {
            mDialog.dismiss();

            mHintView.setText(String.format(getResources().getString(R.string.otp_sent_verification)
                    , mMobileNumber.getText().toString()));

            if(mMobileLayout.getVisibility() == View.VISIBLE)
                mMobileLayout.setVisibility(View.GONE);

            if(mVerifyLayout.getVisibility() == View.GONE)
                mVerifyLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onErrorResponse(String error) {
            mDialog.dismiss();
            Toast.makeText(appContext, error, Toast.LENGTH_SHORT).show();
        }
    };
    private PhoneAuthentication phoneAuthentication;
    private Dialog mDialog;

    private String userId = "";

    private MyReceiver myReceiver = new MyReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.change_password_layout);

        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appContext = ForgotPassword.this;
        phoneAuthentication = new PhoneAuthentication(appContext, phoneAuthInterface);
        MyReceiver.bindMessageListener(this);

        mDialog = new Dialog(appContext, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        mHintView = findViewById(R.id.hint_mobile);

        mMobileLayout = findViewById(R.id.main_otp);
        mVerifyLayout = findViewById(R.id.main_otp_verify);
        mChangeLayout = findViewById(R.id.main_forgot_layout);

        mMobileProceed = findViewById(R.id.get_otp);
        mVerifyProceed = findViewById(R.id.verify_otp);
        mUpdatePassword = findViewById(R.id.txtUpdatePassword);

        mMobileNumber = findViewById(R.id.pOtp);
        mVerifyCode = findViewById(R.id.otp_verify);
        newPassword = (EditText) findViewById(R.id.pNewPassword);

        mMobileLayout.setVisibility(View.VISIBLE);

        mMobileProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMobileNumber.getText().toString().length() == 0) {
                    Snackbar.make(mMobileProceed, "Enter your mobile number.", Snackbar.LENGTH_SHORT).show();
                    mMobileNumber.requestFocus();
                } else {
                    new VerifyNumberTask().execute(mMobileNumber.getText().toString());
                }
            }
        });

        mVerifyProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVerifyCode.getText().toString().length() == 0) {
                    Snackbar.make(mVerifyProceed, "Enter Old Password", Snackbar.LENGTH_SHORT).show();
                    mVerifyCode.requestFocus();
                } else {
                    if(AuthPreference.verifyCode(appContext, mVerifyCode.getText().toString())){
                        if(mVerifyLayout.getVisibility() == View.VISIBLE)
                            mVerifyLayout.setVisibility(View.GONE);

                        mChangeLayout.setVisibility(View.VISIBLE);

                    }else {
                        Snackbar.make(view, "Please, Enter Correct Verification Code", Snackbar.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        });

        mUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newPassword.getText().toString().length() == 0) {
                    Snackbar.make(mUpdatePassword, "Enter Old Password", Snackbar.LENGTH_SHORT).show();
                    newPassword.requestFocus();
                } else {
                    Authentication authentication = new Authentication();
                    String password = newPassword.getText().toString();
                    password = authentication.getSecurePassword(password, userId);
                    new UpdatePass().execute(userId, mMobileNumber.getText().toString()
                            , password);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyReceiver.bindMessageListener(this);
        IntentFilter smsFilter = new IntentFilter();
        smsFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        this.registerReceiver(myReceiver, smsFilter);
    }

    @Override
    public void onMessageReceived(String message) {
        if (message != null && message.length() > 0) {
            char[] chars = message.toCharArray();

            Log.d(TAG, "onMessageReceived: Message  : " + message + " \n" + chars.toString());
            if (AuthPreference.verifyCode(appContext, message)) {
                if(mVerifyLayout.getVisibility() == View.VISIBLE)
                    mVerifyLayout.setVisibility(View.GONE);

                mChangeLayout.setVisibility(View.VISIBLE);
            } else {
                Log.d(TAG, "onMessageReceived: Not Verified");
            }
        }
    }

    public class VerifyNumberTask extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            mDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String answer = "";
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject jsonObject = restAPI.GetUserId(strings[0]);
                JSONPARSE jsonparse = new JSONPARSE();
                answer = jsonparse.parse(jsonObject);
            } catch (Exception e) {
                answer = e.getMessage();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            String exp = "";
            if (Utility.checkConnection(s)) {
                Pair<String, String> message = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(ForgotPassword.this, message.first, message.second
                        , false);
            }  else {
                try {
                    JSONObject object = new JSONObject(s);
                    exp = object.getString("status");
                    if (exp.compareTo("ok") == 0) {

                        userId = object.getJSONArray("Data").getJSONObject(0).getString("data0");

                        if (mMobileLayout.getVisibility() == View.VISIBLE) {
                            mMobileLayout.setVisibility(View.GONE);
                        }

                        mDialog.show();

                        phoneAuthentication.generateOTP("+91"+mMobileNumber.getText().toString());

                    } else if (exp.compareTo("false") == 0) {
                        Snackbar.make(mUpdatePassword, "No such user exists. Please check the number or register if not existing user.", Snackbar.LENGTH_SHORT).show();
                    } else {
                        String error = object.getString("Data");
                        Toast.makeText(appContext, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(appContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                super.onPostExecute(s);
            }
        }
    }

    public class UpdatePass extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            mDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String answer = "";
            RestAPI restAPI = new RestAPI();
            try {
                JSONObject jsonObject = restAPI.changePassword(strings[0], strings[1], strings[2]);
                JSONPARSE jsonparse = new JSONPARSE();
                answer = jsonparse.parse(jsonObject);
            } catch (Exception e) {
                answer = e.getMessage();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String s) {
            mDialog.dismiss();
            String exp = "";
            if (Utility.checkConnection(s)) {
                Pair<String, String> message = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(ForgotPassword.this, message.first, message.second
                        , false);
            } else {
                try {
                    JSONObject object = new JSONObject(s);
                    exp = object.getString("status");
                    if (exp.compareTo("true") == 0) {

                        Snackbar.make(mUpdatePassword, "Password Updated Successfully, Login with the new password.", Snackbar.LENGTH_SHORT).show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 200);

                    } else if (exp.compareTo("false") == 0) {
                        Snackbar.make(mUpdatePassword, "In-Correct Old Password", Snackbar.LENGTH_SHORT).show();
                    } else {
                        String error = object.getString("Data");
                        Toast.makeText(appContext, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(appContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                super.onPostExecute(s);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(myReceiver);
    }
}
