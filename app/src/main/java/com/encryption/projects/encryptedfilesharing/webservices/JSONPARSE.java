package com.encryption.projects.encryptedfilesharing.webservices;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONPARSE {

    public String parse(JSONObject json){
        String name = " ";
        try {
            name = json.getString("Value");
        } catch (JSONException e) {
//            e.printStackTrace();
            name =e.getMessage();

        }
        return name;
    }

}
