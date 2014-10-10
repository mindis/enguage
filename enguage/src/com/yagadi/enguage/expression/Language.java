package com.yagadi.enguage.expression;

import java.util.Locale;
import com.yagadi.enguage.util.Strings;

public class Language {  // English-ism!
	static public final String[] headers = { "(", "{", "[" };
	static public final String[] tailers = { ")", "}", "]" };
	
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
	static public boolean isPlural(String plural ) { // rough and ready!
		int len = plural.length();
		return (len>=3 && plural.substring( len-3 ).equals( "ies" )) ||
			(len>=4 && plural.substring( len-4 ).equals( "sses")) ||
			(len>=1 && plural.substring( len-1 ).equals( "s" ) && !(len>=2 && plural.substring( len-2 ).equals( "ss" )));
	}
	static public String asString( String[] ans ) {
		String str = "";
		ans = apostropheContraction( ans, "t" );
		ans = apostropheContraction( ans, "s" );
		for (int i=0; i<ans.length; i++) {
			if (i > 0 &&
				!Strings.contain( headers,     ans[ i-1]) &&
				!Strings.contain( terminators, ans[  i ]) &&
				!Strings.contain( tailers,     ans[  i ]))
				str += " ";
			str += ans[ i ];
		}
		return str;
	}
	public static String capitalise( String a ) {
		return a.length()>0 ? a.toUpperCase(Locale.getDefault()).charAt(0) + a.substring( 1 ) : "";
	}
	static String[] sentenceCapitalisation( String[] a ) {
		if (a != null && a.length > 0 && a[ 0 ].length()>0)
			a[ 0 ] = capitalise( a[ 0 ]); // ... if so, start with capital
		return a;
	}
	// replace [ x, "'", "y" ] with "x'y" -- or /dont/ or /martins/ if vocalised
	static String[] apostropheContraction( String[] a, String letter ) {
		if (null != a) for (int i=0, sz=a.length; i<sz-2; i++)
			if ( a[ i+1 ].equals( "'" ) && a[ i+2 ].equalsIgnoreCase(letter)) {
				a[ i ] = (a[ i ] +"'"+ letter);
				a = Strings.removeAt( a, i+1 );
				a = Strings.removeAt( a, i+1 );
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
	static public String[] indefiniteArticleVowelSwap( String[] ans ) {
		for (int i=0, sz=ans.length; i<sz-1; ++i)
			if (   ans[ i ].equalsIgnoreCase(  "a" )
			    || ans[ i ].equalsIgnoreCase( "an" ))
				ans[ i ] = isVowel( ans[ 1+i ].charAt( 0 )) ? "an" : "a";
		return ans;
	}
	static public String[] indefiniteArticleFlatten( String[] a ) {
		for (int i=0; i<a.length; i++)
			if (a[ i ].equalsIgnoreCase( "an" ))
				a[ i ] = "a";
		return a;	
	}
	static public boolean wordsEqualIgnoreCase( String a, String b ) {
		if ((a.equalsIgnoreCase( "an" ) || a.equalsIgnoreCase( "a" )) &&
		    (b.equalsIgnoreCase( "an" ) || b.equalsIgnoreCase( "a" ))    ) return true;
		return a.equalsIgnoreCase( b );
	}
	// ...and finally, terminators
	static public String[] terminators = { ".", "?", "!" };
	static public void     terminators( String[] a ){ terminators = a; }
	static public String[] terminators() { return terminators; }
	static public boolean  isTerminator( String s ) { return Strings.contain( terminators(), s ); }
	static public String   terminatorIs( String[] a ){ return (null != a) && a.length>0 ? a[ a.length -1] : ""; }
	static public boolean  isTerminated( String[] a ) {
		boolean rc = false;
		if (null != a) {
			int last = a.length - 1;
			if (last > -1) rc = isTerminator( a[ last ]);
		}
		return rc; 
	}
	static public String[] stripTerminator( String[] a ) {
		if (isTerminated( a ))
			a = Strings.removeAt( a, a.length - 1 );
		return a;
	}
	static public String stripTerminator( String a ) {
		return Strings.toString( stripTerminator( Strings.fromString( a )), Strings.SPACED );
	}
	static public String[] addTerminator( String[] a, String term ) {
		if (!isTerminated( a ) && null != term)
			a = Strings.append( a, term );
		return a;
	}
	static public String[] addTerminator( String[] a ) { return addTerminator( a, terminators()[ 0 ]); }
}