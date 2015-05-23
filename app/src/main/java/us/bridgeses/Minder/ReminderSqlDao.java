package us.bridgeses.Minder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Tony on 5/5/2015.
 */
public class ReminderSqlDao implements ReminderDAO {

    Context context;
    SQLiteDatabase database;
    ReminderDBHelper dbHelper;

    @Override
    public void setContext(Context context){
        this.context = context;
    }

    private static Reminder[] cursorToReminders(Cursor cursor){
        cursor.moveToFirst();
        Reminder[] reminders = new Reminder[cursor.getCount()];
        for (int i=0; i<cursor.getCount(); i++) {
            Reminder reminder = new Reminder();
            reminder.setId(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_ID)));
            reminder.setActive(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_ACTIVE)) == 1);
            reminder.setName(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_NAME)));
            reminder.setDescription(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_DESCRIPTION)));
            reminder.setDate(cursor.getLong(cursor.getColumnIndex(ReminderDBHelper.COLUMN_DATE)) * 1000);
            reminder.setDaysOfWeek((byte) cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_DAYSOFWEEK)));
            reminder.setMonthType((byte) cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_MONTHTYPE)));
            reminder.setLocation(new LatLng(cursor.getFloat(cursor.getColumnIndex(ReminderDBHelper.COLUMN_LATITUDE)),
                    cursor.getFloat(cursor.getColumnIndex(ReminderDBHelper.COLUMN_LONGITUDE))));
            reminder.setRepeatLength(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_REPEATLENGTH)));
            reminder.setRepeatType(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_REPEATTYPE)));
            reminder.setRadius(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_RADIUS)));
            reminder.setQr(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_QR)));
            reminder.setPersistence((byte) cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_PERSISTENCE)));
            reminder.setConditions((byte) cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_CONDITIONS)));
            reminder.setStyle((byte) cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_STYLE)));
            reminder.setSSID(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_SSID)));
            reminder.setSnoozeDuration(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_SNOOZEDURATION)));
            reminder.setLedColor(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_LEDCOLOR)));
            reminder.setLedPattern(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_LEDPATTERN)));
            reminder.setSnoozeNumber(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_SNOOZENUM)));
            try {
                reminder.setRingtone(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_RINGTONE)));
            }
            catch (NullPointerException e){
                reminder.setRingtone("");
            }
            reminder.setVolume(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_VOLUME)));
            reminder.setLedColor(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_LEDCOLOR)));
            reminder.setTextColor(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_TEXTCOLOR)));
            reminder.setImage(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_IMAGE)));
            reminders[i] = reminder;
            cursor.moveToNext();
        }
        cursor.close();
        return reminders;
    }

    public Cursor getCursor(SQLiteDatabase database){
        String[] projection = {
                ReminderDBHelper.COLUMN_ID,
                ReminderDBHelper.COLUMN_ACTIVE,
                ReminderDBHelper.COLUMN_NAME,
                ReminderDBHelper.COLUMN_DESCRIPTION,
                ReminderDBHelper.COLUMN_DATE,
                ReminderDBHelper.COLUMN_DAYSOFWEEK,
                ReminderDBHelper.COLUMN_MONTHTYPE,
                ReminderDBHelper.COLUMN_REPEATTYPE,
                ReminderDBHelper.COLUMN_REPEATLENGTH,
                ReminderDBHelper.COLUMN_LATITUDE,
                ReminderDBHelper.COLUMN_LONGITUDE,
                ReminderDBHelper.COLUMN_RINGTONE,
                ReminderDBHelper.COLUMN_PERSISTENCE,
                ReminderDBHelper.COLUMN_RADIUS,
                ReminderDBHelper.COLUMN_QR,
                ReminderDBHelper.COLUMN_SSID,
                ReminderDBHelper.COLUMN_CONDITIONS,
                ReminderDBHelper.COLUMN_STYLE,
                ReminderDBHelper.COLUMN_SNOOZEDURATION,
                ReminderDBHelper.COLUMN_LEDCOLOR,
                ReminderDBHelper.COLUMN_LEDPATTERN,
                ReminderDBHelper.COLUMN_VOLUME,
                ReminderDBHelper.COLUMN_SNOOZENUM,
                ReminderDBHelper.COLUMN_TEXTCOLOR,
                ReminderDBHelper.COLUMN_LEDCOLOR,
                ReminderDBHelper.COLUMN_IMAGE,
        };
        String sortOrder = ReminderDBHelper.COLUMN_ACTIVE + " DESC, " + ReminderDBHelper.COLUMN_DATE + " ASC";
        Cursor cursor =database.query(
                ReminderDBHelper.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder,
                null);
        return cursor;
    }

    @Override
    public Reminder[] getReminders(){
        if (dbHelper == null){
            dbHelper  = ReminderDBHelper.getInstance(context);
        }
        database = dbHelper.openDatabase();
        Cursor cursor = getCursor(database);
        Reminder[] reminders = cursorToReminders(cursor);
        cursor.close();
        dbHelper.closeDatabase();
        return reminders;
    }

    @Override
    public Cursor getAndKeepOpen(){
        if (dbHelper == null){
            dbHelper  = ReminderDBHelper.getInstance(context);
        }
        database = dbHelper.openDatabase();
        Cursor cursor = getCursor(database);
        return cursor;
    }

    @Override
    public void close(){
        if (dbHelper != null){
            dbHelper.closeDatabase();
        }
    }

    @Override
    public Reminder getReminder(int id){
        if (dbHelper == null){
            dbHelper  = ReminderDBHelper.getInstance(context);
        }
        database = dbHelper.openDatabase();
        Cursor cursor = database.rawQuery("select * from " + ReminderDBHelper.TABLE_NAME
                + " where " + ReminderDBHelper.COLUMN_ID + "="
                + id, null);
        Reminder reminder;
        if (cursor.getCount()>=1) {
            Reminder[] reminders = cursorToReminders(cursor);
            reminder = reminders[0];
        }
        else {
            reminder = Reminder.reminderFactory(context);
        }
        cursor.close();
        dbHelper.closeDatabase();
        return reminder;
    }

    @Override
    public Reminder saveReminder(Reminder reminder) {
        if (dbHelper == null){
            dbHelper  = ReminderDBHelper.getInstance(context);
        }
        database = dbHelper.openDatabase();
        ContentValues values = new ContentValues();
        if (reminder.getId() != -1)
            values.put(ReminderDBHelper.COLUMN_ID,reminder.getId());

        values.put(ReminderDBHelper.COLUMN_ACTIVE,reminder.getActive());
        values.put(ReminderDBHelper.COLUMN_NAME,reminder.getName());
        values.put(ReminderDBHelper.COLUMN_DESCRIPTION,reminder.getDescription());
        values.put(ReminderDBHelper.COLUMN_DATE,reminder.getDate().getTimeInMillis()/1000);
        values.put(ReminderDBHelper.COLUMN_DAYSOFWEEK,reminder.getDaysOfWeek());
        values.put(ReminderDBHelper.COLUMN_MONTHTYPE,reminder.getMonthType());
        values.put(ReminderDBHelper.COLUMN_LATITUDE,reminder.getLocation().latitude);
        values.put(ReminderDBHelper.COLUMN_LONGITUDE,reminder.getLocation().longitude);
        values.put(ReminderDBHelper.COLUMN_REPEATTYPE,reminder.getRepeatType());
        values.put(ReminderDBHelper.COLUMN_REPEATLENGTH,reminder.getRepeatLength());
        values.put(ReminderDBHelper.COLUMN_RINGTONE,reminder.getRingtone());
        values.put(ReminderDBHelper.COLUMN_PERSISTENCE,reminder.getPersistence());
        values.put(ReminderDBHelper.COLUMN_RADIUS,reminder.getRadius());
        values.put(ReminderDBHelper.COLUMN_QR,reminder.getQr());
        values.put(ReminderDBHelper.COLUMN_SSID, reminder.getSSID());
        values.put(ReminderDBHelper.COLUMN_SNOOZEDURATION, reminder.getSnoozeDuration());
        values.put(ReminderDBHelper.COLUMN_CONDITIONS, reminder.getConditions());
        values.put(ReminderDBHelper.COLUMN_STYLE, reminder.getStyle());
        values.put(ReminderDBHelper.COLUMN_VOLUME, reminder.getVolume());
        values.put(ReminderDBHelper.COLUMN_SNOOZENUM, reminder.getSnoozeNumber());
        values.put(ReminderDBHelper.COLUMN_LEDCOLOR, reminder.getLedColor());
        values.put(ReminderDBHelper.COLUMN_TEXTCOLOR, reminder.getTextColor());
        values.put(ReminderDBHelper.COLUMN_IMAGE, reminder.getImage());
        reminder.setId((int) database.replace(
                ReminderDBHelper.TABLE_NAME,
                null,
                values));
        dbHelper.closeDatabase();
        return reminder;
    }

    @Override
    public int deleteReminder(int id){
        if (dbHelper == null){
            dbHelper  = ReminderDBHelper.getInstance(context);
        }
        database = dbHelper.openDatabase();
        String[] args = { String.valueOf(id) };
        int result = database.delete(ReminderDBHelper.TABLE_NAME,ReminderDBHelper.COLUMN_ID+" LIKE ?",args);
        dbHelper.closeDatabase();
        return result;
    }

    @Override
    public boolean isOpen(){
        return database.isOpen();
    }
}
