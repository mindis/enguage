package com.yagadi.enguage.util;

// todo: remove use of ArrayList??? or use in throughout??? or LinkedList?
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.TreeSet;

public class Strings extends ArrayList<String> implements Comparable<Strings> {
	
	public static final long serialVersionUID = 0;
	static private Audit audit = new Audit( "Strings" );
	
	public final static int     CSV = 0;
	public final static int   SQCSV = 1;
	public final static int   DQCSV = 2;
	public final static int  SPACED = 3;
	public final static int    PATH = 4;
	public final static int   LINES = 5;
	public final static int  CONCAT = 6;
	public final static int ABSPATH = 7;
	
	public final static String      lineTerm = "\n";
	public final static String           AND = "&&";
	public final static String            OR = "||";
	public final static String    PLUS_ABOUT = "+~";
	public final static String   MINUS_ABOUT = "-~";
	public final static String   PLUS_EQUALS = "+=";
	public final static String  MINUS_EQUALS = "-=";
	public final static String      ELLIPSIS = "...";
	public final static Strings ellipsis = new Strings( ELLIPSIS, '/' );
	
	private String[] tokens = {
			ELLIPSIS,    AND,  OR,
			PLUS_EQUALS, MINUS_EQUALS,
			PLUS_ABOUT,  MINUS_ABOUT };
	
	public Strings() { super(); }
	
