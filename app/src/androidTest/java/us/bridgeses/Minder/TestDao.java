package us.bridgeses.Minder;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

/**
 * Created by Tony on 5/7/2015.
 */
public class TestDao implements ReminderDAO {

    @Override
    public void setContext(Context context){

    }

    private Reminder notificationReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(0);
        reminder.setName("Notification");
        reminder.setDescription("Test Reminder");
        reminder.setDisplayScreen(false);
        reminder.setInsistent(false);
        Calendar date = Calendar.getInstance();
        //date.roll(Calendar.MINUTE, 1);
        reminder.setDate(date);
        return reminder;
    }

    private Reminder insistentNotificationReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(1);
        reminder.setName("Insistent Notification");
        reminder.setDescription("Test Reminder");
        reminder.setDisplayScreen(false);
        reminder.setInsistent(true);
        Calendar date = Calendar.getInstance();
        //date.roll(Calendar.MINUTE, 1);
        reminder.setDate(date);
        return reminder;
    }

    private Reminder simpleReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(2);
        reminder.setName("Simple Reminder");
        reminder.setDescription("Test Reminder");
        reminder.setInsistent(false);
        Calendar date = Calendar.getInstance();
        //date.roll(Calendar.MINUTE, 1);
        reminder.setDate(date);
        return reminder;
    }

    private Reminder insistentReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(3);
        reminder.setName("Insistent Reminder");
        reminder.setDescription("Test Reminder");
        reminder.setInsistent(true);
        Calendar date = Calendar.getInstance();
        //date.roll(Calendar.MINUTE, 1);
        reminder.setDate(date);
        return reminder;
    }

    private Reminder confirmReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(4);
        reminder.setName("Confirmation Reminder");
        reminder.setDescription("Test Reminder");
        reminder.setInsistent(true);
        reminder.setConfirmDismiss(true);
        Calendar date = Calendar.getInstance();
        //date.roll(Calendar.MINUTE, 1);
        reminder.setDate(date);
        return reminder;
    }

    private Reminder untilLocationReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(5);
        reminder.setName("Until Location Reminder");
        reminder.setDescription("Test Reminder");
        reminder.setInsistent(true);
        reminder.setLocation(new LatLng(0, 0));
        reminder.setUntilLocation(true);
        Calendar date = Calendar.getInstance();
        //date.roll(Calendar.MINUTE, 1);
        reminder.setDate(date);
        return reminder;
    }

    private Reminder onlylLocationReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(6);
        reminder.setName("Only Location Reminder");
        reminder.setDescription("Test Reminder");
        reminder.setInsistent(true);
        reminder.setLocation(new LatLng(0, 0));
        reminder.setOnlyAtLocation(true);
        Calendar date = Calendar.getInstance();
        //date.roll(Calendar.MINUTE, 1);
        reminder.setDate(date);
        return reminder;
    }

    private Reminder wifiReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(7);
        reminder.setName("Wifi Reminder");
        reminder.setDescription("Test Reminder");
        reminder.setInsistent(true);
        reminder.setSSID("test");
        reminder.setNeedWifi(true);
        Calendar date = Calendar.getInstance();
        //date.roll(Calendar.MINUTE, 1);
        reminder.setDate(date);
        return reminder;
    }

    private Reminder ringtoneReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(8);
        reminder.setName("Ringtone Reminder");
        reminder.setDescription("Test Reminder");
        reminder.setInsistent(false);
        reminder.setRingtone("default");
        Calendar date = Calendar.getInstance();
        //date.roll(Calendar.MINUTE, 1);
        reminder.setDate(date);
        return reminder;
    }

    private Reminder vibrateReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(9);
        reminder.setName("Vibrate Reminder");
        reminder.setDescription("Test Reminder");
        reminder.setInsistent(false);
        reminder.setVibrate(true);
        Calendar date = Calendar.getInstance();
        //date.roll(Calendar.MINUTE, 1);
        reminder.setDate(date);
        return reminder;
    }

    private Reminder codeReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(10);
        reminder.setName("Code Reminder");
        reminder.setDescription("Test Reminder");
        reminder.setInsistent(false);
        reminder.setQr("12345");
        reminder.setNeedQr(true);
        Calendar date = Calendar.getInstance();
        //date.roll(Calendar.MINUTE, 1);
        reminder.setDate(date);
        return reminder;
    }

    private Reminder nullLocationReminder(){
        Reminder reminder = new Reminder();
        reminder.setLocation((LatLng) null);
        reminder.setId(11);
        return reminder;
    }

    private Reminder nullDateReminder(){
        Reminder reminder = new Reminder();
        reminder.setDate((Calendar) null);
        reminder.setId(12);
        return reminder;
    }

    private Reminder invalidMonthTypeReminder(){
        Reminder reminder = new Reminder();
        reminder.setMonthType((byte) 15);
        reminder.setId(13);
        return reminder;
    }

    private Reminder negativeIdReminder(){
        Reminder reminder = new Reminder();
        reminder.setId(-5);
        return reminder;
    }

    @Override
    public Reminder[] getReminders(){
        Reminder[] reminders = new Reminder[15];
        reminders[0] = notificationReminder();
        reminders[1] = insistentNotificationReminder();
        reminders[2] = simpleReminder();
        reminders[3] = insistentReminder();
        reminders[4] = confirmReminder();
        reminders[5] = untilLocationReminder();
        reminders[6] = onlylLocationReminder();
        reminders[7] = wifiReminder();
        reminders[8] = ringtoneReminder();
        reminders[9] = vibrateReminder();
        reminders[10] = codeReminder();
        return reminders;
    }

    @Override
    public Reminder getReminder(int id){
        if (id <= 10){
            return getReminders()[id];
        }
        switch (id){
            case 11:{
                return nullLocationReminder();
            }
            case 12: {
                return nullDateReminder();
            }
            case 13: {
                return invalidMonthTypeReminder();
            }
            case 14: {
                return negativeIdReminder();
            }
        }
        return new Reminder();
    }

    @Override
    public Reminder saveReminder(Reminder reminder){
        return reminder;
    }

    @Override
    public int deleteReminder(int id){
        return 1;
    }

    @Override
    public Cursor getAndKeepOpen(){
        return new MatrixCursor(new String[] { "colName1", "colName2" });
    }

    @Override
    public void close(){

    }

    @Override
    public boolean isOpen(){
        return false;
    }
}
