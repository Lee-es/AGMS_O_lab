 package kr.co.uxn.agms.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.MainActivity;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.SplashActivity;
import kr.co.uxn.agms.data.room.AdminData;
import kr.co.uxn.agms.data.room.EventData;
import kr.co.uxn.agms.data.room.GlucoseData;
import kr.co.uxn.agms.data.room.PatientData;
import kr.co.uxn.agms.data.room.SensorLog;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.ui.connect.ConnectActivity;
import kr.co.uxn.agms.ui.connect.WarmupActivity;
import kr.co.uxn.agms.ui.popup.DisconnectAlarmActivity;
import kr.co.uxn.agms.util.StepHelper;


public class BluetoothService extends  LifecycleService implements BluetoothConnectionCallback {
    private final static String TAG = BluetoothService.class.getSimpleName();


    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public static final String BASE_PACKAGE = "kr.co.uxn.agms";
    public static final String EXTRA_DEVICE = "extra_device";

    public final static String ACTION_BLE_SCAN =
            BASE_PACKAGE+".bluetooth.le.scan";
    public final static String ACTION_GATT_CONNECTED =
            BASE_PACKAGE+".bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            BASE_PACKAGE+".bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_TIMEOUT =
            BASE_PACKAGE+".bluetooth.le.ACTION_GATT_TIMEOUT";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            BASE_PACKAGE+".bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            BASE_PACKAGE+".bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            BASE_PACKAGE+".bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_ADDRESS =
            BASE_PACKAGE+".bluetooth.le.EXTRA_ADDRESS";
    public final static String EXTRA_NAME =
            BASE_PACKAGE+".bluetooth.le.EXTRA_NAME";

    public final static String ACTION_DATA_RECEIVED =
            BASE_PACKAGE+".bluetooth.le.ACTION_DATA_RECEIVED";


    public final static String EXTRA_BATTERY_STATE =
            BASE_PACKAGE+".bluetooth.le.EXTRA_BATTERY_STATE";

    public final static String EXTRA_RSSI =
            BASE_PACKAGE+".bluetooth.le.EXTRA_RSSI";

    public final static String EXTRA_STARTED_FROM_NOTIFICATION = BASE_PACKAGE+".bluetooth.le.started_from_notification";


    public final static UUID UUID_CONNECTION_SERVICE = UUID.fromString("331a36f5-2459-45ea-9d95-6142f0c4b307");
    public final static UUID UUID_CONNECTION_CHARACTERISTIC_WRITE = UUID.fromString("a9da6040-0823-4995-94ec-9ce41ca28833");
    public final static UUID UUID_CONNECTION_CHARACTERISTIC_READ = UUID.fromString("a73e9a10-628f-4494-a099-12efaf72258f");
    public final static UUID UUID_BLUETOOTH_GATT_DESCRIPTOR = new UUID(0x0000290200001000L, 0x800000805f9B34FBL);
//    private final static UUID UUID_BLUETOOTH_GATT_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");




    public static final String CHANNEL_ID = "channel_01";

    public static final String MESSAGE_CHANNEL_ID = "message_channel";

    private static final int NOTIFICATION_ID = 210204;


    private static final long INTERVAL_WIRTE_TO_DEVICE = 50;

    private static final long INTERVAL_DEVICE_DISCONNECT = 120000L;

    private static final int HANDLER_WRITE_TO_DEVICE = 1;

    private static final int HANDLER_CHECK_BATTERY = 2;

    private static final int HANDLER_CHECK_CALLBACK = 3;

    private final float WOKING_CURRENT=0.895f;



    private Handler mServiceHandler;

    private NotificationManager mNotificationManager;
    private boolean mChangingConfiguration = false;

    private boolean isBinded = false;

    public static final String ACTION_STOP_SERVICE = "bluetooth.service.stop";
    public static final String ACTION_CHECK_LIVE_SERVICE = "bluetooth.service.check_live";

    public String mServiceUUID = null;

    public float getCurrent(){
       return BluetoothConnections.getInstance(this).getCurrent();
    }
    public void setPatient(String name, long number){
        currentUser = name;
        patientNumber = number;
    }

    private Handler mHandler = new Handler(Looper.myLooper()){

    };



    private String currentUser;

