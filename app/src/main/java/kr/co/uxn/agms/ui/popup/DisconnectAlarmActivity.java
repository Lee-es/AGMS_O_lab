package kr.co.uxn.agms.ui.popup;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.SensorLog;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.service.BluetoothService;

public class DisconnectAlarmActivity extends AppCompatActivity {

    private static final int HANDLER_STOP = 1;
    private static final int HANDLER_START = 2;
    AppCompatButton buttonConfirm;
    SensorRepository mRepository;

    Vibrator vibrator;
    MediaPlayer player;

    Handler mHandler;
    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disconnect_alarm);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyApp::MyWakelockTag");
        wakeLock.acquire();
        mHandler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                mHandler.removeMessages(HANDLER_START);
                mHandler.removeMessages(HANDLER_STOP);
                if(msg.what == HANDLER_START){
                    mHandler.sendEmptyMessageDelayed(HANDLER_STOP, CommonConstant.SOUND_VIBRATE_DURATION);
                    vibrate();
                    playSound();
                } else if(msg.what ==HANDLER_STOP){
                    mHandler.sendEmptyMessageDelayed(HANDLER_START, CommonConstant.ONE_MINUTE);
                    try {
                        vibrator.cancel();
                    }catch (Exception e){

                    }
                    try {
                        player.pause();
                    }catch (Exception e){}
                }
            }
        };
        buttonConfirm = findViewById(R.id.confirm_button);
        buttonConfirm.setOnClickListener(view -> {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true),500);
            removeAlert();
        });
        mRepository = new SensorRepository(getApplication());
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        player = MediaPlayer.create(this,
                R.raw.warning);


        player.setLooping(true);

        playSound();
        vibrate();
        mHandler.sendEmptyMessageDelayed(HANDLER_STOP, CommonConstant.SOUND_VIBRATE_DURATION);
        setData();
    }
    public void setData(){
        LiveData<SensorLog> logData = mRepository.getLastLog();
        logData.observe(this, sensorLog -> {
            if(sensorLog == null){
                return;
            }
            if(sensorLog.getSensorState()== BluetoothService.STATE_CONNECTED){
                removeAlert();
            }
        });
    }
    public void vibrate(){
        try{
            long[] pattern = {0, 200, 1000};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                vibrator.vibrate(VibrationEffect.createWaveform(pattern,0));
            } else {

                vibrator.vibrate(pattern,0);
            }
        }catch (Exception e){

        }
    }

    @Override
    public void onBackPressed() {
        removeAlert();
    }

    public void playSound(){

        try {
            player.seekTo(0);
            player.start();
        }catch (Exception e){

        }
    }

    public void removeAlert(){
        mHandler.removeMessages(HANDLER_START);
        mHandler.removeMessages(HANDLER_STOP);
        try {
            vibrator.cancel();
        }catch (Exception e){}
        try {
            player.stop();
        }catch (Exception e){}
        try {
            wakeLock.release();
        }catch (Exception e){}

        NotificationManager manager =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(CommonConstant.NOTIFICATION_ID_DISCONNECT);
        finish();
    }



}
