package kr.co.uxn.agms.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.MainActivity;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.GlucoseData;
import kr.co.uxn.agms.data.room.PatientData;
import kr.co.uxn.agms.data.room.SensorLog;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.service.BluetoothService;
import kr.co.uxn.agms.ui.dashboard.FullChartActivity;
import kr.co.uxn.agms.util.GraphUtil;

public class NewHomeFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {


    private LiveData<List<GlucoseData>> mList;
    private boolean isChartInit = false;
    private boolean chartUpdateFlag = false;



    TextView greet;
    TextView userName;
    TextView currentData;
    TextView patientNumber;

    SimpleDateFormat df5 = new SimpleDateFormat("HH");
//    SimpleDateFormat df3 = new SimpleDateFormat("HH:mm");
//    SimpleDateFormat df1 = new SimpleDateFormat("HH:mm:ss");


    private SensorRepository mRepository;

    Handler mHander = new Handler(Looper.getMainLooper());

    LiveData<GlucoseData> lastData;
    LiveData<SensorLog> lastSensorLog;

    AppCompatImageButton fullScreen;
    private long mPatientNumber=0;
    private int graphMin =GraphUtil.getInstance().getGraph_Current_MIN();
    private int graphMax =GraphUtil.getInstance().getGraph_Current_MAX();

    //ChartHelper2 mHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(!CommonConstant.MODE_IS_MEDICAL){
            graphMin = GraphUtil.getInstance().getGraph_Current_MIN();
            graphMax = GraphUtil.getInstance().getGraph_Current_MAX();
        }
        int layoutId = R.layout.fragment_new_home;
        if(CommonConstant.MODE_IS_MEDICAL){
            layoutId = R.layout.fragment_new_home_medical;
        }
        View root = inflater.inflate(layoutId, container, false);
        greet = root.findViewById(R.id.greet_message);
        userName = root.findViewById(R.id.username);
        patientNumber = root.findViewById(R.id.patient_number);
        currentData = root.findViewById(R.id.textView2);

        fullScreen = root.findViewById(R.id.full_screen_button);
        try{
            fullScreen.setOnClickListener(view -> {
                view.setEnabled(false);
                view.postDelayed(() -> view.setEnabled(true),1000);
                Intent intent = new Intent(getContext(), FullChartActivity.class);
                intent.putExtra(FullChartActivity.ARGS_TYPE,CommonConstant.CHART_DATA_DAY );
                startActivity(intent);
            });
        }catch (Exception e){}

        mRepository = new SensorRepository(getActivity().getApplication());


       // mHelper = new ChartHelper2(getContext(), root.findViewById(R.id.chart1), graphMin, graphMax);
