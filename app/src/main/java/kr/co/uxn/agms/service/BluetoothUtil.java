package kr.co.uxn.agms.service;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

public class BluetoothUtil {
    public static final String KEY_REQUESTING_LOCATION_UPDATES = "ble_requesting_locaction_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static String requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_REQUESTING_LOCATION_UPDATES, null);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, String requestingLocationUpdates) {

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .commit();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */

}
