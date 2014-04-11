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

    private boolean isActivityRecognitionUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false);
    }

    private boolean isLocationUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_LOCATION_UPDATES_STARTED, false);
    }

    private void saveLocation(Location location) {
        String rutaId = mPreferences.getString(Constants.KEY_CURRENT_TRACK_ID, null);
        if (rutaId != null) {
            Localizacion localizacion = new Localizacion(location, Long.valueOf(rutaId));
            cupboard().withContext(mContext).put(Localizaciones.CONTENT_URI, localizacion);
        }
    }

}
