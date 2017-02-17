package us.bridgeses.Minder;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ACTIVE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DATE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ID;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_REPEATTYPE;

/**
 * This ListAdapter displays data from the Reminders in the cursor and has multiple button options
 * Created by Tony on 1/23/2015.
 */
public class ReminderListAdapter extends SimpleCursorAdapter implements View.OnClickListener{

    /**
     * An activity must implement these to get special button presses
     * SkipCklick is called when a button is pressed indicating the reminder has already been completed
     * IconClick is called when a button with the app's icon on it has been pressed
     */
	interface ListClicksListener {
		void SkipClick(int id);
		void IconClick(int id);
	}

	private Context mContext;
	private int layout;
	private final LayoutInflater inflater;
	private ListClicksListener callbacks;

    /**
     * Create new ReminderListAdapter
     * @param context The calling activity
     * @param layout The layout to use for each item
     * @param c The cursor with the data
     * @param from  The database column names
     * @param to The views those columns map to
     */
	public ReminderListAdapter(Context context,int layout, Cursor c,String[] from,int[] to) {
		super(context,layout,c,from,to, 0);
		this.layout=layout;
		this.mContext = context;
		this.inflater=LayoutInflater.from(context);
		try {
			callbacks = (ListClicksListener) context;
		}
		catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(context.toString()
					+ " must implement ListClicksListener");
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(layout, null);
	}

    /**
     * Called when something is clicked on screen. Determines what was clicked and takes appropriate
     * action
     * @param v the view that was clicked
     */
    @SuppressWarnings("unchecked")
	public void onClick(View v){
		HashMap<String, Integer> hashMap;
        hashMap = (HashMap<String,Integer>) v.getTag();
		int id = hashMap.get("id");
		int type = hashMap.get("type");
		Logger.d(Integer.toString(id));
		switch (type) {
			case 1: {
				callbacks.SkipClick(id);
				break;
			}
			case 2: {
				callbacks.IconClick(id);
				break;
			}
		}
	}

    /**
     * Returns a short string representation of a date that gets less precise the further away the
     * date is
     * @param calendar The date to be represented
     * @return Returns a short, user-friendly string representing a date or time
     */
	private String getNext(Calendar calendar){
		Calendar now = Calendar.getInstance();

		String next = "";
		SimpleDateFormat sdf;

		if (calendar.before(now)){
			next = "Passed";
		}
		else {
			if (calendar.get(Calendar.DAY_OF_YEAR) - 6 <= now.get(Calendar.DAY_OF_YEAR)) {
				if ((calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR))||
						(calendar.getTimeInMillis()) - Reminder.HOUR * 16 <= now.getTimeInMillis()){
					sdf = new SimpleDateFormat(mContext.getResources().getString(R.string.time_code));
				}
				else {
					sdf = new SimpleDateFormat("EEEE ha");
				}
			} else {
				if (calendar.get(Calendar.YEAR) != (now.get(Calendar.YEAR))) {
					sdf = new SimpleDateFormat("yyyy");
				} else {
					sdf = new SimpleDateFormat("MMM dd");
				}
			}
			next = sdf.format(calendar.getTime());
		}
		return next;
	}

	private String getRepeat(int repeatType){
		switch(repeatType){
			case 0: return "";
			case 1: return "Daily";
			case 2: return "Weekly";
			case 3: return "Monthly";
			case 4: return "Annually";
		}
		return "";
	}

    /**
     * Create the view and do any custom work that needs to be done on all items
     * @param view The view being bound
     * @param context The calling activity
     * @param cursor The cursor with all the data
     */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
		TextView nextText=(TextView)view.findViewById(R.id.list_next);
		TextView repeatText=(TextView)view.findViewById(R.id.list_repeat);
		ImageView repeatIcon=(ImageView)view.findViewById(R.id.list_repeat_icon);

		int idIndex=cursor.getColumnIndexOrThrow(COLUMN_ID);
		int dateIndex = cursor.getColumnIndexOrThrow(COLUMN_DATE);
		int repeatIndex = cursor.getColumnIndexOrThrow(COLUMN_REPEATTYPE);
		int activeIndex = cursor.getColumnIndexOrThrow(COLUMN_ACTIVE);

		Calendar calendar = Calendar.getInstance();
		long time = cursor.getLong(dateIndex)*1000;
		calendar.setTimeInMillis(time);

		nextText.setText(getNext(calendar));

		int repeatType = cursor.getInt(repeatIndex);

		if (repeatType != 0){
			repeatIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_action_av_repeat));
		}
		repeatText.setText(getRepeat(repeatType));

		Boolean active = cursor.getInt(activeIndex)==1;

		ImageView finish = (ImageView) view.findViewById(R.id.finished_button);
		HashMap<String,Integer> hashMap = new HashMap<>(2);
		hashMap.put("id", cursor.getInt(idIndex));
		hashMap.put("type", 1);
		finish.setTag(hashMap);
		finish.setOnClickListener(this);

		ImageView icon = (ImageView) view.findViewById(R.id.list_icon);
		HashMap<String,Integer> iconMap = new HashMap<>(2);
		iconMap.put("id", cursor.getInt(idIndex));
		iconMap.put("type",2);
		icon.setTag(iconMap);
		icon.setOnClickListener(this);

		if (!active) {
			view.setAlpha((float)0.4);
		}
		else{
			view.setAlpha((float)1.0);
		}
	}
}
