package com.encryption.projects.encryptedfilesharing.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.encryption.projects.encryptedfilesharing.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyReceiver extends BroadcastReceiver {
    private static MessageListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("OTP", "onMessageReceived : ");
        if (intent.getExtras() != null) {
            Bundle data = intent.getExtras();
            Object[] pdus = (Object[]) data.get("pdus");
            if (pdus != null) {
                for (Object pdus1 : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus1);
                    if (smsMessage.getMessageBody().contains(context.getString(R.string.app_name))) {
                        String authCode = getAuthCode(smsMessage.getMessageBody());
                        Log.d("OTP", "onReceiveOTP: " + authCode);
                        mListener.onMessageReceived(authCode.substring(1));
                    }
                }
            }
        }
    }

    private String getAuthCode(String messageBody) {
        String code = "No Code Found";
        Pattern pattern = Pattern.compile("^(#[0-9]{6})$");
        Matcher matcher = pattern.matcher(messageBody);
        while (matcher.find()) {
            code = matcher.group();
        }
        return code;
    }

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public static void bindMessageListener(MessageListener messageListener) {
        mListener = messageListener;
    }
}
