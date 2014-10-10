package com.yagadi.enguage.sofa;

import java.io.File;
import com.yagadi.enguage.util.*;

class ValuesTest extends Shell {
	ValuesTest( String[] args ) { super( "ValuesTes", args ); }
	public String interpret( String[] a ) { return Value.interpret( a ); }
}

public class Value {
	static private Audit audit = new Audit( "Value" );
	public static final String NAME = "value";
	
	protected String ent, attr;

	// constructor
	public Value( String e, String a ) {
		ent  = e;
		attr = a;
	}

	// statics
	static public String name(  String entity, String attr, String rw ) { return Overlay.fsname( entity +"/"+ attr, rw ); }
	
	// members
	// TODO: cache items[]? Lock file - this code is not suitable for IPC? Exists in constructor?
	// set() methods return change in number of items
	public boolean exists() {   return Filesystem.exists(        name( ent, attr, Overlay.MODE_READ )); }
	public void    set( String val ) { Filesystem.stringToFile(  name( ent, attr, Overlay.MODE_WRITE ), val ); }
	public void    unset() {           Filesystem.destroyEntity( name( ent, attr, Overlay.MODE_WRITE )); }
	public String  get() {
		// stringFromFile() currently returns null if non-existent :/
		String rc = Filesystem.stringFromFile( name( ent, attr, Overlay.MODE_READ ) );
		return rc == null ? "" : rc;
	}
	
	public boolean equals( String val ) { return get().equals( val ); }
	public boolean contains( String val ) { return get().contains( val ); }
	
	// this works..
	private static final String marker = "this is a marker file";
	public void  ignore() {
		audit.debug( "In Ignore()" );
		if (Filesystem.exists( name( ent, attr, Overlay.MODE_READ ))) {
			String writeName = name( ent, attr, Overlay.MODE_WRITE );
			String deleteName = Entity.deleteName( writeName );
			if ( Filesystem.exists( writeName )) { // rename
				File oldFile = new File( writeName ),
				     newFile = new File( deleteName );
				audit.debug( "Moving "+ oldFile.toString() +" to "+ deleteName );
				oldFile.renameTo( newFile );
			} else { // create
				audit.debug( "creating marker file "+ deleteName );
				Filesystem.stringToFile( deleteName, marker );
	}	}	}
	public void restore() {
		String writeName = name( ent, attr, Overlay.MODE_WRITE ); // if not overlayed - simply delete!?!
		String deletedName = Entity.deleteName( writeName );
		File deletedFile = new File( deletedName );
		String content = Filesystem.stringFromFile( deletedName );
		if (content != null && content.equals( marker )) {
			audit.debug( "deleting marker file "+ deletedFile.toString());
			deletedFile.delete();
		} else {
		    File restoredFile = new File( writeName );
			audit.debug( "Moving "+ deletedFile.toString() +" to "+ restoredFile.toString());
			deletedFile.renameTo( restoredFile );
	}	}
	
	static private String usage( String[] a ) {
		System.out.println(
				"Usage: [set|get|add|removeFirst|removeAll|exists|equals|delete] <ent> <attr>[ / <attr> ...] [<values>...]\n"+
				"given: "+ Strings.toString( a, Strings.CSV ));
		return Shell.FAIL;
	}
	static public String interpret( String[] sa ) {
		// sa might be: [ "add", "_user", "need", "some", "beer", "+", "some crisps" ]
		audit.traceIn( "interpret", Strings.toString( sa, Strings.CSV ));
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
			// [ "some", "beer", "+", "some crisps" ] => "some beer", "some crisps" ]
			//String[] values = Strings.rejig( Strings.copyAfter( a, i-1 ), sep );
			String val = Strings.toString( Strings.copyAfter( a, i-1 ), Strings.SPACED ); 
			//audit.debug( "values => ["+ Strings.toString( values,  Strings.DQCSV ) +"]" );
			
			Value m = new Value( entity, attribute ); //attribute needs to be composite: dir dir dir file values
			if (cmd.equals( "set" )) {
				m.set( val );
			} else if (null == value && cmd.equals( "get" )) {
				rc = m.get();
			} else if (cmd.equals( "unset" )) {
				rc = Shell.SUCCESS;
				m.unset();
			} else if (cmd.equals( "exists" )) {
				rc = (null==val || 0 == val.length()) ?
					m.exists() ? Shell.SUCCESS : Shell.FAIL :
					m.contains( val ) ? Shell.SUCCESS : Shell.FAIL;
			} else if (cmd.equals( "equals" )) {
				rc = m.equals( val ) ? Shell.SUCCESS : Shell.FAIL;
			} else if (cmd.equals( "delete" )) {
				m.ignore();
			} else if (cmd.equals( "undelete" )) {
				m.restore();
			} else
				rc = usage( a );
		} else
			rc = usage( a );
		audit.traceOut( rc );
		return rc;
	}
	public static void main( String[] args ) {
		Overlay.Set( Overlay.Get());
		String rc = Overlay.autoAttach();
		if (!rc.equals( "" ))
			System.out.println( "Ouch!" );
		else
			new ValuesTest( args ).run();
}	}