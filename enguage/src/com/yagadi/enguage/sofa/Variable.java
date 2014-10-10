package com.yagadi.enguage.sofa;

import com.yagadi.enguage.util.*;

public class Variable {
	/* As part of the Sofa library, variable manages persistent values:
	 * /a/b/c/_NAME -> Value, which is the persistent equivalent of NAME="value".
	 * 
	 */
	//static private Audit audit = new Audit( "Variable" );
	
	static private String prefix = "$";
	static public  void   prefix( String s ) { if (s!=null && s.length()>0) prefix = s; }
	static public  String prefix( ) { return prefix; }
	
	static public void set( String name, String value ) {
		if (name.substring( 0, prefix.length() ).equals( prefix )) // if prefixed
			name = "_" + name.substring( 1 );
		else if ('_' != name.charAt( 0 ))
			name = "_" + name;
		Link.set( name, value, null );
	}
	static public void unset( String name ) {
		if (name.substring( 0, prefix.length() ).equals( prefix )) // if prefixed
			name = "_" + name.substring( 1 );
		else if ('_' != name.charAt( 0 ))
			name = "_" + name;
		Link.destroy( name, null );
	}
	static public String get( String name ) {     // this returns newChars
		if (name.substring( 0, prefix.length()).equals( prefix ))
			name = "_" + name.substring( 1 );
		else if ('_' != name.charAt( 0 ))
			name = "_" + name;
		return Link.get( name, null );
	}
	static public String get( String name, String def ) {
		String value = get( name );
		return value == null ? def : value;
	}
	static public boolean isSet( String name ) { return get( name ) != null; }
	
	static public String deref( String name ) {
		String val = null;
		if (null != name
		 && !name.equals("")
		 &&	name.substring( 0, prefix.length()).equals( prefix )
		)
			val = get( name );
		return null != val ? val : name;
	}
	static public String[] deref( String[] a ) {
		int sz = a.length;
		String[] b = new String[ sz ];
		for (int i=0; i<sz; i++)
			b[ i ] = deref( a[ i ]);
		return b;
	}

	static public void setEnvHOST( String defaultName ) {
		// HOST isn't in env on cygwin -- but is needed!
		if (null != System.getenv( "HOST" )) {
			String hostname = System.getenv( "HOSTNAME" );
			if (null == hostname || 0 == hostname.length()) {
				hostname = Filesystem.stringFromFile( "/etc/hostname" );
				if (null == hostname || hostname.equals("")) {
					hostname = defaultName;
				} else
					hostname = hostname.substring( 0, hostname.length() - 1 ); // remove cr 
			}
			set( "_host", hostname );
	}	}

	static public String interpret( String[] args ) {
		String rc = Shell.SUCCESS;
		if (args[ 0 ].equals( "set" ) && args.length > 2)
			set( args[ 1 ], Strings.toString( Strings.copyAfter( args, 1 ), Strings.SPACED ));
		else if (args[ 0 ].equals( "unset" ) && args.length > 1)
			unset( args[ 1 ]);
		else if (args[ 0 ].equals( "exists" ) && args.length > 1)
			isSet( args[ 1 ]);
		else if (args[ 0 ].equals( "get" ) && args.length > 1)
			rc = get( Strings.toString( Strings.copyAfter( args, 1 ), Strings.SPACED ));
		else
			rc = Shell.FAIL;
		return rc;
	}
	public static void main( String[] args ) {
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
