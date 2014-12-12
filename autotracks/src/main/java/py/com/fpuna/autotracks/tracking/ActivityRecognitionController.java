package py.com.fpuna.autotracks.tracking;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;

import py.com.fpuna.autotracks.Constants;

public class ActivityRecognitionController implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private enum Operation {
        START, STOP
    }

    private static final int INTERVAL_IN_MILLIS = 15 * 1000; // 15 segundos

    private Context mContext;
    private SharedPreferences mPreferences;
    private ActivityRecognitionApi mClient;
    private GoogleApiClient mGClient;
    private Operation operation;

    public ActivityRecognitionController(Context context) {
        this.mPreferences = context.getSharedPreferences("py.com.fpuna.autotracks_preferences",
                Context.MODE_PRIVATE);
        this.mGClient = new GoogleApiClient.Builder(context)
                .addApi(ActivityRecognition.API).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        this.mClient = ActivityRecognition.ActivityRecognitionApi;
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void onConnected(Bundle bundle) {
        switch (operation) {
            case START:
                mClient.requestActivityUpdates(mGClient, INTERVAL_IN_MILLIS, getPendingIntent());
                mPreferences.edit().putBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, true).commit();
                break;
            case STOP:
                mClient.removeActivityUpdates(mGClient, getPendingIntent());
                mPreferences.edit().putBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false).commit();
                break;
        }
        mGClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void startActivityRecognitionUpdates() {
        operation = Operation.START;
        mGClient.connect();
    }

    public void stopActivityRecognitionUpdates() {
        operation = Operation.STOP;
        mGClient.connect();
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(mContext, ActivityRecognitionService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
