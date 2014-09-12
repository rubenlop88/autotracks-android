package py.com.fpuna.autotracks;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;
import java.util.List;

import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.AlarmReceiver;

public class MainActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static String TAG = MainActivity.class.getSimpleName();
    private SharedPreferences mPreferences;
    private ActivityRecognitionController mActivityRecognitionController;
    private LocationClient mLocationclient;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "2915cd16");
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                checkCurrentLocation();
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("file:///android_asset/index.html");
        if (!isActivityRecognitionUpdatesStarted()) {
            startActivityRecognition();
        }
        AlarmReceiver.startInexactRepeatingAlarm(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refrescarTrafico();
                return true;
            case R.id.action_share:
                startShareIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_acivity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void startActivityRecognition() {
        mActivityRecognitionController = new ActivityRecognitionController(this);
        mActivityRecognitionController.startActivityRecognitionUpdates();
    }

    private boolean isActivityRecognitionUpdatesStarted() {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return mPreferences.getBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false);
    }

    private void checkCurrentLocation() {
        int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resp == ConnectionResult.SUCCESS) {
            mLocationclient = new LocationClient(this, this, this);
            mLocationclient.connect();
        } else {
            Toast.makeText(this, "No se encontró Google Play Services en el dispositivo",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void refrescarTrafico() {
        mWebView.loadUrl("javascript:dibujarTraficoVelocidad();");
    }

    public void startShareIntent() {
        PackageManager pm = getPackageManager();

        // Chooser especifico para apps de correo electronico, de esta forma el chooser inicialmente
        // tendra solo algunos activities, no se mostraran por ej. las opciones de Bluetooth o Wifi.
        Intent emailIntent = getShareIntent();
        emailIntent.setType("message/rfc822");
        CharSequence title = getResources().getText(R.string.share_intent_title);
        Intent openInChooser = Intent.createChooser(emailIntent, title);

        // Obtenemos todos los activities que responden a text/plain
        Intent sendIntent = getShareIntent();
        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);

        // Filtramos solo los activities que queremos mostrar
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();
        for (int i = 0; i < resInfo.size(); i++) {
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if(packageName.contains("mms")
                    || packageName.contains("twitter")
                    || packageName.contains("facebook")
                    || packageName.contains("whatsapp")
                    || packageName.contains("plus")
                    || packageName.contains("talk")
                    || packageName.contains("viber")) {
                Intent intent = getShareIntent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        // Agregamos los demas activities que queremos mostrar al chooser
        LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);
        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

        // Mostramos el chooser
        startActivity(openInChooser);
    }

    private Intent getShareIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_intent_text));
        sendIntent.setType("text/plain");
        return sendIntent;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location client connected");
        Location loc = mLocationclient.getLastLocation();
        if (loc != null) {
            mWebView.loadUrl("javascript:centrarMapa(" + loc.getLatitude() + ","
                    + loc.getLongitude() + ");");
        }
        mLocationclient.disconnect();
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "Location client Disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Location client failed");
    }
}
