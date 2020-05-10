package com.encryption.projects.encryptedfilesharing;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.gsm.SmsManager;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.encryption.projects.encryptedfilesharing.webservices.JSONPARSE;
import com.encryption.projects.encryptedfilesharing.webservices.RestAPI;
import com.encryption.projects.encryptedfilesharing.webservices.Utility;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class FileDetail_Activity extends AppCompatActivity {

    public static final String TAG = "USER_LIST";
    TextView FileName, Description_text, FileSize, date_time, FileId;
    ImageView Back_btn;
    Button Download_file, DeleteFile, ShareFile;
    String FName, FDescription, FDateTime, File_Type, Path, AesPath;
    DecimalFormat df = new DecimalFormat("#.00");
    double FSize;
    RelativeLayout relativeLayout;

    ProgressDialog pd;
    FTPClient mFTPClient;
    CopyStreamAdapter streamListener;
    Long size = null;

    SharedPreferences pref;
    String userId, Fid;
    Dialog mDialog;

    Authentication authetication;


    String Status, DownloadStatus;
    ArrayList<String> data;


    AlertDialog.Builder builderSingle;
    ArrayAdapter<String> arrayAdapter;

    String[] UserName;
    String[] UserNumber;
    Authentication authentication;
    private ArrayList<String> mUserName, mUserNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_detail_activity);
        getSupportActionBar().hide();

        pref = getSharedPreferences("EncryptedFileSharing", Context.MODE_PRIVATE);
        userId = pref.getString("UserId", "");

        authentication = new Authentication(FileDetail_Activity.this);


        authetication = new Authentication(FileDetail_Activity.this);


        Intent intent = getIntent();
        Fid = intent.getStringExtra("fid");
        FName = intent.getStringExtra("fName");
        FDescription = intent.getStringExtra("fDescription");
        File_Type = intent.getStringExtra("fType");
        FSize = Double.parseDouble(intent.getStringExtra("fSize"));
        FDateTime = intent.getStringExtra("fDatetime");
        AesPath = intent.getStringExtra("fAesPath");

        showDialog();

        init();

    }

    public void init() {

        mDialog = new Dialog(FileDetail_Activity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        FileId = findViewById(R.id.file_id);
        FileName = findViewById(R.id.file_name);
        Description_text = findViewById(R.id.description_text);
        FileSize = findViewById(R.id.size_text);
        date_time = findViewById(R.id.date_time);

        Back_btn = findViewById(R.id.back_btn);
        Download_file = findViewById(R.id.file_download);
        relativeLayout = findViewById(R.id.file_detail_layout);

        FileName.setText(FName);
        Description_text.setText(FDescription);

        DeleteFile = findViewById(R.id.delete_file);
        ShareFile = findViewById(R.id.share_file);

        double file_size = FSize / 1024;

        if (file_size >= 1000) {
            file_size = file_size / 1024;
            String tempSize = "<b>Size: </b>" + df.format(file_size) + " Mb";
            FileSize.setText(Html.fromHtml(tempSize));
        } else {
            String tempSize = "<b>Size: </b>" + df.format(file_size) + " Kb";
            FileSize.setText(Html.fromHtml(tempSize));
        }

        date_time.setText(FDateTime);

        FileId.setText(Html.fromHtml("<b>File Id: </b>" + Fid));

        Back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        new GetUsersListTask().execute(userId);


        Download_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!weHavePermission()) {
                    requestforPermissionFirst();
                } else {
                    File file = new File("/sdcard/EncryptedFileManager/temp");
                    file.mkdirs();
                    File finalfile = new File(file.getAbsolutePath(), "aes.txt");
                    String[] fPath = AesPath.split("/");
                    //Destination Path(Phone_path)//FileName//Source Full Path(server)
                    DownloadStatus = "Aes";
                    Log.d(TAG, "onClick: " + Arrays.asList(fPath).toString());
                    Log.d(TAG, "onClick: " + fPath[3]);
                    new DownloadFileTask().execute(finalfile.getAbsolutePath(), fPath[3], AesPath);
                }
            }
        });


        DeleteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(FileDetail_Activity.this).setTitle("Delete")
                        .setMessage("Are you sure to Delete ?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Status = "Aes";
                                new DeletFileFTPTask().execute(AesPath);
                            }

                        })
                        .setNegativeButton(android.R.string.no, null).show();


            }
        });


        ShareFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                builderSingle.show();

            }
        });

    }

    //Android Runtime Permission
    private boolean weHavePermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestforPermissionFirst() {
        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS))) {
            requestForResultContactsPermission();
        } else {
            requestForResultContactsPermission();
        }
    }

    private void requestForResultContactsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS}, 111);
    }

    public void showDialog() {


        builderSingle = new AlertDialog.Builder(FileDetail_Activity.this);
//                builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Select One Contact");

        arrayAdapter = new ArrayAdapter<String>(FileDetail_Activity.this, android.R.layout.simple_list_item_1);


        builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String phone_no = mUserNumber.get(which);
                String user_name = mUserName.get(which);

                sendSMS(phone_no, user_name);

            }
        });

    }

    public void sendSMS(String phoneNo, String userName) {

        String BfEncryptionResponse = null;
        try {
            String bf_msg = Fid + "*" + userId;
            Log.d(TAG, "sendSMS: " + Fid + " UserId :" + userId);
            BfEncryptionResponse = authentication.BF_encrypt(bf_msg);

        } catch (Exception e) {
            Toast.makeText(FileDetail_Activity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        try {
            String msgContent = "EFM:" + BfEncryptionResponse;
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msgContent, null, null);
            Toast.makeText(FileDetail_Activity.this.getApplicationContext(), "Message Sent to" + " " + userName, Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(FileDetail_Activity.this.getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }


    }

    public class DownloadFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(FileDetail_Activity.this);
            pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
            pd.setMessage("Downloading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setProgress(0);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String ans = "";

            try {
                mFTPClient = new FTPClient();
                mFTPClient.connect(BuildConfig.HOST);

                if (mFTPClient.login(BuildConfig.USER
                        , BuildConfig.PASSWORD)) {
                    mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);

                    BufferedOutputStream buffout = null;
                    final File file = new File(params[0]);

                    buffout = new BufferedOutputStream(new FileOutputStream(file));
                    mFTPClient.enterLocalPassiveMode();

                    //2nd method to get size
                    String mainfolder = "/Asecurefile/Files";

                    FTPFile[] f = mFTPClient.listFiles(mainfolder);
                    for (int i = 0; i < f.length; i++) {
                        if (f[i].toString().contains(params[1])) {
                            size = f[i].getSize();
                        }
                    }

                    streamListener = new CopyStreamAdapter() {

                        @Override
                        public void bytesTransferred(long totalBytesTransferred,
                                                     int bytesTransferred, long streamSize) {

                            int percent = (int) (totalBytesTransferred * 100 / size);
                            pd.setProgress(percent);
                            publishProgress();

                            if (totalBytesTransferred == file.length()) {
                                System.out.println("100% transfered");

                                removeCopyStreamListener(streamListener);

                            }

                        }

                    };
                    mFTPClient.setCopyStreamListener(streamListener);

                    Boolean status = mFTPClient.retrieveFile(params[2], buffout);
                    if (status) {
                        ans = "true";
                    } else {
                        ans = "false";
                    }

                    buffout.close();
                    mFTPClient.logout();
                    mFTPClient.disconnect();
                } else {
                    ans = "Authentication failure";
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ans = "file not found-" + "\n" + e.getMessage();
            } catch (SocketException e) {
                e.printStackTrace();
                ans = "socket-" + "\n" + e.getMessage();
            } catch (IOException e) {
                e.printStackTrace();
                ans = "IO-" + "\n" + e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                ans = "E-" + "\n" + e.getMessage();
            }
            return ans;
        }

        protected void onProgressUpdate(String... values) {
            pd.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.cancel();
            if (s.compareTo("true") == 0) {


                if (DownloadStatus.compareTo("Aes") == 0) {

                    new DecryptionTask().execute();

                }
            } else {
                Toast.makeText(FileDetail_Activity.this, s, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class DeletFileFTPTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String ans = "";
            try {
                mFTPClient = new FTPClient();
                mFTPClient.connect(BuildConfig.HOST);

                if (mFTPClient.login(BuildConfig.USER
                        , BuildConfig.PASSWORD)) {
                    mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
                    mFTPClient.enterLocalPassiveMode();

                    boolean status = mFTPClient.deleteFile(params[0]);
                    if (status) {
                        ans = "true";
                    } else {
                        ans = "false";
                    }
                }

            } catch (FileNotFoundException e) {
                ans = "file not found-" + "\n" + e.getMessage();
            } catch (SocketException e) {
                ans = "socket-" + "\n" + e.getMessage();
            } catch (IOException e) {
                ans = "IO-" + "\n" + e.getMessage();
            } catch (Exception e) {
                ans = "E-" + "\n" + e.getMessage();
            }
            return ans;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.compareTo("true") == 0) {
                if (Status.compareTo("Aes") == 0) {

                    new Delete_File().execute(Fid);

                }

            } else if (s.compareTo("false") == 0) {
                Toast.makeText(FileDetail_Activity.this, "Something went wrong, Try Again!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FileDetail_Activity.this, s, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class Delete_File extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.deletefile(params[0]);
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
            if (Utility.checkConnection(s)) {
                Pair<String, String> errorMessage = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(FileDetail_Activity.this, errorMessage.first
                        , errorMessage.second, false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("true")) {
                        Snackbar.make(relativeLayout, "File Deleted successfully", Snackbar.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);
                    } else {
                        String error = jsonObject.getString("Error");
                        Log.d(TAG, "onPostExecute: Error " + error);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(FileDetail_Activity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public class DecryptionTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String answer = "";
            try {

                File file1 = new File("/sdcard/EncryptedFileManager/temp");
                file1.mkdirs();
                File fileAES1 = new File(file1.getAbsolutePath(), "aes.txt");
                int fileLength = (int) fileAES1.length();
                byte[] plainText = new byte[fileLength];
                FileInputStream in = new FileInputStream(fileAES1);
                int bytesRead = in.read(plainText);
                in.close();

                byte[] dec = authetication.decrypt(plainText);


                answer = "true";


            } catch (Exception e) {

                answer = e.getMessage();
            }

            return answer;
        }

        protected void onProgressUpdate(String... values) {
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.dismiss();
            Snackbar.make(relativeLayout, "File Downloaded to EncryptedFileManager Folder", Snackbar.LENGTH_SHORT).show();

        }
    }

    public class GetUsersListTask extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.getusers(params[0]);
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
            Log.d(TAG, "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> message = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(FileDetail_Activity.this, message.first, message.second
                        , false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("ok")) {
                        JSONArray jsonArray = jsonObject.getJSONArray("Data");

                        mUserName = new ArrayList<String>();
                        mUserNumber = new ArrayList<String>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            mUserName.add(jsonObject1.getString("data0"));
                            mUserNumber.add(jsonObject1.getString("data1"));

                            String source = "<b>" + jsonObject1.getString("data0")
                                    + "</b><br>" + jsonObject1.getString("data1");

                            arrayAdapter.add(Html.fromHtml(source).toString());
                        }


                    } else if (jsonObject.getString("status").equalsIgnoreCase("false")) {


                    } else {
                        String error = jsonObject.getString("Error");
                        Log.d(TAG, "onPostExecute: Error " + error);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(FileDetail_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}

