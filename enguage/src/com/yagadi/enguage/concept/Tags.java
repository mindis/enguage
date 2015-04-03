package com.yagadi.enguage.concept;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import com.yagadi.enguage.expression.Language;
import com.yagadi.enguage.expression.Plural;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Number;
import com.yagadi.enguage.util.Strings;
//import com.yagadi.enguage.sofa.Numeric;


public class Tags extends ArrayList<Tag> {
	static final   long serialVersionUID = 0;
	static private Audit audit = new Audit( "Tags" );
	static public  boolean debug = false;
	
	// with postfix boilerplate:
	// typically { [ ">>>", "name1" ], [ "/", "name2" ], [ "/", "name3" ], [ "<<<", "" ] }.
	// could be  { [ ">>>", "name1", "" ], [ "/", "name2", "" ], [ "/", "name3", "<<<" ] }.
	public String toString() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) str += ti.next().toString();
		return str;
	}
	public String toText() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) str += ti.next().toText();
		return str;
	}
	public String toLine() {
		String str="";
		Iterator<Tag> ti = iterator();
		while (ti.hasNext()) {
			Tag t = ti.next();
			str += ( " "+t.prefix()+" <"+t.name() +" "+ t.attributes().toString() +"/> "+t.postfix());
		}
		return str;
	}

	public Tags() { super(); }
	public boolean equals( Tags ta ) {
		if (ta == null || size() != ta.size())
			return false;
		else {
			Iterator<Tag> it = iterator(), tait = ta.iterator();
			while (it.hasNext())
				if (!it.next().equals( tait.next() ))
					return false;
		}
		return true;
	}
	public boolean matches( Tags patterns ) {
		//audit.traceIn( "matches", patterns.toString() );
		if (patterns == null || size() < patterns.size()) return false;
		Iterator<Tag> it = iterator(), pit = patterns.iterator();
		while (it.hasNext()) // ordered by patterns
			if (!it.next().matches( pit.next() ))
				return false;
		return true;
	}
	// "if X do Y" -> [ <x pref="if "/>, <y pref="do "/>, <pref="."> ]
	public Tags( String str ) {
		Strings words = new Strings( str ); // correct? ...FromChars? ...NonWS?
		String prefix = "";
		Tag t = new Tag();
		int i = -1;
		while (words.size() > ++i ) {
			//System.out.println( "  Processing word " + "\n", words.get( i ));
			if ((1 == words.get( i ).length()) && Strings.isUpperCase( words.get( i )) && !words.get( i ).equals("I")) {
				t.name( words.get( i ).toLowerCase( Locale.getDefault())).prefix( prefix );
				add( t );
				t = new Tag();
				prefix = new String( Tag.emptyPrefix );
			} else if (Strings.isUpperCaseWithHyphens( words.get( i )) && !words.get( i ).equals( "I")) {
				Strings arr = new Strings( words.get( i ), '-' );
				int j = -1;
				if (null != arr) while (arr.size() > ++j) {
					arr.set( j, arr.get( j ).toLowerCase( Locale.getDefault()));
					if (arr.size() > j+1 )
						t.attribute( arr.get( j ), arr.get( j ));
					else
						t.name( arr.get( j ));
				}
				t.prefix( prefix );
				add( t );
				t = new Tag();
				prefix =  new String( Tag.emptyPrefix );
			} else
				prefix += (words.get( i ) + " ");
		}
		t.prefix( prefix );
		if (!t.isNull()) add( t );
	}
	/*
	 * matchValues() coming soon...
	 */
	private Attribute matchedAttr( Tag t, String val ) {
		// ...add this extracted a value
		//
		//if (debug) audit.traceIn( "matchedAttr", t.toString() +", "+ val );
		String newVal = Attribute.expandValues( // prevents X="x='val'"
				t.name().equals("unit") ? Plural.singular( val ) : val
			).toString( Strings.SPACED );
		//if (debug) audit.traceOut( t.name() +"="+ newVal );
		return new Attribute( t.name(), newVal );
	}
	
	private String doNumeric( Tag t, ListIterator<String> ui ) {
		//if (debug) audit.traceIn( "doNumeric", "getting number to match "+ t.name() );
		Number number = Number.getNumber( ui );
		String tmp = number.valueOf().equals( Number.NotANumber ) ? null : 
			t.attribute( Tag.phrase ).equals( Tag.phrase ) ? number.toString() :
				number.valueOf();
		//if (debug) audit.traceOut( tmp ); //: number.array() );
		return tmp;
	}
	private String getPhraseTerm( Tag t, ListIterator<Tag> ti ) {
		String term = null;
		if (t.postfix != null && !t.postfix.equals( "" ))
			term = t.postfixAsStrings().get( 0 );
		else if (ti.hasNext()) {
			// next prefix as array is...
			Strings arr = ti.next().prefixAsStrings();
			// ...first token of which is the terminator
			term = (arr == null || arr.size() == 0) ? null : arr.get( 0 );
			ti.previous();
		}
		return term;
	}
	private boolean validListEnd( String name, ListIterator li ) { // li IS generic, ok!
		if (li.hasNext()) {
			//if (debug) {
			//	String tmp = "";
			//	while (li.hasNext()) tmp += (" " + li.next().toString()); 
				//audit.audit( "not matched: remaining "+ name +": "+ tmp  );
			//}
			return false;
		}
		return true;
	}
	private String getVal( Tag t, ListIterator<Tag> ti, ListIterator<String> ui) {
		String u = "unset";
		if (ui.hasNext()) u = ui.next();
		Strings vals = new Strings( u );
		if (t.phrased() || (ui.hasNext() && Strings.contain( Reply.conjunctions(), u ))) {
			String term = getPhraseTerm( t, ti );
			//audit.audit( "phrased, looking for terminator "+ term );
			// here: "... one AND two AND three" => "one+two+three"
			if (term == null) {  // just read to the end
				while (ui.hasNext()) {
					u = ui.next();
					vals.add(
						Strings.contain( Reply.conjunctions(), u ) || u.equals( Attribute.VALUE_SEP ) ?
							Attribute.VALUE_SEP : u
					);
				}
			} else {
				while (ui.hasNext()) {
					u = ui.next();
					//audit.debug( "next u="+ u );
					if (term.equals( u )) {
						ui.previous();
						break;
					} else {
						vals.add(
								Strings.contain( Reply.conjunctions(), u ) || u.equals( Attribute.VALUE_SEP ) ?
									Attribute.VALUE_SEP : u
							);
		}	}	}	}
		return vals.toString( Strings.SPACED );
	}
	
	public Attributes matchValues( Strings utterance ) {
		//* sanity check
		Strings  prefix = get( 0 ).prefixAsStrings();
		if (prefix.size() > 0) {
			//audit.audit( "sanity check:"+ prefix.get( 0 ) + ":AND:"+ utterance.get( 0 ));
			if (!prefix.get( 0 ).equalsIgnoreCase( utterance.get( 0 ) ))
				return null;
		}
		// */
		/* We need to be able to extract:
		 * NAME="value"				... <NAME/>
		 * NAME="some value"		... <NAME phrased="phrased"/>
		 * NAME="68"                ... <NAME numeric='numeric'/>
		 * ???NAME="an/array/or/list"	... <NAME array="array"/>
		 * ???NAME="value one/value two/value three" <NAME phrased="phrased" array="array"/>
		 */
		Attributes matched = null; //lazy creation
		ListIterator<Tag>    ti = listIterator();
		ListIterator<String> ui = utterance.listIterator();
		
		//if (debug) audit.traceIn( "matchValues", "'"+ toLine() +"'" ); // +"', a =>'"+Strings.toString( sa, Strings.SPACED) +"'" );
		Tag readAhead = null;
		// step thru' [..., "pref"+<pattern/>, ...] && [..., "pref", "value", ...] together
		while ( ti.hasNext() && ui.hasNext() ) {
			Tag t = (readAhead != null) ? readAhead : ti.next();
			readAhead = null;
			if (null == (ui = matchBoilerplate( t.prefixAsStrings(), ui ))) { // ...match prefix
				//if (debug) audit.traceOut("prefix mismatch:"+ (!ti.hasNext() ? "LENGTH" : null == t ? "NULL" : t.prefix()));
				return null;
				
			} else if (!ui.hasNext() && t.name().equals( "" )) { // end of array on null (end?) tag...
				// ...don't move ai on: !ai & we're finished, !NAME(ti) & check ai with next tag
				//if (debug) audit.audit( "Tags.matchValues():EOU && blankTag("+ t.toString() +") -- read over empty tag" );
				if (ti.hasNext()) readAhead = ti.next();
				
			} else if (ui.hasNext() && !t.name().equals( "" )) { // do these names match?
				String val = null;
				if (t.attribute( Tag.numeric ).equals( Tag.numeric )) {
					
					if (null == (val = doNumeric( t, ui ))) 
						//if (debug) audit.traceOut( "non-numeric" );
						return null;
					
				} else if ( null == invalidTagReason( t, ui ) ) {
					//if (debug) audit.traceOut( "invalid:"+ reason );
					return null;
				} else {
					val = getVal( t, ti, ui );
				}
					
				// ...add value
				if (null == matched) matched = new Attributes();
				matched.add( matchedAttr( t, val )); // remember what it was matched with!
				
				//if (debugSwitch) audit.debug("checking postfix boilerplate:"+ t.postfix() +":"+ (ui.hasNext()?u:"UEND") +":" );
				if (null == (ui = matchBoilerplate( t.postfixAsStrings(), ui ))) {
					//if (debug) audit.traceOut( "postfix mismatch:"+ t.postfix() );
					return null;
		}	}	}
		
		if (!validListEnd(      "tags", ti )) {
			//if (debug) audit.traceOut();
			return null;
		}
		if (!validListEnd( "utterance", ui )) {
			//if (debug) audit.traceOut();
			return null;
		}
		
		//if (debug) audit.traceOut( "matched => "+ (matched==null ? "no values" : matched.toString()));
		return null == matched ? new Attributes() : matched;
	}
	private String invalidTagReason( Tag t, ListIterator<String> ui  ) {
		String rc = "";
		// This matches a tag name with a candidate string
		// used in interpreting tags and their (string) prefixes: "some text prefix <tag/>" <=> "some",...
		// "matches" === "case insensitive"
		// this deals with QUOTED-, PLURAL- and ABSTRACT- which, i think, are mutually exclusive.
		// now also   with NUMERIC- and PLURAL- which are, i think, are typically used together: I need another 2 beers.
		// this all assumes there are not two hotspots together <x/><y/>, or <x phrase/> <y phrase/>
		String candidate = "unset";
		if (ui.hasNext()) candidate = ui.next();
		boolean more = ui.hasNext();
		boolean
			textIsQuoted  = Language.isQuoted( candidate ) /*|| candidate.contains( Tag.quotedPrefix )*/, // "a quote"  || "QUOTED-X"
			textIsPlural  =   Plural.isPlural( candidate ) /*|| candidate.contains( Tag.pluralPrefix )*/; //  queenies  || "PLURAL-X"
		if (!t.validQuote( textIsQuoted )) {
			rc = "quote mismatch:"+ t.name()+":"+textIsQuoted+":"+ (more?candidate:"--end--");
		} else if (!t.validPlural( textIsPlural )) {
			rc = "plural mismatch:"+ t.name()+":"+textIsPlural+":"+ (more?candidate:"--end--");
		} else if (t.attribute( Tag.abstr ).equals( Tag.abstr ) &&
		           candidate.equalsIgnoreCase( t.name()) )
		{ // don't match ABSTRACT tags if names are the same!
			rc = "abstract mismatch:"+ t.name()+":"+ (more?candidate:"--end--");
		}
		if (ui.hasPrevious()) candidate = ui.previous();
		return rc;
	}
	private static ListIterator<String> matchBoilerplate( Strings tbp, ListIterator<String> ui ) {
		Iterator<String> tbpi = tbp.iterator();
		while ( tbpi.hasNext() && ui.hasNext()) 
			if (!Language.wordsEqualIgnoreCase( tbpi.next(), ui.next() ))
				return null; // string mismatch
		// have we reached end of boilerplate, but not utterance?
		return tbpi.hasNext() ? null : ui;
	}
	
	
	// --- test code...
	static String
			prefix = "what is",
			prefix2 = "wh is",
            postfix = "?",
	       	testPhrase = prefix + " " + "1 + 2" + postfix, //+" "+ prefix +" "+ "3+4"+ postfix;
	       	testPhrase2 = prefix + " " + "1 + 2" + postfix +" "+ prefix2 +" "+ "5 + 4"+ postfix;
	
	private static void printTagsAndValues( Tags ta, String phrase ) {
		audit.audit( "Matching phrase: "+ phrase +"\n      with:"+ ta.toLine());
		Attributes values = ta.matchValues( new Strings( phrase ));
		audit.audit( "values => ["+ (values==null?" null":values.toString()) +" ]\n" );
	}

	public static void main(String args[]) {
		Audit.turnOn();
		debug = true;
		Tags ta = new Tags();
		Tag t = new Tag( prefix + " ", "X", postfix ).attribute( Tag.numeric, Tag.numeric );
		ta.add( t );
		printTagsAndValues( ta, testPhrase );
		
		// ok, add a PHRASE- attribute and repeat this test...
		ta = new Tags();
		t = new Tag( prefix + " ", "X", postfix ).attribute( Tag.numeric, Tag.numeric );
		t.attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, testPhrase );
		// */
		
		// ok, add a PHRASE- attribute and repeat this test...
		ta = new Tags();
		t = new Tag( prefix + " ", "X", postfix ).attribute( Tag.numeric, Tag.numeric );
		ta.add( t );
		t = new Tag( prefix2 + " ", "Y", postfix ).attribute( Tag.numeric, Tag.numeric );
		ta.add( t );
		printTagsAndValues( ta, testPhrase2 );
		// */
		
		ta = new Tags();
		t = new Tag( "I've seen ", "X", "" ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, "I've seen all good people" );

		ta = new Tags();
		t = new Tag( "I've seen ", "X", " people" ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, "I've seen all good people" );

		ta = new Tags();
		t = new Tag( "I've seen ", "X", "" ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		t = new Tag("turn their heads", "Y" ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, "I've seen all good people turn their heads each day" );

		ta = new Tags();
		t = new Tag( "I've seen ", "X", "" ).attribute( Tag.quoted, Tag.quoted );
		ta.add( t );
		printTagsAndValues( ta, "I've seen \"all good people\"" );

		ta = new Tags();
		t = new Tag( "I've seen ", "X", "" ).attribute( Tag.numeric, Tag.numeric );
		ta.add( t );
		t = new Tag( " ", "Y", " people " ).attribute( Tag.phrase, Tag.phrase );
		ta.add( t );
		printTagsAndValues( ta, "I've seen a damn good people" );
}	}