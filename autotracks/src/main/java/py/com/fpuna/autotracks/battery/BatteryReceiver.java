package py.com.fpuna.autotracks.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import py.com.fpuna.autotracks.Constants;
import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.LocationController;

public class BatteryReceiver extends BroadcastReceiver {
    private ActivityRecognitionController mActivityRecognitionController;
    private LocationController mLocationController;
    private SharedPreferences mPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        mActivityRecognitionController = new ActivityRecognitionController(context);
        mLocationController = new LocationController(context);
        mPreferences = context.getSharedPreferences("py.com.fpuna.autotracks_preferences",
                Context.MODE_PRIVATE);

        if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            mActivityRecognitionController.stopActivityRecognitionUpdates();
            mLocationController.stopLocationUpdates();
            mPreferences.edit().putBoolean(Constants.KEY_BATTERY_LEVEL_LOW, true).apply();
        } else if (Intent.ACTION_BATTERY_OKAY.equals(intent.getAction())) {
            mActivityRecognitionController.startActivityRecognitionUpdates();
            mLocationController.startLocationUpdates();
            mPreferences.edit().putBoolean(Constants.KEY_BATTERY_LEVEL_LOW, false).apply();
        }
    }
}
