package py.com.fpuna.autotracks;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
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

import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.AlarmReceiver;

public class MainActivity extends ActionBarActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static String TAG = MainActivity.class.getSimpleName();
    private SharedPreferences mPreferences;
    private ActivityRecognitionController mActivityRecognitionController;
    private WebView myWebView;
    private LocationClient locationclient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "2915cd16");
        setContentView(R.layout.activity_main);
        myWebView = (WebView) findViewById(R.id.webView);
        myWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                checkCurrentLocation();
            }
        });
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.loadUrl("file:///android_asset/index.html");
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
            /*case R.id.action_settings:
                //TODO opciones del menú
                return true;*/
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
            locationclient = new LocationClient(this, this, this);
            locationclient.connect();
        } else {
            Toast.makeText(this, "No se encontró Google Play Services en el dispositivo",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void refrescarTrafico() {
        myWebView.loadUrl("javascript:dibujarTraficoVelocidad();");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location client connected");
        Location loc = locationclient.getLastLocation();
        if (loc != null) {
            myWebView.loadUrl("javascript:centrarMapa(" + loc.getLatitude() + ","
                    + loc.getLongitude() + ");");
        }
        locationclient.disconnect();
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