    private long patientNumber;
    private boolean batteryWarned = false;
    private void setBattery(float battery){
        BluetoothConnections.getInstance(this).setBattery(battery);
        if(battery<CommonConstant.MIN_BATTERY_LEVEL){
            if(!batteryWarned){
                batteryWarned = true;
                Notification notification = createBatteryNotification();
                if(mNotificationManager!=null && notification!=null){
                    mNotificationManager.notify(CommonConstant.REQUEST_ACTIVITY, notification);
                }
            }
        } else {
            batteryWarned = false;
        }
    }
    public boolean isInitializeed(){
        return BluetoothConnections.getInstance(this).isInitializeed();
    }
    @Override
    public void doOnConnectionStateChange(String address, int status, int newState, int mConnectionState, boolean showReconnectNoti) {

            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mNotificationManager.notify(NOTIFICATION_ID, getNotification());

                if(mRepository!=null){
                    SensorLog log = new SensorLog(0,address, STATE_CONNECTED, System.currentTimeMillis());
                    mRepository.insertSensorLog(log);
                }
                mNotificationManager.cancel(CommonConstant.NOTIFICATION_ID_DISCONNECT);
                mNotificationManager.cancel(CommonConstant.REQUEST_CONNECT);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                if(mConnectionState==STATE_CONNECTED){
                    Notification notification =  createDisconnectAlarmNotification();
                    if(mNotificationManager!=null){
                        mNotificationManager.notify(CommonConstant.NOTIFICATION_ID_DISCONNECT, notification);
                    }
                    Intent dialogIntent = new Intent(getApplicationContext(), DisconnectAlarmActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);
                }


                mHandler.removeMessages(HANDLER_CHECK_BATTERY);

                if(mRepository!=null){
                    SensorLog log = new SensorLog(0,address, STATE_DISCONNECTED, System.currentTimeMillis());
                    mRepository.insertSensorLog(log);
                    if(CommonConstant.MODE_IS_MEDICAL){
                        EventData eventData = new EventData(0,currentUser,patientNumber,System.currentTimeMillis(),"연결x",0);
                        mRepository.createEventData(eventData);
                    }
                }
                if(showReconnectNoti){
                    mNotificationManager.notify(NOTIFICATION_ID, getNotification());

                } else {
                    mNotificationManager.cancelAll();
                    BluetoothConnections.getInstance(this).disconnect();
                    BluetoothConnections.getInstance(this).close();

                    if(!TextUtils.isEmpty(mServiceUUID)){
                        BluetoothConnections.getInstance(this).removeGattCallback(mServiceUUID);
                    }
                    stopSelf();
                }

            }

    }


    @Override
    public void onUartChanged(byte[] uartData, String address) {
        if(uartData==null){
            return;
        }
        Log.e(TAG,"onUartChanged:"+address);
        if(currentUser==null){
            observeUserLiveData();
        }
        float current = 0;
        float glucose = 0;
        /**
         * remove 0xFD: // Set Timer
         */
        switch (uartData[0]){


            case (byte)0xF9:

                byte[] we1bytes = {uartData[2], uartData[3], uartData[4], uartData[5]};
                byte[] we2bytes = {uartData[6], uartData[7], uartData[8], uartData[9]};

                byte[] bat_level = { uartData[13], uartData[14], uartData[15], uartData[16]};
                byte[] ref_level = { uartData[17], uartData[18], uartData[19], uartData[20]};

                float bat = ByteBuffer.wrap(bat_level).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                float ref = ByteBuffer.wrap(ref_level).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                float final_bat_level = bat / ref * 100;

                setBattery(bat);
                current = ByteBuffer.wrap(we1bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();
                glucose = current;
                BluetoothConnections.getInstance(this).setCurrent(current);

                mRepository.insertGluecoseData(new GlucoseData(0,
                        address,
                        System.currentTimeMillis(),
                        current, glucose,currentUser,patientNumber, getBattery()
                        ));

                mNotificationManager.notify(NOTIFICATION_ID, getNotification());

                break;
            case (byte)0x47:
                //kjlee Test
                byte[] s = {uartData[2], uartData[3], uartData[4], uartData[5],uartData[6], uartData[7]};
                byte[] s2 = {uartData[12], uartData[13], uartData[14], uartData[15]};
                String BatLevel = new String(s2);
                float batt = Float.parseFloat(BatLevel);
                batt=batt*2.0f;

                setBattery(batt);

                String data = new String(s);
                float voltage = Float.parseFloat(data);
                current =  (float) ((voltage*2.0 - WOKING_CURRENT)*1000.0/10.0);
                glucose = current;

                mRepository.insertGluecoseData(new GlucoseData(0,
                        address,
                        System.currentTimeMillis(),
                        current, glucose,currentUser, patientNumber, getBattery()
                ));
                break;
            default:
                break;
        }
    }

    private void broadcastUpdate(final String action, final String address) {
        final String newAddress = String.valueOf(address);
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_ADDRESS, newAddress);
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private SensorRepository mRepository;

    LiveData<PatientData> userLiveData;
    private void observeUserLiveData(){
        if(CommonConstant.MODE_IS_MEDICAL){
            if(userLiveData!=null){
                return;
            }
            userLiveData = mRepository.getLastUser();
            userLiveData.observe(this, patientData -> {
                if(patientData!=null){
                    currentUser = patientData.getName();
                    patientNumber = patientData.getPatientNumber();
                }
            });
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate start");
        if(mServiceUUID == null){
            mServiceUUID = UUID.randomUUID().toString();
            boolean fallback = BluetoothConnections.getInstance(this).setGattCallbackResultWithResult(mServiceUUID, this);
            if(!fallback){
                BluetoothConnections.getInstance(this).setGattCallbackResultWithResult(mServiceUUID, this);
            }
        }

        mRepository = new SensorRepository(getApplication());
        observeUserLiveData();


        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();

        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification());
        Log.e(TAG, "onCreate over");

    }
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(mNotificationManager.getNotificationChannel(CHANNEL_ID)==null){
                CharSequence name = getString(R.string.app_name);
                // Create the channel for the notification
                NotificationChannel mChannel =
                        new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
                // Set the Notification Channel for the Notification Manager.
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }
    }
    private void createMessageNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(mNotificationManager.getNotificationChannel(MESSAGE_CHANNEL_ID)==null){
                CharSequence name = getString(R.string.app_name);
                // Create the channel for the notification
                NotificationChannel mChannel =
                        new NotificationChannel(MESSAGE_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
                // Set the Notification Channel for the Notification Manager.
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e(TAG, "bluetoothservice onStartCommand");
        if(mServiceUUID == null){
            mServiceUUID = UUID.randomUUID().toString();
            boolean fallback = BluetoothConnections.getInstance(this).setGattCallbackResultWithResult(mServiceUUID, this);
            if(!fallback){
                BluetoothConnections.getInstance(this).setGattCallbackResultWithResult(mServiceUUID, this);
            }
        }

        if (intent.getAction() != null && intent.getAction().equals(ACTION_STOP_SERVICE)) {
            BluetoothConnections.getInstance(this).disconnect();
            BluetoothConnections.getInstance(this).close();
            mNotificationManager.cancelAll();
            BluetoothConnections.getInstance(this).removeBluetoothUpdates();
            stopForeground(true);
            if(!TextUtils.isEmpty(mServiceUUID)){
                BluetoothConnections.getInstance(this).removeGattCallback(mServiceUUID);
            }
            stopSelf();

        } else {
            setAlarm();
            if (!isBinded) {
                Log.e(TAG, "start foreground");

                startForeground(NOTIFICATION_ID, getNotification());
            }
            boolean init =BluetoothConnections.getInstance(this).isInitializeed();
            if(!init){
                init = BluetoothConnections.getInstance(this).initialize();
            }
            if(init){
                String device = BluetoothUtil.requestingLocationUpdates(this);
                Log.e(TAG, "bluetoothservice last connected device:" + device);
                if (!TextUtils.isEmpty(device) && !isDeviceConnected() && BluetoothConnections.getInstance(this).getState() != STATE_CONNECTING) {
                    BluetoothConnections.getInstance(this).setReconnectRequested(true);
                    scanStartForConnect(device);

                }
            }

            checkCurrentState();
        }

        return START_STICKY;
    }
    public void checkCurrentState(){
        Log.e("check","admindata checkCurrentState");
        boolean isGoConnectActivity = false;
        boolean isGoWarmUpActivity = false;
        boolean isGoCalibration = false;
        boolean isChangeAdminPassword = false;
        boolean isChangeSensor = false;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String deviceAddress = sp.getString(CommonConstant.PREF_LAST_CONNECT_DEVICE,null);

        long sensorDate = sp.getLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE, 0);
        long deviceFirstPairingDate = sp.getLong(CommonConstant.PREF_DEVICE_FIRST_PAIRING_DATE,0);
        long warmUpStartDate = sp.getLong(CommonConstant.PREF_WARM_UP_START_DATE,0);
        long calibrationStartDate = sp.getLong(CommonConstant.PREF_CALIBRATION_START_TIME,0);
        int calibrationValue1 = sp.getInt(CommonConstant.PREF_CALIBRATION_TMP1,0);
        int calibrationValue2 = sp.getInt(CommonConstant.PREF_CALIBRATION_TMP2,0);

        try{
            if(CommonConstant.MODE_IS_MEDICAL){
                Log.e("check","admindata check");
                LiveData<AdminData> data = mRepository.getLastLiveAdminData();
                data.observe(this, new Observer<AdminData>() {
                    @Override
                    public void onChanged(AdminData adminData) {
                        if(adminData!=null){
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(adminData.getEventTime());
                            calendar.add(Calendar.MONTH,3);
                            long targetDate = calendar.getTimeInMillis();
                            if(targetDate <= System.currentTimeMillis()){
                                Notification notification = createChangeAdminPasswordNotification();
                                if(mNotificationManager!=null && notification!=null){
                                    mNotificationManager.notify(CommonConstant.REQUEST_ACTIVITY, notification);
                                }
                            }
                        }
                    }
                });
            }


        }catch (Exception e){}

        currentUser = sp.getString(CommonConstant.PREF_CURRENT_PATIENT_NAME, null);
        patientNumber = sp.getLong(CommonConstant.PREF_CURRENT_PATIENT_NUMBER, 0);

        if(!CommonConstant.MODE_IS_MEDICAL){
            isGoCalibration = true;
            if(DateUtils.isToday(calibrationStartDate)){
                if(calibrationValue1!=0 && calibrationValue2!=0){
                    isGoCalibration = false;
                }
            }
        }

        if(TextUtils.isEmpty(deviceAddress)){
            isGoConnectActivity = true;
            Log.e(TAG,"isGoConnectActivity deviceAddress is empty" );
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

        if(isGoConnectActivity || !isDeviceConnected()){
            step =  StepHelper.ScreenStep.CONNECT;
        }
        Notification notification = null;
        switch (step){
            case CONNECT:
                notification = createConnectNotification();
                if(mNotificationManager!=null){
                    mNotificationManager.cancel(CommonConstant.REQUEST_ACTIVITY);
                    mNotificationManager.cancel(CommonConstant.REQUEST_WARMUP);
                    if(notification!=null){
                        mNotificationManager.notify(CommonConstant.REQUEST_CONNECT, notification);
                    }
                }

                break;
            case WARM_UP:
                notification = createWarmupNotification();
                if(mNotificationManager!=null){
                    mNotificationManager.cancel(CommonConstant.REQUEST_ACTIVITY);
                    mNotificationManager.cancel(CommonConstant.REQUEST_CONNECT);
                    if(notification!=null){
                        mNotificationManager.notify(CommonConstant.REQUEST_WARMUP, notification);
                    }
                }

                break;
            case CHANGE_ADMIN_PASSWORD:
                notification = createChangeAdminPasswordNotification();

                if(mNotificationManager!=null && notification!=null){
                    mNotificationManager.notify(CommonConstant.REQUEST_ACTIVITY, notification);
                }
                break;
            case CHANGE_SENSOR:
                notification = createChangeSensorNotification();
                if(mNotificationManager!=null && notification!=null){
                    mNotificationManager.notify(CommonConstant.REQUEST_ACTIVITY, notification);
                }
                break;
            case CALIBRATION:
                notification = createCalibrationNotification();
                if(mNotificationManager!=null && notification!=null){
                    mNotificationManager.notify(CommonConstant.REQUEST_ACTIVITY, notification);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }
    public void disconnect(){
        BluetoothConnections.getInstance(this).disconnect();
    }
    public void removeBluetoothUpdates(){
        BluetoothConnections.getInstance(this).removeBluetoothUpdates();
    }
    @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        isBinded = true;
        if(mServiceUUID == null){
            mServiceUUID = UUID.randomUUID().toString();
            boolean fallback = BluetoothConnections.getInstance(this).setGattCallbackResultWithResult(mServiceUUID, this);
            if(!fallback){
                BluetoothConnections.getInstance(this).setGattCallbackResultWithResult(mServiceUUID, this);
            }
        }
        stopForeground(true);
        mChangingConfiguration = false;
        setAlarm();
        boolean init =BluetoothConnections.getInstance(this).isInitializeed();
        if(!init){
            init = BluetoothConnections.getInstance(this).initialize();
        }
        if(init){
            String device = BluetoothUtil.requestingLocationUpdates(this);
            Log.e(TAG, "bluetoothservice last connected device:" + device);
            if (!TextUtils.isEmpty(device) && !isDeviceConnected() && BluetoothConnections.getInstance(this).getState() != STATE_CONNECTING) {
                BluetoothConnections.getInstance(this).setReconnectRequested(true);
                scanStartForConnect(device);
            }

        }

        checkCurrentState();
        return mBinder;

    }
    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG,"service onRebind");
        isBinded = true;
        stopForeground(true);
        mChangingConfiguration = false;
        setAlarm();
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBinded = false;
        if (!mChangingConfiguration ) {
            String device = BluetoothUtil.requestingLocationUpdates(this);
            if(!TextUtils.isEmpty(device)){

                startForeground(NOTIFICATION_ID, getNotification());
                return true;
            } else {
                Log.e(TAG,"637 destroy");
                try{
                    BluetoothConnections.getInstance(this).disconnect();
                }catch (Exception e){}
                try{
                    BluetoothConnections.getInstance(this).close();
                }catch (Exception e){}

                try{
                    mNotificationManager.cancelAll();
                }catch (Exception e){}
                try{
                    BluetoothConnections.getInstance(this).removeBluetoothUpdates();
                }catch (Exception e){}

                try{
                    stopForeground(true);
                }catch (Exception e){}
                try{
                    if(!TextUtils.isEmpty(mServiceUUID)){
                        BluetoothConnections.getInstance(this).removeGattCallback(mServiceUUID);
                    }
                    stopSelf();
                }catch (Exception e){}
            }


        } else {
            Log.e(TAG,"665 destroy");
            try{
                BluetoothConnections.getInstance(this).disconnect();
            }catch (Exception e){}
            try{
                BluetoothConnections.getInstance(this).close();
            }catch (Exception e){}

            try{
                mNotificationManager.cancelAll();
            }catch (Exception e){}


            try{
                stopForeground(true);
            }catch (Exception e){}
            try{
                if(!TextUtils.isEmpty(mServiceUUID)){
                    BluetoothConnections.getInstance(this).removeGattCallback(mServiceUUID);
                }
                stopSelf();
            }catch (Exception e){}


        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        mServiceHandler.removeCallbacksAndMessages(null);
    }


    private final IBinder mBinder = new LocalBinder();



    public String getConnectedAddress(){
        return BluetoothConnections.getInstance(this).getConnectedAddress();
    }

    public boolean isDeviceConnected(){
       return BluetoothConnections.getInstance(this).isDeviceConnected();
    }
    public int getPairingDeviceStatus() throws IllegalStateException {
        return BluetoothConnections.getInstance(this).getPairingDeviceStatus();
    }





    public Notification getNotification() {
        createNotificationChannel();
        Intent intent = new Intent(this, BluetoothService.class);

        CharSequence text = getString(R.string.app_name) + " ";
        String title = BluetoothConnections.getInstance(this).bluetoothDeviceAddress();
        if(TextUtils.isEmpty(title)){
            title = getString(R.string.notification_text_device);
        }
        if(BluetoothConnections.getInstance(this).checkDeviceConnected()){
            title += getString(R.string.notification_text_connected);
            /*
            if(BluetoothConnections.getInstance(this).getCurrent() != Float.NaN){
                title+= (getString(R.string.notification_text_glucose) + String.format("%.1f",BluetoothConnections.getInstance(this).getCurrent()));
            }
            */

        } else if(BluetoothConnections.getInstance(this).checkDeviceConnecting()){
            title += getString(R.string.notification_text_connecting);
        } else {
            title += getString(R.string.notification_text_disconnected);
        }


        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String address = PreferenceManager.getDefaultSharedPreferences(this).getString(CommonConstant.PREF_LAST_CONNECT_DEVICE,null);
        Intent activityIntent = null;

        if(address==null){
            activityIntent = new Intent(this, ConnectActivity.class);
        } else {
            activityIntent = new Intent(this, MainActivity.class);
        }

        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                activityIntent,
                //0
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID )
                .setContentText(text)
                .setContentIntent(activityPendingIntent)
                .setContentTitle(title)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_bluetooth_24)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }
        Notification noti = builder.build();

        removeSoundAndVibration(noti);
        return noti;
    }



    private void removeSoundAndVibration(Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        } else {
            notification.sound = null;
            notification.vibrate = null;
            notification.defaults &= ~Notification.DEFAULT_SOUND;
            notification.defaults &= ~Notification.DEFAULT_VIBRATE;
        }
    }
    public Notification createChangeAdminPasswordNotification() {
        createMessageNotificationChannel();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(CommonConstant.EXTRA_STEP, StepHelper.ScreenStep.CHANGE_ADMIN_PASSWORD.ordinal());
        intent.putExtra(CommonConstant.EXTRA_STEP_STRING, StepHelper.ScreenStep.CHANGE_ADMIN_PASSWORD.toString());

        String title = getString(R.string.notification_text_change_sensor);


        CharSequence text = getString(R.string.app_name) + " ";

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this,
                CommonConstant.REQUEST_ACTIVITY,
                intent,PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID )
                .setContentText(title)
                .setContentIntent(activityPendingIntent)
                .setContentTitle(title)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_bluetooth_24)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(MESSAGE_CHANNEL_ID); // Channel ID
        }
        Notification noti = builder.build();
        return noti;
    }
    public Notification createChangeSensorNotification() {
        createMessageNotificationChannel();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(CommonConstant.EXTRA_STEP, StepHelper.ScreenStep.CHANGE_SENSOR.ordinal());
        intent.putExtra(CommonConstant.EXTRA_STEP_STRING, StepHelper.ScreenStep.CHANGE_SENSOR.toString());

        String title = getString(R.string.notification_text_change_sensor);


        CharSequence text = getString(R.string.app_name) + " ";

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this,
                CommonConstant.REQUEST_ACTIVITY,
                intent,PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID )
                .setContentText(title)
                .setContentIntent(activityPendingIntent)
                .setContentTitle(title)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_bluetooth_24)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(MESSAGE_CHANNEL_ID); // Channel ID
        }
        Notification noti = builder.build();
        return noti;
    }
    public Notification createBatteryNotification() {
        createMessageNotificationChannel();
        Intent intent = new Intent(this, SplashActivity.class);

        String title = getString(R.string.notification_text_battery_cahnge);


        CharSequence text = getString(R.string.app_name) + " ";

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this,
                CommonConstant.REQUEST_ACTIVITY,
                intent,PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID )
                .setContentText(title)
                .setContentIntent(activityPendingIntent)
                .setContentTitle(title)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_bluetooth_24)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(MESSAGE_CHANNEL_ID); // Channel ID
        }
        Notification noti = builder.build();
        return noti;
    }
    public Notification createDisconnectAlarmNotification() {
        createMessageNotificationChannel();
        Intent intent = new Intent(this, DisconnectAlarmActivity.class);

        String title = getString(R.string.text_device_disconnected);


        CharSequence text = getString(R.string.app_name) + " ";

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this,
                CommonConstant.NOTIFICATION_ID_DISCONNECT,
                intent,PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID )
                .setContentText(title)
                .setContentIntent(activityPendingIntent)
                .setContentTitle(title)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_bluetooth_24)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(MESSAGE_CHANNEL_ID); // Channel ID
        }
        Notification noti = builder.build();
        return noti;
    }

    public Notification createConnectNotification() {
        createMessageNotificationChannel();
        Intent intent = new Intent(this, ConnectActivity.class);

        String title = getString(R.string.notification_text_device_connect_plz);


        CharSequence text = getString(R.string.app_name) + " ";

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this,
                CommonConstant.REQUEST_CONNECT,
                intent,PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID )
                .setContentText(title)
                .setContentIntent(activityPendingIntent)
                .setContentTitle(title)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_bluetooth_24)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(MESSAGE_CHANNEL_ID); // Channel ID
        }
        Notification noti = builder.build();
        return noti;
    }
    public Notification createWarmupNotification() {
        createMessageNotificationChannel();
        Intent intent = new Intent(this, WarmupActivity.class);
        long calibrationStartTime = PreferenceManager.getDefaultSharedPreferences(this)
                .getLong(CommonConstant.PREF_CALIBRATION_START_TIME,0);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String title = getString(R.string.notification_text_warmup_plz);


        CharSequence text = getString(R.string.app_name) + " ";

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this,
                CommonConstant.REQUEST_WARMUP,
                intent,PendingIntent.FLAG_UPDATE_CURRENT
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID )
                .setContentText(title)
                .setContentIntent(activityPendingIntent)
                .setContentTitle(title)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_bluetooth_24)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(MESSAGE_CHANNEL_ID); // Channel ID
        }
        Notification noti = builder.build();
        return noti;
    }

    public Notification createCalibrationNotification() {
        createMessageNotificationChannel();
        Intent intent = new Intent(this, MainActivity.class);
        long calibrationStartTime = PreferenceManager.getDefaultSharedPreferences(this)
                .getLong(CommonConstant.PREF_CALIBRATION_START_TIME,0);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String title = getString(R.string.notification_calibration) ;
        if(DateUtils.isToday(calibrationStartTime)){
            int tmp1 = sp.getInt(CommonConstant.PREF_CALIBRATION_TMP1,0);
            int tmp2 = sp.getInt(CommonConstant.PREF_CALIBRATION_TMP2,0);
            if(tmp1!=0 && tmp2!=0){
                return null;
            } else if(tmp1!=0){

                if(calibrationStartTime+CommonConstant.CALIBRATION_INPUT_DELAY < System.currentTimeMillis()){
                    title =  getString(R.string.notification_calibration) ;
                } else {
                    long tmpTime = calibrationStartTime + CommonConstant.CALIBRATION_INPUT_DELAY;
                    SimpleDateFormat df = new SimpleDateFormat(getString(R.string.calibration_date_format));
                    title = df.format(new Date(tmpTime)) +  getString(R.string.notification_calibration) ;
                }
            } else {

            }

        } else {
            sp.edit()
                    .putInt(CommonConstant.PREF_CALIBRATION_TMP1,0)
                    .putInt(CommonConstant.PREF_CALIBRATION_TMP2,0)
                    .apply();

        }

        CharSequence text = getString(R.string.app_name) + " ";

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(CommonConstant.EXTRA_STEP, StepHelper.ScreenStep.CALIBRATION.ordinal());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this,
                CommonConstant.REQUEST_ACTIVITY,
                intent,PendingIntent.FLAG_UPDATE_CURRENT
                );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID )
                .setContentText(title)
                .setContentIntent(activityPendingIntent)
                .setContentTitle(title)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_bluetooth_24)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(MESSAGE_CHANNEL_ID); // Channel ID
        }
        Notification noti = builder.build();


        return noti;
    }

    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final String PACKAGE_NAME =
            CommonConstant.BASE_PACKAGE;
    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";




    public float getBattery(){
        return BluetoothConnections.getInstance(this).getBattery();
    }
    public int getRSSI(){
        return BluetoothConnections.getInstance(this).getRSSI();
    }

    public void scanStartForConnect(String address){
        BluetoothConnections.getInstance(this).scanStartForConnect(address);
    }
    public boolean isBluetoothEnabled(){
        return BluetoothConnections.getInstance(this).isBluetoothEnabled();
    }
    public void scanLeDevice(final boolean enable, boolean isReset) {
        BluetoothConnections.getInstance(this).scanLeDevice(enable, isReset);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        setAlarm();
    }
    private void setAlarm(){
        Intent intent = new Intent(CommonConstant.ACTION_ALARM);

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent =
                PendingIntent.getService(this, CommonConstant.REQUEST_CODE_ALARM, intent,
                        PendingIntent.FLAG_CANCEL_CURRENT);
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        if(alarmManager != null){
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        }
    }

}