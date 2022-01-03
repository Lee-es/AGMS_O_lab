package kr.co.uxn.agms.ui.connect;

import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import kr.co.uxn.agms.R;
import kr.co.uxn.agms.SplashActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class WarmupActivityTest extends TestCase {
    @Rule
    public ActivityTestRule<WarmupActivity> activityRule
            = new ActivityTestRule<>(WarmupActivity.class);


    @Test
    public void testSkip(){

        onView(withId(R.id.buttoSkp)).perform(click());
    }

}