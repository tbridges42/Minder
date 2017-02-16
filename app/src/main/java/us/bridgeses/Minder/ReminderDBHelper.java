package us.bridgeses.Minder;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.atomic.AtomicInteger;

import static us.bridgeses.Minder.persistence.RemindersContract.*;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.*;

/**
 * Created by Tony on 7/23/2014.
 */
public class ReminderDBHelper extends SQLiteOpenHelper {

    private AtomicInteger openCounter = new AtomicInteger();
    private static ReminderDBHelper singleInstance;
    private SQLiteDatabase database;

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
		            COLUMN_SNOOZENUM + " INTEGER, " +
                    COLUMN_IMAGE + " TEXT, " +
                    COLUMN_TEXTCOLOR + " INTEGER " + ")";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final int DATABASE_VERSION = 17;
    public static final String DATABASE_NAME = "Reminders.db";

    /**
     * We follow a singleton pattern here to avoid simultaneous attempts to open or close the database
     * and to keep the database in a sane state
     * @param context The context from which this is being created.
     * @return Returns a new ReminderDBHelper if none existed, otherwise returns singleton
     */
    public static synchronized ReminderDBHelper getInstance(Context context){
        if (singleInstance == null) {
            singleInstance = new ReminderDBHelper(context.getApplicationContext());
        }
        return singleInstance;
    }

    private ReminderDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Here we do incremental upgrades when we need to make changes to the database structure
     * @param database
     * @param oldVersion
     * @param newVersion
     */
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
        if (oldVersion == 16){
            database.execSQL("ALTER TABLE "+TABLE_NAME+" ADD " + COLUMN_IMAGE + " STRING" );
            database.execSQL("ALTER TABLE "+TABLE_NAME+" ADD " + COLUMN_TEXTCOLOR + " INTEGER");
        }
    }

    public void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_ENTRIES);
    }

    public boolean isOpen(){
        return database.isOpen();
    }

    /**
     * Only open the database if it's not already open
     * @return an open database
     */
    public synchronized SQLiteDatabase openDatabase() {
        if(openCounter.incrementAndGet() == 1) {
            // Opening new database
            database = singleInstance.getWritableDatabase();
        }
        return database;
    }

    /**
     * Close the database if no one else is using it
     */
    public synchronized void closeDatabase() {
        if(openCounter.decrementAndGet() == 0) {
            // Closing database
            database.close();

        }
    }
}
