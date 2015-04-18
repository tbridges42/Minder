package us.bridgeses.Minder.util;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;

import com.orhanobut.logger.Logger;

/**
 * Created by Tony on 4/14/2015.
 */
public class AlertService extends Service {
	private Ringtone ringtone;
	private Vibrator vibrator;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	private void startRingtone(Uri ringtoneUri){
		this.ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
		ringtone.play();
	}

	private void stopRingtone(){
		if (ringtone != null) {
			Logger.d("Stopping Ringtone");
			ringtone.stop();
		}
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
		boolean startRingtone = intent.getBooleanExtra("StartRingtone",false);
		boolean startVibrate = intent.getBooleanExtra("StartVibrate",false);
		if (startRingtone) {
			Logger.d("Starting ringtone service");
			Uri ringtoneUri = Uri.parse(intent.getExtras().getString("ringtone-uri"));
			startRingtone(ringtoneUri);
		}
		else {
			stopRingtone();
		}
		if (startVibrate) {
			Logger.d("Starting vibrate service");
			byte pattern = (byte) intent.getIntExtra("VibratePattern",1);
			boolean repeat = intent.getBooleanExtra("VibrateRepeat",false);
			startVibrate(pattern,repeat);
		}
		else {
			stopVibrate();
		}

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy()
	{

		if (ringtone != null) {
			Logger.d("Stopping Ringtone");
			ringtone.stop();
		}

		Logger.d("Stopping Vibrate");
		if (vibrator != null) {
			vibrator.cancel();
		}
		super.onDestroy();
	}
}
