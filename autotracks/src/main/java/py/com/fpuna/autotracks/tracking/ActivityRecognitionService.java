package py.com.fpuna.autotracks.tracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationClient;

import py.com.fpuna.autotracks.Constants;

import static py.com.fpuna.autotracks.tracking.ActivityRecognitionController.LONG_INTERVAL;
import static py.com.fpuna.autotracks.tracking.ActivityRecognitionController.SHORT_INTERVAL;

public class ActivityRecognitionService extends IntentService implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String KEY_WAS_MOVING = "was_moving";

    private SharedPreferences mPreferences;
    private LocationClient mLocationClient;
    private LocationController mLocationController;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private ActivityRecognitionController mActivityRecognitionController;

    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mLocationClient = new LocationClient(context, this, this);
        mLocationController = new LocationController(context, mLocationClient);
        mActivityRecognitionClient = new ActivityRecognitionClient(context, this, this);
        mActivityRecognitionController = new ActivityRecognitionController(context, mActivityRecognitionClient);

        mLocationClient.connect();
        mActivityRecognitionClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.disconnect();
        mActivityRecognitionClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (isActivityRecognitionUpdatesStarted()) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                boolean moving = isMoving(result);
                setMoving(moving);
                if (moving) {
                    if (!isLocationUpdatesStarted()) {
                        mLocationController.startLocationUpdates();
                        mActivityRecognitionController.stopActivityRecognitionUpdates();
                        mActivityRecognitionController.startActivityRecognitionUpdates(LONG_INTERVAL);
                    }
                } else {
                    if (isLocationUpdatesStarted()) {
                        mLocationController.stopLocationUpdates();
                        mActivityRecognitionController.stopActivityRecognitionUpdates();
                        mActivityRecognitionController.startActivityRecognitionUpdates(SHORT_INTERVAL);
                    }
                }
            }
        }
    }

    private boolean isMoving(ActivityRecognitionResult result) {
        int mostProbableActivity = result.getMostProbableActivity().getType();
        switch (mostProbableActivity) {
            case DetectedActivity.ON_BICYCLE:
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.STILL:
                return false;
            case DetectedActivity.IN_VEHICLE:
                return true;
            default:
                return wasMoving();
        }
    }

    private boolean wasMoving() {
        return mPreferences.getBoolean(KEY_WAS_MOVING, false);
    }

    private void setMoving(boolean moving) {
        mPreferences.edit().putBoolean(KEY_WAS_MOVING, moving).commit();
    }

    public boolean isLocationUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_LOCATION_UPDATES_STARTED, false);
    }

    private boolean isActivityRecognitionUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false);
    }

}
