package py.com.fpuna.autotracks.tracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import py.com.fpuna.autotracks.Constants;

public class ActivityRecognitionService extends IntentService  {

    private static final String KEY_WAS_MOVING = "was_moving";
    private static final String KEY_DETECTION_TIME = "detection_time";

    private static final String DEFAULT_TOLERANCE = "5"; // 5 minutos

    private SharedPreferences mPreferences;
    private LocationController mLocationController;

    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        mPreferences = context.getSharedPreferences("py.com.fpuna.autotracks_preferences", Context.MODE_PRIVATE);
        mLocationController = new LocationController(context);
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
            long tolerance = getToleranceMillis();
            if (elapsedTime < tolerance) {
                return true;
            }
        }
        DetectedActivity mostProbableActivity = result.getMostProbableActivity();
        switch (mostProbableActivity.getType()) {
            case DetectedActivity.ON_BICYCLE:
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.STILL:
            case DetectedActivity.RUNNING:
            case DetectedActivity.WALKING:
                return false;
            case DetectedActivity.IN_VEHICLE:
                boolean isMoving = mostProbableActivity.getConfidence() > 80;
                if (isMoving) {
                    setDetectionTimeMillis(currentTime);
                    return true;
                } else {
                    return false;
                }
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

    public long getToleranceMillis() {
        String toleranceInMinutes = mPreferences.getString(Constants.KEY_RECOGNITION_TOLERANCE, DEFAULT_TOLERANCE);
        return Long.valueOf(toleranceInMinutes) * 60 * 1000;
    }

}
