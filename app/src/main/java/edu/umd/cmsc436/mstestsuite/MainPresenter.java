package edu.umd.cmsc436.mstestsuite;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.umd.cmsc436.mstestsuite.data.Action;
import edu.umd.cmsc436.mstestsuite.data.ActionsAdapter;
import edu.umd.cmsc436.mstestsuite.data.TestApp;
import edu.umd.cmsc436.mstestsuite.model.UserManager;
import edu.umd.cmsc436.mstestsuite.ui.CoordinatorActivity;
import edu.umd.cmsc436.mstestsuite.util.PackageChecker;
import edu.umd.cmsc436.mstestsuite.util.PackageUtil;
import edu.umd.cmsc436.sheets.DriveApkTask;
import edu.umd.cmsc436.sheets.Sheets;

/**
 * Do the logic of things without worrying about the view
 */

class MainPresenter implements MainContract.Presenter, TestApp.Events,
        Sheets.OnPrescriptionFetchedListener, PackageChecker.OnCheckFinishListener {

    private enum Mode {
        MAIN,
        PRACTICE,
        TRIAL
    }

    private final Action[] actions = new Action[] {
            new Action("Practice", R.drawable.ic_practice_mode, new Runnable() {
                @Override
                public void run() {
                    mCurMode = Mode.PRACTICE;
                    mView.loadActions(mPracticeModeAdapter);
                }
            }),
            new Action("Switch User", R.drawable.ic_switch_users, new Runnable() {
                @Override
                public void run() {
                    mView.showUserSwitcher(mUserManager.getAllUsers().toArray(new String[mUserManager.getAllUsers().size()]));
                }
            }),
            new Action("Help", R.drawable.ic_help, new Runnable() {
                @Override
                public void run() {
                    mView.showHelpDialog();
                }
            }),
            new Action("History", R.drawable.ic_history, new Runnable() {
                @Override
                public void run() {
                    mView.showHistoryDialog(mUserManager.getCurUserID());
                }
            }),
            new Action("Feedback", R.drawable.ic_feedback, new Runnable() {
                @Override
                public void run() {
                    mView.openFeedbackEmail();
                }
            }),
            new Action("Refresh", R.drawable.ic_refresh_prescription, new Runnable() {
                @Override
                public void run() {
                    mMainAdapter.setEnabled(0, false);
                    mView.hideBottomSheet();
                    mSheet.fetchPrescription(mUserManager.getCurUserID(), MainPresenter.this);
                }
            })
    };

    private MainContract.View mView;

    private Mode mCurMode;
    private boolean isBottomSheetExpanded;
    private UserManager mUserManager;

    private ActionsAdapter mMainAdapter;
    private ActionsAdapter mPracticeModeAdapter;
    private ActionsAdapter mTrialModeAdapter;
    private Sheets mSheet;
    private TestApp[] mAllApps;
    private TestApp[] mAllTrialApps;
    private List<TestApp> mTrialDisplayApps;

    private int mNumTrials;
    private int[] mAllDifficulties;
    private Map<File, Float> mToInstall;
    private ArrayList<TestApp> mDesiredApps;

    MainPresenter(MainContract.View v) {
        mView = v;
        mView.hideBottomSheet();

        mCurMode = Mode.MAIN;
        isBottomSheetExpanded = false;

        mUserManager = new UserManager(mView.getContext());
        if (mUserManager.getCurUserID() == null) {
            UserManager.initWithUser(mView.getContext(), "default patient", UserManager.Handedness.RIGHT, "1/1/1970", UserManager.Gender.MALE);
            mUserManager = new UserManager(mView.getContext());
        }

        mAllApps = PackageUtil.loadAppInfo(mView.getContext(), this);
        mAllTrialApps = PackageUtil.loadAppInfo(mView.getContext(), new TestApp.Events() {
            @Override
            public void onAppSelected(TestApp app) {
                int i;
                for (i = 0; i < mAllApps.length; i++) {
                    if (mAllApps[i].getPackageName().equals(app.getPackageName())) {
                        break;
                    }
                }
                CoordinatorActivity.start(mView.getActivity(), mUserManager.getCurUserID(), mAllDifficulties[i], PackageUtil.getType(app.getPackageName()), mNumTrials);
            }
        });

        mToInstall = new HashMap<>();

        mMainAdapter = new ActionsAdapter(actions, mView.getContext().getString(R.string.main_actions_header, mUserManager.getCurUserID()));
        mPracticeModeAdapter = new ActionsAdapter(mAllApps, mView.getContext().getString(R.string.practice_mode_header_text));
        mTrialModeAdapter = new ActionsAdapter(mAllTrialApps, mView.getContext().getString(R.string.trial_mode_header_text));

        mMainAdapter.setEnabled(0, false);

        mView.loadActions(mMainAdapter);

        mSheet = new Sheets(mView.getHost(),
                mView.getActivity(),
                mView.getContext().getString(R.string.app_name),
                mView.getContext().getString(R.string.prescription_spreadsheet_id),
                mView.getContext().getString(R.string.prescription_spreadsheet_id));
        mSheet.fetchPrescription(mUserManager.getCurUserID(), this);
    }

    @Override
    public void onDailyStart() {
        mView.hideBottomSheet();
        mView.loadActions(mTrialModeAdapter);
        mCurMode = Mode.TRIAL;
    }

    @Override
    public void onCloseBottomSheet() {
        mView.collapseBottomSheet();
    }

    @Override
    public void onBottomSheetSlide() {
        // nothing
    }

    @Override
    public void onBottomSheetStateChange(int newState) {
        isBottomSheetExpanded = newState == BottomSheetBehavior.STATE_EXPANDED;
    }

    @Override
    public boolean onBackPressed() {
        if (isBottomSheetExpanded) {
            mView.collapseBottomSheet();
            return false;
        } else if (mCurMode == Mode.PRACTICE || mCurMode == Mode.TRIAL) {
            if (mCurMode == Mode.TRIAL && mTrialDisplayApps.size() > 0) {
                mView.collapseBottomSheet();
            }
            mCurMode = Mode.MAIN;
            mView.loadActions(mMainAdapter);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onDestroy() {
        // nothing
    }

    @Override
    public void onUserSelected(String patient_id) {
        mUserManager.onUserSelected(patient_id);
        mMainAdapter.setHeader(mView.getContext().getString(R.string.main_actions_header, mUserManager.getCurUserID()));
        mMainAdapter.setEnabled(0, false);
        mSheet.fetchPrescription(mUserManager.getCurUserID(), this);
    }

    @Override
    public void onUserCreated(String patient_id, UserManager.Handedness h, String dateOfBirth, UserManager.Gender gender) {
        mUserManager.onUserCreated(patient_id, h, dateOfBirth, gender);
        mMainAdapter.setHeader(mView.getContext().getString(R.string.main_actions_header, mUserManager.getCurUserID()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSheet.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mSheet.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPackageInstalled() {
        installFirst();
    }

    @Override
    public void onCoordinatorDone() {
        mView.hideBottomSheet();
    }

    @Override
    public void onTrialFinished(String type) {

        for (int i = 0; i < mTrialDisplayApps.size(); i++) {
            if (type.equals(PackageUtil.getType(mTrialDisplayApps.get(i).getPackageName()))) {
                mTrialDisplayApps.remove(i);
                break;
            }
        }

        mTrialModeAdapter.setActions(mTrialDisplayApps.toArray(new Action[mTrialDisplayApps.size()]));

        if (mTrialDisplayApps.size() == 0) {
            mView.showToast(mView.getContext().getString(R.string.trials_completed_text));
            onBackPressed();
        }
    }

    @Override
    public void onGoToPracticeMode() {
        mView.collapseBottomSheet();
        if (mCurMode != Mode.PRACTICE) {
            mCurMode = Mode.PRACTICE;
            mView.loadActions(mPracticeModeAdapter);
        }
    }

    @Override
    public void onAppSelected(TestApp app) {
        try {
            mView.startPracticeMode(app.getPackageName());
        } catch (ActivityNotFoundException anfe) {
            mView.showToast(app.getDisplayName() + " not found");
        }
    }

    @Override
    public void onPrescriptionFetched(@Nullable List<String> list) {
        // Ensure apps installed
        // check if update
        // reveal proper apps
        // check frequency and last completed in settings to determine if ready
        // store difficulty list and number of trials

        if (list == null) {
            Log.e(getClass().getCanonicalName(), "Null prescription");
            mView.showToast("No prescription found for " + mUserManager.getCurUserID());
            mView.hideBottomSheet();
            return;
        }

        try {
            mNumTrials = Integer.parseInt(list.get(2));
        } catch (NumberFormatException nfe) {
            mNumTrials = 1;
        }

        mDesiredApps = new ArrayList<>();
        mAllDifficulties = new int[mAllApps.length];
        for (int i = 0; i < mAllApps.length; i++) {
            try {
                int difficulty = Integer.parseInt(list.get(i + 4));
                mAllDifficulties[i] = difficulty;
                if (difficulty > 0) {
                    mDesiredApps.add(mAllApps[i]);
                }
            } catch (NumberFormatException nfe) {
                // not desired
            }
        }

        boolean update;
        try {
            update = Integer.parseInt(list.get(4 + mAllApps.length)) == 1;
        } catch (NumberFormatException nfe) {
            update = false;
        }

        PackageUtil packageUtil = new PackageUtil(mView.getContext());
        PackageChecker packageChecker = new PackageChecker(update, packageUtil, this);
        packageChecker.execute(mDesiredApps.toArray(new TestApp[mDesiredApps.size()]));
    }

    @Override
    public void onCheckFinished(Map<String, Float> versionMap) {
        mSheet.fetchApks(mView.getContext().getString(R.string.test_apk_folder_id), versionMap, new DriveApkTask.OnFinishListener() {

            @Override
            public void onFinish(Map<File, Float> map) {
                for (File f : map.keySet()) {
                    Log.i(MainPresenter.class.getCanonicalName(), "DOWNLOADED FILE: " + f.getAbsolutePath());
                }

                mToInstall = map;
                installFirst();
            }
        });
    }

    private void installFirst() {

        if (mToInstall.size() == 0) {
            postInstall();
            return;
        }

        File first = mToInstall.keySet().iterator().next();
        float version = mToInstall.remove(first);
        try {
            PackageUtil packageUtil = new PackageUtil(mView.getContext());
            packageUtil.setVersion("edu.umd.cmsc436." + first.getName().substring(0, first.getName().indexOf('-')), version);
            mView.installPackage(first);
        } catch (IOException e) {
            Log.e(getClass().getCanonicalName(), "install failed for " + first.getAbsolutePath());
        }
    }

    private void postInstall () {
        PackageUtil packageUtil = new PackageUtil(mView.getContext());
        PackageChecker packageChecker = new PackageChecker(true, packageUtil, new PackageChecker.OnCheckFinishListener() {
            @Override
            public void onCheckFinished(Map<String, Float> versionMap) {

                refreshTrialAdapter(versionMap);

                List<TestApp> toShow = new ArrayList<>();
                for (TestApp app : mDesiredApps) {
                    String type = PackageUtil.getType(app.getPackageName());
                    if (versionMap.containsKey(type) && versionMap.get(type) > 0f) {
                        // well we have an app I suppose
                        toShow.add(app);
                    }
                }

                mPracticeModeAdapter = new ActionsAdapter(toShow.toArray(new Action[toShow.size()]), mView.getContext().getString(R.string.practice_mode_header_text));

                mView.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMainAdapter.setEnabled(0, true);
                        mView.expandBottomSheet();
                    }
                });
            }
        });
        packageChecker.execute(mDesiredApps.toArray(new TestApp[mDesiredApps.size()]));
    }

    private void refreshTrialAdapter(Map<String, Float> versionMap) {
        mTrialDisplayApps = new ArrayList<>();
        for (TestApp app : mAllTrialApps) {
            String type = PackageUtil.getType(app.getPackageName());
            if (versionMap.containsKey(type) && versionMap.get(type) > 0f) {
                mTrialDisplayApps.add(app);
            }
        }
        mTrialModeAdapter = new ActionsAdapter(mTrialDisplayApps.toArray(new Action[mTrialDisplayApps.size()]), mView.getContext().getString(R.string.trial_mode_header_text));
    }
}
