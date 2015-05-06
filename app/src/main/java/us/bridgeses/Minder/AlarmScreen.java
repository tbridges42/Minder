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
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import us.bridgeses.Minder.receivers.ReminderReceiver;
import us.bridgeses.Minder.util.AlertService;
import us.bridgeses.Minder.util.scanner.ScannerActivity;


public class AlarmScreen extends Activity implements View.OnLongClickListener{

    private Reminder reminder;
	private Context context;
	private int snoozeNum;
    private final Handler handler = new Handler();


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
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
        reminder = Reminder.get(context,id);
        return reminder;
    }

    @Override
    protected void onNewIntent(Intent intent){
        int id = intent.getIntExtra("Id", -1);
        if (id == -1) {
            finish();
            return;
        }

	    if (reminder.getId() != id){
		    reminder = retrieveReminder(id);
	    }

        boolean dismiss = intent.getBooleanExtra("Dismiss",false);

        if (dismiss) {
			checkDismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    context = this.getApplicationContext();

        Intent intent = getIntent();
        int id = intent.getIntExtra("Id", -1);
        if (id == -1) {
            finish();
            return;
        }
        reminder = retrieveReminder(id);

        boolean dismiss = intent.getBooleanExtra("Dismiss",false);
	    snoozeNum = intent.getIntExtra("SnoozeNum",0);

	    createScreen();

        if (dismiss){
            checkDismiss();
        }
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode,resultCode,data);
		if (requestCode == 0) {

			if (resultCode == Activity.RESULT_OK) {
				String code = data.getStringExtra("SCAN_RESULT");
				if (code.equals(reminder.getQr())) {
					dismiss();
				}
			}
			if(resultCode == Activity.RESULT_CANCELED){
				AlarmClass.makeNoise(reminder,context);
                createScreen();
			}
		}
	}

	private void checkQr(){
		Intent intent = new Intent(context,ScannerActivity.class);
		startActivityForResult(intent, 0);
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
	    AlarmClass.silence(reminder, context);
	    int id = reminder.getId();
        cancelNotification(id);
	    cancelAlarm(id);
	    reminder = Reminder.nextRepeat(reminder).save(context);
	    if ((reminder.getActive()) && (reminder.getId() != -1)) {
		    setNewAlarm(id);
	    }
        finish();
    }

    public void confirmDismiss(){
		createScreen();
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
				AlarmClass.makeNoise(reminder,context);
            }
        });
        builder.create();
        builder.show();
    }

    public void checkDismiss(){
	    AlarmClass.silence(reminder,context);
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
	    AlarmClass.silence(reminder,context);
	    Intent snoozeIntent = new Intent(context,AlertService.class);
	    snoozeIntent.putExtra("Id",reminder.getId());
	    snoozeIntent.putExtra("Snooze",true);
	    snoozeNum++;
	    Logger.d("Sending SnoozeNum: "+snoozeNum);
	    snoozeIntent.putExtra("SnoozeNum",snoozeNum);
	    Logger.d("Snooze before sending: " + snoozeIntent.getBooleanExtra("Snooze", false));
	    snoozeIntent.putExtra("Duration", duration);
	    context.startService(snoozeIntent);
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
	    if ((snoozeNum < reminder.getSnoozeNumber())||(reminder.getSnoozeNumber() < 0)) {
		    if (reminder.getSnoozeNumber() > 0){
			    String snoozeText = (reminder.getSnoozeNumber() - snoozeNum - 1) +" snoozes remaining";
			    Toast.makeText(this,snoozeText,Toast.LENGTH_SHORT).show();
		    }
		    snooze(reminder.getSnoozeDuration());
	    }
	    else {
		    Toast.makeText(this,"Snooze limit reached",Toast.LENGTH_SHORT).show();
	    }
    }
}
