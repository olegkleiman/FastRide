package com.maximum.fastride.tests;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.ImageView;

import com.maximum.fastride.MainActivity;
import com.maximum.fastride.R;

/**
 * Created by Oleg Kleiman on 19-Jun-15.
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

//ok    @SmallTest
//ok    public void testLayout() {
//ok        ImageView imageAvatar = (ImageView)activity.findViewById(R.id.userAvatarView);
//ok        assertNotNull(imageAvatar);
//ok    }

    @MediumTest
    public void testGetLatestVersion(){

        String url = "https://www.dropbox.com/s/txoch9xp6k71b8y/app-lpFlavor-debug.apk?dl=0";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        //intent.setDataAndType(Uri.parse(url), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if( activity != null )
            activity.startActivity(intent);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
