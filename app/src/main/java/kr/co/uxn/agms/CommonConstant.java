package kr.co.uxn.agms;



public class CommonConstant {
    private CommonConstant(){}

    public static final String PROGRESS_TAG = "fragment_progress";

    public static final int REQUEST_ACTIVITY = 11111;
    public static final int REQUEST_CONNECT = 11112;
    public static final int REQUEST_WARMUP = 11113;
    public static final int NOTIFICATION_ID_DISCONNECT = 1114;

    public static final int REQUEST_CODE_BLE_ENABLE = 1222;
    public static final int REQUEST_CODE_CHECK_PASSWORD = 1223;
    public static final int REQUEST_CODE_CHANGE_PASSWORD= 1224;
    public static final int REQUEST_CODE_PERMISSION_BLUETOOTH= 1225;
    public static final int REQUEST_CODE_PERMISSION_WRITE= 1226;

    public static final int REQUEST_CODE_ALARM= 2000;
    public static final int REQUEST_CREATE_FILE_FOR_DATA = 2001;
    public static final int REQUEST_CREATE_FILE_FOR_EVENT = 2002;

    public static final String BASE_PACKAGE = "kr.co.uxn.agms";

    public static final String ACTION_ALARM = "kr.co.uxn.agms.alarm";
    public static final String ACTION_SERVICE_CONNECTED = BASE_PACKAGE + ".action.service_connected";


    public static final int CHART_DATA_DAY = 1;
    public static final int CHART_DATA_WEEK = 2;
    public static final int CHART_DATA_MONTH = 3;

    public static final String PREF_USE_NOTIFICATION = "pref_use_notification";
    public static final String PREF_LAST_CONNECT_DEVICE = "pref_last_connect_device";

    public static final String PREF_WARM_UP_START_DATE = "pref_warm_start_date";
    public static final String PREF_DEVICE_FIRST_PAIRING_DATE = "pref_device_first_paring_date";
    public static final String PREF_DEVICE_NEW_SENSOR_DATE = "pref_new_sensor_date";
    public static final String PREF_SESSION_TIME = "pref_session_time";

    public static final String PREF_ALERT_HIGH_VALUE = "pref_alert_high_value";
    public static final String PREF_ALERT_LOW_VALUE = "pref_alert_low_value";

    public static final String PREF_USERNAME = "pref_username";
    public static final String PREF_PASSWORD = "pref_password";

    public static final int DEFAULT_ALERT_HIGH = 180;
    public static final int DEFAULT_ALERT_LOW = 50;

    public static final long HOME_UPDATE_DELAY = 300000L;

    public static final long WARM_UP_DELAY =  7200000L;
    public static final long LAST_SESSION_VALID_INTERVAL = 300000L;
    public static final long VALID_DATA_INTERVAL = 300000L;
    public static final long MINIMUM_DATA_INTERVAL = 5000L;
    public static final long CALIBRATION_INPUT_DELAY = 1800000L;
    public static final long MINUTE_5 = 300000L;

    public static final String PREF_CALIBRATION_TMP1 = "pref_calibration_tmp1";
    public static final String PREF_CALIBRATION_SIGNAL1 = "pref_calibration_signal1";
    public static final String PREF_CALIBRATION_TMP2 = "pref_calibration_tmp2";

    public static final String PREF_CALIBRATION_START_TIME = "pref_calibration_start_time";

    public static final String PREF_CALIBRATION_1 = "pref_calibration_1";
    public static final String PREF_CALIBRATION_2 = "pref_calibration_2";

    public static final String EXTRA_STEP = "extra_step";
    public static final String EXTRA_STEP_STRING = "extra_step_string";

    public static final boolean MODE_IS_MEDICAL = true;

    public static final String PREF_CURRENT_PATIENT_NAME = "pref_current_patient_name";
    public static final String PREF_CURRENT_PATIENT_NUMBER = "pref_current_patient_number";

    public static final String PREF_CURRENT_ADMIN_ID = "pref_current_admin_id";

    public static final int ADMIN_MIN_PASSWORD_LENGTH  = 5;
    public static final float MIN_BATTERY_LEVEL = 0.1f;

    public static final long SOUND_VIBRATE_DURATION = 15000L;

    public static final long ONE_MINUTE = 60000L;

    public static final int GRAPH_GLUCOSE_MIN = 0;
    public static final int GRAPH_GLUCOSE_MAX = 200;
    public static  int GRAPH_CURRENT_MIN = -20;
    public static  int GRAPH_CURRENT_MAX = 100;
}
