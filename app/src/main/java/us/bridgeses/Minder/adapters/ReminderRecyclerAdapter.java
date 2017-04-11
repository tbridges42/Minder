package us.bridgeses.Minder.adapters;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.model.Reminder;

import static android.support.v7.util.SortedList.INVALID_POSITION;

/**
 * Created by tbrid on 2/15/2017.
 */

public class ReminderRecyclerAdapter
        extends RecyclerView.Adapter<ReminderRecyclerAdapter.ReminderHolder> {

    private static final String TAG = "ReminderRecyclerAdapter";

    public interface OnFinishClickedListener {
        void onFinishClicked(Reminder reminder);
    }

    public interface OnItemClickedListener {
        void onItemClicked(Reminder reminder);
    }

    private SortedList<Reminder> reminders;
    private final OnFinishClickedListener finishClickedListener;
    private final OnItemClickedListener itemClickedListener;

    public ReminderRecyclerAdapter(List<Reminder> reminderList,
                                   OnFinishClickedListener finishClickedListener,
                                   OnItemClickedListener itemClickedListener) {
        this.finishClickedListener = finishClickedListener;
        this.itemClickedListener = itemClickedListener;
        reminders = new SortedList<>(Reminder.class,
                new ReminderSorter(this));
        for (Reminder reminder: reminderList) {
            Log.d(TAG, "ReminderRecyclerAdapter: id " + reminder.getId());
            Log.d(TAG, "ReminderRecyclerAdapter: active " + reminder.getActive());
            reminders.add((Reminder)reminder.clone());
        }
        setHasStableIds(true);
    }

    public void addReminder(Reminder reminder) {
        int index = getPosition(reminder.getId());
        if (index == INVALID_POSITION) {
            reminders.add(reminder);
        }
        else {
            Log.d(TAG, "addReminder: old reminder id " + reminders.get(index).getId());
            Log.d(TAG, "addReminder: old reminder active " + reminders.get(index).getActive());
            Log.d(TAG, "addReminder: new reminder id " + reminder.getId());
            Log.d(TAG, "addReminder: new reminder active " + reminder.getActive());
            reminders.updateItemAt(index, reminder);
        }
    }

    public void removeReminder(Reminder reminder) {
        reminders.remove(reminder);
    }

    public Reminder getReminder(int index) {
        return reminders.get(index);
    }

    public int getPosition(long id) {
        for (int i = 0; i < reminders.size(); i++) {
            if (reminders.get(i).getId() == id) {
                return i;
            }
        }
        return INVALID_POSITION;
    }


    @Override
    public long getItemId(int position) {
        return reminders.get(position).getId();
    }

    @Override
    public ReminderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View reminderView = inflater.inflate(R.layout.item_reminder, parent, false);

        ReminderHolder reminderHolder = new ReminderHolder(reminderView);
        return reminderHolder;
    }

    @Override
    public void onBindViewHolder(ReminderHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.name.setText(reminder.getName());
        holder.description.setText(reminder.getDescription());
        holder.id = reminder.getId();
        if (reminder.getRepeatType() == 0) {
            holder.repeatIcon.setVisibility(View.INVISIBLE);
        }
        else {
            holder.repeatIcon.setVisibility(View.VISIBLE);
        }
        holder.nextDate.setText(getNext(holder.nextDate.getContext(),
                reminder.getDate()));
        holder.repeatDescription.setText(getRepeat(reminder.getRepeatType()));
        if (reminder.getActive()) {
            holder.itemView.setAlpha(1.0f);
        }
        else {
            holder.itemView.setAlpha(0.4f);
        }
    }

    @Override
    public void onBindViewHolder(ReminderHolder holder, int position, List<Object> payload) {
        super.onBindViewHolder(holder, position, payload);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    /**
     * Returns a short string representation of a date that gets less precise the further away the
     * date is
     * @param calendar The date to be represented
     * @return Returns a short, user-friendly string representing a date or time
     */
    private String getNext(Context context, Calendar calendar){
        Calendar now = Calendar.getInstance();
        // TODO: Clean this up and make localizable
        String next = "";
        SimpleDateFormat sdf;

        if (calendar.before(now)){
            next = "Passed";
        }
        else {
            if (calendar.get(Calendar.DAY_OF_YEAR) - 6 <= now.get(Calendar.DAY_OF_YEAR)) {
                if ((calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR))||
                        (calendar.getTimeInMillis()) - Reminder.HOUR * 16 <= now.getTimeInMillis()){
                    sdf = new SimpleDateFormat(context.getResources().getString(R.string.time_code));
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
        // TODO: 2/15/2017 Make localizable

        switch(repeatType){
            case 0: return "";
            case 1: return "Daily";
            case 2: return "Weekly";
            case 3: return "Monthly";
            case 4: return "Annually";
        }
        return "";
    }

    class ReminderHolder extends RecyclerView.ViewHolder{

        ImageView finishedButton;
        ImageView listIcon;
        TextView name;
        TextView nextDate;
        TextView description;
        ImageView repeatIcon;
        TextView repeatDescription;
        long id;

        ReminderHolder(View itemView) {
            super(itemView);
            finishedButton = (ImageView) itemView.findViewById(R.id.finished_button);
            listIcon = (ImageView) itemView.findViewById(R.id.list_icon);
            name = (TextView) itemView.findViewById(R.id.list_name);
            nextDate = (TextView) itemView.findViewById(R.id.list_next);
            description = (TextView) itemView.findViewById(R.id.list_description);
            repeatIcon = (ImageView) itemView.findViewById(R.id.list_repeat_icon);
            repeatDescription = (TextView) itemView.findViewById(R.id.list_repeat);

            finishedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (finishClickedListener != null) {
                        finishClickedListener.onFinishClicked(reminders.get(getAdapterPosition()));
                    }
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickedListener != null) {
                        itemClickedListener.onItemClicked(reminders.get(getAdapterPosition()));
                    }
                }
            });
        }
    }
}
