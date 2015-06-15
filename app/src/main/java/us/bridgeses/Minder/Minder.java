package us.bridgeses.Minder;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.orhanobut.logger.Logger;

import java.util.HashMap;

/**
 * An extension of Application used by this app to enable things like analytic tracking
 * Created by Tony on 2/2/2015.
 */
public class Minder extends Application {

    // Google assigned UUID
	private static final String PROPERTY_ID = "UA-59294502-1";

    Context appContext;

	/**
	 * Enum used to identify the tracker that needs to be used for tracking.
	 *
	 * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
	 * storing them all in Application object helps ensure that they are created only once per
	 * application instance.
	 */
	public enum TrackerName {
		APP_TRACKER, // Tracker used only in this app.
		GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
		ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
	}

	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    /**
     * Get a Google Analytics tracker
     * @param trackerId The TrackerName you want
     * @return The requested tracker
     */
	synchronized Tracker getTracker(TrackerName trackerId) {
		if (!mTrackers.containsKey(trackerId)) {

			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
					: (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(PROPERTY_ID)
					: analytics.newTracker(PROPERTY_ID);
			mTrackers.put(trackerId, t);

		}
		return mTrackers.get(trackerId);
	}

    /**
     * A globally available application context
     * @return this context
     */
    synchronized Context getAppContext() {
        return appContext;
    }

	@Override
	public void onCreate(){
		super.onCreate();
        appContext = this;
        //Initialize Logger for better logging
		Logger.init("MinderLog");
	}
}
