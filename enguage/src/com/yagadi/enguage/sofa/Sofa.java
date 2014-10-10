package com.yagadi.enguage.sofa;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.*;
import com.yagadi.enguage.util.*;

public class Sofa extends Shell {
	//static private Audit audit = new Audit( "Sofa" );

	public Sofa( String[] args ){ super( "Sofa", args );}
	private static final String True  = SUCCESS;
	private static final String False = FAIL;

	static String helpSofa[] = { "help", null, null };

	// -- helpers
	// [ ""hello", "there"" ] --> "hello there"
	// not sure if this will work, e.g.: [ "hello \"", "there", "\" martin" ] ???
	static String newUnquotedCharsFromStrings( String[] a ) { // strips unquoted '"' and "'" parenthesis
		String chs = "";
		for (int i=0, asz=a.length; i<asz; i++) {
			if (i > 0) chs += " ";
			char firstCh = a[ i ].charAt( 0 );
			if (('"' == firstCh) || ( '\'' == firstCh))
				for (int cp = 0, cz = a[ i ].length(); cp < cz && (firstCh != a[ i ].charAt( cp )); cp++)
					chs += Character.toString( a[ i ].charAt( cp ));
			else
				chs += a[ i ];
		}
		return chs;
	}
/*
long xxxvalueInterp( String[] a ) { // e.g. [ "set", "$user", "=", "Martin" ]
	TRACEIN1("%s", arrayAsChars( a, SPACED ));
	long rc=1, argc = arraySize( a );
	String[] args = copyStringsItemsAfter( a, 2, CHAR_ITEMS );
	
	if(( argc >= 4 ) && 0 == .compareTo("set", a[ 0 ]) && 0 == .compareTo("=", a[ 2 ])) {
		//rc = createLink( a[ 1 ], a[ 2 ], args );
		String cp = ALL_toUpper( a[ 1 ]);
		setenv( *cp=='$' ? ++cp : cp, a[ 3 ], 1 );
	} else {
		fprintf( stderr, "Usage: value: set <var> = <value>\ngiven: %s\n", arrayAsChars( a, SPACED ));	
		rc = 0;
	}
	
	deleteStrings( &args, CHAR_ITEMS );
	TRACEOUTlong( rc );
	return rc;
}
*/
	
	private String doCall( String[] a ) {
		//audit.traceIn( "doCall", Strings.toString( a, Strings.CSV ));
		if (null != a && a.length > 0)
			return //audit.traceOut(
			     a.length == 1 && a[ 0 ].equals(         True ) ? True :
				 a.length == 1 && a[ 0 ].equals(        False ) ? False :
			                      a[ 0 ].equals(     "entity" ) ?      Entity.interpret( Strings.copyAfter( a, 0 ) ) :
			                      a[ 0 ].equals(       "link" ) ?        Link.interpret( Strings.copyAfter( a, 0 ) ) :
			                      a[ 0 ].equals(   Value.NAME ) ?       Value.interpret( Strings.copyAfter( a, 0 ) ) :
			                      a[ 0 ].equals(    List.NAME ) ?        List.interpret( Strings.copyAfter( a, 0 ) ) :
			                      a[ 0 ].equals( "preferences") ? Preferences.interpret( Strings.copyAfter( a, 0 ) ) :
			                 	  a[ 0 ].equals( Numeric.NAME ) ?     Numeric.interpret( Strings.copyAfter( a, 0 ) ) :
						          a[ 0 ].equals(   "variable" ) ?    Variable.interpret( Strings.copyAfter( a, 0 ) ) :
			                      a[ 0 ].equals(    "overlay" ) ?     Overlay.interpret( Strings.copyAfter( a, 0 ) ) :
				                  a[ 0 ].equals( "colloquial" ) ? Colloquials.interpret( Strings.copyAfter( a, 0 ) ) :
				 a.length == 4 && a[ 0 ].equals( "host" ) && a[ 1 ].equals( "add" ) ? Reply.host().add( Strings.trim( a[ 2 ], '"' ), Strings.trim( a[ 3 ], '"' )) :
				 a.length == 4 && a[ 0 ].equals( "user" ) && a[ 1 ].equals( "add" ) ? Enguage.user().add( Strings.trim( a[ 2 ], '"' ), Strings.trim( a[ 3 ], '"' )) :
			                    	  Shell.FAIL ;//);
		return Shell.FAIL; //audit.traceOut( Shell.FAIL );
	}
	
	// perhaps need to re-think this? Do we need this stage - other than for relative concept???
	private String doSofa( String[] prog ) {
		String rc = null;
		/* TODO: not sure if this is used anymore -- 
		 * stdout isn't used - need to return unquoted,
		 * prog will be the first value only, e.g. '"hello there"', '||', ...
		 * newUnquoted... is flawed, in several ways.
		 */
		if (('"' == prog[ 0 ].charAt( 0 )) || ('\'' == prog[ 0 ].charAt( 0 ))) { // is prog a constant string
			String chs = newUnquotedCharsFromStrings( prog );
			String[] a = Strings.fromNonWS( chs );
			//a = preProcessAnA( a );
			System.out.println( Strings.toString( a, Strings.SPACED ));
		} else
			rc = doCall( prog );
		return rc ;
	}

