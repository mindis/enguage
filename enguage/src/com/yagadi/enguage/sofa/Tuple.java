package com.yagadi.enguage.sofa;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

/*
 * Class Tuple drives a List of comma separated values
 */
public class Tuple extends List {
	static private Audit audit = new Audit( "List" );
	public static final String NAME = "tuple";
	
	// member
	static protected String sep = ",";
	static public  void   sep( String s ) { sep = s; }
	static public  String sep() { return sep; }

	// constructors
	public Tuple( String e, String a ) { super( e, a ); }
	
	// methods
/*	public int      set( String[] vals ) {
		int orig = items().length;
		set( Strings.toString( vals, "", Strings.lineTerm, "" )); // e.g. "one\ntwo\nthree"
		return items().length - orig; // 0->1  => 1-0 == 1 -- change in n-items
	}
	public String[] items() { return Strings.fromLines( super.get() ); }
	
	@Override
	public String     get() { return Strings.toString( items(), "", sep, "" ); }
	public void       add( String newVal ) { set( Strings.toString( Strings.append( items(), newVal ), "", Strings.lineTerm, "" )); }
	
	public boolean equals( String[] vals ) { return Strings.equals(  items(), vals ); }
	@Override
	public boolean contains( String val ) { return Strings.contain( items(), val ); }
	public boolean contains( String[] vals ) { // contains ALL
		String[] items = items();
		for (String val : vals)
			if (!Strings.contain( items, val )) return false;
		return true;
	}
	public boolean removeFirst(      String val ) { return set( Strings.removeFirst(      items(), val )) < 0; }
	public boolean removeAll(        String val ) { return set( Strings.removeAll(        items(), val )) < 0; }
	public boolean removeAllMatched( String val ) { return set( Strings.removeAllMatched( items(), val )) < 0; }

	// statics
	static private String usage( String[] a ) {
		System.out.println(
				"Usage: [set|get|add|removeFirst|removeAll|exists|equals|delete] <ent> <attr>[ / <attr> ...] [<values>...]\n"+
				"given: "+ Strings.toString( a, Strings.CSV ));
		return Shell.FAIL;
	}
	*/
	static public String interpret( String[] sa ) {
		audit.audit( "in Tuple.interpret("+ Strings.toString( sa, Strings.CSV ) +")" );
		return List.interpret( sa );
	}
		/*
		// sa might be: [ "add", "_user", "needs", "3", ",", "beers", "&", "some crisps" ]
		// sa comes from SOFA -- not from intention!
		//audit.traceIn( "interpret", Strings.toString( sa, Strings.CSV ));
		String[] a = Strings.normalise( sa );
		String rc = Shell.SUCCESS;
		if (null != a && a.length > 2) {
			int i = 2;
			String cmd = a[ 0 ], entity = a[ 1 ], value = null, attribute = null;
			if (i<a.length) { // components? martin car / body / paint / colour red
				attribute = a[ i ];
				while (++i < a.length && a[ i ].equals( "/" ))
					attribute += ( "/"+ a[ i ]);
			}
			//audit.debug( "entity => '"+ entity +"'" );
			//audit.debug( "attr => '"+ attribute +"'" );
			// [ "3", ",", "beers", "&", "some crisps" ] => [ "3 beer", "some crisps" ]
			String[] values = Strings.normalise( Strings.copyAfter( a, i-1 ), sep );
			//audit.debug( "values => ["+ Strings.toString( values,  Strings.DQCSV ) +"]" );
			
			Tuple m = new Tuple( entity, attribute ); //attribute needs to be composite: dir dir dir file values
			if (cmd.equals( "set" )) {
				for (String v : values )
					m.set( v );
			} else if (cmd.equals( "add" )) {
				for (String v : values ) 
					if (!m.contains( v )) // prevent duplicate values
						m.add( v );
			} else if (null == value && cmd.equals( "get" )) {
				rc = m.get();
			} else if (cmd.equals( "removeFirst" )) {
				for (String v : values )
					if (m.removeFirst( v )) rc = Shell.FAIL;
			} else if (cmd.equals( "removeAll" )) {
				rc = Shell.FAIL;
				for (String v : values)
					if ( v.length() > Reply.referencers()[ 0 ].length() &&
							v.substring( 0, Reply.referencers()[ 0 ].length() ).equals( Reply.referencers()[ 0 ])) {
						if (m.removeAllMatched( v.substring( Reply.referencers()[ 0 ].length() + 1 /* space * /) ))
							rc = Shell.SUCCESS;
					} else {
						if (m.removeAll( v ))
							rc = Shell.SUCCESS;
					}
			} else if (cmd.equals( "exists" )) {
				rc = (0 == values.length) ?
					m.exists() ? Shell.SUCCESS : Shell.FAIL :
					m.contains( values ) ? Shell.SUCCESS : Shell.FAIL;
			} else if (cmd.equals( "equals" )) {
				rc = m.equals( values ) ? Shell.SUCCESS : Shell.FAIL;
			} else if (cmd.equals( "delete" )) {
				m.ignore();
			} else
				rc = usage( a );
		} else
			rc = usage( a );
		//audit.traceOut( rc );
		return rc;
	*/
	public static void main( String[] args ) {
		Overlay.Set( Overlay.Get());
		String rc = Overlay.autoAttach();
		if (!rc.equals( "" ))
			System.out.println( "Ouch!" );
		else
			new ValuesTest( args ).run();
}	}