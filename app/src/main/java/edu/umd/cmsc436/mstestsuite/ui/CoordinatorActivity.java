package edu.umd.cmsc436.mstestsuite.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.umd.cmsc436.frontendhelper.TrialMode;
import edu.umd.cmsc436.mstestsuite.R;
import edu.umd.cmsc436.mstestsuite.data.TestApp;
import edu.umd.cmsc436.mstestsuite.util.PackageUtil;
import edu.umd.cmsc436.sheets.Sheets;

public class CoordinatorActivity extends AppCompatActivity implements Sheets.Host {

    private static final String KEY_PID = "patient id";
    private static final String KEY_DIFFICULTY = "difficulties";
    private static final String KEY_N_TRIALS = "number of trials";
    private static final String KEY_APP_TYPE = "app pkg name";
    public static final int REQUEST_CODE = 789;

    public static void start(Activity activity, String patientId, int difficulty, String app_type, int n_trials) {
        Intent starter = new Intent(activity, CoordinatorActivity.class);
        starter.putExtra(KEY_PID, patientId);
        starter.putExtra(KEY_DIFFICULTY, difficulty);
        starter.putExtra(KEY_APP_TYPE, app_type);
        starter.putExtra(KEY_N_TRIALS, n_trials);
        activity.startActivityForResult(starter, REQUEST_CODE);
    }

    public static String getType (Intent i) {
        if (i != null && i.getExtras() != null) {
            return i.getStringExtra(KEY_APP_TYPE);
        }

        return null;
    }

    private String mCurPatient;
    private int mNumTrials;
    private TestApp mDailyApp;
    private int mDailyDifficulty;
    private String mAppType;
    private List<Intent> mLaunchIntents;
    private Intent mLastIntent;
    private Sheets mScoreSheet;
    private List<Float> mScoreHolder;
    private boolean once;
    private Toast toaster;

    @SuppressLint("ShowToast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onWindowFocusChanged(true);
        setContentView(R.layout.activity_coordinator);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            mCurPatient = extras.getString(KEY_PID, "default patient");
            mNumTrials = extras.getInt(KEY_N_TRIALS);
            mAppType= extras.getString(KEY_APP_TYPE);
            TestApp[] allApps = PackageUtil.loadAppInfo(this, null);

            for (TestApp app : allApps) {
                if (mAppType.equals(PackageUtil.getType(app.getPackageName()))) {
                    mDailyApp = app;
                    break;
                }
            }

            mDailyDifficulty = extras.getInt(KEY_DIFFICULTY);

            toaster = Toast.makeText(this, "", Toast.LENGTH_SHORT);
            createLaunchIntents();
        } else {
            finishWithType(RESULT_CANCELED);
        }

        once = false;
        mScoreHolder = new ArrayList<>();
        mScoreSheet = new Sheets(this, this, getString(R.string.app_name), getString(R.string.scores_spreadsheet_id), getString(R.string.scores_spreadsheet_id));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!once) {
            popLaunchIntent();
            once = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mScoreSheet.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 3000 + mLaunchIntents.size()) {
            if (resultCode == RESULT_OK) {
                mScoreHolder.add(data.getExtras().getFloat(TrialMode.KEY_SCORE, 0f));
            } else {
                // TODO finishWithResult(RESULT_CANCELLED)
                mScoreHolder.add(0f);
            }

            if (mScoreHolder.size() == mNumTrials) {
                float avg = 0f;
                for (float f : mScoreHolder) {
                    avg += f;
                }

                avg /= mScoreHolder.size() == 0 ? 1 : mScoreHolder.size();
                mScoreHolder.clear();

                mScoreSheet.writeData(TrialMode.getAppendage(mLastIntent), mCurPatient, avg);
            } else {
                popLaunchIntent();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mScoreSheet.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void popLaunchIntent () {
        if (mLaunchIntents.size() > 0) {
            mLastIntent = mLaunchIntents.remove(0);

            Sheets.TestType appendage = TrialMode.getAppendage(mLastIntent);
            String text = (appendage == null ? "null" : appendage.toId()) + " " + TrialMode.getTrialNum(mLastIntent) + "/" + TrialMode.getTrialOutOf(mLastIntent);
            toaster.setText(text);
            toaster.show();

            startActivityForResult(mLastIntent, 3000 + mLaunchIntents.size());
        } else {
            finishWithType(RESULT_OK);
        }
    }

    private void finishWithType(int result_code) {
        Intent i = new Intent();
        i.putExtra(KEY_APP_TYPE, mAppType);
        setResult(result_code, i);
        finish();
    }

    private void createLaunchIntents () {
        mLaunchIntents = new ArrayList<>();

        for (Sheets.TestType appendage : mDailyApp.getSupportedAppendages()) {
            for (int cur_trial = 1; cur_trial <= mNumTrials; cur_trial++) {
                Intent launcher = new Intent(mDailyApp.getPackageName() + ".action.TRIAL");
                launcher.putExtra(TrialMode.KEY_APPENDAGE, appendage.ordinal());
                launcher.putExtra(TrialMode.KEY_TRIAL_NUM, cur_trial);
                launcher.putExtra(TrialMode.KEY_DIFFICULTY, mDailyDifficulty);
                launcher.putExtra(TrialMode.KEY_TRIAL_OUT_OF, mNumTrials);
                launcher.putExtra(TrialMode.KEY_PATIENT_ID, mCurPatient);
                mLaunchIntents.add(launcher);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    public int getRequestCode(Sheets.Action action) {
        switch (action) {
            case REQUEST_ACCOUNT_NAME:
                return 2017;
            case REQUEST_AUTHORIZATION:
                return 2018;
            case REQUEST_CONNECTION_RESOLUTION:
                return 2019;
            case REQUEST_PERMISSIONS:
                return 2020;
            case REQUEST_PLAY_SERVICES:
                return 2021;
            default:
                return 2016;
        }
    }

    @Override
    public void notifyFinished(Exception e) {
        popLaunchIntent();
    }
}
