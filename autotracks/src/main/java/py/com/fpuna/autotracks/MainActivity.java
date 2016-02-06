package py.com.fpuna.autotracks;

import android.content.Intent;
<<<<<<< HEAD
=======
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
>>>>>>> 6b2f0c5f43300dafe62d3663cc58971153213c7a
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

<<<<<<< HEAD
        mPreferenceUtils = new PreferenceUtils(this);
        mActivityRecognitionController = new ActivityRecognitionController(this);
=======
        mPreferences = getSharedPreferences("py.com.fpuna.autotracks_preferences",
                Context.MODE_PRIVATE);
>>>>>>> 6b2f0c5f43300dafe62d3663cc58971153213c7a

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                checkCurrentLocation();
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("file:///android_asset/index.html");
<<<<<<< HEAD

        if (!mPreferenceUtils.isActivityUpdatesStarted() && mPreferenceUtils.isBatteryLevelOk()) {
            mActivityRecognitionController.startActivityRecognitionUpdates();
=======
        if (isBatteryLevelOk() && !isActivityRecognitionUpdatesStarted()) {
            startActivityRecognition();
>>>>>>> 6b2f0c5f43300dafe62d3663cc58971153213c7a
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

<<<<<<< HEAD
=======
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_acivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void startActivityRecognition() {
        mActivityRecognitionController = new ActivityRecognitionController(this);
        mActivityRecognitionController.startActivityRecognitionUpdates();
    }

    private boolean isActivityRecognitionUpdatesStarted() {

        return mPreferences.getBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false);
    }

>>>>>>> 6b2f0c5f43300dafe62d3663cc58971153213c7a
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
<<<<<<< HEAD
            Toast.makeText(this, "No se encontró Google Play Services", Toast.LENGTH_LONG).show();
=======
            Toast.makeText(this, "No se encontró Google Play Services en el dispositivo",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void refreschTrafic() {
        mWebView.loadUrl("javascript:dibujarTraficoVelocidad();");
    }

    private boolean isBatteryLevelOk() {
        Intent batteryIntent = this.getApplicationContext().registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int rawlevel = batteryIntent.getIntExtra("level", -1);
        double scale = batteryIntent.getIntExtra("scale", -1);
        double level = -1;
        if (rawlevel >= 0 && scale > 0) {
            level = rawlevel * 100 / scale;
        }
        if (level >= 20) {
            mPreferences.edit().putBoolean(Constants.KEY_BATTERY_LEVEL_LOW, false).apply();
            return true;
        } else if (level <= 15 && level >= 0) {
            mPreferences.edit().putBoolean(Constants.KEY_BATTERY_LEVEL_LOW, true).apply();
            return true;
        }

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
>>>>>>> 6b2f0c5f43300dafe62d3663cc58971153213c7a
        }
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
