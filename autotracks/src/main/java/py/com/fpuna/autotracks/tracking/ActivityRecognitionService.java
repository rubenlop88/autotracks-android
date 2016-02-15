package py.com.fpuna.autotracks.tracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import py.com.fpuna.autotracks.util.PreferenceUtils;

import static com.google.android.gms.location.DetectedActivity.IN_VEHICLE;
import static com.google.android.gms.location.DetectedActivity.ON_FOOT;
import static com.google.android.gms.location.DetectedActivity.RUNNING;
import static com.google.android.gms.location.DetectedActivity.STILL;
import static com.google.android.gms.location.DetectedActivity.TILTING;
import static com.google.android.gms.location.DetectedActivity.WALKING;

/**
 * Servicio que se encarga de manejar las actividades reconocidas.
 */
public class ActivityRecognitionService extends IntentService {

    private PreferenceUtils mPreferenceUtils;
    private LocationUpdatesController mLocationUpdatesController;

    public ActivityRecognitionService() {
        super("ActivityRecognitionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        mPreferenceUtils = new PreferenceUtils(context);
        mLocationUpdatesController = new LocationUpdatesController(context);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mPreferenceUtils.isActivityUpdatesStarted()) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                boolean moving = isMoving(result);
                mPreferenceUtils.setMoving(moving);
                if (moving) {
                    if (!mPreferenceUtils.isLocationUpdatesStarted()) {
                        mLocationUpdatesController.startLocationUpdates();
                    }
                } else {
                    if (mPreferenceUtils.isLocationUpdatesStarted()) {
                        mLocationUpdatesController.stopLocationUpdates();
                    }
                }
            }
        }
    }

    /**
     * Detecta si el dispositivo se encuentra en un vehículo en movimiento o no
     *
     * @param result resultado de la detección de actividad
     * @return <code>true</code> si se encuentra en un vehículo.
     */
    private boolean isMoving(ActivityRecognitionResult result) {
        long currentTime = System.currentTimeMillis();

        DetectedActivity mostProbableActivity = result.getMostProbableActivity();
        int confidence = mostProbableActivity.getConfidence();
        int type = mostProbableActivity.getType();

        if ((type == ON_FOOT || type == RUNNING || type == WALKING) && confidence > 80) {
            return false;
        }

        if (type == IN_VEHICLE && confidence > 80) {
            mPreferenceUtils.setDetectionTimeMillis(currentTime);
            return true;
        }

        if (mPreferenceUtils.wasMoving()) {
            long tolerance = mPreferenceUtils.getActivityRecognitionToleranceMillis();
            long lastDetectionTime = mPreferenceUtils.getDetectionTimeMillis();
            long elapsedTime = currentTime - lastDetectionTime;
            return elapsedTime <= tolerance;
        }

        return false;
    }

}
