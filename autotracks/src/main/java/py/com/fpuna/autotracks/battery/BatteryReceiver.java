package py.com.fpuna.autotracks.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import py.com.fpuna.autotracks.Constants;
import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.LocationController;

public class BatteryReceiver extends BroadcastReceiver {
    private static String TAG = BatteryReceiver.class.getSimpleName();
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
            if (mActivityRecognitionController.isActivityRecognitionUpdatesStarted()) {
                mActivityRecognitionController.stopActivityRecognitionUpdates();
            }
            if (mLocationController.isLocationUpdatesStarted()) {
                mLocationController.stopLocationUpdates();
            }
            mPreferences.edit().putBoolean(Constants.KEY_BATTERY_LEVEL_LOW, true).apply();
        } else if (Intent.ACTION_BATTERY_OKAY.equals(intent.getAction())) {
            if (!mActivityRecognitionController.isActivityRecognitionUpdatesStarted()) {
                mActivityRecognitionController.startActivityRecognitionUpdates();
            }
            if (!mLocationController.isLocationUpdatesStarted()) {
                mLocationController.startLocationUpdates();
            }
            mPreferences.edit().putBoolean(Constants.KEY_BATTERY_LEVEL_LOW, false).apply();
        }

    }
}
