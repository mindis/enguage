package com.yagadi.enguage.concept;

import java.util.ArrayList;
import java.util.Iterator;
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
	static final long serialVersionUID = 0;
	static Audit audit = new Audit( "Tags" );
	
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
		audit.traceIn( "equals", ta.toString() );
		if (size() != ta.size()) return false;
		for (int i=0; i<size(); i++)
			if (!get( i ).equals( ta.get( i ) ))
				return audit.traceOut( false );
		return audit.traceOut( true );
	}
	public boolean matches( Tags patterns ) {
		//audit.traceIn( "matches", patterns.toString() );
		if (size() < patterns.size()) return false;
		for (int i=0; i<patterns.size(); i++) // ordered by patterns
			if (!get( i ).matches( patterns.get( i ) )) // pattern is parameter
				return false; //audit.traceOut( false );
		return true; //audit.traceOut( true ); //
	}
	
	// "if X do Y" -> [ <x pref="if "/>, <y pref="do "/>, <pref="."> ]
	public Tags( String str ) {
		Strings words = new Strings( str ); // correct? ...FromChars? ...NonWS?
		int sz = words.size();
		// add period if description is not terminated -- see also Cmd.c
		if (!words.get( sz - 1 ).equals( "." ) && !words.get( sz - 1 ).equals( "!" ) && !words.get( sz - 1 ).equals( "?" ))
			words.add( "." );
			
		String prefix = "";
		Tag t = new Tag();
		int i = -1;
		while (words.size() > ++i ) {
			//System.out.println( "  Processing word " + "\n", words.get( i ));
			if ((1 == words.get( i ).length()) && Strings.isUpperCase( words.get( i )) && !words.get( i ).equals("I")) {
				t.name( words.get( i ).toLowerCase( Locale.getDefault())).prefix( prefix );
				add( t );
				t = new Tag();
				prefix = new String(" ");
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
				prefix = " ";
			} else
				prefix += (words.get( i ) + " ");
		}
		t.prefix( prefix );
		add( t );
	}
	private static int matchBoilerplate( Strings sa, Strings data, int di ) {
		/*
		 * returns either -1 or the new offset. 
		 */
		if ( null != sa && sa.size() > 0 && di < data.size() ) {
			int n = 0;
			int bi = di;
			int sz = sa.size(), dz = data.size();
			while (n<sz
					&& bi+n<dz
					&& Language.wordsEqualIgnoreCase( sa.get( n ), data.get( bi+n ) ))
				n++;
			di = n == sa.size() ? di+n : -1;
		}
		return di;
	}
	public boolean debugSwitch = false;
	// used in interpreting ML tags and their (string) prefixes: "some text prefix <tag/>" <=> "some",...
	// "matches" === "case insensitive"
	// this deals with QUOTED-, PLURAL- and ABSTRACT- which, i think, are mutually exclusive.
	// now also   with NUMERIC- and PLURAL- which are, i think, are typically used together: I need another 2 beers.
	// this all assumes there are not two hotspots together <x/><y/>, or <x phrase/> <y phrase/>
	public Attributes matchValues( Strings utterance ) {
		int usz = utterance.size();
		// check for early termination...
		String firstPrefix = get( 0 ).prefix(),
		       firstUtt    = utterance.get( 0 );
		if (size() == 0 ||
		! /* <-- negate all the below!!!
		   * all the below is to see if they match, negated above
		   * if prefix is just whitespace, ie. " " then they may still match, so say they do!
		   */(  //this is to show if they "could" match
				firstPrefix == null || firstPrefix.equals(" ") || firstPrefix.equals("") // there is no prefix
			 ||
				(firstUtt.length() <= firstPrefix.length() &&                                    //  prefix<X/> &&
		    	 firstUtt.equalsIgnoreCase( firstPrefix.substring(0,firstUtt.length())) )    // "prefixtoken"
		     ||
				((" "+firstUtt).length() <= firstPrefix.length() &&                                    //
				 (" "+firstUtt).equalsIgnoreCase( firstPrefix.substring(0,firstUtt.length()+1 )) ) // 
			))
		{
			//if (debugSwitch) audit.debug( "matchValues() -- early termination! "+ get( 0 ).toString() );
			return null; // -- */
		}
		
		if (debugSwitch) audit.traceIn( "matchValues", "'"+ toLine() +"'" ); // +"', a =>'"+Strings.toString( sa, Strings.SPACED) +"'" );
		Attributes matched = null; //new Attributes();
		int tai = 0, uai = 0; // 'cos we've got 2 indexes tag[++ti] would fail before a[++ai] is evaluated
		while ( tai<size() && uai<usz) { // step thru' [..., "pref"+<pattern/>, ...] && [..., "pref", "value", ...] together
			Tag t = get( tai );
			if (-1 == (uai = matchBoilerplate( t.prefixAsStrings(), utterance, uai ))) { // ...match prefix
				if (debugSwitch) audit.traceOut("prefix mismatch:"+ (tai >= size() ? "LENGTH" : null == t ? "NULL" : t.prefix()));
				return null;
			} else if (uai>=usz && t.name().equals( "" )) { // end of array on null (end?) tag...
				tai++; // ...don't move ai on: !ai & we're finished, !NAME(ti) & check ai with next tag
			} else if (uai<usz && /* !a[ ai ].equals( "" ) &&*/ !t.name().equals( "" )) { // do these names match?
				/*
				 * OK, now validate this tag...
				 */
				String u = utterance.get( uai );
				Number number = Number.getNumber( utterance, uai ); //read ahead... it IS numeric if >0
				if (debugSwitch) audit.debug( "n="+ number.toString());
				boolean // TODO: try taking this first half out -- would be part of boilerplate!
					textIsQuoted  = Language.isQuoted( u ) || u.contains( Tag.quotedPrefix ), // "a quote"  || "QUOTED-X"
					textIsPlural  =   Plural.isPlural( u ) || u.contains( Tag.pluralPrefix ), //  queenies  || "PLURAL-X"
					textIsNumeric =        number.repSize()>0 || u.contains( Tag.numericPrefix );//               "NUMERIC-X"
					if (debugSwitch) audit.audit( "nNumeric="+ number.repSize() +", sa[ sai ]="+ u +", ta="+ t.toString() +", textIsNumeric="+ textIsNumeric );
				if (!t.validQuote( textIsQuoted )) {
					if (debugSwitch) audit.traceOut("quote mismatch:"+ t.name()+":"+textIsQuoted+":"+ (usz>uai?u:"--end--"));
					return null;
				} else if (!t.validPlural( textIsPlural )) {
					if (debugSwitch) audit.traceOut("plural mismatch:"+ t.name()+":"+textIsPlural+":"+ (usz>uai?u:"--end--"));
					return null;
				} else if (t.attribute( Tag.abstr ).equals( Tag.abstr ) &&
				           u.equalsIgnoreCase( t.name()) )
				{ // don't match ABSTRACT tags if names are the same!
					if (debugSwitch) audit.traceOut("abstract mismatch:"+ t.name()+":"+ (usz>uai?u:"--end--"));
					return null;
				} else {
					/*
					 *  ...OK so its valid, now extract a value...
					 */
					/* We need to be able to extract:
					 * NAME="value"				... <NAME/>
					 * NAME="some value"		... <NAME phrased="phrased"/>
					 * NAME="an/array/or/list"	... <NAME  array="array"/>
					 * NAME="value one/value two/value three" <NAME phrased="phrased" array="array"/>
					 * NAME="68"                ... <NAME numeric='numeric'/>
					 */
					u = utterance.get( uai++ );
					String val = u;
					if (t.attribute( Tag.numeric ).equals( Tag.numeric )) {
						//int n.size() = Numeric.areNumeric( sa, sai ); //read ahead... it IS numeric if >0
						//boolean textIsNumeric = n.size() > 0 || sa[ sai ].contains( Tag.numericPrefix );// "NUMERIC-X"
						if (debugSwitch) audit.audit( "tag has "+ Tag.numeric +" attribute =>nNumeric is expected" );
						if (number.valueOf().equals( Number.NotANumber )) {
							// bomb out should be numeric but isn't
							if (debugSwitch) audit.traceOut("numeric mismatch:"+ t.name()+":"+textIsNumeric+":"+ (usz>uai?u:"--end--"));
							return null;
						}
						int orig = number.repSize(), ni = number.repSize();
						while (--ni > 0) {
							if (debugSwitch) audit.audit( "Tags.mV():loading"+ utterance.get( uai ));
							val += (" " + utterance.get( uai++ ));
						}
						if (debugSwitch) audit.audit( "FOUND NUMERICAL PHRASE sz=("+ orig +"): "+ val );
						/* NB Here val may be "another one", but n will be "+1"
						 */
					} else if (t.phrased() || (uai<usz && Strings.contain( Reply.conjunctions(), u ))) {
						// next prefix as array is...
						Strings arr = (size() > tai+1) ? get( tai+1 ).prefixAsStrings() : null;
						// ...first token of which is the terminator
						String term = arr == null || arr.size() == 0 ? null : arr.get( 0 );
						// here: "... one AND two AND three" => "one+two+three"
						String sep = " ";
						if (uai < usz) u = utterance.get( uai );
						while (uai < usz && (term == null || !term.equals( u ))) {
							if ( Strings.contain( Reply.conjunctions(), u ) || u.equals( Attribute.VALUE_SEP )) {
								val += Attribute.VALUE_SEP;
								sep = "";
							} else {
								val += (sep + u );
								sep = " ";
							}
							uai++;
							if (uai < usz) u = utterance.get( uai );
					}	}
					
					// ...add this extracted a value
					//
					String newVal = Attribute.expandValues( // prevents X="x='val'"
							t.attribute( Tag.numeric ).equals( Tag.numeric ) ? t.attribute( Tag.phrase ).equals( Tag.phrase ) ? number.toString() : number.valueOf() :
								(t.name().equals("unit")) ? Plural.singular( val ) : val ).toString( Strings.SPACED );
					if (debugSwitch)
						audit.debug("new val ("+ t.attribute( Tag.numeric )+") is "+ number.toString());
					if (debugSwitch)
						audit.debug( "matched.set( "+ t.name() +"="+ val +" ); -- as: \""+ newVal+"\", "+ toString() );
					if (null == matched) matched = new Attributes();
					matched.add( new Attribute( t.name(), newVal )); // remember what it was matched with!
					
					//if (debugSwitch) audit.debug("checking postfix boilerplate:"+ t.postfix() );
					if (-1 == (uai = matchBoilerplate( t.postfixAsStrings(), utterance, uai ))) { // ...match postfix
						if (debugSwitch) audit.traceOut( "postfix mismatch:"+ t.postfix() );
						return null;
					}
					++tai;
				}
		}	} // fall through on tag&&!a || a&&!tag -- loop will terminate
		
		if (size() > tai ) {// if anything left we're not matched...
			if (debugSwitch) {
				String tmp = "";
				for (int tmpi=tai; tmpi < size(); tmpi++)
					tmp += (" " + get( tmpi ).toString()); 
				audit.traceOut( "not matched: tags left "+ size() +"/"+ tai +"("+ tmp +")" );
			}
			return null;
		}
		if (usz > uai) {// if anything left we're not matched...
			if (debugSwitch) {
				String tmp = "";
				for (int tmpi=uai; tmpi < usz; tmpi++)
					tmp += (" " + utterance.get( uai )); 
				audit.traceOut( "not matched: utterance left "+ usz +"/"+ uai +"("+ tmp +")" );
			}
			return null;
		}
		
		if (debugSwitch) audit.traceOut( "matched => "+ (matched==null ? "no values" : matched.toString()));
		return null == matched ? new Attributes() : matched;
	}
	public static void main(String args[]) {
		//Audit.turnOn();

		Tags ta = new Tags();
		Tag t = new Tag( "what is ", "X", "." )
				.attribute( Tag.numeric, Tag.numeric );
		ta.add( t );
		audit.audit("t="+t.toString());
		Attributes values = ta.matchValues( new Strings( "what is 1 + 2 ." ));
		audit.audit( "values => ["+ (values==null?"null":values.toString()) +"]" );
		
		t.attribute( Tag.phrase,  Tag.phrase  );
		audit.audit("t is now ="+t.toString());
		values = ta.matchValues( new Strings( "what is 1 + 2 ." ));
		audit.audit( "values => ["+ (values==null?"null":values.toString()) +"]" );
		
}	}