	public Strings( Strings orig ) {
		super();
		if (null != orig)
			for (int i=0; i<orig.size(); i++)
				add( orig.get( i ));
	}
	public Strings( String[] sa ) {
		super();
		if (null != sa)
			for (int i=0; i<sa.length; i++)
				add( sa[ i ]);
	}
	public Strings( TreeSet<String> sa ) {
		super();
		Iterator<String> i = sa.iterator();
		while (i.hasNext())
			add( i.next());
	}
	public Strings( String buf ) {
		if (buf != null && !buf.equals( "" )) { // NB this doesn't tie up with parsing in Attributes.c!!!!
			int sz = buf.length();
			for (int i=0; i<sz; ) {
				while (i<sz && Character.isWhitespace( buf.charAt( i ))) i++;
				if (i<sz) {
					String word = "";
					if (Character.isLetter( buf.charAt( i ))
						|| (('_' == buf.charAt( i )	|| '$' == buf.charAt( i ))
								&& 1+i<sz && Character.isLetter( buf.charAt( 1+i ))))
					{	//audit.audit("reading AlphaNumeric including embedded apostropies");
						word += Character.toString( buf.charAt( i++ ));
						while (i<sz && (
							   Character.isLetter( buf.charAt( i ))
							|| Character.isDigit(  buf.charAt( i ))
							||	( '-'  == buf.charAt( i ))
							||	( '\'' == buf.charAt( i ))
							||	( '_'  == buf.charAt( i ))
						))
							word += Character.toString( buf.charAt( i++ ));
						
					} else if (Character.isDigit( buf.charAt( i ))
							 ||	(	i+1<sz
								 && Character.isDigit( buf.charAt( 1+i ))
								 && (	buf.charAt( i )=='-'   // -ve numbers
								 	 || buf.charAt( i )=='+')) // +ve numbers
							)
					{	//audit.audit("reading NUMBER");
						word += buf.charAt( i++ );
						boolean pointDone = false;
						while (i<sz
								&& (Character.isDigit( buf.charAt( i ))
								    || (  !pointDone
								    	&& buf.charAt( i )=='.')
								        && i+i<sz
								        && Character.isDigit( buf.charAt( 1+i ))))
						{	if (buf.charAt( i ) == '.') pointDone = true;
							word += Character.toString( buf.charAt( i++ ));
						}
						
					} else if ('\'' == buf.charAt( i ) ) {
						// embedded apostrophes: check "def'def", " 'def" or "...def'[ ,.?!]" 
						// quoted string with embedded apostrophes 'no don't'
						//audit.audit("SQ string");
						word += Character.toString( buf.charAt( i++ ));
						while( i<sz &&
						      !('\'' == buf.charAt( i ) && // ' must be followed by WS
						        (1+i==sz || Character.isWhitespace( buf.charAt( 1+i )))
						     ) )
							word += Character.toString( buf.charAt( i++ ));
						if (i<sz) word += Character.toString( buf.charAt( i++ ));
						
					} else if ('"' == buf.charAt( i ) ) {
						//audit.audit("DQ string");
						word += Character.toString( buf.charAt( i++ ));
						while( i<sz && '"' != buf.charAt( i ) )
							word += Character.toString( buf.charAt( i++ ));
						if (i<sz) word += Character.toString( buf.charAt( i++ ));
						
					} else {
						boolean found = false;
						//audit.audit("TOKEN");
						for (int ti=0; ti<tokens.length && !found; ti++)
							if (tokenMatch( tokens[ ti ],  buf,  i,  sz )) {
								found=true;
								word = tokens[ ti ];
								i += tokens[ ti ].length();
							}
						if (!found) word = Character.toString( buf.charAt( i++ ));
					}
					if (!word.equals( "" )) {
						//audit.audit(">>>>adding:"+word);
						add( word );
						word = "";
					}
		}	}	}
	}
	private static boolean tokenMatch( String token, String buf, int i, int sz ) {
		int tsz = token.length();
		return (i+tsz <= sz) && token.equals( buf.substring( i, i+tsz ));
	}
	public String toString( String fore, String mid, String aft ) {
		String as = "";
		int sz = size();
		if (sz > 0) {
			as = fore;
			for (int i=0; i<sz; i++)
				as += ((i == 0 ? "" : mid) + get( i ));
			as += aft;
		}
		return as;
	}
	public String toString( int n ) {
		return
			( n ==  SPACED ) ? toString(   "",      " ",   "" ) :
			( n ==  CONCAT ) ? toString(   "",       "",   "" ) :
			( n ==   DQCSV ) ? toString( "\"", "\", \"", "\"" ) :
			( n ==   SQCSV ) ? toString(  "'",   "', '",  "'" ) :
			( n ==     CSV ) ? toString(   "",      ",",   "" ) :
			( n ==    PATH ) ? toString(   "",      "/",   "" ) :
			( n ==   LINES ) ? toString(   "",     "\n",   "" ) :
			( n == ABSPATH ) ? toString(   "/",     "/",   "" ) :
			"Strings.toString( "+ toString( CSV ) +", n="+ n +"? )";
	}
	public String toString( Strings seps ) {
		if (size() == 0)
			return "";
		else if (null == seps)
			return toString( SPACED );
		else if (seps.size() == 1)
			return toString( "", seps.get( 0 ), "" );
		else if (seps.size() == 2) { // oxford comma: ", ", ", and "
			String tmp = get( 0 );
			for (int i=1, sz=size(); i<sz; i++)
				tmp += ((i+1==sz)?seps.get( 1 ):seps.get( 0 )) + get( i );
			return tmp;
		} else if (seps.size() == 4) {
			Strings tmp = new Strings();
			tmp.add( seps.get( 1 ));
			tmp.add( seps.get( 2 ));
			return seps.get( 0 ) + toString( tmp ) + seps.get( 3 );
		} else 
			return toString( seps.get( 0 ), seps.get( 1 ), seps.get( 2 ));
	} // don't use traceOutStrings here -- it calls Strings.toString()!
	/* --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- 
	static private String[] fromString( String buf ) {
		Strings a = new Strings( buf );
		return Strings.fromArrayList( a );
	}*/
	public Strings( String buf, char sep ) {
		if (null != buf) {
			int sz = buf.length();
			if (0 < sz) {
				int cp = 0;
				String word = null;
				while( cp<sz ) {
					word="";
					while( cp<sz && (sep != buf.charAt(cp)))
						word += Character.toString( buf.charAt( cp++ )); // *cp++ = *buf++;
					add( new String( word ));
					if ( cp<sz && sep == buf.charAt(cp) ) { // not finished
						cp++;         // avoid separator
						if (cp>=sz) // now finished!
							add( new String( "" )); // add trailing blank string!
	}	}	}	}	}
	public Strings filter() {
		// remove any [ superfluous ] stuff
		Strings filtered = new Strings();
		ListIterator<String> li = listIterator();
		while (li.hasNext()) {
			String item=li.next();
			if (item.equals( "[" )) // skip to closing ]
				while (li.hasNext() && !item.equals( "]" ))
					item=li.next();
			else
				filtered.add( item );
		}
		return filtered;
	}
	public Strings removeAll( String val ) {
		Iterator<String> ai = iterator();
		while (ai.hasNext()) {
			if (ai.next().equals( val ))
				ai.remove();
		}
		return this;
	}
	// EITHER:
	// (a=[ "One Two Three", "Ay Bee Cee", "Alpha Beta" ], val= "Bee") => "Ay Bee Cee";
	//static public String getContext( String[] a, String val ) {
	//	return "incomplete";
	//}
	// OR: (outer=[ "a", "strong", "beer" ], inner=[ "strong", "beer" ]) => true
/*	static public boolean xcontainsStrings( String[] outer, String[] inner ) {
		if (outer.length == 0 && 0 == inner.length)
			return true;
		else if (outer.length >= inner.length)
			for (int o=0; o<=outer.length-inner.length; o++)
				for (int i=0; i<inner.length; i++)
					if (outer[ o + i ].equals( inner[ i ])) return true;
		return false;
	} // */
	public boolean containsMatched( Strings inner ) {
		boolean rc = false;
		if (size() == 0 && 0 == inner.size())
			return true;
		else if (size() >= inner.size())
			// this loop goes thru outer in a chunk size of inner
			for (int o=0; rc == false && o<=size()-inner.size(); o++) {
				// see if the inner chunk matches from posn o
				rc = true; // lets assume it does
				for (int i=0; rc == true && i<inner.size(); i++)
					if (!get( o + i ).equals( inner.get( i ))) // if one doesn't match
						rc = false;
			}
		return rc;
	}
	// ...OR: -------------------------------------------
	// a=[ "One Two Three", "Aye Bee Cee", "Alpha Beta" ], val= "Bee" => b = [ "One Two Three", "Alpha Beta" ];
	public Strings removeAllMatched( String val ) {
		Strings b = new Strings();
		Strings valItems = new Strings( val );
		for (int ai=0; ai<size(); ai++) 
			if (!new Strings( get( ai )).containsMatched( valItems ))
				b.add( get( ai ));
		return b;
	}
	// ---------------------------------------------
	public Strings removeFirst( String val ) {
		Iterator<String> si = iterator();
		while (si.hasNext())
			if (si.next().equals( val )) {
				si.remove();
				break;
			}
		return this;
	}
	public String remove( int i ) {
		String str = "";
		if (i>= 0 && i<size()) {
			str = get( i );
			super.remove( i );
		} else
			audit.ERROR("trying to remove "+ i +(i%10==1&&i!=11?"st":i%10==2&&i!=12?"nd":i%10==3&&i!=13?"rd":"th")+ " element");
		return str;
	}
	public Strings contract( String item ) {
		int sz=size()-1;
		for( int i=1; i<sz; i++ )
			if (get( i ).equals( item )) {
				set( i-1, get( i-1 )+ item +remove( i+1 ) );
				remove( i );
				sz -= 2;
			}
		return this;
	}
	public Strings replace( int i, String s ) {
		remove( i );
		add( i, s );
		return this;
	}
	public Strings append( Strings sa ) {
		addAll( sa );
		return this;
	}
	//
	public Strings prepend( String str ) {
		add( 0, str );
		return this;
	}
	public Strings copyFrom( int n ) {
		Strings b = new Strings();
		for (int i=n, sz = size(); i<sz; i++)
			b.add( get( i ));
		return b;
	}
	public Strings copyAfter( int n ) {
		Strings b = new Strings();
		for (int i=n+1, sz = size(); i<sz; i++)
			b.add( get( i ));
		return b;
	}
	public Strings copyFromUntil( int n, String until ) {
		Strings b = new Strings();
		for (int i=n, sz = size(); i<sz; i++) {
			String item = get( i );
			if (item.equals( until ))
				break;
			else
				b.add( item );
		}
		return b;
	}
	static public Strings fromNonWS( String buf ) {
		Strings a = new Strings();
		if (buf != null) {
			StringBuffer word = null;
			for (int i=0, sz=buf.length(); i<sz; i++ ) {
				word = new StringBuffer();
				while( i<sz &&  Character.isWhitespace( buf.charAt( i ))) i++;
				while( i<sz && !Character.isWhitespace( buf.charAt( i ))) { word.append( buf.charAt( i )); i++; }
				
				if (null != word)
					a.add( word.toString());
		}	}
		return a;
	}
	public Strings reverse() {
		Strings b = new Strings();
		for (int sz=size(), i=sz-1; i>=0; i--)
			b.add( get( i ));
		return b;
	}
	// replace( [ "hello", "martin", "!" ], [ "martin" ], [ "to", "you" ]) => [ "hello", "to", "you", "!" ]
	public Strings replace( Strings b, Strings c ) {
		//audit.traceIn("replace", b.toString() +" with "+ c.toString() +" in "+ toString());
		//boolean lookingForElipsis = false;
		//if (c.size() == 1 && c.get( 0 ).equals( ELLIPSIS )) lookingForElipsis= true;
		int len = size(), blen = b.size(), clen = c.size();
		for (int i=0; i <= len - blen; i++) {
			boolean found = true;
			int j=0;
			for (j=0; j<blen && found; j++)
				if (!get( i+j ).equalsIgnoreCase( b.get( j ))) found=false;
			if (found) {
				//if (lookingForElipsis) audit.ERROR(">>>>>>>>>>>>>>>>>>Strings.replace(): looking for and found "+ ELLIPSIS);
				for (j=0; j<blen; j++) remove( i );
				for (j=0; j<clen; j++) add( i+j, c.get( j ));
				len = size(); // ...since we've messed with a
			}
		}
		//audit.traceOut( toString());
		return this;
	}
	static public boolean contain( Strings a, String s ) { return -1 != a.indexOf( s ); }
	public boolean contain( String s ) { return -1 != indexOf( s ); }
	
