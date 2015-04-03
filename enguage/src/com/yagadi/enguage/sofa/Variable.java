package com.yagadi.enguage.sofa;

import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;


public class Variable {
	/* As part of the Sofa library, variable manages persistent values:
	 * /a/b/c/_NAME -> value, which is the persistent equivalent of NAME="value".
	 */
	//static private Audit  audit = new Audit( "Variable" );
	static public  String  NAME = "variable";
	
	static private char prefix = '$';
	static public  void prefix( char s ) { prefix = s; }
	static public  char prefix( ) { return prefix; }
	
	static public void set( String name, String value ) {
		if (name.charAt( 0 ) == prefix ) // if prefixed
			name = "_" + name.substring( 1 );
		else if ('_' != name.charAt( 0 ))
			name = "_" + name;
		Link.set( name, value, null );
	}
	static public void unset( String name ) {
		if (name.charAt( 0 ) == prefix ) // if prefixed
			name = "_" + name.substring( 1 );
		else if ('_' != name.charAt( 0 ))
			name = "_" + name;
		Link.destroy( name, null );
	}
	static public String get( String name ) {
		//audit.debug( "getting "+ name );
		if (name.charAt( 0 ) == prefix )
			name = "_" + name.substring( 1 );
		else if ('_' != name.charAt( 0 ))
			name = "_" + name;
		return Link.get( name, null );
	}
	static public String get( String name, String def ) {
		String value = get( name );
		return value.equals("") ? def : value;
	}
	static public boolean isSet( String name ) { return !get( name ).equals(""); }
	static public Strings deref( String name ) {
		/* TODO need to strip x='$VAR' down to $VAR to deref!
		 * and return x='val'
		 */
		//audit.traceIn( "deref", name );
		return (null != name &&
				!name.equals("") &&
				name.charAt( 0 ) == prefix) ?
			new Strings( get( name )) :
			new Strings( name ).contract( "=" );
	}
	static public Strings deref( Strings a ) {
		//audit.traceIn( "deref", a.toString());
		int sz = a.size();
		Strings b = new Strings();
		for (int i=0; i<sz; i++)
			b.addAll( deref( a.get( i ))); // preserve name='value'
		//audit.traceOut( b );
		return b;
	}
	static public String interpret( Strings args ) {
		//audit.traceIn( "interpret", args.toString() );
		String rc = Shell.SUCCESS;
		if (args.get( 0 ).equals( "set" ) && args.size() > 2)
			set( args.get( 1 ), args.copyAfter( 1 ).toString( Strings.SPACED ));
		else if (args.get( 0 ).equals( "unset" ) && args.size() > 1)
			unset( args.get( 1 ));
		else if (args.get( 0 ).equals( "exists" ) && args.size() > 1)
			isSet( args.get( 1 ));
		else if (args.get( 0 ).equals( "get" ) && args.size() > 1)
			rc = get( args.copyAfter( 1 ).toString( Strings.SPACED ));
		else
			rc = Shell.FAIL;
		//audit.traceOut( rc );
		return rc;
	}
	public static void main( String args[] ) {
		Overlay.Set( Overlay.Get());
		Overlay.autoAttach();
		
		/*  Variable.set( "hello", "there" );
		 * Or rather:-
		 *  Variable v = new Variable( "hello" );
		 *  v.set( "there" );
		 *  System.out.println( "hello is:"+ v.get());
		 */
		
		System.out.println( "hello is "+ Variable.get( "hello" ) +" (there=>pass)" );
		System.out.println( "there is "+ Variable.get( "there" ) +" (null=>pass)" );
}	}
