package com.example.android.bluetoothlegatt;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickBtn1(View v){
        deadlyIntent(1);
    }

    public void onClickBtn2(View v){
        deadlyIntent(2);
    }

    public void onClickUpdate(View v){
        EditText e = (EditText) findViewById(R.id.edit_label);
        String label = e.getText().toString();
    }

    private void deadlyIntent(int i){
        startActivityForResult(new Intent(this, DeviceScanActivity.class), i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode == RESULT_OK){

            if(requestCode == 1){
                String name = data.getStringExtra("DEVICE_NAME");
                String address = data.getStringExtra("DEVICE_ADDRESS");

                TextView t = (TextView) findViewById(R.id.txt_1);
                t.setText("[1] " + name + " " + address);

            }

            if(requestCode == 2){
                String name = data.getStringExtra("DEVICE_NAME");
                String address = data.getStringExtra("DEVICE_ADDRESS");

                TextView t = (TextView) findViewById(R.id.txt_2);
                t.setText("[2] " + name + " " + address);
            }
        }

    }

}
