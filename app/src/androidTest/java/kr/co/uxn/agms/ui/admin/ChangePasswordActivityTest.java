package kr.co.uxn.agms.ui.admin;

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

import kr.co.uxn.agms.R;
import kr.co.uxn.agms.data.room.AdminData;
import kr.co.uxn.agms.data.room.SensorRepository;
import kr.co.uxn.agms.util.PasswordUtil;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ChangePasswordActivityTest extends TestCase {
    private String testId;
    private String testPassword;
    private String testPasswordConfirm;
    private String testPasswordHint;

    @Rule
    public ActivityTestRule<ChangePasswordActivity> activityRule
            = new ActivityTestRule<>(ChangePasswordActivity.class);

    private Context context = ApplicationProvider.getApplicationContext();

    @Before
    public void testCreateAdmin(){
        SensorRepository repository = new SensorRepository(context);
        testId = "ui_test_id";
        testPassword = "ui_test_password";
        testPasswordConfirm = "ui_test_password";
        testPasswordHint = "ui_test_password_hint";
        AdminData adminData = new AdminData(0,testId, PasswordUtil.getEncrypt("test_password") ,testPasswordHint, System.currentTimeMillis(), System.currentTimeMillis(),System.currentTimeMillis());
        repository.createAdminData(adminData);
    }
    @Test
    public void test1(){
        onView(withId(R.id.buttonContinue)).perform(click());
    }
    @Test
    public void test2(){
        onView(withId(R.id.password))
                .perform(typeText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.buttonContinue)).perform(click());
    }
    @Test
    public void test3(){
        onView(withId(R.id.password))
                .perform(typeText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.password_check))
                .perform(typeText(testPasswordConfirm), closeSoftKeyboard());
        onView(withId(R.id.buttonContinue)).perform(click());
    }
    @Test
    public void test4(){
        onView(withId(R.id.password))
                .perform(typeText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.password_check))
                .perform(typeText("sdff"), closeSoftKeyboard());
        onView(withId(R.id.buttonContinue)).perform(click());
    }
    @Test
    public void test5(){
        onView(withId(R.id.password))
                .perform(typeText("!23"), closeSoftKeyboard());
        onView(withId(R.id.password_check))
                .perform(typeText("sdff"), closeSoftKeyboard());
        onView(withId(R.id.buttonContinue)).perform(click());
    }

    @Test
    public void testValidate(){

        onView(withId(R.id.password))
                .perform(typeText(testPassword), closeSoftKeyboard());

        onView(withId(R.id.password_check))
                .perform(typeText(testPasswordConfirm), closeSoftKeyboard());
        onView(withId(R.id.password_hint))
                .perform(typeText(testPasswordHint), closeSoftKeyboard());
        onView(withId(R.id.buttonContinue)).perform(click());

    }

}