package com.yagadi.enguage;

import java.io.File;
import java.util.GregorianCalendar;

import android.app.Activity;

import com.yagadi.enguage.concept.Autoload;
import com.yagadi.enguage.concept.Engine;
import com.yagadi.enguage.concept.Repertoires;
import com.yagadi.enguage.concept.Signs;
import com.yagadi.enguage.concept.Tag;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Overlay;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Enguage extends Shell {
	static       private Audit           audit = new Audit( "Enguage" );
	static final public  String           name = "enguage";
	static final public  String configFilename = "config.xml";
	
	// global DEBUGging switches...
	static final public boolean  startupDebugging = false;
	static final public boolean  runtimeDebugging = false;
	static final public boolean detailedDebugging = false;
	static final public boolean   timingDebugging = false;
	static final public boolean  numericDebugging = false;
	
	static public  boolean silentStartup() { return !startupDebugging; }
	
	/* This singleton is managed here because the Engine code needs to know about
	 * the state that the e is in, but won't have access to it unless the
	 * calling code sets up that access.
	 */
	static public Enguage e = null; // TODO: remove this have all access as static
	static public Overlay o = null; // TODO: ditto

	private Activity ctx = null;
	public  Activity ctx() { return ctx; }
	
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
	/*
	 * This is separate from the c'tor as it uses itself to read the config file's txt files. 
	 */
	static public void loadConfig( String fname ) {
		Audit.turnOff();
		if (Enguage.startupDebugging) Audit.turnOn();
		Engine.undoEnabledIs( false );

		long then = new GregorianCalendar().getTimeInMillis();
		
		String fullName = Repertoires.location() + fname;
		//prefer user data over assets
		Tag t = new File( fullName ).exists() ?
				  Tag.fromFile( fullName )
				: Tag.fromAsset( fname, Enguage.e.ctx() );
		if (t != null) {
			t = t.findByName( "config" );
			Reply.setContext( t.attributes());
			Repertoires.loadConcepts( t.findByName( "concepts" ));
		} else
			audit.ERROR( "Not found "+ fname );
		
		// turn on undo from within
		Engine.undoEnabledIs( true );
		
		long now = new GregorianCalendar().getTimeInMillis();
		audit.audit( "Initialisation in: "+ (now - then) +"ms" );
		audit.audit( Signs.stats() );
		
		Audit.turnOff();
		if (Enguage.runtimeDebugging) Audit.turnOn();
	}

	@Override
	public String interpret( Strings utterance ) {
		if (runtimeDebugging) 
			audit.traceIn( "interpret", utterance.toString( Strings.CSV ));
		//else // always audit what is said
		//	audit.audit( ">>>> "+ utterance.toString( Strings.SPACED ));

		
		// we're looking to find if "No, ..." is in this utterance, so
		// normally simply set as off here, BUT we may have left it on from last time...
		//Engine.disambFound( false );

		if (lastReplyWasUnderstood())
			o.startTxn( Engine.undoIsEnabled()); // all work in this new overlay
		
		// obtain a reply
		Reply r = Repertoires.interpret( utterance );
		
		lastReplyWasUnderstood( Reply.DNU != r.getType() );
		if (lastReplyWasUnderstood()) {
			// tick the box to say user is effective...
			Engine.spoken( true );
			// Accept the previous overlay.
			o.finishTxn( Engine.undoIsEnabled());
		}
		
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
			audit.audit( "Enguage:interpret():not understood, forget ignores: "+ Repertoires.signs.ignore().toString());
			Repertoires.signs.ignoreNone();
			aloudIs( true ); // sets aloud for whole session if reading from fp
			// put in "dnu, '...'" here? -- if aloud()?
			r.handleDNU( utterance );
			reply = r.toString();
		}
		
		// TODO: ..try only to unload anything not used.
		// if no last autoloaded list save autoloaded list as last autoloaded list
		// if there is, remove any of those that are not in this utterance.
		//        Then, save this autoloaded list as last autoloaded list
		
		if (!Repertoires.isInitialising() && !Autoload.ing()) {
			Autoload.unload();
		}

		if (runtimeDebugging) audit.traceOut( reply );
		return reply;
	}
	// ==== test code =====
	private static void test( String cmd ) {
		audit.audit( cmd );
		audit.audit( "Enguage.main()> "+ e.interpret( new Strings( cmd )));
	}
	public static void main( String args[] ) {
		
		// set where we get repertoire files from...
		// test here so /home/martin/workspace/iNeed/assets
		Filesystem.location( "/home/martin/workspace/iNeed/assets" );
		
		e = new Enguage( null );
		loadConfig( configFilename );

		
		audit.audit( e.copyright() );
		audit.audit( "Enguage main(): overlay is: "+ Overlay.Get().toString());
		
		test( "i don't need anything" );
		//Repertoires.signs.show();
		//Audit.turnOn();
		/*
		test( "i need another 3 cups of coffee" );
		test( "what do i need" );
		test( "i need 5 packets of ground coffee and carrots and potatoes" );
		test( "i don't need anything" );
		test( "i need milk and coffee and ground coffee" );
		test( "i need a cup of coffee" );
		// */
		//test( "i need 3 coffees" );
		//test( "no i need 5 coffees" );
		
		e.aloudIs( true );
		e.run();
}	}
