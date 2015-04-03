package com.yagadi.enguage.sofa;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

// TODO: need to cache these values -- read at startup time, written if modified.

public class Variable {
	/* As part of the Sofa library, variable manages persistent values:
	 * /a/b/c/_NAME -> value, which is the persistent equivalent of NAME="value".
	 * Because $ has special significance in the filesystem/shell
	 * prefix variables with '_'
	 */
	static private Audit  audit = new Audit( "Variable" );
	static public  String  NAME = "variable";
	
	static private char prefix = '$';
	static public  void prefix( char s ) { prefix = s; }
	static public  char prefix( ) { return prefix; }
	
	static public void set( String name, String value ) {
		if (name.charAt( 0 ) == prefix ) // if prefixed
			name = "_" + name.substring( 1 );
		else if ('_' != name.charAt( 0 ))
			name = "_" + name;
		new Value(name, null).set( value );
	}
	static public void unset( String name ) {
		if (name.charAt( 0 ) == prefix ) // if prefixed
			name = "_" + name.substring( 1 );
		else if ('_' != name.charAt( 0 ))
			name = "_" + name;
		new Value( name, null ).ignore();
	}
	static public String get( String name ) {
		//audit.debug( "getting "+ name );
		if (name.charAt( 0 ) == prefix )
			name = "_" + name.substring( 1 );
		else if ('_' != name.charAt( 0 ))
			name = "_" + name;
		return new Value(name, null).getAsString();
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
		return (null != name &&
				!name.equals("") &&
				name.charAt( 0 ) == prefix) ?
			new Strings( get( name )) :
			new Strings( name ).contract( "=" );
	}
	/*static private Strings deref( Strings a ) {
		//audit.traceIn( "deref", a.toString());
		Strings b = new Strings();
		Iterator<String> ai = a.iterator();
		while (ai.hasNext())
			b.addAll( deref( ai.next() )); // preserve name='value'
		//audit.traceOut( b );
		return b;
	} // */
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
		Audit.turnOn();
		
		/*  Variable.set( "hello", "there" );
		 * Or rather:-
		 *  Variable v = new Variable( "hello" );
		 *  v.set( "there" );
		 *  System.out.println( "hello is:"+ v.get());
		 */
		Variable.set( "hello", "there" );
		audit.audit( "hello is "+ Variable.get( "hello" ) +" (there=>pass)" );
		Variable.unset( "hello" );
		audit.audit( "hello is "+ Variable.get( "hello" ) +" (null=>pass)" );
		audit.audit( "there is "+ Variable.get( "there" ) +" (null=>pass)" );
}	}
