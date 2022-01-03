package kr.co.uxn.agms.data.model;

import junit.framework.TestCase;


import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;


public class LoggedInUserTest extends TestCase {
    private static final String FAKE_STRING = "HELLO_WORLD";
    private static final String FAKE_STRING_DISPLAY = "display";

    @Test
    public void testGetUserId() {
        LoggedInUser user = new LoggedInUser(FAKE_STRING,FAKE_STRING_DISPLAY);
        assertThat(user.getUserId()).isEqualTo(FAKE_STRING);
    }

    @Test
    public void testGetDisplayName() {
        LoggedInUser user = new LoggedInUser(FAKE_STRING,FAKE_STRING_DISPLAY);
        assertThat(user.getDisplayName()).isEqualTo(FAKE_STRING_DISPLAY);
    }
}