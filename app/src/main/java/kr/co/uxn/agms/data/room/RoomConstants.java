package kr.co.uxn.agms.data.room;

public class RoomConstants {
    private RoomConstants(){

    }
    public static final String DATABASE_NAME = "uxn_database";

    public static final int VERSION_1 = 2;

    /**
     * 현재 데이터베이스 버전
     */
    public static final int DATABASE_VERSION = VERSION_1;

    public static final String TABLE_SENSOR_LOG = "sensor_log";
    public static final String TABLE_SENSOR_DATA = "sensor_data";
    public static final String TABLE_USER_DATA = "user_data";
    public static final String TABLE_CARLIBRATION = "calibration";
    public static final String TABLE_DEVICE_DATA = "device_data";
    public static final String TABLE_GLUCOSE_DATA = "glucose_data";
    public static final String TABLE_PATIENT_DATA = "patient_data";
    public static final String TABLE_EVENT_DATA = "event_data";
    public static final String TABLE_ADMIN_DATA = "admin_data";
}
