package py.com.fpuna.autotracks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.LocationController;

public class RutaListActivity extends ActionBarActivity implements RutaListFragment.Callbacks {

    public static void startActivity(Context context) {
        Intent detailIntent = new Intent(context, RutaListActivity.class);
        context.startActivity(detailIntent);
    }

    private boolean mTwoPane;
    private SharedPreferences mPreferences;
    private LocationController mLocationController;
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
        mLocationController = new LocationController(this);
        mActivityRecognitionController = new ActivityRecognitionController(this);
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

    private boolean isActivityRecognitionUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false);
    }

    private boolean isLocationUpdatesStarted() {
        return mPreferences.getBoolean(Constants.KEY_LOCATION_UPDATES_STARTED, false);
    }

}
