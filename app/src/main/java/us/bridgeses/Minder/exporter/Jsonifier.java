package us.bridgeses.Minder.exporter;

import org.json.JSONObject;

import us.bridgeses.Minder.Reminder;

/**
 * Created by tbrid on 1/9/2017.
 */

public interface Jsonifier {
    String toJson(Reminder reminder);

    Reminder fromJson(String json);
}
