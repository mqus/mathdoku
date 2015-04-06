package net.mathdoku.plus.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.util.fileprovider.DatabaseFileType;
import net.mathdoku.plus.util.fileprovider.FileProvider;
import net.mathdoku.plus.util.fileprovider.LogFileType;
import net.mathdoku.plus.util.fileprovider.ScreendumpFileType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class FeedbackEmail {
    @SuppressWarnings("unused")
    private static final String TAG = FeedbackEmail.class.getName();

    private static final String EOL_DELIMITER = "\n";
    private static final String FIELD_DELIMITER_LEVEL1 = "|";
    private static final String FIELD_DELIMITER_LEVEL2 = "=";

    // Date format for log file
    private final DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    // Reference to log file
    private OutputStream mLogFile;

    private final Activity mActivity;

    /**
     * Creates a new instance of {@link net.mathdoku.plus.util.FeedbackEmail}.
     *
     * @param activity
     *         The activity context in which the UsageLogging is created.
     */
    public FeedbackEmail(Activity activity) {
        mActivity = activity;
    }

    /**
     * Create a log file containing basic information about the device, the configuration and the preferences.
     *
     * @param filename
     *         The name of the log file to be created.
     * @return True in case the file has been created. False otherwise.
     */
    @SuppressWarnings("SameParameterValue")
    private boolean createLogFile(String filename) {
        File file = new File(mActivity.getFilesDir(), filename);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    return false;
                }
            } catch (IOException e) {
                Log.d(TAG, "Error while creating log file for feedback email.", e);
                return false;
            }
        }

        try {
            mLogFile = new FileOutputStream(file);
            logDevice();
            logConfiguration();
            logPreferences();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Log file for feedback email not found.", e);
            return false;
        } catch (IOException e) {
            Log.d(TAG, "Error while writing to log file for feedback email.", e);
            return false;
        } finally {
            close();
        }

        return true;
    }

    /**
     * Logs info about device.
     */
    private void logDevice() throws IOException {
        SortedMap<String, String> sortedMap = new TreeMap<String, String>();

        sortedMap.put("Android.Version", android.os.Build.VERSION.CODENAME);
        sortedMap.put("Android.SDK_INT", Integer.toString(android.os.Build.VERSION.SDK_INT));

        sortedMap.put("Dimension", mActivity.getResources()
                .getString(R.string.dimension));

        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mActivity.getWindowManager()
                .getDefaultDisplay()
                .getMetrics(mDisplayMetrics);

        sortedMap.put("Display.Density", Float.toString(mDisplayMetrics.density));
        sortedMap.put("Display.Width", Float.toString(mDisplayMetrics.widthPixels));
        sortedMap.put("Display.Height", Float.toString(mDisplayMetrics.heightPixels));
        sortedMap.put("Display.xdpi", Float.toString(mDisplayMetrics.xdpi));
        sortedMap.put("Display.ydpi", Float.toString(mDisplayMetrics.ydpi));

        logSortedMap("Device", sortedMap);
    }

    /**
     * Logs information about a configuration.
     */
    private void logConfiguration() throws IOException {
        SortedMap<String, String> sortedMap = new TreeMap<String, String>();

        Configuration configuration = mActivity.getResources()
                .getConfiguration();
        // noinspection ConstantConditions
        sortedMap.put("locale", configuration.locale.toString());
        sortedMap.put("orientation", Integer.toString(configuration.orientation));

        logSortedMap("Configuration", sortedMap);
    }

    /**
     * Logs all preferences.
     */
    private void logPreferences() throws IOException {
        // Get preferences and check whether it is allowed to gather new data.
        Preferences preferences = Preferences.getInstance();

        if (preferences != null) {
            logSortedMap("Settings", getSortedPreferencesMap(preferences));
        }
    }

    private SortedMap<String, String> getSortedPreferencesMap(Preferences preferences) {
        SortedMap<String, String> sortedMap = new TreeMap<String, String>();

        for (Map.Entry<String, ?> entry : preferences.getAllSharedPreferences()
                .entrySet()) {
            if (entry != null) {
                String key = entry.getKey();
                if (key == null) {
                    continue;
                }
                Object value = entry.getValue();
                sortedMap.put(key, value == null ? "" : value.toString());
            }
        }

        return sortedMap;
    }

    /**
     * Write a set of key/value pairs to the log.
     *
     * @param identifier
     *         The identifier for this map
     * @param map
     *         The set of key/values to be written to the log.
     */
    private void logSortedMap(String identifier, SortedMap<String, String> map) throws IOException {
        String logLine = identifier;

        // Get Map in Set interface to get key and value
        for (Map.Entry<String, String> entry : map.entrySet()) {
            // Get key and value
            String key = entry.getKey();
            String value = entry.getValue();

            logLine += FIELD_DELIMITER_LEVEL1 + key + FIELD_DELIMITER_LEVEL2 + (value == null ? "" : value);
        }
        writeLine(logLine + EOL_DELIMITER);
    }

    /**
     * Write a single line to the log file.
     *
     * @param line
     *         The line to be written.
     */
    private void writeLine(String line) throws IOException {
        mLogFile.write(getLinePrefixedWithCurrentDatetime(line).getBytes());
        mLogFile.flush();
    }

    private String getLinePrefixedWithCurrentDatetime(String line) {
        return mDateFormat.format(new Date()) + FIELD_DELIMITER_LEVEL1 + line;
    }

    /**
     * Close the log file.
     */
    private void close() {
        if (mLogFile != null) {
            try {
                mLogFile.flush();
                mLogFile.close();
            } catch (IOException e) {
                Log.d(TAG, "Error while closing logfile of FeedbackEmail.", e);
            }
        }
    }

    /**
     * Ask consent of user to send log via email.
     */
    public void show() {
        if (isNoEmailIntentAvailable(mActivity)) {
            // No email client available anymore.
            return;
        }

        // Create a screen dump before showing the alert.
        final Screendump screendump = new Screendump(mActivity);
        screendump.save(mActivity.getWindow()
                                .getDecorView(), ScreendumpFileType.NAME);

        new AlertDialog.Builder(mActivity).setTitle(mActivity.getResources()
                                                            .getString(R.string.dialog_send_feedback_title))
                .setMessage(mActivity.getResources()
                                    .getString(R.string.dialog_send_feedback_text))
                .setNegativeButton(R.string.dialog_general_button_close, null)
                .setPositiveButton(R.string.dialog_send_feedback_positive_button,
                                   new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                           createEmailWithAttachments();
                                       }
                                   })
                .show();
    }

    private void createEmailWithAttachments() {
        try {
            mActivity.startActivity(Intent.createChooser(createEmailWithAttachmentsIntent(), mActivity.getResources()
                    .getString(R.string.dialog_send_feedback_title)));
        } catch (android.content.ActivityNotFoundException ex) {
            Log.d(TAG, "No email app installed", ex);
            showNoEmailAppInstalledDialog();
        }
    }

    private Intent createEmailWithAttachmentsIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"info@mathdoku.net"});
        intent.putExtra(Intent.EXTRA_SUBJECT, mActivity.getResources()
                .getString(R.string.feedback_email_subject));
        intent.putExtra(Intent.EXTRA_TEXT, mActivity.getResources()
                .getString(R.string.feedback_email_body));
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, getAttachmentUris());

        return intent;
    }

    private ArrayList<?
            extends
            Parcelable> getAttachmentUris() {
        List<Uri> uris = new ArrayList<Uri>();

        uris.add(FileProvider.getUri(ScreendumpFileType.NAME));
        if (createLogFile(LogFileType.NAME)) {
            uris.add(FileProvider.getUri(LogFileType.NAME));
        }
        if (Config.APP_MODE == Config.AppMode.DEVELOPMENT && copyDatabase(DatabaseFileType.NAME)) {
            uris.add(FileProvider.getUri(DatabaseFileType.NAME));
        }
        return (ArrayList<?
                extends
                Parcelable>) uris;
    }

    private void showNoEmailAppInstalledDialog() {
        new AlertDialog.Builder(mActivity).setTitle(R.string.dialog_no_email_client_found_title)
                .setMessage(R.string.dialog_sending_feedback_email_is_not_possible_body)
                .setNeutralButton(R.string.dialog_general_button_close, null)
                .create()
                .show();
    }

    /**
     * Checks if an email client is available on the device.
     *
     * @param activity
     *         The activity which started this usage logger.
     * @return True in case no email client is installed. False otherwise.
     */
    private static boolean isNoEmailIntentAvailable(Activity activity) {
        final PackageManager packageManager = activity.getPackageManager();
        if (packageManager == null) {
            return false;
        }

        return packageManager.queryIntentActivities(getSendEmailIntent(), PackageManager.MATCH_DEFAULT_ONLY)
                .isEmpty();
    }

    private static Intent getSendEmailIntent() {
        return new Intent(Intent.ACTION_SEND).setType("message/rfc822");
    }

    private boolean copyDatabase(String filename) {
        File file = new File(mActivity.getFilesDir(), filename);
        if (file.exists() && !file.delete()) {
            return false;
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(mActivity.getDatabasePath(filename));
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = fileInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len1);
            }
            fileInputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            Log.d(TAG, "Error while copying database for feedback email.", e);
            return false;
        } finally {
            close();
        }

        return true;
    }
}
