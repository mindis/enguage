package com.yagadi.enguage.expression;

import java.io.File;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Answer {
	//static private       Audit  audit = new Audit( "Answer" );
	
	/* value can be
	 *   "42"                     -- a simple string
	 *   "martin/mother/computer" -- martin's mother's computer
	 *   "eggs+some milk+sugar"   -- a list, as retrieved from List.java
	 *   "hard disk+martin/mother/computer" -- combo - which takes primacy?
	 */
	private Strings values; // as answers go: "" <==effectively==> null
	public  Answer  values( Strings vals ) { values = vals; return this; }
	public  Answer  values( String  val  ) { values = new Strings(); values.add( val ); return this; }
	public  Answer  valuesAdd( String  val  ) { values.add( val ); return this; }
	public  Strings values() { return new Strings( values ); }

	public  boolean none() { return values.size() == 0 || (values.size() == 1 && values.get( 0 ).equals("") );}

	private char   separator = File.separatorChar;
	public  Answer separator( char ch ) { separator = ch; return this; }
	public  char   separator() { return separator; }
	
	private boolean forward = true; // matches default seps below
	public  Answer  forward( boolean fwd ) { forward = fwd; return this; }
	public  boolean forward() { return forward; }

	private Strings format;
	public  Answer  format( Strings fmt ) { format = fmt; return this; }
	public  Strings format() { return format; }

	public  String toString() {
		//audit.traceIn("toString","value='"+values+"'");
		Strings vals = values;
		for (int i=0; i<vals.size(); i++) {
			Strings val = new Strings( vals.get( i ), separator);
			// format each value as answer as before
			if (val.size() > 1 ) {
				if (!forward) val = val.reverse();
				vals.set( i, val.toString( format() ));
		}	}
		//return audit.traceOut( vals.toString( Reply.andListFormat() )); 
		return vals.toString( Reply.andListFormat() ); 
	}
	public Answer() {
		Strings seps = new Strings( "/'s/", '/' ); // English-ism -- by default!
		format( seps );
		forward( true );
		values( new Strings( "" ));
	}
	public Answer( Strings val ) {
		this();
		values( val );
	}
	public Answer( String val ) {
		this();
		values( new Strings( val ));
	}
	//public Answer( Strings seps, boolean forward ) {
	//	this();
	//	format( seps ).forward( forward );
	//}
	public static void main( String args[] ) {
		Audit.turnOn();
		String[] sa = { "the ", " of the ", " of ", "" };
		Strings seps = new Strings( sa );
		Answer a = new Answer().forward( false ).format( seps );
		a.values( new Strings( "martin/mother/computer&fred", '&' ));
		// this is how it'll get used:
		String[] sa2 = { "Welcome", "to", "...", "." };
		Strings bps = new Strings( sa2 );
		bps.replace( Strings.ellipsis, new Strings( a.toString()) );
		System.out.println( bps.toString( Strings.SPACED ));
}	}
