package us.bridgeses.Minder.editor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.BaseAdapter;

import com.orhanobut.logger.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.util.Repeat;

/**
 * Created by Tony on 9/13/2014.
 */
public class RepeatFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener{

    PreferenceScreen repeatScreenPreference;
    SharedPreferences sharedPreferences;
    PreferenceCategory dailyRepeatMenu;
    PreferenceCategory weeklyRepeatMenu;
    PreferenceCategory monthlyRepeatMenu;
    PreferenceCategory yearlyRepeatMenu;
    ListPreference repeatList;
    EditTextPreference numDaysPreference;
    EditTextPreference numWeeksPreference;
    EditTextPreference numMonthsPreference;
    EditTextPreference numYearsPreference;
    ListPreference monthlyTypePreference;
    Repeat repeat;

    private void setWeeklyTitle() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String title;
        if (sharedPreferences.getString("temp_weeks", "0").equals("1"))
            title = "Every week";
        else
            title = "Every " + sharedPreferences.getString("temp_weeks", "0") + " weeks";
        byte daysOfWeek = setDaysOfWeek();
        if (daysOfWeek == 0){
            title = "Please select days for weekly repeat";
            numWeeksPreference.setTitle(title);
            return;
        }
        if (daysOfWeek == Reminder.ALL_WEEK) {
            title += " on each day";
            numWeeksPreference.setTitle(title);
            return;
        }
        if (daysOfWeek == Reminder.WEEKDAYS) {
            title += " on weekdays";
            numWeeksPreference.setTitle(title);
            return;
        }
        if (daysOfWeek == Reminder.WEEKENDS) {
            title += " on weekends";
            numWeeksPreference.setTitle(title);
            return;
        }
        title += " on";
        if (sharedPreferences.getBoolean("temp_sunday", false)) {
            title += " Su";
        }
        if (sharedPreferences.getBoolean("temp_monday", false)) {
            title += " Mo";
        }
        if (sharedPreferences.getBoolean("temp_tuesday", false)) {
            title += " Tu";
        }
        if (sharedPreferences.getBoolean("temp_wednesday", false)) {
            title += " We";
        }
        if (sharedPreferences.getBoolean("temp_thursday", false)) {
            title += " Th";
        }
        if (sharedPreferences.getBoolean("temp_friday", false)) {
            title += " Fr";
        }
        if (sharedPreferences.getBoolean("temp_saturday", false)) {
            title += " Sa";
        }
        numWeeksPreference.setTitle(title);
    }

    private void initSummaries(){
        repeatScreenPreference = (PreferenceScreen) super.findPreference("button_repeat_menu_key");
        dailyRepeatMenu = (PreferenceCategory) super.findPreference("daily_repeat_menu");
        yearlyRepeatMenu = (PreferenceCategory) super.findPreference("yearly_repeat_menu");
        weeklyRepeatMenu = (PreferenceCategory) super.findPreference("weekly_repeat_menu");
        monthlyRepeatMenu = (PreferenceCategory) super.findPreference("monthly_repeat_menu");
        repeatList = (ListPreference) super.findPreference("temp_repeat_type");
        numDaysPreference = (EditTextPreference) super.findPreference("temp_days");
        numWeeksPreference = (EditTextPreference) super.findPreference("temp_weeks");
        numMonthsPreference = (EditTextPreference) super.findPreference("temp_months");
        numYearsPreference = (EditTextPreference) super.findPreference("temp_years");
        monthlyTypePreference = (ListPreference) super.findPreference("temp_monthly_type");

        setRepeatSummary();
        repeatList.setSummary(repeatList.getEntry());
	    String repeatType = repeatList.getValue();
	    int numDays = Integer.parseInt(numDaysPreference.getText());
        if (repeatType.equals(repeat.DAILY)){
            if (numDays == 1)
                numDaysPreference.setTitle("Every day");
            else
                numDaysPreference.setTitle("Every " + numDays + " days");
        }
        if (repeatType.equals(repeat.WEEKLY)){
            setWeeklyTitle();
        }
        if (repeatType.equals(repeat.MONTHLY)){
            if (Integer.parseInt(numMonthsPreference.getText()) == 1)
                numMonthsPreference.setTitle("Every month");
            else
                numMonthsPreference.setTitle("Every " + Integer.parseInt(numMonthsPreference.getText()) + " months");
        }
        if (repeatType.equals(repeat.YEARLY)){
            if (Integer.parseInt(numYearsPreference.getText()) == 1)
                numYearsPreference.setTitle("Every year");
            else
                numYearsPreference.setTitle("Every " + Integer.parseInt(numYearsPreference.getText()) + " years");
        }
    }


    private void expandRepeatMenu() {
        String repeatType = sharedPreferences.getString("temp_repeat_type", "0");
        if (repeatType.equals(repeat.NONE)){
            repeatScreenPreference.removePreference(dailyRepeatMenu);
            repeatScreenPreference.removePreference(weeklyRepeatMenu);
            repeatScreenPreference.removePreference(monthlyRepeatMenu);
            repeatScreenPreference.removePreference(yearlyRepeatMenu);
        }
        if (repeatType.equals(repeat.DAILY)){
            repeatScreenPreference.addPreference(dailyRepeatMenu);
            repeatScreenPreference.removePreference(weeklyRepeatMenu);
            repeatScreenPreference.removePreference(monthlyRepeatMenu);
            repeatScreenPreference.removePreference(yearlyRepeatMenu);
            numDaysPreference = (EditTextPreference) super.findPreference("temp_days");
            if (numDaysPreference.getText().equals("0")){
                numDaysPreference.setText("1");
            }
            setPeriodTitle("temp_days", "day");
        }
        if (repeatType.equals(repeat.WEEKLY)){
            repeatScreenPreference.removePreference(dailyRepeatMenu);
            repeatScreenPreference.addPreference(weeklyRepeatMenu);
            repeatScreenPreference.removePreference(monthlyRepeatMenu);
            repeatScreenPreference.removePreference(yearlyRepeatMenu);
            if (setDaysOfWeek()==0) {
                Calendar date = Calendar.getInstance();
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa EEEE, MMMM d, yyyy");
                try {
                    String newDate = sharedPreferences.getString("temp_time", "") + " " + sharedPreferences.getString("temp_date", "");
                    date.setTime(timeFormat.parse(newDate));
                    int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    switch (dayOfWeek) {
                        case 1: {
                            editor.putBoolean("temp_sunday", true);
                            CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_sunday");
                            dayPreference.setChecked(true);
                            break;
                        }
                        case 2: {
                            editor.putBoolean("temp_monday", true);
                            CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_monday");
                            dayPreference.setChecked(true);
                            break;
                        }
                        case 3: {
                            editor.putBoolean("temp_tuesday", true);
                            CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_tuesday");
                            dayPreference.setChecked(true);
                            break;
                        }
                        case 4: {
                            editor.putBoolean("temp_wednesday", true);
                            CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_wednesday");
                            dayPreference.setChecked(true);
                            break;
                        }
                        case 5: {
                            editor.putBoolean("temp_thursday", true);
                            CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_thursday");
                            dayPreference.setChecked(true);
                            break;
                        }
                        case 6: {
                            editor.putBoolean("temp_friday", true);
                            CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_friday");
                            dayPreference.setChecked(true);
                            break;
                        }
                        case 7: {
                            editor.putBoolean("temp_saturday", true);
                            CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_saturday");
                            dayPreference.setChecked(true);
                            break;
                        }
                    }
                    editor.apply();
                } catch (ParseException e) {
                    Logger.e("Parse Error");
                }
            }
            setWeeklyTitle();
            setPeriodTitle("temp_weeks","week");
        }
        if (repeatType.equals(repeat.MONTHLY)){
            repeatScreenPreference.removePreference(dailyRepeatMenu);
            repeatScreenPreference.removePreference(weeklyRepeatMenu);
            repeatScreenPreference.addPreference(monthlyRepeatMenu);
            repeatScreenPreference.removePreference(yearlyRepeatMenu);
            numMonthsPreference = (EditTextPreference) super.findPreference("temp_months");
            setPeriodTitle("temp_months","month");
        }
        if (repeatType.equals(repeat.YEARLY)){
            repeatScreenPreference.removePreference(dailyRepeatMenu);
            repeatScreenPreference.removePreference(weeklyRepeatMenu);
            repeatScreenPreference.removePreference(monthlyRepeatMenu);
            repeatScreenPreference.addPreference(yearlyRepeatMenu);
            numYearsPreference = (EditTextPreference) super.findPreference("temp_years");
            setPeriodTitle("temp_years","year");
        }
    }

    private byte setDaysOfWeek() {
        byte daysOfWeek = 0;
        if (sharedPreferences.getBoolean("temp_sunday",false)) {
            daysOfWeek += Reminder.SUNDAY;
        }
        if (sharedPreferences.getBoolean("temp_monday",false)) {
            daysOfWeek += Reminder.MONDAY;
        }
        if (sharedPreferences.getBoolean("temp_tuesday",false)) {
            daysOfWeek += Reminder.TUESDAY;
        }
        if (sharedPreferences.getBoolean("temp_wednesday",false)) {
            daysOfWeek += Reminder.WEDNESDAY;
        }
        if (sharedPreferences.getBoolean("temp_thursday",false)) {
            daysOfWeek += Reminder.THURSDAY;
        }
        if (sharedPreferences.getBoolean("temp_friday",false)) {
            daysOfWeek += Reminder.FRIDAY;
        }
        if (sharedPreferences.getBoolean("temp_saturday",false)) {
            daysOfWeek += Reminder.SATURDAY;
        }
        return daysOfWeek;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.repeat_preference);

        repeat = new Repeat(getActivity());

        initSummaries();

        expandRepeatMenu();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public static RepeatFragment newInstance(){
        RepeatFragment fragment = new RepeatFragment();
        return fragment;
    }

    private Boolean setMonthlyTitle(int monthType) {
        Boolean set = false;
        Calendar date = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa EEEE, MMMM d, yyyy");
        try {
            String newDate = sharedPreferences.getString("temp_time", "") + " " + sharedPreferences.getString("temp_date", "");
            date.setTime(timeFormat.parse(newDate));
        }
        catch (ParseException e){
            Logger.e("Parse Error");
        }

        switch (monthType) {
            case 0: {
                int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
                monthlyTypePreference.setTitle("On the " + dayOfMonth + Reminder.appendInt(dayOfMonth) + " day of the month");
                set = true;
                break;
            }
            case 1: {
	            String nameOfDay = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
                int weekOfMonth = date.get(Calendar.WEEK_OF_MONTH);
                monthlyTypePreference.setTitle("On the " +  weekOfMonth + Reminder.appendInt(weekOfMonth)
		                + " " + nameOfDay);
                set = true;
                break;
            }
            case 2: {
                int day = date.get(Calendar.DAY_OF_MONTH);
                int daysInMonth = date.getActualMaximum(Calendar.DAY_OF_MONTH);
                monthlyTypePreference.setTitle(daysInMonth - day + " days from the end of the month");
                set = true;
                break;
            }
	        case 3: {
		        String nameOfDay = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
		        int weekOfMonth = date.get(Calendar.WEEK_OF_MONTH);
		        int weeksInMonth = date.getActualMaximum(Calendar.WEEK_OF_MONTH)+1;
		        int weeksFromEnd = weeksInMonth - weekOfMonth;
		        if (weeksFromEnd == 1){
			        monthlyTypePreference.setTitle("The last " + nameOfDay + " of the month");
			        set = true;
		        }
		        if (weeksFromEnd == 2){
			        monthlyTypePreference.setTitle("The next to last " + nameOfDay + " of the month");
			        set = true;
		        }
		        if (weeksFromEnd >= 3) {
			        monthlyTypePreference.setTitle(weeksFromEnd + " " + nameOfDay +
					        "s from the end of the month");
			        set = true;
		        }
		        if (!set){
			        Logger.e("Negative weeks in month");
		        }
		        break;
	        }
        }
        return set;
    }

    private Boolean setPeriodTitle(String key, String period) {
        EditTextPreference textPreference = (EditTextPreference) findPreference(key);
        if (textPreference == null){
            // No matching preference
            return false;
        }
        Boolean set = true;
        String value = sharedPreferences.getString(key,"0");
        if (key.equals("temp_days")){
            if (value.equals("1"))
                textPreference.setTitle("Every day");
            else
                textPreference.setTitle("Every " + value + " days");
        }
        else {
            if (key.equals("temp_weeks")) {
                setWeeklyTitle();
            } else {
                if (key.equals("temp_months")) {
	                if (value.equals("1"))
		                textPreference.setTitle("Every month");
	                else
		                textPreference.setTitle("Every " + value + " months");
                    setMonthlyTitle(monthlyTypePreference.findIndexOfValue(monthlyTypePreference.getValue()));
                } else {
                    if (key.equals("temp_years")) {
                        if (value.equals("1"))
                            textPreference.setTitle("Every year");
                        else
                            textPreference.setTitle("Every " + value + " years");
                    } else
                        set = false;
                }
            }
        }
        return set;
    }

    private void setRepeatSummary(){
	    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int repeatType = Integer.parseInt(sharedPreferences.getString("temp_repeat_type","0"));
        switch (repeatType) {
            case 0: {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("temp_days", "0");
                editor.apply();
                numDaysPreference.setText("0");
                repeatScreenPreference.setSummary("Do not repeat");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 1: {
                if (sharedPreferences.getString("temp_days","0").equals("0")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("temp_days", "1");
                    editor.apply();
                    numDaysPreference.setText("1");
                    setPeriodTitle("temp_days","day");
                }
                if (sharedPreferences.getString("temp_days","0").equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every day");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " + sharedPreferences.getString("temp_days", "0") + " days");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 2: {
                if (sharedPreferences.getString("temp_weeks","0").equals("0")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("temp_weeks", "1");
                    editor.apply();
                    numWeeksPreference.setText("1");
                    setPeriodTitle("temp_weeks","week");
                }
                if (sharedPreferences.getString("temp_weeks","0").equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every week");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " + sharedPreferences.getString("temp_weeks", "0") + " weeks");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 3: {
                if (sharedPreferences.getString("temp_months","0").equals("0")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("temp_months", "1");
                    editor.putString("temp_monthly_type","1");
                    editor.apply();
                    numMonthsPreference.setText("1");
                    setPeriodTitle("temp_months","month");
                    setMonthlyTitle(0);
                }
                if (sharedPreferences.getString("temp_months","0").equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every month");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " + sharedPreferences.getString("temp_months", "0") + " months");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 4: {
                if (sharedPreferences.getString("temp_years","0").equals("0")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("temp_years", "1");
                    editor.apply();
                    numYearsPreference.setText("1");
                    setPeriodTitle("temp_years","year");
                }
                if (sharedPreferences.getString("temp_years","0").equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every year");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " + sharedPreferences.getString("temp_years", "0") + " years");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preference, String key) {

        Preference mPreference = findPreference(key);
        if (mPreference instanceof  EditTextPreference){
            EditTextPreference textPreference = (EditTextPreference) mPreference;
            if (!setPeriodTitle(key,"key")){
                textPreference.setSummary(textPreference.getText());
            }
            else
            setRepeatSummary();
        }
        if (mPreference instanceof CheckBoxPreference) {
            setWeeklyTitle();
        }
        if (key.equals("temp_repeat_type")) {
            repeatList.setSummary(repeatList.getEntry());
            expandRepeatMenu();

        }
        if (key.equals("temp_weeks")) {
            setWeeklyTitle();
        }
        if ((key.equals("temp_monthly_type")) || (key.equals("temp_date"))) {
            setMonthlyTitle(monthlyTypePreference.findIndexOfValue(monthlyTypePreference.getValue()));
        }
        ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
    }
}
