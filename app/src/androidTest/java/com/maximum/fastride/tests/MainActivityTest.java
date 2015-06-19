package com.maximum.fastride.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ImageView;

import com.maximum.fastride.MainActivity;
import com.maximum.fastride.R;

/**
 * Created by Oleg on 19-Jun-15.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    MainActivity activity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    public MainActivityTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        activity = getActivity();
    }

    @SmallTest
    public void testLayout() {
        ImageView imageAvatar = (ImageView)activity.findViewById(R.id.userAvatarView);
        assertNotNull(imageAvatar);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
