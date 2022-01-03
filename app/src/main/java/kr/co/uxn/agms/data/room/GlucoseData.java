package kr.co.uxn.agms.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = RoomConstants.TABLE_GLUCOSE_DATA)
public class GlucoseData {
    @PrimaryKey(autoGenerate = true)
    private long uid;

    @ColumnInfo(name = "device_address")
    private String deviceAddress;

    @ColumnInfo(name = "data_date")
    private long data_date;

    @ColumnInfo(name = "we_current")
    private float we_current;

    @ColumnInfo(name = "we_glucose")
    private float we_glucose;

    @ColumnInfo(name = "user")
    private String user;

    @ColumnInfo(name = "patient_number")
    private long patientNumber;

    @ColumnInfo(name = "battery_level")
    private float batteryLevel;

    public GlucoseData(long uid, String deviceAddress, long data_date, float we_current, float we_glucose, String user, long patientNumber, float batteryLevel) {
        this.uid = uid;
        this.deviceAddress = deviceAddress;
        this.data_date = data_date;
        this.we_current = we_current;
        this.we_glucose = we_glucose;
        this.user = user;
        this.patientNumber = patientNumber;
        this.batteryLevel = batteryLevel;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public long getData_date() {
        return data_date;
    }

    public void setData_date(long data_date) {
        this.data_date = data_date;
    }

    public float getWe_current() {
        return we_current;
    }

    public void setWe_current(float we_current) {
        this.we_current = we_current;
    }

    public float getWe_glucose() {
        return we_glucose;
    }

    public void setWe_glucose(float we_glucose) {
        this.we_glucose = we_glucose;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(long patientNumber) {
        this.patientNumber = patientNumber;
    }

    public float getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(float batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}
