package edu.my.rstetsenko.zenua.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import edu.my.rstetsenko.zenua.fragments.MainPreferenceFragment;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }
}
