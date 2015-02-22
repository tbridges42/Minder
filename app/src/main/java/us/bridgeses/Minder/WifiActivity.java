package us.bridgeses.Minder;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import us.bridgeses.Minder.R;

public class WifiActivity extends Activity implements WiFiFragment.OnFragmentInteractionListener {

    SharedPreferences sharedPreferences;
    Bundle saved;

    public void save(View view){
        finish();
    }

    public void cancel(View view){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ssid",saved.getString("ssid",""));
        editor.apply();
        finish();
    }

    public void onFragmentInteraction(String ssid){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ssid",ssid);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_frame);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.reminder_frame, new WiFiFragment())
                    .commit();
            savedInstanceState = new Bundle();
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            savedInstanceState.putString("ssid",sharedPreferences.getString("ssid",""));
        }
        saved = savedInstanceState;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_wifi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.edit_frame, container, false);
            return rootView;
        }
    }
}
