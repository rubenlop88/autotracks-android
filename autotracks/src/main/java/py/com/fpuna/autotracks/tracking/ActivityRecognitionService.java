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

public class ActivityRecognitionService extends IntentService implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String KEY_WAS_MOVING = "was_moving";
    private static final String KEY_DETECTION_TIME = "detection_time";
    public static final int MIN_ELAPSED_TIME_MILLIS = 10 * 60 * 1000; // 10 minutos

    private SharedPreferences mPreferences;
    private LocationClient mLocationClient;
    private LocationController mLocationController;
    private ActivityRecognitionClient mActivityRecognitionClient;

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
                    }
                } else {
                    if (isLocationUpdatesStarted()) {
                        mLocationController.stopLocationUpdates();
                    }
                }
            }
        }
    }

    private boolean isMoving(ActivityRecognitionResult result) {
        long currentTime = System.currentTimeMillis();
        if (wasMoving()) {
            long lastDetectionTime = getDetectionTimeMillis();
            long elapsedTime = currentTime - lastDetectionTime;
            if (elapsedTime < MIN_ELAPSED_TIME_MILLIS) {
                return true;
            }
        }
        int mostProbableActivity = result.getMostProbableActivity().getType();
        switch (mostProbableActivity) {
            case DetectedActivity.ON_BICYCLE:
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.STILL:
                return false;
            case DetectedActivity.IN_VEHICLE:
                setDetectionTimeMillis(currentTime);
                return true;
            default:
                return wasMoving();
        }
    }

    private long getDetectionTimeMillis() {
        return mPreferences.getLong(KEY_DETECTION_TIME, -1);
    }

    private void setDetectionTimeMillis(long time) {
        mPreferences.edit().putLong(KEY_DETECTION_TIME, time).commit();
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
