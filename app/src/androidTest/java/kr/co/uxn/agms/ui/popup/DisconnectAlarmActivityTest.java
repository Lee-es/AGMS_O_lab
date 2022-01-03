package kr.co.uxn.agms.ui.popup;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import junit.framework.TestCase;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import kr.co.uxn.agms.ui.admin.CreateAdminActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DisconnectAlarmActivityTest extends TestCase {
    @Rule
    public ActivityTestRule<DisconnectAlarmActivity> activityRule
            = new ActivityTestRule<>(DisconnectAlarmActivity.class);

    @Test
    public void test(){

        activityRule.getActivity().removeAlert();
    }
}