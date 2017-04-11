package us.bridgeses.Minder;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import us.bridgeses.Minder.editor.EditStyle;
import us.bridgeses.Minder.persistence.dao.DaoFactory;
import us.bridgeses.Minder.persistence.dao.ReminderDAO;
import us.bridgeses.Minder.reminder.Conditions;

import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ACTIVE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DATE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DAYSOFWEEK;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DESCRIPTION;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ID;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_IMAGE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LEDCOLOR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LEDPATTERN;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_MONTHTYPE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_NAME;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_PERSISTENCE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_QR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_REPEATLENGTH;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_REPEATTYPE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_RINGTONE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_SNOOZEDURATION;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_SNOOZENUM;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_STYLE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_TEXTCOLOR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_VOLUME;

/**
 * Model class for a Reminder
 */

public class Reminder implements Parcelable, Cloneable {

	private static final String TAG = "Reminder";
// TODO: 1/3/2017  break this up!!!
//Constructors
    public Reminder() {
        setActive(ACTIVEDEFAULT);
		conditions = new Conditions();
        name = NAMEDEFAULT;
        repeatType = REPEATTYPEDEFAULT;
        repeatLength = REPEATLENGTHDEFAULT;
        daysOfWeek = DAYSOFWEEKDEFAULT;
        monthType = MONTHTYPEDEFAULT;
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
        setWakeUp(WAKEUPDEFAULT);
        setDisplayScreen(DISPLAYSCREENDEFAULT);
	    setConfirmDismiss(DISMISSDIALOGDEFAULT);
	    setFadeVolume(FADEDEFAULT);
	    setVibrateRepeat(VIBRATEREPEATDEFAULT);
	    setVolume(VOLUMEDEFAULT);
	    setSnoozeNumber(SNOOZENUMDEFAULT);
        setImage(IMAGEDEFAULT);
        setTextColor(TEXTCOLORDEFAULT);
    }
	
