package py.com.fpuna.autotracks;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.bugsense.trace.BugSenseHandler;

import py.com.fpuna.autotracks.tracking.ActivityRecognitionController;
import py.com.fpuna.autotracks.tracking.AlarmReceiver;

public class MainActivity extends ActionBarActivity {

    private SharedPreferences mPreferences;
    private ActivityRecognitionController mActivityRecognitionController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "2915cd16");
        setContentView(R.layout.activity_main);
        WebView myWebView = (WebView) findViewById(R.id.webView);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.loadUrl("file:///android_asset/index.html");
        if (!isActivityRecognitionUpdatesStarted()) {
            startActivityRecognition();
        }
        AlarmReceiver.startInexactRepeatingAlarm(this);
    }

    private void startActivityRecognition() {
        mActivityRecognitionController = new ActivityRecognitionController(this);
        mActivityRecognitionController.startActivityRecognitionUpdates();
    }

    private boolean isActivityRecognitionUpdatesStarted() {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return mPreferences.getBoolean(Constants.KEY_ACTIVITY_UPDATES_STARTED, false);
    }
}
