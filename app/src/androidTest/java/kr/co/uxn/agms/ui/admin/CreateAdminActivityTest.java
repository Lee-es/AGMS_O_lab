package kr.co.uxn.agms.ui.admin;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import kr.co.uxn.agms.R;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateAdminActivityTest extends TestCase {
    private String testId;
    private String testPassword;
    private String testPasswordConfirm;
    private String testPasswordHint;

    @Rule
    public ActivityTestRule<CreateAdminActivity> activityRule
            = new ActivityTestRule<>(CreateAdminActivity.class);

    @Before
    public void initValidString() {
        // Specify a valid string.
        testId = "ui_test_id";
        testPassword = "ui_test_password";
        testPasswordConfirm = "ui_test_password";
        testPasswordHint = "ui_test_password_hint";
    }

    @Test
    public void successTest(){

        onView(withId(R.id.admin_id))
                .perform(typeText(testId), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText(testPassword), closeSoftKeyboard());

        onView(withId(R.id.password_check))
                .perform(typeText(testPasswordConfirm), closeSoftKeyboard());
        onView(withId(R.id.password_hint))
                .perform(typeText(testPasswordHint), closeSoftKeyboard());
        onView(withId(R.id.buttonContinue)).perform(click());

    }
    @Test
    public void test1(){

        onView(withId(R.id.admin_id))
                .perform(typeText(testId), closeSoftKeyboard());
        onView(withId(R.id.password))
                .perform(typeText(testPassword), closeSoftKeyboard());

        onView(withId(R.id.password_check))
                .perform(typeText(testPasswordConfirm), closeSoftKeyboard());
        onView(withId(R.id.password_hint))
                .perform(typeText(testPasswordHint), closeSoftKeyboard());
        onView(withId(R.id.buttonContinue)).perform(click());
        activityRule.getActivity().createAdmin();
    }
}