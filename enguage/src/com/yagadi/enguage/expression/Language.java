package com.yagadi.enguage.expression;

import java.util.Locale;

import com.yagadi.enguage.concept.Repertoire;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;


public class Language {  // English-ism!
	static public final Strings headers = new Strings( "( { [" );
	static public final Strings tailers = new Strings( ") } ]" );
	
	static public boolean isQuoted(String a) { // universal?
		boolean rc = false;
		if (null!=a) {
			int len = a.length();
			rc = ((a.charAt( 0 ) ==  '"') && (a.charAt( len-1 ) ==  '"'))
			  || ((a.charAt( 0 ) == '\'') && (a.charAt( len-1 ) == '\''));
		}
		return rc;
	}
	static public boolean isQuote(String a) { // universal?
		return (null!=a) && (a.equals('\'') || a.equals('"'));
	}
	static public String asString( Strings ans ) {
		String str = "";
		ans = apostropheContraction( ans, "tag" );
		ans = apostropheContraction( ans, "s" );
		for (int i=0; i<ans.size(); i++) {
			if (i > 0 &&
				          !headers.contain( ans.get( i-1)) &&
				!Shell.terminators.contain( ans.get(  i )) &&
				          !tailers.contain( ans.get(  i )))
				str += " ";
			str += ans.get( i );
		}
		return str;
	}
	public static String capitalise( String a ) {
		return a.length()>0 ? a.toUpperCase(Locale.getDefault()).charAt(0) + a.substring( 1 ) : "";
	}
	static Strings sentenceCapitalisation( Strings a ) {
		if (a != null && a.size() > 0 && a.get( 0 ).length()>0)
			a.set( 0, capitalise( a.get( 0 ))); // ... if so, start with capital
		return a;
	}
	static Strings pronunciation( Strings a ) {
		if (a != null) for(int i=0; i< a.size(); i++)
			if (a.get(i).equals( Repertoire.NAME ))
				a.set( i, Repertoire.PRONUNCIATION );
			else if (a.get(i).equals( Plural.plural( Repertoire.NAME )))
				a.set( i, Repertoire.PLURALISATION );
		return a;
	}
	// replace [ x, "'", "y" ] with "x'y" -- or /dont/ or /martins/ if vocalised
	static Strings apostropheContraction( Strings a, String letter ) {
		if (null != a) for (int i=0, sz=a.size(); i<sz-2; i++)
			if ( a.get( i+1 ).equals( "'" ) && a.get( i+2 ).equalsIgnoreCase(letter)) {
				a.set( i, a.get( i ) +"'"+ letter);
				a.remove( i+1 );
				a.remove( i+1 );
			}
		return a;
	}
	/*public static String apostropheRemoval( String a ) {
		String b = "";
		if (null != a) for (int i=0; i<a.length(); i++)
			if ( a.charAt( i ) != '\'' )
				b += a.charAt( i );
		return b;
	} // */
	static private boolean isVowel( char ch ) {
		return  ('a' == ch) || ('e' == ch) || ('i' == ch) || ('o' == ch) || ('u' == ch)  
		     || ('A' == ch) || ('E' == ch) || ('I' == ch) || ('O' == ch) || ('U' == ch); 
	}
	static public Strings indefiniteArticleVowelSwap( Strings ans ) {
		for (int i=0, sz=ans.size(); i<sz-1; ++i)
			if (   ans.get( i ).equalsIgnoreCase(  "a" )
			    || ans.get( i ).equalsIgnoreCase( "an" ))
				ans.set( i, isVowel( ans.get( 1+i ).charAt( 0 )) ? "an" : "a" );
		return ans;
	}
	static public Strings indefiniteArticleFlatten( Strings a ) {
		for (int i=0; i<a.size(); i++)
			if (a.get( i ).equalsIgnoreCase( "an" ))
				a.set( i, "a" );
		return a;	
	}
	static public boolean wordsEqualIgnoreCase( String a, String b ) {
		if ((a.equalsIgnoreCase( "an" ) || a.equalsIgnoreCase( "a" )) &&
		    (b.equalsIgnoreCase( "an" ) || b.equalsIgnoreCase( "a" ))    ) return true;
		return a.equalsIgnoreCase( b );
	}
	// ...and finally, terminators... moved to Shell
}