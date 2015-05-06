package us.bridgeses.Minder;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Tony on 1/23/2015.
 */
public class ReminderListAdapter extends SimpleCursorAdapter implements View.OnClickListener, SkipDialogFragment.NoticeDialogListener{

	private Context mContext;
	private Context appContext;
	private int layout;
	private Cursor myCursor;
	private final LayoutInflater inflater;
	private final FragmentManager fragmentManager;

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {

	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog, int id) {
		Reminder reminder = Reminder.get(mContext,id);
		Reminder.nextRepeat(reminder).save(mContext);
	}

	public ReminderListAdapter(Context context,int layout, Cursor c,String[] from,int[] to,FragmentManager fragmentManager) {
		super(context,layout,c,from,to);
		this.layout=layout;
		this.mContext = context;
		this.inflater=LayoutInflater.from(context);
		this.myCursor=c;
		this.fragmentManager = fragmentManager;
	}

	@Override
	public View newView (Context context, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(layout, null);
	}



	public void onClick(View v){
		int id = (int)v.getTag();
		Logger.e(Integer.toString(id));
		SkipDialogFragment df = SkipDialogFragment.newInstance(id);
		df.show(fragmentManager,"SkipDialog");
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
			finish.setTag(cursor.getInt(idIndex));
			finish.setOnClickListener(this);

		if (!active) {
			view.setAlpha((float)0.4);
		}
	}


}
