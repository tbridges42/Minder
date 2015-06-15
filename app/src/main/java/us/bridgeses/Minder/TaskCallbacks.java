package us.bridgeses.Minder;

/**
 * An interface for using AsyncTasks
 * Created by Tony on 6/12/2015.
 */
public interface TaskCallbacks {
    void onPreExecute();
    void onProgressUpdate(int percent);
    void onCancelled();
    void onPostExecute();
}
