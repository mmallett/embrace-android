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
    private String mLabel = "default_label";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static final String WICED_SERVICE = SampleGattAttributes.WICED_DATA_SERVICE;
    private static final String WICED_CHAR = SampleGattAttributes.WICED_DATA_CHARACTERISTIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!initBle()){
            Toast.makeText(getBaseContext(), "!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickBtn1(View v){
        deadlyIntent(1);
    }

    public void onClickBtn2(View v){
        deadlyIntent(2);
    }

    public void onClickCollect(View v){
        EditText e = (EditText) findViewById(R.id.edit_label);
        mLabel = e.getText().toString();

//        if(mAddress1 == null || mAddress2 == null){
//            toast("Sensors not connected!");
//
//            return;
//        }

//        Intent serviceIntent = new Intent(getBaseContext(), BleService.class);
//        serviceIntent.putExtra("ADDRESS_1", mAddress1);
//        serviceIntent.putExtra("ADDRESS_2", mAddress2);
//        serviceIntent.putExtra("LABEL", mLabel);
//
//        startService(serviceIntent);
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
//            return false;
                }
                // We want to directly connect to the device, so we are setting the autoConnect
                // parameter to false.
                mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
                Log.d(TAG, "Trying to create a new connection.");
                mConnectionState = STATE_CONNECTING;

            }

            if(requestCode == 2){
                String name = data.getStringExtra("DEVICE_NAME");
                String address = data.getStringExtra("DEVICE_ADDRESS");

                TextView t = (TextView) findViewById(R.id.txt_2);
                t.setText("[2] " + name + " " + address);

                mAddress2 = address;
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

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                statusText1("connected");

                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                statusText1("disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                List<BluetoothGattService> services = mBluetoothGatt.getServices();
                for(BluetoothGattService service : services){
                    if(service.getUuid().toString().equals(WICED_SERVICE)){
                        Log.w(TAG, "found the wiced service");
                        List<BluetoothGattCharacteristic> chars = service.getCharacteristics();
                        for(BluetoothGattCharacteristic c : chars){
                            if(c.getUuid().toString().equals(WICED_CHAR)){
                                Log.w(TAG, "found the wiced char");
                                mBluetoothGatt.setCharacteristicNotification(c, true);
                                BluetoothGattDescriptor descriptor = c.getDescriptor(
                                        UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                mBluetoothGatt.writeDescriptor(descriptor);

                            }

                        }

                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "READ!");
                StringBuilder sb = new StringBuilder();
                byte[] bytes = characteristic.getValue();
                for(byte b : bytes){
                    sb.append(Integer.toHexString(b));
                    sb.append(" ");
                }
                Log.w(TAG, sb.toString());
            }
            mBluetoothGatt.readCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

            Log.w(TAG, "UPDATE!");
            StringBuilder sb = new StringBuilder();
            byte[] bytes = characteristic.getValue();
            for(byte b : bytes){
                sb.append(Integer.toHexString(b));
                sb.append(" ");
            }
            Log.w(TAG, sb.toString());

            SensorData sensorData = new SensorData(bytes, mLabel, mAddress1);

            Random r = new Random();
            if(r.nextInt(1000) > 990) {
                BraceApi api = new BraceApi();

                api.postData(sensorData);
            }

        }
    };

}


