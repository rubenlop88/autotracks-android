package py.com.fpuna.autotracks;

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

public class SettingsActivity extends PreferenceActivity  {

    private Preference.OnPreferenceChangeListener mListener = new Preference.OnPreferenceChangeListener() {

        private ActivityRecognitionController mActivityRecognitionController;

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
            }
            startActivityRecognition();
            return true;
        }

        private void startActivityRecognition() {
            mActivityRecognitionController = new ActivityRecognitionController(SettingsActivity.this);
            mActivityRecognitionController.restartActivityRecognitionUpdates();
        }

    };

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
        addPreferencesFromResource(R.xml.pref_general);
        bindPreference(findPreference(Constants.KEY_RECOGNITION_INTERVAL));
        bindPreference(findPreference(Constants.KEY_RECOGNITION_TOLERANCE));
    }

    private void bindPreference(Preference preference) {
        preference.setOnPreferenceChangeListener(mListener);
        mListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), ""));
    }

}
