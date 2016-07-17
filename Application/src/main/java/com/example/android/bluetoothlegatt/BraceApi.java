package com.example.android.bluetoothlegatt;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by matt on 7/13/16.
 */
public class BraceApi {

    public static final String HOST = "http://54.208.116.19:8111";

    private static final String tag = "BRACEAPI";

    public BraceApi(){

    }

    public void postData(SensorData data){

        if(data.type.equals(SensorData.TYPE_NULL)){
            Log.d(tag, "null data");
            return;
        }

        try{

//            HttpURLConnection con = (HttpURLConnection) url.openConnection();
//            con.setDoOutput(true);
//            con.setDoInput(true);
//            con.setRequestProperty("Content-Type", "application/json");
//            con.setRequestProperty("Accept", "application/json");
//            con.setRequestMethod("POST");


            JSONObject json = new JSONObject();
            json.put("time", System.currentTimeMillis());

            if(data.type.equals(SensorData.TYPE_ACCEL)){
                json.put("accel_x", data.accel.x);
                json.put("accel_y", data.accel.y);
                json.put("accel_z", data.accel.z);
                json.put("gyro_x", data.gyro.x);
                json.put("gyro_y", data.gyro.y);
                json.put("gyro_z", data.gyro.z);
                json.put("label", data.label);
                json.put("device", data.device);

                HttpTask http = new HttpTask();

                http.execute(json);
            }


//            OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream());
//            w.write(json.toString());
//            w.flush();
//
//            Log.d(tag, json.toString());
//
//            int responseCode = con.getResponseCode();
//            ret = responseCode == HttpURLConnection.HTTP_CREATED;
//
//            Log.d(tag, Integer.toString(responseCode));
//
//            w.close();

        }
        catch(Exception e){

            Log.d(tag, e.toString());
        }

    }

    private class HttpTask extends AsyncTask<Object, Void, Boolean> {

        protected Boolean doInBackground(Object... args){

            boolean ret = false;

            try{
                URL url = new URL(HOST + "/");
                JSONObject json = (JSONObject) args[0];

                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");


                OutputStreamWriter w = new OutputStreamWriter(con.getOutputStream());
                w.write(json.toString());
                w.flush();

                Log.d(tag, json.toString());

                int responseCode = con.getResponseCode();
                ret = responseCode == HttpURLConnection.HTTP_CREATED;

                Log.d(tag, Integer.toString(responseCode));

                w.close();
            }
            catch(Exception e){

                Log.d(tag, e.toString());
                return false;
            }

            return ret;
        }

    }

}
