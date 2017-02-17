package us.bridgeses.Minder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ACTIVE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_CONDITIONS;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DATE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DAYSOFWEEK;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DESCRIPTION;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ID;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_IMAGE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LATITUDE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LEDCOLOR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LEDPATTERN;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LONGITUDE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_MONTHTYPE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_NAME;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_PERSISTENCE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_QR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_RADIUS;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_REPEATLENGTH;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_REPEATTYPE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_RINGTONE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_SNOOZEDURATION;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_SNOOZENUM;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_SSID;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_STYLE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_TEXTCOLOR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_VOLUME;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.REMINDER_URI;

/**
 * This class stores and retrieves {@link Reminder}s from a SQL database
 */
public class ReminderSqlDao implements ReminderDAO {

    Context context;
    private ContentResolver resolver;

    /**
     * Using SQLite in Android requires access to an application context
     * Use this method to set the context from the calling class
     * @param context an Application context
     */
    @Override
    public void setContext(Context context){
        resolver = context.getContentResolver();
    }

    /**
     * Convert a cursor into a list of {@link Reminder}s
     * @param cursor A populated cursor with reminders
     * @return All of the reminders contained in cursor
     */
    private static Reminder[] cursorToReminders(Cursor cursor){
        cursor.moveToFirst();
        Reminder[] reminders = new Reminder[cursor.getCount()];
        for (int i=0; i<cursor.getCount(); i++) {
            Reminder reminder = new Reminder();
            reminder.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            reminder.setActive(cursor.getInt(cursor.getColumnIndex(COLUMN_ACTIVE)) == 1);
            reminder.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
            reminder.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
            reminder.setDate(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)) * 1000);
            reminder.setDaysOfWeek((byte) cursor.getInt(cursor.getColumnIndex(COLUMN_DAYSOFWEEK)));
            reminder.setMonthType((byte) cursor.getInt(cursor.getColumnIndex(COLUMN_MONTHTYPE)));
            reminder.setLocation(new LatLng(cursor.getFloat(cursor.getColumnIndex(COLUMN_LATITUDE)),
                    cursor.getFloat(cursor.getColumnIndex(COLUMN_LONGITUDE))));
            reminder.setRepeatLength(cursor.getInt(cursor.getColumnIndex(COLUMN_REPEATLENGTH)));
            reminder.setRepeatType(cursor.getInt(cursor.getColumnIndex(COLUMN_REPEATTYPE)));
            reminder.setRadius(cursor.getInt(cursor.getColumnIndex(COLUMN_RADIUS)));
            reminder.setQr(cursor.getString(cursor.getColumnIndex(COLUMN_QR)));
            reminder.setPersistence((byte) cursor.getInt(cursor.getColumnIndex(COLUMN_PERSISTENCE)));
            reminder.setConditions((byte) cursor.getInt(cursor.getColumnIndex(COLUMN_CONDITIONS)));
            reminder.setStyle((byte) cursor.getInt(cursor.getColumnIndex(COLUMN_STYLE)));
            reminder.setSSID(cursor.getString(cursor.getColumnIndex(COLUMN_SSID)));
            reminder.setSnoozeDuration(cursor.getInt(cursor.getColumnIndex(COLUMN_SNOOZEDURATION)));
            reminder.setLedColor(cursor.getInt(cursor.getColumnIndex(COLUMN_LEDCOLOR)));
            reminder.setLedPattern(cursor.getInt(cursor.getColumnIndex(COLUMN_LEDPATTERN)));
            reminder.setSnoozeNumber(cursor.getInt(cursor.getColumnIndex(COLUMN_SNOOZENUM)));
            try {
                reminder.setRingtone(cursor.getString(cursor.getColumnIndex(COLUMN_RINGTONE)));
            }
            catch (NullPointerException e){
                reminder.setRingtone("");
            }
            reminder.setVolume(cursor.getInt(cursor.getColumnIndex(COLUMN_VOLUME)));
            reminder.setLedColor(cursor.getInt(cursor.getColumnIndex(COLUMN_LEDCOLOR)));
            reminder.setTextColor(cursor.getInt(cursor.getColumnIndex(COLUMN_TEXTCOLOR)));
            reminder.setImage(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE)));
            reminders[i] = reminder;
            cursor.moveToNext();
        }
        cursor.close();
        return reminders;
    }

    /**
     * Retrieve a cursor from the database
     * @param resolver The ContentResolver to retrieve the cursor from
     * @return The cursor
     */
    private Cursor getCursor(ContentResolver resolver){
        String[] projection = {
                COLUMN_ID,
                COLUMN_ACTIVE,
                COLUMN_NAME,
                COLUMN_DESCRIPTION,
                COLUMN_DATE,
                COLUMN_DAYSOFWEEK,
                COLUMN_MONTHTYPE,
                COLUMN_REPEATTYPE,
                COLUMN_REPEATLENGTH,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_RINGTONE,
                COLUMN_PERSISTENCE,
                COLUMN_RADIUS,
                COLUMN_QR,
                COLUMN_SSID,
                COLUMN_CONDITIONS,
                COLUMN_STYLE,
                COLUMN_SNOOZEDURATION,
                COLUMN_LEDCOLOR,
                COLUMN_LEDPATTERN,
                COLUMN_VOLUME,
                COLUMN_SNOOZENUM,
                COLUMN_TEXTCOLOR,
                COLUMN_LEDCOLOR,
                COLUMN_IMAGE,
        };
        String sortOrder = COLUMN_ACTIVE + " DESC, " + COLUMN_DATE + " ASC";
        Cursor cursor = resolver.query(
                REMINDER_URI,
                projection,
                null,
                null,
                sortOrder,
                null);
        return cursor;
    }

    /**
     * Retrieve a list of all reminders in the database
     * @return The reminders in the database
     */
    @Override
    public Reminder[] getReminders(){
        Cursor cursor = getCursor(resolver);
        Reminder[] reminders = cursorToReminders(cursor);
        cursor.close();
        return reminders;
    }

    /**
     * Returns a Cursor that will not be managed by this class. This cursor must be closed manually
     * @return A cursor with all the reminders in the database
     */
    @Override
    public Cursor getAndKeepOpen(){
        Cursor cursor = getCursor(resolver);
        return cursor;
    }

    /**
     * Get a single reminder from the database
     * @param id The id of the reminder to be retrieved
     * @return The reminder
     */
    public Reminder getReminder(long id){
        Uri requestUri = Uri.withAppendedPath(REMINDER_URI, Long.toString(id));
        Cursor cursor = resolver.query(requestUri, null, null, null, null);
        Reminder reminder;
        if (cursor != null  && cursor.getCount()>=1) {
            Reminder[] reminders = cursorToReminders(cursor);
            reminder = reminders[0];
        }
        else {
            reminder = Reminder.reminderFactory(context);
        }
        if (cursor != null) {
            cursor.close();
        }
        return reminder;
    }

    /**
     * Persist a reminder to the database. If the reminder does not exist in the database, the
     * reminder's id is assigned to the next available value
     * @param reminder The reminder to be persisted
     * @return The reminder with an updated id, if applicable
     */
    @Override
    public Reminder saveReminder(Reminder reminder) {
        ContentValues values = new ContentValues();
        if (reminder.getId() != -1)
            values.put(COLUMN_ID,reminder.getId());

        values.put(COLUMN_ACTIVE,reminder.getActive());
        values.put(COLUMN_NAME,reminder.getName());
        values.put(COLUMN_DESCRIPTION,reminder.getDescription());
        values.put(COLUMN_DATE,reminder.getDate().getTimeInMillis()/1000);
        values.put(COLUMN_DAYSOFWEEK,reminder.getDaysOfWeek());
        values.put(COLUMN_MONTHTYPE,reminder.getMonthType());
        values.put(COLUMN_LATITUDE,reminder.getLocation().latitude);
        values.put(COLUMN_LONGITUDE,reminder.getLocation().longitude);
        values.put(COLUMN_REPEATTYPE,reminder.getRepeatType());
        values.put(COLUMN_REPEATLENGTH,reminder.getRepeatLength());
        values.put(COLUMN_RINGTONE,reminder.getRingtone());
        values.put(COLUMN_PERSISTENCE,reminder.getPersistence());
        values.put(COLUMN_RADIUS,reminder.getRadius());
        values.put(COLUMN_QR,reminder.getQr());
        values.put(COLUMN_SSID, reminder.getSSID());
        values.put(COLUMN_SNOOZEDURATION, reminder.getSnoozeDuration());
        values.put(COLUMN_CONDITIONS, reminder.getConditions());
        values.put(COLUMN_STYLE, reminder.getStyle());
        values.put(COLUMN_VOLUME, reminder.getVolume());
        values.put(COLUMN_SNOOZENUM, reminder.getSnoozeNumber());
        values.put(COLUMN_LEDCOLOR, reminder.getLedColor());
        values.put(COLUMN_TEXTCOLOR, reminder.getTextColor());
        values.put(COLUMN_IMAGE, reminder.getImage());
        reminder.setId(Integer.parseInt( resolver.insert(
                REMINDER_URI,
                values).getLastPathSegment()));
        return reminder;
    }

    /**
     * Delete the reminder from the database, if it exists
     * @param id The id of the reminder to be deleted
     * @return the number of rows deleted. Should be 1 if successful, 0 if reminder does not exist
     */
    @Override
    public int deleteReminder(int id){
        String[] args = { String.valueOf(id) };
        Uri requestUri = Uri.withAppendedPath(REMINDER_URI, Long.toString(id));
        int result = resolver.delete(requestUri, null, null);
        return result;
    }
}