//        Intent intent = new Intent(
//                super.getContext(),//현재제어권자
//                AutoSave.class); // 이동할 컴포넌트
//        super.getContext().startService(intent);
//        Log.e("test","자동저장 서비스 시작");

        return root;
    }


    @Override
    public void onResume() {
        super.onResume();
        doOnResume();
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }
    private void doOnResume(){
        setGreetMessage();
        setUserName();
        setCurrentData();
        mHander.removeCallbacks(mUpdateRunnable);
        mHander.postDelayed(mUpdateRunnable, CommonConstant.HOME_UPDATE_DELAY);
    }
    private  void doChartUpdate(){
        mHander.removeCallbacks(mUpdateRunnable);
        mHander.postDelayed(mUpdateRunnable, CommonConstant.HOME_UPDATE_DELAY);
    }

    private boolean isConnected(){
        boolean result = false;
        try {
            result = ((MainActivity)getActivity()).isDeviceConnected();
        }catch (Exception e){}
        return result;
    }
    private void setCurrentData(){
        String address = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(CommonConstant.PREF_LAST_CONNECT_DEVICE, null);
        Log.e("check","setCurrentData:"+address);
        lastData = mRepository.getLastGlucoseData();
        lastData.observe(getViewLifecycleOwner(), mDataObserver);
        if(!TextUtils.isEmpty(address)){
            lastSensorLog = mRepository.getSensorCurrentState(address);
            lastSensorLog.observe(getViewLifecycleOwner(),mLogObserver);
        } else {
            currentData.setText("?");
            currentData.setTag(false);
        }

    }
    private Observer<GlucoseData> mDataObserver = glucoseData -> {
        //doChartUpdate();
        if(glucoseData==null){

            currentData.setText("?");
            return;
        }
        double current = glucoseData.getWe_current();

        if(glucoseData.getData_date() + CommonConstant.MINIMUM_DATA_INTERVAL >= System.currentTimeMillis() ){


            currentData.setText(String.format("%.1f",current));


        } else {
            currentData.setText("-");
        }
    };

    private Observer<SensorLog> mLogObserver = sensorLog -> {
        if(sensorLog==null){
            currentData.setText("?");
            return;
        }

        if(sensorLog.getSensorState()== BluetoothService.STATE_CONNECTED){
            currentData.setTag(true);
        } else {
            currentData.setTag(false);
            currentData.setText("?");
        }
    };

    Runnable mUpdateRunnable = () -> {
        setGreetMessage();
    };

    @Override
    public void onPause() {
        super.onPause();
        mHander.removeCallbacks(mUpdateRunnable);
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setGreetMessage(){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if(5<hour && hour < 12){
            greet.setText(R.string.greet_morning);
        } else if(12<=hour && hour < 15){
            greet.setText(R.string.greet_lunch);
        } else if(15<=hour && hour < 18){
            greet.setText(R.string.greet_afternoon);
        } else if(18<=hour && hour < 22){
            greet.setText(R.string.greet_evening);
        } else {
            greet.setText(R.string.greet_night);
        }
    }
    private void setUserName(){
        if(CommonConstant.MODE_IS_MEDICAL){
            LiveData<PatientData> data = mRepository.getLastUser();
            PatientData mdata =data.getValue();
            data.observe(getViewLifecycleOwner(), patientData -> {
                if(patientData!=null){
                    userName.setText(patientData.getName());
                    patientNumber.setText(String.valueOf(patientData.getPatientNumber()));
                    mPatientNumber = patientData.getPatientNumber();
                   // loadData();
                }

            });
        }
    }

    private void loadData(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTime = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endTime = calendar.getTimeInMillis();
        String address = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(CommonConstant.PREF_LAST_CONNECT_DEVICE, null);

        if(CommonConstant.MODE_IS_MEDICAL){
            if(mPatientNumber!=0){
                mList = mRepository.getGlucoseDataList(mPatientNumber, startTime, endTime);
               // mList.observe(getViewLifecycleOwner(),mListObserver);
              //  mHelper.setData(mList.getValue());
               // mHelper.initChartView();
            }
        } else {
            if(address!=null){
                mList = mRepository.getGlucoseDataList(address, startTime, endTime);
               // mList.observe(getViewLifecycleOwner(),mListObserver);
            }
        }



    }

    private long lastUpdateTime= 0;
//    private Observer<List<GlucoseData>> mListObserver = new Observer<List<GlucoseData>>() {
//        @Override
//        public void onChanged(List<GlucoseData> sensorData) {
//
//            if(!chartUpdateFlag){
//                chartUpdateFlag = true;
//                if(isChartInit){
//                    mHelper.setData(mList.getValue());
//                    mHelper.updateFirstData();
//                    chartUpdateFlag = false;
//                } else {
//                    mHelper.setData(mList.getValue());
//                    lastUpdateTime = System.currentTimeMillis();
//                    mHelper.initChartView();
//                    isChartInit = true;
//                    chartUpdateFlag = false;
//                }
//            }
//
//
//        }
//    };




    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equalsIgnoreCase(CommonConstant.PREF_LAST_CONNECT_DEVICE)){
            setCurrentData();
        }
    }
}