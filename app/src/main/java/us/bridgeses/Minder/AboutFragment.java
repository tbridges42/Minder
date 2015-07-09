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
 * This class is a fragment displaying information about the app itself, its author,
 * and significant contributors.
 * <p>It extends {@link PreferenceFragment} to utilize its default formatting</p>
 */
public class AboutFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, IFragment {

	/**
	 * Static factory method to encapsulate the production of AboutFragment
	 * @return the new AboutFragment
	 */
	public static AboutFragment newInstance() {
		return new AboutFragment();
	}

	/**
	 * Determine what preference was clicked and take appropriate action.
	 * <p>Called by Android system when a preference is clicked</p>
	 * <ul>
	 *     <li>website - send to <a href="http://apps.bridgeses.us/minder">app site</a></li>
	 *     <li>support - send to <a href="http://support.bridgeses.us">support site</a></li>
	 *     <li>icon_attribution - send to Travelling Monster Shop Etsy page</li>
	 *     <li>feedback - open default mail app and compose email to tbridges42@gmail.com</li>
	 * </ul>
	 * @param preference the preference that the user clicked
	 * @return whether or not the click was handled here
	 */
	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		switch (key){
			case "website":{
				String url = getResources().getString(R.string.site_url);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				return true;
			}
			case "support":{
				String url = getResources().getString(R.string.support_url);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				return true;
			}
			case "icon_attribution": {
				String url = getResources().getString(R.string.monster_shop);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
				return true;
			}
			case "feedback": {
				Intent send = new Intent(Intent.ACTION_SENDTO);
				String uriText = getResources().getString(R.string.mailto)
						+ Uri.encode(getResources().getString(R.string.email))
						+ getResources().getString(R.string.subject_code)
						+ Uri.encode(getResources().getString(R.string.feedback_subject));
				Uri uri = Uri.parse(uriText);

				send.setData(uri);
				startActivity(send);
				return true;
			}
			default:
				return false;
		}
	}

	/**
	 * Set up fragment display
	 * <p>Attach to preference xml</p>
	 * <p>Set this class as onPreferenceClickListener for all clickable preferences</p>
	 * <p>Get and display version number</p>
	 * @param savedInstanceState is passed to super, not otherwise used
	 */
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.about_preference);
		super.findPreference("website").setOnPreferenceClickListener(this);
		super.findPreference("support").setOnPreferenceClickListener(this);
		super.findPreference("feedback").setOnPreferenceClickListener(this);
		super.findPreference("icon_attribution").setOnPreferenceClickListener(this);
		try {
			PackageInfo pInfo = getActivity().getPackageManager()
					.getPackageInfo(getActivity().getPackageName(), 0);
			String version = pInfo.versionName;
			super.findPreference("version").setSummary(version);
		}
		catch(PackageManager.NameNotFoundException e){
			Logger.e("Package name not found");
		}
	}
}
