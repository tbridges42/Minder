package us.bridgeses.Minder.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * This is a generic confirmation dialog
 */
public class ConfirmDialogFragment extends DialogFragment {

    /**
     * An interface for receiving callbacks when a button is pressed.
     * The methods pass the dialog in the event that the host needs to query it
     */
    public interface NoticeDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    NoticeDialogListener mListener;
    String title;
    String message;
    String positive;
    String negative;

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

    /**
     * Creates a confirmation dialog with the text set in the parameters
     * @param title will be the dialog title
     * @param message will be the dialog message
     * @param positive will appear on the positive button
     * @param negative will appear on the negative button
     * @return the dialog instance
     */
    public static ConfirmDialogFragment newInstance(String title, String message, String positive, String negative) {
        ConfirmDialogFragment dialog = new ConfirmDialogFragment();

        Bundle args = new Bundle();
        args.putString("title",title);
        args.putString("message",message);
        args.putString("positive",positive);
        args.putString("negative", negative);
        dialog.setArguments(args);

        return dialog;
    }

    private void parseArgs(Bundle args){
        title = args.getString("title");
        message = args.getString("message");
        positive = args.getString("positive");
        negative = args.getString("negative");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        parseArgs(getArguments());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(title);
	    builder.setMessage(message);
        builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                mListener.onDialogPositiveClick(ConfirmDialogFragment.this);
            }
        });
        builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Send the negative button event back to the host activity
                mListener.onDialogNegativeClick(ConfirmDialogFragment.this);
            }
        });
        return builder.create();
    }
}