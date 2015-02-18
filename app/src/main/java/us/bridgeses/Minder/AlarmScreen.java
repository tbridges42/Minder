package us.bridgeses.Minder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

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


public class AlarmScreen extends Activity implements GoogleApiClient.ConnectionCallbacks,
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

	@Override
	public void onConnectionSuspended(int i){
		Log.w("Minder","Connection Suspended");
	}

	public void onConnectionFailed(ConnectionResult result){
		Log.w("Minder","Connection Failed");
	}

	@Override
	public void onLocationChanged(Location mLastLocation) {
		Log.i("Minder","Location updated");
		stopLocationUpdates();
		LatLng location = reminder.getLocation();
		float results[] = new float[1];
		Location.distanceBetween(mLastLocation.getLatitude(),mLastLocation.getLongitude()
				,location.latitude,location.longitude,results);
		if (results[0] <= reminder.getRadius()){
			Log.i("Minder","At Location");
			createScreen();
		}
		else{
			Log.i("Minder","Not at Location");
			snooze(reminder.getSnoozeDuration());
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
			startLocationUpdates(createLocationRequest());
		}

		float results[] = new float[1];
		Location.distanceBetween(mLastLocation.getLatitude(),mLastLocation.getLongitude()
				,location.latitude,location.longitude,results);
		if (results[0] <= reminder.getRadius()){
			Log.i("Minder","At Location");
			if (reminder.getOnlyAtLocation()){
				createScreen();
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
				createScreen();
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
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(reminder.getName())
				.setContentText(reminder.getDescription())
				.setOngoing(true);

		Intent resultIntent = new Intent(this, AlarmScreen.class);
		resultIntent.putExtra("Id",reminder.getId());
		resultIntent.putExtra("Override", Boolean.TRUE);

		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent =
				PendingIntent.getActivity(
						this,
						reminder.getId(),
						resultIntent,
						PendingIntent.FLAG_UPDATE_CURRENT
				);
		mBuilder.setContentIntent(resultPendingIntent);
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr =
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.notify(reminder.getId(), mBuilder.build());
	}

	private void createScreen() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		setContentView(R.layout.activity_alarm_screen);

		TextView titleText = (TextView) findViewById(R.id.fullscreen_name);
		TextView descriptionText = (TextView) findViewById(R.id.fullscreen_description);
		titleText.setText(reminder.getName());
		descriptionText.setText(reminder.getDescription());
		if (reminder.getVibrate()) {
			if (vibrator.hasVibrator()) {
				vibrator.vibrate(1000);
			}
		}
		if (!reminder.getRingtone().equals("")) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                if (reminder.getOutLoud()) {
                    AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    Log.e("Minder", "Maxing out volume");
                    curVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
                    manager.setStreamVolume(AudioManager.STREAM_ALARM, manager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
                    curRingMode = manager.getRingerMode();
                    manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                } else
                    Log.e("Minder", "Not maxing volume");
                ringtone = RingtoneManager.getRingtone(context, Uri.parse(reminder.getRingtone()));
                ringtone.setStreamType(AudioManager.STREAM_ALARM);

            }
            else {
                if (reminder.getOutLoud()) {
                    AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    Log.e("Minder", "Maxing out volume, Lollipop");
                    curVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
                    AudioAttributes.Builder builder = new AudioAttributes.Builder();
                    builder.setUsage(AudioAttributes.USAGE_ALARM);
                    builder.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED);
                    curRingMode = manager.getRingerMode();
                    manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                } else
                    Log.e("Minder", "Not maxing volume, Lollipop");
                ringtone = RingtoneManager.getRingtone(context, Uri.parse(reminder.getRingtone()));
                ringtone.setStreamType(AudioManager.STREAM_ALARM);
            }
			ringtone.play();
		}



		scheduleTaskExecutor.schedule(new Runnable() {
			public void run() {
				snooze(reminder.getSnoozeDuration()+5*Reminder.MINUTE);
			}

		}, 5, TimeUnit.MINUTES);
	}

	protected synchronized void buildGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
		mGoogleApiClient.connect();
		Log.i("Minder","API Client built");
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    context = this.getApplicationContext();

	    scheduleTaskExecutor = Executors.newScheduledThreadPool(2);

        Intent intent = getIntent();
        int id = intent.getIntExtra("Id",-1);
	    Boolean override = intent.getBooleanExtra("Override",false);

        if (id == -1){
			Log.w("Minder","Invalid ID");
            finish();
        }

	    int snooze = intent.getIntExtra("Snooze",0);   //TODO: Implement limited number of snoozes

	    dbHelper  = ReminderDBHelper.getInstance(this);
	    SQLiteDatabase database = dbHelper.openDatabase();

        reminder = Reminder.getReminder(database,id);

        dbHelper.closeDatabase();

	    if (reminder.getVibrate()) {
		    vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
	    }

	    if (override){
		    createScreen();
	    }
	    else {

		    createNotification();

		    if (reminder.getOnlyAtLocation() || reminder.getUntilLocation()) {
			    Log.i("Minder", "Checking Location");
			    buildGoogleApiClient();
		    } else
			    createScreen();
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
				createScreen();
			}
		}
	}

	public void checkQr(){
		try {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			startActivityForResult(intent, 0);
		}
		catch (Exception e) {
			//TODO: Create confirmation dialog
			Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
			Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
			startActivity(marketIntent);

		}
	}

    public void dismiss() {
	    int id = reminder.getId();
	    Intent intentAlarm = new Intent(this, ReminderReceiver.class);      //Create alarm intent
	    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(), id, intentAlarm,
			    PendingIntent.FLAG_UPDATE_CURRENT));
	    SQLiteDatabase database = dbHelper.openDatabase();
	    reminder = Reminder.nextRepeat(database,reminder);
	    dbHelper.closeDatabase();
	    intentAlarm = new Intent(context, ReminderReceiver.class);//Create alarm intent

	    if (reminder.getActive()) {
		    intentAlarm.putExtra("Id", id);           //Associate intent with specific reminder
		    intentAlarm.putExtra("Snooze", 0);                       //This alarm has not been snoozed
		    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		    alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.getDate().getTimeInMillis(),
				    PendingIntent.getBroadcast(context, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

		    Log.v("us.bridgeses.minder", "Alarm " + id + " set");
	    }

	    // Gets an instance of the NotificationManager service
	    NotificationManager mNotifyMgr =
			    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    // Builds the notification and issues it.
	    mNotifyMgr.cancel(reminder.getId());
        scheduleTaskExecutor.shutdownNow();
        finish();
    }

	public void dismissButton(View view) {
		if (!reminder.getRingtone().equals("")) {
			ringtone.stop();
            if (reminder.getOutLoud()) {
                AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                manager.setStreamVolume(AudioManager.STREAM_ALARM, curVolume,0);
                manager.setRingerMode(curRingMode);
            }
		}
		if (reminder.getVibrate() && (vibrator != null)) {
			vibrator.cancel();
		}
		if (reminder.getNeedQr()){
			checkQr();
		}
		else
			dismiss();
	}

    public void snooze(int duration) {
	    Intent intentAlarm = new Intent(context, ReminderReceiver.class);//Create alarm intent
	    int id = reminder.getId();
	    intentAlarm.putExtra("Id", id);           //Associate intent with specific reminder
	    intentAlarm.putExtra("Snooze",snooze++);                       //Increment snooze count
	    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		    alarmManager.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + duration,
				    PendingIntent.getBroadcast(context, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

	    Log.v("us.bridgeses.minder", "Alarm " + id + " set");
        if (reminder.getVibrate() && (vibrator != null)) {
            vibrator.cancel();
        }
        if (!reminder.getRingtone().equals("")) {
            ringtone.stop();
            if (reminder.getOutLoud()) {
                AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                manager.setStreamVolume(AudioManager.STREAM_ALARM, curVolume,0);
                manager.setRingerMode(curRingMode);
            }
        }
        scheduleTaskExecutor.shutdownNow();
        finish();
    }

    public void snoozeButton(View view) {
        snooze(reminder.getSnoozeDuration());
    }
}
