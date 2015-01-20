package com.tupigames.sunshine;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;


public class SettingsActivity extends PreferenceActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener
{

    public static final String PREF_KEY_LOCALIZATION    = "pref_localization";
    public static final String PREF_KEY_UNITS           = "pref_unit";
    public static final String PREF_KEY_USEGPS          = "pref_use_gps";
    public static final String PREF_KEY_GPSLOCAL        = "pref_gpslocalization";
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v("SettingsActivity", "onCreate");

        PreferenceManager.setDefaultValues(this, R.xml.preference, false);

        addPreferencesFromResource(R.xml.preference);
        //TODO criar a preferência de "Usar minha localização" e buscar uma localização



        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String unitSelected        = sharedPreferences.getString(SettingsActivity.PREF_KEY_UNITS, "metric");
        Preference connectionPref = findPreference(PREF_KEY_UNITS);
        connectionPref.setSummary( unitSelected );

        connectionPref              = findPreference(PREF_KEY_LOCALIZATION);
        String locaSelected         = sharedPreferences.getString(SettingsActivity.PREF_KEY_LOCALIZATION, "");
        String localByGPS           = sharedPreferences.getString(SettingsActivity.PREF_KEY_GPSLOCAL, "");
        boolean useGPS              = sharedPreferences.getBoolean(SettingsActivity.PREF_KEY_USEGPS, true);

        if ( useGPS )
        {
            connectionPref.setSummary( localByGPS );
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SettingsActivity.PREF_KEY_LOCALIZATION, localByGPS);
        } else {
            connectionPref.setSummary(locaSelected);
        }

        Log.v("SettingsActivity", "PREF_KEY_UNITS - " + sharedPreferences.getString("pref_unit", "??"));



    }

    @Override
    protected void onResume()
    {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }



    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Log.v("SettingsActivity", "onSharedPreferenceChanged");

        if (key.equals(PREF_KEY_LOCALIZATION))
        {
            Log.v("SettingsActivity", "PREF_KEY_LOCALIZATION");
            Preference connectionPref = findPreference(key);
            connectionPref.setSummary(sharedPreferences.getString(key, ""));
        }

        if (key.equals(PREF_KEY_UNITS))
        {
            Log.v("SettingsActivity", "PREF_KEY_LOCALIZATION");
            Preference connectionPref = findPreference(key);
            connectionPref.setSummary(sharedPreferences.getString(key, "metric"));
        }
    }


}
