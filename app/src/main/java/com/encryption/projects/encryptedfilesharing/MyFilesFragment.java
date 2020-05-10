package com.encryption.projects.encryptedfilesharing;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.encryption.projects.encryptedfilesharing.webservices.JSONPARSE;
import com.encryption.projects.encryptedfilesharing.webservices.RestAPI;
import com.encryption.projects.encryptedfilesharing.webservices.Utility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class MyFilesFragment extends Fragment {

    public static final String TAG = "FILES";
    protected View mView;
    private String mobileNo_Pref;
    private SharedPreferences pref;

    private RecyclerView list;
    private ArrayList<FilesModel> data;

    private String FileName, File_Type, Path, Fid;

    private FloatingActionButton floatingActionButton;

    private Dialog mDialog;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.myfiles_layout, container, false);

        mDialog = new Dialog(getActivity(), R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.circular_dialog);
        mDialog.setCancelable(false);

        list = mView.findViewById(R.id.filelist);
        floatingActionButton = mView.findViewById(R.id.addFloatButton);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadFiles(v);
            }
        });

        pref = getActivity().getSharedPreferences("EncryptedFileSharing", Context.MODE_PRIVATE);
        mobileNo_Pref = pref.getString("UserId", "");

        return mView;
    }


    @Override
    public void onResume() {
        super.onResume();
        new GetFileListTask().execute(mobileNo_Pref);

    }

    public String FileType(String type) {

        if (type.contains("jpg") || type.contains("tiff") || type.contains("jpeg") || type.contains("bmp") || type.contains("gif") || type.contains("png")) {

            return "Image";
        } else if (type.contains("doc") || type.contains("docx") || type.contains("odt") || type.contains("pdf") || type.contains("txt") || type.contains("xml")) {

            return "Document";
        } else if (type.contains("mkv") || type.contains("flv") || type.contains("avi") || type.contains("wmv") || type.contains("mp4") || type.contains("3gp")) {

            return "Video";
        } else if (type.contains("mp3") || type.contains("msv") || type.contains("wav") || type.contains("wma") || type.contains("ogg") || type.contains("ram")) {

            return "Music";
        } else {

            return "Other";
        }

    }

    public void loadFiles(View view) {
        Intent i = new Intent();
        i.setType("*/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select a Files"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && data != null && data.getData() != null) {

            Uri uri = data.getData();
            String path = ImageFilePath.getPath(getActivity(), uri);

            Intent intent = new Intent(getActivity(), UploadFile_Activity.class);
            intent.putExtra("FilePath", path);
            startActivity(intent);

        }
    }

    public class GetFileListTask extends AsyncTask<String, JSONObject, String> {

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
                JSONObject json = api.getfiles(params[0]);
                Log.d(TAG, "doInBackground: " + json.toString());
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
                        JSONArray json = jsonObject.getJSONArray("Data");
                        data = new ArrayList<FilesModel>();
                        for (int i = 0; i < json.length(); i++) {
                            JSONObject jsonObject1 = json.getJSONObject(i);
                            data.add(new FilesModel(jsonObject1.getString("data0")
                                    , jsonObject1.getString("data1")
                                    , jsonObject1.getString("data2")
                                    , jsonObject1.getString("data3")
                                    , jsonObject1.getString("data4")
                                    , jsonObject1.getString("data5")
                                    , jsonObject1.getString("data6")
                                    , jsonObject1.getString("data7")));
                        }

                        RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity(), 2);
                        list.setLayoutManager(lm);
                        FileAdapter adapt = new FileAdapter(data);
                        list.setAdapter(adapt);

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

    public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

        ArrayList<FilesModel> dataset;
        Context con;
        int i = 0;


        public FileAdapter(ArrayList<FilesModel> d) {
            dataset = d;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_cardview, parent, false);
            ViewHolder vh = new ViewHolder(v);
            con = parent.getContext();
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            //Fid * Name * Extension * Description * Size * DateTime * AESPath * BFPath #

            final FilesModel temp = dataset.get(position);


            holder.name.setText(temp.getFileName());

            String type = temp.getFileExtension();
            String FileFormat = FileType(type);

            Fid = temp.getFileId();
            FileName = temp.getFileName();
            File_Type = temp.getFileExtension();
            Path = temp.getFilePath();

            if (FileFormat.equals("Image")) {

                holder.cardOptions.setImageResource(R.drawable.image_type);

            } else if (FileFormat.equals("Document")) {

                holder.cardOptions.setImageResource(R.drawable.document_type);

            } else if (FileFormat.equals("Video")) {

                holder.cardOptions.setImageResource(R.drawable.video_type);

            } else if (FileFormat.equals("Music")) {

                holder.cardOptions.setImageResource(R.drawable.music_type);

            } else {

                holder.cardOptions.setImageResource(R.drawable.all_type);

            }

            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Fid * Name * Extension * Description * Size * DateTime * AESPath * BFPath #

                    Intent intent = new Intent(getActivity(), FileDetail_Activity.class);
                    intent.putExtra("fid", dataset.get(position).getFileId());
                    intent.putExtra("fName", dataset.get(position).getFileName());
                    intent.putExtra("fType", dataset.get(position).getFileExtension());
                    intent.putExtra("fDescription", dataset.get(position).getFileDescription());
                    intent.putExtra("fSize", dataset.get(position).getFileSize());
                    intent.putExtra("fDatetime", dataset.get(position).getFileDateTime());
                    intent.putExtra("fAesPath", dataset.get(position).getFilePath());
                    startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            return dataset.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView name;
            public CardView card;
            public ImageView cardOptions;

            public ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.file_name);
                card = v.findViewById(R.id.card);
                cardOptions = v.findViewById(R.id.card_pic);
            }
        }
    }


}

