package com.encryption.projects.encryptedfilesharing.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

public class AuthPreference {

    private static final String AUTH_PREFERENCE = "LockApp_Persuasive";
    private static final String AUTH_CODE = "Auth_Code";
    private static final String AUTH_VERIFICATION_ID = "Verification_ID";
    private static final String USER_NAME = "UserName";
    private static final String USER_PHONE = "UserPhoneNumber";

    public static void saveUserDetails(@NonNull Context context, @NonNull String userName
            , @NonNull String phone) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTH_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USER_NAME, userName);
        editor.putString(USER_PHONE, phone);
        editor.apply();
        editor.commit();
    }

    public static boolean checkUserLoggedIn(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTH_PREFERENCE, Context.MODE_PRIVATE);
        Log.d("SHARED", "checkUserLoggedIn: " + sharedPreferences.getString(USER_NAME, ""));
        return (sharedPreferences.getString(USER_NAME, "").compareTo("") == 0);
    }

    public static String getPhoneNumber(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTH_PREFERENCE, Context.MODE_PRIVATE);
        Log.d("SHARED", "checkUserLoggedIn: " + sharedPreferences.getString(USER_NAME, ""));
        return sharedPreferences.getString(USER_PHONE, "");
    }

    public static String getUsername(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTH_PREFERENCE, Context.MODE_PRIVATE);
        Log.d("SHARED", "checkUserLoggedIn: " + sharedPreferences.getString(USER_NAME, ""));
        return sharedPreferences.getString(USER_NAME, "");
    }


    public static void saveVerifyCode(@NonNull Context context, @NonNull String verificationCode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTH_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AUTH_CODE, verificationCode);
        editor.apply();
        editor.commit();
    }

    public static boolean verifyCode(@NonNull Context context, @NonNull String code) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTH_PREFERENCE, Context.MODE_PRIVATE);
        if (sharedPreferences.getString(AUTH_CODE, "").compareTo(code) == 0) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(AUTH_CODE, "");
            editor.apply();
            return true;
        }
        return false;
    }

    public static String getVerficationCode(@NonNull Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTH_PREFERENCE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AUTH_CODE, "");
    }


}
