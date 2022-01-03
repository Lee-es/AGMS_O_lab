package kr.co.uxn.agms.ui;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.MainActivity;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.SensorLog;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.service.BluetoothService;
import kr.co.uxn.agms.service.BluetoothUtil;
import kr.co.uxn.agms.ui.connect.WarmupActivity;
import kr.co.uxn.agms.util.StepHelper;

public abstract class BleActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    protected BluetoothService mBluetoothService = null;
    private boolean mBluetoothBound = false;
    int permissionEvent =0;
    private Handler mHandler = null;
    private LiveData<SensorLog> mLastLog;
    private SensorRepository mRepository;

    private static final int PERMISSION_BLUETOOTH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mHandler = new Handler(getMainLooper());
        mRepository = new SensorRepository(getApplication());

    }
    public abstract void doWhenDeviceConnected();
    public abstract void doWhenDeviceDisconnected();
    public abstract void doWhenConnectFail();
    public abstract void doWhenDeviceFound(Intent intent);


    public void goWarmup(){
        finishAffinity();
        Intent intent = new Intent(this, WarmupActivity.class);
        startActivity(intent);

    }
    public void goHome(){
        finishAffinity();
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra(CommonConstant.EXTRA_STEP, StepHelper.ScreenStep.CALIBRATION.ordinal());
        intent.putExtra(CommonConstant.EXTRA_STEP_STRING, StepHelper.ScreenStep.CALIBRATION.toString());
        startActivity(intent);

    }


    public void doScan(){
        if(!mBluetoothBound || mBluetoothService == null){
            permissionEvent = 2;
            if(!checkPermissions()){
                requestPermission();
            } else {
                bindService(new Intent(this, BluetoothService.class), mBluetoothServiceConnection,
                        Context.BIND_AUTO_CREATE);
            }
        } else {
            if(!mBluetoothService.isBluetoothEnabled()){
                Intent eintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(eintent, CommonConstant.REQUEST_CODE_BLE_ENABLE);
                return;
            }
            mBluetoothService.scanLeDevice(true, true);
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try{

                String action = intent.getAction();
                if(action==null){
                    return;
                }

                if(action.equals(BluetoothService.ACTION_GATT_CONNECTED)){
                    removeDeviceConnectError();
                    doWhenDeviceConnected();
                    mHandler.removeCallbacks(doWhenConnectFailRunnable);
                } else if(action.equals(BluetoothService.ACTION_GATT_TIMEOUT)) {

                } else if(action.equals(BluetoothService.ACTION_GATT_DISCONNECTED)) {
                    doWhenDeviceDisconnected();
                } else if(action.equals(BluetoothService.ACTION_BLE_SCAN)) {
                    doWhenDeviceFound(intent);
                }

            }catch (Exception e){}
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        boolean needBluetoothStart = false;
        String blue = BluetoothUtil.requestingLocationUpdates(this);
        if(!TextUtils.isEmpty(blue)){
            needBluetoothStart = true;
        }

        if(needBluetoothStart && !mBluetoothBound){
            doDeviceClicked(false, null);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothService.ACTION_BLE_SCAN);
        filter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
        filter.addAction(BluetoothService.ACTION_GATT_TIMEOUT);
        filter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);

        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_BLUETOOTH) {
            boolean bluetoothGranted = false;
            if(grantResults.length == permissions.length){
                for(int i=0;i<permissions.length;i++){
                    int grantResult = grantResults[i];
                    String permission = permissions[i];
                    if(permission.equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) ||
                            permission.equalsIgnoreCase(Manifest.permission.BLUETOOTH_ADMIN)){
                        if(grantResult==PackageManager.PERMISSION_GRANTED){
                            bluetoothGranted = true;
                        }
                    }
                }
            }


            if (bluetoothGranted) {
                if(!mBluetoothBound){
                    bindService(new Intent(this, BluetoothService.class), mBluetoothServiceConnection,
                            Context.BIND_AUTO_CREATE);
                } else {
                    if(permissionEvent==1){
                        doDeviceClicked(false, null);
                    } else if(permissionEvent==2){
                        doScan();
                    } else if(permissionEvent==3){
                        serviceBinding();
                    }
                }
            } else {
                Toast.makeText(this, R.string.permission_error_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        try{
            if (requestCode == CommonConstant.REQUEST_CODE_BLE_ENABLE) {
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        if (permissionEvent == 1) {
                            doDeviceClicked(true, null);
                        } else if (permissionEvent == 2) {
                            doScan();
                        } else if (permissionEvent == 3) {
                            serviceBinding();
                        }


                        break;
                    case Activity.RESULT_CANCELED:
                        showSimpleDialog(R.string.bluetooth_on_error_message);
                        break;
                    default:
                        break;
                }
            }
        }catch (Exception e){}



    }

    private boolean checkPermissions() {

        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

    }

    private void requestPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this,R.string.permission_error_message, Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(BleActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_BLUETOOTH);
        } else {
            Toast.makeText(this,R.string.permission_error_message, Toast.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_BLUETOOTH);
        }
    }
    private String tryToConnectAddress = null;
    public boolean doDeviceClicked(boolean isShow, String address){
        if(address!=null){
            tryToConnectAddress = address;
        }

        if(!mBluetoothBound || mBluetoothService == null){
            permissionEvent = 1;

            if(!checkPermissions()){
                requestPermission();

            } else {
                bindService(new Intent(this, BluetoothService.class), mBluetoothServiceConnection,
                        Context.BIND_AUTO_CREATE);
            }
            return false;
        } else {
            if(!mBluetoothService.isBluetoothEnabled()){
                Intent eintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(eintent, CommonConstant.REQUEST_CODE_BLE_ENABLE);
                return false;
            }
            boolean isDeviceConnected = mBluetoothService.isDeviceConnected();
            if(isDeviceConnected){
                if(isShow){
                    Toast.makeText(this,R.string.toast_device_already_connected,Toast.LENGTH_SHORT).show();
                }
                return false;
            } else {
                int status = mBluetoothService.getPairingDeviceStatus();
                if(status == BluetoothDevice.BOND_BONDING){
                    if(isShow){
                        Toast.makeText(this,R.string.toast_device_scan_now,Toast.LENGTH_SHORT).show();
                    }
                    return false;
                } else {
                    String lastDevice = PreferenceManager.getDefaultSharedPreferences(BleActivity.this).getString(CommonConstant.PREF_LAST_CONNECT_DEVICE,null);
                    if(!TextUtils.isEmpty(tryToConnectAddress)){
                        if(isShow){
                            Toast.makeText(this,R.string.toast_device_start_connect,Toast.LENGTH_SHORT).show();
                        }
                        mBluetoothService.scanStartForConnect(tryToConnectAddress);
                    }
                    else if(!TextUtils.isEmpty(lastDevice)){
                        if(isShow){
                            Toast.makeText(this,R.string.toast_device_start_connect,Toast.LENGTH_SHORT).show();
                        }
                        mBluetoothService.scanStartForConnect(lastDevice);
                    } else {
                        mBluetoothService.scanLeDevice(true,true);
                    }
                    mLastLog = mRepository.getLastLog();
                    mLastLog.observe(BleActivity.this,mLastLogObserver);
                    if(isShow){
                        mHandler.removeCallbacks(showDeviceConnectError);
                        mHandler.postDelayed(showDeviceConnectError, 5000);
                    } else {
                        mHandler.postDelayed(doWhenConnectFailRunnable, 5000);

                    }
                    return true;
                }
            }
        }

    }


    public void removeDeviceConnectError(){
        mHandler.removeCallbacks(showDeviceConnectError);
    }



    private Runnable doWhenConnectFailRunnable = () -> {
        try{
            if(isFinishing()){
                return;
            }
            doWhenConnectFail();
        }catch (Exception e){}
    };

    private Runnable showDeviceConnectError = () -> {
        try{
            if(isFinishing()){
                return;
            }
            showSimpleDialog(R.string.alert_message_device_connect_error);
        }catch (Exception e){}
    };

    private Observer<SensorLog> mLastLogObserver = sensorLog -> {
        if(sensorLog!=null && sensorLog.getSensorState() == BluetoothService.STATE_CONNECTED){
            removeDeviceConnectError();
            mHandler.removeCallbacks(doWhenConnectFailRunnable);
            Toast.makeText(BleActivity.this,R.string.toast_device_connected,Toast.LENGTH_SHORT).show();
        }
    };
    private void showSimpleDialog(int resStringId){
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title)
                .setMessage(getString(resStringId))
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private final ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mBluetoothService = binder.getService();
            mBluetoothBound = true;
            if (!mBluetoothService.isInitializeed()) {

                new AlertDialog.Builder(BleActivity.this).setMessage(R.string.bluetooth_init_error)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss()).show();

            }


            String tmp = BluetoothUtil.requestingLocationUpdates(BleActivity.this);
            if(!TextUtils.isEmpty(tmp)){
                Intent intent = new Intent(CommonConstant.ACTION_SERVICE_CONNECTED);
                sendBroadcast(intent);
            }
            if(permissionEvent==1){
                doDeviceClicked(true,null);
            } else if(permissionEvent==2){
                doScan();
            } else if(permissionEvent==3){
                serviceBinding();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
            mBluetoothBound = false;

        }
    };

    public void serviceBinding(){
        if(!mBluetoothBound || mBluetoothService == null){
            permissionEvent = 3;

            if(!checkPermissions()){
                requestPermission();

            } else {
                bindService(new Intent(this, BluetoothService.class), mBluetoothServiceConnection,
                        Context.BIND_AUTO_CREATE);
            }
        } else {
            if(!mBluetoothService.isBluetoothEnabled()){
                Intent eintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(eintent, CommonConstant.REQUEST_CODE_BLE_ENABLE);
                return;
            }
            mLastLog = mRepository.getLastLog();
            mLastLog.observe(BleActivity.this,mLastLogObserver);
        }
    }

}