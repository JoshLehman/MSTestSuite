# MS Test Suite

Front end for the various MS test apps.

## How to integrate a test app

There are four intent actions (in the category `android.intent.category.DEFAULT`) that should be caught by the test app's intent-filters:
- `edu.umd.cmsc436.{...}.action.TRIAL`
- `edu.umd.cmsc436.{...}.action.PRACTICE`
- `edu.umd.cmsc436.{...}.action.HELP`
- `edu.umd.cmsc436.{...}.action.HISTORY`

The possible values in the curly braces are:
- `tap.hand`
- `tap.foot`
- `spiral`
- `balance`
- `level`
- `pop`
- `flex`
- `walk.outdoors`
- `walk.indoors`
- `symbol`

For example, the intent to launch the Practice Mode of the Hand Tap app would have the action `edu.umd.cmsc436.tap.hand.action.PRACTICE`.

`.PRACTICE` and `.HELP` don't have any arguments or expect any results.  Practice mode itself is relatively unstructured, and should let the patient practice as long as they want before exiting.  Help mode should display instructions or a guide to using the specific test, and then switch to Practice mode.

`.HISTORY` has 1 argument:
- Patient ID, which is a string of the current patient's id

`.TRIAL` has 5 arguments:
- Appendage, which is a `Sheets.TestType` (from the [sheets436](https://github.com/cmsc436/sheets436) library)
- Trial Num, an integer, the current trial out of this appendage's number of trials, for display purposes
- Trial Out Of, an integer, the current appendage's number of trials, for display purposes
- Patient ID, a string, for storing raw data appropriately
- Difficulty, an integer greater than 0, the lower the easier

There are a few static methods to help with argument extraction as well:
- `getAppendage(Intent)`
- `getTrialNum(Intent)`
- `getTrialOutOf(Intent)`
- `getPatientId(Intent)`
- `getDifficulty(Intent)`

These will help with getting info out of intents delivered with the `.TRIAL` action.

`.TRIAL` also expects a single float result, the score for the trial.  This can be done with the [`Activity#setResult(int, Intent)`](https://developer.android.com/reference/android/app/Activity.html#setResult(int%2c%20android.content.Intent)) method, and the intent from the `TrialMode.getResultIntent(float)` helper static method.  The `int` argument should be `Activity.RESULT_OK` if everything's okay, or `Activity.RESULT_CANCELLED` if the patient has exited early or there is some other error.

The helper library uses types from the sheets436 library, so make sure to include that as a dependency.  There are also a few color resources defined to help with consistency:
- `colorPrimary436`, orange
- `colorPrimaryDark436`, a slightly darker orange (for the status bar, if present)
- `colorAccent436`, blue
- `colorAccentLight436`, a light blue
- `colorBackground436`, a light gray

These values were taken from [colorcombos.com](http://www.colorcombos.com/color-schemes/89/ColorCombo89.html).

Please place APKs of your test apps in [this folder](https://drive.google.com/drive/folders/0B0tPbEe1hFoEWGc4YllWT3hjQ1E?usp=sharing) with the filename of `<test type>-<version>.apk`, where `<test type>` is one of the same values as the possible actions ("tap", "spiral", "walk.indoors", etc.) and version is a whole number or a number with a single decimal point, that increases with new versions.

## Prescription Format

One row per patient, with columns:

Patient ID | Date Assigned | Frequency | Number of trials per app | Column per Test App ... | Status
--- | --- | --- | --- | --- | ---
String | DD/MM/YYYY HH:MM:SS (24 hour time) | Integer > 0 | Jenkins format (eventually) | Difficulty Integer (0 is disable, 1 is easiest) | Assigned, Ignored, or Completed
