package com.yagadi.enguage.expression;

import java.util.ListIterator;
import java.util.Locale;

import com.yagadi.enguage.concept.Repertoire;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;


public class Language {  // English-ism!
	
	//static private Audit audit = new Audit( "Language" );

	static public final Strings headers = new Strings( "( { [" );
	static public final Strings tailers = new Strings( ") } ]" );
	
	static public boolean isQuoted(String a) { // universal?
		int len;
		return (null != a) &&
			   ((len = a.length())>1) &&
			   (   ((a.charAt( 0 ) ==  '"') && (a.charAt( len-1 ) ==  '"'))
			    || ((a.charAt( 0 ) == '\'') && (a.charAt( len-1 ) == '\'')) );
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
		if (a != null && a.size() > 0)
			a.set( 0, capitalise( a.get( 0 ))); // ... if so, start with capital
		return a;
	}
	static Strings pronunciation( Strings a ) {
		if (a != null) {
			for(ListIterator<String> ai = a.listIterator(); ai.hasNext();) {
				String s = ai.next();
				if (s.equals( Repertoire.NAME ))
					ai.set( Repertoire.PRONUNCIATION );
				else if (s.equals( Plural.plural( Repertoire.NAME )))
					ai.set( Repertoire.PLURALISATION );
		}	}
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
	/*static public Strings indefiniteArticleVowelSwap( Strings ans ) {
		ListIterator<String> ai = ans.listIterator();
		while (ai.hasNext()) {
			String articleCandidate = ai.next();
			if (ai.hasNext() &&
				(   articleCandidate.equalsIgnoreCase(  "a" )      // ... a  QUANTITY ...
				 || articleCandidate.equalsIgnoreCase( "an" ))) {  // ... an ENGINEER ...
				// look forward to next word...
				String nextWord = new String( ai.next());
				String tmp = new String( ai.previous()); // go back to article
				audit.audit( "art next: "+ tmp +" "+ nextWord );
				ai.set( isVowel( nextWord.charAt( 0 )) ? "an" : "a" );
		}	}
		return ans;
	}*/

	static public Strings indefiniteArticleVowelSwap( Strings ans ) {
		for (int i=0, sz=ans.size(); i<sz-1; ++i)
			if (   ans.get( i ).equalsIgnoreCase(  "a" )
			    || ans.get( i ).equalsIgnoreCase( "an" ))
				ans.set( i, isVowel( ans.get( 1+i ).charAt( 0 )) ? "an" : "a" );
		return ans;
	}
/*	static private Strings indefiniteArticleFlatten( Strings a ) {
		for (int i=0; i<a.size(); i++)
			if (a.get( i ).equalsIgnoreCase( "an" ))
				a.set( i, "a" );
		return a;	
	}*/
	static public boolean wordsEqualIgnoreCase( String a, String b ) {
		if ((a.equalsIgnoreCase( "an" ) || a.equalsIgnoreCase( "a" )) &&
		    (b.equalsIgnoreCase( "an" ) || b.equalsIgnoreCase( "a" ))    ) return true;
		return a.equalsIgnoreCase( b );
	}
	// ...and finally, terminators... moved to Shell
}