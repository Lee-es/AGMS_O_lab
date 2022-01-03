package kr.co.uxn.agms.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ServiceTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.concurrent.TimeoutException;

import kr.co.uxn.agms.data.room.AdminData;
import kr.co.uxn.agms.data.room.GlucoseData;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.data.room.SensorRepositoryTest;
import kr.co.uxn.agms.util.HexString;
import kr.co.uxn.agms.util.PasswordUtil;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class BluetoothServiceBindTest {
    @Rule
    public final ServiceTestRule serviceRule = new ServiceTestRule();
    private Context context = ApplicationProvider.getApplicationContext();

    @Before
    public void initFirst(){
        Random random = new Random(System.currentTimeMillis());
        SensorRepository repository = new SensorRepository(context);
        repository.createPatient(SensorRepositoryTest.TEST_PATIENT_ID, SensorRepositoryTest.TEST_PATIENT_NUMBER);
        GlucoseData glucoseData = new GlucoseData(0, SensorRepositoryTest.TEST_MAC_ADDRESS, System.currentTimeMillis(),random.nextInt(1000)+random.nextFloat(),0,SensorRepositoryTest.TEST_PATIENT_ID,SensorRepositoryTest.TEST_PATIENT_NUMBER,random.nextFloat());
        repository.insertGluecoseData(glucoseData);
        AdminData adminData = new AdminData(0,SensorRepositoryTest.TEST_ADMIN_ID, PasswordUtil.getEncrypt(SensorRepositoryTest.TEST_ADMIN_PASSWORD) ,SensorRepositoryTest.TEST_ADMIN_PASSWORD_HINT, System.currentTimeMillis(), System.currentTimeMillis(),System.currentTimeMillis());
        repository.createAdminData(adminData);
        BluetoothUtil.setRequestingLocationUpdates(context,SensorRepositoryTest.TEST_MAC_ADDRESS);
    }

    @Test
    public void testWithBoundService() throws TimeoutException {
        // Create the service Intent.
        Intent serviceIntent =
                new Intent(ApplicationProvider.getApplicationContext(),
                        BluetoothService.class);


        // Bind the service and grab a reference to the binder.
        IBinder binder = serviceRule.bindService(serviceIntent);

        // Get the reference to the service, or you can call
        // public methods on the binder directly.
        BluetoothService service =
                ((BluetoothService.LocalBinder) binder).getService();


        service.getCurrent();
        service.setPatient(SensorRepositoryTest.TEST_PATIENT_ID,SensorRepositoryTest.TEST_PATIENT_NUMBER);


        service.checkCurrentState();


        service.getConnectedAddress();

        service.isDeviceConnected();
        service.getPairingDeviceStatus();


        service.getBattery();
        service.getRSSI();

        service.isBluetoothEnabled();
        service.checkCurrentState();
        service.serviceIsRunningInForeground(context);

    }

    @Test
    public void testCreateNotification() throws TimeoutException {
        // Create the service Intent.
        Intent serviceIntent =
                new Intent(ApplicationProvider.getApplicationContext(),
                        BluetoothService.class);

        serviceIntent.setAction(BluetoothService.ACTION_STOP_SERVICE);
        // Bind the service and grab a reference to the binder.
        IBinder binder = serviceRule.bindService(serviceIntent);

        // Get the reference to the service, or you can call
        // public methods on the binder directly.
        BluetoothService service =
                ((BluetoothService.LocalBinder) binder).getService();
        service.createConnectNotification();
        service.getPairingDeviceStatus();
        service.createBatteryNotification();
        service.createCalibrationNotification();
        service.createChangeAdminPasswordNotification();
        service.createChangeSensorNotification();
        service.createDisconnectAlarmNotification();
        service.createWarmupNotification();
    }


}