	public Reminder(@NonNull Cursor cursor) {
		if (cursor.isBeforeFirst() || cursor.isAfterLast()) {
			throw new IllegalArgumentException("Cursor is not pointing at a valid row");
		}
		setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
		setActive(cursor.getInt(cursor.getColumnIndex(COLUMN_ACTIVE)) == 1);
		setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
		setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
		setDate(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)) * 1000);
		setDaysOfWeek((byte) cursor.getInt(cursor.getColumnIndex(COLUMN_DAYSOFWEEK)));
		setMonthType((byte) cursor.getInt(cursor.getColumnIndex(COLUMN_MONTHTYPE)));
		setRepeatLength(cursor.getInt(cursor.getColumnIndex(COLUMN_REPEATLENGTH)));
		setRepeatType(cursor.getInt(cursor.getColumnIndex(COLUMN_REPEATTYPE)));
		setQr(cursor.getString(cursor.getColumnIndex(COLUMN_QR)));
		setPersistence((byte) cursor.getInt(cursor.getColumnIndex(COLUMN_PERSISTENCE)));
		setStyle((byte) cursor.getInt(cursor.getColumnIndex(COLUMN_STYLE)));
		setSnoozeDuration(cursor.getInt(cursor.getColumnIndex(COLUMN_SNOOZEDURATION)));
		setLedColor(cursor.getInt(cursor.getColumnIndex(COLUMN_LEDCOLOR)));
		setLedPattern(cursor.getInt(cursor.getColumnIndex(COLUMN_LEDPATTERN)));
		setSnoozeNumber(cursor.getInt(cursor.getColumnIndex(COLUMN_SNOOZENUM)));
		try {
			setRingtone(cursor.getString(cursor.getColumnIndex(COLUMN_RINGTONE)));
		}
		catch (NullPointerException e){
			setRingtone("");
		}
		setVolume(cursor.getInt(cursor.getColumnIndex(COLUMN_VOLUME)));
		setLedColor(cursor.getInt(cursor.getColumnIndex(COLUMN_LEDCOLOR)));
		setTextColor(cursor.getInt(cursor.getColumnIndex(COLUMN_TEXTCOLOR)));
		setImage(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE)));
		setConditions(new Conditions(cursor));
	}

    /**
     * Return a new reminder from the defaults in the given SharedPreferences
     * @param sharedPreferences
     * @param context
     * @return
     */
    public static Reminder reminderFactory(SharedPreferences sharedPreferences, Context context){
        return preferenceToReminder(sharedPreferences, context);
    }

    /**
     * Return a new reminder from the standard defaults
     * @param context The application context
     * @return A reminder
     */
	public static Reminder reminderFactory(Context context){
		// TODO: 2/17/2017 Why oh why is our model class in charge of Preference management?
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferenceToReminder(sharedPreferences, context);
	}

    /* Parcelable constructor methods */
    public Reminder(Parcel in){
        readFromParcel(in);
    }

    // Return if two Reminders are equal for display purposes
	public boolean displayEquals(Object obj) {
		if (!(obj instanceof Reminder)) {
			return false;
		}
		Reminder other = (Reminder) obj;
		return this.getName().equals(other.getName())
				&& (this.getActive() == other.getActive())
				&& this.getDescription().equals(other.getDescription())
				&& this.repeatType == other.repeatType
				&& this.date.equals(other.date);
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
	private boolean active;
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
                                                     (e.g. the last friday, not yet implemented)*/
    private byte daysOfWeek;                   //Bitwise byte representing seven boolean values for the seven days of the week
    private byte persistence;                  //Bitwise byte representing an array of boolean values related to reminder Persistence
    private Conditions conditions;                   //Bitwise byte representing an array of boolean values related to reminder Conditions
    private byte style;                        //Bitwise byte representing an array of boolean values related to reminder Style
    private Calendar date;                     //The date and time at which the reminder should fire, truncated to second
    private String qr;                         //A string representing the encoded value of a barcode or QR code, scanned in by user
	private int snoozeNumber;                  //A limit on the number of times the reminder can be snoozed
    private int snoozeDuration;                //The default duration before trying the reminder again if not dismissed
    private int ledColor;                      //An int representing the hexadecimal color of the LED
    private int ledPattern;                    //An int representing the pattern in which the LED should flash
    private String ringtone;                   //A string representing a URI for a ringtone selected by the user, to be played when reminder fires
	private int volume;                        //An integer representing the volume ratio out of 100
	private String image;                      //A path to a background image
    private int textColor;                     //A hexadecimal representation of font color

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
    public static final boolean LEDDEFAULT = true;
    public static final int LEDCOLORDEFAULT = 0xff000000;
    public static final int LEDPATTERNDEFAULT = -1;
    public static final int IDDEFAULT = -1;
    public static final int RADIUSDEFAULT = 200;
    public static final String SSIDDEFAULT = "";
    public static final String BTDEFAULT = "";
    public static final boolean WIFIDEFAULT = false;
    public static final boolean BTNEEDEDDEFAULT = false;
	public static final boolean DISMISSDIALOGDEFAULT = false;
	public static final boolean FADEDEFAULT = false;
	public static final boolean VIBRATEREPEATDEFAULT = false;
	public static final int VOLUMEDEFAULT = 80;
	public static final boolean INSISTENTDEFAULT = true;
	public static final int SNOOZENUMDEFAULT = -1;
    public static final String IMAGEDEFAULT = "";
    public static final int TEXTCOLORDEFAULT = 0xff000000;

    //Time constants
    public static final long MINUTE = TimeUnit.MILLISECONDS.convert(1L, TimeUnit.MINUTES);
    public static final long HOUR = 60*MINUTE;
    public static final long DAY = 24*HOUR;
    public static final long WEEK = 7*DAY;
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
    public static final byte MWF = MONDAY + WEDNESDAY + FRIDAY;
    public static final byte TTH = TUESDAY + THURSDAY;

	//Persistence constants
	public static final byte VOLUME_OVERRIDE = 1;
	public static final byte REQUIRE_CODE = 2;
    public static final byte DISPLAY_SCREEN = 4;
    public static final byte WAKE_UP = 8;
	public static final byte DISMISS_DIALOG = 16;
	public static final byte INSISTENT = 32;

    //Conditions constants
    public static final byte UNTIL_LOCATION = 1;
    public static final byte ONLY_AT_LOCATION = 2;
    public static final byte WIFINEEDED = 4;
    public static final byte BLUETOOTHNEEDED = 8;
    public static final byte ACTIVE = 16;

    //Style constants
    public static final byte LED = 1;
    public static final byte VIBRATE = 2;
	public static final byte VIBRATEREPEAT = 4;
	public static final byte FADE = 8;

    public static final String PREFS_NAME = "ReminderPrefs";

    public void setImage(String image){
        this.image = image;
    }

    public String getImage(){
        return image;
    }

    public void setTextColor(int textColor){
        this.textColor = textColor;
    }

    public int getTextColor(){
        return textColor;
    }

    public void setSSID(String ssid){
        conditions.setSsid(ssid);
    }

    public String getSSID(){
        return conditions.getSsid();
    }

    public String getBluetooth(){
        return conditions.getBtMacAddress();
    }

    public void setBluetooth(String bluetooth){
        this.conditions.setBtMacAddress(bluetooth);
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
		if (id >= -1){
			this.id = id;
		}
		else {
			throw new IllegalArgumentException("ID must be greater than zero");
		}
	}

	public LatLng getLocation() {
		return conditions.getLatLng();
	}

	public void setLocation(LatLng location) {
		conditions.setLatLng(location);
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
		this.description = description;
	}

	public String getQr() {
		return qr;
	}

	public void setQr(String qr) {
		this.qr = qr;
	}

	public int getSnoozeNumber() {
		return snoozeNumber;
	}

	public void setSnoozeNumber(int snoozeNumber){
		this.snoozeNumber = snoozeNumber;
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
        this.ledColor = ledColor;
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
		conditions.setRadius(radius);
	}

	public int getRadius(){
		return conditions.getRadius();
	}

	public void setVolume(int volume){
		this.volume = volume;
	}

	public int getVolume(){
		return volume;
	}

	public Conditions.LocationPreference getLocationType(){

		return conditions.getLocationPreference();
	}

	/******************************* Bitwise getters and setters *************************/
    public void setConditions (Conditions conditions){
		// Defensive copy
        this.conditions = new Conditions(conditions);
    }

	public void setConditions(byte conditions) {
		this.conditions = new Conditions(conditions);

	}

    public Conditions getConditions() {
        return new Conditions(conditions);
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

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

	/************************ Persistence bitwise getters and setters **********************/

	public boolean getNeedQr() {
		return getBitwise(this.getPersistence(),REQUIRE_CODE);
	}

	public void setNeedQr(boolean needQr) {
		this.setPersistence(makeBitwise(this.getPersistence(),REQUIRE_CODE,needQr));
	}

	public boolean getVolumeOverride(){
		return getBitwise(this.getPersistence(),VOLUME_OVERRIDE);
	}

	public void setVolumeOverride(boolean volumeOverride){
		this.setPersistence(makeBitwise(this.getPersistence(),VOLUME_OVERRIDE,volumeOverride));
	}

	public boolean getDisplayScreen(){
		return getBitwise(this.getPersistence(),DISPLAY_SCREEN);
	}

	public void setDisplayScreen(boolean displayScreen){
		this.setPersistence(makeBitwise(this.getPersistence(),DISPLAY_SCREEN,displayScreen));
	}

	public boolean getWakeUp(){
		return getBitwise(this.getPersistence(),WAKE_UP);

	}

	public void setWakeUp(boolean wakeUp){
		this.setPersistence(makeBitwise(this.getPersistence(),WAKE_UP,wakeUp));
	}

	public boolean getConfirmDismiss(){
		return getBitwise(this.getPersistence(),DISMISS_DIALOG);

	}

	public void setConfirmDismiss(boolean dismissDialog){
		this.setPersistence(makeBitwise(this.getPersistence(),DISMISS_DIALOG,dismissDialog));
	}

	public boolean isInsistent(){
		return getBitwise(this.getPersistence(),INSISTENT);
	}

	public void setInsistent(boolean insistent){
		this.setPersistence(makeBitwise(this.getPersistence(),INSISTENT,insistent));
	}

	/*************************** Style bitwise getters and setters ************************/

	public boolean getFadeVolume(){
		return getBitwise(this.getStyle(),FADE);
	}

	public void setFadeVolume(boolean fade){
		this.setStyle(makeBitwise(this.getStyle(),FADE,fade));
	}

	public boolean getVibrate() {
		return getBitwise(this.getStyle(),VIBRATE);
	}

	public void setVibrate(boolean vibrate) {
		this.setStyle(makeBitwise(this.getStyle(),VIBRATE,vibrate));
	}

	public boolean getLed() {
		return getBitwise(this.getStyle(),LED);
	}

	public void setLed(boolean led) {
		this.setStyle(makeBitwise(this.getStyle(),LED,led));
	}
	
	public boolean getVibrateRepeat() {
		return getBitwise(this.getStyle(),VIBRATEREPEAT);
	}

	public void setVibrateRepeat(boolean vibrateRepeat) {
		this.setStyle(makeBitwise(this.getStyle(), VIBRATEREPEAT, vibrateRepeat));
	}

	/***************************** Storage methods ********************************/
	//// TODO: 1/3/2017 Invert these dependencies
	public Reminder save(Context context){
		DaoFactory daoFactory = DaoFactory.getInstance();
		ReminderDAO dao = daoFactory.getDao(context);
		return dao.saveReminder(this);
	}

	public int delete(Context context){
		DaoFactory daoFactory = DaoFactory.getInstance();
		ReminderDAO dao = daoFactory.getDao(context);
		return dao.deleteReminder(getId());
	}

	public static Reminder get(Context context, int id){
		DaoFactory daoFactory = DaoFactory.getInstance();
		ReminderDAO dao = daoFactory.getDao(context);
		return dao.getReminder(id);
	}

	public static Reminder[] getAll(Context context){
		DaoFactory daoFactory = DaoFactory.getInstance();
		ReminderDAO dao = daoFactory.getDao(context);
		return dao.getReminders();
	}
	
	public ContentValues toContentValues() {
		ContentValues values = new ContentValues();
		return toContentValues(values);
	}

	private ContentValues toContentValues(ContentValues values) {
		if (getId() != -1)
			values.put(COLUMN_ID,getId());

		values.put(COLUMN_ACTIVE,getActive());
		values.put(COLUMN_NAME,getName());
		values.put(COLUMN_DESCRIPTION,getDescription());
		values.put(COLUMN_DATE,getDate().getTimeInMillis()/1000);
		values.put(COLUMN_DAYSOFWEEK,getDaysOfWeek());
		values.put(COLUMN_MONTHTYPE,getMonthType());
		values.put(COLUMN_REPEATTYPE,getRepeatType());
		values.put(COLUMN_REPEATLENGTH,getRepeatLength());
		values.put(COLUMN_RINGTONE,getRingtone());
		values.put(COLUMN_PERSISTENCE,getPersistence());
		values.put(COLUMN_QR,getQr());
		values.put(COLUMN_SNOOZEDURATION, getSnoozeDuration());
		values.put(COLUMN_STYLE, getStyle());
		values.put(COLUMN_VOLUME, getVolume());
		values.put(COLUMN_SNOOZENUM, getSnoozeNumber());
		values.put(COLUMN_LEDCOLOR, getLedColor());
		values.put(COLUMN_TEXTCOLOR, getTextColor());
		values.put(COLUMN_IMAGE, getImage());
		conditions.toContentValues(values);
		return values;
	}

	/******************************** Parcel Methods ******************************/

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(name);
        out.writeInt(repeatType);
        out.writeInt(repeatLength);
	    out.writeByte(monthType);
        out.writeByte(daysOfWeek);
        out.writeByte(persistence);
        out.writeSerializable(date);
        out.writeString(description);
        out.writeString(qr);
        out.writeInt(snoozeDuration);
        out.writeInt(ledColor);
        out.writeInt(ledPattern);
        out.writeString(ringtone);
        out.writeParcelable(conditions, 0);
        out.writeByte(style);
	    out.writeInt(volume);
	    out.writeInt(snoozeNumber);
        out.writeString(image);
        out.writeInt(textColor);
		out.writeParcelable(conditions, 0);
    }

    public void readFromParcel(Parcel in){
        id = in.readInt();
        name = in.readString();
        repeatType = in.readInt();
        repeatLength = in.readInt();
	    monthType = in.readByte();
        daysOfWeek = in.readByte();
        persistence = in.readByte();
        date = (Calendar) in.readSerializable();
        description = in.readString();
        qr = in.readString();
        snoozeDuration = in.readInt();
        ledColor = in.readInt();
        ledPattern = in.readInt();
        ringtone = in.readString();
        conditions = in.readParcelable(Conditions.class.getClassLoader());
        style = in.readByte();
	    volume = in.readInt();
	    snoozeNumber = in.readInt();
        image = in.readString();
        textColor = in.readInt();
		conditions = in.readParcelable(Conditions.class.getClassLoader());
    }

    @Override
    public int describeContents(){
        return 0;
    }

	/********************************* Repeat Methods ****************************************/

    //Returns true if thisDay is in bitwise set daysOfWeek
    public static boolean checkDayOfWeek(byte daysOfWeek, int thisDay) {	//Call with checkDayOfWeek(reminder.getDaysOfWeek,thisDay)
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
        boolean repeat = false;
        int count = 0;      //Count checks for error conditions and prevents infinite loops
        while (!repeat) {
            if (count > (7*(reminder.getRepeatLength()+1))){ //Cycled through entire week without finding match
                Logger.e("Next Weekly Repeat does not exist");
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
                        Logger.e("Next Monthly Repeat does not exist");
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
                        Logger.e("Next Monthly Repeat does not exist");
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
                        Logger.e("Next Monthly Repeat does not exist");
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
	        case 3:{            //Monthly on Day of Week from end of month
		        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
		        int weekOfMonth = date.get(Calendar.WEEK_OF_MONTH);
		        int weeksInMonth = date.getActualMaximum(Calendar.WEEK_OF_MONTH);
		        int weeksFromEnd = weeksInMonth - weekOfMonth;
		        date.add(Calendar.MONTH,reminder.getRepeatLength()+1);
		        while (weeksFromEnd > date.getActualMaximum(Calendar.WEEK_OF_MONTH)) {
			        if (count == 13){
				        Logger.e("Next Monthly Repeat does not exist");
				        reminder.setActive(false);
				        return;
			        }
			        date.add(Calendar.MONTH,1);
			        count++;
		        }
		        date.set(Calendar.DAY_OF_WEEK,dayOfWeek);
		        date.set(Calendar.DAY_OF_WEEK_IN_MONTH,0-weeksFromEnd);
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
    public static Reminder nextRepeat(Reminder reminder){
		Reminder nextReminder = reminder.clone();
        switch (reminder.getRepeatType()) {
            case 0: {
				Log.d(TAG, "nextRepeat: deactivating");
				nextReminder.setActive(false);          //Deactivate
	            break;
            }
            case 1: {
                nextDailyRepeat(nextReminder);          //Repeat Daily
                break;
            }
            case 2: {
                nextWeeklyRepeat(nextReminder);         //Repeat Weekly
                break;
            }
            case 3: {
                nextMonthlyRepeat(nextReminder);        //Repeat Monthly
                break;
            }
            case 4: {
                nextYearlyRepeat(nextReminder);         //Repeat Yearly
                break;
            }
        }
		if ((nextReminder.getDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) &&
                (nextReminder.getRepeatType() != 0)){
            nextReminder = nextRepeat(reminder);
        }
	    return nextReminder;
    }


    //Return the appropriate appendix to convert number to an ordinal number
    //English only
    public static String appendInt(int number) {
        if (number <= 0) throw new IllegalArgumentException("Cardinal must be positive.");
        String value = String.valueOf(number);
        String appendix;
        if(value.length() > 1) {
            // Check for special case: 11 - 19 are all "th".
            // So if the second to last digit is 1, it is "th".
            char secondToLastDigit = value.charAt(value.length()-2);
            if(secondToLastDigit == '1')
                return "th";
        }
        char lastDigit = value.charAt(value.length() - 1);    //Only the last digit affects the appendix
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

	/******************************** Preference methods ****************************************/

	public static SharedPreferences.Editor dateToPreference(SharedPreferences.Editor editor, Calendar calendar){
		SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa;EEEE, MMMM d, yyyy");
		String fullString = timeFormat.format(calendar.getTime());
		String[] results = fullString.split("[;]");
		editor.putString("temp_time",results[0]);
		editor.putString("temp_date",results[1]);
		return editor;
	}

    public static void loadImage(Context context, int id){
        File file = new File(context.getExternalFilesDir(null), EditStyle.tempFile);
        File saveFile = new File(context.getFilesDir(), Integer.toString(id));
        if (file.exists()){
            file.delete();
        }
        if (saveFile.exists()){
            Logger.d("Loading image");
            try {
                InputStream in = new FileInputStream(saveFile);

                OutputStream out = new FileOutputStream(file);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
            catch(IOException e){
                Logger.e("Error copying file");
                e.printStackTrace();
            }
        }
    }

    public static void reminderToPreference(Context context, SharedPreferences sharedPreferences, Reminder reminder){
        SharedPreferences.Editor editor = sharedPreferences.edit();
	    editor.putInt("temp_id",reminder.getId());
        editor.putString("temp_name",reminder.getName());
	    editor = dateToPreference(editor,reminder.getDate());
        editor.putString("temp_description",reminder.getDescription());
        editor.putBoolean("temp_vibrate",reminder.getVibrate());
        editor.putString("temp_ringtone",reminder.getRingtone());
        editor.putBoolean("volume_override", reminder.getVolumeOverride());
	    editor.putString("location_type",Integer.toString(reminder.getLocationType().ordinal()));
        LatLng location = reminder.getLocation();
        editor.putFloat("Latitude",(float) location.latitude);
        editor.putFloat("Longitude",(float) location.longitude);
        editor.putInt("radius",reminder.getRadius());
        editor.putString("temp_code",reminder.getQr());
        editor.putBoolean("code_type",reminder.getNeedQr());

        editor.putBoolean("out_loud",reminder.getVolumeOverride());
        editor.putBoolean("display_screen",reminder.getDisplayScreen());
        editor.putBoolean("wake_up",reminder.getWakeUp());
        editor.putString("temp_repeat_type", Integer.toString(reminder.getRepeatType()));
	    
	    //These four are EditTextPreferences and must be handled as strings
        editor.putString("temp_days", Integer.toString(reminder.getRepeatLength()));
        editor.putString("temp_weeks", Integer.toString(reminder.getRepeatLength()));
	    editor.putString("temp_months", Integer.toString(reminder.getRepeatLength()));
	    editor.putString("temp_years", Integer.toString(reminder.getRepeatLength()));
	    editor.putString("snooze_number", Integer.toString(reminder.getSnoozeNumber()));
	    
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
        
        editor.putString("temp_monthly_type", Integer.toString(reminder.getMonthType()));
        
        editor.putString("ssid", reminder.getSSID());
        editor.putBoolean("wifi", reminder.getConditions().getWifiPreference() ==
				Conditions.WifiPreference.CONNECTED);
        editor.putString("snooze_duration", Integer.toString(reminder.getSnoozeDuration()));
        editor.putBoolean("bluetooth", reminder.getConditions().getBluetoothPreference() ==
				Conditions.BluetoothPreference.CONNECTED);
        editor.putString("bt_name", reminder.getBluetooth());
	    editor.putInt("volume", reminder.getVolume());
	    editor.putBoolean("vibrate_repeat", reminder.getVibrateRepeat());
	    editor.putBoolean("led",reminder.getLed());
	    editor.putBoolean("dismiss_check", reminder.getConfirmDismiss());
	    editor.putBoolean("try_again", reminder.isInsistent());
        loadImage(context,reminder.getId());
        editor.putInt("font_color",reminder.getTextColor());
        editor.putInt("led_color",reminder.getLedColor());
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
        int locationType = Integer.valueOf(sharedPreferences.getString("location_type", "-1"));
        conditions.setLocationPreference(Conditions.LocationPreference.values()[locationType]);

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
	            Logger.d("Date set: "+newDate);
                date.setTime(timeFormat.parse(newDate));
            }
        }
        catch (ParseException e){
            Log.e("Minder","Parse Error");
        }
        if ((!Reminder.checkDayOfWeek(getDaysOfWeek(),          //If initial day is not in
                date.get(Calendar.DAY_OF_WEEK)))&&(getRepeatType()==2)){                       //repeat pattern, skip
            Reminder.nextRepeat(this).save(context);
        }
        setDate(date);                                         //Store reminder date + time
    }

    public static Reminder preferenceToReminder(SharedPreferences sharedPreferences, Context context){
        Reminder reminder = new Reminder();
	    reminder.setId(sharedPreferences.getInt("temp_id", IDDEFAULT));
	    reminder.setName(sharedPreferences.getString("temp_name", NAMEDEFAULT));
        reminder.setDescription(sharedPreferences.getString("temp_description", DESCRIPTIONDEFAULT));
        reminder.setDate(sharedPreferences, context);
        reminder.setRepeat(sharedPreferences);
        reminder.setLocation(sharedPreferences);
        reminder.setRadius(sharedPreferences.getInt("radius", RADIUSDEFAULT));
        reminder.setVibrate(sharedPreferences.getBoolean("temp_vibrate", VIBRATEDEFAULT));
        reminder.setRingtone(sharedPreferences.getString("temp_ringtone", RINGTONEDEFAULT));
        reminder.setActive(reminder.getDate().after(Calendar.getInstance()));
        reminder.setQr(sharedPreferences.getString("temp_code", QRDEFAULT));
        reminder.setNeedQr(sharedPreferences.getBoolean("code_type", NEEDQRDEFAULT));
        reminder.setVolumeOverride(sharedPreferences.getBoolean("out_loud", VOLUMEOVERRIDEDEFAULT));
        reminder.setDisplayScreen(sharedPreferences.getBoolean("display_screen", DISPLAYSCREENDEFAULT));
        reminder.setWakeUp(sharedPreferences.getBoolean("wake_up", WAKEUPDEFAULT));
        reminder.setSSID(sharedPreferences.getString("ssid", SSIDDEFAULT));
		if (sharedPreferences.getBoolean("wifi", false)) {
			reminder.getConditions().setWifiPreference(Conditions.WifiPreference.CONNECTED);
		}
		else {
			reminder.getConditions().setWifiPreference(Conditions.WifiPreference.NONE);
		}
		if (sharedPreferences.getBoolean("bluetooth", false)) {
			reminder.getConditions().setBluetoothPreference(Conditions.BluetoothPreference.CONNECTED);
		}
		else {
			reminder.getConditions().setBluetoothPreference(Conditions.BluetoothPreference.NONE);
		}
        reminder.getConditions().setBtMacAddress(sharedPreferences.getString("bt_name", BTDEFAULT));
        reminder.setSnoozeDuration(Integer.parseInt(sharedPreferences
                .getString("snooze_duration", Integer.toString(SNOOZEDURATIONDEFAULT))));
	    reminder.setSnoozeNumber(Integer.parseInt(sharedPreferences
                .getString("snooze_number", Integer.toString(SNOOZENUMDEFAULT))));
	    reminder.setFadeVolume(sharedPreferences.getBoolean("fade", FADEDEFAULT));
	    reminder.setConfirmDismiss(sharedPreferences.getBoolean("dismiss_check", DISMISSDIALOGDEFAULT));
	    reminder.setVibrateRepeat(sharedPreferences.getBoolean("vibrate_repeat", VIBRATEREPEATDEFAULT));
	    reminder.setLed(sharedPreferences.getBoolean("led", LEDDEFAULT));
	    reminder.setVolume(sharedPreferences.getInt("volume", VOLUMEDEFAULT));
	    reminder.setInsistent(sharedPreferences.getBoolean("try_again", INSISTENTDEFAULT));
        File file = new File(context.getExternalFilesDir(null), EditStyle.tempFile);
        File saveFile = new File(context.getFilesDir(), Integer.toString(reminder.getId()));
        if (saveFile.exists()){
            saveFile.delete();
        }
        if (file.exists()){
            Logger.d("Saveing image");
            file.renameTo(saveFile);
        }
        reminder.setTextColor(sharedPreferences.getInt("font_color",TEXTCOLORDEFAULT));
        reminder.setLedColor(sharedPreferences.getInt("led_color",LEDCOLORDEFAULT));
        return reminder;
    }

	@Override
	public Reminder clone() {
		try {
			return (Reminder)super.clone();
		}
		catch (CloneNotSupportedException e) {
			// Should not get here
			e.printStackTrace();
		}
		return null;
	}
}
