package kr.co.uxn.agms.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.text.TextUtils;
import android.text.format.DateUtils;

import androidx.preference.PreferenceManager;

import java.util.Calendar;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.MainActivity;
import kr.co.uxn.agms.SplashActivity;
import kr.co.uxn.agms.ui.admin.CreateAdminActivity;
import kr.co.uxn.agms.ui.connect.ConnectActivity;
import kr.co.uxn.agms.ui.connect.WarmupActivity;
import kr.co.uxn.agms.ui.patient.CreatePatientActivity;

public class StepHelper {
    private StepHelper(){

    }
    public enum ScreenStep {
        SPLASH, LOGIN, CONNECT, WARM_UP, HOME,CALIBRATION, CREATE_PATIENT, CREATE_ADMIN,CHANGE_SENSOR,CHANGE_ADMIN_PASSWORD
    }

    private static String USER_NAME  = null;

    public static void setUserName(String name){
        USER_NAME = name;
    }
    public static String getUserName(){
        return USER_NAME;
    }

    public static Intent checkNextState(final Context context, ScreenStep currentStep){
        boolean isGoLoginActivity = false;
        boolean isGoCreatePatientActivity = false;
        boolean isGoConnectActivity = false;
        boolean isGoWarmUpActivity = false;
        boolean isGoHome = false;
        boolean isChangeSensor = false;
        boolean isGoAdminRegister = false;
        boolean isGoCalibration = false;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String deviceAddress = sp.getString(CommonConstant.PREF_LAST_CONNECT_DEVICE,null);
        long sensorDate = sp.getLong(CommonConstant.PREF_DEVICE_NEW_SENSOR_DATE, 0);
        long deviceFirstPairingDate = sp.getLong(CommonConstant.PREF_DEVICE_FIRST_PAIRING_DATE,0);
        long warmUpStartDate = sp.getLong(CommonConstant.PREF_WARM_UP_START_DATE,0);
        long lastCalibrationDAte = sp.getLong(CommonConstant.PREF_CALIBRATION_START_TIME,0);


        if(TextUtils.isEmpty(deviceAddress)){
            isGoConnectActivity = true;
        }
        if(warmUpStartDate == 0){
            isGoConnectActivity = true;
        } else {
            long currentTime = System.currentTimeMillis();
            if(currentTime - warmUpStartDate < CommonConstant.WARM_UP_DELAY){
                isGoWarmUpActivity = true;
            }
        }
        if(lastCalibrationDAte == 0 || !DateUtils.isToday(lastCalibrationDAte)){
            isGoCalibration= true;
        }
        if(sensorDate > 0){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(sensorDate);
            calendar.add(Calendar.DAY_OF_MONTH,10);
            long targetDate = calendar.getTimeInMillis();
            if(targetDate <= System.currentTimeMillis()){
                isChangeSensor = true;
            }
        }


        ScreenStep step = ScreenStep.HOME;

        if(currentStep==ScreenStep.SPLASH){
            isGoLoginActivity = true;

        }
        if(CommonConstant.MODE_IS_MEDICAL){
            isGoLoginActivity = false;
            String patientName = sp.getString(CommonConstant.PREF_CURRENT_PATIENT_NAME, null);
            long patientNumber = sp.getLong(CommonConstant.PREF_CURRENT_PATIENT_NUMBER, 0);
            String currentAdminId = sp.getString(CommonConstant.PREF_CURRENT_ADMIN_ID, null);
            if(TextUtils.isEmpty(patientName) || patientNumber==0){
                isGoCreatePatientActivity = true;
            }
            if(TextUtils.isEmpty(currentAdminId)){
                isGoAdminRegister = true;
            }
        }

        if(isGoCalibration){
            step =  ScreenStep.CALIBRATION;
        }
        if(isGoWarmUpActivity){
            step =  ScreenStep.WARM_UP;
        }
        if(isGoConnectActivity){
            step =  ScreenStep.CONNECT;
        }

        if(isGoLoginActivity){
            step = ScreenStep.LOGIN;
        }
        if(isGoCreatePatientActivity){
            step = ScreenStep.CREATE_PATIENT;
        }
        if(isGoAdminRegister){
            step = ScreenStep.CREATE_ADMIN;
        }

        Intent intent = null;
        switch (step){
            case CONNECT:
                intent = new Intent(context, ConnectActivity.class);
                break;
            case LOGIN:

                break;
            case SPLASH:
                intent = new Intent(context, SplashActivity.class);
                break;
            case WARM_UP:
                intent = new Intent(context, WarmupActivity.class);
                break;
            case HOME:
                intent = new Intent(context, MainActivity.class);
                break;
            case CALIBRATION:
                intent = new Intent(context, MainActivity.class);
                intent.putExtra(CommonConstant.EXTRA_STEP, ScreenStep.CALIBRATION.ordinal());
                intent.putExtra(CommonConstant.EXTRA_STEP_STRING, ScreenStep.CALIBRATION.toString());
                break;
            case CREATE_PATIENT:
                intent = new Intent(context, CreatePatientActivity.class);
                break;
            case CREATE_ADMIN:
                intent = new Intent(context, CreateAdminActivity.class);
                break;
            default:
                break;
        }

        return intent;
    }
}
