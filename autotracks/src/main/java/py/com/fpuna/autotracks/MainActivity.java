package py.com.fpuna.autotracks;

import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.DataUploadAlarmReceiver;
import py.com.fpuna.autotracks.util.PreferenceUtils;
import py.com.fpuna.autotracks.util.ShareIntentBuilder;

public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGClient;

    private WebView mWebView;
    private PreferenceUtils mPreferenceUtils;
    private ActivityRecognitionController mActivityRecognitionController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPreferenceUtils = new PreferenceUtils(this);
        mActivityRecognitionController = new ActivityRecognitionController(this);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                checkCurrentLocation();
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("file:///android_asset/index.html");

        if (!isBatteryLevelOk() && mPreferenceUtils.isActivityUpdatesStarted()) {
            mActivityRecognitionController.startActivityRecognitionUpdates();
        }

        if (!DataUploadAlarmReceiver.isAlarmSetUp(this)) {
            DataUploadAlarmReceiver.setUpAlarm(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_acivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mWebView.loadUrl("javascript:dibujarTraficoVelocidad();");
                return true;
            case R.id.action_share:
                startActivity(ShareIntentBuilder.buildShareIntent(this));
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_rutas:
                startActivity(new Intent(this, RutasActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void checkCurrentLocation() {
        int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resp == ConnectionResult.SUCCESS) {
            mGClient = new GoogleApiClient.Builder(this)
                    .addOnConnectionFailedListener(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
            mGClient.connect();
        } else {
            Toast.makeText(this, "No se encontró Google Play Services", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isBatteryLevelOk() {
        Intent batteryIntent = getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int rawlevel = batteryIntent.getIntExtra("level", -1);
        double scale = batteryIntent.getIntExtra("scale", -1);
        double level = -1;
        if (rawlevel >= 0 && scale > 0) {
            level = rawlevel * 100 / scale;
        }
        if (level >= 20) {
            mPreferenceUtils.setBatteryLevelOk(true);
            return true;
        } else if (level <= 15 && level >= 0) {
            mPreferenceUtils.setBatteryLevelOk(false);
            return true;
        }

        return !mPreferenceUtils.isBatteryLevelOk();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGClient);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            mWebView.loadUrl("javascript:centrarMapa(" + latitude + "," + longitude + ");");
        }
        mGClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

}
