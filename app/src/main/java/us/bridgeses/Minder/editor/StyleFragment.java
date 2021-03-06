package us.bridgeses.Minder.editor;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.view.Display;
import android.widget.BaseAdapter;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.util.ImageHelper;

public class StyleFragment extends PreferenceFragment implements 
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    PreferenceScreen imagePreference;
    SharedPreferences sharedPreferences;
    Point size = new Point();

    public static PreferenceFragment newInstance(){
        return new StyleFragment();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals("image")){
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            display.getSize(size);
            intent.putExtra("outputX", size.x);
            intent.putExtra("outputY", size.y);
            intent.putExtra("scale",true);
            intent.putExtra("aspectX", size.x);
            intent.putExtra("aspectY",size.y);
            intent.putExtra("return-data", false);
            File file = new File(getActivity().getExternalFilesDir(null), EditStyle.tempFile);
            if (file.exists()){
                file.delete();
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(intent, 1);
        }
        if (key.equals("clear_image")){
            File file = new File(getActivity().getFilesDir(), EditStyle.tempFile);
            if (file.exists()){
                file.delete();
            }
            imagePreference.setIcon(null);
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Logger.d("onActivityResult");
        if (resultCode == Activity.RESULT_CANCELED){
            return;
        }
        if (requestCode == 1){
            Logger.d("requestCode 1");
            //TODO: Null checking
            File file = new File(getActivity().getExternalFilesDir(null), EditStyle.tempFile);
            Bitmap bitmap = BitmapFactory.decodeFile((file.getAbsolutePath()));
            Logger.d("x: " + size.x + " y: " + size.y);
            bitmap = ImageHelper.scaleBitmap(bitmap, size.x, size.y);
            try{
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();
                Logger.d("Image size: " + Double.toString(file.length()/1000000d) + "MB");
            }
            catch (IOException e){
                Logger.d("File does not exist");
            }

            imagePreference.setIcon(new BitmapDrawable(getResources(), bitmap));

            Logger.d("Bitmap received");
        }
    }

	public void onSharedPreferenceChanged(SharedPreferences preference, String key){
		if (key.equals("temp_vibrate")){
			CheckBoxPreference mPreference = (CheckBoxPreference) findPreference(key);
			super.findPreference("vibrate_repeat").setEnabled(mPreference.isChecked());
		}
        if (key.equals("image")){
            //imagePreference.setImage(sharedPreferences.getString("image", null));
        }
		((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
	}

    private void initSummaries(){
	    CheckBoxPreference vibrateRepeat = (CheckBoxPreference) super.findPreference("vibrate_repeat");
	    CheckBoxPreference led = (CheckBoxPreference) super.findPreference("led");
	    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        imagePreference = (PreferenceScreen) super.findPreference("image");
        imagePreference.setOnPreferenceClickListener(this);
        File file = new File(getActivity().getExternalFilesDir(null),EditStyle.tempFile);
        if (file.exists()){
            Logger.d("Initializing image");
            imagePreference.setIcon(new BitmapDrawable(getResources(), BitmapFactory.decodeFile((file.getAbsolutePath()))));
        }
        findPreference("clear_image").setOnPreferenceClickListener(this);
	    vibrateRepeat.setChecked(sharedPreferences.getBoolean("vibrate_repeat",false));
	    vibrateRepeat.setEnabled(sharedPreferences.getBoolean("temp_vibrate",false));
	    led.setChecked(sharedPreferences.getBoolean("led", false));
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Logger.d("creating stylefragment");
        addPreferencesFromResource(R.xml.style_preference);
        initSummaries();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
