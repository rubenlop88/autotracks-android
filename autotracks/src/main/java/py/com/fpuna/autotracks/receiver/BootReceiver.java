package py.com.fpuna.autotracks.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.DataUploadAlarmReceiver;

public class BootReceiver extends BroadcastReceiver {

    private ActivityRecognitionController mActivityRecognitionController;

    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            mActivityRecognitionController = new ActivityRecognitionController(context);
            mActivityRecognitionController.startActivityRecognitionUpdates();
            DataUploadAlarmReceiver.setUpAlarm(context);
        }
    }

}
