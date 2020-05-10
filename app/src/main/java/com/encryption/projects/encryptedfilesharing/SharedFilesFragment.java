package com.encryption.projects.encryptedfilesharing;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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

public class SharedFilesFragment extends Fragment {

    protected View mView;
    EditText SearchFile;
    ImageView SearchFile_Btn;
    RelativeLayout SharedFileLayout;
    SharedPreferences pref;

    ArrayList<String> msg_name;
    ArrayList<String> msg_body;

    Dialog mDialog;

    String BfKey, MobileNo;

    Authentication authentication;

    TextView FileName, FileDescription, FileSize, FileDateTime;
    Button FileDownload;
    String AesPath;

    LinearLayout Before_Search;
    RelativeLayout After_Search;

    String DownloadStatus;

    ProgressDialog pd;
    FTPClient mFTPClient;
    CopyStreamAdapter streamListener;
    Long size = null;

    String FName, File_Type;
    RelativeLayout relativeLayout;
    double FSize;
    DecimalFormat df = new DecimalFormat("#.00");


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.sharedfiles_layout, container, false);
        pref = getActivity().getSharedPreferences("EncryptedFileSharing", Context.MODE_PRIVATE);
        String mobileNo_pref = pref.getString("UserId", "");
        relativeLayout = mView.findViewById(R.id.share_file_layout);

        authentication = new Authentication(getActivity());

        mDialog = new Dialog(getActivity(), R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        init();
        return mView;
    }


    public void init() {

        SearchFile = mView.findViewById(R.id.search_file_name);
        SearchFile_Btn = mView.findViewById(R.id.search_file);
        SharedFileLayout = mView.findViewById(R.id.share_file_layout);

        FileName = mView.findViewById(R.id.file_name);
        FileDescription = mView.findViewById(R.id.description_text);
        FileSize = mView.findViewById(R.id.size_text);
        FileDateTime = mView.findViewById(R.id.date_time);
        FileDownload = mView.findViewById(R.id.file_download);

        Before_Search = mView.findViewById(R.id.before_search);
        After_Search = mView.findViewById(R.id.after_search);

        SearchFile_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                if (SearchFile.getText().toString().equals("")) {
                    Snackbar.make(SharedFileLayout, "Enter File Name", Snackbar.LENGTH_LONG).show();
                    SearchFile.requestFocus();
                } else {
                    getMessages();
                }
            }
        });


        FileDownload.setOnClickListener(new View.OnClickListener() {
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
                    new DownloadFileTask().execute(finalfile.getAbsolutePath(), fPath[3], AesPath);
                }
            }
        });

    }

    public void getMessages() {

        Uri uriSMSURI = Uri.parse("content://sms/inbox");
        Cursor cur = getActivity().getContentResolver().query(uriSMSURI, new String[]{"address", "body"}, "body LIKE '%EFM:%'", null, null);
        String sms = "";
        msg_name = new ArrayList<String>();
        msg_body = new ArrayList<String>();


        if (cur.getCount() == 0) {

            Toast.makeText(getActivity(), "File Id Does Not Exist", Toast.LENGTH_SHORT).show();
            After_Search.setVisibility(View.GONE);
        } else {

            while (cur.moveToNext()) {
                msg_name.add(cur.getString(0));
                String b = cur.getString(1);
                msg_body.add(b);
                sms += cur.getString(1);
            }

            new CheckUserDetails().execute(SearchFile.getText().toString());
//            CheckFiles(SearchFile.getText().toString());

        }


    }

    private void CheckFiles(String fileIds) {
        for (int i = 0; i < msg_body.size(); i++) {

            String[] temp1 = msg_body.get(i).split(":");

            try {
                Log.d("BFKEY", "CheckFiles: " + msg_body.get(i));
                String BfDecryptResponse = authentication.BF_decrypt(temp1[1]);
                Log.d("BFKEY", "CheckFiles: Decrypted = " + BfDecryptResponse);

                String[] decrypt = BfDecryptResponse.split("\\*");

                String fid = decrypt[0];
                String userId = decrypt[1];


                if (userId.compareTo(MobileNo) == 0 && fid.compareTo(fileIds) == 0) {
                    new SearchFileTask().execute(fileIds);
                    break;
                } else {
                    if (i == msg_body.size()) {
                        Toast.makeText(getActivity(), "File Id does not exist", Toast.LENGTH_SHORT).show();
                        After_Search.setVisibility(View.GONE);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Android Runtime Permission
    private boolean weHavePermission() {
        return (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestforPermissionFirst() {
        if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) || (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) || (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE)) || (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.SEND_SMS))) {
            requestForResultContactsPermission();
        } else {
            requestForResultContactsPermission();
        }
    }

    private void requestForResultContactsPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS}, 111);
    }

    public class SearchFileTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.getfiledetails(params[0]);
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
            Log.d("FILES", "onPostExecute: " + s);
            if (Utility.checkConnection(s)) {
                mDialog.dismiss();
                Pair<String, String> message = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), message.first, message.second
                        , false);
            } else {

                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("ok")) {
                        Before_Search.setVisibility(View.VISIBLE);
                        After_Search.setVisibility(View.VISIBLE);
                        mDialog.dismiss();
                        JSONArray json = jsonObject.getJSONArray("Data");
                        FilesModel mCurrentFile;
                        JSONObject jsonObject1 = json.getJSONObject(0);
                        //Name, Extension, Desc, Size, Datetime, FilePath
                        mCurrentFile = new FilesModel(""
                                , jsonObject1.getString("data0")
                                , jsonObject1.getString("data1")
                                , jsonObject1.getString("data2")
                                , jsonObject1.getString("data3")
                                , jsonObject1.getString("data4")
                                , ""
                                , jsonObject1.getString("data5"));

                        FName = mCurrentFile.getFileName();
                        File_Type = mCurrentFile.getFileExtension();

                        FileName.setText(FName + File_Type);
                        FileDescription.setText(mCurrentFile.getFileDescription());

                        FSize = Double.parseDouble(mCurrentFile.getFileSize());

                        double file_size = FSize / 1024;

                        if (file_size >= 1000) {
                            file_size = file_size / 1024;
                            String tempSize = "<b>Size: </b>" + df.format(file_size) + " Mb";
                            FileSize.setText(Html.fromHtml(tempSize));
                        } else {
                            String tempSize = "<b>Size: </b>" + df.format(file_size) + " Kb";
                            FileSize.setText(Html.fromHtml(tempSize));
                        }

//                FileSize.setText(temp[3]);
                        FileDateTime.setText(mCurrentFile.getFileDateTime());
                        AesPath = mCurrentFile.getFilePath();


                    } else if (jsonObject.getString("status").equalsIgnoreCase("false")) {
                        mDialog.dismiss();
                        Utility.ShowAlertDialog(getActivity(), "No Files Found!"
                                , "Seems you have not UploadFileTask any file. To UploadFileTask a new file click add button."
                                , false);

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

    public class CheckUserDetails extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.getUserDetails(params[0]);
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
                mDialog.dismiss();
                Pair<String, String> message = Utility.GetErrorMessage(s);
                Utility.ShowAlertDialog(getActivity(), message.first, message.second
                        , false);
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equalsIgnoreCase("ok")) {
                        JSONObject jsonObject1 = jsonObject.getJSONArray("Data")
                                .getJSONObject(0);
                        MobileNo = jsonObject1.getString("data0");
                        CheckFiles(SearchFile.getText().toString());

                    } else if (jsonObject.getString("status").equalsIgnoreCase("false")) {
                        mDialog.dismiss();
                        Utility.ShowAlertDialog(getActivity(), "No User Found!"
                                , "Could not find user details for the file you have searched."
                                , false);

                    } else {
                        mDialog.dismiss();
                        String error = jsonObject.getString("Error");
                        Log.d("Register", "onPostExecute: Error " + error);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class DownloadFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getActivity());
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

                if (mFTPClient.login(BuildConfig.USER, BuildConfig.PASSWORD)) {
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
                Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
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
                int flength = (int) fileAES1.length();
                byte[] AESEResbytes = new byte[flength];
                FileInputStream in = new FileInputStream(fileAES1);
                in.read(AESEResbytes);
                in.close();

                byte[] dec = authentication.decrypt(AESEResbytes);

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
            if (s.compareTo("true") == 0) {
                Snackbar.make(relativeLayout, "File Downloaded to EncryptedFileManager Folder", Snackbar.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
            }

        }
    }


}


