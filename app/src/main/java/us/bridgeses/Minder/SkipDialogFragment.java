package us.bridgeses.Minder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.orhanobut.logger.Logger;

/**
 * Created by Tony on 9/13/2014.
 */
public class SkipDialogFragment extends DialogFragment {


    /* The activity that creates an instance of this dialog fragment must
* implement this interface in order to receive event callbacks.
* Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, int id);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
	int id = -1;

	static SkipDialogFragment newInstance(int id) {
		SkipDialogFragment f = new SkipDialogFragment();

		// Supply num input as an argument.
		Bundle args = new Bundle();
		args.putInt("Id", id);
		f.setArguments(args);

		return f;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }

    }

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

	}

	public int getID(){
		return id;
	}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
	    id = getArguments().getInt("Id");
	    Logger.d("Sending " + Integer.toString(id));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle("Skip Reminder");
	    builder.setMessage("Skip the next instance of this Reminder")
                .setPositiveButton("Skip", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
	                    NoticeDialogListener activity = (NoticeDialogListener) getActivity();
                        activity.onDialogPositiveClick(SkipDialogFragment.this,getID());
                    }
                })
                .setNegativeButton(getResources().getString(R.string.edit_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(SkipDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}