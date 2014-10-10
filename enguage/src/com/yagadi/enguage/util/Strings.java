package com.yagadi.enguage.util;

// TODO: remove use of ArrayList??? or use in throughout??? or LinkedList?
import java.util.ArrayList;

public class Strings /*extends ArrayList<String>???*/ {
	public final static int     CSV = 0;
	public final static int   SQCSV = 1;
	public final static int   DQCSV = 2;
	public final static int  SPACED = 3;
	public final static int    PATH = 4;
	public final static int   LINES = 5;
	public final static int  CONCAT = 6;
	public final static int ABSPATH = 7;
	public final static String dotDotDot[] = { ".", ".", "." };
	public final static String ELLIPSIS    =     "...";
	public final static String ellipsis[]  = {  ELLIPSIS     };

	static public String toString( String a[], String fore, String mid, String aft) {
		String as = "NULL";
		if (null != a) {
			as = "";
			if (a.length > 0) {
				as = fore;
				for (int i=0; i < a.length; i++)
					as += ((i == 0 ? "" : mid) + a[ i ]);
				as += aft;
		}	}
		return as;
	}
	static public String toString( ArrayList<String> a, String fore, String mid, String aft) {
		return toString( Strings.fromArrayList( a ), fore, mid, aft );
	}
	static public String toString( String a[], int n ) {
		return
			( n ==  SPACED ) ? toString( a,   "",      " ",   "" ) :
			( n ==  CONCAT ) ? toString( a,   "",       "",   "" ) :
			( n ==   DQCSV ) ? toString( a, "\"", "\", \"", "\"" ) :
			( n ==   SQCSV ) ? toString( a,  "'",   "', '",  "'" ) :
			( n ==     CSV ) ? toString( a,   "",     ", ",   "" ) :
			( n ==    PATH ) ? toString( a,   "",      "/",   "" ) :
			( n ==   LINES ) ? toString( a,   "",     "\n",   "" ) :
			( n == ABSPATH ) ? toString( a,   "/",     "/",   "" ) :
			"Strings.toString( "+ Strings.toString( a, CSV ) +", n="+ n +"? )";
	}
	static public String toString( ArrayList<String> a, int n){
		return toString( Strings.fromArrayList( a ), n );
	}
	static public String toString( String[] a, String[] seps ) {
		if (null == a || a.length == 0)
			return "";
		else if (null == seps)
			return toString( a, SPACED );
		else if (seps.length == 1)
			return toString( a, "", seps[0], "" );
		else if (seps.length == 2) { // oxford comma: ", ", ", and "
			String tmp = a[ 0 ];
			for (int i=1, sz=a.length; i<sz; i++)
				tmp += ((i+1==sz)?seps[ 1 ]:seps[ 0 ]) + a[ i ];
			return tmp;
		} else if (seps.length == 4) {
			String[] tmp = { seps[ 1 ], seps[ 2 ]};
			return seps[ 0 ] + Strings.toString( a, tmp ) + seps[ 3 ];
		} else 
			return toString( a, seps[ 0 ], seps[ 1 ], seps[ 2 ]);
	} // don't use traceOutStrings here -- it calls Strings.toString()!
	// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- 
	static public String[] fromArrayList( ArrayList<String> als ) {
		// There is probably a util to do this...!
		String rc [] = new String [als==null?0:als.size()];
		if (null != als) for (int i=0; i<als.size(); i++)
			rc[ i ] = als.get( i );
		return rc;
	}
	static public String[] fromString( String buf ) {
		ArrayList<String> a = Strings.arrayListFromString( buf );
		return Strings.fromArrayList( a );
	}
	static public ArrayList<String> arrayListFromString( String buf ) {
		ArrayList<String> a = new ArrayList<String>();
		if (buf != null && !buf.equals( "" )) { // NB this doesn't tie up with parsing in Attributes.c!!!!
			for (int i=0, sz=buf.length(); i<sz; ) {
				while (i<sz && Character.isWhitespace( buf.charAt( i ) ))
					i++;
				if (i<sz) {
					String word = "";
					if (Character.isLetter( buf.charAt( i ))
						|| '_' == buf.charAt( i )
						|| '$' == buf.charAt( i ))
					{ // "normal string -- >don't< is now all one string!
						word += Character.toString( buf.charAt( i++ ));
						while (i<sz && (
							   Character.isLetter( buf.charAt( i ))
							|| Character.isDigit(  buf.charAt( i ))
							||	( '-'  == buf.charAt( i ))
							||	( '\'' == buf.charAt( i ))
							||	( '_'  == buf.charAt( i ))
						))
							word += Character.toString( buf.charAt( i++ ));
					} else if (Character.isDigit( buf.charAt( i ) )) {
						while (i<sz && Character.isDigit( buf.charAt( i ) ))
							word += Character.toString( buf.charAt( i++ ));   // will need to read 100.00
// check for "def'def", " 'def" or "...def'[ ,.?!]" to capture embedded apostrophes
					} else if ('\'' == buf.charAt( i ) ) {
						// quoted string with embedded apostrophes 'no don't'
						word += Character.toString( buf.charAt( i++ ));
						while( i<sz &&
						      !('\'' == buf.charAt( i ) && // ' must be followed by WS
						        (1+i==sz || Character.isWhitespace( buf.charAt( 1+i )))
						     ) )
							word += Character.toString( buf.charAt( i++ ));
						if (i<sz) word += Character.toString( buf.charAt( i++ ));
					} else if ('"' == buf.charAt( i ) ) {
						word += Character.toString( buf.charAt( i++ ));
						while( i<sz && '"' != buf.charAt( i ) )
							word += Character.toString( buf.charAt( i++ ));
						if (i<sz) word += Character.toString( buf.charAt( i++ ));
					} else if ((i+2 < sz)
									&& buf.charAt( i ) == '.'
									&& buf.charAt( i+1 ) == '.'
									&& buf.charAt( i+2 ) == '.')	{
						word = "...";
						i += 3;
					} else
						word = Character.toString( buf.charAt( i++ ));
					if (!word.equals( "" )) { a.add( word ); word = ""; }
		}	}	}	
		return a;
	}
	static public ArrayList<String> ListFromSeparatedString( String buf, char sep ) {
		ArrayList<String> a = null;
		if (null != buf) {
			a = new ArrayList<String>();
			int sz = buf.length();
			if (0 < sz) {
				int cp = 0;
				String word = null;
				while( cp<sz ) {
					word="";
					while( cp<sz && (sep != buf.charAt(cp)))
						word += Character.toString( buf.charAt( cp++ )); // *cp++ = *buf++;
					a.add( new String( word ));
					if ( cp<sz && sep == buf.charAt(cp) ) { // not finished
						cp++;         // avoid separator
						if (cp>=sz) // now finished!
							a.add( new String( "" )); // add trailing blank string!
		}	}	}	}
		return a;
	}
	static public boolean equals( String[] a, String[] b ) {
		if (a == b) return true;
		if (   (a != null && b != null)
		    && (a.length  == b.length ))
		{
			for (int i=0; i<a.length; i++)
				if (!a[ i ].equals( b[ i ]))
					return false;
			return true;
		}
		return false;
	}
	static public String[] fromString( String buf, char sep ) {
		ArrayList<String> a = Strings.ListFromSeparatedString( buf, sep );
		return Strings.fromArrayList( a );
	}
	static public String[] fromString( String buf, String sep ) {
		ArrayList<String> a = Strings.ListFromSeparatedString( buf, sep.charAt( 0 ));
		return Strings.fromArrayList( a );
	}
	public static final String lineTerm = "\n";
	static public String[] fromLines( String buf ) {
		ArrayList<String> a = Strings.ListFromSeparatedString( buf, lineTerm.charAt( 0 ));
		return Strings.fromArrayList( a );
	}
	static public String[] removeAt( String[] a, int n ) {
		if (a==null || n >= a.length) return a;
		String [] b = new String[ a.length - 1 ];
		for(int ai=0, bi=0; ai<a.length; ai++)
			if (ai != n )
				b[ bi++ ] = a[ ai ];
		return b;
	}
	static public String[] removeAll( String[] a, String val ) {
		String [] b = new String[ 0 ];
		for (int ai=0; ai<a.length; ai++)
			if (!a[ ai ].equals( val ))
				b = Strings.append( b, a[ ai ]);
		return b;
	}
	// EITHER:
	// (a=[ "One Two Three", "Ay Bee Cee", "Alpha Beta" ], val= "Bee") => "Ay Bee Cee";
	//static public String getContext( String[] a, String val ) {
	//	return "incomplete";
	//}
	// OR: (outer=[ "a", "strong", "beer" ], inner=[ "strong", "beer" ]) => true
	static public boolean containsStrings( String[] outer, String[] inner ) {
		if (outer.length == 0 && 0 == inner.length)
			return true;
		else if (outer.length >= inner.length)
			for (int o=0; o<=outer.length-inner.length; o++)
				for (int i=0; i<inner.length; i++)
					if (outer[ o + i ].equals( inner[ i ])) return true;
		return false;
	}
	// ...OR: -------------------------------------------
	// a=[ "One Two Three", "Aye Bee Cee", "Alpha Beta" ], val= "Bee" => b = [ "One Two Three", "Alpha Beta" ];
	static public String[] removeAllMatched( String[] a, String val ) {
		String [] b = new String[ 0 ];
		String[] valItems = Strings.fromString( val );
		for (int ai=0; ai<a.length; ai++) 
			if (!Strings.containsStrings( Strings.fromString( a[ ai ]), valItems ))
				b = Strings.append( b, a[ ai ]);
		return b;
	}
	// ---------------------------------------------
	static public String[] removeFirst( String[] a, String val ) {
		boolean removed = false;
		String [] b = new String[ 0 ];
		for (int ai=0; ai<a.length; ai++)
			if (!removed && a[ ai ].equals( val ))
				removed = true;
			else
				b = Strings.append( b, a[ ai ]);
		return b;
	}
	static public String[] append( String[] a, String str ) {
		if (null == str) return a;
		String [] b = new String[ null == a ? 1 : a.length+1 ];
		if (a != null) for(int i=0, sz=a.length; i<sz; i++)
			b[ i ] = a[ i ];
		b[ b.length - 1 ] = str;
		return b;
	}
	static public String[] append( String[] a, String sa[] ) {
		if (null == a || a.length == 0) return sa;
		if (null == sa || sa.length == 0) return a;
		String [] b = new String[ a.length+sa.length ];
		for(int i=0; i<a.length; i++)
			b[ i ] = a[ i ];
		for(int i=0; i<sa.length; i++)
			b[ a.length + i  ] = sa[ i ];
		return b;
	}
	static public String[] prepend( String[] a, String str ) {
		String [] b = new String[ null == a ? 1 : a.length+1 ];
		if (a != null) for(int i=0, sz=a.length; i<sz; i++)
			b[ i+1 ] = a[ i ];
		b[ 0 ] = str;
		return b;
	}
	static public String[] copyAfter( String[] a, int n ) {
		String [] b = null;
		if (a != null && a.length > n+1) {
			b = new String[ a.length - n - 1 ]; // a[3], 0 ==> b[2]
			for (int i = 0, sz = b.length; i<sz; i++)
				b[ i ] = a[ n+1 + i ];
		}
		return b;
	}
	static public String[] copyFromUntil( String[] a, int n, String until ) {
		String[] b = null;
		if (null != a) {
			// calc position of until token - or size of a if not found
			int az=a.length, posn=az;
			for (int ai=n; ai<az && posn == az; ai++)
				if ((until != null) && a[ ai ].equals( until ))
					posn = ai;
			b = new String[ posn - n ];
			for (int i = 0, sz = b.length; i<sz; i++)
				b[ i ] = a[ n + i ];
		}
		return b;
	}
	
