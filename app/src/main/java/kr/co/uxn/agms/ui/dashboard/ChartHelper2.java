package kr.co.uxn.agms.ui.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import kr.co.uxn.agms.CommonConstant;
import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.GlucoseData;
import kr.co.uxn.agms.util.GraphUtil;


public class ChartHelper2 {

    private LineChart mLineChart;
    private int graphMin ;
    private int graphMax ;
    private ArrayList<String> chartXaxis = new ArrayList<>();
    private ArrayList<String> chart_xaxis = new ArrayList<>();
    private List<String> selectedRecordXaxis;
    private int currentType= CommonConstant.CHART_DATA_DAY;
    private List<String> selected_record_xaxis;
    SimpleDateFormat df = new SimpleDateFormat("HH");
    SimpleDateFormat weekDateformat = new SimpleDateFormat("EEE");
    SimpleDateFormat monthDateformat = new SimpleDateFormat("MMM");
    private boolean isResetZoom = false;

    List<GlucoseData> mList;

    final Context mContext;

    public ChartHelper2(final Context context, LineChart chart,int graphMin, int graphMax ) {
        mContext = context;
        mLineChart = chart;
        this.graphMin = graphMin;
        this.graphMax = graphMax;
    }
    public void setChartType(int type){
        currentType = type;
    }
    public void setRest(boolean isResetZoom){
        this.isResetZoom = isResetZoom;
    }
    public void setData(List<GlucoseData> list) {
        mList = list;
    }

