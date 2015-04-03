package com.yagadi.enguage.concept;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Language;
import com.yagadi.enguage.expression.Plural;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Autoload {
	/* Implements Dynamic Repertoires:
	 * attempts to load all words in an utterance, singularly, as a repertoire.
	 */
	private static Audit           audit = new Audit( "Autoload" );
	private static boolean         debug = Enguage.runtimeDebugging; //  false;
	
	private static int ttl = 5;
	public  static void ttl( String age ) { try { ttl = Integer.valueOf( age ); } catch( Exception e ) {}}
	
	public static int autoloading = 0;
	public static void    ing( boolean al ) { if (al || autoloading>0) autoloading += al ? 1 : -1; }
	public static boolean ing() { return autoloading>0; }

	static private TreeMap<String,Integer> autoloaded = new TreeMap<String,Integer>();
	
	static public  Strings load( Strings utterance ) {
		// should not be called if initialising or if autoloading
		if (ing()) {
			audit.ERROR("prog error: called autoload.load() while already autoloading" );
		} else {
			if (debug) audit.traceIn( "load", "utterance="+ utterance );
			Autoload.ing( true );
			Engine.undoEnabledIs( false ); // disable undo while loading repertoires
			
			for (String repertoire : utterance )
				if (!Language.isQuoted( repertoire )// don't try to load: anything that is quoted, ...
					&&	!repertoire.equals(",")             // ...punctuation, ...
					&&	!Strings.isUpperCase( repertoire )) // ...hotspots, ...
				{
					// let's just singularise it: needs -> need
					if (Plural.isPlural( repertoire ))
						repertoire = Plural.singular( repertoire );
					if (Repertoires.names().contains( repertoire )) {// don't load...
						audit.debug( "already loaded on init: "+ repertoire );
					} else if (null==autoloaded.get( repertoire )) { //...stuff already loaded.
						if (Repertoires.loadSigns( repertoire )) {
							audit.debug( "autoloaded: "+ repertoire );
							autoloaded.put( repertoire, 0 ); // just loaded so set new entry to age=0
						} // ignore, if no repertoire!
					} else { // already exists, so reset age to 0
						audit.debug( "resetting age: "+ repertoire );
						autoloaded.put( repertoire, 0 );
				}	}
			
			Engine.undoEnabledIs( true );
			Autoload.ing( false );
			if (debug) audit.traceOut();
		}
		return utterance;
	}
	static public void unload() {
		if (ing()) {
			if (debug) audit.traceIn( "unload", "" );
			
			// read repertoires to remove/age
			Strings repsToRemove = new Strings();
			Set<Map.Entry<String,Integer>> set = autoloaded.entrySet();
			Iterator<Map.Entry<String,Integer>> i = set.iterator();
			while(i.hasNext()) {
				Map.Entry<String,Integer> me = (Map.Entry<String,Integer>)i.next();
				String repertoire = me.getKey();
				Integer nextVal = me.getValue() + 1;
				if (nextVal > ttl) {
					repsToRemove.add( repertoire );
				} else {
					audit.debug( "ageing (now="+ nextVal +"): "+ repertoire );
					autoloaded.put( repertoire, nextVal );
			}	}
			
			// now do the removals...
			Iterator<String> ri = repsToRemove.iterator();
			while (ri.hasNext()) {
				String repertoire = ri.next();
				audit.debug( "unloaded: "+ repertoire );
				Repertoires.signs.remove( repertoire );
				autoloaded.remove( repertoire );
			}
			if (debug) audit.traceOut();
}	}	}