	// deals with matched and unmatched values:
	// [ "a", "$matched", ".",  "b" ] => [ "a", "martin", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
	// [ "a",  "MATCHED", ".",  "b" ] => [ "a", "martin", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
	// [ "a", "$unmatch", ".",  "b" ] => [ "a", "_USER", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
	// [ "we are holding hands", "."  ] => [ "we", "are", "holding", "hands", "." ] -- jik - just in case!
	// matches are from tags, *ap contains mixed case - any UPPERCASE items should match matches OR envvars.
	// [ 'some', 'bread', '+', 'fish'n'chips', '+', 'some', 'milk' ], "+"
	//                                => [  'some bread', 'fish and chips', 'some milk' ]
	private Strings normalise( String ipSep, String opSep ) {
		//audit.traceIn( "normalise", "ipSep='"+ ipSep +"' opSep='"+ opSep +"'");
		// remember, if sep='+', 'fish', '+', 'chips' => 'fish+chips' (i.e. NOT 'fish + chips')
		// here: some coffee + fish + chips => some coffee + fish and chips
		Strings values = new Strings();
		if (size() > 0) {
			int i = 0;
			String value = get( 0 ); //
			//audit.audit(":gotVal:"+ value +":");
			String localSep = opSep; // ""; // only use op sep on appending subsequent strings
			while (++i < size()) {
				String tmp = get( i );
				//audit.audit(":gotTmp:"+ tmp +":");
				if (tmp.equals( ipSep )) {
					//audit.audit("normalise():adding:"+ value +":");
					values.add( value );
					value = "";
					localSep = "";
				} else {
					value += ( localSep + tmp );
					localSep = opSep;
					//audit.audit(":valNow:"+ value +":");
			}	}
			//audit.audit("normalise():adding:"+ value +":");
			values.add( value );
		}
		//return audit.traceOut( values );
		return values;
	}
	/*
	 * normalise with a parameter uses that param as a user defined separator, rather than whitespace
	 * normalise([ "one", "two", "+", "three four" ], "+") => [ "one two", "three four" ]
	 */
	public Strings normalise( String sep ) {
		return normalise( sep, " " );
	}
	// normalise([ "one", "two three" ]) => [ "one", "two", "three" ]
	public Strings normalise() {
		Strings a = new Strings();
		for (String s1 : this )
			for ( String s2: new Strings( s1 ))
				a.add( s2 );
		return a;
	}
	
