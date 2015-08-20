package com.maximum.fastride.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        setActivityInitialTouchMode(true);

        activity = getActivity();
    }

    @MediumTest
    public void makePicture() {
        TextView clickMe = (TextView)activity.findViewById(R.id.detection_monitor);
        TouchUtils.clickView(this, clickMe);

        LinearLayout layout = (LinearLayout)activity.findViewById(R.id.detection_buttons_bar);
        assertTrue(View.VISIBLE == layout.getVisibility());
    }

    @SmallTest
    public void testLayout() {
        CameraBridgeViewBase camera = (CameraBridgeViewBase)activity.findViewById(R.id.java_surface_view);
        assertNotNull(camera);

        final View decorView = activity.getWindow().getDecorView();
        ViewAsserts.assertOnScreen(decorView, camera);

        final ViewGroup.LayoutParams layoutParams = camera.getLayoutParams();
        assertNotNull(layoutParams);

        assertEquals(layoutParams.width, WindowManager.LayoutParams.MATCH_PARENT);
        assertEquals(layoutParams.height, WindowManager.LayoutParams.MATCH_PARENT);

        LinearLayout layout = (LinearLayout)activity.findViewById(R.id.detection_buttons_bar);
        ViewAsserts.assertOnScreen(decorView, layout);
        assertTrue(View.GONE == layout.getVisibility());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
