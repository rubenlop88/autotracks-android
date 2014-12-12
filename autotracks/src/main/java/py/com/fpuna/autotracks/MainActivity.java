package py.com.fpuna.autotracks;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.AlarmReceiver;

public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {

    private static String TAG = MainActivity.class.getSimpleName();
    private SharedPreferences mPreferences;
    private ActivityRecognitionController mActivityRecognitionController;
    private WebView mWebView;
    private GoogleApiClient gApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                checkCurrentLocation();
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("file:///android_asset/index.html");
        if (!isActivityRecognitionUpdatesStarted() && isBatteryLevelOk()) {
            startActivityRecognition();
        }
        if (!AlarmReceiver.isAlarmSetUp(this)) {
            AlarmReceiver.setUpAlarm(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreschTrafic();
                return true;
            case R.id.action_share:
                startShareIntent();
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
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
        mPreferences = getSharedPreferences("py.com.fpuna.autotracks_preferences",
                Context.MODE_PRIVATE);
        return mPreferences.getBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false);
    }

    private void checkCurrentLocation() {
        int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resp == ConnectionResult.SUCCESS) {
            gApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            gApiClient.connect();
        } else {
            Toast.makeText(this, "No se encontró Google Play Services en el dispositivo",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void refreschTrafic() {
        mWebView.loadUrl("javascript:dibujarTraficoVelocidad();");
    }

    private boolean isBatteryLevelOk() {
        return !mPreferences.getBoolean(Constants.KEY_BATTERY_LEVEL_LOW, false);
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
        for (ResolveInfo ri : resInfo) {
            String packageName = ri.activityInfo.packageName;
            if (packageName.contains("twitter")
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
        Location loc = LocationServices.FusedLocationApi.getLastLocation(gApiClient);
        if (loc != null) {
            mWebView.loadUrl("javascript:centrarMapa(" + loc.getLatitude() + ","
                    + loc.getLongitude() + ");");
        }
        gApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location client Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Location client failed");
    }
}
