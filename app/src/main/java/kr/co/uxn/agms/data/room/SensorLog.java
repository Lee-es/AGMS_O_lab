package kr.co.uxn.agms.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = RoomConstants.TABLE_SENSOR_LOG)
public class SensorLog {
    @PrimaryKey(autoGenerate = true)
    private long uid;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "sensor_state")
    private int sensorState;

    @ColumnInfo(name = "event_time")
    private long eventTime;

    public SensorLog(long uid, String address, int sensorState, long eventTime) {
        this.uid = uid;
        this.address = address;
        this.sensorState = sensorState;
        this.eventTime = eventTime;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getSensorState() {
        return sensorState;
    }

    public void setSensorState(int sensorState) {
        this.sensorState = sensorState;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }
}
