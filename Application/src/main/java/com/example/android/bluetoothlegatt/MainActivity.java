package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends Activity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private String mAddress1 = null;
    private String mBaseText1 = null;
    private String mAddress2 = null;
    private String mBaseText2 = null;
    private String mLabel = "default_label";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothGatt mSensor1;
    private int mSensor1State = STATE_DISCONNECTED;

    private BluetoothGatt mSensor2;
    private int mSensor2State = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static final String WICED_SERVICE = SampleGattAttributes.WICED_DATA_SERVICE;
    private static final String WICED_CHAR = SampleGattAttributes.WICED_DATA_CHARACTERISTIC;

    private static BraceApi mBraceApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!initBle()){
            Toast.makeText(getBaseContext(), "!", Toast.LENGTH_SHORT).show();
        }

        mBraceApi = new BraceApi();
    }

    public void onClickBtn1(View v){
        deadlyIntent(1);
    }

    public void onClickBtn2(View v){
        deadlyIntent(2);
    }

    public void onClickUpdateLabel(View v){
        EditText e = (EditText) findViewById(R.id.edit_label);
        mLabel = e.getText().toString();

    }

    public void onClickDisconnect(View v){
        if(mSensor1 != null){
            mSensor1.disconnect();
            statusText1("disconnected");
        }
        if(mSensor2 != null){
            mSensor2.disconnect();
            statusText2("disconnected");
        }
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

                mBaseText1 = "[1] " + name + " " + address + " ";

                statusText1("connecting..");

                mAddress1 = address;

                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mAddress1);
                if (device == null) {
                    Log.w(TAG, "Device not found.  Unable to connect.");
                    statusText1("can't find device");
                    return;
//            return false;
                }
                // We want to directly connect to the device, so we are setting the autoConnect
                // parameter to false.
                mSensor1 = device.connectGatt(this, false, mGattCallback);
                Log.d(TAG, "Trying to create a new connection.");
                mSensor1State = STATE_CONNECTING;

            }

            if(requestCode == 2){
                String name = data.getStringExtra("DEVICE_NAME");
                String address = data.getStringExtra("DEVICE_ADDRESS");

                mBaseText2 = "[2] " + name + " " + address + " ";

                statusText2("connecting..");

                mAddress2 = address;

                final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mAddress2);
                if (device == null) {
                    Log.w(TAG, "Device not found.  Unable to connect.");
                    statusText1("can't find device");
                    return;
//            return false;
                }
                // We want to directly connect to the device, so we are setting the autoConnect
                // parameter to false.
                mSensor2 = device.connectGatt(this, false, mGattCallback2);
                Log.d(TAG, "Trying to create a new connection.");
                mSensor2State = STATE_CONNECTING;
            }
        }

    }

    private boolean initBle(){

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;


    }

    private void toast(String message){
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void statusText1(String status){

        final String status2 = status;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView t = (TextView) findViewById(R.id.txt_1);
                t.setText(mBaseText1 + status2);
            }
        });

    }

    private void statusText2(String status){

        final String status2 = status;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView t = (TextView) findViewById(R.id.txt_2);
                t.setText(mBaseText2 + status2);
            }
        });

    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mSensor1State = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                statusText1("connected");

                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mSensor1.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mSensor1State = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                statusText1("disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                List<BluetoothGattService> services = mSensor1.getServices();
                for(BluetoothGattService service : services){
                    if(service.getUuid().toString().equals(WICED_SERVICE)){
                        Log.w(TAG, "found the wiced service");
                        List<BluetoothGattCharacteristic> chars = service.getCharacteristics();
                        for(BluetoothGattCharacteristic c : chars){
                            if(c.getUuid().toString().equals(WICED_CHAR)){
                                Log.w(TAG, "found the wiced char");
                                mSensor1.setCharacteristicNotification(c, true);
                                BluetoothGattDescriptor descriptor = c.getDescriptor(
                                        UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                mSensor1.writeDescriptor(descriptor);

                            }

                        }

                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt,
//                                         BluetoothGattCharacteristic characteristic,
//                                         int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.w(TAG, "READ!");
//                StringBuilder sb = new StringBuilder();
//                byte[] bytes = characteristic.getValue();
//                for(byte b : bytes){
//                    sb.append(Integer.toHexString(b));
//                    sb.append(" ");
//                }
//                Log.w(TAG, sb.toString());
//            }
//            mSensor1.readCharacteristic(characteristic);
//        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

//            Log.w(TAG, "UPDATE 1");
//            StringBuilder sb = new StringBuilder();
            byte[] bytes = characteristic.getValue();
//            for(byte b : bytes){
//                sb.append(Integer.toHexString(b));
//                sb.append(" ");
//            }
//            Log.w(TAG, sb.toString());

            SensorData sensorData = new SensorData(bytes, mLabel, mAddress1);

            mBraceApi.postData(sensorData);

        }
    };

    private final BluetoothGattCallback mGattCallback2 = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mSensor2State = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                statusText2("connected");

                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mSensor2.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mSensor2State = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                statusText2("disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                List<BluetoothGattService> services = mSensor2.getServices();
                for(BluetoothGattService service : services){
                    if(service.getUuid().toString().equals(WICED_SERVICE)){
                        Log.w(TAG, "found the wiced service");
                        List<BluetoothGattCharacteristic> chars = service.getCharacteristics();
                        for(BluetoothGattCharacteristic c : chars){
                            if(c.getUuid().toString().equals(WICED_CHAR)){
                                Log.w(TAG, "found the wiced char");
                                mSensor2.setCharacteristicNotification(c, true);
                                BluetoothGattDescriptor descriptor = c.getDescriptor(
                                        UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                mSensor2.writeDescriptor(descriptor);

                            }

                        }

                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt,
//                                         BluetoothGattCharacteristic characteristic,
//                                         int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.w(TAG, "READ!");
//                StringBuilder sb = new StringBuilder();
//                byte[] bytes = characteristic.getValue();
//                for(byte b : bytes){
//                    sb.append(Integer.toHexString(b));
//                    sb.append(" ");
//                }
//                Log.w(TAG, sb.toString());
//            }
//            mSensor1.readCharacteristic(characteristic);
//        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

//            Log.w(TAG, "UPDATE 2");
//            StringBuilder sb = new StringBuilder();
            byte[] bytes = characteristic.getValue();
//            for(byte b : bytes){
//                sb.append(Integer.toHexString(b));
//                sb.append(" ");
//            }
//            Log.w(TAG, sb.toString());

            SensorData sensorData = new SensorData(bytes, mLabel, mAddress2);

            mBraceApi.postData(sensorData);

        }
    };

}


