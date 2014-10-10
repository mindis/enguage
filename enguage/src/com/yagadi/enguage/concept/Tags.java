package com.yagadi.enguage.concept;

import java.util.Locale;

import com.yagadi.enguage.expression.Language;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.List;
import com.yagadi.enguage.util.Attribute;
import com.yagadi.enguage.util.Attributes;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.util.Audit;

public class Tags {
	static Audit audit = new Audit( "Tags" );
	
	protected Tag[] ta = new Tag[ 0 ];
	public    Tag[] ta() { return ta; }
	
	public int   length() { return ta.length; }
	public void  append( Tag t ) {
		Tag[] b = new Tag[ ta.length+1 ];
		if (ta != null)
			for(int i=0, sz=ta.length; i<sz; i++)
				b[ i ] = ta[ i ];
		b[ b.length - 1 ] = t;
		ta = b;
	}
	
	// with postfix boilerplate:
	// typically { [ ">>>", "name1" ], [ "/", "name2" ], [ "/", "name3" ], [ "<<<", "" ] }.
	// could be  { [ ">>>", "name1", "" ], [ "/", "name2", "" ], [ "/", "name3", "<<<" ] }.
	public String toString() {
		String str="";
		for (int i = 0; i<ta.length; ++i) str += ta[ i ].toString();
		return str;
	}
	public String toText() {
		String str="";
		for (int i = 0; i<ta.length; ++i) str += ta[ i ].toText();
		return str;
	}
	public String toLine() {
		String str="";
		for (int i = 0; i<ta.length; ++i)
			str += ( " "+ta[ i ].prefix()+" <"+ta[ i ].name()+"> "+ta[ i ].postfix());
		return str;
	}

	public Tags() {}
	
	// "if X do Y" -> [ <x pref="if "/>, <y pref="do "/>, <pref="."> ]
	public Tags( String str ) {
		String[] words = Strings.fromString( str ); // correct? ...FromChars? ...NonWS?
		int sz = words.length;
		// add period if description is not terminated -- see also Cmd.c
		if (!words[ sz - 1 ].equals( "." ) && !words[ sz - 1 ].equals( "!" ) && !words[ sz - 1 ].equals( "?" ))
			words = Strings.append( words, "." );
			
		String prefix = "";
		Tag t = new Tag();
		int i = -1;
		while (words.length > ++i ) {
			//System.out.println( "  Processing word " + "\n", words[ i ]);
			if ((1 == words[ i ].length()) && Strings.isUpperCase( words[ i ]) && !words[ i ].equals("I")) {
				t.name( words[ i ].toLowerCase( Locale.getDefault())).prefix( prefix );
				append( t );
				t = new Tag();
				prefix = new String(" ");
			} else if (Strings.isUpperCaseWithHyphens( words[ i ]) && !words[ i ].equals( "I")) {
				String[] arr = Strings.fromString( words[ i ], '-' );
				int j = -1;
				if (null != arr) while (arr.length > ++j) {
					arr[ j ] = arr[ j ].toLowerCase( Locale.getDefault());
					if (arr.length > j+1 )
						t.attribute( arr[ j ], arr[ j ]);
					else
						t.name( arr[ j ]);
				}
				t.prefix( prefix );
				append( t );
				t = new Tag();
				prefix = " ";
			} else
				prefix += (words[ i ] + " ");
		}
		t.prefix( prefix );
		append( t );
	}
/*	private static int matchBoilerplate( String s, String[] data, int di ) {
		audit.traceIn( "matchBoilerplate", "pref='"+ s +"', from '"+ data[ di ]);
		String[] pattern = Strings.fromString( s );
		if ( pattern.length > 0 ) {
			int n = 0;
			String[] b = Strings.copyAfter( data, di - 1);
			if (pattern != null && b != null)
				while (n<pattern.length && n<b.length && Language.wordsEqualIgnoreCase( pattern[ n ], b[ n ])) n++ ;
			di = n == pattern.length ? di+n : -1; 
		}
		return audit.traceOut( di );
	}*/
	private static int matchBoilerplate( String[] sa, String[] data, int di ) {
		/*audit.traceIn( "matchBoilerplate", 
				"match '"+ Strings.toString( sa, Strings.SPACED )
				+"', with '"+ Strings.toString( data, Strings.SPACED )
				+"', from o/s "+ di );
		 */
		if ( null != sa && sa.length > 0 && di < data.length ) {
			int n = 0;
//			String[] b = Strings.copyAfter( data, di - 1);
			int bi = di; // added
//			if (sa != null && b != null)
//				while (n<sa.length && n<b.length && Language.wordsEqualIgnoreCase( sa[ n ], b[ n ] )) n++ ;
			if (sa != null) //added
				while (n<sa.length && bi+n<data.length && Language.wordsEqualIgnoreCase( sa[ n ], data[ bi+n] )) n++ ; //added
			di = n == sa.length ? di+n : -1;
		}
		//return audit.traceOut( di );
		return di;
	}
	
