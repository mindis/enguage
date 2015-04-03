package com.yagadi.enguage.concept;

import java.util.ArrayList;
import java.util.ListIterator;

import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Signs extends ArrayList<Sign> {
	static final long serialVersionUID = 0l;
	private static  Audit audit = new Audit( "Signs" );
	
	//private String name = ""; // for debug statements
	public  Signs( String nm ) { /*name = nm;*/ }
	
	public Signs insert( Sign insertMe ) {
		insertMe.setComplexity();
		add( insertMe.rank( this ), insertMe );
		return this;
	}
	
	public void swap( int a, int b) {
		//audit.traceIn( "swap", "a="+ a +"', b='"+ b +"'");
		if (a == b) {
			//audit.debug( a +" == "+ b +", nothing to swap." );
		} else if (a > b) {
			//audit.debug( "inverting swap" );
			swap( b, a );
		} else if (a>=0 && b>=0 && a<size() && b<size()) {
			Sign tmp = get( a );
			set( a, get( b ));
			set( b, tmp );
		} else {
			audit.ERROR( "Sign.swap(): dimentional issue" );
		}
		//audit.traceOut();
		return;
	}
	public void reorder() {
		if (ignore().size() > 0) { // not needed unless we've no signs
			/* OK, here we've said "tiat", foundAt=35
			 * AND...
			 * THEN we've said "No, tiat" - ignoring [35], foundAt=42
			 * SO to tidy up:
			 * SWAP SIGNS
			 * FROM	sign order=..., 35, ..., 42, ..., 53
			 */
			int swap = ignore().get( 0 ), // 35
				with = lastFoundAt();     // 42
			//audit.debug( "OK SWAPPING "+ swap +" WITH "+ with );
			swap( swap, with );	
			/*
			 * TO	sign order=..., 42, ..., 35, ..., 53,
			 * 
			 * BUT ignore remains as [35]?
			 * Therefore replace INGORE val 35 with 42.
			 */
			//audit.debug("Ignores was "+ ignore().toString());
			ignore().set( ignore().indexOf( swap ), with );
			//audit.debug("Ignores now "+ Enguage.e.signs.ignore().toString());
			
			// readjust where this was found too!
			foundAt( swap );
	}	}
	
	public void remove( String id ) {
		for (int i=0; i < size(); i++) 
			if (!id.equals( get( i ).attribute( "id" ) ))
				remove( i );
	}
	public void show() {
		int n=0;
		for( Sign s : this )			
			System.out.println( s.toString( n++ ));
	}
	public String toString() {
		String str = "";
		for( Sign s : this )
			str += s.toString();
		return str;
	}
	/*
	 * remember which sign we interpreted last
	 */
	private static final int listStart = -1;
	private int  posn = listStart;
	public void foundAt( int i ) { 
		//audit.debug( "Signs.foundAt( i="+ i +")" );
		posn = i;
	}
	public int lastFoundAt() { return posn; }
	
	// ---------------------------------------------
	private ArrayList<Integer> ignore = new ArrayList<Integer>();
	public  ArrayList<Integer> ignore() { return ignore; }
	public  void               ignore( int i ) {
		if (i == -1)
			ignoreNone();
		else {
			//audit.debug("Sign.numToAvoid( "+ i +" )");
			ignore.add( i );
	}	}
	public  void               ignoreNone() { ignore.clear(); }
	// ---------------------------------------------
		
	public Reply interpret( Strings utterance ) {
		utterance.contract( "=" );
		//if (Audit.isOn()) audit.traceIn( "interpret",
		//		"'"+ utterance.toString() +"'"
		//		+ " ("+ name +") "
		//		+ (ignore.size()==0?"":("avoiding "+ignore)));
		Reply r = new Reply();
		int i = -1;
		ListIterator<Sign> si = listIterator();
		while (si.hasNext())
			if (ignore.contains( ++i ))
				audit.debug( "Signs.skipped during interpret() "+ i );
			else {
				Sign s = si.next();
				Attributes match = s.content().matchValues( utterance );
				if (null != match) { // we have found a meaning! So I do understand...!
					// here: match=[ x="a", y="b+c+d", z="e+f" ]
					// will here need to spot/translate references x="the queen" -> x="QE2"
					// if no context, pass down as it may have context at the Value level: "I have the beer"
	
					foundAt( i ); /* This MUST be recorded before interpretation, below:
					 *  this will be overwritten during interpretation, eventually being
					 *  left with the last in the chain of signs in an interpretation.
					 */
					//audit.debug( "Found sign "+ i +":"+ get( i ).content().toLine() +":"+ match.toString() +")");
					
					Reply.pushContext( match );
					r = s.interpret(); // may pass back DNU
					Reply.popContext();
					// if reply is DNU, this meaning is not appropriate!
					if (r.getType() != Reply.DNU) {
						break;
			}	}	}
		//if (Audit.isOn()) audit.traceOut( r.toString());
		return r;
}	}