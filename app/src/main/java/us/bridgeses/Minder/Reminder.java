package us.bridgeses.Minder;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
        active = ACTIVEDEFAULT;
        location = LOCATIONDEFAULT;
        name = NAMEDEFAULT;
        repeatType = REPEATTYPEDEFAULT;
        repeatLength = REPEATLENGTHDEFAULT;
        daysOfWeek = DAYSOFWEEKDEFAULT;
        //daysOfMonth = new HashSet();
        monthType = MONTHTYPEDEFAULT;
        onlyAtLocation = ONLYATLOCATIONDEFAULT;
        untilLocation = UNTILLOCATIONDEFAULT;
        untilLocation = UNTILLOCATIONDEFAULT;
        persistence = PERSISTENCEDEFAULT;
        date = Calendar.getInstance();
        description = DESCRIPTIONDEFAULT;
        qr = QRDEFAULT;
	    needQr = NEEDQRDEFAULT;
        snoozeDuration = SNOOZEDURATIONDEFAULT;
        vibrate = VIBRATEDEFAULT;
        ringtone = RINGTONEDEFAULT;
        ledPattern = LEDPATTERNDEFAULT;
        ledColor = LEDCOLORDEFAULT;
        led = LEDDEFAULT;
        id = IDDEFAULT;
        radius = RADIUSDEFAULT;
    }

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
    private int id;
    private Boolean active;
    private LatLng location;
    private String name;
    private int repeatType;
    private int repeatLength;
    private byte daysOfWeek;                    //Bitwise byte
    private byte monthType;
    private Boolean onlyAtLocation;
    private Boolean untilLocation;
    private byte persistence;                   //Bitwise byte
    private Calendar date;
    private String description;
    private String qr;
	private Boolean needQr;
    private int snoozeDuration;
    private Boolean vibrate;
    private Boolean led;
    private int ledColor;
    private int ledPattern;
    private String ringtone;
    private int radius;

    //Default constants
    public static final Boolean ACTIVEDEFAULT = true;
    public static final LatLng LOCATIONDEFAULT = new LatLng(0,0);
    public static final String NAMEDEFAULT = "";
    public static final int REPEATTYPEDEFAULT = 0;
    public static final int REPEATLENGTHDEFAULT = 1;
    public static final byte DAYSOFWEEKDEFAULT = 0;
    public static final byte MONTHTYPEDEFAULT = 0;
    public static final Boolean ONLYATLOCATIONDEFAULT = false;
    public static final Boolean UNTILLOCATIONDEFAULT = false;
    public static final int PERSISTENCEDEFAULT = 12;
    public static final String DESCRIPTIONDEFAULT = "";
    public static final int SNOOZEDURATIONDEFAULT = 300000;
    public static final String QRDEFAULT = "";
	public static final Boolean NEEDQRDEFAULT = false;
    public static final Boolean VIBRATEDEFAULT = false;
    public static final String RINGTONEDEFAULT = "";
    public static final Boolean LEDDEFAULT = false;
    public static final int LEDCOLORDEFAULT = -1;
    public static final int LEDPATTERNDEFAULT = -1;
    public static final int IDDEFAULT = -1;
    public static final int RADIUSDEFAULT = 500;

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

    public static final String PREFS_NAME = "ReminderPrefs";


    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public byte getMonthType() {
        return monthType;
    }

    public void setMonthType(byte monthType) {
        this.monthType = monthType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
        this.repeatType = repeatType;
    }

    public int getRepeatLength() {
        return repeatLength;
    }

    public void setRepeatLength(int repeatLength) {
        this.repeatLength = repeatLength;
    }

    public byte getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(byte daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public Boolean getOnlyAtLocation() {
        return onlyAtLocation;
    }

    public void setOnlyAtLocation(Boolean onlyAtLocation) {
        this.onlyAtLocation = onlyAtLocation;
    }

    public Boolean getUntilLocation() {
        return untilLocation;
    }

    public void setUntilLocation(Boolean untilLocation) {
        this.untilLocation = untilLocation;
    }

    public byte getPersistence() {
        return persistence;
    }

    public void setPersistence(byte persistence) {
        this.persistence = persistence;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
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

	public Boolean getNeedQr() {
        return (this.getPersistence() & REQUIRE_CODE) == REQUIRE_CODE;
    }

	public void setNeedQr(Boolean needQr) {
        byte mask = Reminder.REQUIRE_CODE;
        if (active && !this.getNeedQr()){
            this.setPersistence((byte)(this.getPersistence()+mask));
        }
        else {
            if (!active && this.getNeedQr()){
                this.setPersistence((byte)(this.getPersistence()-mask));
            }
        }
    }

    public int getSnoozeDuration() {
        return snoozeDuration;
    }

    public void setSnoozeDuration(int snoozeDuration) {
        this.snoozeDuration = snoozeDuration;
    }

    public Boolean getVibrate() {
        return vibrate;
    }

    public void setVibrate(Boolean vibrate) {
        this.vibrate = vibrate;
    }

    public Boolean getLed() {
        return led;
    }

    public void setLed(Boolean led) {
        this.led = led;
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
        this.ledPattern = ledPattern;
    }

    public String getRingtone() {
        return ringtone;
    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }

    public void setRadius(int radius){
        this.radius = radius;
    }

    public int getRadius(){
        return radius;
    }

	public Boolean getVolumeOverride(){
		return (this.getPersistence() & VOLUME_OVERRIDE) == VOLUME_OVERRIDE;
	}

	public void setVolumeOverride(Boolean active){
		byte mask = Reminder.VOLUME_OVERRIDE;
		if (active && !this.getVolumeOverride()){
			this.setPersistence((byte)(this.getPersistence()+mask));
		}
		else {
			if (!active && this.getVolumeOverride()){
				this.setPersistence((byte)(this.getPersistence()-mask));
			}
		}
	}

    public Boolean getDisplayScreen(){
        return (this.getPersistence() & DISPLAY_SCREEN) == DISPLAY_SCREEN;
    }

    public void setDisplayScreen(Boolean active){
        byte mask = Reminder.DISPLAY_SCREEN;
        if (active && !this.getDisplayScreen()){
            this.setPersistence((byte)(this.getPersistence()+mask));
        }
        else {
            if (!active && this.getDisplayScreen()){
                this.setPersistence((byte)(this.getPersistence()-mask));
            }
        }
    }

    public Boolean getWakeUp(){
        return (this.getPersistence() & WAKE_UP) == WAKE_UP;

    }

    public void setWakeUp(Boolean wakeUp){
        byte mask = Reminder.WAKE_UP;
        if (active && !this.getDisplayScreen()){
            this.setPersistence((byte)(this.getPersistence()+mask));
        }
        else {
            if (!active && this.getDisplayScreen()){
                this.setPersistence((byte)(this.getPersistence()-mask));
            }
        }
    }

    private static Reminder[] cursorToReminders(Cursor cursor){
        int numReminders = cursor.getCount();
        cursor.moveToFirst();
        Reminder[] reminders = new Reminder[numReminders];
        for (int i=0; i<numReminders; i++) {
            Reminder reminder = new Reminder();
            reminder.setId(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_ID)));
            reminder.setActive(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_ACTIVE)) == 1);
            int index = cursor.getColumnIndex(ReminderDBHelper.COLUMN_NAME);
            String name = cursor.getString(index);
            reminder.setName(name);
            reminder.setDescription(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_DESCRIPTION)));
            Calendar calendar = Calendar.getInstance();
            long time = cursor.getLong(cursor.getColumnIndex(ReminderDBHelper.COLUMN_DATE))*1000;
            calendar.setTimeInMillis(time);
            reminder.setDate(calendar);
            reminder.setDaysOfWeek((byte) cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_DAYSOFWEEK)));
            reminder.setMonthType((byte) cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_MONTHTYPE)));
            reminder.setLocation(new LatLng(cursor.getFloat(cursor.getColumnIndex(ReminderDBHelper.COLUMN_LATITUDE)),cursor.getFloat(cursor.getColumnIndex(ReminderDBHelper.COLUMN_LONGITUDE))));
            reminder.setRepeatLength(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_REPEATLENGTH)));
            reminder.setRepeatType(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_REPEATTYPE)));
            reminder.setVibrate(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_VIBRATE))==1);
            reminder.setRadius(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_RADIUS)));
	        reminder.setOnlyAtLocation(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_ONLYATLOCATION)) == 1);
	        reminder.setUntilLocation(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_UNTILLOCATION)) == 1);
	        reminder.setQr(cursor.getString(cursor.getColumnIndex(ReminderDBHelper.COLUMN_QR)));
	        reminder.setNeedQr(cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_NEEDQR)) == 1);
	        reminder.setPersistence((byte)cursor.getInt(cursor.getColumnIndex(ReminderDBHelper.COLUMN_PERSISTENCE)));
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
                ReminderDBHelper.COLUMN_DAYSOFMONTH,
                ReminderDBHelper.COLUMN_MONTHTYPE,
                ReminderDBHelper.COLUMN_REPEATTYPE,
                ReminderDBHelper.COLUMN_REPEATLENGTH,
                ReminderDBHelper.COLUMN_LATITUDE,
                ReminderDBHelper.COLUMN_LONGITUDE,
                ReminderDBHelper.COLUMN_VIBRATE,
                ReminderDBHelper.COLUMN_RINGTONE,
		        ReminderDBHelper.COLUMN_PERSISTENCE,
                ReminderDBHelper.COLUMN_RADIUS,
		        ReminderDBHelper.COLUMN_ONLYATLOCATION,
		        ReminderDBHelper.COLUMN_UNTILLOCATION,
		        ReminderDBHelper.COLUMN_QR,
		        ReminderDBHelper.COLUMN_NEEDQR
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
        //Gson gson = new Gson();
        //values.put(ReminderDBHelper.COLUMN_DAYSOFMONTH,gson.toJson(reminder.getDaysOfMonth()));
        values.put(ReminderDBHelper.COLUMN_MONTHTYPE,reminder.getMonthType());
        values.put(ReminderDBHelper.COLUMN_LATITUDE,reminder.getLocation().latitude);
        values.put(ReminderDBHelper.COLUMN_LONGITUDE,reminder.getLocation().longitude);
        values.put(ReminderDBHelper.COLUMN_REPEATTYPE,reminder.getRepeatType());
        values.put(ReminderDBHelper.COLUMN_REPEATLENGTH,reminder.getRepeatLength());
        values.put(ReminderDBHelper.COLUMN_VIBRATE,reminder.getVibrate());
        values.put(ReminderDBHelper.COLUMN_RINGTONE,reminder.getRingtone());
	    values.put(ReminderDBHelper.COLUMN_PERSISTENCE,reminder.getPersistence());
        values.put(ReminderDBHelper.COLUMN_RADIUS,reminder.getRadius());
	    values.put(ReminderDBHelper.COLUMN_QR,reminder.getQr());
	    if (reminder.getNeedQr()) {
		    values.put(ReminderDBHelper.COLUMN_NEEDQR,"1");
	    }
	    else {
		    values.put(ReminderDBHelper.COLUMN_QR,"0");
	    }
	    if (reminder.getOnlyAtLocation()) {
		    values.put(ReminderDBHelper.COLUMN_ONLYATLOCATION,"1");
	    }
	    else {
		    values.put(ReminderDBHelper.COLUMN_ONLYATLOCATION,"0");
	    }
	    if (reminder.getUntilLocation()){
		    values.put(ReminderDBHelper.COLUMN_UNTILLOCATION,"1");
	    }
	    else {
		    values.put(ReminderDBHelper.COLUMN_UNTILLOCATION,"0");
	    }
        if (reminder.getVolumeOverride()) {
            values.put(ReminderDBHelper.COLUMN_OUTLOUD,"1");
        }
        else {
            values.put(ReminderDBHelper.COLUMN_OUTLOUD, "0");
        }
        long newRowId;
        newRowId = database.replace(
                ReminderDBHelper.TABLE_NAME,
                null,
                values);
        return newRowId;
    }

    protected static Boolean deleteReminder(SQLiteDatabase database, int id){
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

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeByte((byte) (active ? 1 : 0));
        out.writeDouble(location.latitude);
        out.writeDouble(location.longitude);
        out.writeString(name);
        out.writeInt(repeatType);
        out.writeInt(repeatLength);
        out.writeByte(daysOfWeek);
        out.writeByte((byte) (onlyAtLocation ? 1 : 0));
        out.writeByte((byte) (untilLocation ? 1 : 0));
        out.writeByte(persistence);
        Log.d("Minder", String.valueOf(date.getTimeInMillis()));
        out.writeSerializable(date);
        out.writeString(description);
        out.writeString(qr);
        out.writeInt(snoozeDuration);
        out.writeByte((byte) (vibrate ? 1 : 0));
        out.writeByte((byte) (led ? 1 : 0));
        out.writeInt(ledColor);
        out.writeInt(ledPattern);
        out.writeString(ringtone);
        out.writeInt(radius);
	    out.writeString(qr);
	    out.writeByte((byte) (needQr ? 1 : 0));
    }


    public void readFromParcel(Parcel in){
        id = in.readInt();
        active = in.readByte() != 1;
        double lon = in.readDouble();
        double lat = in.readDouble();
        location = new LatLng(lon,lat);
        name = in.readString();
        repeatType = in.readInt();
        repeatLength = in.readInt();
        daysOfWeek = in.readByte();
        onlyAtLocation = in.readByte() != 0;
        untilLocation = in.readByte() != 0;
        persistence = in.readByte();
        date = (Calendar) in.readSerializable();
        description = in.readString();
        qr = in.readString();
        snoozeDuration = in.readInt();
        vibrate = in.readByte() != 0;
        led = in.readByte() != 0;
        ledColor = in.readInt();
        ledPattern = in.readInt();
        ringtone = in.readString();
        radius = in.readInt();
	    qr = in.readString();
	    needQr = in.readByte() != 0;
    }

    @Override
    public int describeContents(){

        return 0;
    }

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
                return;
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
}
