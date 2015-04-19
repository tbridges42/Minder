package us.bridgeses.Minder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import us.bridgeses.Minder.receivers.ReminderReceiver;
import us.bridgeses.Minder.util.AlertService;


public class AlarmScreen extends Activity implements View.OnLongClickListener{

    private ReminderDBHelper dbHelper;
    private Reminder reminder;
    private Ringtone ringtone;
    private Vibrator vibrator;
    private ScheduledExecutorService scheduleTaskExecutor;
	private Context context;
	private int snooze;
    private int curVolume;
    private int curRingMode;
    private final Handler handler = new Handler();


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    private void silence() {
	    Intent stopIntent = new Intent(context, AlertService.class);
	    context.stopService(stopIntent);
    }

    @Override
    public void onStop() {
        super.onStop();
        //silence();
    }

    @Override
    public boolean onLongClick(View view){
        customSnooze(view);
        return true;
    }

	private void createScreen() {
        if (reminder.getDisplayScreen()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    //WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
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

    private Reminder retrieveReminder(int id){
        if (id == -1){
            Logger.w("Invalid ID");
            return new Reminder();
        }
        if (context == null){
            Logger.e("Invalid context");
            return new Reminder();
        }
        ReminderDBHelper dbHelper  = ReminderDBHelper.getInstance(context);
        SQLiteDatabase database = dbHelper.openDatabase();
        reminder = Reminder.getReminder(database,id);
        dbHelper.closeDatabase();
        return reminder;
    }

    @Override
    protected void onNewIntent(Intent intent){
        int id = intent.getIntExtra("Id", -1);
        if (id == -1) {
            finish();
            return;
        }

        boolean dismiss = intent.getBooleanExtra("Dismiss",false);

        if (dismiss){
            reminder = retrieveReminder(id);
	        silence();
	        if (reminder.getNeedQr()){
		        checkQr();
	        }
	        else
		        checkDismiss();
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    context = this.getApplicationContext();
        scheduleTaskExecutor = Executors.newScheduledThreadPool(2);

        Intent intent = getIntent();
        int id = intent.getIntExtra("Id", -1);
        if (id == -1) {
            finish();
            return;
        }
        reminder = retrieveReminder(id);

        boolean dismiss = intent.getBooleanExtra("Dismiss",false);

	    Logger.d(Boolean.toString(dismiss));
        if (dismiss){
            checkDismiss();
            return;
        }
        else {
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

    private void cancelNotification(int id){
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.cancel(id);
    }

    private void cancelAlarm(int id){
        Intent intentAlarm = new Intent(this, ReminderReceiver.class);      //Create alarm intent
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(),
                id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private void setNewAlarm(int id){
        int alarmType;
        if (reminder.getWakeUp()){
            alarmType = AlarmManager.RTC_WAKEUP;
        }
        else {
            alarmType = AlarmManager.RTC;
        }
        Intent intentAlarm = new Intent(context, ReminderReceiver.class);//Create alarm intent
        intentAlarm.putExtra("Id", id);           //Associate intent with specific Reminder
        intentAlarm.putExtra("Snooze", 0);                       //This alarm has not been snoozed
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(alarmType, reminder.getDate().getTimeInMillis(),
                PendingIntent.getBroadcast(context, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

        Logger.v("Alarm " + id + " set");
    }

    private void dismiss() {
	    silence();
	    int id = reminder.getId();
        cancelNotification(id);
	    cancelAlarm(id);
        dbHelper = ReminderDBHelper.getInstance(context);
	    SQLiteDatabase database = dbHelper.openDatabase();
	    reminder = Reminder.nextRepeat(database,reminder);
	    dbHelper.closeDatabase();

	    if ((reminder.getActive()) && (reminder.getId() != -1)) {
		    setNewAlarm(id);
	    }
        if (scheduleTaskExecutor != null) {
            scheduleTaskExecutor.shutdownNow();
        }
        finish();
    }

    public void confirmDismiss(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dismiss Reminder");
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (reminder.getNeedQr()) {
                    checkQr();
                } else
                    dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.create();
        builder.show();
    }

    public void checkDismiss(){
        if (reminder.getConfirmDismiss()){
            confirmDismiss();
        }
        else {
            if (reminder.getNeedQr()) {
                checkQr();
            } else
                dismiss();
        }
    }

	public void dismissButton(View view) {
		checkDismiss();
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
	    Logger.v("Alarm " + id + " set");
        silence();
        if (scheduleTaskExecutor != null){
            scheduleTaskExecutor.shutdownNow();
        }
        finish();
    }

    private void customSnooze(View view) {
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
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

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
