package com.maximum.fastride.tests;

import android.opengl.Visibility;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.maximum.fastride.MainActivity;
import com.maximum.fastride.R;
import com.maximum.fastride.RegisterActivity;

/**
 * Created by Oleg on 19-Jun-15.
 */
public class RegistrationTest extends ActivityInstrumentationTestCase2<RegisterActivity> {

    RegisterActivity activity;

    public RegistrationTest() {
        super(RegisterActivity.class);
    }

    public RegistrationTest(Class<RegisterActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();
    }

    @SmallTest
    public void testInitialLayout() {
        Button btnNext = (Button)activity.findViewById(R.id.btnRegistrationNext);
        int visibility = btnNext.getVisibility();
        assertEquals(visibility, View.INVISIBLE);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
