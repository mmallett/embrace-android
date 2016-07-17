package com.example.android.bluetoothlegatt;

import android.util.Log;

/**
 * Created by matt on 7/13/16.
 */
public class SensorData {

    public static final int SENSOR_FLAG_ACCEL = (0x1 << 0);
    public static final int SENSOR_FLAG_GYRO = (0x1 << 1);
    public static final int SENSOR_FLAG_HUMIDITY = (0x1 << 2);
    public static final int SENSOR_FLAG_MAGNO = (0x1 << 3);
    public static final int SENSOR_FLAG_PRESSURE = (0x1 << 4);
    public static final int SENSOR_FLAG_TEMP = (0x1 << 5);

    public Vector accel;
    public Vector gyro;

    private byte[] data;

    public static final String TYPE_ACCEL = "ACCEL";
    public static final String TYPE_GYRO = "GYRO";
    public static final String TYPE_NULL = "null";

    public String type;



    public SensorData(byte[] data){

        this.data = data;

        parseData();

    }


    private void parseData(){

        StringBuilder b = new StringBuilder();

        b.append(data.length);
        b.append(" ");

        for(byte by : data){
            b.append(Integer.toHexString(by));
            b.append(" ");
        }

        byte bitmask = data[0];

        // fix sign errors

        int x = ((int)data[2]) << 8 | ((int)data[1]);
        int y = ((int)data[4]) << 8 | ((int)data[3]);
        int z = ((int)data[6]) << 8 | ((int)data[5]);

        Vector v = new Vector(x,y,z);

        if(((bitmask & SENSOR_FLAG_ACCEL) > 0) && ((bitmask & SENSOR_FLAG_GYRO) > 0)){
            type = TYPE_ACCEL;

            accel = new Vector(
                leint16(data[1], data[2]),
                leint16(data[3], data[4]),
                leint16(data[5], data[6])
            );

            gyro = new Vector(
                leint16(data[7], data[8]),
                leint16(data[9], data[10]),
                leint16(data[11], data[12])
            );

            Log.d("PARSE DATA", "ACCEL " + accel.toString());
            Log.d("PARSE DATA", "GYRO " + gyro.toString());

        }

        else{
            type = TYPE_NULL;
        }



    }

    private int leint16(byte a, byte b){

        int top = ((int) b) << 8;
        int bot = ((int) a) & 0x0F;

        return top | bot;

    }

    private int uint8(byte b){

        int i = ((int) b) & 0x0F;
        return i;

    }


}
