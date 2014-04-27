package py.com.fpuna.autotracks.tracking;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.location.ActivityRecognitionClient;

import py.com.fpuna.autotracks.Constants;

public class ActivityRecognitionController {

    private static final int INTERVAL_IN_MILLIS = 10 * 1000; // 10 segundos

    private Context mContext;
    private SharedPreferences mPreferences;
    private ActivityRecognitionClient mClient;

    public ActivityRecognitionController(Context context, ActivityRecognitionClient client) {
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mContext = context;
        this.mClient = client;
    }

    public void startActivityRecognitionUpdates() {
        PendingIntent pendingIntent = getPendingIntent();
        mClient.requestActivityUpdates(INTERVAL_IN_MILLIS, pendingIntent);
        mPreferences.edit().putBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, true).commit();
    }

    public void stopActivityRecognitionUpdates() {
        PendingIntent pendingIntent = getPendingIntent();
        mClient.removeActivityUpdates(pendingIntent);
        mPreferences.edit().putBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false).commit();
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(mContext, ActivityRecognitionService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
