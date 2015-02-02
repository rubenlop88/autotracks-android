package py.com.fpuna.autotracks.tracking;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionApi;

import py.com.fpuna.autotracks.util.PreferenceUtils;

public class ActivityRecognitionController implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private enum Operation {
        START, STOP, RESTART
    }

    private Context mContext;
    private PreferenceUtils mPreferenceUtils;
    private ActivityRecognitionApi mClient;
    private GoogleApiClient mGClient;
    private Operation mOperation;

    public ActivityRecognitionController(Context context) {
        this.mContext = context.getApplicationContext();
        this.mPreferenceUtils = new PreferenceUtils(mContext);
        this.mGClient = new GoogleApiClient.Builder(mContext)
                .addApi(ActivityRecognition.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
        this.mClient = ActivityRecognition.ActivityRecognitionApi;
    }

    @Override
    public void onConnected(Bundle bundle) {
        switch (mOperation) {
            case RESTART:
                if (mPreferenceUtils.isActivityUpdatesStarted()) {
                    mClient.removeActivityUpdates(mGClient, getPendingIntent());
                    mClient.requestActivityUpdates(mGClient, mPreferenceUtils.getActivityRecognitionIntervalMillis(), getPendingIntent());
                }
                break;
            case START:
                mClient.requestActivityUpdates(mGClient, mPreferenceUtils.getActivityRecognitionIntervalMillis(), getPendingIntent());
                mPreferenceUtils.setActivityUpdatesStarted(true);
                break;
            case STOP:
                mClient.removeActivityUpdates(mGClient, getPendingIntent());
                mPreferenceUtils.setActivityUpdatesStarted(false);
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

    public void restartActivityRecognitionUpdates() {
        mOperation = Operation.RESTART;
        mGClient.connect();
    }

    public void startActivityRecognitionUpdates() {
        mOperation = Operation.START;
        mGClient.connect();
    }

    public void stopActivityRecognitionUpdates() {
        mOperation = Operation.STOP;
        mGClient.connect();
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(mContext, ActivityRecognitionService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
