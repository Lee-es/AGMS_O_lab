package kr.co.uxn.agms;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import kr.co.uxn.agms.data.room.AdminData;
import kr.co.uxn.agms.data.room.GlucoseData;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.data.room.SensorRepositoryTest;
import kr.co.uxn.agms.service.BluetoothUtil;
import kr.co.uxn.agms.util.PasswordUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest extends TestCase {
    private Context context = ApplicationProvider.getApplicationContext();

    @Rule
    public ActivityTestRule<MainActivity> activityRule
            = new ActivityTestRule<>(MainActivity.class);
    @Before
    public void beforeSet(){
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
    public void test(){
        onView(withId(R.id.navigation_home)).perform(click());


        onView(withId(R.id.navigation_event)).perform(click());

        onView(withId(R.id.username)).perform(click());

        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.patient_number))
                .perform(typeText("event 1"), closeSoftKeyboard());
        onView(withId(R.id.event))
                .perform(typeText("10"), closeSoftKeyboard());

        onView(withId(R.id.create)).perform(click());


    }


    @Test
    public void settingTest1(){
        onView(withId(R.id.navigation_setting)).perform(click());

        onView(withId(R.id.admin_id))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_ID), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_PASSWORD), closeSoftKeyboard());


        onView(withId(R.id.buttonContinue)).perform(click());
        onView(withId(R.id.button_new_user)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());
    }
    @Test
    public void settingTest2(){
        onView(withId(R.id.navigation_setting)).perform(click());

        onView(withId(R.id.admin_id))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_ID), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_PASSWORD), closeSoftKeyboard());


        onView(withId(R.id.buttonContinue)).perform(click());
        onView(withId(R.id.button_new_device)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());
    }
    @Test
    public void settingTest3(){
        onView(withId(R.id.navigation_setting)).perform(click());

        onView(withId(R.id.admin_id))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_ID), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_PASSWORD), closeSoftKeyboard());


        onView(withId(R.id.buttonContinue)).perform(click());
        onView(withId(R.id.button_new_sensor)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());
    }
    @Test
    public void settingTest4(){
        onView(withId(R.id.navigation_setting)).perform(click());

        onView(withId(R.id.admin_id))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_ID), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_PASSWORD), closeSoftKeyboard());


        onView(withId(R.id.buttonContinue)).perform(click());
        onView(withId(R.id.button_save)).perform(click());

    }
    @Test
    public void settingTest5(){
        onView(withId(R.id.navigation_setting)).perform(click());

        onView(withId(R.id.admin_id))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_ID), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_PASSWORD), closeSoftKeyboard());


        onView(withId(R.id.buttonContinue)).perform(click());
        onView(withId(R.id.button_cancel)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void testOff(){
        activityRule.getActivity().doOff();
    }

}