	// TODO: expand input, and apply each thought...
	// I need to go to the gym and the jewellers =>
	// (I need to go to the gym and I need to go to the jewellers =>)
	// I need to go to the gym. I need to go to the jewellers.
	/* [ [ "to", "go", "to", "the", "gym" ], [ "the", "jewellers" ] ] 
	 * => [ [ "to", "go", "to", "the", "gym" ], [ "to", "go", "to", "the", "jewellers" ] ]
	 */
	// [ "THIS", "is", "Martin" ] => [ "THIS", "is", "martin" ]
	public Strings decap() {
		int i = -1;
		//remove all capitalisation... we can re-capitalise on output.
		while (size() > ++i) {
			String tmp = get( i );
			if (isCapitalised( tmp ))
				set( i, Character.toLowerCase( tmp.charAt( 0 )) + tmp.substring( 1 ));
		}
		return this;
	}
	private static boolean isCapitalised( String str ) {
		if (null == str) return false;
		int len = str.length();
		if (Character.isUpperCase( str.charAt( 0 )) && len > 1) {
			int i = 0;
			while (len > ++i && Character.isLowerCase( str.charAt( i )))
				;
			return str.length() == i; // capitalised if we're at the end of the string
		}
		return false;
	}
	public static boolean isUpperCase( String a ) {
		for (int i=0; i<a.length(); i++)
			if (!Character.isUpperCase( a.charAt( i )) )
				return false;
		return true;
	}
	public static boolean isUpperCaseWithHyphens( String a ) {
		for (int i=0; i<a.length(); i++)
			if (!Character.isUpperCase( a.charAt( i )) && a.charAt( i ) != '-' )
				return false;
		return true;
	}
	public Strings trimAll( char ch ) {
		int i=0;
		for( String s : this )
			set( i++, trim( s, ch ));
		return this;
	}
	static public String trim( String a, char ch ) { return triml( a, a.length(), ch ); }
	static public String triml( String a, int asz, char ch ) {
		// (a="\"hello\"", ch='"') => "hello"; ( "ohio", 'o' ) => "hi"
		char ch0 = a.charAt( 0 );
		if (asz == 2 && ch0 == ch && a.charAt( 1 ) == ch)
			return "";
		else if (asz > 2 && ch0 == ch && a.charAt( asz-1 ) == ch)
			return a.substring( 1, asz-1 );
		else
			return a;
	}
	static public String stripQuotes( String s ) {
		int sz = s.length();
		if (sz>1) {
			char ch = s.charAt( 0 );
			     if (ch == '\'') s = Strings.triml( s, sz, '\'' );
			else if (ch ==  '"') s = Strings.triml( s, sz,  '"' );
		}
		return s; 
	}
	
