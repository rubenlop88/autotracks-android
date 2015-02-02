package py.com.fpuna.autotracks.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

import py.com.fpuna.autotracks.R;

public class ShareIntentBuilder {

    private ShareIntentBuilder () {
    }

    public static Intent buildShareIntent(Context context) {
        // Obtenemos un chooser especifico para apps de email, de esta forma el chooser inicialmente
        // tendra solo algunos activities, no se mostraran por ej. las opciones de Bluetooth o Wifi.
        Intent openInChooser = Intent.createChooser(getEmailIntent(context), context.getResources().getText(R.string.share_intent_title));
        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, getLabeledIntents(context));
        return openInChooser;
    }

    private static Intent getEmailIntent(Context context) {
        Intent emailIntent = getShareIntent(context);
        emailIntent.setType("message/rfc822");
        return emailIntent;
    }

    private static LabeledIntent[] getLabeledIntents(Context context) {
        Intent sendIntent = getShareIntent(context);
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intents = new ArrayList<>();
        for (ResolveInfo ri : resInfo) {
            String packageName = ri.activityInfo.packageName;
            if (packageName.contains("twitter")
                    || packageName.contains("facebook")
                    || packageName.contains("whatsapp")
                    || packageName.contains("plus")
                    || packageName.contains("talk")
                    || packageName.contains("viber")) {
                Intent intent = getShareIntent(context);
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intents.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }
        return intents.toArray(new LabeledIntent[intents.size()]);
    }

    private static Intent getShareIntent(Context context) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_intent_text));
        sendIntent.setType("text/plain");
        return sendIntent;
    }

}
