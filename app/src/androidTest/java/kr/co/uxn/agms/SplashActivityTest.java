package kr.co.uxn.agms;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import kr.co.uxn.agms.ui.admin.ChangePasswordActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SplashActivityTest extends TestCase {

    @Rule
    public ActivityTestRule<SplashActivity> activityRule
            = new ActivityTestRule<>(SplashActivity.class);

    @Test
    public void test(){
        activityRule.getActivity().checkCurrentState();
    }
}