package py.com.fpuna.autotracks.tracking;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.ActivityRecognitionClient;

import py.com.fpuna.autotracks.Constants;

public class ActivityRecognitionController implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private enum Operation {
        START, STOP
    }

    private static final int INTERVAL_IN_MILLIS = 10 * 1000; // 10 segundos

    private Context mContext;
    private SharedPreferences mPreferences;
    private ActivityRecognitionClient mClient;
    private Operation operation;

    public ActivityRecognitionController(Context context) {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mClient = new ActivityRecognitionClient(context, this, this);
        this.mContext = context.getApplicationContext();
    }

    @Override
    public void onConnected(Bundle bundle) {
        switch (operation) {
            case START:
                mClient.requestActivityUpdates(INTERVAL_IN_MILLIS, getPendingIntent());
                mPreferences.edit().putBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, true).commit();
                break;
            case STOP:
                mClient.removeActivityUpdates(getPendingIntent());
                mPreferences.edit().putBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false).commit();
                break;
        }
        mClient.disconnect();
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void startActivityRecognitionUpdates() {
        operation = Operation.START;
        mClient.connect();
    }

    public void stopActivityRecognitionUpdates() {
        operation = Operation.STOP;
        mClient.connect();
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(mContext, ActivityRecognitionService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
