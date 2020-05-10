package com.encryption.projects.encryptedfilesharing.helper;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.encryption.projects.encryptedfilesharing.R;

import java.net.URLEncoder;
import java.util.Random;

public class PhoneAuthentication {
    private PhoneAuthInterface phoneInterface;
    private static final String ClickNSendUserName = "x15016005@student.ncirl.ie";
    private static final String ClickNSendAPI = "057C25E0-F02F-8636-BD5D-DEEDC5C51AAE";
//    private static final String ClickNSendAPI = "NqDfeR75vGjZLmQ6apgoksd3YcTrFSWKlAnP4CJOHVX0E9BUxbOmXgipR3kyd82lQAGuCrzV0LhUeISY";

    private Context appContext;

    public PhoneAuthentication(Context appContext, PhoneAuthInterface phoneAuthInterface) {
        this.appContext = appContext;
        this.phoneInterface = phoneAuthInterface;
    }


    public interface PhoneAuthInterface {

        void onResponse(String response);

        void onErrorResponse(String error);
    }


    public void generateOTP(@NonNull final String phoneNumber) {
        String code = getCode();
        Log.d("OTP", code);

        try {
            String m = String.format("Your generated OTP to reset your password for %s is %s.", appContext.getResources().getString(R.string.app_name)
                    , code);
            String message = URLEncoder.encode(m, "utf-8");
            String contactNumber = URLEncoder.encode(phoneNumber, "utf-8");

            Log.d("OTP", "generatedOTP: " + code);

            AuthPreference.saveVerifyCode(appContext, code);

            String url = "https://api-mapper.clicksend.com/http/v2/send.php?method=http&username=" + ClickNSendUserName + "" +
                    "&key=" + ClickNSendAPI + "&to=" + contactNumber + "&message=" + message;

            //            final String url = "https://www.fast2sms.com/dev/bulk?authorization=" + ClickNSendAPI
//                    + "&sender_id=FSTSMS&message=" + message + "&language=english&route=p&numbers=" + phoneNumber;
            //            JsonObjectRequest request1 = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject response) {
//                    if (response != null) {
//
//                    }
//                    phoneInterface.onResponse(response.toString());
//                }
//            };

//            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//                        @Override
//                        public void onResponse(JSONObject response) {
//                            Log.d("REQUEST", "onResponse: " + response);
//                            if (response != null) {
//                                try {
//                                    if (response.getString("return").compareTo("false") != 0) {
//                                        phoneInterface.onResponse("Message Sent Successfully");
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                    phoneInterface.onResponse("Message Sent Successfully");
//
//                                }
//                            }
//                        }
//                    }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                            error.printStackTrace();
//                            phoneInterface.onResponse("Message Sent Successfully");
//
//                        }
//                    });

            StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    phoneInterface.onResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    phoneInterface.onErrorResponse(error.getMessage() == null
                            ? "Something Went Wrong" : error.getMessage());
                }
            });

            RequestQueue requestQueue = Volley.newRequestQueue(appContext);
            requestQueue.add(request);
//            new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        } catch (Exception e) {
            e.printStackTrace();
            phoneInterface.onErrorResponse(e.getMessage());
        }
    }

    public void resendOTP(@NonNull final String phoneNumber) {
        String code = AuthPreference.getVerficationCode(appContext).length() > 0
                ? AuthPreference.getVerficationCode(appContext) : null;
        if (code != null) {
            Log.d("OTP", code);

            try {
                String m = "Your generated OTP for TwoFactorAuth is #" + code;
                String message = URLEncoder.encode(m, "utf-8");
                String contactNumber = URLEncoder.encode(phoneNumber, "utf-8");

                Log.d("OTP", "generatedOTP: " + code);

                AuthPreference.saveVerifyCode(appContext, code);

                String url = "https://api-mapper.clicksend.com/http/v2/send.php?method=http&username=" + ClickNSendUserName + "" +
                        "&key=" + ClickNSendAPI + "&to=" + contactNumber + "&message=" + message;

                //                final String url = "https://www.fast2sms.com/dev/bulk?authorization=" + ClickNSendAPI
//                        + "&sender_id=FSTSMS&message=" + message + "&language=english&route=p&numbers=" + phoneNumber;

//            JsonObjectRequest request1 = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
//                @Override
//                public void onResponse(JSONObject response) {
//                    if (response != null) {
//
//                    }
//                    phoneInterface.onResponse(response.toString());
//                }
//            };

//                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                Log.d("REQUEST", "onResponse: " + response);
//                                if (response != null) {
//                                    try {
//                                        if (response.getString("return").compareTo("false") != 0) {
//                                            phoneInterface.onResponse("Message Sent Successfully");
//                                        }
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                        phoneInterface.onResponse("Message Sent Successfully");
//
//                                    }
//                                }
//                            }
//                        }, new Response.ErrorListener() {
//                            @Override
//                            public void onErrorResponse(VolleyError error) {
//                                error.printStackTrace();
//                                phoneInterface.onResponse("Message Sent Successfully");
//
//                            }
//                        });

                StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        phoneInterface.onResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        phoneInterface.onErrorResponse(error.getMessage() == null
                                ? "Something Went Wrong" : error.getMessage());
                    }
                });

                RequestQueue requestQueue = Volley.newRequestQueue(appContext);
                requestQueue.add(request);
//                new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            } catch (Exception e) {
                e.printStackTrace();
                phoneInterface.onErrorResponse(e.getMessage());
            }
        }
    }

    private String getCode() {
        int nos[] = new int[]{0, 1, 2, 3, 5, 6, 7, 8, 9};
        Random r = new Random();

        String ans = nos[r.nextInt(nos.length)] + ""
                + nos[r.nextInt(nos.length)] + ""
                + nos[r.nextInt(nos.length)] + ""
                + nos[r.nextInt(nos.length)] + ""
                + nos[r.nextInt(nos.length)] + ""
                + nos[r.nextInt(nos.length)];
        return ans;
    }
}
