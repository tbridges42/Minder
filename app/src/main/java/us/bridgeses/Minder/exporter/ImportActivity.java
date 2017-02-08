package us.bridgeses.Minder.exporter;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import us.bridgeses.Minder.DaoFactory;
import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.ReminderDAO;

/**
 * Created by tbrid on 2/7/2017.
 */

public class ImportActivity {

    private static final String TAG = "ImportActivity";

    private static final int CALLBACK_CONSTANT = 1;
    private Activity activity;

    public ImportActivity(Activity activity) {
        this.activity = activity;
    }

    public void importFrom(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            Importer importer = new Importer(reader, new Gsonifier(new Gson()));
            List<Reminder> reminders = importer.readAll();
            putReminders(reminders);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importBackup() {
        DialogProperties properties=new DialogProperties();
        properties.selection_mode= DialogConfigs.SINGLE_MODE;
        properties.selection_type=DialogConfigs.FILE_SELECT;
        properties.root=new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir=new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions=null;
        FilePickerDialog dialog = new FilePickerDialog(activity,properties);
        dialog.setTitle("Select a File");
        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                importFrom(new File(files[0]));
            }
        });
        dialog.show();
    }

    public void putReminders(List<Reminder> reminders) {
        DaoFactory daoFactory = DaoFactory.getInstance();
        ReminderDAO dao = daoFactory.getDao(activity);
        for (Reminder reminder : reminders) {
            dao.saveReminder(reminder);
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public boolean hasPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // TODO: Show explanation

        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    CALLBACK_CONSTANT);
        }
        return false;
    }
}
