package com.yagadi.enguage.concept;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Colloquial;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.sofa.Sofa;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Intention extends Attribute {
	static Audit audit = new Audit( "Intention" );

	public static final String REPLY      = "reply";
	public static final String ELSE_REPLY = "elseReply";
	public static final String THINK      = "think";
	public static final String ELSE_THINK = "elseThink";
	public static final String PERFORM    = "perform";
	public static final String ELSE_PERFORM = "elsePerform";
	public static final String FINALLY    = "finally";
	
	public Intention( String name, String value ) { super( name, value ); }	
	
	// processes: think="... is a thought".
	private Reply think( String answer ) {
		//audit.traceIn( "think", "value='"+ value +"', previous='"+ answer +"', ctx =>"+ Reply.context().toString( " " ));
			
		// pre-process value to get an utterance...
		// we don't know the state of the intentional value
		Strings u =
			Shell.addTerminator(// _user is => _user is.
				Colloquial.user().internalise( // I am => _user is
					Colloquial.symmetric().internalise( // I'm => I am
						Variable.deref( // $BEVERAGE + _BEVERAGE -> ../coffee => coffee
							Reply.context().deref( // X => "coffee", singular-x="80s" -> "80"
								new Strings( value ).replace( // replace "..." with answer
										Strings.ellipsis,
										new Strings( answer )),
								false // don't expand, UNIT => cup NOT unit='cup'
				)	)	)	)	);
		//u = u.normalise(); // [ "this is a test ." ] => [ "this" , is", ...

		audit.debug( "Thinking: "+ u.toString( Strings.CSV ));
		Reply r = Enguage.e.innerterpret( u );
		
		r.doneIs( false );
		// TODO: should be toasted?
		if ( Reply.DNU == r.getType()) {
			/* TODO: At this point do I want to cancel all skipped signs? 
			 * Or just check if we've skipped any signs and thus report 
			 * this as simply a warning not an ERROR?
			 */
			if (Engine.disambFound())
				audit.ERROR( "Following ERROR: maybe just run out of meanings?" );
			audit.ERROR( "Strange thought: I don't understand: '"+ u.toString( Strings.SPACED ) +"'" );
		
		} else if ( Reply.NO == r.getType() && r.answer().equalsIgnoreCase( Reply.ik()))
			r.answer( Reply.yes());
		
		//audit.traceOut( r.asString());
		return r;
	}
	private String conceptualise( String answer ) {
		//audit.traceIn(  "conceputalise", "value='"+ value +"', ["+ Reply.context().toString( " " ) +"]" );
		// SofA CLI in C returned 0, 1, or "xxx" - translate these values into Reply values
		Strings cmd = // Don't Strings.normalise() coz sofa requires "1" parameter
				Variable.deref( // $BEVERAGE + _BEVERAGE -> ../coffee => coffee
					Reply.context().deref(
							new Strings( value ).replace( // replace "..." with answer
								Strings.ellipsis,
								new Strings( answer )),
							true // DO expand, UNIT => unit='non-null value'
				)	);
		
		//audit.debug(  "conceptualising: "+ cmd.toString( Strings.CSV ));
		String rc = new Sofa( null ).interpret( cmd );
		//audit.debug(  "raw answer is: '"+ rc +"'" );
		if (cmd.get( 1 ).equals( "get" ) && (null == rc || rc.equals( "" ))) {
			audit.debug("conceptualise: get returned null -- should return something");
			rc = Reply.dnk();
		} else if (rc.equals( Shell.FAIL )) {
			audit.debug("conceptualise: get returned FALSE --> no");
			rc = Reply.no();
		} else if (rc.equals( Shell.SUCCESS )) {
			// was rc = Reply.yes(); -- need to perpetuate answer
			// if no ans or -ve ans - set to No, otherwise set to existing ans
			rc =  answer.equals("")||answer.equals( Reply.no() ) ? Reply.success() : answer;
		}
		//audit.traceOut( rc );
		return rc;
	}
	private Reply reply( Reply r ) {
		//audit.traceIn(  "reply", "value='"+ value +"', ["+ Reply.context().toString( " " ) +"]" );
		// value="X needs Y"; X="_user", Y="beer+crisps" -- ?"fuller/beer+crisps"?
		// we're on the way out - treat each value as an answer!
		Reply.context().delistify();
		// now Y="beer and crisps" -- ?"fuller/beer+crisps"?
		r.format(
			Variable.deref(
				Reply.context().deref(
					value // ?replaced with...
					/*new Strings( value ).replace( // replace "..." with answer
					 *	Strings.ellipsis,
					 *	new Strings( r.answer()))
					 */
			)	)
		)
		.doneIs( true );
		//audit.traceOut( r.toString() +"::"+ r.format().toString() );
		return r;
	}
	
	public Reply mediate( Reply r ) {
		//if (!name.equals("id") && !name.equals("help"))
		//	audit.traceIn( "mediate", name +"='"+ value +"', r='"+ r.asString() +"', ctx =>"+ Reply.context().toString( " " ));
		
		if (name.equals( "finally" ))
			conceptualise( r.answer()); // ignore result of finally

		else if (name.equals( "id" ))
			; // ignore id

		else if (name.equals( "help" ))
			; // ignore help

		else if (!r.isDone())	{
			
			if (r.negative()) {
				if (name.equals( ELSE_THINK ))
					r = think( r.answer() );
				else if (name.equals( ELSE_PERFORM ))
					r.answer( conceptualise( r.answer()));
				else if (name.equals( ELSE_REPLY ))
					r = reply( r );
 					
			} else { // train of thought is positive
				if (name.equals( THINK ))
					r = think( r.answer() );
				else if (name.equals( PERFORM ))
					r.answer( conceptualise( r.answer()));
				else if (name.equals( REPLY )) // if Reply.NO -- deal with -ve replies!
					r = reply( r );
			}
		}// else
		//	audit.debug( "skipping "+ NAME +": reply already found" );
		//if (!name.equals("id") && !name.equals("help"))
		//	audit.traceOut( "r='"+ r.toString() +"' ("+ r.asString() +")");
		return r;
	}
	public static void main( String argv[]) {
		Reply r = new Reply().answer( "world" );
		Intention intent = new Intention( REPLY, "hello ..." );
		r = intent.mediate( r );
		System.out.println( r.toString() );
}	}
