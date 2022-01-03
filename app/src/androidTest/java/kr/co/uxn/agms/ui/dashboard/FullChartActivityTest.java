package kr.co.uxn.agms.ui.dashboard;

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

import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.GlucoseData;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.data.room.SensorRepositoryTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class FullChartActivityTest extends TestCase {
    private Context context = ApplicationProvider.getApplicationContext();

    @Rule
    public ActivityTestRule<FullChartActivity> activityRule
            = new ActivityTestRule<>(FullChartActivity.class);

    @Before
    public void addTestDAta(){
        Random random = new Random(System.currentTimeMillis());
        SensorRepository repository = new SensorRepository(context);
        repository.createPatient(SensorRepositoryTest.TEST_PATIENT_ID, SensorRepositoryTest.TEST_PATIENT_NUMBER);
        GlucoseData glucoseData = new GlucoseData(0, SensorRepositoryTest.TEST_MAC_ADDRESS, System.currentTimeMillis(),random.nextInt(1000)+random.nextFloat(),0,SensorRepositoryTest.TEST_PATIENT_ID,SensorRepositoryTest.TEST_PATIENT_NUMBER,random.nextFloat());
        repository.insertGluecoseData(glucoseData);
    }

    @Test
    public void test(){
        onView(withId(R.id.month_average)).perform(click());
        onView(withId(R.id.day_average)).perform(click());
        onView(withId(R.id.week_average)).perform(click());

    }
}
