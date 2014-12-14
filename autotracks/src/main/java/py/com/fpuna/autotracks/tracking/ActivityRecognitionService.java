package py.com.fpuna.autotracks.tracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import py.com.fpuna.autotracks.Constants;

/**
 * Servicio que se encarga de manejar las actividades reconocidas.
 */
public class ActivityRecognitionService extends IntentService  {

    private static final String KEY_WAS_MOVING = "was_moving";
    private static final String KEY_DETECTION_TIME = "detection_time";

    private static final String DEFAULT_TOLERANCE = "10"; // 10 minutos

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

    /**
     * Detecta si el dispositivo se encuentra en un vehículo en movimiento o no
     * @param result resultado de la detección de actividad
     * @return  <code>true</code> si se encuentra en un vehículo;
     *          <code>false</code> en caso contrario.
     */
    private boolean isMoving(ActivityRecognitionResult result) {
        boolean retorno = false;
        boolean isOnFoot = false;
        long currentTime = System.currentTimeMillis();

        DetectedActivity mostProbableActivity = result.getMostProbableActivity();
        switch (mostProbableActivity.getType()) {
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.RUNNING:
            case DetectedActivity.WALKING:
                isOnFoot = mostProbableActivity.getConfidence() > 80;
            case DetectedActivity.ON_BICYCLE:
            case DetectedActivity.STILL:
                break;
            case DetectedActivity.IN_VEHICLE:
                boolean isMoving = mostProbableActivity.getConfidence() > 80;
                if (isMoving) {
                    setDetectionTimeMillis(currentTime);
                    retorno = true;
                }
                break;
            default:
                retorno = wasMoving();
        }

        if (wasMoving() && !retorno && !isOnFoot) {
            long lastDetectionTime = getDetectionTimeMillis();
            long elapsedTime = currentTime - lastDetectionTime;
            long tolerance = getToleranceMillis();
            if (elapsedTime < tolerance) {
                retorno = true;
            }
        }

        return retorno;
    }

    /**
     * Retorna el momento de la última detección de movimiento en vehículo
     * @return tiempo en milisegundos
     */
    private long getDetectionTimeMillis() {
        return mPreferences.getLong(KEY_DETECTION_TIME, -1);
    }

    /**
     * Setea el momento de detección de movimiento en vehículo
     * @param time tiempo en milisegundos
     */
    private void setDetectionTimeMillis(long time) {
        mPreferences.edit().putLong(KEY_DETECTION_TIME, time).commit();
    }

    /**
     * Detecta  la última actividad conocida.
     * @return  <code>true</code> si la última actividad conocida es en vehículo;
     *          <code>false</code> en otro caso.
     */
    private boolean wasMoving() {
        return mPreferences.getBoolean(KEY_WAS_MOVING, false);
    }

    /**
     * Setea la última actividad conocida.
     * @param moving    <code>true</code> si la última actividad conocida es en vehículo;
     *                  <code>false</code> en otro caso.
     */
    private void setMoving(boolean moving) {
        mPreferences.edit().putBoolean(KEY_WAS_MOVING, moving).commit();
    }

    /**
     * Detecta si las actualizaciones de ubicación están iniciadas
     * @return      <code>true</code> si el servicio de locaciones está activado;
     *              <code>false</code> en caso contrario.
     */
    public boolean isLocationUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_LOCATION_UPDATES_STARTED, false);
    }

    /**
     * Detecta si el reconocimiento de actividad está iniciado.
     * @return      <code>true</code> si el servicio de reconocimiento de actividad está activado;
     *              <code>false</code> en caso contrario.
     */
    private boolean isActivityRecognitionUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false);
    }

    /**
     * Retorna la tolerancia para el reconocimiento de actividad.
     * @return  tolreancia en milisegundos.
     */
    public long getToleranceMillis() {
        String toleranceInMinutes = mPreferences.getString(Constants.KEY_RECOGNITION_TOLERANCE, DEFAULT_TOLERANCE);
        return Long.valueOf(toleranceInMinutes) * 60 * 1000;
    }

}
