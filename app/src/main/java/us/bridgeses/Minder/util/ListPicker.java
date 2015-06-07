package us.bridgeses.Minder.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Tony on 6/3/2015.
 */
public class ListPicker extends DialogFragment{

    public interface ListDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, int id);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    AlertDialog.Builder dialogBuilder;
    ListDialogListener mListener;
    Context context;
    String[] items;

    public static ListPicker newInstance(String[] items){
        ListPicker listPicker = new ListPicker();

        Bundle args = new Bundle();
        args.putStringArray("items", items);
        listPicker.setArguments(args);

        return listPicker;
    }

    /**
     * Given a list of scan results, display a picker dialog for the user to choose from
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle("Select WiFi Network");

        String[] items = getArguments().getStringArray("items");
        dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host fragment
                ListDialogListener activity = (ListDialogListener) getActivity();
                activity.onDialogPositiveClick(ListPicker.this, id);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Send the negative button event back to the host fragment
                mListener.onDialogNegativeClick(ListPicker.this);
            }
        });
        return dialogBuilder.create();
    }

}
