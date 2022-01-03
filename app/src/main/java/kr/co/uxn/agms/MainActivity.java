package kr.co.uxn.agms;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import kr.co.uxn.agms.data.room.AdminData;
import kr.co.uxn.agms.data.room.SensorLog;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.service.BluetoothService;
import kr.co.uxn.agms.service.BluetoothUtil;
import kr.co.uxn.agms.ui.BaseFragment;
import kr.co.uxn.agms.ui.connect.ConnectActivity;
import kr.co.uxn.agms.ui.popup.ProgressDialogFragment;
import kr.co.uxn.agms.util.StepHelper;



public class MainActivity extends AppCompatActivity  {


    private static final String TAG = "MainActivity";


    private BluetoothService mBluetoothService = null;
    private boolean mBluetoothBound = false;
    int permissionEvent =0;
    private Handler mHandler = null;

    private LiveData<SensorLog> mLastLog;
    private SensorRepository mRepository;


    NavController navController;
    BottomNavigationView navView;

    private StepHelper.ScreenStep mStep;

    public BluetoothService getBluetoothService(){
        return mBluetoothService;
    }

    View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(CommonConstant.MODE_IS_MEDICAL){
            setContentView(R.layout.activity_main_medical);
            progressView = findViewById(R.id.patient_number);
        } else {
            setContentView(R.layout.activity_main);
        }


        mHandler = new Handler(getMainLooper());
        try {
            String step = getIntent().getStringExtra(CommonConstant.EXTRA_STEP_STRING);
            if(step!=null){
                mStep = StepHelper.ScreenStep.valueOf(step);
            }
        }catch (Exception  e){
            mStep=null;
        }

