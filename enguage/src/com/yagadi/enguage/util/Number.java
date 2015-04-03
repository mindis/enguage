package com.yagadi.enguage.util;

import com.yagadi.enguage.expression.Reply;

public class Number {
	/* a number is obtained from a string of characters, and can be:
	 * relative/absolute, exact/vague, positive/negative, e.g
	 * i need  /another 3/  cups of coffee
	 * i need /another few/ cups of coffee
	 * i need     /some/    cups of coffee
	 * i need       /a/     cups of coffee
	 * i need   /1 plus 2/  cups of coffee
	 */
	static private Audit audit = new Audit( "Number" );
	static public final String NotANumber = "NaN";
	
	static private boolean debugSwitch = false;
	
	// ===== LtoR Number parsing 
	/* {}=repeating 0..n, []=optional
	 * 
	 * numeral == digit{digit}[.digit{digit}]
	 *  postOp == ["all"] "squared" | "cubed"
	 *      op == "plus" | "minus" | "times" | "divided by" |
	 *            "multiplied by" | "times by" | "+" | "-" | "*" | "/"
	 *    expr == numeral {[postOp] [op expr]}
	 *      
	 * E.g. expr = 1 plus 2 squared plus 3 squared plus 4 all squared. 
	 */
	private int idx;
	private String op;
	private String nextOp;

