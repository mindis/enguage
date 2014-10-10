package com.yagadi.enguage.sofa;

import com.yagadi.enguage.util.*;

public class Numeric extends Value {
	static private Audit audit = new Audit( "Numeric" );
	public static final String NAME = "numeric";
	
	public Numeric( String e, String a ) { super( e, a ); }
	
	public void  set( Float val ) {
		Filesystem.stringToFile( name( ent, attr, Overlay.MODE_WRITE ), Float.toString( val ));
	}
	public Float get( Float def ) {
		Float val;
		try {
			val = Float.valueOf( Filesystem.stringFromFile( name( ent, attr, Overlay.MODE_READ ) ));
		} catch (NumberFormatException nfe) {
			val = def;
		} catch (NullPointerException npe) {
			val = def;
		}
		return val;
	}
	
	static private String usage( String[] a ) {
		System.out.println(
				"Usage: [set|get|remove|increase|decrease|exists|equals|delete] <ent> <attr>[ / <attr> ...] [<values>...]\n"+
				"given: "+ Strings.toString( a, Strings.CSV ));
		return Shell.FAIL;
	}
	//String valueFromArray( String[] a, int offset) {return "";}
	static public String interpret( String[] sa ) {
		// interpret( ["increase", "device", "textSize", "4"] )
		audit.traceIn( "interpret", Strings.toString( sa, Strings.CSV ));
		String[] a = Strings.normalise( sa );
		String rc = Shell.SUCCESS;
		if (null != a && a.length > 2) {
			int i = 2;
			String cmd = a[ 0 ], entity = a[ 1 ], attribute = null;
			if (i<a.length) { // components? martin car / body / paint / colour red
				attribute = a[ i ];
				while (++i < a.length && a[ i ].equals( "/" ))
					attribute += ( "/"+ a[ i ]);
			}
			// [ "4", "+", "3" ] => "7" ] ???? at some point!
			String[] values = Strings.rejig( Strings.copyAfter( a, i-1 ), List.sep );
			String value = values[ 0 ];
			audit.debug( "values => ["+ Strings.toString( values,  Strings.DQCSV ) +"]" );
			
			Numeric n = new Numeric( entity, attribute );
			
			if (cmd.equals( "increase" )) {
				Float v = n.get( 0f );
				v += Float.valueOf( value );
				n.set( v );
			} else if (cmd.equals( "decrease" )) {
				Float v = n.get( 0f );
				v -= Float.valueOf( value );
				n.set( v );
			} else
				rc = usage( a );
		} else
			rc = usage( a );
		return audit.traceOut( rc );
}	}