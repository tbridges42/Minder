package us.bridgeses.Minder.controllers;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import us.bridgeses.Minder.Minder;

/**
 * Created by tbrid on 2/12/2017.
 */

public class TrackingController extends Fragment {

    private static final String SCREEN_NAME_KEY = "us.bridgeses.Minder.screen_name";

    public static TrackingController getInstance(String screenName) {
        TrackingController trackingController = new TrackingController();
        Bundle args = new Bundle();
        args.putString(SCREEN_NAME_KEY, screenName);
        trackingController.setArguments(args);
        return trackingController;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // This fragment has no UI. Do not attempt to inflate it
        return null;
    }

    public void sendEvent(String category, String action, String label, long value) {
        Minder minder = (Minder) getActivity().getApplicationContext();
        Tracker t = minder.getTracker(Minder.TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }
}
