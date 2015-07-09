package us.bridgeses.Minder;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.orhanobut.logger.Logger;
import com.robotium.solo.Solo;

import us.bridgeses.Minder.editor.EditReminder;

public class EditActivityTest extends
        ActivityInstrumentationTestCase2<MainListActivity>{

    private Solo solo;

    public EditActivityTest() {
        super(MainListActivity.class);
    }

    @Override
    protected void setUp() throws Exception{
        solo = new Solo(getInstrumentation(),getActivity());
        solo.clickOnActionBarItem(R.id.action_new);
        solo.getCurrentActivity();
    }

    /*public void testConditionsEditFragment() throws Exception {
        solo.clickOnText("Conditions");
        assertTrue(solo.searchText("Location Restriction"));
        assertTrue(solo.searchText("Set Location"));
        assertTrue(solo.searchText("WiFi Restriction"));
        assertTrue(solo.searchText("Select WiFi"));
        assertTrue(solo.searchText("Bluetooth Restriction"));
        assertTrue(solo.searchText("Select Bluetooth"));
        solo.clickOnButton("Cancel");
    }*/

    public void testStyleEditFragment() throws Exception {
        solo.clickOnText("Style");
        assertTrue(solo.searchText("Vibrate"));
        assertTrue(solo.searchText("Vibrate Repeat"));
        assertTrue(solo.searchText("Vibrate Pattern"));
        assertTrue(solo.searchText("Flash LED"));
        assertTrue(solo.searchText("LED Color"));
        assertTrue(solo.searchText("LED Pattern"));
        assertTrue(solo.searchText("Background Image"));
        assertTrue(solo.searchText("Clear Image"));
        assertTrue(solo.searchText("Font Color"));
        solo.clickOnButton("Cancel");
    }

    public void testPersistenceEditFragment() throws Exception {
        /*solo.clickOnText("Persistence");
        assertTrue(solo.searchText("Require Code"));
        assertTrue(solo.searchText("Set Code"));
        assertTrue(solo.searchText("Override Volume Settings"));
        assertTrue(solo.searchText("Volume Level"));
        assertTrue(solo.searchText("Display Reminder Screen"));
        assertTrue(solo.searchText("Wake Up For Reminder"));
        assertTrue(solo.searchText("Dismiss Dialog"));
        assertTrue(solo.searchText("Keep Trying"));
        assertTrue(solo.searchText("Snooze Limit"));
        assertTrue(solo.searchText("Snooze Time"));
        solo.clickOnButton("Cancel");*/
    }

    public void testMainEditActivity() throws Exception {
        /*Logger.d("Started test");
        assertTrue(solo.searchText("Name"));
        Logger.d("Found Name");
        assertTrue(solo.searchText("Description"));
        Logger.d("Found Description");
        assertTrue(solo.searchText("Time"));
        assertTrue(solo.searchText("Date"));
        assertTrue(solo.searchText("Repeat"));
        assertTrue(solo.searchText("Vibrate"));
        assertTrue(solo.searchText("Ringtone"));
        assertTrue(solo.searchText("Conditions"));
        assertTrue(solo.searchText("Style"));
        assertTrue(solo.searchText("Persistence"));
        //assertTrue(solo.searchText("Save"));
        //assertTrue(solo.searchText("Delete"));
        //assertTrue(solo.searchText("Cancel"));
        Logger.d("Finished Test");*/
    }

    @Override
    public void tearDown() throws Exception {
        try {
            solo.finalize();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
        getActivity().finish();
        super.tearDown();
    }
}
