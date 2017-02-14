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

	@Override
	public void onCreate(){
		super.onCreate();
        //Initialize Logger for better logging
		Logger.init("MinderLog");
	}
}
