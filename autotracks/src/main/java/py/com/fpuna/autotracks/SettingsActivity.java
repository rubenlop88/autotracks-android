package py.com.fpuna.autotracks;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.LocationUpdatesController;
import py.com.fpuna.autotracks.util.PreferenceUtils;

import static py.com.fpuna.autotracks.util.PreferenceUtils.KEY_LOCATION_UPDATES_INTERVAL;
import static py.com.fpuna.autotracks.util.PreferenceUtils.KEY_ACTIVITY_RECOGNITION_INTERVAL;
import static py.com.fpuna.autotracks.util.PreferenceUtils.KEY_ACTIVITY_RECOGNITION_TOLERANCE;

public class SettingsActivity extends PreferenceActivity  {

    private PreferenceUtils mPreferenceUtils;
    private Preference.OnPreferenceChangeListener mPreferencesListener;

    // alambre para mostrar un toolbar en este PreferenceActivity.
    // ver  http://stackoverflow.com/questions/17849193/how-to-add-action-bar-from-support-library-into-preferenceactivity
    @Override
    public void setContentView(int layoutResId) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.activity_settings,
                new LinearLayout(this),
                false);

        Toolbar toolbar = (Toolbar) contentView.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setTitle(getTitle());

        ViewGroup contentWrapper = (ViewGroup) contentView.findViewById(R.id.content_wrapper);
        LayoutInflater.from(this).inflate(layoutResId, contentWrapper, true);

        getWindow().setContentView(contentView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mPreferenceUtils = new PreferenceUtils(this);
        mPreferencesListener = new PreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.pref_general);
        bindPreference(findPreference(KEY_ACTIVITY_RECOGNITION_INTERVAL));
        bindPreference(findPreference(KEY_ACTIVITY_RECOGNITION_TOLERANCE));
        bindPreference(findPreference(KEY_LOCATION_UPDATES_INTERVAL));
    }

    private void bindPreference(Preference preference) {
        preference.setOnPreferenceChangeListener(mPreferencesListener);
        mPreferencesListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }

    private class PreferenceChangeListener implements Preference.OnPreferenceChangeListener {

        private ActivityRecognitionController mActivityRecognitionController;
        private LocationUpdatesController mLocationUpdatesController;

        public PreferenceChangeListener(Context context) {
            mActivityRecognitionController = new ActivityRecognitionController(context);
            mLocationUpdatesController = new LocationUpdatesController(context);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(value.toString());
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            String key = listPreference.getKey();
            if (KEY_ACTIVITY_RECOGNITION_INTERVAL.equals(key) ||
                    KEY_ACTIVITY_RECOGNITION_TOLERANCE.equals(key)) {
                mActivityRecognitionController.restartActivityRecognitionUpdates();
            } else if (KEY_LOCATION_UPDATES_INTERVAL.equals(key)) {
                if (mPreferenceUtils.isLocationUpdatesStarted()) {
                    mLocationUpdatesController.restartLocationUpdates();
                }
            }
            return true;
        }

    }
}
