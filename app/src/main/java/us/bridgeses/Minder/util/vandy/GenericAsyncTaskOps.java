package us.bridgeses.Minder.util.vandy;

/**
 * This helper class was created by the Vanderbilt University and made available
 * in the course Programming Cloud Services for Android Handheld Systems at Coursera
 */

/**
 * The base interface that an operations ("Ops") class must implement
 * so that it can be notified automatically by the GenericAsyncTask
 * framework during the AsyncTask processing.
 */
public interface GenericAsyncTaskOps<Params, Progress, Result> {
    /**
     * Process the @a param in a background thread.
     */
    Result doInBackground(Params param);

    /**
     * Process the @a result in the UI Thread.
     */
    void onPostExecute(Result result,
                       Params param);
}
