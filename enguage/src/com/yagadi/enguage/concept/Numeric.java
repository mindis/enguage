package com.yagadi.enguage.concept;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.util.Number;

public class Numeric {
	static private Audit audit = new Audit( "Numeric" );
	
	static private int opLen( Strings s, int i ) { // too simplistic?
		audit.traceIn( "opLen", "s="+ s.toString() +", from="+ i );
		int rc = 0;
		if (i+1<s.size()) {
			String op1 = s.get( i ), op2 = s.get( i + 1 );
			if ((op1.equals( "times"      ) && op2.equals( "by" ))
			 || (op1.equals( "multiplied" ) && op2.equals( "by" ))
			 || (op1.equals( "divided"    ) && op2.equals( "by" )) ) rc = 2;
		}
		if (i<s.size()) {
			String op = s.get( i );
			if (op.equals( "+" ) || op.equals( "plus"  ) ||
			    op.equals( "-" ) || op.equals( "minus" ) ||
			    op.equals( "*" ) || op.equals( "times" ) ||
			    op.equals( "/" )                               ) rc = 1;
		}
		return audit.traceOut( rc );
	}

	private Number number = new Number();
	public  Number number() { return number; }
	
	public  boolean positive() { return number.positive(); }
	public  Numeric positive( boolean b ) { number.positive( b ); return this; }

	public  boolean exact() { return number.exact(); }
	public  Numeric exact( boolean b ) { number.exact( b ); return this; }

	public  int     size() { return number.repSize(); }
	//public  Numeric size( int i ) { number.repSize( i ); return this; }
	
	private  Numeric append( String s ) { number.append( s ); return this; }

	// +=6 => relative, 6, +6, -6 => absolute
	public boolean relative() { return number.relative(); }
	public Numeric relative( boolean b ) { number.relative( b ); return this; }
	
/*	static private String toAbs( String s ) {
		return s.length() > 2 && s.charAt( 1 ) == '=' ?
				(s.charAt(0)=='-'?"-":"") + s.substring( 2 ) : s;
	}
	static private boolean isRel( String s ) { // != !isAbs()
		if (s.length() <= 2) return false;
		if (s.charAt( 0 ) != '+' && s.charAt( 0 ) != '!') return false;
		if (s.charAt( 1 ) != '=') return false;
		if (s.substring( 2 ).equals( "0" ))return true;
		try {Integer.valueOf( s.substring( 2 ) );} catch (Exception e) {return false;}
		return true;
	} // */
	static private boolean isNumeric( String s ) {
		float i = 0;
		try {
			i = Float.parseFloat( s );
		} catch (NumberFormatException nfe) {} //fail silently!
		return s.equals( "0" ) || 0 != i;
	}
	
	/* this is currently an integer but should be a numerator/denominator
	 * and other numerical dimensions.
	 */
	
	public String toString() { return number.toString(); }
	public String  valueOf() { return number.valueOf();  }

	/* areNumeric() identifies how many items in the array, from the index are numeric
	 *   [..., "68",    "guns", ...]         => 1 //  9
	 *   [..., "17", "*",  "4", "guns", ...] => 3 //  9
	 *   [..., "another", "68", "guns", ...] => 2 // +6
	 *   [..., "68",    "more", "guns", ...] => 2 // +6
	 *   [..., "some",  "guns", ...]         => 1 // <undefined few|many>
	 *   [..., "some",   "jam", ...]         => 0 
	 *   [..., "a",      "gun", ...]         => 1 // 1
	 * TODO: these can be hardcoded for now but need to be specified somewhere somehow!
	 * 
	 * This MUST match eval()!
	 */
	public Numeric( Strings sa, int from ) {
		// /*
		if (sa!=null && from < sa.size() )
			audit.traceIn( "ctor", "["+ sa.toString( Strings.CSV ) +"], from="+ from );
		else
			audit.traceIn( "ctor", "null or "+ from +" >= "+ sa.size() );
		// */
		int posn = from;
		String token = sa.get( posn );
		if (token.equals("a") || token.equals("a")) {
			audit.debug( "Numeric is a <x singular='singular'>" );
			//size( 1 + (posn - from));
			append( "1" );
		} else {
			// pre-numeric
			if (token.equals("another")) {
				relative( true );
				positive( true );
				append( "1" );
				if (++posn<sa.size()) token = sa.get( posn );
			}
			if (token.equals("some")) { // prob !singular
				exact( false );
				if (++posn<sa.size()) token = sa.get( posn );
			}
			if (isNumeric( token )) {
				audit.debug( "yes, "+ token +" is numeric" );
				exact( true );
				append( token );
				posn++;
				
				while (posn<sa.size()) {
					int opLen = opLen( sa, posn );
					audit.audit("opLen="+ opLen );
					if (0 < opLen) { // ... * 4
						for (int j=0; j<opLen; j++) {
							audit.debug( "found OP:"+ sa.get( posn+j ) +":oplen="+ opLen);
							append( sa.get( posn+j ));
						}
						posn += opLen;
					} else
						break;
					if (posn<sa.size()) {
						String tmp = token = sa.get( posn );
						if (isNumeric( tmp )) {
							audit.audit( "found numeric: "+ tmp );
							append( tmp );
							posn++;
						} else
							break;
			}	}	}
			if (posn<sa.size() && token.equals("more")) {
				relative( true );
				positive( true );
				if (++posn<sa.size()) token = sa.get( posn );
			}
			if (posn<sa.size() && token.equals("less")) {
				relative( true );
				positive( false );
				if (++posn<sa.size()) token = sa.get( posn );
		}	}
		audit.traceOut( "value="+ number().valueOf() );
	}
	public static void main( String[] args ) {
		Audit.turnOn(); //main()
		Strings sa = new Strings( "what is 1 + 2");
		Numeric n = new Numeric( sa, 2 );
		audit.audit( "n is "+ n.toString());
		audit.audit( "n is "+ n.valueOf());
}	}
