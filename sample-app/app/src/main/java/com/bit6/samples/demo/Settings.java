package com.bit6.samples.demo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private SharedPreferences pref;
    public static int PRODUCTION = 0;
    public static int DEVELOPMENT = 1;
    public static int MODE_P2P = 0, MODE_MIX = 1;
    final static String PREF_ENV_ID = "envId";
    public static String MEDIA_MODE = "media_mode";

    public Settings(Context context) {
        pref = context.getSharedPreferences("Settings", Activity.MODE_PRIVATE);
    }

    public int getEnvironment() {
        return pref.getInt(PREF_ENV_ID, PRODUCTION);
    }

    public void setEnvironment(int env) {
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt(PREF_ENV_ID, env);
        ed.commit();
    }

    public void setMediaMode(int newMode) {
        int oldMode = getMediaMode();
        if (newMode == oldMode) {
            return;
        }

        SharedPreferences.Editor ed = pref.edit();
        ed.putInt(MEDIA_MODE, newMode);
        ed.commit();
    }

    public int getMediaMode() {
        int oldMode = pref.getInt(MEDIA_MODE, MODE_P2P);
        return oldMode;
    }
}
