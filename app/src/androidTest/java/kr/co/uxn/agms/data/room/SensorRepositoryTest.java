package kr.co.uxn.agms.data.room;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.Random;

import kr.co.uxn.agms.service.BluetoothService;
import kr.co.uxn.agms.util.PasswordUtil;

public class SensorRepositoryTest extends TestCase {
    private Context context = ApplicationProvider.getApplicationContext();
    public static final String TEST_MAC_ADDRESS = "5C:02:72:68:03:C3";
    public static final String TEST_ADMIN_ID = "test_admin_id";
    public static final String TEST_ADMIN_PASSWORD = "test_admin_password";
    public static final String TEST_ADMIN_NEW_PASSWORD = "test_admin_password";

    public static final String TEST_ADMIN_PASSWORD_CHECK = TEST_ADMIN_PASSWORD;
    public static final String TEST_ADMIN_PASSWORD_HINT = "test_admin_password_hint";
    public static final String TEST_PATIENT_ID = "patient1";
    public static final long TEST_PATIENT_NUMBER = 1234;
    public static final String TEST_SENSOR_DATA = "473A302E343437302020423A312E33330D0A";


    @Test
    public void test(){
        byte[] testSensorData = null;
        Random random = new Random(System.currentTimeMillis());
        SensorRepository repository = new SensorRepository(context);

        AdminData adminData = new AdminData(0,TEST_ADMIN_ID, PasswordUtil.getEncrypt(TEST_ADMIN_PASSWORD) ,TEST_ADMIN_PASSWORD_HINT, System.currentTimeMillis(), System.currentTimeMillis(),System.currentTimeMillis());
        repository.createAdminData(adminData);

        repository.createPatient(TEST_PATIENT_ID, TEST_PATIENT_NUMBER);

        SensorLog sensorLog = new SensorLog(0,TEST_MAC_ADDRESS, BluetoothService.STATE_CONNECTED,System.currentTimeMillis());
        repository.insertSensorLog(sensorLog);

        SensorData sensorData = new SensorData(0,TEST_MAC_ADDRESS, System.currentTimeMillis(), System.currentTimeMillis(),testSensorData,TEST_SENSOR_DATA,TEST_PATIENT_ID );
        repository.insertSensorData(sensorData);

        GlucoseData glucoseData = new GlucoseData(0,TEST_MAC_ADDRESS, System.currentTimeMillis(),random.nextInt(1000)+random.nextFloat(),0,TEST_PATIENT_ID,TEST_PATIENT_NUMBER,random.nextFloat());
        repository.insertGluecoseData(glucoseData);


        EventData eventData = new EventData(0, TEST_PATIENT_ID, TEST_PATIENT_NUMBER,System.currentTimeMillis(), "test event",0);
        repository.createEventData(eventData);

        AdminData dbAdminData = repository.getAdminData(TEST_ADMIN_ID);
        if(dbAdminData!=null){
            assertEquals(TEST_ADMIN_ID, dbAdminData.getAdminId());
            assertEquals(true,PasswordUtil.verify(TEST_ADMIN_PASSWORD,dbAdminData.getPassword()));
            assertEquals(TEST_ADMIN_PASSWORD_HINT, dbAdminData.getPasswordHint());
            adminData.setPassword(PasswordUtil.getEncrypt(TEST_ADMIN_PASSWORD));
            repository.updateAdminData(adminData);
        }

        PatientData dbPatientData = repository.getPatientData(TEST_PATIENT_NUMBER);
        if(dbPatientData!=null){
            assertEquals(TEST_PATIENT_ID,dbPatientData.getName());
        }




    }

}