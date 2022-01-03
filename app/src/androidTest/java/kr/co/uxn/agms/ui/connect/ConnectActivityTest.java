package kr.co.uxn.agms.ui.connect;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.SensorRepositoryTest;
import kr.co.uxn.agms.ui.admin.LoginAdminActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ConnectActivityTest extends TestCase {
    @Rule
    public ActivityTestRule<ConnectActivity> activityRule
            = new ActivityTestRule<>(ConnectActivity.class);

    @Test
    public void testConnect(){
        onView(withId(R.id.input))
                .perform(typeText(SensorRepositoryTest.TEST_MAC_ADDRESS), closeSoftKeyboard());
        onView(withId(R.id.connect)).perform(click());

    }

}