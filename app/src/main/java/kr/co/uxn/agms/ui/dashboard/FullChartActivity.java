package kr.co.uxn.agms.ui.dashboard;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.Calendar;
import java.util.List;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.GlucoseData;
import kr.co.uxn.agms.data.room.PatientData;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.util.GraphUtil;

public class FullChartActivity extends AppCompatActivity {
    public static final String ARGS_TYPE=  "args_type";


    private AppCompatRadioButton dayButton;
    private AppCompatRadioButton weekButton;
    private AppCompatRadioButton monthButton;
//    private LineChart mLineChart;

    private AppCompatImageButton closeButton;

    private SensorRepository mRepository;


//    private int currentType= CommonConstant.CHART_DATA_DAY;

    private long chartStartTime =0;
    private long chartEndTime = 0;
    private LiveData<List<GlucoseData>> mList;
    private boolean isChartInit = false;
    private boolean chartUpdateFlag = false;

    private int graphMin = GraphUtil.getInstance().getGraph_Current_MIN();
    private int graphMax = GraphUtil.getInstance().getGraph_Current_MAX();
    ChartHelper2 mHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_chart);
        if(!CommonConstant.MODE_IS_MEDICAL){
            graphMin = GraphUtil.getInstance().getGraph_Current_MIN();
            graphMax = GraphUtil.getInstance().getGraph_Current_MAX();
        }
        mRepository = new SensorRepository(getApplication());

        closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
//        mLineChart = findViewById(R.id.chart1);
        dayButton = findViewById(R.id.day_average);
        weekButton = findViewById(R.id.week_average);
        monthButton = findViewById(R.id.month_average);

        dayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dayButton.setEnabled(false);
                dayButton.post(new Runnable() {
                    @Override
                    public void run() {
                        dayButton.setEnabled(true);
                    }
                });

                doWhenButtonClick();
            }
        });
        weekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                weekButton.setEnabled(false);
                weekButton.post(new Runnable() {
                    @Override
                    public void run() {
                        weekButton.setEnabled(true);
                    }
                });
                doWhenButtonClick();
            }
        });
        monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                monthButton.setEnabled(false);
                monthButton.post(new Runnable() {
                    @Override
                    public void run() {
                        monthButton.setEnabled(true);
                    }
                });
                doWhenButtonClick();
            }
        });

        int type = getIntent().getIntExtra(ARGS_TYPE, CommonConstant.CHART_DATA_DAY);
        mHelper = new ChartHelper2(FullChartActivity.this,findViewById(R.id.chart1), graphMin, graphMax);
        mHelper.setChartType(type);
        switch (type){
            case CommonConstant.CHART_DATA_DAY:
                dayButton.performClick();
                break;
            case CommonConstant.CHART_DATA_WEEK:
                weekButton.performClick();
                break;
            case CommonConstant.CHART_DATA_MONTH:
                monthButton.performClick();
                break;
            default:
                break;
        }

        hideSystemUI();
        if(CommonConstant.MODE_IS_MEDICAL){
            LiveData<PatientData> patientData = mRepository.getLastUser();
            patientData.observe(this, mPatientObserver);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
    public void doWhenButtonClick(){
        mHelper.setRest(true);
        if(dayButton.isChecked()){
            getData(CommonConstant.CHART_DATA_DAY);
        } else if(weekButton.isChecked()){
            getData(CommonConstant.CHART_DATA_WEEK);
        } else if(monthButton.isChecked()){
            getData(CommonConstant.CHART_DATA_MONTH);
        }
    }
    public void getData(int type){
        mHelper.setChartType(type);
        Calendar calendar = Calendar.getInstance();
        if(type==CommonConstant.CHART_DATA_MONTH){
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            chartEndTime = calendar.getTimeInMillis();
            calendar.add(Calendar.MONTH,-1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            chartStartTime = calendar.getTimeInMillis();
        } else if(type==CommonConstant.CHART_DATA_WEEK){
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            chartEndTime = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_MONTH,-7);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            chartStartTime = calendar.getTimeInMillis();
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            chartStartTime = calendar.getTimeInMillis();

            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            chartEndTime = calendar.getTimeInMillis();
        }


        if(CommonConstant.MODE_IS_MEDICAL && mPatientNumber!=0){
            mList = mRepository.getGlucoseDataList(mPatientNumber, chartStartTime,chartEndTime);
            mList.observe(this,mListObserver);
        } else if(!CommonConstant.MODE_IS_MEDICAL){
            String address = PreferenceManager.getDefaultSharedPreferences(this)
                    .getString(CommonConstant.PREF_LAST_CONNECT_DEVICE, null);


            if(address!=null){
                mList = mRepository.getGlucoseDataList(address, chartStartTime,chartEndTime);
                mList.observe(this,mListObserver);
            }
        }
    }
    private long lastUpdateTime= 0;
    private long mPatientNumber = 0;
    private Observer<PatientData> mPatientObserver = new Observer<PatientData>() {
        @Override
        public void onChanged(PatientData patientData) {
            if(patientData!=null){
                if(patientData.getPatientNumber()!=mPatientNumber){
                    mPatientNumber = patientData.getPatientNumber();
                    doWhenButtonClick();
                }
            }
        }
    };
    private Observer<List<GlucoseData>> mListObserver = new Observer<List<GlucoseData>>() {
        @Override
        public void onChanged(List<GlucoseData> sensorData) {

            if(!chartUpdateFlag){
                chartUpdateFlag = true;
                if(isChartInit){
                    mHelper.setData(mList.getValue());
                    mHelper.updateFirstData();
                    chartUpdateFlag = false;
                } else {
                    mHelper.setData(mList.getValue());
                    lastUpdateTime = System.currentTimeMillis();
                    mHelper.initChartView();
                    isChartInit = true;
                    chartUpdateFlag = false;
                }
            }


        }
    };

}
