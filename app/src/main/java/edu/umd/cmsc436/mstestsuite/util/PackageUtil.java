package edu.umd.cmsc436.mstestsuite.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;

import edu.umd.cmsc436.mstestsuite.R;
import edu.umd.cmsc436.mstestsuite.data.TestApp;

/**
 * Helper to check what packages are installed that respond to a given criteria
 */

public class PackageUtil {

    private static final String PREFS_VERSIONS = "Version info";

    private PackageManager pm;
    private SharedPreferences mVersionPrefs;

    public PackageUtil(Context c) {
        pm = c.getPackageManager();
        mVersionPrefs = c.getSharedPreferences(PREFS_VERSIONS, Context.MODE_PRIVATE);
    }

    public static String getType (String packageName) {
        return packageName.replaceFirst("edu\\.umd\\.cmsc436\\.", "");
    }

    public static TestApp[] loadAppInfo(Context c, TestApp.Events callback) {
        Resources res = c.getResources();
        TypedArray package_names = res.obtainTypedArray(R.array.test_prefixes);
        TypedArray display_names = res.obtainTypedArray(R.array.display_names);
        TypedArray icons = res.obtainTypedArray(R.array.icons);
        TypedArray supportedAppendages = res.obtainTypedArray(R.array.supported_appendages);

        TestApp[] apps = new TestApp[package_names.length()];
        if (package_names.length() == display_names.length() && display_names.length() == icons.length() && icons.length() == supportedAppendages.length()) {
            for (int i = 0; i < package_names.length(); i++) {
                apps[i] = new TestApp(
                        package_names.getString(i),
                        display_names.getString(i),
                        icons.getResourceId(i, R.mipmap.ic_launcher),
                        res.getTextArray(supportedAppendages.getResourceId(i, 0)),
                        callback
                );
            }
        } else {
            Log.e(PackageUtil.class.getCanonicalName(), "XML resource arrays not same length");
            apps = new TestApp[0];
        }

        package_names.recycle();
        display_names.recycle();
        icons.recycle();
        supportedAppendages.recycle();

        return apps;
    }

    private boolean wouldSucceed (Intent i) {
        return pm.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

    /**
     * Checks if something can handle the action
     * @param app the TestApp
     * @return true if something exists, else false
     */
    public boolean wouldSucceed (TestApp app) {
        Intent i = new Intent(app.getPackageName() + ".action.PRACTICE");
        return wouldSucceed(i);
    }

    public float getVersion (TestApp app) {
        return mVersionPrefs.getFloat(app.getPackageName(), 0f);
    }

    public void setVersion (String typeAndDomain, float version) {
        Log.i(getClass().getCanonicalName(), "Updating " + typeAndDomain + " to version " + version);
        mVersionPrefs.edit()
                .putFloat(typeAndDomain, version)
                .apply();
    }
}
