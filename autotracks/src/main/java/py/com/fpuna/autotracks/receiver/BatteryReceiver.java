package py.com.fpuna.autotracks.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.LocationUpdatesController;
import py.com.fpuna.autotracks.util.PreferenceUtils;

public class BatteryReceiver extends BroadcastReceiver {

    private ActivityRecognitionController mActivityRecognitionController;
    private LocationUpdatesController mLocationUpdatesController;
    private PreferenceUtils mPreferenceUtils;

    @Override
    public void onReceive(Context context, Intent intent) {
        mActivityRecognitionController = new ActivityRecognitionController(context);
        mLocationUpdatesController = new LocationUpdatesController(context);
        mPreferenceUtils = new PreferenceUtils(context);
        if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            mActivityRecognitionController.stopActivityRecognitionUpdates();
            mLocationUpdatesController.stopLocationUpdates();
            mPreferenceUtils.setBatteryLevelOk(false);
        } else if (Intent.ACTION_BATTERY_OKAY.equals(intent.getAction())) {
            mActivityRecognitionController.startActivityRecognitionUpdates();
            mLocationUpdatesController.startLocationUpdates();
            mPreferenceUtils.setBatteryLevelOk(true);
        }
    }

}