	static public String[] fromNonWS( String buf ) {
		String[] a = null;
		if (buf != null) {
			StringBuffer word = null;
			for (int i=0, sz=buf.length(); i<sz; i++ ) {
				word = new StringBuffer();
				while( i<sz &&  Character.isWhitespace( buf.charAt( i ))) i++;
				while( i<sz && !Character.isWhitespace( buf.charAt( i ))) { word.append( buf.charAt( i )); i++; }
				
				if (null != word)
					a = append( a, word.toString());
		}	}
		return a;
	}
	private static String[] incrStringsSpace( String[] a ) {
		String[] b = new String[ null == a ? 1 : a.length + 1 ];
		if (a!=null) for (int i=0, sz=a.length; i<sz; i++) b[ i ] = a[ i ]; 
		return b;
	}
	static public String[] insertAt( String[] a, int pos, String str ) {
		if (str != null) {
			int i, sz = a.length;
			if ( pos <  0 ) pos = 0;
			else if ( pos > sz ) pos = sz;
			a = incrStringsSpace( a );
			for( i=sz; i>pos; i--) a[ i ] = a[ i-1 ];
			a[ pos ] = str;
		}
		return a;
	}
	static public String[] reverse( String[] a ) {
		int sz=a.length;
		String[] b = new String[ sz ];
		for (int ai=0, bi=sz-1; ai<sz; ai++, bi-- ) b[ bi ] = a[ ai ];
		return b;
	}

