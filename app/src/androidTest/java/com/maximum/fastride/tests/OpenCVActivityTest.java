package com.maximum.fastride.tests;

import android.test.ActivityInstrumentationTestCase2;

import com.maximum.fastride.CameraCVActivity;

/**
 * Created by Oleg Kleiman on 29-Jul-15.
 */
public class OpenCVActivityTest extends ActivityInstrumentationTestCase2<CameraCVActivity> {

    CameraCVActivity activity;

    public OpenCVActivityTest() {
        super(CameraCVActivity.class);
    }

    public OpenCVActivityTest(Class<CameraCVActivity> activityClass) {
        super(activityClass);

        activity = getActivity();

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
