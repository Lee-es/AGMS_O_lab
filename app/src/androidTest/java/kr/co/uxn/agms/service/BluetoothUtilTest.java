package kr.co.uxn.agms.service;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.test.core.app.ApplicationProvider;

import junit.framework.TestCase;

import org.junit.Test;

public class BluetoothUtilTest extends TestCase {
    private Context context = ApplicationProvider.getApplicationContext();

    @Test
    public void test() {
        String testString = "test";
        BluetoothUtil.setRequestingLocationUpdates(context, testString);
        String result = BluetoothUtil.requestingLocationUpdates(context);
        assertEquals(testString, result);

    }


}