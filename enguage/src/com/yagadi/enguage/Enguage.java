package com.yagadi.enguage;

import java.io.File;

import com.yagadi.enguage.concept.Autopoiesis;
import com.yagadi.enguage.concept.Engine;
import com.yagadi.enguage.concept.Repertoire;
import com.yagadi.enguage.concept.Tag;
import com.yagadi.enguage.expression.Colloquial;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Overlay;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

import android.app.Activity;

public class Enguage extends Shell {
	static private Audit audit = new Audit( "Enguage" );
	static private boolean debug = Audit.isOn();

	static final public  String              name = "enguage";
	static final public  String    configFilename = "config.xml";
	
	// global DEBUGging switches...
	static final public  boolean runtimeDebugging = false;
	static final private boolean startupDebugging = false;
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
	public Activity     ctx = null;
	
	// are we taking the hit of creating / deleting overlays
	private boolean undoEnabled = false;
	public  boolean undoIsEnabled() { return undoEnabled; }
	public  Enguage undoEnabledIs( boolean enabled ) { undoEnabled = enabled; return this; }
	
	private boolean understood = false;
	public  boolean lastReplyWasUnderstood() { return understood; }
	private void    lastReplyWasUnderstood( boolean was ) { understood = was; }

	private String lastOutput = null;
	public  String lastOutput( String l ) { return lastOutput = l; }
	public  String lastOutput() { return lastOutput; }

	/* 
	 * To be used in identifying ambiguity...
	 */
	private Strings lastInput = null;
	public  Strings lastInput( Strings sa ) { return lastInput = sa; }
	public  Strings lastInput() { return lastInput; }

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
	public Repertoire signs = new Repertoire( "users" );
	public Repertoire autop = new Repertoire( "autop", Autopoiesis.autopoiesis );
	public Repertoire engin = new Repertoire( "engin", Engine.commands );
	
	// ----- read concepts used in main e
	private boolean initialising = false;
	//private boolean isInitialising() { return initialising; }
	private boolean initialisingIs( boolean b ) { return initialising = b; }
	
	private void loadConcepts( Tag concepts ) {
		audit.traceIn( "loadConcepts", "" );
		if (null != concepts) {
			initialisingIs( true );
			audit.audit( "Found: "+ concepts.content().size() +" concept(s)" );
			for (int j=0; j<concepts.content().size(); j++) {
				String name = concepts.content().get( j ).name();
				if (name.equals( "concept" )) {
					String	op = concepts.content().get( j ).attribute( "op" ),
							id = concepts.content().get( j ).attribute( "id" );
					
					// get default also from config file: ensure def is at least set to last rep name  
					audit.audit( "id="+ id +", op="+ op );
					if (op.equals( "default" )) {
						audit.audit( "Default repertoire is "+ id );
						Repertoire.def( id );
					}
					
					if (silentStartup()) Audit.suspend();
					
					if (op.equals( "load" ) || op.equals( "default" ))
						Repertoire.load( id ); // using itself!!
					else if (op.equals( "unload" ))
						Repertoire.unload( id ); // using itself!!
					else if (!op.equals( "ignore" ))
						audit.ERROR( "unknown op "+ op +" on reading concept "+ name );
					
					if (silentStartup()) Audit.resume();
			}	}
			
			// if we're still on default, but default ain't there
			if (Repertoire.def().equals( Repertoire.DEFAULT )
			 && !Repertoire.names().contains( Repertoire.DEFAULT )) {
				audit.audit( "Using "+ Repertoire.lastLoaded() +" as default" );
				Repertoire.def( Repertoire.lastLoaded() ); // use last as default
			}
			
			initialisingIs( false );
		} else
			audit.ERROR( "Concepts tag not found!" );
		audit.traceOut();
	}
	//
	// operational code
	//
	public Reply innerterpret( Strings utterance ) {
		//sanity check here!
		if (utterance == null || utterance.size() == 0) return new Reply();
		
		//audit.traceIn( "innerterpret", utterance.toString());
		// preprocess the utterance
		Strings u =  Variable.deref(
				utterance.normalise() // here: "i am/." becomes "i/am/."
		).decap(); // dereference anything in the environment
		
		// ordering - to reduce runtime startup, check through autop first, as this gets called many times
		// e commands should go at the end in case they are overridden by user signs?
		Reply  r = null;
		if (initialising) {
			r = autop.interpret( u );
			if (Reply.DNU == r.getType()) {
				r = signs.interpret( u );
				if (Reply.DNU == r.getType())
					r = engin.interpret( u );
			}
		} else { // if not initialising, do user signs first
			r = signs.interpret( u );
			if (Reply.DNU == r.getType()) {
				r = engin.interpret( u ); // ...then e signs
				if (Reply.DNU == r.getType())
					r = autop.interpret( u ); // ...just in case
		}	}
		//audit.traceOut( r.toString() );
		return r;
	}
	
	/*
	 * This is separate from the c'tor as it uses itself to read the config file's txt files. 
	 */
	public void loadConfig( String fname ) {
		Audit.turnOn(); // -- uncomment this to debug startup
		String fullName = 
				Filesystem.root + File.separator
				+ "yagadi.com"  + File.separator
				+ fname;
		//prefer user data over assets
		Tag t = new File( fullName ).exists() ?
				  Tag.fromFile( fullName )
				: Tag.fromAsset( fname, ctx );
		if (t != null) {
			t = t.findByName( "config" );
			Reply.setContext( t.attributes());
			loadConcepts( t.findByName( "concepts" ));
		} else
			audit.ERROR( "Not found "+ fname );
		//// uncomment to sets debugging on at the end of initialisation!
		if (Enguage.runtimeDebugging) Audit.turnOn();
	}
	//
	// concept management -- above
	// *********************************************************** 

	@Override
	public String interpret( Strings utterance ) {
		if (debug) 
			audit.traceIn( "interpret", utterance.toString( Strings.CSV ));
		else // always audit what is said
			audit.audit( utterance.toString( Strings.SPACED ));

		// just to say things are working...
		Engine.spoken( true );
		
		// we're looking to find if "No, ..." is in this utterance, so
		// normally simply set as off here, BUT we may have left it on from last time...
		//Engine.disambFound( false );
		
		// obtain a reply
		if (lastReplyWasUnderstood()) o.startTxn( undoIsEnabled()); // all work in this new overlay
		Reply r =
			innerterpret(
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
			audit.audit( "Enguage:interpret():not understood, forget ignores: "+ Enguage.e.signs.ignore().toString());
			Enguage.e.signs.ignoreNone();
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
		e.loadConfig( configFilename );
		System.out.println( e.copyright() );
		System.out.println( "Enguage main(): overlay is: "+ Overlay.Get().toString());
		//e.signs.show();
		//*
//		e.interpret( new Strings( "i need a cup of coffee." ));
//		e.interpret( new Strings( "i need another 3 cups of coffee." ));
		e.interpret( new Strings( "i need 5 packets of ground coffee and carrots and potatoes." ));
		e.interpret( new Strings( "i need peas and gravy and 5 bottles of real ale." ));
		//e.interpret( new Strings( "i need another cup of coffee." ));
		//e.interpret( new Strings( "i need carrots and potatoes." ));
		e.interpret( new Strings( "what do i need." ));
		e.interpret( new Strings( "i don't need anything." ));
		// */
		//e.interpret( System.in );
		// */
}	}
