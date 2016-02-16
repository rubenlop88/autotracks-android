package py.com.fpuna.autotracks.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

public class PreferenceUtils {

    // datos configurables
    public static final String KEY_ACTIVITY_RECOGNITION_INTERVAL = "activity_recognition_interval";
    public static final String KEY_ACTIVITY_RECOGNITION_TOLERANCE = "activity_recognition_tolerance";
    public static final String KEY_LOCATION_UPDATES_INTERVAL = "location_updates_interval";

    // valores por defecto de los datos configurables
    private static final String DEFAULT_ACTIVITY_RECOGNITION_INTERVAL = "15"; // 15 segundos
    private static final String DEFAULT_ACTIVITY_RECOGNITION_TOLERANCE = "10"; // 10 minutos
    private static final String DEFAULT_LOCATION_UPDATES_INTREVAL = "60"; // 60 segundos

    private static final String KEY_WAS_MOVING = "was_moving";
    private static final String KEY_DETECTION_TIME = "detection_time";
    private static final String KEY_ACTIVITY_UPDATES_STARTED = "activity_updates_started";
    private static final String KEY_LOCATION_UPDATES_STARTED = "location_updates_started";
    private static final String KEY_CURRENT_TRACK_ID = "track_id";
    private static final String KEY_LAST_LOCATION_TRACK_ID = "key_last_location_track_id";
    private static final String KEY_LAST_LOCATION_LATITUDE = "key_last_location_latitude";
    private static final String KEY_LAST_LOCATION_LONGITUDE = "key_last_location_longitude";
    private static final String KEY_LAST_LOCATION_TIME = "key_las_location_time";
    private static final String KEY_LAST_ACTIVITY_TIME = "key_las_activity_time";

    private SharedPreferences mPreferences;

    public PreferenceUtils(Context context) {
        mPreferences = context.getSharedPreferences("py.com.fpuna.autotracks_preferences", Context.MODE_PRIVATE);
    }

    /**
     * Retorna el intervalo de muestreo del reconocimiento de actividad.
     *
     * @return intervalo en milisegundos
     */
    public long getActivityRecognitionIntervalMillis() {
        String intervalInSeconds = mPreferences.getString(KEY_ACTIVITY_RECOGNITION_INTERVAL, DEFAULT_ACTIVITY_RECOGNITION_INTERVAL);
        return Integer.valueOf(intervalInSeconds) * 1000;
    }

    /**
     * Retorna la tolerancia para el reconocimiento de actividad.
     *
     * @return tolreancia en milisegundos.
     */
    public long getActivityRecognitionToleranceMillis() {
        String toleranceInMinutes = mPreferences.getString(KEY_ACTIVITY_RECOGNITION_TOLERANCE, DEFAULT_ACTIVITY_RECOGNITION_TOLERANCE);
        return Long.valueOf(toleranceInMinutes) * 60 * 1000;
    }

    /**
     * Retorna el intervalo de muestreo de la toma de localizaciones.
     *
     * @return intervalo en milisegundos
     */
    public long getLocationUpdatesIntervalMillis() {
        String interval = mPreferences.getString(KEY_LOCATION_UPDATES_INTERVAL, DEFAULT_LOCATION_UPDATES_INTREVAL);
        return Long.valueOf(interval) * 1000;
    }

    /**
     * Detecta la última actividad conocida.
     *
     * @return <code>true</code> si la última actividad conocida es en vehículo;
     * <code>false</code> en otro caso.
     */
    public boolean wasMoving() {
        return mPreferences.getBoolean(KEY_WAS_MOVING, false);
    }

    /**
     * Setea la última actividad conocida.
     *
     * @param moving <code>true</code> si la última actividad conocida es en vehículo;
     *               <code>false</code> en otro caso.
     */
    public void setMoving(boolean moving) {
        mPreferences.edit().putBoolean(KEY_WAS_MOVING, moving).commit();
    }

    /**
     * Retorna el momento de la última detección de movimiento en vehículo
     *
     * @return tiempo en milisegundos
     */
    public long getDetectionTimeMillis() {
        return mPreferences.getLong(KEY_DETECTION_TIME, -1);
    }

    /**
     * Setea el momento de detección de movimiento en vehículo
     *
     * @param time tiempo en milisegundos
     */
    public void setDetectionTimeMillis(long time) {
        mPreferences.edit().putLong(KEY_DETECTION_TIME, time).commit();
    }

