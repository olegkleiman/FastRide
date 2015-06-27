package com.maximum.fastride.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.ImageView;

import com.maximum.fastride.DriverRoleActivity;
import com.maximum.fastride.MainActivity;
import com.maximum.fastride.R;

/**
 * Created by Oleg on 24-Jun-15.
 */
public class DriverActivityTest extends ActivityInstrumentationTestCase2<DriverRoleActivity> {

    DriverRoleActivity activity;

    public DriverActivityTest() {
        super(DriverRoleActivity.class);
    }

    public DriverActivityTest(Class<DriverRoleActivity> activityClass) {
        super(activityClass);

        activity = getActivity();

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();
    }

    @SmallTest
    public void testLayout() {
        View fab = (View)activity.findViewById(R.id.submit_ride_button);
        assertNotNull(fab);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
