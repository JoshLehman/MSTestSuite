package edu.umd.cmsc436.mstestsuite.util;

import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Map;

import edu.umd.cmsc436.mstestsuite.data.TestApp;

/**
 * Asynchronously check installed packages
 *
 * Don't want to clog that UI thread do we now
 */

public class PackageChecker extends AsyncTask<TestApp, Void, Map<String, Float>> {

    private boolean should_update;
    private PackageUtil packageUtil;
    private OnCheckFinishListener listener;

    public PackageChecker(boolean should_update, PackageUtil packageUtil, OnCheckFinishListener listener) {
        this.should_update = should_update;
        this.packageUtil = packageUtil;
        this.listener = listener;
    }

    @Override
    protected Map<String, Float> doInBackground(TestApp... params) {
        Map<String, Float> results = new HashMap<>();

        for (TestApp app : params) {
            String type = PackageUtil.getType(app.getPackageName());
            if (packageUtil.wouldSucceed(app)) {
                if (should_update) {
                    results.put(type, packageUtil.getVersion(app));
                }
            } else {
                results.put(type, -1f);
            }
        }

        return results;
    }

    @Override
    protected void onPostExecute(Map<String, Float> versionInfo) {
        if (listener != null) {
            listener.onCheckFinished(versionInfo);
        }
    }

    public interface OnCheckFinishListener {
        void onCheckFinished(Map<String, Float> versionMap);
    }
}