	// used in interpreting ML tags and their (string) prefixes: "some text prefix <tag/>" <=> "some",...
	// "matches" === "case insensitive"
	// this deals with QUOTED-, PLURAL- and ABSTRACT- which, i think, are mutually exclusive.
	// this all assumes there are not two hotspots together...
	public Attributes matchValues( String[] a ) {
		// check for early termination...
		if (null == ta || ta.length == 0 || null == a || a.length == 0 ||
		! // <-- negate all the below!!!
		// all this is to see if they match, negated above
		// if prefix is just whitespace, ie. " " then they may still match, so say they do!
			(   //this is to show if they "could" match
				ta[0].prefix() == null || ta[0].prefix().equals(" ") || ta[0].prefix().equals("") // there is no prefix
			 ||
				(a[ 0 ].length() <= ta[0].prefix().length() &&                                    //  prefix<X/> &&
		    	 a[ 0 ].equalsIgnoreCase( ta[0].prefix().substring(0,a[0].length())) )            // "prefixtoken"
		     ||
				((" "+a[ 0 ]).length() <= ta[0].prefix().length() &&                              //
				 (" "+a[ 0 ]).equalsIgnoreCase( ta[0].prefix().substring(0,a[0].length()+1 )) )   // 
			))
		{
			//audit.debug( "matchValues() -- early termintation! "+ t[ 0 ].toString() );
			return null; // -- */
		}
		
		//audit.traceIn( "matchValues", "t='"+ toLine() +"', a =>'"+Strings.toString(a, Strings.SPACED) +"'" );
		Attributes matched = null; //new Attributes();
		int ti = 0, ai = 0; // 'cos we've got 2 indexes t[++ti] would fail before a[++ai] is evaluated
		while ( ti<ta.length && ai<a.length) { // step thru' [..., "pref"+<pattern/>, ...] && [..., "pref", "value", ...] together
			if (-1 == (ai = matchBoilerplate( ta[ ti ].prefixAsStrings(), a, ai ))) { // ...match prefix
				//audit.traceOut("prefix mismatch:"+ (ti >= ta.length ? "LENGTH" : null == ta[ ti ] ? "NULL" : ta[ ti ].prefix()));
				return null;
			} else if (ai>=a.length && ta[ ti ].name().equals( "" )) { // end of array on null (end?) tag...
				ti++; // ...don't move ai on: !ai & we're finished, !NAME(ti) & check ai with next tag
			} else if (ai<a.length && /* !a[ ai ].equals( "" ) &&*/ !ta[ ti ].name().equals( "" )) { // do these names match?
				// Ok, now validate this text...
				boolean
					textIsQuoted = Language.isQuoted( a[ ai ]) ||
						(a[ ai ].length() >= 7 && a[ ai ].substring( 0, 6 ).equals( "QUOTED-" )),
					textIsPlural = Language.isPlural( a[ ai ]) ||
						(a[ ai ].length() >= 7 && a[ ai ].substring( 0, 6 ).equals( "PLURAL-" )); //explicit
				if (!ta[ ti ].validQuote( textIsQuoted )) {
					//audit.traceOut("quote mismatch:"+ ta[ ti ].name()+":"+textIsQuoted+":"+ (a.length>ai?a[ ai ]:"--end--"));
					return null;
				} else if (!ta[ ti ].validPlural( textIsPlural )) {
					//audit.traceOut("plural mismatch:"+ ta[ ti ].name()+":"+textIsPlural+":"+ (a.length>ai?a[ ai ]:"--end--"));
					return null;
				} else if (ta[ ti ].attribute( "abstract" ).equals( "abstract" ) &&
				            a[ ai ].equalsIgnoreCase( ta[ ti ].name()) )
				{ // don't match ABSTRACT tags if names are the same!
					//audit.traceOut("abstract mismatch:"+ ta[ ti ].name()+":"+ (a.length>ai?a[ ai ]:"--end--"));
					return null;
				} else {
					// ...OK so its valid, now extract a value...
					/* We need to be able to extract:
					 * NAME="value"				... <NAME/>
					 * NAME="some value"		... <NAME phrased="phrased"/>
					 * NAME="an/array/or/list"	... <NAME  array="array"/>
					 * NAME="value one/value two/value three" <NAME phrased="phrased" array="array"/>
					 */
					String val = a[ ai++ ];
					if (ta[ ti ].phrased() || (ai<a.length && Strings.contain( Reply.conjunctions(), a[ ai ] ))) {
						// next prefix as array is...
						String[] arr = (ta.length > ti+1) ? ta[ ti+1 ].prefixAsStrings() : null;
						// ...first token of which is the terminator
						String term = arr == null || arr.length == 0 ? null : arr[ 0 ];
						// here: "... one AND two AND three" => "one+two+three"
						String sep = " ";
						while (ai < a.length && (term == null || !term.equals( a[ ai ] ))) {
							if ( Strings.contain( Reply.conjunctions(), a[ ai ]) || a[ ai ].equals( List. sep() )) {
								val += List.sep();
								sep = "";
							} else {
								val += (sep + a[ ai ]);
								sep = " ";
							}
							ai++;
					}	}
					
					// ...add this extracted a value
					//
					//audit.debug( "matched.set( "+ ta[ ti ].name() +"="+ val +" );");
					if (null == matched) matched = new Attributes();
					matched.add( new Attribute( ta[ ti ].name(), val )); // remember what it was matched with!
					
					if (-1 == (ai = matchBoilerplate( ta[ ti ].postfixAsStrings(), a, ai ))) { // ...match postfix
						//audit.traceOut( "postfix mismatch:"+ ta[ ti ].postfix());
						return null;
					}
					++ti;
				}
		}	} // fall through on t&&!a || a&&!t -- loop will terminate
		
		if (ta.length > ti || a.length > ai) {// if anything left we're not matched...
			//audit.traceOut( "not matched: something left" );
			return null;
		}
		
		//audit.traceOut( "matched => "+ (matched==null ? "no values" : matched.toString( " " )));
		return null == matched ? new Attributes() : matched;
	}
	public static void main(String args[]) {
		//audit.traceOut( "hello there");
/*		String[] sa = {"is", "X", "'",  "s",  "M",  "V",  "?"};
		Tag[] ta = null;
		Tag t = new Tag( " is ", "x" );
		ta = Tags.append( ta, t );
		t = new Tag( " ' s ", "M" );
		ta = Tags.append( ta, t );
		t = new Tag( " ", "V" );
		ta = Tags.append( ta, t );
		t = new Tag( " ? ", "" );
		ta = Tags.append( ta, t );
		
		System.out.println( "Matching ["+ Strings.toString( sa, Strings.CSV ) +"] with >"+ Tags.toString( ta )+"<");
		Attributes a = Tags.matchValues( ta, sa ); 
		if (a != null)
			System.out.println( "matched: "+ a.toString(" "));
		else
			System.out.println( "NOT matched" );*/
}	}