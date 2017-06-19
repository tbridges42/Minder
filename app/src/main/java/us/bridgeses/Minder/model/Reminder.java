package us.bridgeses.Minder.model;

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

import us.bridgeses.Minder.editor.EditStyle;
import us.bridgeses.Minder.persistence.dao.DaoFactory;
import us.bridgeses.Minder.persistence.dao.ReminderDAO;

import static us.bridgeses.Minder.model.Conditions.BT_MAC_ADDRESS_DEFAULT;
import static us.bridgeses.Minder.model.Conditions.LATITUDE_DEFAULT;
import static us.bridgeses.Minder.model.Conditions.LONGITUDE_DEFAULT;
import static us.bridgeses.Minder.model.Conditions.RADIUS_DEFAULT;
import static us.bridgeses.Minder.model.Conditions.SSID_DEFAULT;
import static us.bridgeses.Minder.model.Persistence.CODE_DEFAULT;
import static us.bridgeses.Minder.model.Persistence.SNOOZE_LIMIT_DEFAULT;
import static us.bridgeses.Minder.model.Persistence.SNOOZE_TIME_DEFAULT;
import static us.bridgeses.Minder.model.Persistence.VOLUME_DEFAULT;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.FRIDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.MONDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.SATURDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.SUNDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.THURSDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.TUESDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.WEDNESDAY;
import static us.bridgeses.Minder.model.Repeat.REPEAT_DATE_TYPE_DEFAULT;
import static us.bridgeses.Minder.model.Repeat.REPEAT_PERIOD_DEFAULT;
import static us.bridgeses.Minder.model.Repeat.REPEAT_TYPE_DEFAULT;
import static us.bridgeses.Minder.model.Style.LED_COLOR_DEFAULT;
import static us.bridgeses.Minder.model.Style.RINGTONE_DEFAULT;
import static us.bridgeses.Minder.model.Style.TEXT_COLOR_DEFAULT;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ACTIVE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DATE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DESCRIPTION;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ID;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_NAME;

/**
 * Model class for a Reminder
 */

public class Reminder implements Parcelable, Cloneable, ReminderComponent {