    public CustomLineDataSet setLineDataSet(List<Entry> list){
        CustomLineDataSet lineDataSet = new CustomLineDataSet(list, "혈당");
        lineDataSet.setDrawFilled(false);

        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setLineWidth(1.0f);
        lineDataSet.setDrawValues(false);

//        lineDataSet.setCircleHoleRadius(0.5f);
        lineDataSet.setCircleRadius(2f);

        lineDataSet.setCircleColors((ResourcesCompat.getColor(mContext.getResources(), R.color.graph_line, null)));
        lineDataSet.setCircleHoleColor((ResourcesCompat.getColor(mContext.getResources(), R.color.graph_line, null)));

        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setValueTextColor(ResourcesCompat.getColor(mContext.getResources(), R.color.graph_coordinates, null));
        lineDataSet.setColor(ResourcesCompat.getColor(mContext.getResources(), R.color.graph_line, null));
        lineDataSet.calcMinMax();
        float tmpMax = lineDataSet.getYMax();
        float tmpMin = lineDataSet.getYMin();
        if(tmpMax < graphMax){
            tmpMax = graphMax;
        }
        if(tmpMin>graphMin){
            tmpMin = graphMin;
        }
        lineDataSet.setYMax(tmpMax);
        lineDataSet.setYMin(tmpMin);



        return lineDataSet;
    }
    public void setChartData(List<GlucoseData> dataSets){
        selected_record_xaxis = new ArrayList<String>();

        List<ILineDataSet> dataSetsList = new ArrayList<ILineDataSet>();
        ArrayList<Entry> list  = new ArrayList<>();
        int tmpIndex =0;
        if(dataSets==null || dataSets.isEmpty()){
            mLineChart.setData(null);
            mLineChart.notifyDataSetChanged();
            return;
        }

        for(GlucoseData data : dataSets){
            double tmpValue= data.getWe_current();
            Entry entry = new Entry(tmpIndex, data.getUid()>0? (float)tmpValue  :0);
            list.add(entry);
            if(currentType == CommonConstant.CHART_DATA_MONTH){
                selected_record_xaxis.add(monthDateformat.format(data.getData_date()));
            } else if(currentType == CommonConstant.CHART_DATA_WEEK){
                selected_record_xaxis.add(weekDateformat.format(data.getData_date()));
            } else {
                selected_record_xaxis.add(df.format(data.getData_date()));
            }

            tmpIndex++;

        }

        dataSetsList.add(setLineDataSet(list));

        LineData data = new LineData(dataSetsList);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if(value==0.0f){
                    return "";
                }
                return getFormatted(value);
            }
        });

        mLineChart.setData(data);
        setChartXAxis(selected_record_xaxis);


        mLineChart.notifyDataSetChanged();
        mLineChart.getData().setHighlightEnabled(false);
        mLineChart.highlightValues(null);
        if(isResetZoom){
            isResetZoom = false;
            mLineChart.fitScreen();
        }

        mLineChart.invalidate();
    }
    public void updateFirstData(){
        List<GlucoseData> dbData = null;

        try{
            dbData = mList;
        }catch (Exception e){}


        List<GlucoseData> intervalList = new ArrayList<>();
        if(dbData!=null && !dbData.isEmpty()) {
            GlucoseData firstData=dbData.get(0);
            long firstTime=firstData.getData_date();

            float dataCount=0;
            long targetTime=firstTime + GraphUtil.getInstance().getGraph_TargetTime();
            long dataTime=firstTime;
            float sumCurrent=0;
            GlucoseData beforeData=null;
            for (GlucoseData data : dbData) {
                 if(GraphUtil.getInstance().getGraph_Current_MIN()<=data.getWe_current()&&data.getWe_current()<=GraphUtil.getInstance().getGraph_Current_MAX()) {
                if (beforeData != null && data.getData_date() >= targetTime) {
                    GlucoseData newData=new GlucoseData(
                            beforeData.getUid(), beforeData.getDeviceAddress(), dataTime,
                            sumCurrent / dataCount, beforeData.getWe_glucose(), beforeData.getUser(),
                            beforeData.getPatientNumber(), beforeData.getBatteryLevel()
                    );


                    intervalList.add(newData);
                    targetTime=data.getData_date() + GraphUtil.getInstance().getGraph_TargetTime();
                    dataTime=data.getData_date();
                    sumCurrent=0;
                    dataCount=0;


                }
                beforeData=data;
                dataCount++;
                sumCurrent+=data.getWe_current();

            }
        }
            if(intervalList.size()==0)
            {
                GlucoseData newData = new GlucoseData(
                        firstData.getUid(), firstData.getDeviceAddress(),System.currentTimeMillis(),
                        0,0, firstData.getUser(),
                        firstData.getPatientNumber(), firstData.getBatteryLevel());
                intervalList.add(newData);
            }


            if(beforeData!=null){
                GlucoseData newData = new GlucoseData(
                        beforeData.getUid(), beforeData.getDeviceAddress(), dataTime,
                        sumCurrent / dataCount, beforeData.getWe_glucose(), beforeData.getUser(),
                        beforeData.getPatientNumber(), beforeData.getBatteryLevel()
                );
                intervalList.add(newData);
            }

        }
        setChartData(intervalList);

    }
    private void setChartXAxis(List<String> dataSets){
        chart_xaxis = new ArrayList<>();
        for(int i = 0 ; i < dataSets.size(); i++){
            chart_xaxis.add(dataSets.get(i));
        }
        mLineChart.getXAxis().setValueFormatter(xaxis_format);
    }
    public void initChartView(){
        mLineChart.setScaleXEnabled(true);
        mLineChart.setScaleYEnabled(true);
        mLineChart.setDrawGridBackground(false);
        mLineChart.getDescription().setEnabled(false);
        mLineChart.getAxisRight().setDrawAxisLine(false);
        mLineChart.getAxisLeft().setDrawAxisLine(false);
        mLineChart.getAxisRight().setDrawLabels(false);
        mLineChart.getAxisLeft().setDrawLabels(false);

        mLineChart.getAxisLeft().setTextColor(Color.WHITE);
        mLineChart.getAxisLeft().setTextSize(10f);
        mLineChart.getAxisLeft().setDrawAxisLine(true);
        mLineChart.getAxisLeft().setDrawLabels(true);
        mLineChart.getAxisLeft().setDrawGridLines(true);
        mLineChart.getAxisLeft().setDrawTopYLabelEntry(true);
        mLineChart.getAxisLeft().setDrawZeroLine(true);
        mLineChart.getAxisLeft().setDrawLimitLinesBehindData(true);
        mLineChart.getAxisLeft().setDrawLimitLinesBehindData(true);

        mLineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mLineChart.getXAxis().setDrawGridLines(false);
        mLineChart.getXAxis().setTextSize(10f);
        mLineChart.getXAxis().setTextColor(Color.WHITE);
        mLineChart.getXAxis().setDrawGridLines(false);
        mLineChart.getXAxis().setLabelCount(7);

        mLineChart.getXAxis().setDrawLabels(true);
        mLineChart.getLegend().setEnabled(false);

        mLineChart.getXAxis().setDrawGridLines(false);

        updateFirstData();

    }
    private String getFormatted(double value){
        return String.format("%.1f", value);
    }

    private ValueFormatter xaxis_format  = new ValueFormatter() {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            try{
                return chart_xaxis.toArray()[(int)value].toString();
            }catch (Exception e){

            }
            return "";

        }
    };
}