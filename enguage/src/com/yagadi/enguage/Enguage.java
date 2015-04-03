package com.yagadi.enguage;

import android.app.Activity;

import com.yagadi.enguage.concept.Autopoiesis;
import com.yagadi.enguage.concept.Engine;
import com.yagadi.enguage.concept.Repertoire;
import com.yagadi.enguage.expression.Colloquial;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Overlay;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Enguage extends Shell {
	static private Audit   audit = new Audit( "Enguage" );
	static private boolean debug = Audit.isOn();

	static final public  String              name = "enguage";
	static final public  String    configFilename = "config.xml";
	
	// global DEBUGging switches...
	static final public boolean startupDebugging = false;
	static final public boolean runtimeDebugging = false;
	
	//static private boolean silentStartup( boolean l ) { return silentStartup = l; }
	static public  boolean silentStartup() { return !startupDebugging; }
	
	/* This singleton is managed here because the Engine code needs to know about
	 * the state that the e is in, but won't have access to it unless the
	 * calling code sets up that access.
	 */
	static public Enguage e = null; // TODO: remove this have all access as static
	static public Overlay o = null; // TODO: ditto

	/*
	 * TODO: Enguage should manage an Engine. Discuss.
	 */
	private Activity ctx = null;
	public  Activity ctx() { return ctx; }
	
	// are we taking the hit of creating / deleting overlays
	static private boolean undoEnabled = false;
	static public  boolean undoIsEnabled() { return undoEnabled; }
	static public  void    undoEnabledIs( boolean enabled ) { undoEnabled = enabled; }
	
	static private boolean understood = false;
	static public  boolean lastReplyWasUnderstood() { return understood; }
	static private void    lastReplyWasUnderstood( boolean was ) { understood = was; }

	static private String lastOutput = null;
	static public  String lastOutput( String l ) { return lastOutput = l; }
	static public  String lastOutput() { return lastOutput; }

	/* 
	 * To be used in identifying ambiguity...
	 */
	static private Strings lastInput = null;
	static public  Strings lastInput( Strings sa ) { return lastInput = sa; }
	static public  Strings lastInput() { return lastInput; }

	public Enguage( Activity a ) {
		super( "Enguage" );
		ctx = a;
		
		if (null == o) Overlay.Set( o = new Overlay()); // use/provide global overlay 
		if (!o.attached()) {
			String rc = Overlay.autoAttach();
			if (!rc.equals( "" ))
				audit.ERROR( "Ouch! Cannot autoAttach() to object space: "+ rc );
		}
		
		if (null == System.getProperty( "HOST" ))
			System.setProperty( "HOST", "phone" );
	}

	// ***********************************************************
	// concept management -- this could go inside Repertoire.java?
	// 
	static public Repertoire signs = new Repertoire( "users" );
	static public Repertoire autop = new Repertoire( "autop", Autopoiesis.autopoiesis );
	static public Repertoire engin = new Repertoire( "engin", Engine.commands );
	

	@Override
	public String interpret( Strings utterance ) {
		if (debug) 
			audit.traceIn( "interpret", utterance.toString( Strings.CSV ));
		else // always audit what is said
			audit.debug( utterance.toString( Strings.SPACED ));

		// just to say things are working...
		Engine.spoken( true );
		
		// we're looking to find if "No, ..." is in this utterance, so
		// normally simply set as off here, BUT we may have left it on from last time...
		//Engine.disambFound( false );
		
		// obtain a reply
		if (lastReplyWasUnderstood()) o.startTxn( undoIsEnabled()); // all work in this new overlay
		Reply r =
			Repertoire.innerterpret(
				Colloquial.user().internalise(
					Colloquial.symmetric().internalise(
						utterance
			)	)	);
		lastReplyWasUnderstood( Reply.DNU != r.getType() );
		if (lastReplyWasUnderstood()) o.finishTxn( undoIsEnabled()); // Accept the previous overlay.
		
		// once processed, keep a copy
		lastInput( utterance );
		
		// convert that reply into text
		String reply = r.toString();
		if ( lastReplyWasUnderstood() ) {
			if (!r.repeated()) lastOutput( reply );
			// deal with redo
			Engine.disambOff();
		} else {
			// really lost track?
			audit.audit( "Enguage:interpret():not understood, forget ignores: "+ Enguage.signs.ignore().toString());
			Enguage.signs.ignoreNone();
			aloudIs( true ); // sets aloud for whole session if reading from fp
			// put in "dnu, '...'" here? -- if aloud()?
			r.handleDNU( utterance );
			reply = r.toString();
		}

		if (debug) audit.traceOut( reply );
		return reply;
	}
		
	public static void main( String args[] ) {
		//Audit.turnOn();
		
		e = new Enguage( null );
		Repertoire.loadConfig( configFilename );
		System.out.println( e.copyright() );
		System.out.println( "Enguage main(): overlay is: "+ Overlay.Get().toString());
		//signs.show();
		//*
//		e.interpret( new Strings( "i need a cup of coffee" ));
//		e.interpret( new Strings( "i need another 3 cups of coffee" ));
		audit.audit( "> "+ e.interpret( new Strings( "i need 5 packets of ground coffee and carrots and potatoes" )));
		audit.audit( "> "+ e.interpret( new Strings( "i need peas and gravy and 5 bottles of real ale" )));
		//e.interpret( new Strings( "i need another cup of coffee" ));
		//e.interpret( new Strings( "i need carrots and potatoes" ));
		audit.audit( "> "+ e.interpret( new Strings( "what do i need" )));
		audit.audit( "> "+ e.interpret( new Strings( "i don't need anything" )));
		// */
		e.aloudIs( true );
		e.interpret( System.in );
		// */
}	}
