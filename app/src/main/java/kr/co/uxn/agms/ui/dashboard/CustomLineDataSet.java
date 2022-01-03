package kr.co.uxn.agms.ui.dashboard;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.List;

public class CustomLineDataSet extends LineDataSet {
    public CustomLineDataSet(List<Entry> yVals, String label) {
        super(yVals, label);
    }
    public void setYMax(float value){
        mYMax = value;
    }
    public void setYMin(float value){
        mYMin = value;
    }
}