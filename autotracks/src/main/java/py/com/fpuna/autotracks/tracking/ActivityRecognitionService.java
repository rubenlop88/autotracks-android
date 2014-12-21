package py.com.fpuna.autotracks.tracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import py.com.fpuna.autotracks.Constants;

import static com.google.android.gms.location.DetectedActivity.IN_VEHICLE;
import static com.google.android.gms.location.DetectedActivity.ON_FOOT;
import static com.google.android.gms.location.DetectedActivity.RUNNING;
import static com.google.android.gms.location.DetectedActivity.WALKING;

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
        long currentTime = System.currentTimeMillis();

        // obtenemos los datos de la actividad del usuario
        DetectedActivity mostProbableActivity = result.getMostProbableActivity();
        int confidence = mostProbableActivity.getConfidence();
        int type = mostProbableActivity.getType();

        // si el usuario esta a pie detenemos inmediatamente el rastreo
        if ((type == ON_FOOT || type == RUNNING || type == WALKING) && confidence > 80) {
            return false;
        }

        // si el usuario esta en vehiculo continuamos con el rastreo
        if (type == IN_VEHICLE && confidence > 80) {
            setDetectionTimeMillis(currentTime);
            return true;
        }

        // si el usuario ya estaba en movimiento comprobamos que haya transcurrido
        // un tiempo de tolerancia predefinido antes de detener el rastreo
        if (wasMoving()) {
            long lastDetectionTime = getDetectionTimeMillis();
            long elapsedTime = currentTime - lastDetectionTime;
            long tolerance = getToleranceMillis();
            return elapsedTime < tolerance;
        }

        return false;
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
