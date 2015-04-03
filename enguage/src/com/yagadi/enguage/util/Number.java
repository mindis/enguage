package com.yagadi.enguage.util;

import java.util.ListIterator;

import com.yagadi.enguage.Enguage;
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
	
	static private boolean debug = Enguage.numericDebugging;
	
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
			if (debug) audit.debug( "getting   SAVED op "+ nextOp );
			op = nextOp;
			nextOp = "";
		} else if (idx >= representamen.size() ){
			audit.ERROR( "getOp(): Reading of end of val buffer");
			return "";
		} else {
			op = representamen.get( idx++ );
			if (idx < representamen.size() && op.equals( "divided" ))
				op +=(" "+representamen.get( idx++ )); // "by"?
			if (debug) audit.debug( "getting NEXT op "+ op );
		}
		return op;
	}
	//retrieves a number from the array and adjusts idx appropriately
	private Float getNumber() {
		if (debug) audit.traceIn( "getNumber", "idx="+ idx +", array=["+representamen.toString( Strings.CSV )+"]");
		String sign="+";
		String got = representamen.get( idx );
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
		String number = representamen.get( idx++ );
		if (idx < representamen.size()) {
			if ( representamen.get( idx ).equals( "point" )) { number += "."; idx++; }
			// TODO: do this in a more productive way!!!
			while ( idx < representamen.size() && (
					representamen.get( idx ).equals( "0" ) ||
					representamen.get( idx ).equals( "1" ) ||
					representamen.get( idx ).equals( "2" ) ||
					representamen.get( idx ).equals( "3" ) ||
					representamen.get( idx ).equals( "4" ) ||
					representamen.get( idx ).equals( "5" ) ||
					representamen.get( idx ).equals( "6" ) ||
					representamen.get( idx ).equals( "7" ) ||
					representamen.get( idx ).equals( "8" ) ||
					representamen.get( idx ).equals( "9" )   )
					) {
				number += representamen.get( idx++ );
		}	}
		Float rc = Float.NaN;
		if (debug) audit.debug( "parsing:"+ sign +"/"+number +":" );
		try { rc =  Float.parseFloat( sign+number ); } catch (Exception e) {}
		if (debug) audit.traceOut( rc );
		return rc;
	}
	/* doPower( 3.0, [ "+", "2" ...]) => "3"
	 * doPower( 3.0, [ "squared", "*", "2" ...]) => "9"
	 */
	private Float doPower(Float value) {
		if (debug) audit.traceIn( "doPower", op +" ["+representamen.copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		if (value != Float.NaN) {
			// to process here we need an op and a value
			if (idx<representamen.size() || !nextOp.equals("")) {
				op = getOp();
				if (debug) audit.debug( "power:"+ op +":" );
				if (op.equals( "cubed" )) {
					if (debug) audit.debug( "cubing: "+ value );
					op = ""; // consumed!
					value = value * value * value;
				} else if (op.equals( "squared" )) {
					if (debug) audit.debug( "squaring: "+ value );
					op = ""; // consumed!
					value *= value;
				} else {
					if (debug) audit.debug( "saving (non-)power op: "+ op );
					nextOp = op;
		}	}	}
		if (debug) audit.traceOut( value );
		return value;
	}
	private Float doPower() {
		if (debug) audit.traceIn( "doPower", op +" ["+representamen.copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		// to process here we need an op and a value
		Float f = doPower( getNumber());
		if (debug) audit.traceOut( f );
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
		if (debug) audit.traceIn( "doProduct", op +" ["+ representamen.copyAfter( idx-1 ).toString( Strings.CSV )+"]" );
		if (value != Float.NaN) {
			// to process here we need an op and a value
			while( idx < representamen.size() ) { // must be at least two array items, e.g. ["*", "2", ...
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
			if (idx >= representamen.size() && !nextOp.equals("")){
				//if (localDebugSwitch) audit.debug( "doing prod trailing nextOp" );
				value = doPower( value );
		}	}

		if (debug) audit.traceOut( value );
		return value;
	}
	private Float doProduct() {
		if (debug) audit.traceIn( "doProduct", op +" ["+representamen.copyAfter( idx-1 ).toString(  Strings.CSV )+"]" );
		// to process here we need an op and a value
		Float f = doProduct( doPower() );
		if (debug) audit.traceOut( f );
		return f;
	}
	/*
	 * term([ "1", "+", "2" ]) => 3
	 * term([ "1", "+", "2", "*", "3" ]) => 7
	 */
	private Float doTerms() {
		if (debug) audit.traceIn( "doTerms", op +", ["+ representamen.copyAfter( idx-1 ).toString(  Strings.CSV )+"]" );
		Float value = doProduct();
		if (value != Float.NaN) {
			//if (localDebugSwitch) audit.debug( "initial term = "+ value );
			while (idx < representamen.size()) {
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
			if (idx < representamen.size()) audit.ERROR( idx +" not end of array, on processing: "+ representamen.get( idx ));
		}
		if (debug) audit.traceOut( value );
		return value;
	}
	// ----------------
	public static String floatToString( Float f ) {
		if (debug) audit.traceIn( "floatToSting", f.toString() );
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
		if (debug) audit.traceOut( value );
		return value;
	}
	// ----------------

	private Strings representamen = new Strings();
	public  Strings array() { return representamen; }
	public  Number  append( String s ) {
		representamen.add( s );
		return this;
	}
	public  Number  remove( int i ) {
		if (i>=representamen.size())
			representamen = new Strings();
		else
			while (i-- > 0)
				representamen.remove( representamen.size() - 1 );
		return this;
	}
	/* NB Size relates to the numbers of words representing
	 * the number. So "another three" is 2
	 */
	public  int    repSize() { return representamen.size(); }
	
	public Number() {
		representamen = new Strings();
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
	
	public String toString() { return representamen.toString( Strings.SPACED ); }
	public String valueOf() {
		if (debug) audit.traceIn( "valueOf", "("+ representamen.toString() +")");
		String rc;
		if (representamen.size() == 0) {
			if (debug) audit.debug( "Number.valueOf(): rep size==0" );
			rc = Number.NotANumber;
		} else if (representamen.equals( new Strings( "some" )))
			rc = "some";
		else if (representamen.equals( new Strings( "a few" )))
			rc = "a few";
		else if (representamen.equals( new Strings( "a couple" )))
			rc = "a couple";
		else {
			idx = 0;
			try {
				rc = floatToString( doTerms());
				if (!rc.equals(Number.NotANumber))
					rc = (relative ? positive ? "+" + (exact ? "=" : "~") : "-" + (exact ? "=" : "~") : "") + rc;
				else
					if (debug) audit.ERROR( "Number.valueOf(): floatToString() returns NaN" );
				
			} catch (Exception nfe) {
				if (debug) audit.ERROR( "Number.valueOf():"+ nfe.toString());
				rc = Number.NotANumber;
		}	}
		if (debug) audit.traceOut( rc ); // if (localDebugSwitch) audit.traceOut( tmp );
		return rc; // if (localDebugSwitch) audit.traceOut( tmp );
	}

	// ===== getNumber(): a Number Factory
	static private int postOpLen( ListIterator<String> si ) {
		if (debug) audit.traceIn( "postOpLen", "");
		if (si.hasNext()) {
			String s = si.next();
			if (debug) audit.debug( "just read: "+ s );
			if (s.equals( "all" ) && si.hasNext()) {
				s = si.next();
				if (debug) audit.debug( "just read: "+ s );
				if (s.equals( "squared" ) || s.equals( "cubed" )) {
					si.previous();
					si.previous();
					if (debug) audit.traceOut( 2 );
					return 2;
			}	}
			if (	s.equals(   "cubed" )
				 || s.equals( "squared" ) ) {
				si.previous();
				if (debug) audit.traceOut( 1 ); 
				return 1;
			}
			// non of these -- so put it back
			if (debug) audit.audit( "Putting back "+ s );
			si.previous();
		}
		if (debug) audit.traceOut( 0 ); 
		return 0;
	}
	static private int opLen( ListIterator<String> si ) { // too simplistic?
		if (debug) audit.traceIn( "opLen", "" );
		if (si.hasNext()) {
			String op = si.next();
			if (debug) audit.debug( "--found op:"+ op );
			if (   op.equals( "times"      )
			    || op.equals( "multiplied" ) 
			    || op.equals( "divided"    )) { 
				if (si.hasNext()) {
					op = si.next();
					if (debug) audit.debug( "found Op:"+ op );
					if (op.equals( "by" )) {
						si.previous();
						if (debug) audit.traceOut( 2 );
						return 2;
					}
					// none of these so put it back
					si.previous();
				}
			} else if (
					op.equals( "+" ) || op.equals( "plus"  ) ||
				    op.equals( "-" ) || op.equals( "minus" ) ||
				    op.equals( "*" ) || op.equals( "times" ) ||
				    op.equals( "/" )                            )
			{
				if (debug) audit.debug( "found op:"+ op );
				si.previous();
				if (debug) audit.traceOut( 1 );
				return 1;
			}
			// none of these so put it back
			si.previous();
		}
		if (debug) audit.traceOut( 0 );
		return 0;
	}
	static public boolean isNumeric( String s ) {
		float i = 0;
		try {
			i = Float.parseFloat( s );
		} catch (NumberFormatException nfe) {} //fail silently!
		return s.equals( "0" ) || 0 != i;
	}
	static public Number getNumber( ListIterator<String> si ) {
		/* this could do with tidying...!
		 * this is a tree-like structure.
		 */
		if (debug)
			audit.traceIn( "getNumber", (si!=null ? si.hasNext() ? "stream":"empty":"null") );
		Number number = new Number();
		String token = si.next();
		if (debug) audit.debug( "first token is "+ token );
		if (token.equals("a") || token.equals("an")) {
			if (debug) audit.debug( "Numeric is a <x singular='singular'>" );
			number.relative( false );
			number.positive( true );
			number.exact( true );
			number.append( "1" );
			if (si.hasNext()) token = si.next();
		}
		// pre-numeric
		/*
		 * 
		 */
		if (token.equals("another")) {
			number.relative( true );
			number.positive( true );
			number.exact( true );
			number.append( "1" );
			if (si.hasNext()) token = si.next();
		}
		if (token.equals("some")) { // prob !singular
			number.exact( false );
			number.append( "some" );
			if (si.hasNext()) token = si.next();
		}
		if (token.equals("few")) { // prob !singular
			number.exact( false );
			number.append( "3" );
			if (si.hasNext()) {
				token = si.next();
		}	}
		if (token.equals("couple")) {
			if (si.hasNext()) {
				token = si.next();
				if (token.equals("of" )) { // prob !singular
					number.exact( false );
					number.append( "2" );
					if (si.hasNext()) token = si.next();
				}
			} else
				si.previous();
		}
/*		if (token.equals( "pair" ) && 1+os<sa.size() && sa.get( ++os ).equals( "of" )) { // prob !singular
			number.exact( true );
			number.append( "2" );
			if (si.hasNext()) {
				token = si.next();
			}
		}*/
		if (isNumeric( token )) {
			// we've read a numeral, so....
			if (debug) audit.debug( "yes, "+ token +" is numeric" );
			number.exact( true );
			// "another" -> (+=) 1
			if (number.representamen.size() == 0) {// != "another"
				number.append( token );
			} else                    // ?= "another"
				number.representamen.replace( 0, token );
			
			// ...read into the array a succession of ops and numerals 
			while (si.hasNext()) {
				if (debug) audit.debug( "in list reading terms" );
				int opLen, postOpLen;
				while (0 < (postOpLen = postOpLen( si ))) { // ... all squared
					if (debug) audit.debug("postOpLen="+ postOpLen );
					for (int j=0; j<postOpLen; j++) {
						if (si.hasNext()) {
							String s = si.next();
							if (debug) audit.debug( "found POP:"+ s );
							number.append( s );
					}	}
					//os += postOpLen;
				} // optional, so no break if not found
				if (0 < (opLen = opLen( si ))) { // ... * 4
					//int tmpOs = os; // remember where we started...
					if (debug) audit.debug("opLen="+ opLen );
					for (int j=0; j<opLen; j++) {
						if (si.hasNext()) {
							String s = si.next();
							if (debug) audit.debug( "found OP:"+ s );
							number.append( s );
					}	}
					//os += opLen;
					// done op so now do a numeral..
					if (si.hasNext()) {
						String tmp = token = si.next();
						if (isNumeric( tmp )) {
							if (debug) audit.debug( "found numeric: "+ tmp );
							number.append( tmp );
							//os ++;
							//if (si.hasNext()) token = si.next();
						} else {
							// if we don't find a number after op, remove op...
							// the number of children we have is 4 plus we have a fostered child.
							number.remove( opLen );
							//os = tmpOs; // ...go back to whence we came
							break;
					}	}
				} else { // not found, so...
					//if (debugSwitch) audit.debug( "moving on at "+ os );
					if (si.hasNext()) token = si.next();
					break;  // if not found
				}
		}	}
		if (debug) audit.audit( ">>>token is "+ token );
		if (si.hasNext() && token.equals("more")) {
			if (debug) audit.debug( "found MORE" );
			number.relative( true );
			number.positive( true );
			if (si.hasNext()) token = si.next();
		} else if (si.hasNext() && token.equals("less")) {
			if (debug) audit.debug( "found LESS" );
			number.relative( true );
			number.positive( false );
			if (si.hasNext()) token = si.next();
		} else { //not more or less, put it back
			si.previous();
		}
		if (debug) {
			String s1 = number.valueOf(), s2 =number.toString();
			audit.traceOut( "number=["+ s2 +"], with a value of "+ s1 );
		}
		return number;
	}
	// =====
	private static void numberTest( String term, String ans ) {
		ListIterator<String> si = new Strings( term ).listIterator();
		Number n = Number.getNumber( si );
		audit.audit( "n is '"+ n.toString() +"' ("+ ans +"=="+ n.valueOf() +") sz="+ n.repSize() +"("+ (new Strings( term ).size())+")");
	}
	private static void anotherTest( ListIterator<String> si ) {
		si.next(); si.next(); // i need
		Number n = Number.getNumber( si );
		audit.audit( "n is "+ n.toString() +" ("+ n.array() +") sz="+ n.repSize());
		if (si.hasNext()) audit.audit( "number go tis "+n.array()+", nxt token is "+ si.next());
	}
	public static void main( String[] args ) {
		Audit.turnOn();
		//debugSwitch = true;
		
		//audit.audit( "3.0 -> "+ floatToString( 3.0f ));
		//audit.audit( "3.25 -> "+ floatToString( 3.25f ));

		Number n;
		//n = new Number( new Strings( "1 plus 2 all squared" ));
		//audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.size());
		ListIterator<String> si;
		
		audit.audit( "arithmetic test:");
		numberTest( "3 plus 2",              "5" );
		numberTest( "3 squared",             "9" );
		numberTest( "3 squared plus 2",     "11" );
		numberTest( "3 plus 1 all squared", "16" );
		
		audit.audit( "another test:");
		anotherTest( new Strings("i need another cup of coffee").listIterator());
		anotherTest( new Strings("i need another  2 cups of coffee").listIterator());
		anotherTest( new Strings("i need another  3 cups of coffee").listIterator());
		anotherTest( new Strings("i need another  4 cups of coffee").listIterator());
		anotherTest( new Strings("i need another  5 cups of coffee").listIterator());
		anotherTest( new Strings("i need another  6 cups of coffee").listIterator());
		anotherTest( new Strings("i need another  7 cups of coffee").listIterator());
		anotherTest( new Strings("i need another  8 cups of coffee").listIterator());
		anotherTest( new Strings("i need another  9 cups of coffee").listIterator());
		anotherTest( new Strings("i need another 10 cups of coffee").listIterator());
		
		anotherTest( new Strings("i need some coffee").listIterator());
		
		audit.audit( "more/less test:");
		si = new Strings("i need 6 more cups of coffee").listIterator();
		si.next(); si.next();
		n = Number.getNumber( si );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		si = new Strings("i need 6 less cups of coffee").listIterator();
		si.next(); si.next();
		n = Number.getNumber( si );
		audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +") sz="+ n.repSize());
		//n = new Number( new Strings( "this is rubbish" ));
		//audit.audit( "n is "+ n.toString() +" ("+ n.valueOf() +")");
}	}
