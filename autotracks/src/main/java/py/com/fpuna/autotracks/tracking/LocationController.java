package py.com.fpuna.autotracks.tracking;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;

import py.com.fpuna.autotracks.Constants;
import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.provider.AutotracksContract.Rutas;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LocationController {

    private static final int INTERVAL_IN_MILLIS = 30 * 1000; // 30 segundos

    private Context mContext;
    private SharedPreferences mPreferences;
    private LocationClient mClient;

    public LocationController(Context context, LocationClient client) {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mContext = context;
        this.mClient = client;
    }

    public void startLocationUpdates() {
        mClient.requestLocationUpdates(getLocationRequest(), getPendingIntent());
        mPreferences.edit().putBoolean(Constants.KEY_LOCATION_UPDATES_STARTED, true).commit();
        startNewTrack();
    }

    public void stopLocationUpdates() {
        mClient.removeLocationUpdates(getPendingIntent());
        mPreferences.edit().putBoolean(Constants.KEY_LOCATION_UPDATES_STARTED, false).commit();
        endCurrentTrack();
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(mContext, LocationService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private LocationRequest getLocationRequest() {
        return new LocationRequest()
                .setInterval(INTERVAL_IN_MILLIS)
                .setFastestInterval(INTERVAL_IN_MILLIS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startNewTrack() {
        Ruta ruta = new Ruta(System.currentTimeMillis());
        Uri uri = cupboard().withContext(mContext).put(Rutas.CONTENT_URI, ruta);
        mPreferences.edit().putString(Constants.KEY_CURRENT_TRACK_ID, Rutas.getId(uri)).commit();
    }

    private void endCurrentTrack() {
        mPreferences.edit().putString(Constants.KEY_CURRENT_TRACK_ID, null).commit();
    }

}
