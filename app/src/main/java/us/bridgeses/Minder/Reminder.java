package us.bridgeses.Minder;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Overseer on 7/13/2014.
 * Reminder class contains all settings for individual reminders
 * and methods for getting/setting, also storing in and retrieving from SQL db
 * The class that makes the magic happen
 */

public class Reminder implements Parcelable{


    //Constructors
    public Reminder() {
        setActive(ACTIVEDEFAULT);
        location = LOCATIONDEFAULT;
        name = NAMEDEFAULT;
        repeatType = REPEATTYPEDEFAULT;
        repeatLength = REPEATLENGTHDEFAULT;
        daysOfWeek = DAYSOFWEEKDEFAULT;
        monthType = MONTHTYPEDEFAULT;
        setOnlyAtLocation(ONLYATLOCATIONDEFAULT);
        setUntilLocation(UNTILLOCATIONDEFAULT);
        date = Calendar.getInstance();
        description = DESCRIPTIONDEFAULT;
        qr = QRDEFAULT;
	    setNeedQr(NEEDQRDEFAULT);
        snoozeDuration = SNOOZEDURATIONDEFAULT;
        setVibrate(VIBRATEDEFAULT);
        ringtone = RINGTONEDEFAULT;
        ledPattern = LEDPATTERNDEFAULT;
        ledColor = LEDCOLORDEFAULT;
        setLed(LEDDEFAULT);
        id = IDDEFAULT;
        radius = RADIUSDEFAULT;
        ssid = SSIDDEFAULT;
        setNeedWifi(WIFIDEFAULT);
        bluetooth = BTDEFAULT;
        setNeedBluetooth(BTNEEDEDDEFAULT);
        setWakeUp(WAKEUPDEFAULT);
        setDisplayScreen(DISPLAYSCREENDEFAULT);
	    setConfirmDismiss(DISMISSDIALOGDEFAULT);
	    setFadeVolume(FADEDEFAULT);
    }

    public static Reminder reminderFactory(SharedPreferences sharedPreferences, Context context){
        return preferenceToReminder(sharedPreferences, new Reminder(), context);
    }

    /* Parcelable constructor methods */
    public Reminder(Parcel in){
        readFromParcel(in);
    }

    public static final Parcelable.Creator<Reminder> CREATOR = new Parcelable.Creator<Reminder>() {
                public Reminder createFromParcel(Parcel in) {
                    return new Reminder(in);
                }

                public Reminder[] newArray(int size) {
                    return new Reminder[size];
                }
    };

    //Variables! Hooray variables!
    private int id;                            //Unique identifier
    private LatLng location;                   //Geographical location constraint center
    private int radius;                        //Geographical constraint radius
    private String name;                       //User defined string representing name or title
    private String description;                //A user defined string intended to give more detail about the purpose of the reminder
    private int repeatType;                    /*Number representing type of repeat to be used
                                                 0 = Do not repeat
                                                 1 = Daily
                                                 2 = Weekly
                                                 3 = Monthly
                                                 4 = Annually*/
    private int repeatLength;                  //Span between repeats. Units are set by {@link #repeatType}
    private byte monthType;                    /*Number representing how to treat monthly repeats
                                                 0: Monthly on this date (e.g. the 3rd of every month)
                                                 1: Monthly on this day of week (e.g. the 2nd Thursday)
                                                 2: Monthly this many days from the end of the month 
                                                     (e.g. three days before the end of the month)
                                                 3: Monthly on this day of week, counting from end of the month 
                                                     (e.g. the last friday, not yet implemented)*///TODO: Implement
    private byte daysOfWeek;                   //Bitwise byte representing seven boolean values for the seven days of the week
    private byte persistence;                  //Bitwise byte representing an array of boolean values related to reminder Persistence
    private byte conditions;                   //Bitwise byte representing an array of boolean values related to reminder Conditions
    private byte style;                        //Bitwise byte representing an array of boolean values related to reminder Style
    private Calendar date;                     //The date and time at which the reminder should fire, truncated to second
    private String qr;                         //A string representing the encoded value of a barcode or QR code, scanned in by user
    private int snoozeDuration;                //The default duration before trying the reminder again if not dismissed
    private int ledColor;                      //An int representing the hexadecimal color of the LED //TODO: Implement ledColor
    private int ledPattern;                    //An int representing the pattern in which the LED should flash //TODO: Implement led
    private String ringtone;                   //A string representing a URI for a ringtone selected by the user, to be played when reminder fires
    private String ssid;                       //A string representing an SSID for a user selected wifi-network
    private String bluetooth;                  //A string representing a user selected bluetooth pairing //TODO: Implement bluetooth

