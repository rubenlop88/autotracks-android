package py.com.fpuna.autotracks.tracking;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import py.com.fpuna.autotracks.Constants;
import py.com.fpuna.autotracks.model.Ruta;
import py.com.fpuna.autotracks.provider.AutotracksContract.Rutas;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class LocationController implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private enum Operation {
        START, STOP
    }

    private static final int INTERVAL_IN_MILLIS = 60 * 1000; // 60 segundos

    private Context mContext;
    private SharedPreferences mPreferences;
    private FusedLocationProviderApi mClient;
    private GoogleApiClient mGClient;
    private Operation operation;

    public LocationController(Context context) {
        this.mPreferences = context.getSharedPreferences("py.com.fpuna.autotracks_preferences",
                Context.MODE_PRIVATE);
        this.mGClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        this.mClient = LocationServices.FusedLocationApi;
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void onConnected(Bundle bundle) {
        switch (operation) {
            case START:
                mClient.requestLocationUpdates(mGClient, getLocationRequest(), getPendingIntent());
                mPreferences.edit().putBoolean(Constants.KEY_LOCATION_UPDATES_STARTED, true).commit();
                startNewTrack();
                break;
            case STOP:
                mClient.removeLocationUpdates(mGClient, getPendingIntent());
                mPreferences.edit().putBoolean(Constants.KEY_LOCATION_UPDATES_STARTED, false).commit();
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

    public void startLocationUpdates() {
        operation = Operation.START;
        mGClient.connect();
    }

    public void stopLocationUpdates() {
        operation = Operation.STOP;
        mGClient.connect();
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
        ContentValues values = new ContentValues();
        values.put(Rutas._ID, Long.valueOf(mPreferences.getString(Constants.KEY_CURRENT_TRACK_ID, null)));
        values.put(Rutas.FIN, System.currentTimeMillis());
        cupboard().withContext(mContext).update(Rutas.CONTENT_URI, values);
        mPreferences.edit().putString(Constants.KEY_CURRENT_TRACK_ID, null).commit();
    }

}