	// ---------------------------------------------------------
	// ---------------------------------------------------------
	/* 
	 * combine and divide --
	 * if a single separator, don't need to store that separator, combine just adds it
	 * if a combination of separators, we need to remember which one it is so it can be added!
	 * 
	 */
	public ArrayList<Strings> divide( Strings terminators ) {
		// [ "o", "t", ".", "t", "?", "f", "f" ]( ".?!" ) => [["o", "t", "."], ["t", "?"], ["f", "f"]]
		ArrayList<Strings> divisions = new ArrayList<Strings>();
		Strings division = new Strings();
		for (String s : this) {
			division.add( s );
			if (terminators.contains( s )) {
				divisions.add( division );
				division = new Strings();
		}	}
		divisions.add( division );
		return divisions;
	}
	static Strings combine( ArrayList<Strings> as ) {
		// [["o", "t". "."], ["t", "?"], ["f", "f"]] => [ "o", "t", ".", "t", "?", "f", "f" ]
		Strings sa = new Strings();
		for (Strings tmp : as)
			sa.addAll( tmp );
		return sa;
	}
	// ---------------------------------------------------------
	public ArrayList<Strings> divide( String sep ) {
		// [ "o", "t", "&", "t", "?", "&", "f", "f" ]( "&" ) => [["o", "t", "."], ["t", "?"], ["f", "f"]]
		ArrayList<Strings> divisions = new ArrayList<Strings>();
		Strings division = new Strings();
		for (String s : this) {
			if (sep.equals( s )) {
				divisions.add( division );
				division = new Strings();
			} else {
				division.add( s );
		}	}
		divisions.add( division );
		return divisions;
	}
	static Strings combine( ArrayList<Strings> as, String sep ) {
		// [["o", "t"], ["t", "?"], ["f", "f"]] => [ "o", "t", "&", "t", "?", "&", "f", "f" ]
		Strings sa = new Strings();
		boolean first = true;
		for (Strings tmp : as) {
			if (first)
				first = false;
			else
				sa.add( sep );
			sa.addAll( tmp );
		}
		return sa;
	}
	
