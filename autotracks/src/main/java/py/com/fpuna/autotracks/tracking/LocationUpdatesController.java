package py.com.fpuna.autotracks.tracking;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.provider.AutotracksContract.Rutas;
import py.com.fpuna.autotracks.util.PreferenceUtils;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LocationUpdatesController implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private enum Operation {
        START, STOP, RESTART
    }

<<<<<<< HEAD:autotracks/src/main/java/py/com/fpuna/autotracks/tracking/LocationUpdatesController.java
=======
    private static final String DEFAULT_INTERVAL = "60"; // 60 segundos

>>>>>>> 6b2f0c5f43300dafe62d3663cc58971153213c7a:autotracks/src/main/java/py/com/fpuna/autotracks/tracking/LocationController.java
    private Context mContext;
    private PreferenceUtils mPreferenceUtils;
    private FusedLocationProviderApi mClient;
    private GoogleApiClient mGClient;
    private Operation mOperation;

<<<<<<< HEAD:autotracks/src/main/java/py/com/fpuna/autotracks/tracking/LocationUpdatesController.java
    public LocationUpdatesController(Context context) {
        this.mContext = context.getApplicationContext();
        this.mPreferenceUtils = new PreferenceUtils(mContext);
        this.mGClient = new GoogleApiClient.Builder(mContext)
=======
    public LocationController(Context context) {
        this.mPreferences = context.getSharedPreferences("py.com.fpuna.autotracks_preferences",
                Context.MODE_PRIVATE);
        this.mContext = context.getApplicationContext();
        this.mGClient = new GoogleApiClient.Builder(this.mContext)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
>>>>>>> 6b2f0c5f43300dafe62d3663cc58971153213c7a:autotracks/src/main/java/py/com/fpuna/autotracks/tracking/LocationController.java
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        this.mClient = LocationServices.FusedLocationApi;
    }

    @Override
    public void onConnected(Bundle bundle) {
        switch (mOperation) {
            case RESTART:
                if (mPreferenceUtils.isLocationUpdatesStarted()) {
                    mClient.removeLocationUpdates(mGClient, getPendingIntent());
                    mClient.requestLocationUpdates(mGClient, getLocationRequest(), getPendingIntent());
                }
                break;
            case START:
                mClient.requestLocationUpdates(mGClient, getLocationRequest(), getPendingIntent());
                mPreferenceUtils.setLocationUpdatesStarted(true);
                startNewTrack();
                break;
            case STOP:
                mClient.removeLocationUpdates(mGClient, getPendingIntent());
                mPreferenceUtils.setLocationUpdatesStarted(false);
                endCurrentTrack();
                break;
        }
        mGClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void restartLocationUpdates() {
        mOperation = Operation.RESTART;
        mGClient.connect();
    }

    public void startLocationUpdates() {
        mOperation = Operation.START;
        mGClient.connect();
    }

    public void stopLocationUpdates() {
        mOperation = Operation.STOP;
        mGClient.connect();
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(mContext, LocationUpdatesService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private LocationRequest getLocationRequest() {
        long interval = mPreferenceUtils.getLocationUpdatesIntervalMillis();
        return new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(interval)
                .setInterval(interval);
    }

<<<<<<< HEAD:autotracks/src/main/java/py/com/fpuna/autotracks/tracking/LocationUpdatesController.java
=======
    private long getIntervalMillis() {
        String interval = mPreferences.getString(Constants.KEY_LOCATION_UPDATES_INTERVAL, DEFAULT_INTERVAL);
        return Long.valueOf(interval) * 1000;
    }

>>>>>>> 6b2f0c5f43300dafe62d3663cc58971153213c7a:autotracks/src/main/java/py/com/fpuna/autotracks/tracking/LocationController.java
    private void startNewTrack() {
        Ruta ruta = new Ruta(System.currentTimeMillis());
        Uri uri = cupboard().withContext(mContext).put(Rutas.CONTENT_URI, ruta);
        mPreferenceUtils.setCurrentTrackId(Rutas.getId(uri));
    }

    private void endCurrentTrack() {
        ContentValues values = new ContentValues();
        values.put(Rutas._ID, Long.valueOf(mPreferenceUtils.getCurrentTrackId()));
        values.put(Rutas.FIN, System.currentTimeMillis());
        cupboard().withContext(mContext).update(Rutas.CONTENT_URI, values);
        mPreferenceUtils.setCurrentTrackId(null);
    }

}
