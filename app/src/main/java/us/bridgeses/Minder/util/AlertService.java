package us.bridgeses.Minder.util;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;

import com.orhanobut.logger.Logger;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import us.bridgeses.Minder.receivers.ReminderReceiver;

/**
 * Created by Tony on 4/14/2015.
 */
public class AlertService extends Service {
	Hashtable<Integer,Ringtone> ringtoneHash = new Hashtable<Integer,Ringtone>();
	private Vibrator vibrator;
	private int currVolume = -1;
	private int currRingMode = -1;
	private ScheduledExecutorService scheduleTaskExecutor;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@SuppressWarnings("deprecation")
	private void overrideVolumeCompat(int volume){
		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		Logger.d("Maxing out volume");
		currVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
		manager.setStreamVolume(AudioManager.STREAM_ALARM, (int) Math.round(manager
				.getStreamMaxVolume(AudioManager.STREAM_ALARM) * volume / 100), 0);
		Logger.d("Ringing at "+Integer.toString(volume));
		currRingMode = manager.getRingerMode();
		manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}

	@TargetApi(21)
	private void overrideVolume(int volume){
		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		Logger.d("Maxing out volume, Lollipop");
		currVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
		manager.setStreamVolume(AudioManager.STREAM_ALARM, (int) Math.round(manager
				.getStreamMaxVolume(AudioManager.STREAM_ALARM) * volume / 100), 0);
		AudioAttributes.Builder builder = new AudioAttributes.Builder();
		builder.setUsage(AudioAttributes.USAGE_ALARM);
		builder.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED);
		currRingMode = manager.getRingerMode();
		manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}

	private void restoreVolume(){
		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		manager.setStreamVolume(AudioManager.STREAM_ALARM, currVolume, 0);
		manager.setRingerMode(currRingMode);
		currVolume = -1;
		currRingMode = -1;
	}

	private void startRingtone(Uri ringtoneUri, boolean override, int volume, int id){
		Ringtone ringtone;
		Logger.d("Starting ringtone "+id);
		if (ringtoneHash.containsKey(id)){
			stopRingtone(id);
		}

		ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
		ringtoneHash.put(id,ringtone);

		if (override){
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				overrideVolumeCompat(volume);
				ringtone.setStreamType(AudioManager.STREAM_ALARM);
			}
			else {
				overrideVolume(volume);
			}
		}
		ringtone.play();
	}

	private void stopRingtone(int id){
		Ringtone ringtone = ringtoneHash.get(id);
		Logger.d("Stopping ringtone "+id);
		if (ringtone != null) {
			Logger.d("Stopping Ringtone");
			ringtone.stop();
		}
		if (currVolume != -1){
			restoreVolume();
		}
		ringtoneHash.remove(id);
	}

	private void stopAllRingtones(){
		Set<Integer> keys = ringtoneHash.keySet();
		for (Integer key : keys){
			stopRingtone(key);
		}
		ringtoneHash = new Hashtable<Integer,Ringtone>();
	}

	private void stopVibrate(){
		Logger.d("Stopping Vibrate");
		if (vibrator != null) {
			vibrator.cancel();
		}
	}

	private void startVibrate(byte pattern,boolean repeat){
		this.vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		long vibratePattern[] = new long[1];
		int repeatIndex = -1;
		switch (pattern) {
			case 1: {
				vibratePattern = new long[2];
				vibratePattern[0] = 1000;
				vibratePattern[1] = 2000;
				if (repeat){
					repeatIndex = 0;
				}
				break;
			}
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			vibrator.vibrate(vibratePattern, repeatIndex);
		}
		else {
			vibrator.vibrate(vibratePattern, repeatIndex, new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());
		}
	}

	private void startTimer(int id, int duration, boolean wakeUp, int snoozeNum){
		SnoozeTimer timer = new SnoozeTimer(id,duration,wakeUp,snoozeNum);
		scheduleTaskExecutor = Executors.newScheduledThreadPool(2);
		scheduleTaskExecutor.schedule(timer,5, TimeUnit.MINUTES);
	}

	private void stopTimer(){
		if (scheduleTaskExecutor != null) {
			scheduleTaskExecutor.shutdownNow();
		}
	}

	private void snooze(int id, int duration, boolean wakeUp, int snoozeNum){
		Logger.d("Snoozing: "+snoozeNum);
		Context context = getApplicationContext();
		Intent intentAlarm = new Intent(context, ReminderReceiver.class);//Create alarm intent
		intentAlarm.putExtra("Id", id);           //Associate intent with specific reminder
		intentAlarm.putExtra("SnoozeNum",snoozeNum);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		int alarmType;
		if (wakeUp){
			alarmType = AlarmManager.RTC_WAKEUP;
		}
		else {
			alarmType = AlarmManager.RTC;
		}
		alarmManager.set(alarmType, Calendar.getInstance().getTimeInMillis() + duration,
				PendingIntent.getBroadcast(context, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		if (scheduleTaskExecutor != null) {
			scheduleTaskExecutor.shutdownNow();
		}
		stopRingtone(id);
		stopVibrate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		int id = intent.getIntExtra("Id",-1);
		if (id != -1) {
			boolean startRingtone = intent.getBooleanExtra("StartRingtone", false);
			boolean startVibrate = intent.getBooleanExtra("StartVibrate", false);
			boolean override = intent.getBooleanExtra("Override",false);
			boolean insist = intent.getBooleanExtra("Insistent",false);
			boolean snooze = intent.getBooleanExtra("Snooze",false);
			boolean wakeUp = intent.getBooleanExtra("WakeUp",false);
			int snoozeNum = intent.getIntExtra("SnoozeNum",0);
			Logger.d("Received snoozeNum:" + snoozeNum);
			int duration = intent.getIntExtra("Duration",5);
			int volume = intent.getIntExtra("Volume",80);
			Logger.d("Snooze: "+snooze);
			if (!snooze) {
				if (insist) {
					startTimer(id, duration, wakeUp, snoozeNum);
				} else {
					stopTimer();
				}
				if (startRingtone) {
					Logger.d("Starting ringtone service");
					Uri ringtoneUri = Uri.parse(intent.getStringExtra("ringtone-uri"));
					startRingtone(ringtoneUri, override, volume, id);
				} else {
					stopRingtone(id);
				}
				if (startVibrate) {
					Logger.d("Starting vibrate service");
					byte pattern = (byte) intent.getIntExtra("VibratePattern", 1);
					boolean repeat = intent.getBooleanExtra("VibrateRepeat", false);
					startVibrate(pattern, repeat);
				} else {
					stopVibrate();
				}
			}
			else{
				snooze(id,duration,wakeUp,snoozeNum);
			}
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy()
	{

		stopAllRingtones();

		stopVibrate();
		super.onDestroy();
	}

	protected class SnoozeTimer implements Runnable{

		int id;
		int duration;
		int snoozeNum;
		boolean wakeUp;

		public SnoozeTimer(int id, int duration, boolean wakeUp, int snoozeNum){
			this.id = id;
			this.duration = duration;
			this.wakeUp = wakeUp;
			this.snoozeNum = snoozeNum;
			Logger.d("SnoozeTimer created");
		}

		@Override
		public void run() {
			Logger.d("SnoozeTimer fired");
			snooze(id,duration,wakeUp,snoozeNum);
		}
	}
}
