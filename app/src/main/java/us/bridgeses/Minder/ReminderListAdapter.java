package us.bridgeses.Minder;

import android.app.DialogFragment;
import android.app.FragmentManager;
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
import java.util.InputMismatchException;

/**
 * Created by Tony on 1/23/2015.
 */
public class ReminderListAdapter extends SimpleCursorAdapter implements View.OnClickListener{

	interface ListClicksListener {
		void SkipClick(int id);
		void IconClick(int id);
	}

	private Context mContext;
	private int layout;
	private final LayoutInflater inflater;
	private ListClicksListener callbacks;

	public ReminderListAdapter(Context context,int layout, Cursor c,String[] from,int[] to,FragmentManager fragmentManager) {
		super(context,layout,c,from,to);
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
	public View newView (Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(layout, null);
	}



	public void onClick(View v){
		HashMap<String, Integer> hashMap;
		try {
			hashMap = (HashMap<String,Integer>) v.getTag();
		}
		catch (Exception e){
			throw e;
		}
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

	private String getNext(Calendar calendar){
		Calendar now = Calendar.getInstance();

		String next = "";
		SimpleDateFormat sdf;

		if (calendar.before(now)){
			next = "Passed";
		}
		else {
			if (calendar.get(Calendar.DAY_OF_YEAR) - 6 <= now.get(Calendar.DAY_OF_YEAR)) {
				if (calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
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

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		super.bindView(view, context, cursor);
		TextView title=(TextView)view.findViewById(R.id.list_name);
		TextView description=(TextView)view.findViewById(R.id.list_description);
		TextView nextText=(TextView)view.findViewById(R.id.list_next);
		TextView repeatText=(TextView)view.findViewById(R.id.list_repeat);
		ImageView repeatIcon=(ImageView)view.findViewById(R.id.list_repeat_icon);

		int titleIndex=cursor.getColumnIndexOrThrow(ReminderDBHelper.COLUMN_NAME);
		int descriptionIndex=cursor.getColumnIndexOrThrow(ReminderDBHelper.COLUMN_DESCRIPTION);
		int idIndex=cursor.getColumnIndexOrThrow(ReminderDBHelper.COLUMN_ID);
		int dateIndex = cursor.getColumnIndexOrThrow(ReminderDBHelper.COLUMN_DATE);
		int repeatIndex = cursor.getColumnIndexOrThrow(ReminderDBHelper.COLUMN_REPEATTYPE);
		int activeIndex = cursor.getColumnIndexOrThrow(ReminderDBHelper.COLUMN_ACTIVE);

		title.setText(cursor.getString(titleIndex));
		description.setText(cursor.getString(descriptionIndex));

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
