package us.bridgeses.Minder;

import android.app.Fragment;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;

/**
 * Created by tbrid on 2/12/2017.
 */
public class AdHandler extends Fragment {

    private static final String TAG = "AdHandler";

    private AdView adView;
    private IInAppBillingService billingService;
    private boolean adFree = false;

    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
            billingService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            billingService = IInAppBillingService.Stub.asInterface(service);
            checkForPurchaseOnline(null);
        }
    };

    private void checkForPurchaseOnline(String continuationToken) {
        try {
            Bundle ownedItems = billingService.getPurchases(3,
                    getActivity().getPackageName(), "inapp", continuationToken);
            int response = ownedItems.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> ownedSkus =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String>  purchaseDataList =
                        ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                ArrayList<String>  signatureList =
                        ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                continuationToken =
                        ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    // TODO: 3/5/2017 Verify signature
                    String signature = signatureList.get(i);
                    String sku = ownedSkus.get(i);

                    if (sku.equals("ad_free")) {
                        setAdFree(true);
                        cacheAdFree();
                        return;
                    }
                }

                if (continuationToken != null) {
                    checkForPurchaseOnline(continuationToken);
                }
            }
        }
        catch (RemoteException e) {
            Log.e(TAG, "checkForPurchaseOnline: Failed to connect to purchase server", e);
        }
    }

    private void cacheAdFree() {
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putBoolean("ad_free", true);
        editor.apply();
    }

    private void checkForPurchaseCached() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferences.getBoolean("ad_free", false)) {
            setAdFree(true);
        }
    }

    public void setAdFree(boolean adFree) {
        this.adFree = adFree;
        if (adFree && adView != null) {
            adView.setVisibility(View.GONE);
            adView.destroy();
        }
    }

    public void initialize(Context context){
        MobileAds.initialize(context, context.getResources().getString(R.string.banner_ad_unit_id));
    }

    public void setUp(){
        try {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    adView.setVisibility(View.GONE);
                }
            });
            adView.loadAd(adRequest);
        }
        catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: 3/5/2017 Handle internet connection issues
        // TODO: 3/5/2017 Take off of main thread
        checkForPurchaseCached();
        if (!adFree) {
            Intent serviceIntent =
                    new Intent("com.android.vending.billing.InAppBillingService.BIND");
            serviceIntent.setPackage("com.android.vending");
            getActivity().bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
            initialize(getActivity());
            setUp();
            setHasOptionsMenu(true);
        }
        else {
            adView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_ads, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: " + item.getTitle());
        if (item.getItemId() == R.id.action_buy_ad_free) {
            launchBuyAdFree();
        }
        return super.onOptionsItemSelected(item);
    }

    private void launchBuyAdFree() {
        Log.d(TAG, "launchBuyAdFree: ");
        if (billingService != null) {
            try {
                Log.d(TAG, "launchBuyAdFree: ");
                Bundle buyIntentBundle = billingService.getBuyIntent(3, getActivity().getPackageName(),
                        "ad_free", "inapp", null);
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                getActivity().startIntentSenderForResult(pendingIntent.getIntentSender(),
                        1001, new Intent(), 0, 0,
                        0);
            }
            catch (RemoteException|IntentSender.SendIntentException e) {
                Log.e(TAG, "launchBuyAdFree: Remote Exception", e);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.banner, container, false);
        adView = (AdView)view.findViewById(R.id.adView);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        adView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        adView.resume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        adView.destroy();
        Log.d(TAG, "onPause: ");
        getActivity().unbindService(mServiceConn);
        mServiceConn = null;
        super.onDestroy();
    }
}
