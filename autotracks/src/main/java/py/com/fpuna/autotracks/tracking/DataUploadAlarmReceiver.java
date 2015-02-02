package py.com.fpuna.autotracks.tracking;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DataUploadAlarmReceiver extends BroadcastReceiver {

    public static boolean isAlarmSetUp(Context context) {
        return getPendingIntent(context, PendingIntent.FLAG_NO_CREATE) != null;
    }

    public static void setUpAlarm(Context context) {
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                getPendingIntent(context, 0));
    }

    private static PendingIntent getPendingIntent(Context context, int flags) {
        Intent intent = new Intent(context, DataUploadAlarmReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, flags);
    }

    public DataUploadAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        DataUploadIntentService.startService(context);
    }

}
