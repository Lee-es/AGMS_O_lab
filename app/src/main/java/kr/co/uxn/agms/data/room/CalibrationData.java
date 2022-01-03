package kr.co.uxn.agms.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = RoomConstants.TABLE_CARLIBRATION)
public class CalibrationData {

    @PrimaryKey(autoGenerate = true)
    private long uid;

    @ColumnInfo(name = "device_address")
    private String deviceAddress;

    @ColumnInfo(name = "calibration1_date")
    private long calibration1Date;
    @ColumnInfo(name = "calibration1")
    private int calibration1;
    @ColumnInfo(name = "calibration1_signal")
    private float calibration1Signal;
    @ColumnInfo(name = "calibration2_date")
    private long calibration2Date;
    @ColumnInfo(name = "calibration2")
    private int calibration2;
    @ColumnInfo(name = "calibration2_signal")
    private float calibration2Signal;

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public long getCalibration1Date() {
        return calibration1Date;
    }

    public void setCalibration1Date(long calibration1_date) {
        this.calibration1Date = calibration1_date;
    }

    public int getCalibration1() {
        return calibration1;
    }

    public void setCalibration1(int calibration1) {
        this.calibration1 = calibration1;
    }

    public float getCalibration1Signal() {
        return calibration1Signal;
    }

    public void setCalibration1Signal(float calibration1Signal) {
        this.calibration1Signal = calibration1Signal;
    }

    public long getCalibration2Date() {
        return calibration2Date;
    }

    public void setCalibration2Date(long calibration2Date) {
        this.calibration2Date = calibration2Date;
    }

    public int getCalibration2() {
        return calibration2;
    }

    public void setCalibration2(int calibration2) {
        this.calibration2 = calibration2;
    }

    public float getCalibration2Signal() {
        return calibration2Signal;
    }

    public void setCalibration2Signal(float calibration2Signal) {
        this.calibration2Signal = calibration2Signal;
    }

    public CalibrationData(long uid, String deviceAddress, long calibration1Date, int calibration1, float calibration1Signal, long calibration2Date, int calibration2, float calibration2Signal) {
        this.uid = uid;
        this.deviceAddress = deviceAddress;
        this.calibration1Date = calibration1Date;
        this.calibration1 = calibration1;
        this.calibration1Signal = calibration1Signal;
        this.calibration2Date = calibration2Date;
        this.calibration2 = calibration2;
        this.calibration2Signal = calibration2Signal;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}
