package com.yagadi.enguage.sofa;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

import android.content.SharedPreferences;

/* Preferences are implemented in the SofA, but are of device structures.
 * The reason for this is so that enguage can also access values. 
 */
public class Preferences {
	static private Audit audit = new Audit( "Preferences" );
	
	static SharedPreferences shPref = null; // can't set yet -- causes null ptr exception

	// singleton
	static private Preferences preferences = null;
	static public void        setPreferences( Preferences p ){ preferences = p; }
	static public Preferences getPreferences(){ return preferences; }

	public Preferences( SharedPreferences s ) { shPref = s; }
	
	public String get( String name, String defVal ) {
		if (defVal.equalsIgnoreCase( "true" ) || defVal.equalsIgnoreCase( "false" ))
			return get( name, defVal.equalsIgnoreCase( "true" )) ? "true" : "false";
		else {
			return shPref.getString( name, defVal );
	}	}
	public void set( String name, String value ) {
		if (value.equalsIgnoreCase( "true" ) || value.equalsIgnoreCase( "false" ))
			set( name, value.equalsIgnoreCase( "true" ));
		else {
			SharedPreferences.Editor editor = shPref.edit();
			editor.putString( name, value );
			editor.commit();
		}	}
	public boolean get( String name, boolean defVal ) {
		return shPref.getBoolean( name, defVal ); // defer to SharedPreference version
	}
	public void set( String name, boolean value ) {
		SharedPreferences.Editor editor = shPref.edit();
		editor.putBoolean( name, value );
		editor.commit();
	}
/*	public float get( String name, float defVal ) {
		return shPref.getFloat( name, defVal ); // defer to SharedPreference version
	}
	public void set( String name, float value ) {
		SharedPreferences.Editor editor = shPref.edit();
		editor.putFloat( name, value );
		editor.commit();
}*/
	static public String interpret( String[] a ) {
		String rc = Shell.FAIL;
		audit.traceIn( "interpret", Strings.toString( a, Strings.CSV ));
		if (preferences != null && null != a && a.length > 2) {
			if (a[ 0 ].equals( "set" )) {
				preferences.set( a[ 1 ], Strings.toString( Strings.copyAfter( a, 1 ), Strings.SPACED ) ); // default value?
				rc = Shell.SUCCESS;
		}	} // interpreter only ever sets the preferences!
		audit.traceOut( rc );
		return rc;
}	}
