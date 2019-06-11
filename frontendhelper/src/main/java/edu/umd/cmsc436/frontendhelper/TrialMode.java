package edu.umd.cmsc436.frontendhelper;

import android.content.Intent;
import android.support.annotation.Nullable;

import edu.umd.cmsc436.sheets.Sheets;

/**
 * Helper functions to get arguments from TRIAL actions
 */

public class TrialMode {

    public static final String KEY_APPENDAGE = "appendage";
    public static final String KEY_TRIAL_NUM = "trial num";
    public static final String KEY_TRIAL_OUT_OF = "trial out of";
    public static final String KEY_PATIENT_ID = "patient id";
    public static final String KEY_SCORE = "score";
    public static final String KEY_DIFFICULTY = "difficulty";

    /**
     * Extract the Test Type from the intent
     *
     * Reuses the Sheets lib type for convenience
     * @param i the Intent from the front end, with the .TRIAL action
     * @return Null if the argument isn't found, TestType otherwise
     */
    @Nullable
    public static Sheets.TestType getAppendage (Intent i) {
        int temp = i.getIntExtra(KEY_APPENDAGE, -1);

        if (temp < 0 || temp >= Sheets.TestType.values().length) {
            return null;
        }

        return Sheets.TestType.values()[temp];
    }

    /**
     * Get the current trial number from the frontend intent
     *
     * For display purposes
     * @param i intent from frontend, with .TRIAL action
     * @return the current trial, or -1 if no arg found
     */
    public static int getTrialNum (Intent i) {
        return i.getIntExtra(KEY_TRIAL_NUM, -1);
    }

    /**
     * Get the total number of trials from the frontend intent
     *
     * For display purposes
     * @param i intent from frontend, with .TRIAL action
     * @return the total number of trials, or -1 if no arg found
     */
    public static int getTrialOutOf (Intent i) {
        return i.getIntExtra(KEY_TRIAL_OUT_OF, -1);
    }

    /**
     * Get the patient id for the current trial from the frontend intent
     *
     * To be used to store raw data for the patient
     * @param i intent from the frontend, with .TRIAL action
     * @return the patient id, or null if not found
     */
    @Nullable
    public static String getPatientId (Intent i) {
        return i.getStringExtra(KEY_PATIENT_ID);
    }

    /**
     * Gets the difficulty for this trial fromt he frontend intent
     * @param i intent from the frontend, with .TRIAL action
     * @return the difficulty, greater than 0 or -1 if not found
     */
    public static int getDifficulty (Intent i) {
        return i.getIntExtra(KEY_DIFFICULTY, -1);
    }

    /**
     * Create a new intent to return a score result
     * @param score the score for the current single trial
     * @return an intent to be used with {@link android.app.Activity#setResult(int, Intent)}
     */
    public static Intent getResultIntent (float score) {
        Intent i = new Intent();
        i.putExtra(KEY_SCORE, score);
        return i;
    }
}