	private static final String TAG = "Reminder";
// TODO: 1/3/2017  break this up!!!
//Constructors
    public Reminder() {
        setActive(ACTIVEDEFAULT);
		conditions = new Conditions();
		persistence = new Persistence();
        name = NAMEDEFAULT;
        date = Calendar.getInstance();
        description = DESCRIPTIONDEFAULT;
        id = IDDEFAULT;
		style = new Style();
        repeat = new Repeat();
        conditions = new Conditions();
        persistence = new Persistence();
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
		setRepeat(new Repeat(cursor));
		setPersistence(new Persistence(cursor));
		setStyle(new Style(cursor));
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
				&& this.getRepeat().equals(other.getRepeat())
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
    private Persistence persistence;
    private Conditions conditions;
	private Repeat repeat;
    private Style style;                        //Bitwise byte representing an array of boolean values related to reminder Style
    private Calendar date;                     //The date and time at which the reminder should fire, truncated to second

    //Default constants
    public static final boolean ACTIVEDEFAULT = true;
    public static final String NAMEDEFAULT = "";
    public static final String DESCRIPTIONDEFAULT = "";
    public static final int IDDEFAULT = -1;

    public static final String PREFS_NAME = "ReminderPrefs";

    public void setImage(String image){
        style.setImagePath(image);
    }

    public String getImage(){
        return style.getImagePath();
    }

    public void setTextColor(int textColor){
        style.setTextColor(textColor);
    }

    public int getTextColor(){
        return style.getTextColor();
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

	public @Repeat.DateType int getMonthType() {
		return getRepeat().getDateType();
	}

	public void setMonthType(@Repeat.DateType int monthType) {
		if ((0 <= monthType) && (monthType <= 3)){
			this.getRepeat().setDateType(monthType);
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
        if (repeat != null) {
            return getRepeat().getRepeatType();
        }
        else {
            return Repeat.RepeatType.NONE;
        }
	}

	public void setRepeatType(@Repeat.RepeatType int repeatType) {
		if ((0 <= repeatType) && (repeatType <= 4)){
			getRepeat().setRepeatType(repeatType);
		}
		else {
			throw new IllegalArgumentException("Repeat type must be between 0 and 4");
		}
	}

	public int getRepeatLength() {
		return getRepeat().getRepeatPeriod();
	}

	public void setRepeatLength(int repeatLength) {
		if (0 < repeatLength){
			getRepeat().setRepeatPeriod(repeatLength);
		}
		else {
			throw new IllegalArgumentException("Repeat length must be greater than zero");
		}
	}

	public int getDaysOfWeek() {
		return getRepeat().getDaysOfWeek();
	}

	//TODO: Move days of week text to getDaysOfWeekNames(int length) where length=1 > umtwrfs; length=2 > MoTuWeThFrSa (weekdays, weekends, every day);
	//length = 3 = SunMonTueWedThuFriSat (weekdays, weekends, every day); index > 3 = Sunday Monday Tuesday Wednesday Thursday Friday Saturday

	public void setDaysOfWeek(byte daysOfWeek) {
		//TODO: Move daysOfWeek logic inside setter
		getRepeat().setDaysOfWeek(daysOfWeek);
	}

	public void setDaysOfWeek(boolean Sunday, boolean Monday, boolean Tuesday, boolean Wednesday,
	                          boolean Thursday, boolean Friday, boolean Saturday){
		repeat.setDayOfWeek(SUNDAY, Sunday);
		repeat.setDayOfWeek(Repeat.DaysOfWeek.MONDAY, Monday);
		repeat.setDayOfWeek(Repeat.DaysOfWeek.TUESDAY, Tuesday);
		repeat.setDayOfWeek(WEDNESDAY, Wednesday);
		repeat.setDayOfWeek(Repeat.DaysOfWeek.THURSDAY, Thursday);
		repeat.setDayOfWeek(Repeat.DaysOfWeek.FRIDAY, Friday);
		repeat.setDayOfWeek(Repeat.DaysOfWeek.SATURDAY, Saturday);
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
		return persistence.getCode();
	}

	public void setQr(String qr) {
		this.persistence.setCode(qr);
	}

	public int getSnoozeNumber() {
		return persistence.getSnoozeLimit();
	}

	public void setSnoozeNumber(int snoozeNumber){
		this.persistence.setSnoozeLimit(snoozeNumber);
	}

	public long getSnoozeDuration() {
		return persistence.getSnoozeTime();
	}

	public void setSnoozeDuration(int snoozeDuration) {
		if (0 < snoozeDuration){
			this.persistence.setSnoozeTime(snoozeDuration);
		}
		else {
			throw new IllegalArgumentException("Snooze Duration must be greater than zero");
		}
	}

	public int getLedColor() {
		return style.getLedColor();
	}

	public void setLedColor(int ledColor) {
        style.setLedColor(ledColor);
	}

	public int getLedPattern() {
		return style.getLedPattern();
	}

	public void setLedPattern(int ledPattern) {
		style.setLedPattern(ledPattern);
	}

	public String getRingtone() {
		return style.getRingtone();
	}

	public void setRingtone(String ringtone) {
		style.setRingtone(ringtone);
	}

	public void setRadius(int radius){
		conditions.setRadius(radius);
	}

	public int getRadius(){
		return conditions.getRadius();
	}

	public void setVolume(int volume){
		this.persistence.setVolume(volume);
	}

	public int getVolume(){
		return persistence.getVolume();
	}

	public @Conditions.LocationPreference int getLocationType(){

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
        return conditions;
    }

	public Persistence getPersistence() {
		return persistence;
	}

	public void setPersistence(Persistence persistence) {
		this.persistence = persistence;
	}

	public Repeat getRepeat() {
		return repeat;
	}

	public void setRepeat(Repeat repeat) {
		this.repeat = repeat;
	}

	public Style getStyle(){
		return style;
	}

	public void setStyle(Style style){
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
		return persistence.hasFlag(Persistence.PersistenceFlags.REQUIRE_CODE);
	}

	public void setNeedQr(boolean needQr) {
		persistence.setFlag(Persistence.PersistenceFlags.REQUIRE_CODE, needQr);
	}

	public boolean getVolumeOverride(){
		return persistence.hasFlag(Persistence.PersistenceFlags.OVERRIDE_VOLUME);
	}

	public void setVolumeOverride(boolean volumeOverride){
		persistence.setFlag(Persistence.PersistenceFlags.OVERRIDE_VOLUME, volumeOverride);
	}

	public boolean getDisplayScreen(){
		return persistence.hasFlag(Persistence.PersistenceFlags.DISPLAY_SCREEN);
	}

	public void setDisplayScreen(boolean displayScreen){
		persistence.setFlag(Persistence.PersistenceFlags.DISPLAY_SCREEN, displayScreen);
	}

	public boolean getWakeUp(){
		return persistence.hasFlag(Persistence.PersistenceFlags.WAKE_UP);

	}

	public void setWakeUp(boolean wakeUp){
		persistence.setFlag(Persistence.PersistenceFlags.WAKE_UP, wakeUp);
	}

	public boolean getConfirmDismiss(){
		return persistence.hasFlag(Persistence.PersistenceFlags.CONFIRM_DISMISS);

	}

	public void setConfirmDismiss(boolean dismissDialog){
		persistence.setFlag(Persistence.PersistenceFlags.CONFIRM_DISMISS, dismissDialog);
	}

	public boolean isInsistent(){
		return persistence.hasFlag(Persistence.PersistenceFlags.KEEP_TRYING);
	}

	public void setInsistent(boolean insistent){
		persistence.setFlag(Persistence.PersistenceFlags.KEEP_TRYING, insistent);
	}

	/*************************** Style bitwise getters and setters ************************/

	public boolean getFadeVolume(){
		return style.hasFlag(Style.StyleFlags.BUILD_VOLUME);
	}

	public void setFadeVolume(boolean fade){
		style.setFlag(Style.StyleFlags.BUILD_VOLUME, fade);
	}

	public boolean getVibrate() {
		return style.hasFlag(Style.StyleFlags.VIBRATE);
	}

	public void setVibrate(boolean vibrate) {
		style.setFlag(Style.StyleFlags.VIBRATE, vibrate);
	}

	public boolean getLed() {
		return style.hasFlag(Style.StyleFlags.LED);
	}

	public void setLed(boolean led) {
		style.setFlag(Style.StyleFlags.LED, led);
	}
	
	public boolean getVibrateRepeat() {
		return style.hasFlag(Style.StyleFlags.REPEAT_VIBRATE);
	}

	public void setVibrateRepeat(boolean vibrateRepeat) {
		style.setFlag(Style.StyleFlags.REPEAT_VIBRATE, vibrateRepeat);
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
		conditions.toContentValues(values);
		persistence.toContentValues(values);
		repeat.toContentValues(values);
		style.toContentValues(values);
		return values;
	}

	/******************************** Parcel Methods ******************************/

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(name);
        out.writeSerializable(date);
        out.writeString(description);
		out.writeParcelable(conditions, 0);
		out.writeParcelable(persistence, 0);
		out.writeParcelable(repeat, 0);
		out.writeParcelable(style, 0);
    }

    public void readFromParcel(Parcel in){
        id = in.readInt();
        name = in.readString();
        date = (Calendar) in.readSerializable();
        description = in.readString();
		conditions = in.readParcelable(Conditions.class.getClassLoader());
		persistence = in.readParcelable(Persistence.class.getClassLoader());
		repeat = in.readParcelable(Repeat.class.getClassLoader());
		style = in.readParcelable(Style.class.getClassLoader());
    }

    @Override
    public int describeContents(){
        return 0;
    }

	/********************************* Repeat Methods ****************************************/

    //Returns true if thisDay is in bitwise set daysOfWeek
    public static boolean checkDayOfWeek(int daysOfWeek, int thisDay) {	//Call with checkDayOfWeek(reminder.getDaysOfWeek,thisDay)
        int mask = 0;
        switch (thisDay) {
            case 1: {
                mask = SUNDAY;
                break;
            }
            case 2: {
                mask = MONDAY;
                break;
            }
            case 3: {
                mask = TUESDAY;
                break;
            }
            case 4: {
                mask = WEDNESDAY;
                break;
            }
            case 5: {
                mask = THURSDAY;
                break;
            }
            case 6: {
                mask = FRIDAY;
                break;
            }
            case 7: {
                mask = SATURDAY;
                break;
            }
        }
        return((daysOfWeek & mask) == mask);
    }

    //Daily Repeat has no special use cases. adds RepeatType days to Date
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
        switch (reminder.getMonthType()) {
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
    //Set the next alarm to be RepeatType years after last alarm
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
	    editor.putString("location_type",Integer.toString(reminder.getLocationType()));
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
	    
        int daysOfWeek = reminder.getDaysOfWeek();
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
        editor.putString("snooze_duration", Long.toString(reminder.getSnoozeDuration()));
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
		@Repeat.RepeatType int repeatType = Integer.parseInt(sharedPreferences.
				getString("temp_repeat_type",Integer.toString(REPEAT_TYPE_DEFAULT)));
		setRepeat(new Repeat());
        setRepeatType(repeatType);
        switch (getRepeatType()) {
            case 1: {
                setRepeatLength(Integer.parseInt(sharedPreferences
                        .getString("temp_days", Integer.toString(REPEAT_PERIOD_DEFAULT))));
                break;
            }
            case 2: {
                setRepeatLength(Integer.parseInt(sharedPreferences
                        .getString("temp_weeks", Integer.toString(REPEAT_PERIOD_DEFAULT))));
                setDaysOfWeek(sharedPreferences);
                break;
            }
            case 3: {
                setRepeatLength(Integer.parseInt(sharedPreferences
                        .getString("temp_months", Integer.toString(REPEAT_PERIOD_DEFAULT))));
				@Repeat.DateType int dateType = Integer.parseInt(sharedPreferences.
						getString("temp_monthly_type", Integer.toString(REPEAT_DATE_TYPE_DEFAULT)));
                setMonthType(dateType);
                break;
            }
            case 4: {
                setRepeatLength(Integer.parseInt(sharedPreferences
                        .getString("temp_years", Integer.toString(REPEAT_PERIOD_DEFAULT))));
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
        @Conditions.LocationPreference int locationType =
				Integer.valueOf(sharedPreferences.getString("location_type", "-1"));
        conditions.setLocationPreference(locationType);

        LatLng location = new LatLng(sharedPreferences.getFloat("Latitude",LATITUDE_DEFAULT),
                sharedPreferences.getFloat("Longitude",LONGITUDE_DEFAULT));
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
		reminder.setRepeat(sharedPreferences);
        reminder.setDate(sharedPreferences, context);
        reminder.setLocation(sharedPreferences);
        reminder.setRadius(sharedPreferences.getInt("radius", RADIUS_DEFAULT));
        reminder.setVibrate(sharedPreferences.getBoolean("temp_vibrate", false));
        reminder.setRingtone(sharedPreferences.getString("temp_ringtone", RINGTONE_DEFAULT));
        reminder.setActive(reminder.getDate().after(Calendar.getInstance()));
        reminder.setQr(sharedPreferences.getString("temp_code", CODE_DEFAULT));
        reminder.setNeedQr(sharedPreferences.getBoolean("code_type", false));
        reminder.setVolumeOverride(sharedPreferences.getBoolean("out_loud", false));
        reminder.setDisplayScreen(sharedPreferences.getBoolean("display_screen", true));
        reminder.setWakeUp(sharedPreferences.getBoolean("wake_up", true));
        reminder.setSSID(sharedPreferences.getString("ssid", SSID_DEFAULT));
		Log.d(TAG, "preferenceToReminder: wifi checked " + sharedPreferences.getBoolean("wifi", false));
		if (sharedPreferences.getBoolean("wifi", false)) {
			reminder.getConditions().setWifiPreference(Conditions.WifiPreference.CONNECTED);
			Log.d(TAG, "preferenceToReminder: wifi is now" + reminder.getConditions().getWifiPreference());
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
        reminder.getConditions().setBtMacAddress(sharedPreferences.getString("bt_name", BT_MAC_ADDRESS_DEFAULT));
        reminder.setSnoozeDuration(Integer.parseInt(sharedPreferences
                .getString("snooze_duration", Long.toString(SNOOZE_TIME_DEFAULT))));
	    reminder.setSnoozeNumber(Integer.parseInt(sharedPreferences
                .getString("snooze_number", Integer.toString(SNOOZE_LIMIT_DEFAULT))));
	    reminder.setFadeVolume(sharedPreferences.getBoolean("fade", false));
	    reminder.setConfirmDismiss(sharedPreferences.getBoolean("dismiss_check", false));
	    reminder.setVibrateRepeat(sharedPreferences.getBoolean("vibrate_repeat", false));
	    reminder.setLed(sharedPreferences.getBoolean("led", false));
	    reminder.setVolume(sharedPreferences.getInt("volume", VOLUME_DEFAULT));
	    reminder.setInsistent(sharedPreferences.getBoolean("try_again", false));
        File file = new File(context.getExternalFilesDir(null), EditStyle.tempFile);
        File saveFile = new File(context.getFilesDir(), Integer.toString(reminder.getId()));
        if (saveFile.exists()){
            saveFile.delete();
        }
        if (file.exists()){
            Logger.d("Saveing image");
            file.renameTo(saveFile);
        }
        reminder.setTextColor(sharedPreferences.getInt("font_color",TEXT_COLOR_DEFAULT));
        reminder.setLedColor(sharedPreferences.getInt("led_color",LED_COLOR_DEFAULT));
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

    @Override
    public void addTo(Reminder reminder) {
        reminder.setId(reminder.getId());
        reminder.setActive(reminder.getActive());
        reminder.setName(reminder.getName());
        reminder.setDescription(reminder.getDescription());
        reminder.setDate(reminder.getDate());
    }
}
