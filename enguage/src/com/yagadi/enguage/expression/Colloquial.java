package com.yagadi.enguage.expression;

import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

class Stringses {
	private static String[][] incrStringsSpace( String[][] a ) {
		String[][] z = new String[ null == a ? 1 : a.length + 1 ][];
		if (a!=null) for (int i=0; i<a.length; i++) z[ i ] = a[ i ];
		return z;
	}
	public static String[][] append( String[][] a, String[] b) {
		a = incrStringsSpace( a );
		a[ a.length-1 ] = b;
		return a;
}	}

public class Colloquial {
	static private Audit audit = new Audit( "Colloquial" );
	
	private String[][] froms;
	private String[][] tos;

	public String add( String fs, String ts ) {
		froms = Stringses.append( froms, Variable.deref( Strings.fromString( fs )) );
		tos  =  Stringses.append( tos,   Variable.deref( Strings.fromString( ts )) );
		return "ok";
	}
	public Colloquial() {
		froms = new String[0][0];
		tos = new String[0][0];
	}
	// ---
	// this doesn't spot overlapping colloquia...
	// wandered lonely --> moved aimlessly
	// aimlessly as a cloud --> duff analogy
	// lonely as a cloud --> very lonely
	// I wandered lonely as a cloud --> I moved duff analogy (not as required: I moved aimlessly very lonely.)
	public String[] externalise( String[] a ) {
		for (int i=0; i<froms.length && i<tos.length; ++i)
			a = Strings.replace( a, froms[ i ], tos[ i ]); // "I have" to "I've"
		return a;
	}
	public String[] internalise( String[] a ) {
		for (int i=0; i<froms.length && i<tos.length; ++i)	
			a = Strings.replace( a, tos[ i ], froms[ i ]);
		return a;
	}
	static public String interpret( String[] a ) {
		if (null == a) return Shell.FAIL;
		//audit.traceIn( "interpret", Strings.toString( a, Strings.CSV ));
		if (a[ 0 ].equals("add") && a.length == 3) {
			if (Audit.isOn()) audit.audit("COLLOQUIALS: adding: "+ a[ 1 ] +"/"+ a[ 2 ]);
			// Ok add it...
			Reply.symmetric().add( Strings.trim( a[ 1 ], '"' ), Strings.trim( a[ 2 ], '"' ));
		} else
			audit.ERROR( "colloquialAdd(): unknown command: "+ Strings.toString( a, Strings.CSV ));
		//return audit.traceOut( Shell.SUCCESS );
		return Shell.SUCCESS;
	}
	public static void main( String[] args ) {
		String[] a = Strings.fromString( "This is a failure" );
		Colloquial c = new Colloquial();
		c.add( "This", "Hello" );
		c.add( "is a failure", "world" );
		a = c.externalise( a );
		System.out.println( Strings.toString( a, Strings.SPACED ));
		a = c.internalise( a );
		System.out.println( Strings.toString( a, Strings.SPACED ));
}	}
