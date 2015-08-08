package com.maximum.fastride.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.maximum.fastride.CameraCVActivity;
import com.maximum.fastride.R;

import org.opencv.android.CameraBridgeViewBase;

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

    @SmallTest
    public void testLayout() {
        CameraBridgeViewBase camera = (CameraBridgeViewBase)activity.findViewById(R.id.java_surface_view);
        assertNotNull(camera);

//        camera.setVisibility(SurfaceView.VISIBLE);
//        camera.setCvCameraViewListener(activity);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
