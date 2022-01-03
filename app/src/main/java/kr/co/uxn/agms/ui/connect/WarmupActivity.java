package kr.co.uxn.agms.ui.connect;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import com.mindorks.RadialProgressBar;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.ui.BleActivity;

public class WarmupActivity extends BleActivity {

    Button buttonIdleCheck;
    Button buttonSkipWarmup;
    TextView textViewState;

    Handler mHandler;
    RadialProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_warm_up);
        mHandler = new Handler(Looper.getMainLooper());
        progressBar = findViewById(R.id.progress_view);
        textViewState= findViewById(R.id.progress);
        buttonIdleCheck = findViewById(R.id.idle_check);
        buttonSkipWarmup = findViewById(R.id.buttoSkp);
        buttonIdleCheck.setOnClickListener(view -> {
            buttonIdleCheck.setEnabled(false);
            checkWarmUp(true);
            buttonIdleCheck.postDelayed(() -> buttonIdleCheck.setEnabled(true),1000);
        });
        buttonSkipWarmup.setOnClickListener(view -> {
            buttonSkipWarmup.setEnabled(false);
            buttonSkipWarmup.postDelayed(() -> buttonSkipWarmup.setEnabled(true),1000);
            skipWarmup();
        });
        serviceBinding();

    }
    public void skipWarmup(){
        mHandler.removeCallbacks(mRunnable);
        long insertValue = System.currentTimeMillis() - CommonConstant.WARM_UP_DELAY;
        PreferenceManager.getDefaultSharedPreferences(WarmupActivity.this)
                .edit().putLong(CommonConstant.PREF_WARM_UP_START_DATE, insertValue).apply();
        goHome();
    }

    public void checkWarmUp(boolean show){
        mHandler.removeCallbacks(mRunnable);
        double value = 0;
        if(mBluetoothService!=null){
            value = mBluetoothService.getCurrent();
        }

        //change logic for current value
        if(value > 200){
            //if value is valid, go home
        }
        long warmUpStartDate = PreferenceManager.getDefaultSharedPreferences(this)
                .getLong(CommonConstant.PREF_WARM_UP_START_DATE,0);
        long currentTime = System.currentTimeMillis();
        if(warmUpStartDate!=0 && currentTime > warmUpStartDate +  CommonConstant.WARM_UP_DELAY){
            mHandler.removeCallbacks(mRunnable);
            goHome();

        } else {
            if(warmUpStartDate!=0){
                double perecent = (currentTime - warmUpStartDate) / (double)CommonConstant.WARM_UP_DELAY * 100.0;
                Log.e("check","warmup: "+warmUpStartDate + " / " + currentTime + " / " + perecent);
                textViewState.setText(Math.round(perecent)  + " %");
                try {
                    progressBar.setOuterProgress((int)Math.round(perecent));
                }catch (Exception e){}

            } else {
                textViewState.setText("? %");
            }




            mHandler.postDelayed(mRunnable, 5000);
            if(show){

                new AlertDialog.Builder(this)
                        .setTitle(R.string.alert_title)
                        .setMessage(R.string.dialog_message_retry_warmup)
                        .setPositiveButton(R.string.dialog_button_stable, (dialogInterface, i) -> resetWarmup())
                        .setNegativeButton(android.R.string.cancel, null)
                        .setNeutralButton(R.string.dialog_button_change_sensor, (dialogInterface, i) -> changeSensor())
                        .show();
            }

        }


    }
    private void resetWarmup(){
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putLong(CommonConstant.PREF_WARM_UP_START_DATE, System.currentTimeMillis())
                .apply();
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 5000);

    }
    private void changeSensor(){
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putLong(CommonConstant.PREF_WARM_UP_START_DATE, System.currentTimeMillis())
                .putLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE, System.currentTimeMillis())
                .apply();

        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 5000);

    }
    Runnable mRunnable = () -> checkWarmUp(false);

    @Override
    protected void onResume() {
        super.onResume();
        checkWarmUp(false);
    }

    @Override
    public void doWhenDeviceConnected() {

    }

    @Override
    public void doWhenDeviceDisconnected() {

    }

    @Override
    public void doWhenDeviceFound(Intent intent) {

    }

    @Override
    public void doWhenConnectFail() {

    }
}
