package us.bridgeses.Minder.util.vandy;

import android.support.v7.app.AppCompatActivity;

/**
 * This helper class was created by the Vanderbilt University and made available
 * in the course Programming Cloud Services for Android Handheld Systems at Coursera
 */

/**
 * This abstract class extends the Activity class and overrides lifecycle
 * callbacks for logging various lifecycle events.
 */
public abstract class LifecycleLoggingActivity extends AppCompatActivity {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG = getClass().getSimpleName();
}
