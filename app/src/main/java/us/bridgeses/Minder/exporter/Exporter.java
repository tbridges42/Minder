package us.bridgeses.Minder.exporter;

import com.orhanobut.logger.Logger;

import java.io.PrintWriter;
import java.util.List;

import us.bridgeses.Minder.Reminder;

/**
 * Created by tbrid on 1/9/2017.
 */

public class Exporter {

    private PrintWriter writer;
    private Jsonifier jsonifier;

    // TODO: 2/17/2017 Embed database version in export file
    // TODO: 2/17/2017 Exported information could include locations and private data: add encryption
    // option. Use AES-128 or better.

    public Exporter(PrintWriter writer, Jsonifier jsonifier) {
        this.writer = writer;
        this.jsonifier = jsonifier;
    }

    public void write(Reminder reminder) {
        Logger.d(jsonifier.toJson(reminder));
        writer.println(jsonifier.toJson(reminder));
    }

    public void writeAll(List<Reminder> reminders) {
        Logger.d("Reminders: " + reminders.size());
        for (Reminder reminder : reminders) {
            write(reminder);
        }
    }
}
