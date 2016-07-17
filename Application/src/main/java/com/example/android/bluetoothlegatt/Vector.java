package com.example.android.bluetoothlegatt;

/**
 * Created by matt on 7/13/16.
 */
public class Vector {


    public final int x;
    public final int y;
    public final int z;

    public Vector(int x, int y, int z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString(){

        return "<" + x + ", " + y + ", " + z + " >";

    }
}
