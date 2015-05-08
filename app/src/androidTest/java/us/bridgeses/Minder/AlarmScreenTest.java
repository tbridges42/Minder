package us.bridgeses.Minder;

import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.internal.Shadow;
import org.robolectric.shadows.ShadowActivity;

/**
 * Created by Tony on 5/7/2015.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class AlarmScreenTest {

    AlarmScreen alarmScreen;

    @Before
    public void setUp(){
        DaoFactory daoFactory = DaoFactory.getInstance();
        daoFactory.setTest(new TestDao());
    }

    @Test
    public void testNegativeIdReminder_shouldFail(){
        Intent intent = new Intent(RuntimeEnvironment.application,AlarmScreen.class);
        intent.putExtra("Id",14);
        try {
            alarmScreen = Robolectric.buildActivity(AlarmScreen.class).withIntent(intent).get();
        }
        catch (Exception e) {
            assert(e instanceof IllegalArgumentException);
        }
    }
}
