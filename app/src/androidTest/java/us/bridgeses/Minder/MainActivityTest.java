package us.bridgeses.Minder;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.robotium.solo.By;
import com.robotium.solo.Solo;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.MainListActivity;

public class MainActivityTest extends
        ActivityInstrumentationTestCase2<MainListActivity>{

    private Solo solo;

    public MainActivityTest() {
        super(MainListActivity.class);
    }

    @Override
    protected void setUp() throws Exception{
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @SmallTest
    public void testActivityExists() {
        assertNotNull(getActivity());
        assertNotNull(solo);
    }

    @SmallTest
    public void testActionBar() {
        assertTrue("Incorrect page heading", solo.searchText("Minder"));
    }

    @SmallTest
    public void testAboutFragment() {
        solo.clickOnActionBarItem(R.id.action_about);
        solo.waitForFragmentByTag("About_fragment");
        assertTrue("Version missing", solo.searchText("Version"));
        assertTrue("Website missing",solo.searchText("Official Website"));
        assertTrue("Bug report missing",solo.searchText("Submit Bugs"));
        assertTrue("Feedback missing",solo.searchText("Feedback"));
        assertTrue("Icon attribution missing", solo.searchText("Icons by:"));
        //TODO: Test links work
        solo.goBack();
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
