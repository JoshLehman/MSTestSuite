package edu.umd.cmsc436.mstestsuite.data;

import edu.umd.cmsc436.sheets.Sheets;

/**
 * Hold app information, not prescription info
 */

public class TestApp extends Action {
    private String packageName;
    private Sheets.TestType[] supportedAppendages;

    public TestApp(final String packageName, String displayName, int iconResource, CharSequence[] textArray, final Events callback) {
        super(displayName, iconResource, null);
        setAction(new Runnable() {
            @Override
            public void run() {
                callback.onAppSelected(TestApp.this);
            }
        });
        this.packageName = packageName;

        supportedAppendages = new Sheets.TestType[textArray.length];
        for (int i = 0; i < textArray.length; i++) {
            supportedAppendages[i] = Sheets.TestType.valueOf(textArray[i].toString());
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public Sheets.TestType[] getSupportedAppendages() {
        return supportedAppendages;
    }

    public interface Events {
        void onAppSelected (TestApp app);
    }
}
