package kr.co.uxn.agms.data.model;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LoggedInUserTest {
    private static final String FAKE_STRING = "HELLO_WORLD";
    private static final String FAKE_STRING_DISPLAY = "display";

    @Test
    public void testGetUserId() {
        LoggedInUser user = new LoggedInUser(FAKE_STRING,FAKE_STRING_DISPLAY);
        assertEquals(user.getUserId(), FAKE_STRING);

    }

    @Test
    public void testGetDisplayName() {
        LoggedInUser user = new LoggedInUser(FAKE_STRING,FAKE_STRING_DISPLAY);
        assertEquals(user.getDisplayName(),FAKE_STRING_DISPLAY );
    }
}
