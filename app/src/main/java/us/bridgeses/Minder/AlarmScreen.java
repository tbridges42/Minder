package us.bridgeses.Minder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import us.bridgeses.Minder.receivers.ReminderReceiver;


public class AlarmScreen extends Activity implements View.OnLongClickListener, GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private ReminderDBHelper dbHelper;
    private Reminder reminder;
    private Ringtone ringtone;
    private Vibrator vibrator;
    private ScheduledExecutorService scheduleTaskExecutor;
	private Context context;
	private int snooze;
	private GoogleApiClient mGoogleApiClient;
    private int curVolume;
    private int curRingMode;
    private boolean hasLocation;
    private boolean hasWiFi;
    private boolean hasBT;
    private boolean hasWake;


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

	@Override
	public void onConnectionSuspended(int i){
		Log.w("Minder","Connection Suspended");
	}

	public void onConnectionFailed(ConnectionResult result){
		Log.w("Minder","Connection Failed");
	}

    private void silence() {
        if (!reminder.getRingtone().equals("")) {
            ringtone.stop();
            if (reminder.getVolumeOverride()) {
                AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                manager.setStreamVolume(AudioManager.STREAM_ALARM, curVolume,0);
                manager.setRingerMode(curRingMode);
            }
        }
        if (reminder.getVibrate() && (vibrator != null)) {
            vibrator.cancel();
        }
    }

    private void makeNoise() {
        if (reminder.getVibrate()) {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }
        if (reminder.getVibrate()) {
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(1000);
            }
        }
        if (!reminder.getRingtone().equals("")) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (reminder.getVolumeOverride()) {
                    AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    Log.d("Minder", "Maxing out volume");
                    curVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
                    manager.setStreamVolume(AudioManager.STREAM_ALARM, manager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
                    curRingMode = manager.getRingerMode();
                    manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                } else
                    Log.d("Minder", "Not maxing volume");
                ringtone = RingtoneManager.getRingtone(context, Uri.parse(reminder.getRingtone()));
                ringtone.setStreamType(AudioManager.STREAM_ALARM);

            }
            else {
                if (reminder.getVolumeOverride()) {
                    AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    Log.d("Minder", "Maxing out volume, Lollipop");
                    curVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
                    AudioAttributes.Builder builder = new AudioAttributes.Builder();
                    builder.setUsage(AudioAttributes.USAGE_ALARM);
                    builder.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED);
                    curRingMode = manager.getRingerMode();
                    manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                } else
                    Log.d("Minder", "Not maxing volume, Lollipop");
                ringtone = RingtoneManager.getRingtone(context, Uri.parse(reminder.getRingtone()));
            }
            ringtone.play();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //silence();
    }

	@Override
	public void onLocationChanged(Location mLastLocation) {
		Log.i("Minder","Location updated");
		LatLng location = reminder.getLocation();
		float results[] = new float[1];
		Location.distanceBetween(mLastLocation.getLatitude(),mLastLocation.getLongitude()
				,location.latitude,location.longitude,results);
		if (results[0] <= reminder.getRadius()){
			Log.i("Minder","At Location");
			hasLocation = true;
            stopLocationUpdates();
            if (checkConditions()){
                createNotification();
                makeNoise();
                createScreen();
            }
		}
		else{
			Log.i("Minder","Not at Location");
		}
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
		LocationRequest mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);
		mLocationRequest.setFastestInterval(5000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		return mLocationRequest;
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i("Minder","Connected to location service");
		Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
				mGoogleApiClient);
		LatLng location = reminder.getLocation();

		long timeDelta = Calendar.getInstance().getTimeInMillis() - mLastLocation.getTime();

		if ((mLastLocation == null) ||  (timeDelta > (Reminder.MINUTE * 10))
				||mLastLocation.getAccuracy() > 1.5*reminder.getRadius()){
			Log.i("Minder","Location is stale");
			try {
                startLocationUpdates(createLocationRequest());
            }
            catch (Exception e){
                Log.e("Minder","Unable to start location updates");
            }
		}

		float results[] = new float[1];
		Location.distanceBetween(mLastLocation.getLatitude(),mLastLocation.getLongitude()
				,location.latitude,location.longitude,results);
		if (results[0] <= reminder.getRadius()){
			Log.i("Minder","At Location");
			if (reminder.getOnlyAtLocation()){
				hasLocation = true;
                if (checkConditions()){
                    createNotification();
                    makeNoise();
                    createScreen();
                    //int duration = Toast.LENGTH_SHORT;
                    //Toast toast = Toast.makeText(this, "Hmm", duration);
                    //toast.show();
                }
			}
			else {
				if (reminder.getUntilLocation()) {
					Log.i("Minder","Not at Location");
					snooze(reminder.getSnoozeDuration());
				}
			}
		}
		else{
			if (reminder.getUntilLocation()){
                hasLocation = true;
                if (checkConditions()){
                    createNotification();
                    makeNoise();
                    createScreen();
                }
			}
			else {
				if (reminder.getOnlyAtLocation()) {
					Log.i("Minder","Not at Location");
					snooze(reminder.getSnoozeDuration());
				}
			}
		}
	}

	private void createNotification() {

		Intent resultIntent = new Intent(this, AlarmScreen.class);
		resultIntent.putExtra("Id",reminder.getId());
		resultIntent.putExtra("Override", true);

		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent =
				PendingIntent.getActivity(
						this,
						0,
						resultIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
				);

        Intent dismissIntent = new Intent(this, AlarmScreen.class);
        dismissIntent.putExtra("Id",reminder.getId());
        dismissIntent.putExtra("Dismiss",true);

        PendingIntent dismissPendingIntent =
                PendingIntent.getActivity(
                        this,
                        1,
                        dismissIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(reminder.getName())
                        .setContentText(Integer.toString(reminder.getId()))
                        .setOngoing(true)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setCategory(Notification.CATEGORY_ALARM)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(reminder.getDescription()))
                        .addAction(R.drawable.ic_stat_content_clear, "Dismiss", dismissPendingIntent);

		mBuilder.setContentIntent(resultPendingIntent);
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr =
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(reminder.getId(), mBuilder.build());
	}

    @Override
    public boolean onLongClick(View view){
        customSnooze(view);
        return true;
    }

	private void createScreen() {
        if (reminder.getDisplayScreen()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            setContentView(R.layout.activity_alarm_screen);

            TextView titleText = (TextView) findViewById(R.id.fullscreen_name);
            TextView descriptionText = (TextView) findViewById(R.id.fullscreen_description);
            titleText.setText(reminder.getName());
            descriptionText.setText(reminder.getDescription());

            findViewById(R.id.snooze_button).setOnLongClickListener(this);

            if (scheduleTaskExecutor != null){
                scheduleTaskExecutor.schedule(new Runnable() {
                    public void run() {
                        snooze(reminder.getSnoozeDuration());
                    }

                }, 5, TimeUnit.MINUTES);
            }
        }
	}

	protected synchronized void buildGoogleApiClient() {
		try {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            Log.i("Minder","API Client built");
        }
        catch (Exception e) {
            Log.e("Minder","API Client failed to build");
        }
	}

    private Boolean checkWake(){
        if (!reminder.getWakeUp()){
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (Build.VERSION.SDK_INT >= 20){
                if (!pm.isInteractive()){
                    snooze(reminder.getSnoozeDuration());
                    finish();
                    return false;
                }
            }
            else {
                if (!pm.isScreenOn()){
                    snooze(reminder.getSnoozeDuration());
                    finish();
                    return false;
                }
            }
        }
        return true;
    }

    private void checkWifi(){
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()){
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String curSSID = wifiInfo.getSSID();
            curSSID = curSSID.replaceAll("^\"|\"$", "");
            if (curSSID.equals(reminder.getSSID())){
                hasWiFi = true;
                if (checkConditions()){
                    createNotification();
                    makeNoise();
                    createScreen();
                }
            }
        }
        else {
            snooze(reminder.getSnoozeDuration()); //TODO: Create wifilistener
        }
    }

    private boolean processIntent(Intent intent){
        int snoozeNum = intent.getIntExtra("Snooze",0);   //TODO: Implement limited number of snoozes
        int id = intent.getIntExtra("Id", -1);
        Log.d("Minder",Integer.toString(id));
        if (id == -1){
            Log.w("Minder","Invalid ID");
            finish();
            return true;
        }
        dbHelper  = ReminderDBHelper.getInstance(this);
        SQLiteDatabase database = dbHelper.openDatabase();
        reminder = Reminder.getReminder(database,id);
        dbHelper.closeDatabase();

        Boolean dismiss = intent.getBooleanExtra("Dismiss",false);
        Boolean override = intent.getBooleanExtra("Override",false);
        if (dismiss) {
            dismiss();
            return true;
        }
        if (override){
            makeNoise();
            createScreen();
            return true;
        }
        return false;
    }

	@Override
	protected void onNewIntent(Intent intent){
        processIntent(intent);
	}

	@Override
	protected void onResume(){
		super.onResume();
	}

    private boolean checkConditions(){
        if (!hasLocation){
            buildGoogleApiClient();
            return false;
        }
        if (!hasWiFi){
            checkWifi();
            return false;
        }
        if (!hasWake){
            checkWake();
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    context = this.getApplicationContext();

        Intent intent = getIntent();

        if (!processIntent(intent)) {

            scheduleTaskExecutor = Executors.newScheduledThreadPool(2);

            hasLocation = !((reminder.getOnlyAtLocation()) || (reminder.getUntilLocation()));
            hasWiFi = !reminder.getNeedWifi();
            hasBT = !reminder.getNeedBluetooth();
            hasWake = reminder.getWakeUp();

            if (checkConditions()) {
                createNotification();
                makeNoise();
                createScreen();
            }
        }
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode,resultCode,data);
		if (requestCode == 0) {

			if (resultCode == Activity.RESULT_OK) {
				dismiss();
			}
			if(resultCode == Activity.RESULT_CANCELED){
				makeNoise();
                createScreen();
			}
		}
	}

	private void checkQr(){
		try {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			startActivityForResult(intent, 0);
		}
		catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("No QR scanner");
            builder.setMessage("This function requires the ZXing Bar Code Scanner");
            builder.setPositiveButton("Play Store", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
                    startActivity(marketIntent);
                }
            });
            builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            builder.create();
            builder.show();
		}
	}

    private void dismiss() {
	    int id = reminder.getId();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.cancel(id);
	    Intent intentAlarm = new Intent(this, ReminderReceiver.class);      //Create alarm intent
	    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(),
		        id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
	    SQLiteDatabase database = dbHelper.openDatabase();
	    reminder = Reminder.nextRepeat(database,reminder);
	    dbHelper.closeDatabase();


	    if ((reminder.getActive()) && (reminder.getId() != -1)) {
		    int alarmType;
		    if (reminder.getWakeUp()){
			    alarmType = AlarmManager.RTC_WAKEUP;
		    }
		    else {
			    alarmType = AlarmManager.RTC;
		    }
            intentAlarm = new Intent(context, ReminderReceiver.class);//Create alarm intent
            intentAlarm.putExtra("Id", id);           //Associate intent with specific Reminder
		    intentAlarm.putExtra("Snooze", 0);                       //This alarm has not been snoozed
		    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		    alarmManager.set(alarmType, reminder.getDate().getTimeInMillis(),
				    PendingIntent.getBroadcast(context, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

		    Log.v("us.bridgeses.minder", "Alarm " + id + " set");
	    }
        if (scheduleTaskExecutor != null) {
            scheduleTaskExecutor.shutdownNow();
        }
        finish();
    }

	public void dismissButton(View view) {
		silence();
		if (reminder.getNeedQr()){
			checkQr();
		}
		else
			dismiss();
	}

    private void snooze(int duration) {
	    Intent intentAlarm = new Intent(context, ReminderReceiver.class);//Create alarm intent
	    int id = reminder.getId();
	    intentAlarm.putExtra("Id", id);           //Associate intent with specific reminder
	    intentAlarm.putExtra("Snooze",snooze++);                       //Increment snooze count
	    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int alarmType;
        if (reminder.getWakeUp()){
            alarmType = AlarmManager.RTC_WAKEUP;
        }
        else {
            alarmType = AlarmManager.RTC;
        }
        alarmManager.set(alarmType, Calendar.getInstance().getTimeInMillis()+duration,
                PendingIntent.getBroadcast(getApplicationContext(), id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
	    Log.v("us.bridgeses.minder", "Alarm " + id + " set");
        silence();
        if (scheduleTaskExecutor != null){
            scheduleTaskExecutor.shutdownNow();
        }
        finish();
    }

    private void customSnooze(View view) {
        silence();
        final View dLayout = View.inflate(this, R.layout.snooze_dialog,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Set Snooze Duration");
        final NumberPicker picker = (NumberPicker) dLayout.findViewById(R.id.snooze_length);
        picker.setMinValue(1);
        picker.setMaxValue(480);
        picker.setValue(reminder.getSnoozeDuration()/Reminder.MINUTE);
        builder.setPositiveButton("Snooze", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                snooze(picker.getValue()*Reminder.MINUTE);
            }
        });
        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                makeNoise();
            }
        });
        builder.setView(dLayout);
        builder.create();
        builder.show();
    }

    public void snoozeButton(View view) {
        snooze(reminder.getSnoozeDuration());
    }
}