	// replace( [ "hello", "martin", "!" ], [ "martin" ], [ "to", "you" ]) => [ "hello", "to", "you", "!" ]
	static public String[] replace( String[] a, String[] b, String[] c ) {
		int alen = a.length, blen = b.length, clen = c.length;
		for (int i=0; i <= alen - blen; i++) {
			boolean found = true;
			int j=0;
			for (j=0; j<blen && found; j++)
				if (0 != a[ i+j ].compareToIgnoreCase( b[ j ])) found=false;
			if (found) {
				for (j=0; j<blen; j++) a = Strings.removeAt( a, i );
				for (j=0; j<clen; j++) a = Strings.insertAt( a, i+j, c[ j ]);
				alen = a.length; // ...since we've messed with a
		}	}
		return a;
	}
	static public int indexOf( String[] a, String s ) {
		int i = -1;
		if (null != a && null != s) {
			while ( ++i < a.length )
				if (a[ i ].equals( s ))
					break;
			if (i == a.length) i = -1;
		}
		return i;
	}
	static public boolean contain( String[] a, String s ) { return -1 != indexOf( a, s ); }
	
	// deals with matched and unmatched values:
	// [ "a", "$matched", ".",  "b" ] => [ "a", "martin", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
	// [ "a",  "MATCHED", ".",  "b" ] => [ "a", "martin", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
	// [ "a", "$unmatch", ".",  "b" ] => [ "a", "_USER", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
	// [ "we are holding hands", "."  ] => [ "we", "are", "holding", "hands", "." ] -- jik - just in case!
	// matches are from tags, *ap contains mixed case - any UPPERCASE items should match matches OR envvars.
	static public String[][] split( String[] a, String[] terminators ) {
		String[][] split = new String[2][];
		if ( a.length > 0 ) {
			boolean done = false;
			// split remote array into local array -- splitting each entry...
			while (!done && a.length > 0) {
				String[] b = Strings.fromString( a[ 0 ]); // split each entry incase they're a phrase!
				for (int bi = 0, sz=b.length; bi<sz; bi++) split[ 0 ] = Strings.append( split[ 0 ], b[ bi ]);
				done = Strings.contain( terminators, a[ 0 ]);
				a = Strings.removeAt( a, 0 );
			}
			// --
			// the last utterance may not be terminated -- add the default terminator
			// if, however, we need not add terms in some cases,
			// we may want to move this code to where it is called -- e.g. Rule.reUtter()
			if (null != split[ 0 ] && split[ 0 ].length > 0)
				if (Strings.contain( terminators, split[ 0 ][ split[ 0 ].length - 1 ]))
					split[ 0 ] = Strings.append( split[ 0 ], terminators[ 0 ]);	
			// --
		}
		split[ 1 ] = a;
		return split;
	}
	// [ 'some', 'bread', '+', 'fish'n'chips', '+', 'some', 'milk' ], "+"  => [  'some bread', 'fish and chips', 'some milk' ]
	static public String[] rejig( String[] a, String ipSep, String opSep ) {
		// remember, if sep='+', 'fish', '+', 'chips' => 'fish+chips' (i.e. NOT 'fish + chips')
		// here: some coffee + fish + chips => some coffee + fish and chips
		String[] values = new String[ 0 ];
		if (a != null) {
			int i = 0;
			String value = a[ i++ ];
			String localSep = opSep;
			while (i < a.length) {
				if (a[ i ].equals( ipSep )) {
					values = Strings.append( values, value );
					value = "";
					localSep = "";
				} else {
					value += ( localSep + a[ i ]);
					localSep = opSep;
				}
				i++;
			}
			values = Strings.append( values, value );
		}
		return values;
	}
	static public String[] rejig( String[] a, String sep ) {
		return rejig( a, sep, " " );
	}
	// TODO: remove rejig, above? Or, combine with expand() and normalise()/
	// NO: Need Stringses to Strings & vv [ "some", "beer", "+", "some crisps" ] => "some beer", "some crisps" ]
	// [ "some beer", "some crisps" ] => [ "some", "beer", "+", "some", "crisps" ]
	// TODO: expand input, and apply each thought...
	// I need to go to the gym and the jewellers =>
	// (I need to go to the gym and I need to go to the jewellers =>)
	// I need to go to the gym. I need to go to the jewellers.
	/* [ [ "to", "go", "to", "the", "gym" ], [ "the", "jewellers" ] ] 
	 * => [ [ "to", "go", "to", "the", "gym" ], [ "to", "go", "to", "the", "jewellers" ] ]
	 */
	static public String[][] expand( String[][] a ) {
		return a;
	}
	
