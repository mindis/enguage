package com.yagadi.enguage.expression;

import java.io.File;

import com.yagadi.enguage.sofa.List;
import com.yagadi.enguage.util.Strings;

public class Answer {
	//static private Audit audit = new Audit( "Answer" );
	
	// value can be "42"        -- a simple string
	// "martin/mother/computer" -- martin's mother's computer
	// "eggs+some milk+sugar"   -- a list, as retrieved from List.java
	// "hard disk+martin/mother/computer" -- combo - which takes primacy?
	
	private String value = ""; // as answers go: "" <==effectively==> null
	public  Answer value( String val ) { value = val; return this; }
	public  String value() { return value; }
	
	private char   separator = File.separatorChar;
	public  Answer separator( char ch ) { separator = ch; return this; }
	public  char   separator() { return separator; }
	
	private boolean forward = true; // matches default seps below
	public  Answer  forward( boolean fwd ) { forward = fwd; return this; }
	public  boolean forward() { return forward; }

	private String[] format;
	public  Answer   format( String[] fmt ) { format = fmt; return this; }
	public  String[] format() { return format; }
	
	public  String toString() {
		String[] list = Strings.fromString( value, List.sep() );
		for (int i=0; i<list.length; i++) {
			String[] items = Strings.fromString( list[ i ], separator );
			if (!forward) items = Strings.reverse( items );
			list[ i ] = Strings.toString( items, format );
		}
		return Strings.toString( list, Reply.andListFormat() ); 
	}
	public Answer() {
		String seps[] = { "", "'s ", "" }; // English-ism -- by default!
		forward( true ).format( seps );
	}
	public Answer( String val ) {
		this();
		value( val );
	}
	public Answer( String[] seps, boolean forward ) {
		this();
		format( seps ).forward( forward );
	}
	public static void main( String[] args ) {
		String seps[] = { "the ", " of the ", " of ", "" };
		Answer a = new Answer( seps, false );
		a.value( "martin/mother/computer" );
		// this is how it'll get used:
		String[] bps = { "Welcome", "to", "...", "." };
		bps = Strings.replace( bps, Strings.ellipsis, Strings.append( null, a.toString() ));
		System.out.println( Strings.toString( bps, Strings.SPACED ));
}	}
