package edu.umd.cmsc436.mstestsuite;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import edu.umd.cmsc436.mstestsuite.data.ActionsAdapter;
import edu.umd.cmsc436.mstestsuite.model.UserManager;
import edu.umd.cmsc436.sheets.Sheets;

/**
 * Define which part of the app can do what
 */

interface MainContract {

    // things the view can do
    interface View {
        void expandBottomSheet ();
        void collapseBottomSheet ();
        void hideBottomSheet ();
        void loadActions(ActionsAdapter adapter);
        void showToast (String message);
        void startPracticeMode(String packageName) throws ActivityNotFoundException;
        Context getContext ();
        void showUserSwitcher (String[] users);
        Sheets.Host getHost ();
        Activity getActivity ();
        void installPackage (File f) throws IOException;
        void showHistoryDialog (String user);
        void openFeedbackEmail ();
        void showHelpDialog ();
    }

    // things the presenter responds to
    interface Presenter {
        void onDailyStart ();
        void onCloseBottomSheet ();
        void onBottomSheetSlide ();
        void onBottomSheetStateChange (int newState);
        boolean onBackPressed ();
        void onDestroy ();
        void onUserSelected (String patient_id);
        void onUserCreated (String patient_id, UserManager.Handedness h, String dateOfBirth, UserManager.Gender gender);
        void onActivityResult(int requestCode, int resultCode, Intent data);
        void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
        void onPackageInstalled ();
        void onCoordinatorDone ();
        void onTrialFinished (String type);
        void onGoToPracticeMode ();
    }
}
