package py.com.fpuna.autotracks.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    private ActivityRecognitionController mActivityRecognitionController;

    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            mActivityRecognitionController = new ActivityRecognitionController(context);
            mActivityRecognitionController.startActivityRecognitionUpdates();
            AlarmReceiver.startInexactRepeatingAlarm(context);
        }
    }

}
