package com.zumobi.android.zbimsampleapp;

import android.util.Log;
import com.zumobi.zbim.logging.ZBiMLogging;

/**
 * Created by danielclark on 3/9/15.
 */
public class ZBiMSDKLogger implements ZBiMLogging {
    @Override
    public void error(String message, String recoverySuggestion) {
        Log.println(Log.ERROR, "ZBiM SDK", message);
    }

    @Override
    public void warning(String message, String recoverySuggestion) {
        Log.println(Log.WARN, "ZBiM SDK", message);
    }

    @Override
    public void info(String message) {
        Log.println(Log.INFO, "ZBiM SDK", message);
    }
}
