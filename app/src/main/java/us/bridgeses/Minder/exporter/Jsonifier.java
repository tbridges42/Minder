package us.bridgeses.Minder.exporter;

import us.bridgeses.Minder.model.Reminder;

/**
 * Created by tbrid on 1/9/2017.
 */

public interface Jsonifier {
    String toJson(Reminder reminder);

    Reminder fromJson(String json);
}
