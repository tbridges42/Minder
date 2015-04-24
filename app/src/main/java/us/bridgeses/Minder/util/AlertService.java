package us.bridgeses.Minder.util;

import android.annotation.TargetApi;
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

import java.util.Hashtable;
import java.util.Set;

/**
 * Created by Tony on 4/14/2015.
 */
public class AlertService extends Service {
	Hashtable<Integer,Ringtone> ringtoneHash = new Hashtable<Integer,Ringtone>();
	private Vibrator vibrator;
	private int currVolume = -1;
	private int currRingMode = -1;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@SuppressWarnings("deprecation")
	private void overrideVolumeCompat(){
		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		Logger.d("Maxing out volume");
		currVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
		manager.setStreamVolume(AudioManager.STREAM_ALARM, (int) Math.round(manager.getStreamMaxVolume(AudioManager.STREAM_ALARM) * 0.8), 0);
		currRingMode = manager.getRingerMode();
		manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}

	@TargetApi(21)
	private void overrideVolume(){
		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		Logger.d("Maxing out volume, Lollipop");
		currVolume = manager.getStreamVolume(AudioManager.STREAM_ALARM);
		manager.setStreamVolume(AudioManager.STREAM_ALARM, (int) Math.round(manager.getStreamMaxVolume(AudioManager.STREAM_ALARM) * 0.8), 0);
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

	private void startRingtone(Uri ringtoneUri, boolean override, int id){
		Ringtone ringtone;
		Logger.d("Starting ringtone "+id);
		if (ringtoneHash.containsKey(id)){
			stopRingtone(id);
		}

		ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
		ringtoneHash.put(id,ringtone);

		if (override){
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
				overrideVolumeCompat();
				ringtone.setStreamType(AudioManager.STREAM_ALARM);
			}
			else {
				overrideVolume();
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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		int id = intent.getIntExtra("Id",-1);
		if (id != -1) {
			boolean startRingtone = intent.getBooleanExtra("StartRingtone", false);
			boolean startVibrate = intent.getBooleanExtra("StartVibrate", false);
			boolean override = intent.getBooleanExtra("Override",false);
			if (startRingtone) {
				Logger.d("Starting ringtone service");
				Uri ringtoneUri = Uri.parse(intent.getStringExtra("ringtone-uri"));
				startRingtone(ringtoneUri, override, id);
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
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy()
	{

		stopAllRingtones();

		stopVibrate();
		super.onDestroy();
	}
}
