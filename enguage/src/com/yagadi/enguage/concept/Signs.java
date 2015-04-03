package com.yagadi.enguage.concept;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Signs extends TreeMap<Integer,Sign> {
	        static final long serialVersionUID = 0l;
	private static       Audit           audit = new Audit( "Signs" );
	private static final boolean         debug = Enguage.runtimeDebugging; // true; //
	
	private String name = ""; // for debug statements
	public  Signs( String nm ) { name = nm; }
	
	static private int   total = 0;
	static private int clashes = 0;
	
	public Signs insert( Sign insertMe ) {
		int c = Sign.complexity( insertMe ),
			i = 0;
		while (i < 99 && containsKey( c + i )) {
			//audit.audit( "Signs:insert()>>>>>>CLASH: "+ insertMe.toLine());
			clashes++;
			i++;
		}
		if (i < 99) {
			total++;
			put( c + i, insertMe );
		} else
			audit.ERROR( "failed to find place for sign:" );
		return this;
	}
	
	static public String stats() { return clashes +" clashes in a total of "+ total; }
	
	private void swap( int a, int b) {
		//audit.traceIn( "swap", "a="+ a +"', b='"+ b +"'");
		if (a<0 || b<0 || a>=size() || b>=size()) {
			audit.ERROR( "Sign.swap(): dimentional issue" );
		} else if (a == b) {
			//audit.debug( a +" == "+ b +", nothing to swap." );
		} else if (a > b) {
			//audit.debug( "inverting swap" );
			swap( b, a );
		} else {
			Sign tmp = get( a );
			put( a, get( b ));
			put( b, tmp );
		} 
		//audit.traceOut();
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
		ArrayList<Integer> removes = new ArrayList<Integer>();
		
		// to prevent co-mod errors, load a list with the keys of those to be removed
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while(i.hasNext()) {
			Map.Entry<Integer,Sign> me = (Map.Entry<Integer,Sign>)i.next();
			if (!id.equals( me.getValue().concept() ))
				removes.add( me.getKey() );
		}
		// then remove them
		ListIterator<Integer> ri = removes.listIterator();
		while( ri.hasNext())
			remove( ri.next() );
	}
	public void show() {
		int n=0;
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = (Map.Entry<Integer,Sign>)i.next();
			Sign s = me.getValue();
			audit.audit( s.toString( n++, me.getKey() ));
	}	}
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
	// used to save the positions of signs to ignore - now keys of signs to ignore
	private ArrayList<Integer> ignore = new ArrayList<Integer>();
	public  ArrayList<Integer> ignore() { return ignore; }
	public  void               ignore( int i ) {
		if (i == -1)
			ignoreNone();
		else {
			//audit.debug("Sign.numToAvoid( "+ i +" )");
			ignore.add( i );
	}	}
	public  void               ignoreNone() {
		audit.traceIn( "ignoreNone", "" );
		ignore.clear();
		audit.traceOut();
	}
	// ---------------------------------------------

	static public  final int noInterpretation  = 0;
	static private       int interpretation = noInterpretation;
	static private       int interpretation() { return ++interpretation; }
	
	public String toString() {
		String str = "";
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = (Map.Entry<Integer,Sign>)i.next();
			str += me.getValue().toString();
		}
		return str;
	}

	public Reply interpret( Strings utterance ) {
		utterance.contract( "=" );
		//*
		if (debug)
			audit.traceIn( "interpret",
				" ("+ name +"/"+ size() +") "
				+ "'"+ utterance.toString( Strings.SPACED ) +"'"
		 		+ (ignore.size()==0?"":("avoiding "+ignore)));
		// */
		
		int i = -1,
		    here = interpretation(); // an ID for this interpretation
		Reply r = new Reply();
		//ListIterator<Sign> si = listIterator();
		//while (si.hasNext())
		Set<Map.Entry<Integer,Sign>> entries = entrySet();
		Iterator<Map.Entry<Integer,Sign>> ei = entries.iterator();
		while( ei.hasNext()) {
			Map.Entry<Integer,Sign> e = (Map.Entry<Integer,Sign>)ei.next();
			int complexity = e.getKey();
			/* TODO: sign ignores () need to be recalced on comodification
			 */
			if (ignore.contains( complexity ))
				audit.audit( "Sign skipped during Signs.interpret() "+ i );
			else {
				Sign s = e.getValue();
				//audit.debug("  interpreting "+ s.toText());
				if (s.interpretation == noInterpretation) {
					
					Attributes match = s.content().matchValues( utterance );
					if (null != match) { // we have found a meaning! So I do understand...!
						// here: match=[ x="a", y="b+c+d", z="e+f" ]
						// will here need to spot/translate references x="the queen" -> x="QE2"
						// if no context, pass down as it may have context at the Value level: "I have the beer"

						//audit.debug("setting "+ i +" to "+ here );
						s.interpretation = here; // mark here first as this understanding may be toxic!
						
						foundAt( complexity ); /* This MUST be recorded before interpretation, below:
						 *  this will be overwritten during interpretation, eventually being
						 *  left with the last in the chain of signs in an interpretation.
						 */
						//audit.debug( "Found@ "+ i +":"+ get( i ).content().toLine() +":"+ match.toString() +")");
						
						Reply.pushContext( match );
						r = s.interpret(); // may pass back DNU
						Reply.popContext();
						
						/* May have modified repertoire by autoloading.
						 * ignores now works on key (complexity)
						 * 
						 * 1 2 3 4 5                  original
						 * 1  2 -1  3 -1 -1  4 -1  5  comodified
						 * 1  2  3  4  5  6  7  8  9  eventual
						 * 
						 * So Ignores got from 2, 4 to 2, 7.
						 */
						
						// if reply is DNU, this meaning is not appropriate!
						if (r.getType() == Reply.DNU) {
							/* Comodification error?
							 * If, during interpretation, we've modified the repertoire by 
							 * autoloading and we've not understood this we've 
							 * screwed the repertoire we're currently half-way through.
							 */
							// Reassign si in here!
							/* Then find our way back "here". The sign-scape will be 
							 * peppered with new signs, so there may not be a complete 
							 * trail of signs where skipme == true. We can work our way 
							 * back to this point as ahead of us there will be a complete 
							 * trail of signs where skipme == noId.
							 * N.B. There is no "jump to the end", so this alg involves 
							 * one read through of the whole list.
							 */
							//audit.debug( "looking for "+ here );
							//audit.incr();
							entries = entrySet();
							ei = entries.iterator();
							//mei = values().iterator();
							while( ei.hasNext()) {
								e = (Map.Entry<Integer,Sign>)ei.next();
								s = e.getValue();
								//audit.debug( "looking at "+ s.toText() );
								if( s.interpretation == here ) { // we are back "here"
									//audit.debug( "continuing, resetting "+ i );
									s.interpretation = noInterpretation; // tidy up this sign.
									break; // return to processing the list...
							}	}
							//audit.decr();
							//audit.debug( "done" );
							if (!ei.hasNext()) audit.ERROR("RESETting si failed <<<<<<<<<<");
						} else {
							
							//audit.debug("understood resetting "+ i );
							s.interpretation = noInterpretation; // tidy as we go
							
							//save the context here, for future use...
							if (!Autoload.ing() && !Repertoires.isInitialising())
								for ( Attribute m : match )
									Variable.set( m.name(), m.value());
							break;
		}	}	}	}	}
		
		if (debug) audit.traceOut( r.toString());
		return r;
}	}