package us.bridgeses.Minder.editor;

import android.app.Fragment;

import us.bridgeses.Minder.ConditionsFragment;
import us.bridgeses.Minder.PersistenceFragment;
import us.bridgeses.Minder.StyleFragment;

/**
 * Created by Tony on 4/18/2015.
 */
public class EditorFragmentFactory {

	public static Fragment newInstance(String type){
		if (type.equals("persistence")){
			return PersistenceFragment.newInstance();
		}
		if (type.equals("style")){
			return StyleFragment.newInstance();
		}
		if (type.equals("conditions")){
			return ConditionsFragment.newInstance();
		}
		return new Fragment();
	}
}
