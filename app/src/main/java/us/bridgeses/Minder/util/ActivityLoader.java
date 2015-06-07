package us.bridgeses.Minder.util;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

/**
 * <p>Asynchronously loads the activity defined in intent.</p>
 * <p>Useful for activities that may take a while to load, such as those that need data passed</p>
 * <p>Or that use external resources</p>
 */
public class ActivityLoader extends AsyncTask<Void, Void, Void> {

    public interface ActivityListener {
        void onStart();
        void onCancel();
        void onFinish();
    }

    Intent intent;
    Fragment fragment = null;
    Activity activity = null;
    int activityCode;
    ProgressDialog progressDialog;
    ActivityListener listener;

    // These first two constructors are used if called from a fragment

    /**
     * This constructor should be used if called from a fragment, and no result is desired
     * @param listener is a callback interface
     * @param intent is the intent used to create the new activity
     * @param fragment is the calling fragment. This is where the result will go when the activity is done
     */
    public ActivityLoader(ActivityListener listener, Intent intent, Fragment fragment){
        this(listener, intent, fragment, 0);
    }

    /**
     * This constructor should be used if called from a fragment, and a result is desired
     * @param listener is a callback interface
     * @param intent is the intent used to create the new activity
     * @param fragment is the calling fragment. This is where the result will go when the activity is done
     * @param activityCode this activity code will be returned to onActivityResult in fragment
     */
    public ActivityLoader(ActivityListener listener, Intent intent, Fragment fragment, int activityCode){
        super();
        this.listener = listener;
        this.intent = intent;
        this.fragment = fragment;
        this.activityCode = activityCode;
    }

    //These next two constructors are used if called from an activity

    /**
     * This constructor should be used if called from an activity, and no result is desired
     * @param listener is a callback interface
     * @param intent is the intent used to create the new activity
     * @param activity is the calling activity. This is where the result will go when the activity is done
     */
    public ActivityLoader(ActivityListener listener, Intent intent, Activity activity){
        this(listener, intent, activity, 0);
    }

    /**
     * This constructor should be used if called from an activity, and a result is desired
     * @param listener is a callback interface
     * @param intent is the intent used to create the new activity
     * @param activity is the calling activity. This is where the result will go when the activity is done
     * @param activityCode this activity code will be returned to onActivityResult in activity
     */
    public ActivityLoader(ActivityListener listener, Intent intent, Activity activity, int activityCode){
        super();
        this.listener = listener;
        this.intent = intent;
        this.activity = activity;
        this.activityCode = activityCode;
    }

    @Override
    protected void onPreExecute() {
        if (listener != null){
            listener.onStart();
        }
    }

    /**
     * Note that we do NOT call the callback object's methods
     * directly from the background thread, as this could result
     * in a race condition.
     */
    @Override
    protected Void doInBackground(Void... ignore) {
        if (fragment != null) {
            fragment.startActivityForResult(intent, activityCode);
            return null;
        }
        if (activity != null) {
            activity.startActivityForResult(intent, activityCode);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... ignore) {

    }

    @Override
    protected void onCancelled() {
        if (listener != null){
            listener.onCancel();
        }
    }

    @Override
    protected void onPostExecute(Void ignore) {
        if (listener != null) {
            listener.onFinish();
        }
    }
}