	private String doNeg( String[] prog ) {
		//audit.traceIn( "doNeg", Strings.toString( prog, Strings.SPACED ));
		boolean negated = prog[ 0 ].equals( "!" );
		String rc = doSofa( Strings.copyAfter( prog, negated ? 0 : -1 ) );
		//audit.debug( "answer is "+ rc );
		if (negated) rc = rc.equals( True ) ? False : rc.equals( False ) ? True : rc;
		return rc; //audit.traceOut( rc );
	}

/*private static String doAssign( Strings prog ) { // x = a b .. z
	TRACEIN1( "'%s'", arrayAsChars( prog, SPACED ));
	int assignment = 0 == .compareTo( prog[ 1 ], "=" );
	Strings interpreter = copyStringsAfter( prog, assignment ? 1 : -1 );
	long rc = doNeg( interpreter );
	if (assignment) {
		if (0 == .compareTo( "value", prog[ 3 ])) { // deal with string return
			AUDIT2( "Assigning STRING %s = %s", prog[ 0 ], rc ? (String )rc : "" );
			int n = arrayContainsCharsAt( symbols, prog[ 0 ]);
			if (n == -1) {
				symbols = arrayAppend( symbols, newChars( prog[ 0 ]));
				values = arrayAppend( values, newChars( rc ? (String )rc : "" ));
			} else
				arrayReplaceCharsAt( values, n, rc ? (String )rc : "" );
		} else if (0 == .compareTo( "exists", prog[ 3 ])) { // deal with string return
			AUDIT2( "Assigning BOOLEAN %s = %s", prog[ 0 ], rc ? "true" : "false" );
			int n = arrayContainsCharsAt( symbols, prog[ 0 ]);
			if (n == -1) {
				symbols = arrayAppend( symbols, newChars( prog[ 0 ]));
				values = arrayAppend( values, newChars( rc ? "true" : "false" ));
			} else
				arrayReplaceCharsAt( values, n, rc ? "true" : "false" );
		} else {
			printf( "type conversion error in '%s'\n", arrayAsChars( prog, SPACED ));
	}	}
	deleteStrings( &interpreter, KEEP_ITEMS );
	TRACEOUTint( rc );
	return rc ;
}// */

	// a b .. z {| a b .. z}
	private String doOrList( String[] a ) {
		//audit.traceIn( "doOrList", Strings.toString( a, Strings.SPACED ));
		String rc = False;
		for (int i = 0, sz = a.length; i<sz; i++) {
			String[] cmd = Strings.copyFromUntil( a, i, "|" );
			i += cmd.length; // left pointing at "|" or null
			if (0 == rc.compareTo( False )) rc = doNeg( cmd ); // only do if not yet succeeded -- was doAssign()
		}
		//return audit.traceOut( rc );
		return rc;
	}

	private String doAndList( String[] a ) {
		//audit.traceIn( "doAndList", Strings.toString( a, Strings.SPACED ));
		String rc = True;
		for (int i=0, sz=a.length; i<sz; i++) {
			String[] cmd = Strings.copyFromUntil( a, i, "&" );
			i += cmd == null ? 0 : cmd.length;
			if (0 == rc.compareTo( True )) rc = doOrList( cmd );
		}
		return rc; // audit.traceOut( rc );
	}

	private String doExpr( String[] a ) {
		//audit.traceIn( "doExpr", Strings.toString( a, Strings.SPACED ));
		String[] cmd = new String[ 0 ];
		while (0 < a.length && 0 != a[ 0 ].compareTo( ")" )) {
			if (a[ 0 ].equals( "(" )) {
				a = Strings.removeAt( a, 0 );
				cmd = Strings.append( cmd, doExpr( a ));
			} else {
				cmd = Strings.append( cmd, a[ 0 ]);
				a = Strings.removeAt( a, 0 ); // KEEP_ITEMS!
		}	}
		String rc = doAndList( cmd );
		if ( 0 < a.length ) a = Strings.removeAt( a, 0 ); // remove ")"
		return rc; //audit.traceOut( rc );
	}
	public String interpret( String[] sa ) { return doExpr( sa ); }
	
	public static void main( String[] args ) { // sanity check...
		Overlay.Set( Overlay.Get());
		String rc = Overlay.autoAttach();
		if (0 != rc.compareTo( "" ))
			System.out.println( "Ouch!" );
		else {
			System.out.println( "Sofa: Ovl is: "+ Overlay.Get().toString());
			
			Attributes a = new Attributes();
			a.add( new Attribute( "m", "martin" ));
			a.add( new Attribute( "r", "ruth" ));
			
			args = a.getCommand(  args );			
			System.out.println( "Cmds are: "+ Strings.toString( args, Strings.SPACED ));
			
			Sofa cmd = new Sofa( args );
			cmd.run();
}	}	}
