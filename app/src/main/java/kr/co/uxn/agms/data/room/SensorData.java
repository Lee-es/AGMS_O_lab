package kr.co.uxn.agms.data.room;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Random;

@Entity(tableName = RoomConstants.TABLE_SENSOR_DATA)
public class SensorData {
    @PrimaryKey(autoGenerate = true)
    private long uid;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "sensor_time")
    private long sensorTime;

    @ColumnInfo(name = "insert_time")
    private long insertTime;

    @ColumnInfo(name = "bin_data")
    private byte[] binData;

    @ColumnInfo(name = "str_data")
    private String stringData;

    @ColumnInfo(name = "user_name")
    private String userName;



    public SensorData(long uid, String address, long sensorTime, long insertTime, byte[] binData, String stringData, String userName) {
        this.uid = uid;
        this.address = address;
        this.sensorTime = sensorTime;
        this.insertTime = insertTime;
        this.binData = binData;
        this.stringData = stringData;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public long getSensorTime() {
        return sensorTime;
    }

    public void setSensorTime(long sensorTime) {
        this.sensorTime = sensorTime;
    }

    public long getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(long insertTime) {
        this.insertTime = insertTime;
    }

    public byte[] getBinData() {
        return binData;
    }

    public void setBinData(byte[] binData) {
        this.binData = binData;
    }

    public String getStringData() {
        return stringData;
    }

    public void setStringData(String stringData) {
        this.stringData = stringData;
    }


}
