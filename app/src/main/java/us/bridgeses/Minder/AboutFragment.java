package us.bridgeses.Minder;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.orhanobut.logger.Logger;

/**
 * Created by Tony on 1/30/2015.
 */
public class AboutFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {


	public static AboutFragment ewInstance() {
		return new AboutFragment();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if (key.equals("support")){
			String url = getResources().getString(R.string.support_url);
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
		}
		if (key.equals("icon_attribution")){
			String url = getResources().getString(R.string.monster_shop);
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
		}
		if (key.equals("feedback")){
			Intent send = new Intent(Intent.ACTION_SENDTO);
			String uriText = getResources().getString(R.string.mailto)
					+ Uri.encode(getResources().getString(R.string.email))
					+ getResources().getString(R.string.subject_code)
					+ Uri.encode(getResources().getString(R.string.feedback_subject));
			Uri uri = Uri.parse(uriText);

			send.setData(uri);
			startActivity(send);
		}
		return false;
	}

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.about_preference);
		super.findPreference("website").setOnPreferenceClickListener(this);
		super.findPreference("support").setOnPreferenceClickListener(this);
		super.findPreference("feedback").setOnPreferenceClickListener(this);
		super.findPreference("icon_attribution").setOnPreferenceClickListener(this);
		try {
			PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
			String version = pInfo.versionName;
			super.findPreference("version").setSummary(version);
		}
		catch(PackageManager.NameNotFoundException e){
			Logger.e("Package name not found");
		}
	}
}