        mRepository = new SensorRepository(getApplication());
        LiveData<AdminData> adminDataLiveData = mRepository.getLastLiveAdminData();
        adminDataLiveData.observe(this, adminData -> {
            if(adminData!=null){
                lastAdminDate = adminData.getEventTime();
            }
        });

        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupWithNavController(navView, navController);

    }

    public String getAdminId(){
        return PreferenceManager.getDefaultSharedPreferences(this).getString(CommonConstant.PREF_CURRENT_ADMIN_ID,"");
    }

    public String getDeviceMac(){
        String result = "";
        if(mBluetoothService!=null){
            result  = mBluetoothService.getConnectedAddress();
        }
        if(result==null){
            result = "";
        }
        return result;
    }
    public boolean isDeviceConnected(){
        boolean reslut = false;
        if(mBluetoothService!=null){
            reslut = mBluetoothService.isDeviceConnected();
        }

        return reslut;
    }

    public String getDeviceBattery(){
        String result = "";
        if(mBluetoothService!=null){
            float battery   = mBluetoothService.getBattery();
            if(battery>0){
                result = String.format("%.2f",battery);
            }
        }
        return result;
    }

    public String getSensorDate(){
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        long startDAte = PreferenceManager.getDefaultSharedPreferences(this).getLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE,0);
        if(startDAte>0){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(startDAte);
            sb.append(format.format(calendar.getTime()));
            sb.append(" ~ ");
            calendar.add(Calendar.DAY_OF_MONTH,10);
            sb.append(format.format(calendar.getTime()));
        }


        return sb.toString();
    }


    public void goTabHome(){
        navView.setSelectedItemId(R.id.navigation_home);
    }
    public void goTabEvent(){
        navView.setSelectedItemId(R.id.navigation_event);
    }
    public void goTabSetting(){
        navView.setSelectedItemId(R.id.navigation_setting);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG,"onStart");



        boolean needBluetoothStart = false;
        String blue = BluetoothUtil.requestingLocationUpdates(this);
        if(!TextUtils.isEmpty(blue)){
            needBluetoothStart = true;
        }

        if(needBluetoothStart && !mBluetoothBound){
            doDeviceClicked(false);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CommonConstant.REQUEST_CODE_PERMISSION_BLUETOOTH) {
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
                }
            } else {
                // Permission request was denied.
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
                        doDeviceClicked(true);

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

    public void requestPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this,R.string.permission_error_message, Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CommonConstant.REQUEST_CODE_PERMISSION_BLUETOOTH);
        } else {
            Toast.makeText(this,R.string.permission_error_message, Toast.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CommonConstant.REQUEST_CODE_PERMISSION_BLUETOOTH);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mStep!=null){
            if(mStep== StepHelper.ScreenStep.CHANGE_ADMIN_PASSWORD){
                mStep = null;
                goTabSetting();
            }
            if(mStep== StepHelper.ScreenStep.CHANGE_SENSOR){
                mStep = null;
                goTabSetting();
            }
        } else {
            checkCurrentState();
        }

    }
    long lastAdminDate = 0;
    public void checkCurrentState(){

        boolean isGoConnectActivity = false;
        boolean isGoWarmUpActivity = false;
        boolean isGoCalibration = false;
        boolean isChangeAdminPassword = false;
        boolean isChangeSensor = false;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String deviceAddress = sp.getString(CommonConstant.PREF_LAST_CONNECT_DEVICE,null);

        long sensorDate = sp.getLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE, 0);

        long warmUpStartDate = sp.getLong(CommonConstant.PREF_WARM_UP_START_DATE,0);
        long calibrationStartDate = sp.getLong(CommonConstant.PREF_CALIBRATION_START_TIME,0);
        int calibrationValue1 = sp.getInt(CommonConstant.PREF_CALIBRATION_TMP1,0);
        int calibrationValue2 = sp.getInt(CommonConstant.PREF_CALIBRATION_TMP2,0);

        try{
            if(CommonConstant.MODE_IS_MEDICAL){

                LiveData<AdminData> data = mRepository.getLastLiveAdminData();
                data.observe(this, adminData -> {
                    if(adminData!=null){
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(lastAdminDate);
                        calendar.add(Calendar.MONTH,3);
                        long targetDate = calendar.getTimeInMillis();
                        if(lastAdminDate > 0 &&  targetDate <= System.currentTimeMillis()){
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.alert_title)
                                    .setMessage(R.string.dialog_message_go_setting_to_change_admin_password)
                                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                                        dialogInterface.dismiss();
                                        goTabSetting();
                                    })
                                    .setNegativeButton(android.R.string.cancel, null).show();
                        }
                    }
                });
            } else {
                isGoCalibration = true;
                if(DateUtils.isToday(calibrationStartDate) &&calibrationValue1!=0 && calibrationValue2!=0){
                    isGoCalibration = false;
                }
            }


        }catch (Exception e){}




        if(TextUtils.isEmpty(deviceAddress)){
            isGoConnectActivity = true;
        }
        if(warmUpStartDate == 0){
            isGoWarmUpActivity = true;
        } else {
            long currentTime = System.currentTimeMillis();
            if(currentTime - warmUpStartDate < CommonConstant.WARM_UP_DELAY){
                isGoWarmUpActivity = true;
            }
        }
        if(sensorDate>0){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(sensorDate);
            calendar.add(Calendar.DAY_OF_MONTH,10);
            long targetDate = calendar.getTimeInMillis();
            if(targetDate <= System.currentTimeMillis()){
                isChangeSensor = true;
            }
        }

        StepHelper.ScreenStep step = StepHelper.ScreenStep.HOME;
        if(isChangeAdminPassword){
            step =  StepHelper.ScreenStep.CHANGE_ADMIN_PASSWORD;
        }
        if(isChangeSensor){
            step =  StepHelper.ScreenStep.CHANGE_SENSOR;
        }

        if(isGoCalibration){
            step =  StepHelper.ScreenStep.CALIBRATION;
        }

        if(isGoWarmUpActivity){
            step =  StepHelper.ScreenStep.WARM_UP;
        }
        if(isGoConnectActivity || !(mBluetoothService != null && mBluetoothService.isDeviceConnected())){
            step =  StepHelper.ScreenStep.CONNECT;
        }

        switch (step){

            case CHANGE_SENSOR:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.alert_title)
                        .setMessage(R.string.dialog_message_go_setting_to_change_sensor)
                        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            goTabSetting();
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
                break;
            default:
                break;
        }
    }

    public void doDeviceClicked(boolean isShow){
        Log.e(TAG,"1");
        if(!mBluetoothBound || mBluetoothService == null){
            permissionEvent = 1;

            if(!checkPermissions()){
                Log.e(TAG,"2");
                requestPermission();

            } else {
                Log.e(TAG,"3");
                bindService(new Intent(this, BluetoothService.class), mBluetoothServiceConnection,
                        Context.BIND_AUTO_CREATE);
            }
        } else {
            Log.e(TAG,"4");
            if(!mBluetoothService.isBluetoothEnabled()){
                Log.e(TAG,"5");
                Intent eintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(eintent, CommonConstant.REQUEST_CODE_BLE_ENABLE);
                return;
            }

            Log.e(TAG,"6");
            boolean isDeviceConnected = mBluetoothService.isDeviceConnected();
            if(isDeviceConnected){
                Log.e(TAG,"7");
                if(isShow){
                    Toast.makeText(this,R.string.toast_device_already_connected,Toast.LENGTH_SHORT).show();
                }

            } else {
                Log.e(TAG,"8");
                int status = mBluetoothService.getPairingDeviceStatus();
                if(status == BluetoothDevice.BOND_BONDING){
                    Log.e(TAG,"9");
                    if(isShow){
                        Toast.makeText(this,R.string.toast_device_scan_now,Toast.LENGTH_SHORT).show();
                    }

                } else {

                    Log.e("main","BluetoothDevice.BOND_BONDING not");
                    String lastDevice = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getString(CommonConstant.PREF_LAST_CONNECT_DEVICE,null);
                    if(!TextUtils.isEmpty(lastDevice)){
                        if(isShow){
                            Toast.makeText(this,R.string.toast_device_start_connect,Toast.LENGTH_SHORT).show();
                        }
                        mBluetoothService.scanStartForConnect(lastDevice);
                    } else {

                        mBluetoothService.scanStartForConnect(null);
                    }
                    mLastLog = mRepository.getLastLog();
                    mLastLog.observe(MainActivity.this,mLastLogObserver);
                    Log.e(TAG,"10");
                    if(isShow){
                        mHandler.removeCallbacks(showDeviceConnectError);
                        mHandler.postDelayed(showDeviceConnectError, 20000);
                    }


                }


            }
        }

    }


    public void removeDeviceConnectError(){
        mHandler.removeCallbacks(showDeviceConnectError);
    }

    private Observer<SensorLog> mLastLogObserver = sensorLog -> {
        if(sensorLog!=null){
            if(sensorLog.getSensorState() == BluetoothService.STATE_CONNECTED){
                removeDeviceConnectError();
                Toast.makeText(MainActivity.this,R.string.toast_device_connected,Toast.LENGTH_SHORT).show();
            }
        }
    };



    private Runnable showDeviceConnectError = () -> {
        try{
            if(isFinishing()){
                return;
            }
            showSimpleDialog(R.string.alert_message_device_connect_error);
        }catch (Exception e){}
    };

    public void showSimpleDialog(int resStringId){
        new AlertDialog.Builder(this)
                .setTitle(R.string.alert_title)
                .setMessage(getString(resStringId))
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private final ServiceConnection mBluetoothServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG,"mBluetoothService connected");
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mBluetoothService = binder.getService();
            mBluetoothBound = true;
            if (!mBluetoothService.isInitializeed()) {

                new AlertDialog.Builder(MainActivity.this).setMessage(R.string.bluetooth_init_error)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss()).show();

            } else {
                Log.e("check","send:"+mBluetoothService.isDeviceConnected());

            }


            String tmp = BluetoothUtil.requestingLocationUpdates(MainActivity.this);
            if(!TextUtils.isEmpty(tmp)){
                Intent intent = new Intent(CommonConstant.ACTION_SERVICE_CONNECTED);
                sendBroadcast(intent);
            }
            if(permissionEvent==1){
                doDeviceClicked(false);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG,"mBluetoothService onServiceDisconnected");
            mBluetoothService = null;
            mBluetoothBound = false;

        }
    };

    public void doOff(){
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(CommonConstant.PREF_LAST_CONNECT_DEVICE, null)
                .putLong(CommonConstant.PREF_WARM_UP_START_DATE,0)
                .putLong(CommonConstant.PREF_DEVICE_FIRST_PAIRING_DATE,0)
                .putLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE,0)
                .commit();
        if(mBluetoothService!=null){
            mBluetoothService.disconnect();
            mBluetoothService.removeBluetoothUpdates();
        }
    }
    public void disconnectAndReset(){
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(CommonConstant.PREF_LAST_CONNECT_DEVICE, null)
                .putLong(CommonConstant.PREF_WARM_UP_START_DATE,0)
                .putLong(CommonConstant.PREF_DEVICE_FIRST_PAIRING_DATE,0)
                .putLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE,0)
                .commit();
        if(mBluetoothService!=null){
            mBluetoothService.disconnect();
            mBluetoothService.removeBluetoothUpdates();
        }
        finishAffinity();
        Intent intent = new Intent(getApplicationContext(), ConnectActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {

        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();

        boolean handled = false;
        for(Fragment f : fragmentList) {
            if(f instanceof BaseFragment) {
                handled = ((BaseFragment)f).onBackPressed();

                if(handled) {
                    break;
                }
            }
        }

        if(!handled) {
            super.onBackPressed();
        }
    }

    public void showActivityProgress(){
        Bundle args = new Bundle();
        args.putBoolean(ProgressDialogFragment.CANCELABLE, false);
        DialogFragment dialog = ProgressDialogFragment.newInstance();
        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(),CommonConstant.PROGRESS_TAG);
    }
    public void dismissActivityProgress(){
        try{
            ProgressDialogFragment fragment = (ProgressDialogFragment) getSupportFragmentManager().findFragmentByTag(CommonConstant.PROGRESS_TAG);
            fragment.dismiss();

        }catch (Exception e){}
    }

}