    //Default constants
    public static final boolean ACTIVEDEFAULT = true;
    public static final LatLng LOCATIONDEFAULT = new LatLng(0,0);
    public static final String NAMEDEFAULT = "";
    public static final int REPEATTYPEDEFAULT = 0;
    public static final int REPEATLENGTHDEFAULT = 1;
    public static final byte DAYSOFWEEKDEFAULT = 0;
    public static final byte MONTHTYPEDEFAULT = 0;
    public static final boolean ONLYATLOCATIONDEFAULT = false;
    public static final boolean UNTILLOCATIONDEFAULT = false;
    public static final boolean VOLUMEOVERRIDEDEFAULT = false;
    public static final boolean DISPLAYSCREENDEFAULT = true;
    public static final boolean WAKEUPDEFAULT = true;
    public static final String DESCRIPTIONDEFAULT = "";
    public static final int SNOOZEDURATIONDEFAULT = 300000;
    public static final String QRDEFAULT = "";
	public static final boolean NEEDQRDEFAULT = false;
    public static final boolean VIBRATEDEFAULT = false;
    public static final String RINGTONEDEFAULT = "";
    public static final boolean LEDDEFAULT = false;
    public static final int LEDCOLORDEFAULT = -1;
    public static final int LEDPATTERNDEFAULT = -1;
    public static final int IDDEFAULT = -1;
    public static final int RADIUSDEFAULT = 500;
    public static final String SSIDDEFAULT = "";
    public static final String BTDEFAULT = "";
    public static final boolean WIFIDEFAULT = false;
    public static final boolean BTNEEDEDDEFAULT = false;
	public static final boolean DISMISSDIALOGDEFAULT = false;
	public static final boolean FADEDEFAULT = false;

    //Time constants
    public static final int MINUTE = 60000;
    public static final int HOUR = 3600000;
    public static final int DAY = 86400000;
    public static final int WEEK = 604800000;
    public static final byte SUNDAY = 64;
    public static final byte MONDAY = 32;
    public static final byte TUESDAY = 16;
    public static final byte WEDNESDAY = 8;
    public static final byte THURSDAY = 4;
    public static final byte FRIDAY = 2;
    public static final byte SATURDAY = 1;
    public static final byte ALL_WEEK = 127;
    public static final byte WEEKDAYS = 62;
    public static final byte WEEKENDS = 65;
    public static final byte MWF = 42;
    public static final byte TTH = 20;

	//Persistence constants
	public static final byte VOLUME_OVERRIDE = 1;
	public static final byte REQUIRE_CODE = 2;
    public static final byte DISPLAY_SCREEN = 4;
    public static final byte WAKE_UP = 8;
	public static final byte DISMISS_DIALOG = 16;

    //Conditions constants
    public static final byte UNTIL_LOCATION = 1;
    public static final byte ONLY_AT_LOCATION = 2;
    public static final byte WIFINEEDED = 4;
    public static final byte BLUETOOTHNEEDED = 8;
    public static final byte ACTIVE = 16;

    //Style constants
    public static final byte LED = 1;
    public static final byte VIBRATE = 2;
	public static final byte FADE = 4;

    public static final String PREFS_NAME = "ReminderPrefs";


    public void setSSID(String ssid){
        //TODO: determine ssid input requirements
        this.ssid=ssid;
    }

    public String getSSID(){
        return ssid;
    }

    public String getBluetooth(){
        return bluetooth;
    }

    public void setBluetooth(String bluetooth){
	    //TODO: determine bluetooth input requirements
        this.bluetooth = bluetooth;
    }

	public byte getMonthType() {
		return monthType;
	}

