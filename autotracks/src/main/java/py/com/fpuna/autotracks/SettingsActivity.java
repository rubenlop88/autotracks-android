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
import py.com.fpuna.autotracks.tracking.LocationController;

public class SettingsActivity extends PreferenceActivity  {

    private Preference.OnPreferenceChangeListener mListener;

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
        mListener  = new PreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.pref_general);
        bindPreference(findPreference(Constants.KEY_RECOGNITION_INTERVAL));
        bindPreference(findPreference(Constants.KEY_RECOGNITION_TOLERANCE));
        bindPreference(findPreference(Constants.KEY_LOCATION_UPDATES_INTERVAL));
    }

    private void bindPreference(Preference preference) {
        preference.setOnPreferenceChangeListener(mListener);
        mListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }

    private class PreferenceChangeListener implements Preference.OnPreferenceChangeListener {

        private ActivityRecognitionController mActivityRecognitionController;
        private LocationController mLocationController;

        public PreferenceChangeListener(Context context) {
            mActivityRecognitionController = new ActivityRecognitionController(context);
            mLocationController = new LocationController(context);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(value.toString());
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            String key = listPreference.getKey();
            if (Constants.KEY_RECOGNITION_INTERVAL.equals(key) || Constants.KEY_RECOGNITION_TOLERANCE.equals(key)) {
                mActivityRecognitionController.restartActivityRecognitionUpdates();
            } else if (Constants.KEY_LOCATION_UPDATES_INTERVAL.equals(key)) {
                if (mLocationController.isLocationUpdatesStarted()) {
                    mLocationController.restartLocationUpdates();
                }
            }
            return true;
        }

    }
}
