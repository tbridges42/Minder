package us.bridgeses.Minder.controllers;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import us.bridgeses.Minder.Minder;

/**
 * Created by tbrid on 2/12/2017.
 */

public class TrackingController {

    // Google assigned UUID
    private static final String PROPERTY_ID = "UA-59294502-2";

    private static TrackingController singletonInstance;

    private Tracker tracker;

    private TrackingController(Tracker tracker) {
        this.tracker = tracker;
    }

    public static TrackingController getInstance(Context context) {
        if (singletonInstance == null) {
            Tracker tracker = GoogleAnalytics.getInstance(context).newTracker(PROPERTY_ID);
            tracker.enableAutoActivityTracking(true);
            tracker.enableExceptionReporting(true);
            tracker.setAnonymizeIp(true);
            tracker.setUseSecure(true);
            singletonInstance = new TrackingController(tracker);
        }
        return singletonInstance;
    }

    public void sendEvent(String category, String action, String label, long value) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    public void sendException(String description, boolean fatal) {
        tracker.send(new HitBuilders.ExceptionBuilder()
                    .setDescription(description)
                    .setFatal(fatal)
                    .build());
    }
}
