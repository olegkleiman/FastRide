package com.maximum.fastride.tests;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.test.ApplicationTestCase;
import android.test.MoreAsserts;
import android.test.suitebuilder.annotation.SmallTest;

import com.maximum.fastride.utils.WAMSVersionTable;

import java.util.StringTokenizer;

public class ApplicationTest extends ApplicationTestCase<Application> {

    private Application application;

    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        application = getApplication();
    }

    @SmallTest
    public void testCorrectVersion() throws Exception {
        PackageInfo info = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
        assertNotNull(info);
        MoreAsserts.assertMatchesRegex("\\d\\.\\d", info.versionName);

        VersionMismatchListener listener = new VersionMismatchListener();
        WAMSVersionTable wamsVersionTable = new WAMSVersionTable(getApplication(), listener);

        String packageVersionName = info.versionName;
        if (!packageVersionName.isEmpty()) {

            String[] tokens = packageVersionName.split("\\.");
            int majorPackageVersion = Integer.parseInt(tokens[0]);
            int minorPackageVersion = Integer.parseInt(tokens[1]);

            wamsVersionTable.compare(majorPackageVersion, minorPackageVersion);
        }
    }

    public void testMainActivity() {
        //getActivity();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}