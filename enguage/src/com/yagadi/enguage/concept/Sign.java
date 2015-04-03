package com.yagadi.enguage.concept;

import java.util.Iterator;
import java.util.Random;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.util.Audit;

public class Sign extends Tag {
	private static final String   NAME = "sign";
	private static       Audit   audit = new Audit("Sign");
	private static final boolean debug = Enguage.runtimeDebugging; // false; // 
	private static final String indent = "    ";

	public Sign() {
		super();
		name( NAME );
	}
	public Sign( String a, String b, String c ) {
		super( a, b, c );
		name( NAME );
	}
	
	/* To protect against being interpreted twice on resetting the iterator
	 * after a DNU is returned on co-modification of the iterator by
	 * autoloading repertoires.
	 * 
	 * We may see this pattern in a list of signs following comodification:
	 * 	t t t t f t t t f f t t f f f t t T f f f f f f f f f f f f f f
	 *                                    ^ 
	 * 'cos some new signs will be peppered through out the list. We need
	 * to start again at the sign following "^".
	 * N.B. Don't really want to unload those autoloaded signs as they may well
	 * be needed by the eventually understood utterance.  Autounloading (aging) 
	 * will manage the list if not.
	 * In fact, there should only be one T, the other t's are tidied as 
	 * processing progresses, so there is no need to determine when the...
	 */
	public int interpretation = Signs.noInterpretation;

	// Set during autopoiesis - replaces 'id' attribute 
	private String concept = "";
	public  String concept() { return concept; }
	public  Sign   concept( String name ) { concept = name; return this; }
	
	private String help = "";
	public  String help() { return help; }
	public  Sign   help( String str ) { help = str; return this; }
	
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
	 *
	 *  Boilerplate complexity:
	 *  Has been fine tuned to reduce number of clashes when inserting into TreeMap
	 *  Using a random number (less processing?/more random?)  Make it least
	 *  
	 *  Finite:
	 *  |-----------+-----------+------------------|
	 *  | tags 0-99 |Words 0-99 | Random num 0-99  |
	 *  |-----------+-----------+------------------|
	 *  
	 *  Infinite:
	 *  |----------|   |------------+-----------|------------------+
	 *  | 1000000  | - | Words 0-99 | tags 0-99 | Random num 0-99  |
	 *  |----------|   |------------+-----------|------------------+
	 */
	static Random rn = new Random();
	static final int RANGE = 1000; // means full range will be up to 1 billion
	static final int FULL_RANGE = RANGE*RANGE*RANGE;
	static final int  MID_RANGE = RANGE*RANGE;
	static final int  LOW_RANGE = RANGE;
	
	static public int complexity( Tag t ) {
		boolean infinite = false;
		int boilerplate = 0,
				tags = 0,
				rnd = rn.nextInt( RANGE ); // word count component
		
		for (Tag content : t.content()) {
			boilerplate += content.prefixAsStrings().size();
			if (content.attributes().get( phrase ).equals( phrase ))
				infinite = true;
			else if (!content.name().equals( "" ))
				tags ++; // count named tags
		}
		// limited to 100bp == 1tag, or phrase + 100 100tags/bp
		return infinite ?
				FULL_RANGE - MID_RANGE*boilerplate - LOW_RANGE*tags + rnd
				: MID_RANGE*tags + LOW_RANGE*boilerplate + rnd;
	}
	
	// methods need to return correct class of this
	public Sign attribute( String name, String value ) { attrs.add( new Attribute( name, value )); return this; }
	
	@Override
	public Sign content( Tags ta ) { content = ta; return this; }
	public Sign content( Tag  t )  { content.add( t ); return this; }
	
	public String toString( int n, long c ) {
		return prefix + (name().equals( "" ) ? "" :
			(indent +"<"+ name() +" n='"+ n +"' complexity='"+ c + "' " +attrs.toString( "\n      " )+
			(null == content() ? "/>" : ( ">\n"+ indent + indent + content().toString() + "</"+ name() +">" ))))
			+ postfix + "\n";
	}
	
	public Reply interpret() {
		if (debug) audit.traceIn( "interpret", toLine() );
		Attributes a = attributes();
		Reply r = new Reply();
		Iterator<Attribute> ai = a.iterator();
		while (!r.isDone() && ai.hasNext()) {
			Attribute an = ai.next();
			String name  = an.name(),
			       value = an.value();
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
	// ---
	public static void complexityTest( Tags t ) {
		Tag container = new Tag( "", "Y" );
		container.content( t );
		audit.audit( "Complexity of "+ container.toString() +"("+ complexity( container ) +")\n");
	}
	public static void main( String argv[]) {
		Sign p = new Sign();
		p.attribute("reply", "hello world");
		p.content( new Tag().prefix( "hello" ));
		Reply r = new Reply();
		Intention intent = new Intention( "say", "hello world" );
		r = intent.mediate( r );
		audit.audit( "r="+ r.toString());
		
		Tags ts = new Tags();
		ts.add( new Tag( "one small step for man", "" ));
		complexityTest( ts );
		
		ts = new Tags();
		ts.add( new Tag( "this is a", "test" ));
		complexityTest( ts );
		
		ts = new Tags();
		ts.add( new Tag( "this is a test", "x" ).attribute( "phrase", "phrase" ) );
		complexityTest( ts );
		
		ts = new Tags();
		ts.add( new Tag( "this is a", "x" ).attribute( "phrase", "phrase" ));
		complexityTest( ts );
		
}	}
