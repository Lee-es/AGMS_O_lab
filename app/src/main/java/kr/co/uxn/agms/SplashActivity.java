package kr.co.uxn.agms;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import kr.co.uxn.agms.util.StepHelper;

public class SplashActivity extends AppCompatActivity  {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkCurrentState();
    }

    public void checkCurrentState(){

        Intent intent = StepHelper.checkNextState(this,StepHelper.ScreenStep.SPLASH);

//        intent = new Intent(this,MainActivity.class);
//        intent = new Intent(this, DisconnectAlarmActivity.class);

        if(intent!=null){
            startActivity(intent);
            finish();
        }


    }




}