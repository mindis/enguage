package com.yagadi.enguage.concept;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Strings;

public class Repertoire extends Signs {
	static final long serialVersionUID = 0l;
	private static Audit audit = new Audit( "Repertoire" );

	private static final String        PREFIX = Reply.helpPrefix();
	public  static final String PRONUNCIATION = "repper-to-are";  // better than  ~wah
	public  static final String PLURALISATION = "repper-to-wahs"; // better than ~ares
	public  static final String          NAME = "repertoire";
	public  static final String       DEFAULT = "need";
	
	public Repertoire( String name ) { super( name );}
	public Repertoire( String name, Sign[] signs ) {
		this( name );
		for (Sign sign: signs )
			insert( sign );
	}
		
	private Strings helpItems( String name, boolean html ) {
		Strings items = new Strings();
		for (Sign s : this ) {
			if (s.attributes().has("help") &&
				(!s.attributes().has("id")
					|| s.attributes().get("id").equalsIgnoreCase( name )))
			{	String helpDesc = s.attribute( "help" );
				items.add( (html?"<b><i>":"")
						+ s.content().toText()
						+ (html?"</i></b> ":" ") +
						(helpDesc.equals("") ? "" : ", " + helpDesc));
		}	}
		return items;
	}
	private String helpToString( String name, String fore, String aft, boolean html ) {
		Strings ss = helpItems( name, html );
		return ss.size() > 0 ? fore + " " + ss.toString( Reply.andListFormat() ) + aft : "";
	}
	public String helpToHtml( String name ) {
		return helpItems( name, true ).toString( "", "<br/>", "" );
	}
	public String helpToString() { return helpToString( def ); }
	public String helpToString( String name ) {
		String output = helpToString( name, PREFIX, ".", false );
		return output != "" ? output :
				name.equals( def ) ? 
					"sorry, aural help is not yet configured" :
					"sorry, there appears to be no aural help for "+name ;
	}
	
	/* this is a general how to get help function returning some string on
	 * whether the user will be able to see the screen, and if app is in 
	 * preview mode. This is dependent on the operation of the app!
	 */
	static public String help = "to get help, just say help";
	static public String help() { return help; }
	static public void   help( String msg ) { help = msg; }

	static private String def = DEFAULT;
	static public  void def( String name ) { def = name; }
	static public  String def() { return def; }
	
	/* repertoire management...
	 * Loading and unloading repertoires
	 */
	static private Strings names = new Strings();
	static private boolean namesAdd(    String name ) {
		if (names.contains( name )) {
			audit.ERROR("Repertoire.load(): already loaded: "+ name );
			return false;
		} else {
			audit.audit( "loading "+ name );
			names.add( name );
			return true;
		}
	}
	static private void    namesRemove( String name ) { names.remove( name ); }
	static public  Strings names() { return names; }
	
	static private String lastLoaded = "";
	static public  void   lastLoaded( String name ) { lastLoaded = name; }
	static public  String lastLoaded() { return lastLoaded; }
		
	static private String nowLoading = "";
	static public    void nowLoading( String name ) { nowLoading = name; }
	static public  String nowLoading() { return nowLoading; }
	
	static private boolean loadedDefaultRep = false;
	static public  boolean defaultRepIsLoaded() { return loadedDefaultRep; }
	static private void    defaultRepLoadedIs( boolean c ) { loadedDefaultRep = c; }
	
