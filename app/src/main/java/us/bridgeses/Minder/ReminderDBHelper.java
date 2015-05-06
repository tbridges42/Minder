package us.bridgeses.Minder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.orhanobut.logger.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Tony on 7/23/2014.
 */
public class ReminderDBHelper extends SQLiteOpenHelper {

    public AtomicInteger openCounter = new AtomicInteger();
    private static ReminderDBHelper singleInstance;
    private SQLiteDatabase database;

    public static final String TABLE_NAME = "Reminders";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ACTIVE = "Active";
    public static final String COLUMN_LATITUDE = "Latitude";
    public static final String COLUMN_LONGITUDE = "Longitude";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_REPEATTYPE = "RepeatType";
    public static final String COLUMN_REPEATLENGTH = "RepeatLength";
    public static final String COLUMN_DAYSOFWEEK = "DaysOfWeek";
    public static final String COLUMN_MONTHTYPE = "MonthType";
    public static final String COLUMN_PERSISTENCE = "Persistence";
    public static final String COLUMN_DATE = "Date";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String COLUMN_QR = "Qr";
    public static final String COLUMN_SNOOZEDURATION = "SnoozeDuration";
    public static final String COLUMN_LEDCOLOR = "LedColor";
    public static final String COLUMN_LEDPATTERN = "LedPattern";
    public static final String COLUMN_RINGTONE = "Ringtone";
    public static final String COLUMN_RADIUS = "Radius";
    public static final String COLUMN_SSID = "SSID";
    public static final String COLUMN_CONDITIONS = "Conditions";
    public static final String COLUMN_STYLE = "Style";
	public static final String COLUMN_VOLUME = "Volume";
	public static final String COLUMN_SNOOZENUM = "SnoozeNum";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    COLUMN_ACTIVE + " INTEGER, " +
                    COLUMN_STYLE + " INTEGER, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_REPEATTYPE + " INTEGER, " +
                    COLUMN_REPEATLENGTH + " INTEGER, " +
                    COLUMN_DAYSOFWEEK + " INTEGER, " +
                    COLUMN_MONTHTYPE + " TEXT, " +
                    COLUMN_CONDITIONS + " INTEGER, " +
                    COLUMN_PERSISTENCE + " INTEGER, " +
                    COLUMN_DATE + " INTEGER, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_QR + " TEXT, " +
                    COLUMN_SNOOZEDURATION + " INTEGER, " +
                    COLUMN_LEDCOLOR + " INTEGER, " +
                    COLUMN_LEDPATTERN + " INTEGER, " +
                    COLUMN_RINGTONE + " TEXT, " +
                    COLUMN_RADIUS + " INTEGER, " +
                    COLUMN_SSID + " TEXT, " +
		            COLUMN_VOLUME + " INTEGER, " +
		            COLUMN_SNOOZENUM + " INTEGER " + ")";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final int DATABASE_VERSION = 16;
    public static final String DATABASE_NAME = "Reminders.db";

    public static synchronized ReminderDBHelper getInstance(Context context){
        if (singleInstance == null) {
            singleInstance = new ReminderDBHelper(context.getApplicationContext());
        }
        return singleInstance;
    }

    private ReminderDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        /********************************************
         * This method will include every incremental
         * change ever made to the db. See example
         * here http://tiny.cc/ohahjx
         ********************************************/
        if (oldVersion <= 13) {
            database.execSQL(SQL_DELETE_ENTRIES);     //Versions less than 13 are incompatible and need to be rewritten
            database.execSQL(SQL_CREATE_ENTRIES);
        }
	    if (oldVersion == 14) {
		    database.execSQL("ALTER TABLE "+TABLE_NAME+" ADD " + COLUMN_VOLUME + " INTEGER");
	    }
	    if (oldVersion == 15) {
		    database.execSQL("ALTER TABLE "+TABLE_NAME+" ADD " + COLUMN_SNOOZENUM + " INTEGER");
	    }
        Logger.i("Database Upgraded");
    }

    public void onCreate(SQLiteDatabase database) {

        database.execSQL(SQL_CREATE_ENTRIES);
    }

    public boolean isOpen(){
        return database.isOpen();
    }

    public synchronized SQLiteDatabase openDatabase() {
        if(openCounter.incrementAndGet() == 1) {
            // Opening new database
            database = singleInstance.getWritableDatabase();
        }
        return database;
    }

    public synchronized void closeDatabase() {
        if(openCounter.decrementAndGet() == 0) {
            // Closing database
            database.close();

        }
    }
}
