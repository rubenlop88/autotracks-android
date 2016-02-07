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

    private Context mContext;
    private PreferenceUtils mPreferenceUtils;
    private FusedLocationProviderApi mClient;
    private GoogleApiClient mGClient;
    private Operation mOperation;

    public LocationUpdatesController(Context context) {
        this.mContext = context.getApplicationContext();
        this.mPreferenceUtils = new PreferenceUtils(mContext);
        this.mGClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
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
