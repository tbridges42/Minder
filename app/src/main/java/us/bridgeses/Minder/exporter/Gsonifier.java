package us.bridgeses.Minder.exporter;

import com.google.gson.Gson;

import us.bridgeses.Minder.model.Reminder;

/**
 * Created by tbrid on 1/9/2017.
 */

public class Gsonifier implements Jsonifier{

    private Gson gson;

    public Gsonifier(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String toJson(Reminder reminder) {
        return gson.toJson(reminder);
    }

    @Override
    public Reminder fromJson(String json) {
        return gson.fromJson(json, Reminder.class);
    }
}
