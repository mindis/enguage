package com.yagadi.enguage.sofa;

import com.yagadi.enguage.expression.Colloquial;
import com.yagadi.enguage.expression.Plural;
import com.yagadi.enguage.sofa.tier2.Item;
import com.yagadi.enguage.sofa.tier2.List;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;


public class Sofa extends Shell {
	static private Audit audit = new Audit( "Sofa" );

	public Sofa(){ this( null );}
	public Sofa( Strings args ){ super( "Sofa", args );}
	private static final String True  = SUCCESS;
	private static final String False = FAIL;

	// -- helpers
	// [ ""hello", "there"" ] --> "hello there"
	// not sure if this will work, e.g.: [ "hello \"", "there", "\" martin" ] ???
	static String newUnquotedCharsFromStrings( Strings a ) { // strips unquoted '"' and "'" parenthesis
		String chs = "";
		for (int i=0, asz=a.size(); i<asz; i++) {
			if (i > 0) chs += " ";
			char firstCh = a.get( i ).charAt( 0 );
			if (('"' == firstCh) || ( '\'' == firstCh))
				for (int cp = 0, cz = a.get( i ).length(); cp < cz && (firstCh != a.get( i ).charAt( cp )); cp++)
					chs += Character.toString( a.get( i ).charAt( cp ));
			else
				chs += a.get( i );
		}
		return chs;
	}
	public String doCall( Strings a ) {
		//audit.traceIn( "doCall", a.toString( Strings.CSV ));
		if (null != a && a.size() > 1) {
			
			/* Tags.matchValues() now produces:
			 * 		["a", "b", "c='d'", "e", "f='g'"]
			 * Sofa.interpret() typically deals with:
			 * 		["string", "get", "martin", "name"]
			 * 		["colloquial", "add", "'I have'", "'I've'"]
			 * Need to ensure first 4? name/value pairs are dereferenced
			 * Needs to be done here, as call() will be called independently
			 * May need to be selective on how this is done, depending on sofa 
			 * package class requirements...?
			 */
			for (int i=0; i<5 && i<a.size(); i++)
				a.set( i, Attribute.expandValues( a.get( i ) ).toString( Strings.SPACED ));
			//audit.audit("Sofa.doCall() => a is "+ a.toString());
			//a = a.normalise().contract( "=" );// rejig:"list","get","one two","three four"] => "list","get","one","two","three","four"]
			//audit.debug("Sofa.doCall() => "+ a.toString());
			
			String  type = a.get( 0 ),
			      method = Attribute.expandValues( a.get( 1 )).toString( Strings.SPACED );
			
			return //audit.traceOut(
			     a.size() == 1 && type.equals(         True ) ? True :
				 a.size() == 1 && type.equals(        False ) ? False :
			                      type.equals(     "entity" ) ?      Entity.interpret( a.copyAfter( 0 ) ) :
			                      type.equals(       "link" ) ?        Link.interpret( a.copyAfter( 0 ) ) :
			                      type.equals(   Value.NAME ) ?       Value.interpret( a.copyAfter( 0 ) ) :
			                      type.equals(    List.NAME ) ?        List.interpret( a.copyAfter( 0 ) ) :
			                      type.equals( "preferences") ? Preferences.interpret( a.copyAfter( 0 ) ) :
			                 	  type.equals( Numeric.NAME ) ?     Numeric.interpret( a.copyAfter( 0 ) ) :
						          type.equals( Variable.NAME) ?    Variable.interpret( a.copyAfter( 0 ) ) :
			                      type.equals(    "overlay" ) ?     Overlay.interpret( a.copyAfter( 0 ) ) :
				                  type.equals( "colloquial" ) ?  Colloquial.interpret( a.copyAfter( 0 ) ) :
				               	  type.equals(  Plural.NAME ) ?      Plural.interpret( a.copyAfter( 0 ) ) :
				               	  type.equals(    Item.NAME ) ?        Item.interpret( a.copyAfter( 0 ) ) :
				 a.size() == 4 && type.equals( "host" ) && method.equals( "add" ) ? Colloquial.host().add( Strings.trim( a.get( 2 ), '"' ), Strings.trim( a.get( 3 ), '"' )) :
				 a.size() == 4 && type.equals( "user" ) && method.equals( "add" ) ? Colloquial.user().add( Strings.trim( a.get( 2 ), '"' ), Strings.trim( a.get( 3 ), '"' )) :
			                    	  Shell.FAIL; // );
		}
		audit.ERROR("doCall() fails - "+ (a==null?"no params":"not enough params: "+ a.toString()));
		return Shell.FAIL; //audit.traceOut( Shell.FAIL ); //
	}
	
	// perhaps need to re-think this? Do we need this stage - other than for relative concept???
	private String doSofa( Strings prog ) {
		String rc = null;
		/* TODO: not sure if this is used anymore -- 
		 * stdout isn't used - need to return unquoted,
		 * prog will be the first value only, e.g. '"hello there"', '||', ...
		 * newUnquoted... is flawed, in several ways.
		 */
		if (('"' == prog.get( 0 ).charAt( 0 )) || ('\'' == prog.get( 0 ).charAt( 0 ))) { // is prog a constant string
			String chs = newUnquotedCharsFromStrings( prog );
			Strings a = Strings.fromNonWS( chs );
			//a = preProcessAnA( a );
			System.out.println( a.toString( Strings.SPACED ));
		} else
			rc = doCall( prog );
		return rc ;
	}

