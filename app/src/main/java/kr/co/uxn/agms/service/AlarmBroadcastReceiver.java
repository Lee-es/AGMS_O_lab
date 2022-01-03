package kr.co.uxn.agms.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import kr.co.uxn.agms.CommonConstant;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(context!=null) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED) || TextUtils.equals(intent.getAction(), CommonConstant.ACTION_SERVICE_CONNECTED)) {
                Intent serviceIntent = new Intent(context, BluetoothService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }

            }
        }

    }
}