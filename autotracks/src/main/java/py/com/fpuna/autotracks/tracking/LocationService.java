package py.com.fpuna.autotracks.tracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.location.LocationClient;

import py.com.fpuna.autotracks.Constants;
import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.provider.AutotracksContract.Localizaciones;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LocationService extends IntentService {

    private static final String KEY_LAST_LOCATION_TRACK_ID = "key_last_location_track_id";
    private static final String KEY_LAST_LOCATION_LATITUDE = "key_last_location_latitude";
    private static final String KEY_LAST_LOCATION_LONGITUDE = "key_last_location_longitude";
    private static final String KEY_LAST_LOCATION_TIME = "key_las_location_time";

    private static final float MIN_DISTANCE_METTERS = 50;
    private static final double MAX_SPEED_METTERS_PER_SECOND = 140 / 3.6;

    private Context mContext;
    private SharedPreferences mPreferences;

    public LocationService() {
        super("LocationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (isActivityRecognitionUpdatesStarted()) {
            if (isLocationUpdatesStarted()) {
                Location location = intent.getExtras().getParcelable(LocationClient.KEY_LOCATION_CHANGED);
                if (location != null) {
                    saveLocation(location);
                }
            }
        }
    }

    private void saveLocation(Location location) {
        String rutaId = getCurrentTrackId();
        if (rutaId != null) {
            String lastRutaId = getLastTrackId();
            if (lastRutaId == null || !rutaId.equals(lastRutaId) || validateLocation(location)) {
                Localizacion localizacion = new Localizacion(location, Long.valueOf(rutaId));
                cupboard().withContext(mContext).put(Localizaciones.CONTENT_URI, localizacion);
                saveLastLocation(location, rutaId);
            }
        }
    }

    private boolean validateLocation(Location location) {
        float[] results = new float[3];
        Location.distanceBetween(getLastLatitude(), getLastLongitude(), location.getLatitude(), location.getLongitude(), results);
        float distanceInMeters = results[0];
        long timeInSeconds = (location.getTime() - getLasTime()) / 1000;
        double speedInMettersPerSecond = distanceInMeters / timeInSeconds;
        return distanceInMeters > MIN_DISTANCE_METTERS && speedInMettersPerSecond < MAX_SPEED_METTERS_PER_SECOND;
    }

    private boolean isActivityRecognitionUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false);
    }

    private boolean isLocationUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_LOCATION_UPDATES_STARTED, false);
    }

    private String getCurrentTrackId() {
        return mPreferences.getString(Constants.KEY_CURRENT_TRACK_ID, null);
    }

    private String getLastTrackId() {
        return mPreferences.getString(KEY_LAST_LOCATION_TRACK_ID, null);
    }

    private double getLastLongitude() {
        return Double.longBitsToDouble(mPreferences.getLong(KEY_LAST_LOCATION_LONGITUDE, -1));
    }

    private double getLastLatitude() {
        return Double.longBitsToDouble(mPreferences.getLong(KEY_LAST_LOCATION_LATITUDE, -1));
    }

    private long getLasTime() {
        return mPreferences.getLong(KEY_LAST_LOCATION_TIME, -1);
    }

    private void saveLastLocation(Location location, String rutaId) {
        mPreferences.edit().putString(KEY_LAST_LOCATION_TRACK_ID, rutaId)
                .putLong(KEY_LAST_LOCATION_LATITUDE, Double.doubleToLongBits(location.getLatitude()))
                .putLong(KEY_LAST_LOCATION_LONGITUDE, Double.doubleToLongBits(location.getLongitude()))
                .putLong(KEY_LAST_LOCATION_TIME, location.getTime())
                .commit();
    }

}
