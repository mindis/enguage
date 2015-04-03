package com.yagadi.enguage.concept;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;

import android.content.res.AssetManager;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Colloquial;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Strings;

public class Repertoires {
	static private       Audit           audit = new Audit( "Repertoires" );
	static private       boolean         debug = Enguage.runtimeDebugging;
	static public  final String  DEFAULT_PRIME = "need";

	/* This class maintains three repertoire groups - signs, autop and engine
	 * Each, well signs, contains signs from all runtime loaded repertoires and
	 * all autoloaded repertoires. Perhaps runtime loaded repertoires could go 
	 * in engine? 
	 */
	static public Repertoire signs = new Repertoire( "users" );
	static public Repertoire autop = new Repertoire( "autop", Autopoiesis.autopoiesis );
	static public Repertoire engin = new Repertoire( "engin", Engine.commands );
	
	// ----- read concepts used in main e
	static private boolean initialising = false;
	static public  boolean isInitialising() { return initialising; }
	static public  boolean initialisingIs( boolean b ) { return initialising = b; }
	
	/* this is used in creating the app help/settings title bar
	 * Not entirely sure that with dynamic repertoires this is no longer sensible.
	 *
	static private String lastLoaded = "";
	static public  void   lastLoaded( String name ) { lastLoaded = name; }
	static public  String lastLoaded() { return lastLoaded; }
	// */
	static private boolean loadedDefaultRep = false;
	static public  boolean defaultRepIsLoaded() { return loadedDefaultRep; }
	static private void    defaultRepLoadedIs( boolean c ) { loadedDefaultRep = c; }

	static public String location() {
		return (
			Filesystem.location().equals("") ?
 				Filesystem.root + File.separator+ "yagadi.com" :
 				Filesystem.location()
 			) + File.separator;
	}
	
	static public boolean loadSigns( String name ) { // to Repertoires
		/* Please note:
		 * when reading a repertoire, any autopoietic intention loads into the 
		 * Enguage.signs list of signs.
		 */
		//if (debug) audit.traceIn( "loadSigns", "'"+ name +"'" );
		
		boolean wasLoaded = false;
		
		/* all repertoires are loaded into the same data structure,
		 * just need to keep the loaded/autoloaded names apart
		 */
		
		// all signs created will be under this id...
		Autopoiesis.concept( name );
		//lastLoaded( name ); -- user the above

		if (name.equals( DEFAULT_PRIME ))
			defaultRepLoadedIs( true );
		
		//...silence on inner thought
		boolean wasSilenced = false;
		boolean wasAloud = Enguage.e.isAloud();
		if (!Enguage.startupDebugging && Enguage.silentStartup()) {
			wasSilenced = true;
			Audit.suspend(); // miss this out for debugging
			Enguage.e.aloudIs( false );
		}
		
		// ...add content from file
		
		/* if got here and we're autoloading we need to tell subsequent calls to
		 * Inrepertoire.interpret() to look in autop first
		 */
		String aname = name +".txt",
		       fname = location() + aname;
		try {
			FileInputStream fis = new FileInputStream( fname );
			Enguage.e.interpret( fis );
			fis.close();
			// And, if we've got this far...
			wasLoaded = true; 
		} catch (IOException e1) {
			// ...if not found add content from asset
			if (Enguage.e.ctx() != null) {
				try {
					AssetManager am = Enguage.e.ctx().getAssets();
					InputStream is = am.open( aname );
					Enguage.e.interpret( is );
					is.close();
					// And, if we've got this far...
					wasLoaded = true;
				} catch (IOException e2 ) {}
		}	}
		
		//...un-silence after inner thought
		if (!Enguage.startupDebugging && wasSilenced) {
			Audit.resume();
			Enguage.e.aloudIs( wasAloud );
		}
		
		
		//if (debug) audit.traceOut();
		return wasLoaded;
	}
	
