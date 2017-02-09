package us.bridgeses.Minder.exporter;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import us.bridgeses.Minder.DaoFactory;
import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.ReminderDAO;

/**
 * Created by tbrid on 1/9/2017.
 */

public class ExportActivity{

    private static final int CALLBACK_CONSTANT = 0;
    private Activity activity;

    public ExportActivity(Activity activity) {
        this.activity = activity;
    }

    public List<Reminder> getReminders() {
        DaoFactory daoFactory = DaoFactory.getInstance();
        ReminderDAO dao = daoFactory.getDao(activity);
        return Arrays.asList(dao.getReminders());
    }

    public void exportTo(File file) throws IOException {
        Logger.d(file.getAbsolutePath());
        if (!file.getParentFile().mkdirs()) {
            Logger.d("Failed to make directories");
            // TODO: Make proper error reporting
            Toast.makeText(activity, "Failed to create directory structure", Toast.LENGTH_LONG).show();
            return;
        }
        if (!file.createNewFile()) {
            Logger.d("Failed to create file");
            Toast.makeText(activity, "Failed to create file", Toast.LENGTH_LONG).show();
            return;
        }
        PrintWriter writer = new PrintWriter(file);
        Exporter exporter = new Exporter(writer, new Gsonifier(new Gson()));
        exporter.writeAll(getReminders());
        writer.close();
        Toast.makeText(activity, "Created backup at " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
    }

    public void export() {
        if (hasPermission() && isExternalStorageWritable()) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
            try {
                String directory;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    directory = Environment.DIRECTORY_DOCUMENTS;
                }
                else {
                    directory = Environment.DIRECTORY_DOWNLOADS;
                }
                File path = Environment.
                        getExternalStoragePublicDirectory(directory);
                exportTo(new File(path,
                        "Minder-" + format.format(Calendar.getInstance().getTime()) + ".bak"));
            }
            catch (IOException e) {
                Logger.e("File not found");
                e.printStackTrace();
            }
        }
        else {
            Logger.e("No write permissions");
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public boolean hasPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // TODO: Show explanation

        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    CALLBACK_CONSTANT);
        }
        return false;
    }
}
