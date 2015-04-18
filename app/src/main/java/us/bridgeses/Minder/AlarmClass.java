package us.bridgeses.Minder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.logger.Logger;

import java.util.Calendar;

import us.bridgeses.Minder.receivers.ReminderReceiver;
import us.bridgeses.Minder.util.RingtoneService;

/**
 * Created by Laura on 3/1/2015.
 */
public class AlarmClass implements Runnable, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private int id = -1;
    private int snoozeNum = 0;
    private Context context;
    private Reminder reminder;
	private boolean dismiss = false;
    private boolean hasLocation;
    private boolean hasWiFi;
    private boolean hasBT;
    private boolean urgentRequest;
    private WifiReceiver wifiReceiver;
    private GoogleApiClient mGoogleApiClient;
	private int curVolume;
	private int curRingMode;
	private Ringtone ringtone;


    public AlarmClass(Context context, int id){
		this.context = context;
		this.id = id;
	}

	public AlarmClass(Context context, int id, boolean dismiss){
		this.context = context;
		this.id = id;
		this.dismiss = dismiss;
	}

    public AlarmClass(Context context, int id, int snoozeNum){
		this.context = context;
		this.id = id;
		this.snoozeNum = snoozeNum;
	}

    public AlarmClass(){
    }

	private void createNotification() {

		Intent resultIntent = new Intent(context, AlarmScreen.class);
		resultIntent.putExtra("Id",reminder.getId());

		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent =
				PendingIntent.getActivity(
						context,
						0,
						resultIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
				);

		Intent dismissIntent = new Intent(context, ReminderReceiver.class);
		dismissIntent.putExtra("Id",reminder.getId());
		dismissIntent.putExtra("Dismiss",true);

		PendingIntent dismissPendingIntent =
				PendingIntent.getBroadcast(
						context,
						1,
						dismissIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
				);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(context)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle(reminder.getName())
						.setContentText(reminder.getDescription())
						.setOngoing(true)
						.setPriority(Notification.PRIORITY_MAX)
						.setStyle(new NotificationCompat.BigTextStyle()
								.bigText(reminder.getDescription()))
						.addAction(R.drawable.ic_stat_content_clear, "Dismiss", dismissPendingIntent);

		if (Build.VERSION.SDK_INT >= 21){
			mBuilder.setCategory(Notification.CATEGORY_ALARM);
		}

		mBuilder.setContentIntent(resultPendingIntent);
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(reminder.getId(), mBuilder.build());
	}

	private void makeNoise() {
		Vibrator vibrator;
		Logger.d("In makeNoise");
		if (reminder.getVibrate()) {
			vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			if (vibrator.hasVibrator()) {
				vibrator.vibrate(1000);
			}
		}
		if (!reminder.getRingtone().equals("")) {
			Logger.d("Starting ringtone");
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				if (reminder.getVolumeOverride()) {
					AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
					Logger.d("Maxing out volume");
					curVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
					manager.setStreamVolume(AudioManager.STREAM_ALARM, manager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
					curRingMode = manager.getRingerMode();
					manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				} else
					Logger.d("Not maxing volume");

			}
			else {
				if (reminder.getVolumeOverride()) {
					AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
					Logger.d("Maxing out volume, Lollipop");
					curVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
					AudioAttributes.Builder builder = new AudioAttributes.Builder();
					builder.setUsage(AudioAttributes.USAGE_ALARM);
					builder.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED);
					curRingMode = manager.getRingerMode();
					manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				} else
					Logger.d("Not maxing volume, Lollipop");
			}
			Intent startIntent = new Intent(context, RingtoneService.class);
			startIntent.putExtra("ringtone-uri", reminder.getRingtone());
			startIntent.putExtra("Start",true);
			context.startService(startIntent);
		}
	}

    private void retrieveReminder(){
        if (id == -1){
            Logger.w("Invalid ID");
            return;
        }
        if (context == null){
            Logger.e("Invalid context");
            return;
        }
        ReminderDBHelper dbHelper  = ReminderDBHelper.getInstance(context);
        SQLiteDatabase database = dbHelper.openDatabase();
        reminder = Reminder.getReminder(database,id);
        dbHelper.closeDatabase();
    }

    private void alarm(){
        Intent intent = new Intent(context,AlarmScreen.class);
        intent.putExtra("Id",id);
        intent.putExtra("SnoozeNum",snoozeNum);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.putExtra("Dismiss",dismiss);
	    Logger.d(Boolean.toString(dismiss));
        context.startActivity(intent);
    }

    protected void startLocationUpdates(LocationRequest mLocationRequest) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    protected LocationRequest createLocationRequest() {
        urgentRequest = false;
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Reminder.MINUTE*15);
        mLocationRequest.setFastestInterval(Reminder.MINUTE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setSmallestDisplacement(reminder.getRadius());
        return mLocationRequest;
    }

    protected LocationRequest createUrgentLocationRequest() {
        urgentRequest = true;
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void snooze(int duration){
        Intent intentAlarm = new Intent(context, ReminderReceiver.class);//Create alarm intent
        int id = reminder.getId();
        intentAlarm.putExtra("Id", id);           //Associate intent with specific reminder
        intentAlarm.putExtra("Snooze",snoozeNum);                       //Increment snooze count
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int alarmType;
        if (reminder.getWakeUp()){
            alarmType = AlarmManager.RTC_WAKEUP;
        }
        else {
            alarmType = AlarmManager.RTC;
        }
        alarmManager.set(alarmType, Calendar.getInstance().getTimeInMillis()+duration,
                PendingIntent.getBroadcast(context, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    public void onLocationChanged(Location mLastLocation) {
        Logger.i("Location updated");
        LatLng location = reminder.getLocation();
        float results[] = new float[1];
        Location.distanceBetween(mLastLocation.getLatitude(),mLastLocation.getLongitude()
                ,location.latitude,location.longitude,results);
        if (results[0] <= reminder.getRadius()){
            Logger.i("At Location");
            if (reminder.getOnlyAtLocation()) {
                Logger.i("Supposed to be at location");
                hasLocation = true;
                stopLocationUpdates();
                checkConditions();
            }
            else {
                Logger.i("Not supposed to be at location");
                snooze(Reminder.MINUTE);
            }
        }
        else{
            Logger.i("Not at Location");
            if (reminder.getUntilLocation()){
                Logger.i("Not supposed to be at location");
                hasLocation = true;
                stopLocationUpdates();
                checkConditions();
            }
            else {
                snooze(Reminder.MINUTE);
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Logger.i("Connected to location service");
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        LatLng location = reminder.getLocation();

        long timeDelta = Calendar.getInstance().getTimeInMillis() - mLastLocation.getTime();

        if ((mLastLocation == null) ||  (timeDelta > (Reminder.MINUTE * 10))
                ||mLastLocation.getAccuracy() > 1.5*reminder.getRadius()){
            Logger.i("Location is stale");
            try {
                startLocationUpdates(createUrgentLocationRequest());
            }
            catch (Exception e){
                Logger.e("Unable to start location updates");
            }
        }

        float results[] = new float[1];
        Location.distanceBetween(mLastLocation.getLatitude(),mLastLocation.getLongitude()
                ,location.latitude,location.longitude,results);
        if (results[0] <= reminder.getRadius()){
            Logger.i("At Location");
            if (reminder.getOnlyAtLocation()){
                Logger.i("Supposed to be at location");
                hasLocation = true;
                checkConditions();
            }
            else {
                if (reminder.getUntilLocation()) {
                    Logger.i("Not supposed to be at Location");
                    try {
                        snooze(Reminder.MINUTE);
                    }
                    catch (Exception e){
                        Logger.e("Unable to start location updates");
                    }
                }
            }
        }
        else{
            Logger.i("Not at location");
            if (reminder.getUntilLocation()){
                Logger.i("Not supposed to be at location");
                hasLocation = true;
                checkConditions();
            }
            else {
                if (reminder.getOnlyAtLocation()) {
                    Logger.i("Supposed to be at location");
                    try {
                        snooze(reminder.getSnoozeDuration());
                    }
                    catch (Exception e){
                        Logger.e("Unable to start location updates");
                    }
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i){
        Logger.w("Connection Suspended");
    }

    public void onConnectionFailed(ConnectionResult result){
        Logger.w("Connection Failed");
    }

    private void checkLocation(){
        Logger.v("Checking for location");
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            Logger.i("API Client built");
        }
        catch (Exception e) {
            Logger.e("API Client failed to build");
        }
    }

    private void checkWifi(){
        Logger.v("Checking Wifi");
        if (wifiReceiver != null){
            context.unregisterReceiver(wifiReceiver);
            wifiReceiver = null;
        }
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()){
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String curSSID = wifiInfo.getSSID();
            curSSID = curSSID.replaceAll("^\"|\"$", "");
            if (curSSID.equals(reminder.getSSID())){
                hasWiFi = true;
                checkConditions();
            }
        }
        else {
            snooze(Reminder.MINUTE);
        }
    }

    private void checkBT(){
        hasBT = true;
        checkConditions();
    }

    private void checkConditions(){
        if (hasLocation && hasBT && hasWiFi){
            if (reminder.getDisplayScreen()){
	            alarm();
            }
	        else{
	            makeNoise();
	            createNotification();
            }
        }
        else {
            if (!hasLocation) {
                checkLocation();
            }
            if (!hasWiFi) {
                checkWifi();
            }
            if (!hasBT) {
                checkBT();
            }
        }
    }

    private void initConditions(){
        hasLocation = !((reminder.getOnlyAtLocation()) || (reminder.getUntilLocation()));
        hasWiFi = !reminder.getNeedWifi();
        hasBT = !reminder.getNeedBluetooth();
    }

    public void run(){
        Logger.v("Reminder fired");
        retrieveReminder();
	    if (dismiss){
		    Intent stopIntent = new Intent(context, RingtoneService.class);
		    context.stopService(stopIntent);
		    alarm();
	    }
	    else {
		    initConditions();
		    checkConditions();
	    }
    }

    class WifiReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null){
                if (wifiInfo.getSSID() != null){
                    Logger.i("Connected to "+wifiInfo.getSSID());
                    checkConditions();
                }
                else{
                    Logger.i("No SSID");
                }
            }
            else {
                Logger.i("No wifi info");
            }
        }
    }
}