	/* ============================================================================
	// Repertoire Management...
	//
	 */
	static private TreeSet<String> loaded = new TreeSet<String>();
	static public  TreeSet<String> names() { return loaded; }
	// backwards compatibility... STATICally load a repertoire file
	static public void loadConcept( String name ) {
		if (debug) audit.traceIn( "loadConcept", "name="+ name );
		
		// as with autoloading, make sure it is singular..
		//if (Plural.isPlural( name ))
		//	name = Plural.singular( name );
		
		// add in name as to what is loaded.
		if (!loaded.contains( name )) {
			Engine.undoEnabledIs( false ); // disable undo while loading repertoires
			if ( Repertoires.loadSigns( name )) {
				loaded.add( name );
				if (debug) audit.debug( "LOADED>>>>>>>>>>>>:"+ name );
			} else
				if (debug) audit.debug( "LOAD skipping already loaded:"+ name );
			Engine.undoEnabledIs( true );
		}
		if (debug) audit.traceOut();
	}
	static public void  unloadConcept( String name ) {
		// remove name from what is loaded
		if (debug) audit.traceIn( "unloadConcept", "name="+ name );
		
		// as with autoloading, make sure it is singular..
		//if (Plural.isPlural( name ))
		//	name = Plural.singular( name );
		
		if (loaded.contains( name )) {
			loaded.remove( name );
			Repertoires.signs.remove( name );
			if (debug) audit.debug( "UNLOADED<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<:"+ name );
		}
		if (debug) audit.traceOut();
	}
	/* This is the STATIC loading of concepts at app startup -- read
	 * from the config.xml file.
	 */
	static public void loadConcepts( Tag concepts ) {
		//if (debug) audit.traceIn( "loadConcepts", "" );
		if (null != concepts) {
			Repertoires.initialisingIs( true );
			audit.audit( "Found: "+ concepts.content().size() +" concept(s)" );
			for (int j=0; j<concepts.content().size(); j++) {
				String name = concepts.content().get( j ).name();
				if (name.equals( "concept" )) {
					String	op = concepts.content().get( j ).attribute( "op" ),
							id = concepts.content().get( j ).attribute( "id" );
					
					// get default also from config file: ensure def is at least set to last rep name  
					audit.audit( "id="+ id +", op="+ op );
					if (op.equals( "prime" )) {
						audit.audit( "Prime repertoire is '"+ id +"'" );
						Repertoire.prime( id );
					}
					
					if (Enguage.silentStartup()) Audit.suspend();
					
					if (op.equals( "load" ) || op.equals( "prime" ))
						loadConcept( id ); // using itself!!
					else if (op.equals( "unload" ))
						unloadConcept( id ); // using itself!!
					else if (!op.equals( "ignore" ))
						audit.ERROR( "unknown op "+ op +" on reading concept "+ name );
					
					if (Enguage.silentStartup()) Audit.resume();
			}	}
			Repertoires.initialisingIs( false );
		} else
			audit.ERROR( "Concepts tag not found!" );
		//if (debug) audit.traceOut();
	}
	//
	// Repertoire Management -- above
	// *********************************************************** 

	static public Reply interpret( Strings utterance ) {
		//sanity check here!
		if (utterance == null || utterance.size() == 0) return new Reply();
		
		if (debug) audit.traceIn( "interpret", utterance.toString());
		// preprocess the utterance
		Strings u = Colloquial.applyIncoming( // I am => _user is + I'm => I am
					/*	Variable.deref(*/
							utterance.normalise() // here: "i am/." becomes "i/am/."
					/*)*/	).decap(); // dereference anything in the environment
		
		// perf ordering - to reduce runtime startup, check through autop first, as this 
		// gets called many times
		// e commands should go at the end in case they are overridden by user signs?
		Reply r = null;
		if (isInitialising() || Autoload.ing()) {
			//if (debug) audit.audit( "CHECKING AUTOP=>SIGNS=>ENGINE");
			r = autop.interpret( u );
			if (Reply.DNU == r.getType()) {
				r = signs.interpret( u );
				if (Reply.DNU == r.getType())
					r = engin.interpret( u );
			}
		} else { // if not initialising, do user signs first
			//if (debug) audit.audit( "AUTOLOAD, CHECKING signs=>engine=>autop");
			u = Autoload.load( u );// ...autoload from expanded utterance...
			r = signs.interpret( u );
			if (Reply.DNU == r.getType()) {
				r = engin.interpret( u ); // ...then e signs
				if (Reply.DNU == r.getType())
					r = autop.interpret( u ); // ...just in case
		}	}
		if (debug) audit.traceOut( r.toString() );
		return r;
	}
}
