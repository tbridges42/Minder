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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.io.IOException;

import us.bridgeses.Minder.receivers.ReminderReceiver;
import us.bridgeses.Minder.util.AlertService;
import us.bridgeses.Minder.util.Scanner.ScannerActivity;


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

    private int getOrientation(String path){
        Cursor cursor = context.getContentResolver().query(Uri.parse(path),
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
                null, null, null);

        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                return -1;
            }
        } finally {
            cursor.close();
        }
    }

    private Bitmap shrinkBitmap(String path, int width, int height) {
        // TODO Auto-generated method stub
        try{
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(path)),null, bmpFactoryOptions);


            int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight/(float)height);
            int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth/(float)width);

            if (heightRatio > 1 || widthRatio > 1)
            {
                if (heightRatio > widthRatio)
                {
                    bmpFactoryOptions.inSampleSize = heightRatio;
                } else {
                    bmpFactoryOptions.inSampleSize = widthRatio;
                }
            }

            bmpFactoryOptions.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(path)), null, bmpFactoryOptions);
        }
        catch (IOException e){
            Toast.makeText(context, "Background Image Missing", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void setBackground(){
        try{
            int orientation = getOrientation(reminder.getImage());
            Logger.d("Found orientation: " + orientation);

            Point size = new Point();
            getWindowManager().getDefaultDisplay().getSize(size);

            Bitmap thumbBM = shrinkBitmap(reminder.getImage(),size.x,size.y);
            if (thumbBM == null){
                return;
            }
            Logger.d("Created raw image");

            Matrix matrix = new Matrix();
            if (orientation != 0f) {
                matrix.preRotate(orientation);
                thumbBM = Bitmap.createBitmap(thumbBM, 0, 0, thumbBM.getWidth(), thumbBM.getHeight(), matrix, true);
            }
            Logger.d("Rotated image");
            Logger.d("Set thumbnail");
            LinearLayout layout = (LinearLayout) findViewById(R.id.background);
            BitmapDrawable background = new BitmapDrawable(getResources(),thumbBM);

            int sdk = android.os.Build.VERSION.SDK_INT;
            if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                layout.setBackgroundDrawable( background );
            } else {
                layout.setBackground( background );
            }
        }
        catch (OutOfMemoryError e){
            Toast.makeText(context, "Background Image Too Large", Toast.LENGTH_SHORT).show();
        }
    }

	private void createScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                //WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_alarm_screen);

        TextView titleText = (TextView) findViewById(R.id.fullscreen_name);
        TextView descriptionText = (TextView) findViewById(R.id.fullscreen_description);
        TextView snooze = (TextView) findViewById(R.id.snooze_button);
        TextView dismiss = (TextView) findViewById(R.id.dismiss_button);
        titleText.setText(reminder.getName());
        titleText.setTextColor(reminder.getTextColor());
        descriptionText.setText(reminder.getDescription());
        descriptionText.setTextColor(reminder.getTextColor());
        snooze.setTextColor(reminder.getTextColor());
        dismiss.setTextColor(reminder.getTextColor());

        if (!reminder.getImage().equals("")){
            setBackground();
        }

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
        Intent intentAlarm = new Intent(getApplicationContext(), ReminderReceiver.class);      //Create alarm intent
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
	    AlarmClass.silence(reminder, context);
	    Intent snoozeIntent = new Intent(context,AlertService.class);
	    snoozeIntent.putExtra("Id",reminder.getId());
	    snoozeIntent.putExtra("Snooze",true);
	    snoozeNum++;
	    Logger.d("Sending SnoozeNum: "+snoozeNum);
	    snoozeIntent.putExtra("SnoozeNum", snoozeNum);
	    Logger.d("Snooze before sending: " + snoozeIntent.getBooleanExtra("Snooze", false));
	    snoozeIntent.putExtra("Duration", duration);
        Logger.d("Sending duration: " + duration);
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
                picker.clearFocus(); //Hopefully makes picker read keyboard entered values
                Logger.d("Entered " + picker.getValue() + " minutes");
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
