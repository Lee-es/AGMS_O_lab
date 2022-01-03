package kr.co.uxn.agms.service;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import junit.framework.TestCase;

import kr.co.uxn.agms.data.room.PatientData;
import kr.co.uxn.agms.data.room.SensorRepository;

public class BluetoothServiceTest extends TestCase {

    private Context context = ApplicationProvider.getApplicationContext();
    SensorRepository mRepository;
    private static final String TEST_ADDRESS = "test_address";
    private static final float TEST_CURRENT = 3.34f;
    private static final float TEST_BATTERY = 1.4f;
    private static final String TEST_PATIENT  = "patient";
    private static final long TEST_PATIENT_NUMBER = 1721;

    public void setUp() throws Exception {
        super.setUp();
        mRepository = new SensorRepository(context);
        mRepository.createPatient(TEST_PATIENT, TEST_PATIENT_NUMBER);
    }



    public void testOnCreate(){
        PatientData patientData = mRepository.getPatientData(TEST_PATIENT_NUMBER);
        assertNotNull(patientData);

    }
    public void testOnStartCommand() {

    }

    public void testSetCharacteristicNotification() {

    }
    public void testOnBind(){

    }
    public void testOnConfigurationChanged(){

    }
    public void testScanStartForConnect(){

    }
    public void testRemoveBluetoothUpdates(){

    }
    public void testRequestBluetoothUpdates(){

    }
    public void testWriteToDevice(){

    }
    public void testSetFlagChange(){

    }
    public void testOnUnbind(){

    }
    public void testOnDestroy(){

    }
    public void testInitialize(){

    }
    public void testDisconnect() {

    }
    public void testDisconnectAndReset() {

    }
    public void testClose() {

    }
}