	//retrieves an op from the array and adjusts idx appropriately
	private String getOp() {
		if (!nextOp.equals( "" )) {
			if (debugSwitch) audit.debug( "getting   SAVED op "+ nextOp );
			op = nextOp;
			nextOp = "";
		} else if (idx >= array.size() ){
			audit.ERROR( "getOp(): Reading of end of val buffer");
			return "";
		} else {
			op = array.get( idx++ );
			if (idx < array.size() && op.equals( "divided" ))
				op +=(" "+array.get( idx++ )); // "by"?
			if (debugSwitch) audit.debug( "getting NEXT op "+ op );
		}
		return op;
	}
	//retrieves a number from the array and adjusts idx appropriately
	private Float getNumber() {
		if (debugSwitch) audit.traceIn( "getNumber", "idx="+ idx +", array=["+array.toString( Strings.CSV )+"]");
		String sign="+";
		String got = array.get( idx );
		if (got.equals( "plus" )) {
			sign = "+";
			idx++;
		} else if (got.equals( "minus" )) {
			sign = "-";
			idx++;
		} else if (got.equals( "+" ) || got.equals( "-" )) {
			sign = got;
			idx++;
		}
		String number = array.get( idx++ );
		if (idx < array.size()) {
			if ( array.get( idx ).equals( "point" )) { number += "."; idx++; }
			// TODO: do this in a more productive way!!!
			while ( idx < array.size() && (
					array.get( idx ).equals( "0" ) ||
					array.get( idx ).equals( "1" ) ||
					array.get( idx ).equals( "2" ) ||
					array.get( idx ).equals( "3" ) ||
					array.get( idx ).equals( "4" ) ||
					array.get( idx ).equals( "5" ) ||
					array.get( idx ).equals( "6" ) ||
					array.get( idx ).equals( "7" ) ||
					array.get( idx ).equals( "8" ) ||
					array.get( idx ).equals( "9" )   )
					) {
				number += array.get( idx++ );
		}	}
		Float rc = Float.NaN;
		if (debugSwitch) audit.debug( "parsing:"+ sign +"/"+number +":" );
		try { rc =  Float.parseFloat( sign+number ); } catch (Exception e) {}
		if (debugSwitch) audit.traceOut( rc );
		return rc;
	}
	/* doPower( 3.0, [ "+", "2" ...]) => "3"
	 * doPower( 3.0, [ "squared", "*", "2" ...]) => "9"
	 */
	private Float doPower(Float value) {
		if (debugSwitch) audit.traceIn( "doPower", op +" ["+array.copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		if (value != Float.NaN) {
			// to process here we need an op and a value
			if (idx<array.size() || !nextOp.equals("")) {
				op = getOp();
				if (debugSwitch) audit.debug( "power:"+ op +":" );
				if (op.equals( "cubed" )) {
					if (debugSwitch) audit.debug( "cubing: "+ value );
					op = ""; // consumed!
					value = value * value * value;
				} else if (op.equals( "squared" )) {
					if (debugSwitch) audit.debug( "squaring: "+ value );
					op = ""; // consumed!
					value *= value;
				} else {
					if (debugSwitch) audit.debug( "saving (non-)power op: "+ op );
					nextOp = op;
		}	}	}
		if (debugSwitch) audit.traceOut( value );
		return value;
	}
	private Float doPower() {
		if (debugSwitch) audit.traceIn( "doPower", op +" ["+array.copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		// to process here we need an op and a value
		Float f = doPower( getNumber());
		if (debugSwitch) audit.traceOut( f );
		return f; 
	}
	/*
	 * product: restarts the product() process
	 * product( 3.0, [ "+", "2" ...]) => "3"
	 * product( 3.0, [ "*", "2", "+" ...]) => "6"
	 */
	/*
	 * Theres a bug here in that op and postOp should be dealt with in their own methods.
	 */
	private Float doProduct(Float value) {
		if (debugSwitch) audit.traceIn( "doProduct", op +" ["+ array.copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		if (value != Float.NaN) {
			// to process here we need an op and a value
			while( idx < array.size() ) { // must be at least two array items, e.g. ["*", "2", ...
				op = getOp();
				//if (localDebugSwitch) audit.debug( "prod op:"+ op +":" );
				if (op.equals( "times" ) || op.equals( "*" )) {
					//if (localDebugSwitch) audit.debug( "mult: "+ value +" * "+ array.get( idx ));
					op = ""; // consumed!
					value *= doPower();
				} else if (op.equals( "divided by" ) || op.equals( "/" )) {
					//if (localDebugSwitch) audit.debug( "divi: "+ value +" / "+ array.get( idx ));
					op = ""; // consumed!
					value /= doPower();
				//} else if (op.equals( "all" )) {
				//	op = ""; // consumed!
				//	value = doPower( value );
				} else {
					//if (localDebugSwitch) audit.debug( "saving (non-)prod op: "+ op );
					nextOp = op;
					break;
			}	}
			if (idx >= array.size() && !nextOp.equals("")){
				//if (localDebugSwitch) audit.debug( "doing prod trailing nextOp" );
				value = doPower( value );
		}	}

		if (debugSwitch) audit.traceOut( value );
		return value;
	}
	private Float doProduct() {
		if (debugSwitch) audit.traceIn( "doProduct", op +" ["+array.copyAfter( idx-1 ).toString(  Strings.CSV )+"]" );
		// to process here we need an op and a value
		Float f = doProduct( doPower() );
		if (debugSwitch) audit.traceOut( f );
		return f;
	}
	/*
	 * term([ "1", "+", "2" ]) => 3
	 * term([ "1", "+", "2", "*", "3" ]) => 7
	 */
	private Float doTerms() {
		if (debugSwitch) audit.traceIn( "doTerms", op +", ["+ array.copyAfter( idx-1 ).toString(  Strings.CSV )+"]" );
		Float value = doProduct();
		if (value != Float.NaN) {
			//if (localDebugSwitch) audit.debug( "initial term = "+ value );
			while (idx < array.size()) {
				op = getOp();
				//if (localDebugSwitch) audit.debug( "term op:"+ op +":" );
				if (op.equals( "plus" ) || op.equals( "+" )) {
					op = ""; // consumed!
					//if (localDebugSwitch) audit.debug( "Doing plus: ["+ array.copyAfter( idx-1 ).toString(  Strings.CSV )+"]" );
					value += doProduct();
				} else if (op.equals( "minus" ) || op.equals( "-" )) {
					op = ""; // consumed!
					value -= doProduct();
				} else if (op.equals( "all" )) {
					op = ""; // consumed!
					value = doProduct( value );
				} else {
					value = doProduct();
					//if (localDebugSwitch) audit.debug( "saving (non-)term op: "+ op );
					nextOp = op;
					break;
				}
				//if (localDebugSwitch) audit.debug( "intermediate value = "+ value );
			}
			if (!nextOp.equals("")) {
				//if (localDebugSwitch) audit.debug( "doing term trailing nextOp" );
				value = doProduct( value );
			}
			if (idx < array.size()) audit.ERROR( idx +" not end of array, on processing: "+ array.get( idx ));
		}
		if (debugSwitch) audit.traceOut( value );
		return value;
	}
	// ----------------
	public static String floatToString( Float f ) {
		String value;
		if (f == Float.NaN)
			value = Number.NotANumber;
		else {
			// 3.0 => "3" -- remove ".0"
			// 3.25 => "3 point 2 5" -- replace "." with "point", .nn should be spelled out
			int i;
			value = Float.toString( f );
			if (value.substring( value.length()-2 ).equals( ".0" ))
				value = value.substring( 0, value.length()-2 );
			if (-1 != (i = value.indexOf( "." )))
				value = value.substring( 0, i ) + " point " + Reply.spell( value.substring( i+1 )); 
		}
		return value;
	}
	// ----------------

	private Strings array = new Strings();
	public  Number  append( String s ) { array.add( s ); return this; }
	public  Number  remove( int i ) {
		if (i>=array.size())
			array = new Strings();
		else
			while (i-- > 0)
				array.remove( array.size() - 1 );
		return this;
	}

	public Number() {
		array = new Strings();
		idx = 0;
		op = nextOp = "";
	}

	public boolean relative = false;
	public boolean relative() { return relative; }
	public Number  relative( boolean b ) { relative = b; return this; }
	
	public boolean positive = true;
	public boolean positive() { return positive; }
	public Number  positive( boolean b ) { positive = b; return this; }
	
	public boolean exact = true;
	public boolean exact() { return exact; }
	public Number  exact( boolean b ) { exact = b; return this; }
	
	/* NB Size relates to the numbers of words representing
	 * the number. So "another three" is 2
	 */
	private int    size = 0;
	public  int    repSize() { return size; }
	public  Number repSize( int b ) { size = b; return this; }
	
	public String toString() { return array.toString( Strings.SPACED ); }
	public String valueOf() {
		//if (localDebugSwitch) audit.traceIn( "valueOf", "("+ array.toString() +")");
		String rc;
		if (array.size() == 0)
			rc = Number.NotANumber;
		else {
			idx = 0;
			try {
				rc = floatToString( doTerms());
				if (!rc.equals(Number.NotANumber))
					rc = (relative ? positive ? "+" + (exact ? "=" : "~") : "-" + (exact ? "=" : "~") : "") + rc;
			} catch (Exception nfe) {
				if (debugSwitch) audit.debug( "Number.valueOf():"+ nfe.toString());
				rc = Number.NotANumber;
		}	}
		//if (localDebugSwitch) audit.traceOut( rc ); // if (localDebugSwitch) audit.traceOut( tmp );
		return rc; // if (localDebugSwitch) audit.traceOut( tmp );
	}
/*	static private boolean xisPostOp( Strings s, int i ) {
 *		return (i+1<s.size() && (
 *				   (s.get( i ).equals(        "all" ) && s.get( i ).equals( "squared" ))
 *				|| (s.get( i ).equals(        "all" ) && s.get( i ).equals(   "cubed" )) ))
 *			||
 *		(i<s.size() && (
 *				s.get( i ).equals(   "cubed" ) || s.get( i ).equals( "/" )
 *			 || s.get( i ).equals( "squared" ) ));
 *	}
 *	static private boolean xisOp( Strings s, int i ) {
 *		return (i+1<s.size() && (
 *				   (s.get( i ).equals(    "divided" ) && s.get( i ).equals( "by" ))
 *				|| (s.get( i ).equals( "multiplied" ) && s.get( i ).equals( "by" ))
 *				|| (s.get( i ).equals(        "all" ) && s.get( i ).equals( "squared" ))
 *				|| (s.get( i ).equals(        "all" ) && s.get( i ).equals(   "cubed" )) ))
 *			||
 *		(i<s.size() && (
 *				s.get( i ).equals(    "plus" ) || s.get( i ).equals( "+" )
 *			 || s.get( i ).equals(   "minus" ) || s.get( i ).equals( "-" )
 *			 || s.get( i ).equals(   "times" ) || s.get( i ).equals( "*" )
 *			 || s.get( i ).equals(   "cubed" ) || s.get( i ).equals( "/" )
 *			 || s.get( i ).equals( "squared" ) ));
 *	}
 */
	// ===== getNumber(): a Number Factory
	static public int postOpLen( Strings s, int i ) {
		int rc = 0;
		if (i+1<s.size() && (
				   (s.get( i ).equals( "all" ) && s.get( i+1 ).equals( "squared" ))
				|| (s.get( i ).equals( "all" ) && s.get( i+1 ).equals(   "cubed" )) ))
			return 2;
		if (i<s.size() && (
				s.get( i ).equals(   "cubed" )
			 || s.get( i ).equals( "squared" ) ))
			return 1;
		return rc;
	}
	static private int opLen( Strings s, int i ) { // too simplistic?
		if (debugSwitch) audit.traceIn( "opLen", "s="+ s.toString() +", from="+ i );
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
		if (debugSwitch) audit.traceOut( rc );
		return rc;
	}
	static public boolean isNumeric( String s ) {
		float i = 0;
		try {
			i = Float.parseFloat( s );
		} catch (NumberFormatException nfe) {} //fail silently!
		return s.equals( "0" ) || 0 != i;
	}
	static public Number getNumber( Strings sa, int os ) {
		if (debugSwitch)
			audit.traceIn( "getNumber", (sa!=null ? sa.toString():"null")  +", from="+ os );
		Number number = new Number();
		int orig = os;
		String token = sa.get( os );
		if (token.equals("a") || token.equals("an")) {
			if (debugSwitch) audit.debug( "Numeric is a <x singular='singular'>" );
			number.append( "1" );
		}
		// pre-numeric
		if (token.equals("another")) {
			number.relative( true );
			number.positive( true );
			number.exact( true );
			number.append( "1" );
			if (++os<sa.size()) token = sa.get( os );
		}
		if (token.equals("some")) { // prob !singular
			number.exact( false );
			number.append( "3" );
			if (++os<sa.size()) token = sa.get( os );
		}
		if (token.equals("few")) { // prob !singular
			number.exact( false );
			number.append( "3" );
			if (++os<sa.size()) token = sa.get( os );
		}
		if (token.equals("couple") && 1+os>sa.size() && sa.get( ++os ).equals( "of" )) { // prob !singular
			number.exact( false );
			number.append( "2" );
			if (++os<sa.size()) token = sa.get( ++os );
		}
		if (token.equals( "pair" ) && 1+os<sa.size() && sa.get( ++os ).equals( "of" )) { // prob !singular
			number.exact( true );
			number.append( "2" );
			if (++os<sa.size()) token = sa.get( os );
		}
		if (isNumeric( token )) {
			// we've read a numeral, so....
			if (debugSwitch) audit.debug( "yes, "+ token +" is numeric" );
			number.exact( true );
			// "another" -> (+=) 1
			if (number.array.size() == 0) // != "another"
				number.append( token );
			else                    // ?= "another"
				number.array.replace( 0, token );
			os++;
			
			// ...read into the array a succession of ops and numerals 
			while (os<sa.size()) {
				int opLen, postOpLen;
				while (0 < (postOpLen = postOpLen( sa, os ))) { // ... all squared
					if (debugSwitch) audit.debug("postOpLen="+ postOpLen );
					for (int j=0; j<postOpLen; j++) {
						if (debugSwitch) audit.debug( "found POP:"+ sa.get( os+j ) +":poplen="+ postOpLen);
						number.append( sa.get( os+j ));
					}
					os += postOpLen;
				} // optional, so no break if not found
				if (0 < (opLen = opLen( sa, os ))) { // ... * 4
					int tmpOs = os; // remember where we started...
					if (debugSwitch) audit.debug("opLen="+ opLen );
					for (int j=0; j<opLen; j++) {
						if (debugSwitch) audit.debug( "found OP:"+ sa.get( os+j ) +":oplen="+ opLen);
						number.append( sa.get( os+j ));
					}
					os += opLen;
					// done op so now do a numeral..
					if (os<sa.size()) {
						String tmp = token = sa.get( os );
						if (isNumeric( tmp )) {
							if (debugSwitch) audit.debug( "found numeric: "+ tmp );
							number.append( tmp );
							if (++os<sa.size()) token = sa.get( os );
						} else {
							// if we don't find a number after op, remove op...
							// the number of children we have is 4 plus we have a fostered child.
							number.remove( opLen );
							os = tmpOs; // ...go back to whence we came
							break;
					}	}
				} else { // not found, so...
					if (debugSwitch) audit.debug( "moving on at "+ os );
					if (os<sa.size()) token = sa.get( os );
					break;  // if not found
				}
		}	}
		if (debugSwitch) audit.debug( "token is "+ token );
		if (os<sa.size() && token.equals("more")) {
			if (debugSwitch) audit.debug( "found MORE" );
			number.relative( true );
			number.positive( true );
			if (++os<sa.size()) token = sa.get( os );
		}
		if (os<sa.size() && token.equals("less")) {
			if (debugSwitch) audit.debug( "found LESS" );
			number.relative( true );
			number.positive( false );
			if (++os<sa.size()) token = sa.get( os );
		}
		if (debugSwitch)
			audit.traceOut( "number=["+ number.toString() +"], with a value of "+ number.valueOf() );
		number.repSize( os - orig );
		return number;
	}
	// =====
	public static void main( String[] args ) {
		//Audit.turnOn();
		Number n;
		//n = new Number( new Strings( "1 plus 2 all squared" ));
		//audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.size());
		audit.audit( "arithmetic test:");
		n = Number.getNumber( new Strings( "3 plus 2" ), 0 );
		audit.audit( "n is "+ n.toString() +" (5=="+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "3 squared" ), 0 );
		audit.audit( "n is "+ n.toString() +" (9=="+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "3 squared plus 2" ), 0 );
		audit.audit( "n is "+ n.toString() +" (11=="+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "3 plus 1 all squared" ), 0 );
		audit.audit( "n is "+ n.toString() +" (16=="+ n.valueOf() +") sz="+ n.repSize());

		audit.audit( "another test:");
		n = Number.getNumber( new Strings( "i need another cup of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "i need another  2 cups of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "i need another  3 cups of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "i need another  4 cups of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "i need another  5 cups of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "i need another  6 cups of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "i need another  7 cups of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "i need another  8 cups of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "i need another  9 cups of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "i need another 10 cups of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
// */
		audit.audit( "more/less test:");
		n = Number.getNumber( new Strings( "i need 6 more cups of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		n = Number.getNumber( new Strings( "i need 6 less cups of coffee" ), 2 );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		//n = new Number( new Strings( "this is rubbish" ));
		//audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +")");
}	}
