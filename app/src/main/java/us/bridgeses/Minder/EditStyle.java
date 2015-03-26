package us.bridgeses.Minder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import us.bridgeses.Minder.R;

public class EditStyle extends Activity {

    Reminder reminder;
    Bundle saved;

    public void save(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancel(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("fade",saved.getBoolean("fade",Reminder.FADEDEFAULT));
        editor.apply();
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_frame);
        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            savedInstanceState.putBoolean("fade",
                    sharedPreferences.getBoolean("fade",Reminder.FADEDEFAULT));
        }
        saved = savedInstanceState;
        Intent intent = getIntent();
        Bundle incoming = intent.getExtras();
        if (incoming != null) {
            reminder = incoming.getParcelable("Reminder");
            if (reminder.getId()==-1){
                reminder = new Reminder();
            }
        }
        else
            reminder = new Reminder();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_style, menu);
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
}
