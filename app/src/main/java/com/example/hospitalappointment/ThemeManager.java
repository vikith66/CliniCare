package com.example.hospitalappointment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;
    
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static void setThemeMode(Context context, int themeMode) {
        SharedPreferences prefs = getPrefs(context);
        prefs.edit().putInt(KEY_THEME_MODE, themeMode).apply();
        
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    
    public static int getThemeMode(Context context) {
        SharedPreferences prefs = getPrefs(context);
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }
    
    public static boolean isDarkTheme(Context context) {
        int currentMode = getThemeMode(context);
        if (currentMode == THEME_DARK) {
            return true;
        } else if (currentMode == THEME_SYSTEM) {
            int nightModeFlags = context.getResources().getConfiguration().uiMode & 
                                Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        }
        return false;
    }
    
    public static void applyTheme(Activity activity) {
        int themeMode = getThemeMode(activity);
        setThemeMode(activity, themeMode);
    }
}
