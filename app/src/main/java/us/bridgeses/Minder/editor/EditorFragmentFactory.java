package us.bridgeses.Minder.editor;

import android.app.Fragment;

/**
 * A factory for creating fragments associated with editing reminders
 */
public class EditorFragmentFactory {

    /**
     * If type matches the name of a known fragment type, return that fragment type. Else return
     * a generic fragment
     * @param type A string representation of the type of fragment desired
     * @return An appropriate type of fragment
     */
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
        if (type.equals("repeat")){
            return RepeatFragment.newInstance();
        }
		return new Fragment();
	}
}
