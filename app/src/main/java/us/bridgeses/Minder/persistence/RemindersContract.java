package us.bridgeses.Minder.persistence;

import android.net.Uri;

/**
 * Created by bridgtxcdf on 2/16/2017.
 */

public class RemindersContract {

    public static final int SCHEMA_VERSION = 1;

    public static final Uri BASE_URI = Uri.parse("content://us.bridgeses.Minder");

    private RemindersContract() {} // Should not be instantiated

    public static class Reminder {

        public static final String TABLE_NAME = "Reminders";

        public static final Uri REMINDER_URI = Uri.withAppendedPath(BASE_URI, TABLE_NAME);

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
        public static final String COLUMN_IMAGE = "Image";
        public static final String COLUMN_TEXTCOLOR = "TextColor";

        public static final String[] DISPLAY_PROJECTION = {
                COLUMN_ID,
                COLUMN_ACTIVE,
                COLUMN_NAME,
                COLUMN_DATE,
                COLUMN_REPEATTYPE
        };
    }
}
