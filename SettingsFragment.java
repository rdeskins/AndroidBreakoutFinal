package com.example.rdeskinsfinal;

import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(prefs, "brickCount");
        onSharedPreferenceChanged(prefs, "paddleSens");
        onSharedPreferenceChanged(prefs, "paddleWidth");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (key.equals("brickCount")) {
            int bricks = Integer.parseInt(sharedPreferences.getString(key, "5"));
            if (bricks > 100)
                bricks = 100;
            else if (bricks < 1)
                bricks = 1;

            String bricksString = Integer.toString(bricks);
            sharedPreferences.edit().putString(key, bricksString).apply();
            pref.setSummary(bricksString);
            ((EditTextPreference) pref).setText(bricksString);
        }
        else if (key.equals("paddleSens")) {
            int paddleSens = Integer.parseInt(sharedPreferences.getString(key,"50"));
            if (paddleSens > 100) {
                paddleSens = 100;
            }
            else if (paddleSens < 1) {
                paddleSens = 1;
            }

            String sensitivityString = Integer.toString(paddleSens);
            sharedPreferences.edit().putString(key, sensitivityString).apply();
            pref.setSummary(sensitivityString);
            ((EditTextPreference) pref).setText(sensitivityString);
        }
        else if (key.equals("paddleWidth")) {
            int paddleWidth = Integer.parseInt(sharedPreferences.getString(key, "30"));
            if (paddleWidth > 50) {
                paddleWidth = 50;
            }
            else if (paddleWidth < 10) {
                paddleWidth = 10;
            }

            String widthString = Integer.toString(paddleWidth);
            sharedPreferences.edit().putString(key, widthString).apply();
            pref.setSummary(widthString);
            ((EditTextPreference) pref).setText(widthString);
        }

    }
}