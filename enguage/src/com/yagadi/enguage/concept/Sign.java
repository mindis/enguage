package com.yagadi.enguage.concept;

import java.util.ArrayList;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.util.Audit;

public class Sign extends Tag {
	private static Audit audit = new Audit("Sign");
	private static boolean debug = Enguage.runtimeDebugging; 

	/*  The complexity of a sign, used to rank signs in a repertoire.
	 *  "the eagle has landed" comes before "the X has landed", BUT
	 *  "the    X    Y-PHRASE" comes before "the Y-PHRASE" so it is not
	 *  a simple count of tags, phrased hot-spots "hoover-up" tags!
	 *  Phrased hot-spot has a large complexity, and any normal tags
	 *  will actually bring this complexity down!
	 *  Think of: "a bad liar" as a better person.
	 *  
	 *  Three planes of complexity: bp tags phrase
	 *  ==========================
	 *  complexity increases with   1xm bp 1->100.
	 *  complexity increases with 100xn tags 100->10000
	 *  if phrase exists, complexity counts down from 1000000:
	 *  10000 x m bp,   range = 10000 -> 100000
	 *    100 x n tags, range =   100 -> 10000, as before
	 */
	private int  complexity;
	public  int getComplexity() { return complexity; }
	public  int setComplexity() {
		complexity = 0;
		boolean infinite = false;
		int boilerplate = 0, tags = 0;
		
		for (Tag t : content()) {
			boilerplate += t.prefixAsStrings().size();
			if (t.attributes().get( "phrase" ).equals( "phrase" ))
				infinite = true;
			else if (!t.name().equals( "" ))
				tags ++;
		}
		// limited to 100bp == 1tag, or phrase + 100 100tags/bp
		return complexity = infinite ? 1000000 - 10000*boilerplate - 100*tags : 100*tags + boilerplate;
	}
	
	public int rank( Signs ss ) {
		int rank = 0;
		for (Sign s : ss)
			if (complexity >= s.getComplexity()) // >= orders in repertoire file order
				rank++;
			else
				break;
		return rank;
	}

	
	// methods need to return correct class of this
	public Sign attribute( String name, String value ) { attrs.add( new Attribute( name, value )); return this; }
	public Sign content( Tag t ) {
		//audit.traceIn( "content", "NAME='"+ t.name() +"', id='"+ t.attribute("id") +"'" );
		content.add( t );
		//audit.traceOut();
		return this;
	}
	@Override
	public Sign content( Tags ta ) { content = ta; return this; }
	public Sign() {
		super();
		name( "pattern" );
	}
	public Sign( String a, String b, String c ) {
		super( a, b, c );
		name( "pattern" );
	}
	public Reply interpret() {
		if (debug) audit.traceIn( "interpret", null );
		ArrayList<Attribute> a = attributes();
		int i = -1;
		Reply r = new Reply();
		while (!r.isDone() && a.size() > ++i) {
			String name  = a.get( i ).name(),
			       value = a.get( i ).value();
			if (debug) audit.debug( name +"='"+ value +"'" );
			r = name.equals( Engine.NAME ) ?
					new Engine(      name, value ).mediate( r )
				: name.equals( Autopoiesis.APPEND )  ||
				  name.equals( Autopoiesis.PREPEND ) ||
				  name.equals( Autopoiesis.NEW ) ?
					new Autopoiesis( name, value ).mediate( r )
				: // finally, perform, think, say...
					new Intention(   name, value ).mediate( r );
		}
		if (debug) audit.traceOut( r.toString() );
		return r;
	}
	private final static String indent = "    ";
	public String toString( int n ) {
		return prefix + (name().equals( "" ) ? "" :
			(indent +"<"+ name() +" n='"+ n +"' complexity='"+ complexity + "' " +attrs.toString( "\n      " )+
			(null == content() ? "/>" : ( ">\n"+ indent + indent + content().toString() + "</"+ name() +">" ))))
			+ postfix + "\n";
	}
	public static void main( String argv[]) {
		Sign p = new Sign();
		p.attribute("reply", "hello world");
		p.content( new Tag().prefix( "hello" ));
		Reply r = new Reply();
		Intention intent = new Intention( "say", "hello world" );
		r = intent.mediate( r );
}	}