	private String doNeg( Strings prog ) {
		//audit.traceIn( "doNeg", prog.toString( Strings.SPACED ));
		boolean negated = prog.get( 0 ).equals( "!" );
		String rc = doSofa( prog.copyAfter( negated ? 0 : -1 ) );
		if (negated) rc = rc.equals( True ) ? False : rc.equals( False ) ? True : rc;
		return rc; // */audit.traceOut( rc );
	}

/*private static String doAssign( Strings prog ) { // x = a b .. z
	TRACEIN1( "'%s'", arrayAsChars( prog, SPACED ));
	int assignment = 0 == .compareTo( prog[ 1 ], "=" );
	Strings e = copyStringsAfter( prog, assignment ? 1 : -1 );
	long rc = doNeg( e );
	if (assignment) {
		if (0 == .compareTo( "value", prog[ 3 ])) { // deal with string return
			AUDIT2( "Assigning STRING %s = %s", prog.get( 0 ), rc ? (String )rc : "" );
			int n = arrayContainsCharsAt( symbols, prog.get( 0 ));
			if (n == -1) {
				symbols = arrayAppend( symbols, newChars( prog.get( 0 )));
				values = arrayAppend( values, newChars( rc ? (String )rc : "" ));
			} else
				arrayReplaceCharsAt( values, n, rc ? (String )rc : "" );
		} else if (0 == .compareTo( "exists", prog[ 3 ])) { // deal with string return
			AUDIT2( "Assigning BOOLEAN %s = %s", prog.get( 0 ), rc ? "true" : "false" );
			int n = arrayContainsCharsAt( symbols, prog.get( 0 ));
			if (n == -1) {
				symbols = arrayAppend( symbols, newChars( prog.get( 0 )));
				values = arrayAppend( values, newChars( rc ? "true" : "false" ));
			} else
				arrayReplaceCharsAt( values, n, rc ? "true" : "false" );
		} else {
			printf( "type conversion error in '%s'\n", arrayAsChars( prog, SPACED ));
	}	}
	deleteStrings( &e, KEEP_ITEMS );
	TRACEOUTint( rc );
	return rc ;
}// */

	// a b .. z {| a b .. z}
	private String doOrList( Strings a ) {
		//audit.traceIn( "doOrList", a.toString( Strings.SPACED ));
		String rc = False;
		for (int i = 0, sz = a.size(); i<sz; i++) {
			Strings cmd = a.copyFromUntil( i, "||" );
			i += cmd.size(); // left pointing at "|" or null
			if (rc.equals( False )) rc = doNeg( cmd ); // only do if not yet succeeded -- was doAssign()
		}
		//return audit.traceOut( rc );
		return rc;
	}

	private String doAndList( Strings a ) {
		//audit.traceIn( "doAndList", a.toString( Strings.SPACED ));
		String rc = True;
		for (int i=0, sz=a.size(); i<sz; i++) {
			Strings cmd = a.copyFromUntil( i, "&&" );
			//audit.debug( "cmd=" + cmd +", i="+ i );
			i += cmd == null ? 0 : cmd.size();
			if (rc.equals( True )) rc = doOrList( cmd );
		}
		return rc; // */ audit.traceOut( rc );
	}

	private String doExpr( Strings a ) {
		//audit.traceIn( "doExpr", a.toString( Strings.SPACED ));
		Strings cmd = new Strings(); // -- build a command...
		while (0 < a.size() && !a.get( 0 ).equals( ")" )) {
			if (a.get( 0 ).equals( "(" )) {
				a.remove( 0 );
				cmd.add( doExpr( a ));
			} else {
				cmd.add( a.get( 0 ));
				a.remove( 0 ); // KEEP_ITEMS!
			}
			//audit.debug( "a="+ a.toString() +", cmd+"+ cmd.toString() );
		}
		String rc = doAndList( cmd );
		if ( 0 < a.size() ) a.remove( 0 ); // remove ")"
		return rc; // */audit.traceOut( rc );
	}
	public String interpret( Strings sa ) {
		Strings a = new Strings( sa );
		return doExpr( a );
	}
	
	public static void main( String[] argv ) { // sanity check...
		Strings args = new Strings( argv );
		Overlay.Set( Overlay.Get());
		String rc = Overlay.autoAttach();
		if (0 != rc.compareTo( "" ))
			System.out.println( "Ouch!" );
		else {
			System.out.println( "Sofa: Ovl is: "+ Overlay.Get().toString());
			
			Attributes a = new Attributes();
			a.add( new Attribute( "m", "martin" ));
			a.add( new Attribute( "r", "ruth" ));
			
			args = a.deref( Variable.deref( args ), true );			
			System.out.println( "Cmds are: "+ args.toString( Strings.SPACED ));
			
			Sofa cmd = new Sofa( args );
			cmd.run();
}	}	}