	public void setMonthType(byte monthType) {
		if ((0 <= monthType) && (monthType <= 3)){
			this.monthType = monthType;
		}
		else {
			throw new IllegalArgumentException("Month Type must be between 0 and 3");
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		if (id >= 0){
			this.id = id;
		}
		else {
			throw new IllegalArgumentException("ID must be greater than zero");
		}
	}

	public LatLng getLocation() {
		return location;
	}

	public void setLocation(LatLng location) {
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRepeatType() {
		return repeatType;
	}

	public void setRepeatType(int repeatType) {
		if ((0 <= repeatType) && (repeatType <= 4)){
			this.repeatType = repeatType;
		}
		else {
			throw new IllegalArgumentException("Repeat type must be between 0 and 4");
		}
	}

	public int getRepeatLength() {
		return repeatLength;
	}

	public void setRepeatLength(int repeatLength) {
		if (0 < repeatLength){
			this.repeatLength = repeatLength;
		}
		else {
			throw new IllegalArgumentException("Repeat length must be greater than zero");
		}
	}

	public byte getDaysOfWeek() {
		return daysOfWeek;
	}

	//TODO: Move days of week text to getDaysOfWeekNames(int length) where length=1 > umtwrfs; length=2 > MoTuWeThFrSa (weekdays, weekends, every day);
	//length = 3 = SunMonTueWedThuFriSat (weekdays, weekends, every day); index > 3 = Sunday Monday Tuesday Wednesday Thursday Friday Saturday

	public void setDaysOfWeek(byte daysOfWeek) {
		//TODO: Move daysOfWeek logic inside setter
		this.daysOfWeek = daysOfWeek;
	}

	public void setDaysOfWeek(boolean Sunday, boolean Monday, boolean Tuesday, boolean Wednesday,
	                          boolean Thursday, boolean Friday, boolean Saturday){
		byte tempDays = 0;
		if (Sunday){
			tempDays += SUNDAY;
		}
		if (Monday){
			tempDays += MONDAY;
		}
		if (Tuesday){
			tempDays += TUESDAY;
		}
		if (Wednesday){
			tempDays += WEDNESDAY;
		}
		if (Thursday){
			tempDays += THURSDAY;
		}
		if (Friday){
			tempDays += FRIDAY;
		}
		if (Saturday){
			tempDays += SATURDAY;
		}
		daysOfWeek = tempDays;
	}

	public Calendar getDate() {
		return date;
	}

	public void setDate(Calendar date) {
		long time = date.getTimeInMillis();
		time = time/60000;
		time = time*60000;
		date.setTimeInMillis(time);                     //Drop seconds
		this.date = date;
	}

	public void setDate(long millis) {
		Calendar tempDate = Calendar.getInstance();
		tempDate.setTimeInMillis(millis);
		this.date = tempDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		//TODO: Does this need sanitizing?
		this.description = description;
	}

	public String getQr() {
		return qr;
	}

	public void setQr(String qr) {
		//TODO: Does this need sanitizing?
		this.qr = qr;
	}

	public int getSnoozeDuration() {
		return snoozeDuration;
	}

	public void setSnoozeDuration(int snoozeDuration) {
		if (0 < snoozeDuration){
			this.snoozeDuration = snoozeDuration;
		}
		else {
			throw new IllegalArgumentException("Snooze Duration must be greater than zero");
		}
	}

	public int getLedColor() {
		return ledColor;
	}

	public void setLedColor(int ledColor) {
		if ((0 <= ledColor) && (ledColor <= 0xffffff)){
			this.ledColor = ledColor;
		}
		else {
			throw new IllegalArgumentException("LED Color must be a valid color");
		}
	}

	public int getLedPattern() {
		return ledPattern;
	}

	public void setLedPattern(int ledPattern) {
		if (0 <= ledPattern){
			this.ledPattern = ledPattern;
		}
		else {
			throw new IllegalArgumentException("LED Pattern must be positive");
		}
	}

	public String getRingtone() {
		return ringtone;
	}

	public void setRingtone(String ringtone) {
		//TODO: Does this need sanitizing? Can this be sanitized?
		this.ringtone = ringtone;
	}

	public void setRadius(int radius){
		if (0 < radius){
			this.radius = radius;
		}
		else {
			throw new IllegalArgumentException("Radius must be greater than zero");
		}
	}

	public int getRadius(){
		return radius;
	}

	/******************************* Bitwise getters and setters *************************/
    public void setConditions (byte conditions){
        this.conditions = conditions;
    }

    public byte getConditions() {
        return conditions;
    }

	public byte getPersistence() {
		return persistence;
	}

	public void setPersistence(byte persistence) {
		this.persistence = persistence;
	}

	public byte getStyle(){
		return style;
	}

	public void setStyle(byte style){
		this.style = style;
	}
    
    private boolean getBitwise(byte store, byte key){
        return (store & key) == key;
    }
        
    private byte makeBitwise(byte store, byte key, boolean value){
        if (value && !this.getBitwise(store,key)){
            return (byte)(store+key);
        }
        else {
            if (!value && this.getBitwise(store,key)){
                return (byte)(store-key);
            }
        }
	    return store;
    }

	/*********************** Conditions bitwise getters and setters ************************/

    public Boolean getActive() {
        return getBitwise(this.getConditions(),ACTIVE);
    }

    public void setActive(boolean active) {
        this.setConditions(makeBitwise(this.getConditions(),ACTIVE,active));
    }

	public Boolean getNeedWifi(){
		return getBitwise(this.getConditions(),WIFINEEDED);
	}

	public void setNeedWifi(boolean wifiNeeded){
		this.setConditions(makeBitwise(this.getConditions(),WIFINEEDED,wifiNeeded));
	}

	public Boolean getNeedBluetooth(){
		return getBitwise(this.getConditions(),BLUETOOTHNEEDED);
	}

	public void setNeedBluetooth(Boolean needBluetooth){
		this.setConditions(makeBitwise(this.getConditions(),BLUETOOTHNEEDED,needBluetooth));
	}

	public Boolean getOnlyAtLocation() {
		return getBitwise(this.getConditions(),ONLY_AT_LOCATION);
	}

	public void setOnlyAtLocation(Boolean onlyAtLocation) {
		this.setConditions(makeBitwise(this.getConditions(),ONLY_AT_LOCATION,onlyAtLocation));
	}

	public Boolean getUntilLocation() {
		return getBitwise(this.getConditions(),UNTIL_LOCATION);
	}

	public void setUntilLocation(Boolean untilLocation) {
		this.setConditions(makeBitwise(this.getConditions(),UNTIL_LOCATION,untilLocation));
	}

	/************************ Persistence bitwise getters and setters **********************/

	public Boolean getNeedQr() {
		return getBitwise(this.getPersistence(),REQUIRE_CODE);
	}

	public void setNeedQr(Boolean needQr) {
		this.setPersistence(makeBitwise(this.getPersistence(),REQUIRE_CODE,needQr));
	}

	public Boolean getVolumeOverride(){
		return getBitwise(this.getPersistence(),VOLUME_OVERRIDE);
	}

	public void setVolumeOverride(Boolean volumeOverride){
		this.setPersistence(makeBitwise(this.getPersistence(),VOLUME_OVERRIDE,volumeOverride));
	}

	public Boolean getDisplayScreen(){
		return getBitwise(this.getPersistence(),DISPLAY_SCREEN);
	}

	public void setDisplayScreen(Boolean displayScreen){
		this.setPersistence(makeBitwise(this.getPersistence(),DISPLAY_SCREEN,displayScreen));
	}

	public Boolean getWakeUp(){
		return getBitwise(this.getPersistence(),WAKE_UP);

	}

	public void setWakeUp(Boolean wakeUp){
		this.setPersistence(makeBitwise(this.getPersistence(),WAKE_UP,wakeUp));
	}

	public Boolean getConfirmDismiss(){
		return getBitwise(this.getPersistence(),DISMISS_DIALOG);

	}

	public void setConfirmDismiss(Boolean dismissDialog){
		this.setPersistence(makeBitwise(this.getPersistence(),DISMISS_DIALOG,dismissDialog));
	}

	/*************************** Style bitwise getters and setters ************************/

	public Boolean getFadeVolume(){
		return getBitwise(this.getStyle(),FADE);
	}

	public void setFadeVolume(Boolean fade){
		this.setStyle(makeBitwise(this.getStyle(),FADE,fade));
	}

	public Boolean getVibrate() {
		return getBitwise(this.getStyle(),VIBRATE);
	}

	public void setVibrate(Boolean vibrate) {
		this.setStyle(makeBitwise(this.getStyle(),VIBRATE,vibrate));
	}

	public Boolean getLed() {
		return getBitwise(this.getStyle(),LED);
	}

	public void setLed(Boolean led) {
		this.setStyle(makeBitwise(this.getStyle(),LED,led));
	}

	/**************************** Database methods ***************************************/

    private static Reminder[] cursorToReminders(Cursor cursor){
        int numReminders = cursor.getCount();
        cursor.moveToFirst();
        Reminder[] reminders = new Reminder[numReminders];
        for (int i=0; i<numReminders; i++) {
            Reminder reminder = new Reminder();
            reminder.setId(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_ID)));
            reminder.setActive(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_ACTIVE)) == 1);
            reminder.setName(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_NAME)));
            reminder.setDescription(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_DESCRIPTION)));
            reminder.setDate(cursor.getLong(cursor.getColumnIndex(ReminderDBHelper.COLUMN_DATE))*1000);
            reminder.setDaysOfWeek((byte) cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_DAYSOFWEEK)));
            reminder.setMonthType((byte) cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_MONTHTYPE)));
            reminder.setLocation(new LatLng(cursor.getFloat(cursor.getColumnIndex(ReminderDBHelper.COLUMN_LATITUDE)),
		            cursor.getFloat(cursor.getColumnIndex(ReminderDBHelper.COLUMN_LONGITUDE))));
            reminder.setRepeatLength(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_REPEATLENGTH)));
            reminder.setRepeatType(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_REPEATTYPE)));
            reminder.setRadius(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_RADIUS)));
	        reminder.setQr(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_QR)));
	        reminder.setPersistence((byte)cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_PERSISTENCE)));
            reminder.setConditions((byte)cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_CONDITIONS)));
            reminder.setStyle((byte)cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_STYLE)));
            reminder.setSSID(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_SSID)));
            reminder.setSnoozeDuration(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_SNOOZEDURATION)));
	        reminder.setLedColor(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_LEDCOLOR)));
	        reminder.setLedPattern(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_LEDPATTERN)));
            try {
                reminder.setRingtone(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_RINGTONE)));
            }
            catch (NullPointerException e){
                reminder.setRingtone("");
            }
            reminders[i] = reminder;
            cursor.moveToNext();
        }
        cursor.close();
        return reminders;
    }

    protected static Cursor getCursor(SQLiteDatabase database){
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
        };
        String sortOrder = ReminderDBHelper.COLUMN_ACTIVE + " DESC, " + ReminderDBHelper.COLUMN_DATE + " ASC";

        return database.query(
                ReminderDBHelper.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder,
                null
        );
    }

    public static Reminder[] readReminders(SQLiteDatabase database){
        Cursor cursor = getCursor(database);
        return cursorToReminders(cursor);
    }

    public static Reminder getReminder(SQLiteDatabase database, int id){
        Cursor cursor = database.rawQuery("select * from " + ReminderDBHelper.TABLE_NAME
                                            + " where " + ReminderDBHelper.COLUMN_ID + "="
                                            + id,null);
        if (cursor.getCount()>=1) {
            Reminder[] reminders = cursorToReminders(cursor);
            return reminders[0];
        }
        else {
            return new Reminder();
        }
    }

    protected static long saveReminder(SQLiteDatabase database, Reminder reminder) {
        Cursor cursor = database.rawQuery("select "+ReminderDBHelper.COLUMN_ID+" from " + ReminderDBHelper.TABLE_NAME
                                             + " where " + ReminderDBHelper.COLUMN_DATE + "="
                                             + reminder.getDate().getTimeInMillis()/1000,null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int id = cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_ID));
            if (id != reminder.getId()){
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(reminder.getDate().getTimeInMillis()+30000);
                reminder.setDate(date);
            }
        }
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

        long newRowId;
        newRowId = database.replace(
                ReminderDBHelper.TABLE_NAME,
                null,
                values);
        return newRowId;
    }

    protected static boolean deleteReminder(SQLiteDatabase database, int id){
        String[] args = { String.valueOf(id) };
        int result = database.delete(ReminderDBHelper.TABLE_NAME,ReminderDBHelper.COLUMN_ID+" LIKE ?",args);
        return result != 0;
    }

    public static Reminder getNextReminder(SQLiteDatabase database){
        Calendar currentDate = Calendar.getInstance();
        long currentTime = (currentDate.getTimeInMillis()/60000)*60;
        String[] args = { String.valueOf(currentTime), "1" };
        Cursor cursor = database.query(ReminderDBHelper.TABLE_NAME,
                                        null,
                                        ReminderDBHelper.COLUMN_DATE + " > ? AND " +
                                        ReminderDBHelper.COLUMN_ACTIVE + " = ?",
                                        args,
                                        null,
                                        null,
                                        ReminderDBHelper.COLUMN_DATE + " ASC",
                                        "1");
        if (cursor.getCount()>=1) {
            Reminder[] reminders = cursorToReminders(cursor);
	        Reminder nextReminder = reminders[0];

            return reminders[0];
        }
        else {
            return new Reminder();
        }
    }

	/******************************** Parcel Methods ******************************/

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeDouble(location.latitude);
        out.writeDouble(location.longitude);
        out.writeString(name);
        out.writeInt(repeatType);
        out.writeInt(repeatLength);
        out.writeByte(daysOfWeek);
        out.writeByte(persistence);
        out.writeSerializable(date);
        out.writeString(description);
        out.writeString(qr);
        out.writeInt(snoozeDuration);
        out.writeInt(ledColor);
        out.writeInt(ledPattern);
        out.writeString(ringtone);
        out.writeInt(radius);
        out.writeString(ssid);
        out.writeByte(conditions);
        out.writeByte(style);
    }

    public void readFromParcel(Parcel in){
        id = in.readInt();
        double lon = in.readDouble();
        double lat = in.readDouble();
        location = new LatLng(lon,lat);
        name = in.readString();
        repeatType = in.readInt();
        repeatLength = in.readInt();
        daysOfWeek = in.readByte();
        persistence = in.readByte();
        date = (Calendar) in.readSerializable();
        description = in.readString();
        qr = in.readString();
        snoozeDuration = in.readInt();
        ledColor = in.readInt();
        ledPattern = in.readInt();
        ringtone = in.readString();
        radius = in.readInt();
        ssid = in.readString();
        conditions = in.readByte();
        style = in.readByte();
    }

    @Override
    public int describeContents(){
        return 0;
    }

	/********************************* Repeat Methods ****************************************/

    //Returns true if thisDay is in bitwise set daysOfWeek
    protected static Boolean checkDayOfWeek(byte daysOfWeek, int thisDay) {	//Call with checkDayOfWeek(reminder.getDaysOfWeek,thisDay)
        int mask = 0;
        switch (thisDay) {
            case 1: {
                mask = Reminder.SUNDAY;
                break;
            }
            case 2: {
                mask = Reminder.MONDAY;
                break;
            }
            case 3: {
                mask = Reminder.TUESDAY;
                break;
            }
            case 4: {
                mask = Reminder.WEDNESDAY;
                break;
            }
            case 5: {
                mask = Reminder.THURSDAY;
                break;
            }
            case 6: {
                mask = Reminder.FRIDAY;
                break;
            }
            case 7: {
                mask = Reminder.SATURDAY;
                break;
            }
        }
        return((daysOfWeek & mask) == mask);
    }

    //Daily Repeat has no special use cases. adds RepeatLength days to Date
    private static void nextDailyRepeat(Reminder reminder){
        Calendar date = reminder.getDate();
        date.add(Calendar.DAY_OF_MONTH,reminder.getRepeatLength());
        reminder.setDate(date);
    }


    //Weekly Repeat finds next chosen day.
    //E.g., if MWF is chosen, finds next of M->W->F->M
    private static void nextWeeklyRepeat(Reminder reminder){
        Calendar date = reminder.getDate();
        int thisDay = date.get(Calendar.DAY_OF_WEEK);
        Boolean repeat = false;
        int count = 0;      //Count checks for error conditions and prevents infinite loops
        while (!repeat) {
            if (count > (7*(reminder.getRepeatLength()+1))){ //Cycled through entire week without finding match
                Log.e("Minder","Next Weekly Repeat does not exist");
                reminder.setActive(false);
	            throw new IndexOutOfBoundsException("Next Weekly Repeat does not exist");
            }
            if (thisDay == 7) { //After Saturday, cycle to Sunday
                thisDay = 1;
                count += 7*(reminder.getRepeatLength()-1); //Skip weeks, if desired
            }
            else
                thisDay++;
            repeat = checkDayOfWeek(reminder.getDaysOfWeek(),thisDay);  //Check day against chosen set
            count++;
        }

        date.add(Calendar.DAY_OF_MONTH,count);
        reminder.setDate(date);
    }


    //Monthly Repeat has four separate use cases
    //Case 0: Monthly on this date (e.g. the 3rd of every month)
    //Case 1: Monthly on this day of week (e.g. the 2nd Thursday)
    //Case 2: Monthly this many days from the end of the month (e.g. three days before the end of
    //the month)
    //Case 3: Monthly on this day of week, counting from end of the month (e.g. the last friday, not
    //yet implemented
    //TODO: Implement case 3
    private static void nextMonthlyRepeat(Reminder reminder) {
        Calendar date = reminder.getDate();
        int count = 0;              //Count checks for error conditions and prevents infinite loops
        switch (reminder.monthType) {
            case 0: {				//Monthly on Date
                int day = date.get(Calendar.DAY_OF_MONTH);
                date.add(Calendar.MONTH,reminder.getRepeatLength());    //Skip months if desired
                while (day > date.getActualMaximum(Calendar.DAY_OF_MONTH)) { //no Day in month
                                                                            //e.g. no 30th in Feb.
                    if (count == 13){   //Cycled through every month without finding a match
                        Log.e("Minder","Next Monthly Repeat does not exist");
                        reminder.setActive(false);
                        return;
                    }
                    date.add(Calendar.MONTH,1);     //Increment until Date is in month
                    count++;
                }
                date.set(Calendar.DAY_OF_MONTH,day);
                break;
            }
            case 1:{				//Monthly on Day of Week
                int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
                int weekOfMonth = date.get(Calendar.WEEK_OF_MONTH);
                date.add(Calendar.MONTH,reminder.getRepeatLength());
                while (weekOfMonth > date.getActualMaximum(Calendar.WEEK_OF_MONTH)) {
                    if (count == 13){
                        Log.e("Minder","Next Monthly Repeat does not exist");
                        reminder.setActive(false);
                        return;
                    }
                    date.add(Calendar.MONTH,1);
                    count++;
                }
                date.set(Calendar.DAY_OF_WEEK,dayOfWeek);
                date.set(Calendar.WEEK_OF_MONTH,weekOfMonth);
                break;
            }
            case 2:{                //Monthly on Date from end of month
                int delta = date.getActualMaximum(Calendar.DAY_OF_MONTH) - date.get(Calendar.DAY_OF_MONTH);
                date.add(Calendar.MONTH,reminder.getRepeatLength());
                while (delta > date.getActualMaximum(Calendar.DAY_OF_MONTH)) { //e.g. Can't be 30 days
                                                                            //from the end of February
                    if (count == 13){
                        Log.e("Minder","Next Monthly Repeat does not exist");
                        reminder.setActive(false);
                        return;
                    }
                    date.add(Calendar.MONTH,1);
                    count++;
                }
                date.set(Calendar.DAY_OF_MONTH,date.getActualMaximum(Calendar.DAY_OF_MONTH));
                date.roll(Calendar.MONTH,-delta);
                break;
            }
        }
        reminder.setDate(date); //Commit change
    }

    //Yearly has no special use cases
    //Set the next alarm to be RepeatLength years after last alarm
    private static void nextYearlyRepeat(Reminder reminder){
        Calendar date = reminder.getDate();
        date.add(Calendar.YEAR,reminder.getRepeatLength());
        reminder.setDate(date);
    }

    //If the reminder is set to repeat, set its Date to the next repeat time
    //Otherwise, deactivate the reminder
    protected static Reminder nextRepeat(SQLiteDatabase database, Reminder reminder){
        switch (reminder.getRepeatType()) {
            case 0: {
                reminder.setActive(false);          //Deactivate
	            break;
            }
            case 1: {
                nextDailyRepeat(reminder);          //Repeat Daily
                break;
            }
            case 2: {
                nextWeeklyRepeat(reminder);         //Repeat Weekly
                break;
            }
            case 3: {
                nextMonthlyRepeat(reminder);        //Repeat Monthly
                break;
            }
            case 4: {
                nextYearlyRepeat(reminder);         //Repeat Yearly
                break;
            }
        }
        reminder.setId((int) Reminder.saveReminder(database, reminder));
	    return reminder;
    }


    //Return the appropriate appendix to convert number to an ordinal number
    //English only
    public static String appendInt(int number) {
        if (number <= 0) throw new IllegalArgumentException("Cardinal must be positive.");
        String value = String.valueOf(number);
        String appendix = "";
        if(value.length() > 1) {
            // Check for special case: 11 - 19 are all "th".
            // So if the second to last digit is 1, it is "th".
            char secondToLastDigit = value.charAt(value.length()-2);
            if(secondToLastDigit == '1')
                return "th";
        }
        char lastDigit = value.charAt(value.length()-1);    //Only the last digit affects the appendix
        switch(lastDigit) {
            case '1': {                         //1st
                appendix = "st";
                break;
            }
            case '2': {                         //2nd
                appendix = "nd";
                break;
            }
            case '3': {                         //3rd
                appendix = "rd";
                break;
            }
            default: {                          //nth
                appendix = "th";
                break;
            }
        }
        return appendix;
    }

	/******************************** Save methods ****************************************/

    public static void saveDefaults(SharedPreferences sharedPreferences, Reminder reminder){
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("temp_name",reminder.getName());
        editor.putString("temp_description",reminder.getDescription());
        editor.putBoolean("temp_vibrate",reminder.getVibrate());
        editor.putString("temp_ringtone",reminder.getRingtone());
        editor.putString("volume_override",reminder.getVolumeOverride().toString());
        if (reminder.getOnlyAtLocation()){
            editor.putString("location_type", Integer.toString(1));
        }
        else {
            if (reminder.getUntilLocation()) {
                editor.putString("location_type", Integer.toString(2));
            }
            else {
                editor.putString("location_type",Integer.toString(0));
            }
        }
        LatLng location = reminder.getLocation();
        editor.putFloat("Latitude",(float) location.latitude);
        editor.putFloat("Longitude",(float) location.longitude);
        editor.putString("radius",Integer.toString(reminder.getRadius()));
        editor.putString("temp_code",reminder.getQr());
        editor.putBoolean("code_type",reminder.getNeedQr());

        editor.putBoolean("out_loud",reminder.getVolumeOverride());
        editor.putBoolean("display_screen",reminder.getDisplayScreen());
        editor.putBoolean("wake_up",reminder.getWakeUp());
        int repeatTypeIndex = reminder.getRepeatType();
        editor.putString("temp_repeat_type", Integer.toString(repeatTypeIndex));
        editor.putString("temp_days", Integer.toString(reminder.getRepeatLength()));
        editor.putString("temp_weeks", Integer.toString(reminder.getRepeatLength()));
        byte daysOfWeek = reminder.getDaysOfWeek();
        if (Reminder.checkDayOfWeek(daysOfWeek, 1)) {
            editor.putBoolean("temp_sunday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 2)) {
            editor.putBoolean("temp_monday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 3)) {
            editor.putBoolean("temp_tuesday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 4)) {
            editor.putBoolean("temp_wednesday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 5)) {
            editor.putBoolean("temp_thursday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 6)) {
            editor.putBoolean("temp_friday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 7)) {
            editor.putBoolean("temp_saturday",true);
        }
        editor.putString("temp_months", Integer.toString(reminder.getRepeatLength()));
        editor.putString("temp_monthly_type", Integer.toString(reminder.getMonthType()));
        editor.putString("temp_years", Integer.toString(reminder.getRepeatLength()));
        editor.putString("ssid",reminder.getSSID());
        editor.putBoolean("wifi",reminder.getNeedWifi());
        editor.putString("snooze_duration",Integer.toString(reminder.getSnoozeDuration()));
        editor.putBoolean("bluetooth",reminder.getNeedBluetooth());
        editor.putString("bt_name",reminder.getBluetooth());
        editor.apply();
    }

    public void setRepeat(SharedPreferences sharedPreferences) {
        setRepeatType(Integer.parseInt(sharedPreferences.
                getString("temp_repeat_type",Integer.toString(REPEATTYPEDEFAULT))));
        switch (getRepeatType()) {
            case 1: {
                setRepeatLength(Integer.parseInt(sharedPreferences
                        .getString("temp_days", Integer.toString(REPEATLENGTHDEFAULT))));
                break;
            }
            case 2: {
                setRepeatLength(Integer.parseInt(sharedPreferences
                        .getString("temp_weeks", Integer.toString(REPEATLENGTHDEFAULT))));
                setDaysOfWeek(sharedPreferences);
                break;
            }
            case 3: {
                setRepeatLength(Integer.parseInt(sharedPreferences
                        .getString("temp_months", Integer.toString(REPEATLENGTHDEFAULT))));
                setMonthType((byte) Integer.parseInt(sharedPreferences.
                        getString("temp_monthly_type", Integer.toString(MONTHTYPEDEFAULT))));
                break;
            }
            case 4: {
                setRepeatLength(Integer.parseInt(sharedPreferences
                        .getString("temp_years", Integer.toString(REPEATLENGTHDEFAULT))));
                break;
            }
        }
    }

	private void setDaysOfWeek(SharedPreferences sharedPreferences){
		setDaysOfWeek(sharedPreferences.getBoolean("temp_sunday",false),
				sharedPreferences.getBoolean("temp_monday",false),
				sharedPreferences.getBoolean("temp_tuesday",false),
				sharedPreferences.getBoolean("temp_wednesday",false),
				sharedPreferences.getBoolean("temp_thursday",false),
				sharedPreferences.getBoolean("temp_friday",false),
				sharedPreferences.getBoolean("temp_saturday",false));
	}

    public void setLocation(SharedPreferences sharedPreferences){
        int locationType = Integer.parseInt(sharedPreferences.getString("location_type", "-1"));
        switch (locationType){
            case 0: {
                setOnlyAtLocation(false);
                setUntilLocation(false);
                break;
            }
            case 1: {
                setOnlyAtLocation(true);
                setUntilLocation(false);
                break;
            }
            case 2: {
                setOnlyAtLocation(false);
                setUntilLocation(true);
                break;
            }
            default: {
                setOnlyAtLocation(ONLYATLOCATIONDEFAULT);
                setUntilLocation(UNTILLOCATIONDEFAULT);
                break;
            }
        }

        LatLng location = new LatLng(sharedPreferences.getFloat("Latitude",(float)LOCATIONDEFAULT.latitude),
                sharedPreferences.getFloat("Longitude",(float)LOCATIONDEFAULT.longitude));
        setLocation(location);
    }

    public void setDate(SharedPreferences sharedPreferences, Context context) {
        Calendar date = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa EEEE, MMMM d, yyyy");
        try {
            String newDate = sharedPreferences.getString("temp_time", "") + " " + sharedPreferences.getString("temp_date", "");
            if (!newDate.equals(" ")) {
                date.setTime(timeFormat.parse(newDate));
            }
        }
        catch (ParseException e){
            Log.e("Minder","Parse Error");
        }
        if ((!Reminder.checkDayOfWeek(getDaysOfWeek(),          //If initial day is not in
                date.get(Calendar.DAY_OF_WEEK)))&&(getRepeatType()==2)){                       //repeat pattern, skip
            ReminderDBHelper dbHelper = ReminderDBHelper.getInstance(context);
            SQLiteDatabase database = dbHelper.openDatabase();
            Reminder.nextRepeat(database,this);
            dbHelper.closeDatabase();
        }
        setDate(date);                                         //Store reminder date + time
    }

    public static Reminder preferenceToReminder(SharedPreferences sharedPreferences, Reminder reminder, Context context){
        reminder.setName(sharedPreferences.getString("temp_name",NAMEDEFAULT));
        reminder.setDescription(sharedPreferences.getString("temp_description", DESCRIPTIONDEFAULT));
        reminder.setDate(sharedPreferences, context);
        reminder.setRepeat(sharedPreferences);
        reminder.setLocation(sharedPreferences);
        reminder.setRadius(sharedPreferences.getInt("radius",Reminder.RADIUSDEFAULT));
        reminder.setVibrate(sharedPreferences.getBoolean("temp_vibrate", VIBRATEDEFAULT));
        reminder.setRingtone(sharedPreferences.getString("temp_ringtone", RINGTONEDEFAULT));
        reminder.setActive(reminder.getDate().after(Calendar.getInstance()));
        reminder.setQr(sharedPreferences.getString("temp_code",QRDEFAULT));
        reminder.setNeedQr(sharedPreferences.getBoolean("code_type",NEEDQRDEFAULT));
        reminder.setVolumeOverride(sharedPreferences.getBoolean("out_loud",VOLUMEOVERRIDEDEFAULT));
        reminder.setDisplayScreen(sharedPreferences.getBoolean("display_screen",DISPLAYSCREENDEFAULT));
        reminder.setWakeUp(sharedPreferences.getBoolean("wake_up",WAKEUPDEFAULT));
        reminder.setSSID(sharedPreferences.getString("ssid",SSIDDEFAULT));
        reminder.setNeedWifi(sharedPreferences.getBoolean("wifi",WIFIDEFAULT));
        reminder.setNeedBluetooth(sharedPreferences.getBoolean("bluetooth",BTNEEDEDDEFAULT));
        reminder.setBluetooth(sharedPreferences.getString("bt_name",BTDEFAULT));
        reminder.setSnoozeDuration(Integer.parseInt(sharedPreferences
                .getString("snooze_duration",Integer.toString(SNOOZEDURATIONDEFAULT))));
	    reminder.setFadeVolume(sharedPreferences.getBoolean("fade",FADEDEFAULT));
	    reminder.setConfirmDismiss(sharedPreferences.getBoolean("dismiss_check",DISMISSDIALOGDEFAULT));
        return reminder;
    }
}
