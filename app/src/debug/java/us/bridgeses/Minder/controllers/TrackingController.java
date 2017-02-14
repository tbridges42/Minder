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

    private static TrackingController singletonInstance;

    public static TrackingController getInstance(Context context) {
        if (singletonInstance == null) {
            singletonInstance = new TrackingController();
        }
        return singletonInstance;
    }

    public void sendEvent(String category, String action, String label, long value) {

    }

    public void sendException(String description, boolean fatal) {

    }
}