	public int compareTo( Strings sa ) {
		/* This compareTo() will put the longer strings first so:
		 * "user", "does", "not"  matches before  "user", "does"
		 */
		int rc = 0;
		Iterator<String> i = iterator(),
		               sai = sa.iterator();
		while (rc==0 && i.hasNext() && sai.hasNext())
			rc = sai.next().compareTo( i.next() );
		
		if (rc==0 && (i.hasNext() || sai.hasNext()))
			rc = i.hasNext() ? -1 : 1 ;
			
		return rc;
	}
	
	// ---------------------------------------------------------
	
	public static void main( String args[]) {
		Audit.turnOn(); //main()
		audit.audit( "hello, world" );
		
		Strings a = new Strings( "hello there" ),
				b = new Strings( "hello world" ),
		        c = new Strings( "hello there martin" );
		
		audit.audit( "comparing "+ a +" to "+ b +" = "+ (a.compareTo( b ) > 0 ? "pass" : "fail" ));
		audit.audit( "comparing "+ a +" to "+ c +" = "+ (a.compareTo( c ) > 0 ? "pass" : "fail" ));
		
		
		audit.audit( "a: "+ new Strings( "failure won't 'do' 'don't'" ));
		audit.audit( "b: "+ new Strings( "..........." ));
		audit.audit( "c: "+ new Strings( "+2.0" ));
		audit.audit( "d: "+ new Strings( "quantity+=2.0" ));
		
		a = new Strings("hello failure");
		b = new Strings( "failure" );
		c = new Strings( "world" );
		audit.audit( "e: ["+ a.replace( b, c ).toString( "'", "', '", "'" ) +"]" );
		String tmp = "+=6";
		audit.audit( "tmp: "+ tmp.substring( 0, 1 ) + tmp.substring( 2 ));
	
		audit.audit("tma:"+(tokenMatch( ELLIPSIS, ELLIPSIS, 0, ELLIPSIS.length() )?"true":"false")+"=>true");
		audit.audit("tma:"+(tokenMatch( ELLIPSIS, ELLIPSIS, 1, ELLIPSIS.length() )?"true":"false")+"=>false");
		audit.audit("tma:"+(tokenMatch( ELLIPSIS,     "..", 0,     "..".length() )?"true":"false")+"=>false");
		
		a = new Strings( "this is a test sentence. And half a" );
		ArrayList<Strings> as = a.divide( Shell.terminators() );
		// as should be of length 2...
		b = as.remove( 0 );
		audit.audit( "b is '"+ b.toString() +"'. as is len "+ as.size() );
		a = Strings.combine( as ); // needs blank last item to add terminating "."
		audit.audit( "a is '"+ a.toString() +"'. a is len "+ a.size() );
		a.addAll( b );
		audit.audit( "a is now '"+ a.toString() +"'." );
// */
		/* /
		String s = "this test should pass";
		Strings sa1 = new Strings( s, ' ' );
		//Strings sa2 = new Strings( s, " " );
		//String[] sa3 = Strings.fromLines( "this\ntest\nshould\npass" );
		//audit.audit( "equals test "+ (sa1.equals( sa2 ) ? "passes" : "fails" ));
		audit.audit( "===> ["+ sa1.toString( "'", "', '", "'" ) +"] <===" );
		//audit.audit( "===> ["+ sa2.toString( Strings.SQCSV ) +"] <===" );
	
		
		//static public String[] removeAt( String[] a, int n ) ;
		//static public String[] removeAll( String[] a, String val ) ;
		// EITHER:
		//String[] a = new String[] {"One Two Three", "this test passes", "Alpha Beta" };
		//String  val= "passes";
		//audit.audit( "getContext test: "+ getContext( a, val ));
		
		Strings outer = new Strings( "a strong beer" );
		Strings inner = new Strings( "strong beer" );
		audit.audit( "containsStrings test "+ (outer.containsMatched( inner ) ? "passes" : "fails" ));
		// ...OR: -------------------------------------------
		// a=[ "One Two Three", "Aye Bee Cee", "Alpha Beta" ], val= "Bee" => b = [ "One Two Three", "Alpha Beta" ];
		//static public String[] removeAllMatched( String[] a, String val ) ;
		// ---------------------------------------------
		//static public String[] removeFirst( String[] a, String val ) ;
		//audit.audit( toString( removeFirst( Strings.fromString( "this test passes" ), "test" ), SPACED ));
		//static public String[] append( String[] a, String str ) ;
		//audit.audit( toString( append( Strings.fromString( "this test " ), "passes" ), SPACED ));
		//static public String[] append( String[] a, String sa[] ) ;
		//audit.audit( toString( append( fromString( "this test " ), fromString( "passes" )), SPACED ));
		//static public String[] prepend( String[] a, String str ) ;
		//audit.audit( toString( prepend( fromString( "test passes" ), "this" ), Strings.SPACED ));
		//static public String[] copyAfter( String[] a, int n ) ;
		//audit.audit( toString( copyAfter( fromString( "error this test passes" ), 0 ), SPACED ));
		//static public String[] copyFromUntil( String[] a, int n, String until ) ;
		//Strings xxx = new Strings( "error this test passes error" );
		//audit.audit( toString( xxx.copyFromUntil( 1, "passes" ), SPACED ));
		//static public String[] fromNonWS( String buf ) ;
	/*	audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//static public String[] insertAt( String[] a, int pos, String str ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//static public String[] reverse( String[] a ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		// replace( [ "hello", "martin", "!" ], [ "martin" ], [ "to", "you" ]) => [ "hello", "to", "you", "!" ]
		//static public String[] replace( String[] a, String[] b, String[] c ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//static public int indexOf( String[] a, String s ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//static public boolean contain( String[] a, String s ) ; return -1 != indexOf( a, s ); }
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		
		// deals with matched and unmatched values:
		// [ "a", "$matched", ".",  "b" ] => [ "a", "martin", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
		// [ "a",  "MATCHED", ".",  "b" ] => [ "a", "martin", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
		// [ "a", "$unmatch", ".",  "b" ] => [ "a", "_USER", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
		// [ "we are holding hands", "."  ] => [ "we", "are", "holding", "hands", "." ] -- jik - just in case!
		// matches are from tags, *ap contains mixed case - any UPPERCASE items should match matches OR envvars.
		//static public String[][] split( String[] a, String[] terminators ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));

		// [ 'some', 'bread', '+', 'fish'n'chips', '+', 'some', 'milk' ], "+"  => [  'some bread', 'fish and chips', 'some milk' ]
		//static public String[] rejig( String[] a, String ipSep, String opSep ) ;
		//static public String[] rejig( String[] a, String sep ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		// todo: remove rejig, above? Or, combine with expand() and normalise()/
		// NO: Need Stringses to Strings & vv [ "some", "beer", "+", "some crisps" ] => "some beer", "some crisps" ]
		// [ "some beer", "some crisps" ] => [ "some", "beer", "+", "some", "crisps" ]
		// todo: expand input, and apply each thought...
		// I need to go to the gym and the jewellers =>
		// (I need to go to the gym and I need to go to the jewellers =>)
		// I need to go to the gym. I need to go to the jewellers.
		//static public String[][] expand( String[][] a ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));

		// [ "one", "two three" ] => [ "one", "two", "three" ]
		// todo: a bit like re-jig aove???
		//static public String[] normalise( String[] sa ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//static public boolean isUpperCase( String a ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//static public boolean isUpperCaseWithHyphens( String a ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		 *
		 */
		// [ "THIS", "is Martin" ] => [ "THIS", "is", "martin" ]
		//audit.audit( Strings.toString( decap( Strings.fromString( "THIS is Martin" )), Strings.DQCSV ) +" should equal [ \"THIS\", \"is\", \"martin\" ]" );
		//audit.audit( trim( "\"hello\"", '"' ) +" there == "+ trim( "ohio", 'o' ) +" there! Ok?" );
		
		//audit.audit( Strings.toString( Strings.fromString( "failure won't 'do' 'don't'" ), Strings.DQCSV ));
		//audit.audit( Strings.toString( Strings.insertAt( Strings.fromString( "is the greatest" ), -1, "martin" ), Strings.SPACED ));
		//audit.audit( "" );
}	}