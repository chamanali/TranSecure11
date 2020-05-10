package com.encryption.projects.encryptedfilesharing;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.encryption.projects.encryptedfilesharing.webservices.JSONPARSE;
import com.encryption.projects.encryptedfilesharing.webservices.RestAPI;
import com.encryption.projects.encryptedfilesharing.webservices.Utility;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class UploadFile_Activity extends AppCompatActivity {

    protected EditText FileName, FileDescription, FileSize, FilePath;
    protected TextView FileType;
    protected Button Upload;
    SimpleDateFormat sdfd = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat sdft = new SimpleDateFormat("HH:mm");
    String path, File_Size;
    Authentication authetication;


    FTPClient mFTPClient;
    CopyStreamAdapter streamListener;
    ProgressDialog pd;


    SharedPreferences pref;
    String userId;
    String DestinationAes;


    RelativeLayout relativeLayout;
    Context con;

    DecimalFormat df = new DecimalFormat("#.00");

    Dialog mDialog;
    String AESEFilePath;
    String Status;
    String FilesDescription;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_upload_activity);
        getSupportActionBar().setTitle("Upload");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        pref = getSharedPreferences("EncryptedFileSharing", Context.MODE_PRIVATE);
        userId = pref.getString("UserId", "");
        authetication = new Authentication(UploadFile_Activity.this);

        Intent intent = getIntent();
        path = intent.getStringExtra("FilePath");

        mDialog = new Dialog(UploadFile_Activity.this, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        init();
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


    public void init() {


        FileName = findViewById(R.id.file_name);
        FileDescription = findViewById(R.id.description);
        FileSize = findViewById(R.id.file_size);
        FilePath = findViewById(R.id.file_path);

        FileType = findViewById(R.id.file_type);
        Upload = findViewById(R.id.upload_Button);
        relativeLayout = findViewById(R.id.activity_fileUpload);

        File file = new File(path);
        try {

            String fname = file.getName();
            String[] fnameDetails = fname.split("\\.");
            final String fileName = fnameDetails[0];
            final String fileType = "." + fnameDetails[1];

            File_Size = String.valueOf(file.length());

            double file_size = Integer.parseInt(String.valueOf(file.length() / 1024));

            if (file_size >= 4) {
                file_size = file_size / 1024;
                FileSize.setText(df.format(file_size) + " Mb");
            } else {
                FileSize.setText(df.format(file_size) + " Kb");
            }

            String FilesName = URLEncoder.encode(fileName, "UTF-8");
            FileName.setText(FilesName);
            FileName.setSelection(FileName.length());
            FileType.setText(fileType);
            FilePath.setText(path);


        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (FileName.getText().toString().equals("")) {
                    Toast.makeText(UploadFile_Activity.this, "File Name is required", Toast.LENGTH_SHORT).show();
                    FileName.requestFocus();
                } else if (FileDescription.getText().toString().equals("")) {
                    Toast.makeText(UploadFile_Activity.this, "File Description is required", Toast.LENGTH_SHORT).show();
                    FileDescription.requestFocus();
                }
            }
        });


    }

    public byte[] toByteArray(InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read = 0;
            byte[] buffer = new byte[1024];
            while (read != -1) {
                read = in.read(buffer);
                if (read != -1)
                    out.write(buffer, 0, read);
            }
            out.close();
            return out.toByteArray();
        } catch (Exception e) {
            Toast.makeText(UploadFile_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }


    }

    public class EncryptionTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }


        @Override
        protected String doInBackground(String... params) {
            String answer = "";
            try {

                InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(new File(params[0])));
                byte[] byteData = new byte[inputStream.available()];
                byteData = toByteArray(inputStream);


//                AES Encryption
                byte[] AESEncryptionResult = authetication.encrypt(byteData);

                File file = new File("/sdcard/EncryptedFileManager/temp");
                file.mkdirs();
                File fileAES = new File(file.getAbsolutePath(), "aes.txt");
                FileOutputStream stream = new FileOutputStream(fileAES);
                stream.write(AESEncryptionResult);
                stream.close();

                AESEFilePath = fileAES.getAbsolutePath();


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
            Status = "Aes";
            new UploadFileTask().execute(AESEFilePath, DestinationAes);
        }
    }

    public class UploadFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(UploadFile_Activity.this);
            pd.requestWindowFeature(Window.FEATURE_NO_TITLE);
            pd.setMessage("Uploading...");
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

                    BufferedInputStream buffIn = null;
                    final File file = new File(params[0]);//File Source Path
                    buffIn = new BufferedInputStream(new FileInputStream(file));
                    mFTPClient.enterLocalPassiveMode();
                    streamListener = new CopyStreamAdapter() {

                        @Override
                        public void bytesTransferred(long totalBytesTransferred,
                                                     int bytesTransferred, long streamSize) {

                            int percent = (int) (totalBytesTransferred * 100 / file.length());
                            pd.setProgress(percent);
                            publishProgress();

                            if (totalBytesTransferred == file.length()) {
                                System.out.println("100% transfered");

                                removeCopyStreamListener(streamListener);

                            }

                        }

                    };
                    mFTPClient.setCopyStreamListener(streamListener);

                    boolean status = mFTPClient.storeFile(params[1], buffIn);
                    if (status) {
                        ans = "true";
                    } else {
                        ans = "false";
                    }

                    buffIn.close();
                    mFTPClient.logout();
                    mFTPClient.disconnect();

                } else {
                    ans = "No unsucessfull";
                }
            } catch (Exception e) {
                e.printStackTrace();
                ans = "Exp:" + e.getMessage();
            }
            return ans;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("UPLOAD", "onPostExecute: " + s);
            pd.cancel();
            if (s.compareTo("true") == 0) {

//                string name, string extension, string size, string datetime, string aespath,
//               , string mobile

                if (Status.compareTo("Aes") == 0) {

                    Date dt = new Date();
                    String date = sdfd.format(dt.getTime());
                    String time = sdft.format(dt.getTime());

                    String FDescription = FileDescription.getText().toString();
                    try {
                        FilesDescription = URLEncoder.encode(FDescription, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    //String name,String extension,String desc,String size,String datetime,String filePath,String mobile
                    new AddNewFile().execute(FileName.getText().toString(), FileType.getText().toString()
                            , FilesDescription, File_Size
                            , date + " " + time, DestinationAes, userId);

                }

            } else if (s.compareTo("false") == 0) {
                Toast.makeText(UploadFile_Activity.this, "Something went Wrong, Try sending the file again!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class AddNewFile extends AsyncTask<String, JSONObject, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.addfile(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
                Log.d("UPLOAD", "onPostExecute: " + json.toString());
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
            Log.d("UPLOAD", "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                Pair<String, String> message = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(UploadFile_Activity.this, message.first, message.second
                        , false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("true")) {
                        Snackbar.make(relativeLayout, "File Uploaded successfully", Snackbar.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1500);
                    } else {
                        String error = jsonObject.getString("Error");
                        Log.d("UPLOAD", "onPostExecute: Error " + error);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(con, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


}

