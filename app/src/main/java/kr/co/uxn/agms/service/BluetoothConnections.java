package kr.co.uxn.agms.service;


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
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.amitshekhar.DebugDB;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import kr.co.uxn.agms.CommonConstant;


public class BluetoothConnections extends BluetoothGattCallback implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = "BluetoothConnections";
    private static final long SCAN_PERIOD = 20000;




    static BluetoothConnections instance;

    public static BluetoothConnections getInstance(Context context){
        if(instance == null){
                    instance=new BluetoothConnections(context);
        }
        return instance;
    }
    public static void removeInstance(){
        if(instance!=null){
            instance.doWhenRemoveInstance();
        }
        instance = null;
    }

    Context mContext;
    public BluetoothConnections(Context context){
        mContext = context.getApplicationContext();
        mBluetoothGattCallbackMap = new HashMap<>();
        mHandler = new Handler(mContext.getMainLooper());
        initialize();
        mDeviceAddress = BluetoothUtil.requestingLocationUpdates(mContext);
        Log.e(TAG, "BluetoothConnections last connected device:" + mDeviceAddress);
    }
    public void doWhenRemoveInstance(){
        mContext = null;
    }
    public boolean setGattCallbackResultWithResult(String key, BluetoothConnectionCallback callback){
        boolean result = false;
        try{
            mBluetoothGattCallbackMap.put(key, callback);
            result = true;
        }catch (Exception e){

        }
        return result;

    }
    public void removeGattCallback(String key){
        try{
            mBluetoothGattCallbackMap.remove(key);
        }catch (Exception e){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    removeGattCallback(key);
                }
            },1000);
        }

    }

    private Handler mHandler;
    private String mDeviceAddress = null;
    private float mBattery = -1;
    private int mRssi = 0;
    private boolean mScanning;
    private double mCurrentValue = 0;

    private boolean isReconnectRequested = false;
    private float we_current =0;
    private float we_glucose = 0;


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = BluetoothService.STATE_DISCONNECTED;

    private HashMap<String, BluetoothConnectionCallback> mBluetoothGattCallbackMap;

    private BluetoothGattCharacteristic writeCharacteristic;

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

        Log.e(TAG,"read rssi : "+rssi);

        if(gatt!=null && mBluetoothGatt.equals(gatt)){
            mRssi = rssi;
            broadcastUpdate(BluetoothService.ACTION_DATA_AVAILABLE, BluetoothService.EXTRA_RSSI, rssi);
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        doOnConnectionStateChange( gatt, status, newState);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        doOnServicesDiscovered(gatt,status);

    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {

        if (status == BluetoothGatt.GATT_SUCCESS) {
            broadcastUpdate(BluetoothService.ACTION_DATA_AVAILABLE, characteristic);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        final byte[] data = characteristic.getValue();
        broadcastUpdate(BluetoothService.ACTION_DATA_AVAILABLE, characteristic);
        if (data != null && data.length > 0) {
            final String targetAddress = gatt.getDevice().getAddress();
            try{
                if(!mBluetoothGattCallbackMap.isEmpty()){
                    for(BluetoothConnectionCallback callback :mBluetoothGattCallbackMap.values()){
                        callback.onUartChanged(data,targetAddress);
                    }
                }
            }catch (Exception e){
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            if(!mBluetoothGattCallbackMap.isEmpty()){
                                for(BluetoothConnectionCallback callback :mBluetoothGattCallbackMap.values()){
                                    callback.onUartChanged(data,targetAddress);
                                }
                            }
                        }catch (Exception e){}
                    }
                },1000);
            }

        }
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(BluetoothService.EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            String tmpString = new String(data);
            Log.e(TAG,"data:"+stringBuilder.toString() + " , " + tmpString);
        }
        mContext.sendBroadcast(intent);
        DebugDB.getAddressLog();
    }
    private void broadcastUpdate(final String action, final String address) {
        final String newAddress = String.valueOf(address);
        final Intent intent = new Intent(action);
        intent.putExtra(BluetoothService.EXTRA_ADDRESS, newAddress);
        mContext.sendBroadcast(intent);
    }
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        mContext.sendBroadcast(intent);
    }
    private void broadcastUpdate(final String action, String extra, int value) {
        final Intent intent = new Intent(action);
        intent.putExtra(extra,value);
        mContext.sendBroadcast(intent);
    }

    private void doOnServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, "BluetoothGattCallback onServicesDiscovered");
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "BluetoothGattCallback GATT_SUCCESS");
            broadcastUpdate(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);
            List<BluetoothGattService> serviceList = gatt.getServices();
            if (serviceList != null) {
                Log.d(TAG, "service print");
                boolean isServiceFound = false;
                for (BluetoothGattService service : serviceList) {
                    Log.e(TAG, "service :" + service.toString());
                    Log.e(TAG, "service uuid :" + service.getUuid());

                    if (service.getUuid().equals(BluetoothService.UUID_CONNECTION_SERVICE)) {

                        isServiceFound = true;
                    }
                    List<BluetoothGattCharacteristic> characteristicsList = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristicsList) {
                        Log.e(TAG, "characteristic :" + characteristic.toString());
                        Log.e(TAG, "characteristic uuid :" + characteristic.getUuid());
                        if (isServiceFound) {
                            if (characteristic.getUuid().equals(BluetoothService.UUID_CONNECTION_CHARACTERISTIC_READ)) {

                                setCharacteristicNotification(characteristic, true);
                            }
                            if (characteristic.getUuid().equals(BluetoothService.UUID_CONNECTION_CHARACTERISTIC_WRITE)) {

                                writeCharacteristic = characteristic;
                            }

                        }
                    }
                    isServiceFound = false;

                }
            } else {

            }
        } else {

        }
    }

    public void setCharacteristicNotification (BluetoothGattCharacteristic characteristic,
        boolean enabled){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.d(TAG, "setCharacteristicNotification");
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        List<BluetoothGattDescriptor> list = characteristic.getDescriptors();
        if (list != null) {
            for (BluetoothGattDescriptor descriptor : list) {
                Log.d(TAG, "BluetoothGattDescriptor :" + descriptor.getUuid() + " / " + descriptor.getValue());
            }
            Log.d(TAG, "BluetoothGattDescriptor list not null:" + list.size());
        } else {
            Log.d(TAG, "BluetoothGattDescriptor null");
        }

        if (BluetoothService.UUID_CONNECTION_CHARACTERISTIC_READ.equals(characteristic.getUuid())) {

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    BluetoothService.UUID_BLUETOOTH_GATT_DESCRIPTOR);
            if (descriptor != null) {
                Log.d(TAG, "add BluetoothGattDescriptor set value ENABLE_NOTIFICATION_VALUE");
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            } else {
                Log.d(TAG, "BluetoothGattDescriptor not found");
            }

        }
    }

    private void doOnConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

        String intentAction;
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.e(TAG,"current connected device address:"+mBluetoothDeviceAddress);
            isReconnectRequested = true;
            intentAction = BluetoothService.ACTION_GATT_CONNECTED;
            mConnectionState = BluetoothService.STATE_CONNECTED;

            broadcastUpdate(intentAction, mBluetoothDeviceAddress);
            Log.d(TAG, "Connected to GATT server.");
            // Attempts to discover services after successful connection.
            Log.d(TAG, "Attempting to start service discovery:" +
                    mBluetoothGatt.discoverServices());
            BluetoothUtil.setRequestingLocationUpdates(mContext,mBluetoothDeviceAddress);
            final String targetAddress = gatt.getDevice().getAddress();
            final int targetState = status;
            final int targetNewState = newState;
            final int targetConnectionState = mConnectionState;
            try{
                if(!mBluetoothGattCallbackMap.isEmpty()){
                    for(BluetoothConnectionCallback callback :mBluetoothGattCallbackMap.values()){
                        callback.doOnConnectionStateChange(targetAddress, targetState, targetNewState,targetConnectionState,true);
                    }
                }
            }catch (Exception e){
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            if(!mBluetoothGattCallbackMap.isEmpty()){
                                for(BluetoothConnectionCallback callback :mBluetoothGattCallbackMap.values()){
                                    callback.doOnConnectionStateChange(targetAddress, targetState, targetNewState,targetConnectionState,true);
                                }
                            }
                        }catch (Exception e){

                        }
                    }
                },1000);
            }


        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

            int beforeSTate = mConnectionState;
            mRssi = 0;
            mBattery = -1;
            intentAction = BluetoothService.ACTION_GATT_DISCONNECTED;
            mConnectionState = BluetoothService.STATE_DISCONNECTED;
            broadcastUpdate(intentAction, mBluetoothDeviceAddress);


                try{
                    if(!mBluetoothGattCallbackMap.isEmpty()) {
                        for(BluetoothConnectionCallback callback :mBluetoothGattCallbackMap.values()){
                            callback.doOnConnectionStateChange(gatt.getDevice().getAddress(), status, newState,beforeSTate,isReconnectRequested);
                        }
                    }

                }catch (Exception e){
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!mBluetoothGattCallbackMap.isEmpty()) {
                                for(BluetoothConnectionCallback callback :mBluetoothGattCallbackMap.values()){
                                    callback.doOnConnectionStateChange(gatt.getDevice().getAddress(), status, newState,beforeSTate,isReconnectRequested);
                                }
                            }
                        }
                    },1000);
                }


            if(isReconnectRequested && !isDeviceConnected() && mConnectionState!=BluetoothService.STATE_CONNECTING){
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!isDeviceConnected() && mConnectionState!=BluetoothService.STATE_CONNECTING){
                            scanStartForConnect(mBluetoothDeviceAddress);
                        }

                    }
                },1000);
            } else {
                mBluetoothDeviceAddress = null;
            }

        } else {

            Log.e(TAG,"onConnectionStateChange else state:"+newState);
        }

    }
    public boolean checkDeviceConnected(){
        return (mBluetoothGatt!=null && mConnectionState==BluetoothService.STATE_CONNECTED);
    }
    public boolean checkDeviceConnecting(){
        return (mBluetoothGatt!=null && mConnectionState==BluetoothService.STATE_CONNECTING);
    }

    public boolean isDeviceConnected(){
        boolean result = false;
        try{
            if(getPairingDeviceStatus()== BluetoothDevice.BOND_BONDED){
                result = true;
            }
        }catch (Exception e){

        }
        return result;
    }
    public int getPairingDeviceStatus() throws IllegalStateException {
        Log.e(TAG,"getPairingDeviceStatus");
        if(mBluetoothGatt==null){
            Log.e(TAG,"mBluetoothGatt null");
            return BluetoothDevice.BOND_NONE;
        }

        if(mBluetoothDeviceAddress!=null) {
            Log.e(TAG,"getPairingDeviceStatus mBluetoothDeviceAddress:"+mBluetoothDeviceAddress);          //여기
            if(mConnectionState==BluetoothService.STATE_CONNECTING){
                Log.e(TAG,"getPairingDeviceStatus mBluetoothDeviceAddress STATE_CONNECTING");
                return BluetoothDevice.BOND_BONDING;
            }
            if(mConnectionState==BluetoothService.STATE_CONNECTED){

                Log.e(TAG,"getPairingDeviceStatus mBluetoothDeviceAddress STATE_CONNECTED");
                return BluetoothDevice.BOND_BONDED;
            }
            Log.e(TAG,"getPairingDeviceStatus mBluetoothDeviceAddress BOND_NONE");                   //여기

            return BluetoothDevice.BOND_NONE;
        } else {
            Log.e(TAG,"getPairingDeviceStatus mBluetoothDeviceAddress is null");

            return BluetoothDevice.BOND_NONE;

        }
    }

    public boolean writeToCharacteristic( byte[] data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || writeCharacteristic==null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        writeCharacteristic.setValue(data);
        boolean write = mBluetoothGatt.writeCharacteristic(writeCharacteristic);
        return write;
    }

    public void removeBluetoothUpdates() {
        try {
            close();
        //   mNotificationManager.cancelAll();
            isReconnectRequested = false;
            BluetoothUtil.setRequestingLocationUpdates(mContext, null);
     //       stopSelf();
        } catch (SecurityException unlikely) {
            BluetoothUtil.setRequestingLocationUpdates(mContext,  null);

        }

    }
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    public void disconnect() {
        Log.e(TAG,"disconnect call");

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    public String getConnectedAddress(){
        if(isDeviceConnected()){
            return mBluetoothDeviceAddress;
        }
        return null;
    }
    public boolean connect(final String addr) {
        Log.d(TAG,"start connect :"+addr);
        if (mBluetoothAdapter == null || addr == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        String targetAddress = addr.toUpperCase();
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && targetAddress.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = BluetoothService.STATE_CONNECTING;
                return true;
            } else {
                try{
                    disconnect();
                    close();
                }catch (Exception e){}
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(targetAddress);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        Log.d(TAG, "start to create a new connection.");
        mBluetoothGatt = device.connectGatt(mContext, false, this);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = targetAddress;
        mConnectionState = BluetoothService.STATE_CONNECTING;

        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putString(CommonConstant.PREF_LAST_CONNECT_DEVICE,mBluetoothDeviceAddress)
                .apply();
        return true;
    }

    public String bluetoothDeviceAddress(){
        return mBluetoothDeviceAddress;
    }

    public float getBattery(){
        return mBattery;
    }
    public int getRSSI(){
        return mRssi;
    }

    public void scanStartForConnect(String address){
        mDeviceAddress = address;
        scanLeDevice(true, false);
    }
    public void scanStartForConnect(){
        if(mDeviceAddress!=null && isDeviceConnected() && !mScanning){
            scanLeDevice(true, false);
        }
    }

    public void scanLeDevice(final boolean enable, boolean isReset) {
        try{
            if (enable) {
                if(isReset){
                    mDeviceAddress = null;
                }
                Log.e(TAG,"mDeviceAddress:"+mDeviceAddress);
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(postDelayRunnable, SCAN_PERIOD);

                mScanning = true;
//                mBluetoothAdapter.startLeScan(new UUID[]{UUID_CONNECTION_SERVICE}, mLeScanCallback);
                mBluetoothAdapter.startLeScan(
                        new UUID[]{BluetoothService.UUID_CONNECTION_SERVICE},
                        BluetoothConnections.this);
            } else {
                mScanning = false;
                mBluetoothAdapter.stopLeScan(BluetoothConnections.this);
                mHandler.removeCallbacks(postDelayRunnable);

            }
        }catch (Exception e){

            Log.e(TAG,e.getLocalizedMessage());
        }

    }

    public boolean isBluetoothEnabled(){
        if(mBluetoothAdapter!=null){
            return mBluetoothAdapter.isEnabled();
        }
        return false;
    }
    @Override
    public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {

        if(device.getAddress()==null){
            return;
        }

        Log.e(TAG,"device scan result: device:"+device.getName() + " / rssi:"+rssi + " / " + device.getAddress());
        Intent intent = new Intent(BluetoothService.ACTION_BLE_SCAN);
        intent.putExtra(BluetoothService.EXTRA_DEVICE, device);
        intent.putExtra(BluetoothService.EXTRA_RSSI, rssi);
        mContext.sendBroadcast(intent);

        if(!TextUtils.isEmpty(mDeviceAddress)){

            if( device.getAddress().equals(mDeviceAddress)) {
                Log.e(TAG,"found address equlas mDeviceAddress:"+mDeviceAddress);
                if (mScanning) {
                    scanLeDevice(false, false);
                }
                connect(mDeviceAddress);
            } else if(device.getAddress().equalsIgnoreCase(mDeviceAddress)) {
                Log.e(TAG,"found address equalsIgnoreCase mDeviceAddress:"+mDeviceAddress);
                if (mScanning) {
                    scanLeDevice(false, false);
                }
                connect(mDeviceAddress);
            }
        }
    }
    public int getState(){
        return mConnectionState;
    }
    public void setReconnectRequested(boolean value){
        isReconnectRequested =value;
    }

    private Runnable postDelayRunnable = new Runnable() {
        @Override
        public void run() {
            mScanning = false;
            if(mBluetoothAdapter!=null){
                try {
                    mBluetoothAdapter.stopLeScan(BluetoothConnections.this);


                }catch (Exception e){

                    Log.e(TAG,e.getLocalizedMessage());
                }
                if(isReconnectRequested && !isDeviceConnected() && mConnectionState != BluetoothService.STATE_CONNECTING){
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!isDeviceConnected() && mConnectionState != BluetoothService.STATE_CONNECTING){
                                scanStartForConnect(mDeviceAddress);
                            }

                        }
                    },1000);
                }
            } else {
                Log.e(TAG,"mBluetoothAdapter null");
            }


        }
    };
    public void setBattery(float battery){
        this.mBattery = battery;

    }
    public float getCurrent(){
        if(isDeviceConnected()){
            return we_current;
        }
        return Float.MIN_VALUE;
    }
    public void setCurrent(float current){
        we_current = current;
    }
    public boolean isInitializeed(){
        if(mBluetoothManager!=null && mBluetoothAdapter!=null){
            return true;
        }
        return initialize();
    }

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {

                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {

            return false;
        }

        return true;
    }

//    public void disconnectAndReset(){
//        mHandler.removeMessages(HANDLER_CHECK_CALLBACK);
//        isReconnectRequested = false;
//        BluetoothUtil.setRequestingLocationUpdates(this, null);
//        disconnect();
//        close();
//    }
}
