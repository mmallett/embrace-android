package com.example.android.bluetoothlegatt;

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

        byte bitmask = data[0];

        // fix sign errors

        int x = ((int)data[2]) << 8 | ((int)data[1]);
        int y = ((int)data[4]) << 8 | ((int)data[3]);
        int z = ((int)data[6]) << 8 | ((int)data[5]);

        Vector v = new Vector(x,y,z);

        if((bitmask & SENSOR_FLAG_ACCEL) > 0){
            accel = v;
            type = TYPE_ACCEL;
        }

        else if((bitmask & SENSOR_FLAG_GYRO) > 0){
            gyro = v;
            type = TYPE_GYRO;
        }

        else{
            type = TYPE_NULL;
        }



    }


}