    /**
     * Detecta si el reconocimiento de actividad está iniciado.
     *
     * @return <code>true</code> si el servicio de reconocimiento de actividad está activado;
     * <code>false</code> en caso contrario.
     */
    public boolean isActivityUpdatesStarted() {
        return mPreferences.getBoolean(KEY_ACTIVITY_UPDATES_STARTED, false);
    }

    /**
     * Guarda el estado del servicio de reconocimiento de actividad.
     *
     * @param started <code>true</code> si ha sido iniciado.
     */
    public void setActivityUpdatesStarted(boolean started) {
        mPreferences.edit().putBoolean(KEY_ACTIVITY_UPDATES_STARTED, started).commit();
    }

    /**
     * Detecta si las actualizaciones de ubicación están iniciadas
     *
     * @return <code>true</code> si el servicio de locaciones está activado;
     * <code>false</code> en caso contrario.
     */
    public boolean isLocationUpdatesStarted() {
        return mPreferences.getBoolean(KEY_LOCATION_UPDATES_STARTED, false);
    }

    /**
     * Guarda el estado del servicio de toma de localizaciones.
     *
     * @param started <code>true</code> si ha sido iniciado.
     */
    public void setLocationUpdatesStarted(boolean started) {
        mPreferences.edit().putBoolean(KEY_LOCATION_UPDATES_STARTED, started).commit();
    }

    /**
     * Obtiene el identificador de la ruta actual del usuario o <code>null</code> si el usuario
     * no está en movimiento.
     *
     * @return Identificador de la ruta actual.
     */
    public String getCurrentTrackId() {
        return mPreferences.getString(KEY_CURRENT_TRACK_ID, null);
    }

    /**
     * Guarda el identificador de la ruta actual del usuario. Pasar como parametro <code>null</code>
     * para indicar que el usuario no esta en ninguna ruta.
     *
     * @param id identificaro de la Ruta
     */
    public void setCurrentTrackId(String id) {
        mPreferences.edit().putString(KEY_CURRENT_TRACK_ID, id).commit();
    }

    /**
     * Obtiene el identificador de la ruta a la que corresponde la ultima localizacion obtenida.
     *
     * @return identificador de la ruta de la ultima localizacion.
     */
    public String getLastTrackId() {
        return mPreferences.getString(KEY_LAST_LOCATION_TRACK_ID, null);
    }

    /**
     * Obtiene la longitud de la ultima localizacion obtenida.
     *
     * @return longitud.
     */
    public double getLastLongitude() {
        return Double.longBitsToDouble(mPreferences.getLong(KEY_LAST_LOCATION_LONGITUDE, -1));
    }

    /**
     * Obtiene la latitud de la ultima localizacion obtenida.
     *
     * @return latitud.
     */
    public double getLastLatitude() {
        return Double.longBitsToDouble(mPreferences.getLong(KEY_LAST_LOCATION_LATITUDE, -1));
    }

    /**
     * Obtiene el timestamp de la ultima localizacion obtenida.
     *
     * @return timestamp de la ultima localizacion
     */
    public long getLasTime() {
        return mPreferences.getLong(KEY_LAST_LOCATION_TIME, -1);
    }

    /**
     * Guarda los datos de la ultima localizacion obtenida.
     *
     * @param location Ultima localizacion obtenida
     * @param rutaId   Ruta a la que pertence la localizacion
     */
    public void saveLastLocation(Location location, String rutaId) {
        mPreferences.edit().putString(KEY_LAST_LOCATION_TRACK_ID, rutaId)
                .putLong(KEY_LAST_LOCATION_LATITUDE, Double.doubleToLongBits(location.getLatitude()))
                .putLong(KEY_LAST_LOCATION_LONGITUDE, Double.doubleToLongBits(location.getLongitude()))
                .putLong(KEY_LAST_LOCATION_TIME, location.getTime())
                .commit();
    }

    /**
     * Guarda el timestamp del ultimo reconocimiento de actividad
     */
    public void setLastActivityTime(long time) {
        mPreferences.edit().putLong(KEY_LAST_ACTIVITY_TIME, time).commit();
    }

    /**
     * Obtiene el timestamp del ultimo reconocimiento de actividad
     */
    public long getLastActivityTime() {
        return mPreferences.getLong(KEY_LAST_ACTIVITY_TIME, -1);
    }

}
