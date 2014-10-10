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

public class Colloquials {
	static private Audit audit = new Audit( "Colloquials" );
	
	String[][] from;
	String[][] to;

	public String add( String fs, String ts ) {
		String[] a = Strings.fromString( fs );
		a = Variable.deref( a );
		from = Stringses.append( from, a );
		a = Strings.fromString( ts );
		a = Variable.deref( a );
		to = Stringses.append( to, a );
		return "ok";
	}
	public Colloquials() {
		from = null;
		to = null;
	}
	// ---
	// this doesn't spot overlappying colloquials...
	// wandered lonely --> moved aimlessly
	// aimlessly as a cloud --> duff analogy
	// lonely as a clous --> very lonely
	// I wandered lonely as a cloud --> I moved duff analogy (not as required: I moved aimlessly very lonely.)
	public String[] apply( String[] a ) {
		if (from != null && to != null) {
			if (from.length != to.length)
				audit.ERROR( "from ("+ from.length +") != to( "+ to.length +" )" );
			for (int i=0; i<from.length && i<to.length; ++i)
				a = Strings.replace( a, from[ i ], to[ i ]);
		}
		return a;
	}
	public String[] disapply( String[] a ) {
		if (from != null && to != null)
			for (int i=0; i<from.length && i<to.length; ++i)	
				a = Strings.replace( a, to[ i ], from[ i ]);
		return a;
	}
	public void xdumpToLog( String name ) {
		if (null != from) for( int i=0; i < from.length; i++)
			audit.audit("'"+ Strings.toString( to[ i ],  Strings.SPACED ) +"' means '"+ Strings.toString( from[ i ],  Strings.SPACED ) +"'");
		else
			audit.ERROR( name +" is null" );
	}
	static public String interpret( String[] a ) {
		if (null == a) return Shell.FAIL;
		audit.traceIn( "interpret", Strings.toString( a, Strings.CSV ));
		if (a[ 0 ].equals("add") && a.length == 3) {
			if (Audit.isOn()) audit.audit("COLLOQUIALS: adding: "+ a[ 1 ] +"/"+ a[ 2 ]);
			// Ok add it...
			Reply.both().add( Strings.trim( a[ 1 ], '"' ), Strings.trim( a[ 2 ], '"' ));
		} else
			audit.ERROR( "colloquialAdd(): unknown command: "+ Strings.toString( a, Strings.CSV ));
		return audit.traceOut( Shell.SUCCESS );
	}
	public static void main( String[] args ) {
		String[] a = Strings.fromString( "This is a failure" );
		Colloquials c = new Colloquials();
		c.add( "This", "Hello" );
		c.add( "is a failure", "world" );
		a = c.apply( a );
		System.out.println( Strings.toString( a, Strings.SPACED ));
		a = c.disapply( a );
		System.out.println( Strings.toString( a, Strings.SPACED ));
}	}
