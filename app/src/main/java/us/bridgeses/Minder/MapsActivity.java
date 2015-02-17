package us.bridgeses.Minder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;

public class MapsActivity extends Activity implements GoogleMap.OnMapLongClickListener,GoogleMap.OnMapClickListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    LatLng location;
    LatLng cameraLocation;
    Circle myCircle;
	int radius=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

	    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    location = new LatLng(sharedPreferences.getFloat("Latitude",0),sharedPreferences.getFloat("Longitude",0));
	    radius = Integer.valueOf(sharedPreferences.getString("radius","0"));

        /*Intent intent = getIntent();
        Bundle incoming = intent.getExtras();
        if (incoming != null) {
            location = incoming.getParcelable("Location");
        }*/
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    public void save(View view){

	    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    SharedPreferences.Editor editor = sharedPreferences.edit();
	    editor.putFloat("Latitude",(float) location.latitude);
	    editor.putFloat("Longitude",(float) location.longitude);
	    editor.putString("radius",Integer.toString(radius));
		editor.apply();

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("Location", location);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancel(View view){
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.
     * <p>
     * If it isn't installed {@link MapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
	    int result = isGooglePlayServicesAvailable(this);
	    if (result != ConnectionResult.SUCCESS) {
		    GooglePlayServicesUtil.getErrorDialog(result,this,1);
	    }
        if ((mMap == null)&&(result == ConnectionResult.SUCCESS)) {
            // Try to obtain the map from the MapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            cameraLocation = getLocation();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(cameraLocation,14);
            mMap.setMyLocationEnabled(true);
            mMap.animateCamera(cameraUpdate);
            mMap.setOnMapLongClickListener(this);
	        mMap.setOnMapClickListener(this);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {
        if ((location.longitude != 0) || (location.latitude != 0)) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(location)   //set center
                    .radius(500)   //set radius in meters
                    .fillColor(0x400000ff)  //default
                    .strokeColor(Color.BLUE)
                    .strokeWidth(5);

            myCircle = mMap.addCircle(circleOptions);
        }
    }

	private boolean checkLocationServices(Context context){
		int locationMode = 0;
		String locationProviders;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
			try {
				locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

			} catch (Settings.SettingNotFoundException e) {
				e.printStackTrace();
			}

			return locationMode != Settings.Secure.LOCATION_MODE_OFF;

		}
		else{
			locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			return !TextUtils.isEmpty(locationProviders);
		}
	}

    private LatLng getLocation() {
        LatLng location;
	    Location lastKnown = null;
		boolean gpsEnabled = checkLocationServices(this);
	    if (gpsEnabled) {
		    LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		    Criteria criteria = new Criteria();
		    criteria.setAccuracy(Criteria.ACCURACY_FINE);
		    criteria.setPowerRequirement(Criteria.POWER_LOW);
		    lastKnown = lm.getLastKnownLocation(lm.getBestProvider(criteria, true));
	    }
        if (lastKnown != null) {
            location = new LatLng(lastKnown.getLatitude(),lastKnown.getLongitude());
        }
        else {
            location = new LatLng(0,0);
        }

        return location;
    }



    @Override
    public void onMapLongClick(final LatLng point) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		View view = getLayoutInflater().inflate(R.layout.dialog_view, null);
	    builder.setView(view);
	    final EditText radiusText = (EditText) view.findViewById(R.id.radiusText);
	    radiusText.setText(String.valueOf(radius));
	    final SeekBar radiusBar = (SeekBar) view.findViewById(R.id.seekBar1);
	    radiusBar.setProgress(radius);
	    radiusText.addTextChangedListener(new TextWatcher() {
		    public void afterTextChanged(Editable s) {
			    int i = Integer.parseInt(s.toString());
			    if (i >= 1 && i <= 1000) {
				    radiusBar.setProgress(i); // This ensures 0-120 value for seekbar
			    }
		    }

		    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		    }

		    public void onTextChanged(CharSequence s, int start, int before, int count) {
		    }
	    });
	    radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
		    public void onProgressChanged(SeekBar seekBar, int progress,
		                                  boolean fromUser) {

			    radiusText.setText("" + progress);
			    radiusText.setSelection(radiusText.getText().length());

		    }

		    public void onStartTrackingTouch(SeekBar seekBar) {

		    }

		    public void onStopTrackingTouch(SeekBar seekBar) {

		    }
	    });
	    builder.setPositiveButton(getResources().getString(R.string.ok),
			    new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			    // User clicked OK button
			    radius = Integer.valueOf(radiusText.getText().toString());
			    onMapClick(point);
		    }
	    });
	    builder.setNegativeButton(getResources().getString(R.string.edit_cancel), new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int id) {
			    // User cancelled the dialog
		    }
	    });
	    AlertDialog dialog = builder.create();
	    dialog.show();
    }

    @Override
    public void onMapClick(LatLng point) {
        location = point;
        if (myCircle != null){
            myCircle.remove();
            myCircle = null;
        }
        CircleOptions circleOptions = new CircleOptions()
                .center(point)   //set center
                .radius(radius)   //set radius in meters
                .fillColor(0x400000ff)
                .strokeColor(Color.BLUE)
                .strokeWidth(5);

        myCircle = mMap.addCircle(circleOptions);
    }
}
