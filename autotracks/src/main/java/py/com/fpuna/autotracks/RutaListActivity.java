package py.com.fpuna.autotracks;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.LocationClient;

import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.LocationController;

public class RutaListActivity extends ActionBarActivity implements
        RutaListFragment.Callbacks,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    public static void startActivity(Context context) {
        Intent detailIntent = new Intent(context, RutaListActivity.class);
        context.startActivity(detailIntent);
    }

    private final static int RESOLUTION_REQUEST = 9000;

    private boolean mTwoPane;
    private SharedPreferences mPreferences;
    private LocationClient mLocationClient;
    private LocationController mLocationController;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private ActivityRecognitionController mActivityRecognitionController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruta_list);

        if (findViewById(R.id.track_detail_container) != null) {
            mTwoPane = true;
            ((RutaListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.track_list))
                    .setActivateOnItemClick(true);
        }

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mLocationClient = new LocationClient(this, this, this);
        mLocationController = new LocationController(this, mLocationClient);
        mActivityRecognitionClient = new ActivityRecognitionClient(this, this, this);
        mActivityRecognitionController = new ActivityRecognitionController(this, mActivityRecognitionClient);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationClient.connect();
        mActivityRecognitionClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationClient.disconnect();
        mActivityRecognitionClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_ruta_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isActivityRecognitionUpdatesStarted()) {
            menu.findItem(R.id.menu_item_iniciar).setVisible(false);
        } else {
            menu.findItem(R.id.menu_item_detener).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_iniciar:
                mActivityRecognitionController.startActivityRecognitionUpdates();
                supportInvalidateOptionsMenu();
                return true;
            case R.id.menu_item_detener:
                if (isLocationUpdatesStarted()) {
                    mLocationController.stopLocationUpdates();
                }
                mActivityRecognitionController.stopActivityRecognitionUpdates();
                supportInvalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            RutaDetailFragment fragment = RutaDetailFragment.newInstance(id);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.track_detail_container, fragment)
                    .commit();
        } else {
            RutaDetailActivity.startActivity(this, id);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(RutaListActivity.this, RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Do nothing.
            }
        } else {
            ErrorDialog errorDialog = ErrorDialog.newInstance(connectionResult.getErrorCode());
            errorDialog.show(getSupportFragmentManager(), "");
        }
    }

    private boolean isActivityRecognitionUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false);
    }

    private boolean isLocationUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_LOCATION_UPDATES_STARTED, false);
    }

    public static class ErrorDialog extends DialogFragment {

        static final String EXTRA_RESULT_CODE = "RESULT_CODE";

        public static ErrorDialog newInstance(int resultCode) {
            Bundle extras = new Bundle();
            extras.putInt(ErrorDialog.EXTRA_RESULT_CODE, resultCode);
            ErrorDialog errorDialog = new ErrorDialog();
            errorDialog.setArguments(extras);
            return errorDialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int resultCode = getArguments().getInt(EXTRA_RESULT_CODE);
            return GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (getActivity() != null) {
                getActivity().finish();
            }
        }

    }

}
