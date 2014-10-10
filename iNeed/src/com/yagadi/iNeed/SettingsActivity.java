package com.yagadi.iNeed;

import com.yagadi.enguage.Enguage;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setTitle( Enguage.lastLoaded() +": Settings");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { //i.e. 11
            onCreatePreferenceFragment();
        } else { //Api < 11
            onCreatePreferenceActivity();
    }   }
    @SuppressWarnings("deprecation")
    protected void onCreatePreferenceActivity() {
        addPreferencesFromResource( R.xml.preferences );
    }
    @TargetApi(11)
    protected void onCreatePreferenceFragment() {
        getFragmentManager()
        	.beginTransaction()
        	.replace(android.R.id.content, new PF())
        	.commit();
    }
    @TargetApi(11)
    public static class PF extends PreferenceFragment {       
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate( savedInstanceState );
            // outer class private members seem to be visible for inner class, 
            // and making it static made things so much easier
            addPreferencesFromResource( R.xml.preferences );
    }	}	
}