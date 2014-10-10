package com.yagadi.enguage.concept;

import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.util.*;

public class Signs {
	//static  Audit audit = new Audit( "Signs" );
	private Sign[] list = new Sign[ 0 ];
	public  Sign[] list() { return list; }
	
	// inserts, sorted on the complexity of sign (number of tags)
	public Signs insert( Sign s ) {
		//if (Audit.isOn()) audit.traceIn( "insert", s.name());
		int alen = list.length;
		int blen = alen+1;
		Sign[] b = new Sign[ blen ];
		int i=-1;
		// copy across all signs of less complexity
		while (++i<alen && s.content().ta.length > list[ i ].content().ta.length) b[ i ] = list[ i ];
		// insert new sign of equal complexity here
		//if (Audit.isOn()) audit.debug( "inserted at i="+ i );
		b[ i ] = s;
		// copy across rest of the signs of equal and greater complexity
		while (++i<blen) b[ i ] = list[ i-1 ];
		list = b;
		//if (Audit.isOn()) audit.traceOut();
		return this;
	}
	public static Sign[] append( Sign[] a, Sign s ) {
		Sign[] b = new Sign[ a.length + 1 ];
		for (int i=0; i<a.length; i++) b[ i ] = a[ i ];
		b[ a.length ] = s;
		return b;
	}
	public void remove( String id ) {
		Sign[] b = new Sign[ 0 ];
		for (int i=0; i < list.length; i++)
			if (!id.equals( list[ i ].name ))
				b = append( b, list[ i ]);
		list = b;
	}
	public void show() {
		for( Sign s : list )
			System.out.println( s.toString() );
	}
	public String toString() {
		String str = "";
		for( Sign s : list )
			str += s.toString();
		return str;
	}
	public Reply interpret( String[] utterance ) {
		//if (Audit.isOn()) audit.traceIn( "interpret", "'"+ Strings.toString( utterance, Strings.SPACED ) +"'" );
		Reply r = new Reply();
		for ( Sign s : list ) {
			Attributes match = s.content().matchValues( utterance );
			if (null != match) { // we have found a meaning! So I do understand...!
				// here: match=[ x="a", y="b+c+d", z="e+f" ]
				// will here need to spot/translate references x="the queen" -> x="QE2"
				// if no context, pass down as it may have context at the Value level: "I have the beer"
				//if (Audit.isOn()) audit.debug( "Matched pattern:\n"+ s.toString() +"Matches: "+ match.toString() +")");
				Reply.pushContext( match );
				r = s.interpret(); // may pass back DNU
				Reply.popContext();
				// if reply is DNU, this meaning is not appropriate!
				if (r.getType() != Reply.DNU) break;
		}	}
		//if (Audit.isOn()) audit.traceOut( r.toString());
		return r;
}	}