	// [ "one", "two three" ] => [ "one", "two", "three" ]
	// TODO: a bit like re-jig aove???
	static public String[] normalise( String[] sa ) {
		String[] a = new String[ 0 ];
		for (String s1 : sa ) 
			for ( String s2: Strings.fromString( s1 ))
				a = Strings.append( a, s2 );
		return a;
	}
	private static boolean isCapitalised( String str ) {
		if (null == str) return false;
		int i = 0;
		if (Character.isUpperCase( str.charAt( i ))) while (str.length() > ++i && Character.isLowerCase( str.charAt( i )));
		return str.length() == i; // capitalised if we're at the end of the string
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
		
	// [ "THIS", "is Martin" ] => [ "THIS", "is", "martin" ]
	static public String[] decap( String[] a ) {
		String[] b = null;
		int i = -1;
		//remove all capitalisation... we can re-capitalise on output.
		if (null != a) while (a.length > ++i) {
			int ti = -1;
			String[] t = fromString( a[ i ]); // split each entry in case they're a phrase!
			while (t.length > ++ti) {
				if (isCapitalised( t[ ti ])) t[ ti ] = Character.toLowerCase( t[ ti ].charAt( 0 )) + t[ ti ].substring( 1 );
				b = Strings.append( b, t[ ti ]);
		}	}
		return b;
	}
	static public String trim( String a, char ch ) {
		// (a="\"hello\"", ch='"') => "hello"; ( "ohio", 'o' ) => "hi"
		if (a.length() == 2 && a.charAt( 0 ) == ch && a.charAt( 1 ) == ch)
			return "";
		else if (a.length() > 2 && a.charAt( 0 ) == ch && a.charAt( a.length()-1 ) == ch)
			return a.substring( 1, a.length()-1 );
		else
			return a;
	}
	public static void main( String args[]) {
		String[] a = Strings.fromString( "failure won't 'do' 'don't'" );
		for (int i=0; i<a.length; i++)
			System.out.println( a[ i ]);
		String[] b = Strings.fromString( "failure" ),
		          c = Strings.fromString( "world" );
		//args = Strings.insertAt( args, -1, "martin" );
		args = Strings.replace( args, b, c );
		System.out.println( "===> ["+ Strings.toString( args, "'", "', '", "'" ) +"] <===" );
}	}