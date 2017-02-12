package us.bridgeses.Minder;

import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

/**
 * Created by tbrid on 2/12/2017.
 */

public class AdHandler {
    public void initialize(Context context){
        MobileAds.initialize(context, context.getResources().getString(R.string.banner_ad_unit_id));
    }

    public void setUp(View view){
        try {
            AdView adView = (AdView) view;
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }
        catch (ClassCastException e) {
            e.printStackTrace();
        }
    }
}
