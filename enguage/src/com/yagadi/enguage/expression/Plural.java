package com.yagadi.enguage.expression;

import java.util.ArrayList;
import com.yagadi.enguage.util.Shell;

public class Plural {
	public static final String NAME = "plural";
	
	private static ArrayList<String> singularExceptions = new ArrayList<String>();
	private static ArrayList<String> pluralExceptions   = new ArrayList<String>();
	static public void addException( String s, String p ) {
		singularExceptions.add( s );
		pluralExceptions.add( p );
	}
	
	private static ArrayList<String> singularEndings = new ArrayList<String>();
	private static ArrayList<String> pluralEndings   = new ArrayList<String>();
	static public void addRule( String s, String p ) {
		singularEndings.add( s );
		pluralEndings.add( p );
	}
	
	static public String plural( String s ) {
		int i, len = s.length();
		// already plural
		if (isPlural( s )) return s;
		// check if it is an exception
		if (-1 != (i = singularExceptions.indexOf( s ))) return pluralExceptions.get( i );
		// check endings
		for (i=0; i< singularEndings.size(); i++) {
			int engingLen = singularEndings.get( i ).length();
			if (len>=engingLen && s.substring( len-engingLen ).equals( singularEndings.get( i ) )) 
				return s.substring( 0, len-engingLen ) + pluralEndings.get( i );
		}
		return s + "s"; // rough and ready!
	}
	static public String singular( String p ) {
		int i, len = p.length();
		// check if already singular
		if (!isPlural( p )) return p;
		// check if it is an exception
		if (-1 != (i = pluralExceptions.indexOf( p ))) return singularExceptions.get( i );
		// check endings
		for (i=0; i< pluralEndings.size(); i++) { 
			int engingLen = pluralEndings.get( i ).length();
			if (len>=engingLen && p.substring( len-engingLen ).equals( pluralEndings.get( i ) )) 
				return p.substring( 0, len-engingLen ) + singularEndings.get( i );
		}
		// single s as plural -- can this be a rule too?
		if (  len>=1 && p.substring( len-1 ).equals(  "s" )
		&&  !(len>=2 && p.substring( len-2 ).equals( "ss" )))
			return p.substring( 0, len-1 );
		
		// doesn't get here
		return p; // already singular
	}
	static public boolean isPlural(String p ) {
		// first check exceptions
		if (-1 != pluralExceptions.indexOf( p )) return true;
		// then the rules
		int i, len = p.length();
		for (i=0; i< pluralEndings.size(); i++) { 
			int engingLen = pluralEndings.get( i ).length();
			if (len>=engingLen && p.substring( len-engingLen ).equals( pluralEndings.get( i ) ))
				return true;
		}
		
		// then plain old singular s -- should this be a rule?
		if (  len>=1 && p.substring( len-1 ).equals(  "s" )
		&&  !(len>=2 && p.substring( len-2 ).equals( "ss" ))) return true;
		
		// its not a plural
		return false;
	}
	public static String interpret( String[] a ) {
		if (null == a)
			return Shell.FAIL;
		else if (a[ 0 ].equals("exception") && a.length == 3)
			addException( a[ 1 ], a[ 2 ]);
		else if (a[ 0 ].equals("rule") && a.length == 3)
			addRule( a[ 1 ], a[ 2 ]);
		return Shell.SUCCESS;
	/* Plurals:
	 * colloquial <=> colloquia  : because it is an exception
	 * princess   <=> princesses : *ss adds 'es'
	 * prince     <=> princes    : just adds 's'
	 *
	}
	public static void stest( String s ) {
		System.out.println( "the singular of "+ s +" is "+ singular( s ));
	}
	public static void ptest( String s ) {
		System.out.println( "the plural of "+ s +" is "+ plural( s ));
	}
	public static void main(String[] args) {
		Plural.addRule(  "y",  "ies" );
		Plural.addRule( "ss", "sses" );
		Plural.addException( "colloquial", "colloquia" ); // the plural of X is Y

		System.out.println( " ==== Plural tests:" );
		ptest( "colloquial" );
		ptest( "kings" );
		ptest( "queeny" );
		ptest( "princess" );
		ptest( "prince" );
		
		System.out.println( " ==== Singular tests:" );
		stest( "colloquia" );
		stest( "king" );
		stest( "queenies" );
		stest( "princesses" );
		stest( "princes" );
	*/
}	}