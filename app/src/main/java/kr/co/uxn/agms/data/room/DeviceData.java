package kr.co.uxn.agms.data.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = RoomConstants.TABLE_DEVICE_DATA)
public class DeviceData {
    @PrimaryKey(autoGenerate = true)
    private long uid;
    @ColumnInfo(name = "address")
    private String address;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "sensitivity")
    private long sensitivity;
    @ColumnInfo(name = "base_current")
    private long baseCurrent;
    @ColumnInfo(name = "sending_interval")
    private int sendingInterval;
    @ColumnInfo(name = "notification_uart")
    private int notificationUart;
    @ColumnInfo(name = "min_y")
    private int minY;
    @ColumnInfo(name = "max_y")
    private int maxY;
    @ColumnInfo(name = "low")
    private int low;
    @ColumnInfo(name = "high")
    private int high;
    @ColumnInfo(name = "urgent_low")
    private int urgentLow;
    @ColumnInfo(name = "urgent_high")
    private int getUrgentHigh;
    @ColumnInfo(name = "range_x")
    private int rangeX;
    @ColumnInfo(name = "create_date")
    private long createDate;
    @ColumnInfo(name = "sensor_start_date")
    private long sensorStartDate;

    public DeviceData(long uid, String address, String name, long sensitivity, long baseCurrent, int sendingInterval, int notificationUart, int minY, int maxY, int low, int high, int urgentLow, int getUrgentHigh, int rangeX, long createDate, long sensorStartDate) {
        this.uid = uid;
        this.address = address;
        this.name = name;
        this.sensitivity = sensitivity;
        this.baseCurrent = baseCurrent;
        this.sendingInterval = sendingInterval;
        this.notificationUart = notificationUart;
        this.minY = minY;
        this.maxY = maxY;
        this.low = low;
        this.high = high;
        this.urgentLow = urgentLow;
        this.getUrgentHigh = getUrgentHigh;
        this.rangeX = rangeX;
        this.createDate = createDate;
        this.sensorStartDate = sensorStartDate;
    }

    public long getSensorStartDate() {
        return sensorStartDate;
    }

    public void setSensorStartDate(long sensorStartDate) {
        this.sensorStartDate = sensorStartDate;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(long sensitivity) {
        this.sensitivity = sensitivity;
    }

    public long getBaseCurrent() {
        return baseCurrent;
    }

    public void setBaseCurrent(long baseCurrent) {
        this.baseCurrent = baseCurrent;
    }

    public int getSendingInterval() {
        return sendingInterval;
    }

    public void setSendingInterval(int sendingInterval) {
        this.sendingInterval = sendingInterval;
    }

    public int getNotificationUart() {
        return notificationUart;
    }

    public void setNotificationUart(int notificationUart) {
        this.notificationUart = notificationUart;
    }

    public int getMinY() {
        return minY;
    }

    public void setMinY(int minY) {
        this.minY = minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public int getUrgentLow() {
        return urgentLow;
    }

    public void setUrgentLow(int urgentLow) {
        this.urgentLow = urgentLow;
    }

    public int getGetUrgentHigh() {
        return getUrgentHigh;
    }

    public void setGetUrgentHigh(int getUrgentHigh) {
        this.getUrgentHigh = getUrgentHigh;
    }

    public int getRangeX() {
        return rangeX;
    }

    public void setRangeX(int rangeX) {
        this.rangeX = rangeX;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }
}
