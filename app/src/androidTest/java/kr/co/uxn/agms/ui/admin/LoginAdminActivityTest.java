package kr.co.uxn.agms.ui.admin;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.data.room.SensorRepositoryTest;
import kr.co.uxn.agms.ui.patient.CreatePatientActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginAdminActivityTest extends TestCase {
    @Rule
    public ActivityTestRule<LoginAdminActivity> activityRule
            = new ActivityTestRule<>(LoginAdminActivity.class);

    @Test
    public void testLogin(){
        onView(withId(R.id.admin_id))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_ID), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText(SensorRepositoryTest.TEST_ADMIN_PASSWORD), closeSoftKeyboard());


        onView(withId(R.id.buttonContinue)).perform(click());
    }
    @Test
    public void testChangePassword(){
        onView(withId(R.id.changePasswordButton)).perform(click());
    }
}