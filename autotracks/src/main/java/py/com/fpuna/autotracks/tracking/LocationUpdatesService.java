package py.com.fpuna.autotracks.tracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderApi;

import py.com.fpuna.autotracks.model.Localizacion;
import py.com.fpuna.autotracks.provider.AutotracksContract.Localizaciones;
import py.com.fpuna.autotracks.util.PreferenceUtils;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LocationUpdatesService extends IntentService {

    private static final float MIN_DISTANCE_METERS = 50;
    private static final double MAX_SPEED_METERS_PER_SECOND = 140 / 3.6;
    private static final float ACCURACY_LIMIT = 200;

    private Context mContext;
    private PreferenceUtils mPreferenceUtils;

    public LocationUpdatesService() {
        super("LocationUpdatesService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        mPreferenceUtils = new PreferenceUtils(mContext);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mPreferenceUtils.isActivityUpdatesStarted()) {
            if (mPreferenceUtils.isLocationUpdatesStarted()) {
                Location location = intent.getExtras().getParcelable(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
                if (location != null) {
                    saveLocation(location);
                }
            }
        }
    }

    private void saveLocation(Location location) {
        String rutaId = mPreferenceUtils.getCurrentTrackId();
        if (rutaId != null) {
            String lastRutaId = mPreferenceUtils.getLastTrackId();
            if (lastRutaId == null || !rutaId.equals(lastRutaId) || validateLocation(location)) {
                Localizacion localizacion = new Localizacion(location, Long.valueOf(rutaId));
                cupboard().withContext(mContext).put(Localizaciones.CONTENT_URI, localizacion);
                mPreferenceUtils.saveLastLocation(location, rutaId);
            }
        }
    }

    private boolean validateLocation(Location location) {
        float distanceInMeters = getDistanceInMetters(location);
        long timeInSeconds = (location.getTime() - mPreferenceUtils.getLasTime()) / 1000;
        double speedInMetersPerSecond = distanceInMeters / timeInSeconds;
        return distanceInMeters > MIN_DISTANCE_METERS
                && speedInMetersPerSecond < MAX_SPEED_METERS_PER_SECOND
                && location.getAccuracy() < ACCURACY_LIMIT ;
    }

    private float getDistanceInMetters(Location location) {
        float[] results = new float[3];
        double lastLatitude = mPreferenceUtils.getLastLatitude();
        double lastLongitude = mPreferenceUtils.getLastLongitude();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Location.distanceBetween(lastLatitude, lastLongitude, latitude, longitude, results);
        return results[0];
    }

}
