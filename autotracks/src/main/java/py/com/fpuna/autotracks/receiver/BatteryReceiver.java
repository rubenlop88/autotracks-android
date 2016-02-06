package py.com.fpuna.autotracks.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
<<<<<<< HEAD:autotracks/src/main/java/py/com/fpuna/autotracks/receiver/BatteryReceiver.java
=======
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
>>>>>>> 6b2f0c5f43300dafe62d3663cc58971153213c7a:autotracks/src/main/java/py/com/fpuna/autotracks/battery/BatteryReceiver.java

import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.LocationUpdatesController;
import py.com.fpuna.autotracks.util.PreferenceUtils;

public class BatteryReceiver extends BroadcastReceiver {
<<<<<<< HEAD:autotracks/src/main/java/py/com/fpuna/autotracks/receiver/BatteryReceiver.java

=======
    private static String TAG = BatteryReceiver.class.getSimpleName();
>>>>>>> 6b2f0c5f43300dafe62d3663cc58971153213c7a:autotracks/src/main/java/py/com/fpuna/autotracks/battery/BatteryReceiver.java
    private ActivityRecognitionController mActivityRecognitionController;
    private LocationUpdatesController mLocationUpdatesController;
    private PreferenceUtils mPreferenceUtils;

    @Override
    public void onReceive(Context context, Intent intent) {
        mActivityRecognitionController = new ActivityRecognitionController(context);
        mLocationUpdatesController = new LocationUpdatesController(context);
        mPreferenceUtils = new PreferenceUtils(context);
        if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
<<<<<<< HEAD:autotracks/src/main/java/py/com/fpuna/autotracks/receiver/BatteryReceiver.java
            mActivityRecognitionController.stopActivityRecognitionUpdates();
            mLocationUpdatesController.stopLocationUpdates();
            mPreferenceUtils.setBatteryLevelOk(false);
        } else if (Intent.ACTION_BATTERY_OKAY.equals(intent.getAction())) {
            mActivityRecognitionController.startActivityRecognitionUpdates();
            mLocationUpdatesController.startLocationUpdates();
            mPreferenceUtils.setBatteryLevelOk(true);
=======
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
>>>>>>> 6b2f0c5f43300dafe62d3663cc58971153213c7a:autotracks/src/main/java/py/com/fpuna/autotracks/battery/BatteryReceiver.java
        }

    }

}
