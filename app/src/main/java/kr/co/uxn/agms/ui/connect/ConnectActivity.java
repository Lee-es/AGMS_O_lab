package kr.co.uxn.agms.ui.connect;


import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.service.BluetoothService;
import kr.co.uxn.agms.ui.BleActivity;

public class ConnectActivity extends BleActivity {



    Button buttonWarmUp;
    Button connect;

    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_connect);
        buttonWarmUp = findViewById(R.id.warm_up);
        buttonWarmUp.setEnabled(false);
        connect = findViewById(R.id.connect);


        editText = findViewById(R.id.input);

        buttonWarmUp.setEnabled(false);



        connect.setOnClickListener(view -> {
            connect.setEnabled(false);
            boolean result = runDeviceConnect();
            long delay = 500;
            if(!result){
                delay = 20000;
            }
            connect.postDelayed(() -> connect.setEnabled(true),delay);
        });

        buttonWarmUp.setOnClickListener(view -> {
            buttonWarmUp.setEnabled(false);
            buttonWarmUp.postDelayed(() -> buttonWarmUp.setEnabled(true),1000);
            PreferenceManager.getDefaultSharedPreferences(ConnectActivity.this)
                    .edit()
                    .putLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE, System.currentTimeMillis())
                    .putLong(CommonConstant.PREF_WARM_UP_START_DATE, System.currentTimeMillis())
                    .apply();

            goWarmup();
        });
        startScan();
    }
    private boolean runDeviceConnect(){
        if(TextUtils.isEmpty(editText.getText())){
            Toast.makeText(this,"Invalid address",Toast.LENGTH_SHORT).show();
            return false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit().putLong(CommonConstant.PREF_WARM_UP_START_DATE,0)
                .putLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE,System.currentTimeMillis())
                .putLong(CommonConstant.PREF_DEVICE_FIRST_PAIRING_DATE, System.currentTimeMillis())
                .apply();
        boolean result = false;
        try {
            result = doDeviceClicked(true,editText.getText().toString());

        }catch (Exception e){}
        if(result){

        }
        return result;
    }
    private void startScan(){
        doScan();
    }

    @Override
    public void doWhenConnectFail() {
        if(!isFinishing()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connect.setEnabled(true);
                }
            });
        }

    }

    @Override
    public void doWhenDeviceFound(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothService.EXTRA_DEVICE);


        if(device!=null){
            int rssi = intent.getIntExtra(BluetoothService.EXTRA_RSSI,0);
            Log.e("check","device:"+device.getName()+"/"+device.getAddress());
            AdapterItem item = new AdapterItem(rssi,device.getName(), device.getAddress());
//            mAdapter.addScanResult(item);
//            if(TextUtils.isEmpty(editText.getText())){
//                editText.setText(device.getAddress());
//            }

        }
    }

    @Override
    public void doWhenDeviceConnected() {

        editText.setText(mBluetoothService.getConnectedAddress());
        connect.setEnabled(true);
        checkWarmup();
    }

    public void checkWarmup(){
        long warmUpStartDate = PreferenceManager.getDefaultSharedPreferences(this)
                .getLong(CommonConstant.PREF_WARM_UP_START_DATE,0);
        long currentTime = System.currentTimeMillis();
        if(mBluetoothService!=null && mBluetoothService.isDeviceConnected()){

            if(warmUpStartDate==0){
                buttonWarmUp.setEnabled(true);

            } else {
                if(currentTime - warmUpStartDate < CommonConstant.WARM_UP_DELAY){
                    goWarmup();
                } else {
                    goHome();
                }
            }
        } else {
            buttonWarmUp.setEnabled(false);
        }


    }

    @Override
    public void doWhenDeviceDisconnected() {
        checkWarmup();
    }

    static class AdapterItem {

        final String uuid;
        final String name;
        final int rssi;

        AdapterItem(int rssi, String name, String uuid) {
            this.name = name;
            this.uuid = uuid;
            this.rssi = rssi;
        }
    }



}