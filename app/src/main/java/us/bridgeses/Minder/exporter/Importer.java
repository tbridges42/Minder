package us.bridgeses.Minder.exporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import us.bridgeses.Minder.model.Reminder;

/**
 * Created by tbrid on 1/9/2017.
 */

public class Importer {

    // TODO: 2/17/2017 Get database version from import and handle possible data conversions

    private BufferedReader reader;
    private Jsonifier jsonifier;

    public Importer(BufferedReader reader, Jsonifier jsonifier) {
        this.reader = reader;
        this.jsonifier = jsonifier;
    }

    public Reminder read() throws IOException {
        String line = reader.readLine();
        if (line != null) {
            return jsonifier.fromJson(line);
        }
        return null;
    }

    public List<Reminder> readAll() throws IOException {
        ArrayList<Reminder> reminders = new ArrayList<>();
        Reminder reminder = read();
        while (reminder != null) {
            reminders.add(reminder);
            reminder = read();
        }
        return reminders;
    }
}
