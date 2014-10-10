package com.yagadi.enguage.concept;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.*;
import com.yagadi.enguage.sofa.*;
import com.yagadi.enguage.util.*;

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
			
		// pre-process utterance...
		String[] u =
			Language.addTerminator(
				Enguage.user().apply( // dereference colloquials
					Reply.both().disapply(
						Variable.deref(
							Reply.context().deref( 
								Strings.replace( // replace "..." with answer
										Strings.fromString( value ),
										Strings.ellipsis,
										Strings.append( null, answer ))
			)	)	)	)	);

		//audit.debug( "Thinking: "+ Strings.toString( u, Strings.CSV ));
		Reply r = Enguage.interpreter.innerterpret( u );
		
		r.doneIs( false );
		// TODO: should be toasted?
		if ( Reply.DNU == r.getType())
			audit.ERROR( "Strange thought: I don't understand: '"+ Strings.toString( u, Strings.SPACED ) +"'" );
		else if ( Reply.NO == r.getType() && r.answer().equalsIgnoreCase( Reply.ik()))
			r.answer( Reply.yes());
		
		//audit.traceOut( r.asString());
		return r;
	}
	private String conceptualise() {
		//audit.traceIn(  "conceputalise", "value='"+ value +"', ["+ Reply.context().toString( " " ) +"]" );
		// SofA CLI in C returned 0, 1, or "xxx" - translate these values into Reply values
		
		String[] cmd = Reply.context().deref(
				Reply.context().getCommand(
						Strings.fromString( value )));
		
		audit.debug(  "command is: "+ Strings.toString( cmd, Strings.CSV ));
		String rc = new Sofa( null ).interpret( cmd );
		//audit.debug(  "raw answer is: '"+ rc +"'" );
		if (cmd[ 1 ].equals( "get" ) && (null == rc || rc.equals( "" ))) {
			//audit.audit("conceptualise: get returned null -- should return something");
			rc = Reply.dnk();
		} else if (rc.equals( Shell.FAIL )) {
			//audit.audit("conceptualise: get returned FALSE --> no");
			rc = Reply.no();
		} else if (rc.equals( Shell.SUCCESS )) {
			//audit.audit("conceptualise: get returned TRUE --> yes");
			rc = Reply.yes();
		}
		//audit.traceOut( rc );
		return rc;
	}
	private Reply reply( Reply r ) {
		// value="X needs Y"; X="_user", Y="beer+crisps" -- ?"fuller/beer+crisps"?
		// we're on the way out - treat each value as an answer!
		Reply.context().delistify();
		// now Y="beer and crisps" -- ?"fuller/beer+crisps"?
		value = Reply.context().deref( value );
		r.format( value );
		r.doneIs( true );
		return r;
	}
	
	public Reply mediate( Reply r ) {
		//if (!name.equals("id") && !name.equals("help"))
		//	audit.traceIn( "mediate", name +"='"+ value +"', r='"+ r.asString() +"', ctx =>"+ Reply.context().toString( " " ));
		
		if (name.equals( "finally" ))
			conceptualise(); // ignore result of finally

		else if (name.equals( "id" ))
			; // ignore id

		else if (name.equals( "help" ))
			; // ignore help

		else if (!r.isDone())	{
			
			if (r.negative()) {
				if (name.equals( ELSE_THINK ))
					r = think( r.answer() );
				else if (name.equals( ELSE_PERFORM ))
					r.answer( conceptualise());
				else if (name.equals( ELSE_REPLY ))
					r = reply( r );
				//else
					//audit.debug( "Skipping "+ name +": infelicitous outcome" );
 					
			} else { // train of thought is positive
				if (name.equals( THINK ))
					r = think( r.answer() );
				else if (name.equals( PERFORM ))
					r.answer( conceptualise());
				else if (name.equals( REPLY )) // if Reply.NO -- deal with -ve replies!
					r = reply( r );
				//else
					//audit.debug( "Skipping "+ name +": felicitous outcome" );
			}
		} else
			audit.debug( "skipping "+ name +": reply already found" );
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
