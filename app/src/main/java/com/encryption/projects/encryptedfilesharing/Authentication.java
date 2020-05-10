package com.encryption.projects.encryptedfilesharing;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Authentication {
    private static byte[] SALT = {0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f,
            0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f, 0x0f};
    private static String KEY_AES = "JtsiYneF5b3ydQM7gcIBOCpkPNSAKTha";
    private static String KEY_BLOWFISH = "YeSGnfQBHFWKdh7Vup5Si9H";

    private static String sharekey = "1234567890123456";
    private static byte[] keybytes;

    private static final String ALGORITHME = "Blowfish";
    private static final String TRANSFORMATION = "Blowfish/ECB/PKCS5Padding";
    private static final String SECRET = "kjkdfjslm";
    private static final String CHARSET = "ISO-8859-1";

    private String AesKey, BfKey;

    Authentication(Context con) {
        SharedPreferences pref = con.getSharedPreferences("EncryptedFileSharing", Context.MODE_PRIVATE);
        AesKey = pref.getString("AesKey", "");
        BfKey = pref.getString("BfKey", "");

    }

    Authentication() {
    }




    String BF_encrypt(String plaintext)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            UnsupportedEncodingException,
            IllegalBlockSizeException,
            BadPaddingException {

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY_BLOWFISH.getBytes(CHARSET), ALGORITHME));
        return new String(cipher.doFinal(plaintext.getBytes()), CHARSET);
    }

    String BF_decrypt(String ciphertext)
            throws NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            UnsupportedEncodingException,
            IllegalBlockSizeException,
            BadPaddingException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY_BLOWFISH.getBytes(), ALGORITHME));
        return new String(cipher.doFinal(ciphertext.getBytes(CHARSET)), CHARSET);
    }



    byte[] BYTE_AESencrypt(byte[] mesg)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        byte[] keybytes = AesKey.getBytes("UTF-8");
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(SALT);
        SecretKeySpec newKey = new SecretKeySpec(keybytes, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(mesg);
    }


    byte[] BYTE_AESdecrypt(byte[] mesg, String Key, int i)
            throws UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        byte[] keybytes=null;
        if (i==0) {
            keybytes = AesKey.getBytes("UTF-8");
        }else {
            keybytes = Key.getBytes("UTF-8");
        }
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(SALT);
        SecretKeySpec newKey = new SecretKeySpec(keybytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(mesg);
    }




    String getSecurePassword(@NonNull String passwordToHash, String salt) {
        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(salt.getBytes());
            //Get the hash's bytes
            byte[] bytes = md.digest(passwordToHash.getBytes());
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    byte[] encrypt(byte[] plainText) throws UnsupportedEncodingException
            , NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException
            , InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES");
        byte[] keybytes = KEY_AES.getBytes("UTF-8");
        SecretKeySpec keySpec = new SecretKeySpec(keybytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(SALT);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(plainText);
    }

    byte[] decrypt(byte[] cipherText) throws NoSuchPaddingException, NoSuchAlgorithmException
            , UnsupportedEncodingException, BadPaddingException, IllegalBlockSizeException
            , InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES");
        byte[] keybytes = KEY_AES.getBytes("UTF-8");
        SecretKeySpec keySpec = new SecretKeySpec(keybytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(SALT);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(cipherText);
    }



}
