package com.ses.zebra.pssdemo_2019.Debugging;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Logger {

    // Constants
    private static final String NEW_LINE =  System.getProperty("line.separator");
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.UK);
    private static String mLogFilePath = Environment.getExternalStorageDirectory()
            + File.separator + "PSSDemo" + File.separator + "Stock" + File.separator + "Log.txt";

    public static void e(@NonNull String TAG, @Nullable String exceptionQualifier, @NonNull Throwable e) {
        // Show in LogCat
        Log.e(TAG, exceptionQualifier == null ? "" : exceptionQualifier);

        // Append Log
        pushLogToStringBuilder("e", getStackTrace(TAG, e));
    }

    public static void i(@NonNull String TAG, @NonNull String logText) {
        // Show in LogCat
        Log.i(TAG, logText);

        // Append Log
        pushLogToStringBuilder("i", TAG + ": " + logText);
    }

    public static void d(@NonNull String TAG, @NonNull String logText) {
        // Show in LogCat
        Log.d(TAG, logText);

        // Append Log
        pushLogToStringBuilder("d", TAG + ": " + logText);
    }

    public static void v(@NonNull String TAG, @NonNull String logText) {
        // Show in LogCat
        Log.v(TAG, logText);

        // Append Log
        pushLogToStringBuilder("v", TAG + ": " + logText);
    }

    private static String getStackTrace(String TAG, Throwable t) {
        // add the class name and any message passed to constructor
        final StringBuilder stackTrace = new StringBuilder(TAG);
        stackTrace.append(t.getMessage());
        stackTrace.append(NEW_LINE);

        //add each element of the stack trace
        if (t.getStackTrace() != null) {
            for (StackTraceElement stackTraceElement : t.getStackTrace()) {
                stackTrace.append(stackTraceElement);
                stackTrace.append(NEW_LINE);
            }
        }
        return stackTrace.toString();
    }

    private static void pushLogToStringBuilder(String logLevel, String logText) {
        StringBuilder logStringBuilder = new StringBuilder();
        logStringBuilder.append(mDateFormat.format(new Date()));
        logStringBuilder.append(": ");
        logStringBuilder.append(logLevel);
        logStringBuilder.append(" - ");
        logStringBuilder.append(logText);
        logStringBuilder.append(NEW_LINE);

        new WriteLogToFile().execute(logStringBuilder);
    }

    public static class WriteLogToFile extends AsyncTask<StringBuilder, Void, Boolean> {
        @Override
        protected Boolean doInBackground(StringBuilder... log) {
            // Create Directory if Doesn't Exist
            File logFile = new File(mLogFilePath);
            if (logFile.getParentFile().exists() || logFile.getParentFile().mkdirs()){
                if (!logFile.exists()) {
                    try {
                        logFile.createNewFile();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Write to LogFile
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(mLogFilePath, true))) {
                final int aLength = log[0].length();
                final int aChunk = 1024;// 1 kb buffer to read data from
                final char[] aChars = new char[aChunk];
                for (int aPosStart = 0; aPosStart < aLength; aPosStart += aChunk) {
                    final int aPosEnd = Math.min(aPosStart + aChunk, aLength);
                    log[0].getChars(aPosStart, aPosEnd, aChars, 0); // Create no new buffer
                    bw.write(aChars, 0, aPosEnd - aPosStart); // This is faster than just copying one byte at the time
                }
                bw.write("\r\n");
                bw.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean writeComplete) {
            if (writeComplete) {
                Log.i("Logger", "Successfully written log to file");
            } else {
                Log.i("Logger", "Could not write log to file");
            }
        }
    }

    public static void logDeviceInfo() {
        StringBuilder deviceInfoStringBuilder = new StringBuilder();
        deviceInfoStringBuilder.append("Model: " + android.os.Build.MODEL);
        deviceInfoStringBuilder.append("\r\n");

        deviceInfoStringBuilder.append("Brand: " + android.os.Build.BRAND);
        deviceInfoStringBuilder.append("\r\n");

        deviceInfoStringBuilder.append("Product: " + android.os.Build.PRODUCT);
        deviceInfoStringBuilder.append("\r\n");

        deviceInfoStringBuilder.append("Device: " + android.os.Build.DEVICE);
        deviceInfoStringBuilder.append("\r\n");

        deviceInfoStringBuilder.append("Codename: " + android.os.Build.VERSION.CODENAME);
        deviceInfoStringBuilder.append("\r\n");

        deviceInfoStringBuilder.append("Release: " + android.os.Build.VERSION.RELEASE);
        deviceInfoStringBuilder.append("\r\n");

        new WriteLogToFile().execute(deviceInfoStringBuilder);
    }

}
