package us.bridgeses.Minder.util;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

import com.orhanobut.logger.Logger;

/**
 * Created by Tony on 4/14/2015.
 */
public class RingtoneService extends Service {
	private Ringtone ringtone;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Logger.d("Starting ringtone service");
		Uri ringtoneUri = Uri.parse(intent.getExtras().getString("ringtone-uri"));

		this.ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
		ringtone.play();

		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy()
	{
		ringtone.stop();
	}
}