	static public void unload( String concept ) {
		Enguage.signs.remove( concept );
		namesRemove( concept );
	}
	static public boolean load( String name ) {
		audit.traceIn("load", "'"+ name +"'");
		boolean loaded = false;
		if (namesAdd( name )) {
			nowLoading( name );
		
			if (name.equals( Repertoire.DEFAULT ))
				defaultRepLoadedIs( true );
			
			// ...add content from file
			String path = Filesystem.location().equals("") ?
				Filesystem.root + File.separator+ "yagadi.com" :
				Filesystem.location(); // concepts path
			
			if (Enguage.silentStartup()) {
				Audit.suspend();
				Enguage.e.aloudIs( false );
			}
			
			String fname = path+File.separator+ name+".txt";
			try {
				FileInputStream fis = new FileInputStream( fname );
				Enguage.e.interpret( fis );
				fis.close();
				audit.debug("Loaded concept from "+ fname );
				loaded = true;
			} catch (IOException e1) {
				// ...if not found add content from asset
				if (Enguage.e.ctx() != null) {
					String aname = name+".txt";
					try {
						AssetManager am = Enguage.e.ctx().getAssets();
						InputStream is = am.open( aname );
						Enguage.e.interpret( is );
						is.close();
						audit.debug("Loaded concept from "+ aname );
						loaded = true;
					} catch (IOException e2 ) {
						audit.ERROR(  "Can't find concept: '"
									+ path +"/"+ name +"': "+ e1.toString()+ "\n"
									+ "                or: '"
									+ aname +"': "+ e2.toString() );
			}	}	}
			
			if (!Enguage.silentStartup()) {
				Audit.resume();
				Enguage.e.aloudIs( true );
			}
			
			lastLoaded( name );
			nowLoading( "" );
		}
		audit.traceOut();
		return loaded;
	}
	
	
	
	
	
	// ----- read concepts used in main e
	static private boolean initialising = false;
	//private boolean isInitialising() { return initialising; }
	static private boolean initialisingIs( boolean b ) { return initialising = b; }
	
	static private void loadConcepts( Tag concepts ) {
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
					
					if (Enguage.silentStartup()) Audit.suspend();
					
					if (op.equals( "load" ) || op.equals( "default" ))
						Repertoire.load( id ); // using itself!!
					else if (op.equals( "unload" ))
						Repertoire.unload( id ); // using itself!!
					else if (!op.equals( "ignore" ))
						audit.ERROR( "unknown op "+ op +" on reading concept "+ name );
					
					if (Enguage.silentStartup()) Audit.resume();
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
	static public Reply innerterpret( Strings utterance ) {
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
			r = Enguage.autop.interpret( u );
			if (Reply.DNU == r.getType()) {
				r = Enguage.signs.interpret( u );
				if (Reply.DNU == r.getType())
					r = Enguage.engin.interpret( u );
			}
		} else { // if not initialising, do user signs first
			r = Enguage.signs.interpret( u );
			if (Reply.DNU == r.getType()) {
				r = Enguage.engin.interpret( u ); // ...then e signs
				if (Reply.DNU == r.getType())
					r = Enguage.autop.interpret( u ); // ...just in case
		}	}
		//audit.traceOut( r.toString() );
		return r;
	}
	
	/*
	 * This is separate from the c'tor as it uses itself to read the config file's txt files. 
	 */
	static public void loadConfig( String fname ) {
		//Audit.turnOn(); // -- uncomment this to debug startup
		String fullName = 
				Filesystem.root + File.separator
				+ "yagadi.com"  + File.separator
				+ fname;
		//prefer user data over assets
		Tag t = new File( fullName ).exists() ?
				  Tag.fromFile( fullName )
				: Tag.fromAsset( fname, Enguage.e.ctx() );
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

	
	
	
	
	
	
	public static void main( String[] args ) {
		Repertoire r = new Repertoire( "test" );
		r.insert(
			new Sign().content( new Tag(  "describe ", "x", "."))
				.attribute( "id", "test" )
				.attribute( "engine", "describe X" )
				.attribute( "help", "where x is a repertoire" )
		);
		r.insert(
			new Sign().content( new Tag(  "list repertoires ", "", "."))
				.attribute( "id", "test" )
				.attribute( "help", "" )
		);
		audit.audit( r.helpToString( "test" ));
		audit.audit( r.helpToHtml(   "test" ));
}	}

