package com.yagadi.enguage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import com.yagadi.enguage.concept.Autopoiesis;
import com.yagadi.enguage.concept.Engine;
import com.yagadi.enguage.concept.Sign;
import com.yagadi.enguage.concept.Signs;
import com.yagadi.enguage.concept.Tag;
import com.yagadi.enguage.expression.Colloquials;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Overlay;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

import android.app.Activity;
import android.content.res.AssetManager;

public class Enguage extends Shell {
	static private Audit audit = new Audit( "Enguage" );

	final public static String              name = "enguage";
	final public static String defaultRepertoire =   "iNeed";
	
	static public Overlay           o = null;
	public Signs         signs = new Signs();
	public Signs         autop = new Signs();
	public Signs         engin = new Signs();
	/*
	 * This singleton is managed here because the Engine code needs to know about
	 * the state that the interpreter is in, but won't have access to it unless the
	 * calling code sets up that access.
	 */
	static public Enguage interpreter = null;

	private Activity ctx = null;
	
	// are we taking the hit of creating / deleting overlays
	private boolean undoEnabled = false;
	public  boolean undoIsEnabled() { return undoEnabled; }
	public  Enguage undoEnabledIs( boolean enabled ) { undoEnabled = enabled; return this; }
	
	private boolean understood = false;
	public  boolean lastReplyWasUnderstood() { return understood; }
	private void    lastReplyWasUnderstood( boolean was ) { understood = was; }

	private String lastThingSaid = null;
	public  String lastThingSaid( String l ) { return lastThingSaid = l; }
	public  String lastThingSaid() { return lastThingSaid; }

	static private Colloquials user = new Colloquials();
	static public  Colloquials user() { return user; }
	static public  void user( Colloquials c ) {user = c;}

	private boolean loadedDefaultRep = false;
	public  boolean defaultRepIsLoaded() { return loadedDefaultRep; }
	private void    defaultRepLoadedIs( boolean c ) { loadedDefaultRep = c; }
	
	/*public static void xinit( Activity a ) {
		Enguage.interpreter = new Enguage( a );
		Enguage.interpreter.loadConfig();
	}*/
	public Enguage( Activity a ) {
		super( "Enguage" );
		ctx = a;
		
		if (null == o) Overlay.Set( o = new Overlay()); // use/provide global overlay 
		if (!o.attached()) {
			String rc = Overlay.autoAttach();
			if (!rc.equals( "" ))
				audit.ERROR( "Ouch! Cannot autoAttach() to object space: "+ rc );
		}
		
		for (Sign s: Autopoiesis.autopoiesis ) 
			autop.insert( s );
		for (Sign s: Engine.commands ) 
			engin.insert( s );

		if (null == System.getProperty( "HOST" ))
			System.setProperty( "HOST", "phone" );
	}
	
	//
	// concept management --
	// 

	// ----- read concepts used in main interpreter
	private static String lastLoaded = "";
	public  static   void lastLoaded( String name ) { lastLoaded = name; }
	public  static String lastLoaded() { return lastLoaded; }
		
	private static String nowLoading = "";
	public  static   void nowLoading( String name ) { nowLoading = name; }
	public  static String nowLoading() { return nowLoading; }
	
	public void  unload( String concept ) { signs.remove( concept ); }
	public boolean load( String concept ) {
		//audit.traceIn("Load", "'"+ name +"'");
		boolean loaded = false;
		aloudIs( false ); // comment this out for runtime checking of config interp
		nowLoading( concept );
		
		if (concept.equals( defaultRepertoire )) defaultRepLoadedIs( true );
		
		// ...add content from file
		String path = Filesystem.location().equals("") ?
			Filesystem.root + File.separator+ "yagadi.com" :
			Filesystem.location(); // concepts path
		
		String fname = path+File.separator+ concept+".txt";
		try {
			FileInputStream fis = new FileInputStream( fname );
			interpret( fis );
			fis.close();
			audit.debug("Loaded concept from "+ fname );
			loaded = true;
		} catch (IOException e1) {
			// ...if not found add content from asset
			if (ctx != null) {
				String aname = concept+".txt";
				try {
					AssetManager am = ctx.getAssets();
					InputStream is = am.open( aname );
					interpret( is );
					is.close();
					audit.debug("Loaded concept from "+ aname );
					loaded = true;
				} catch (IOException e2 ) {
					audit.ERROR(  "Can't find concept: '"
								+ path +"/"+ concept +"': "+ e1.toString()+ "\n"
								+ "                or: '"
								+ aname +"': "+ e2.toString() );
		}	}	}
		lastLoaded( nowLoading() );
		nowLoading( "" );
		aloudIs( true );
		//audit.traceOut();
		return loaded;
	}
	/*
	 * This is separate from the c'tor as it uses itself to read the config file's txt files. 
	 */
	public void loadConfig() {
		//////		Audit.turnOn(); // to turn on debugging on startup
		String fname = "config.xml";
		String fullName = 
				Filesystem.root + File.separator
				+ "yagadi.com"  + File.separator
				+ fname;
		//audit.audit( "config file:"+ (null == ctx ? fname : fullName ));
		
		//prefer user data over assets
		Tag t = new File( fullName ).exists() ? Tag.fromFile( fullName ) : Tag.fromAsset( fname, ctx );
		
		Audit.suspend();
		if (t != null) {
			// config is now wrapped in xml
			t = t.findByName( "config" );
			
			audit.debug( "Adding context............" );
			Reply.setContext( t.attributes());
			
			audit.debug( "Adding concepts............" );
			audit.incr();
			
			Tag concepts = t.findByName( "concepts" );
			if (null != concepts) {
				audit.audit( "Found: "+ concepts.content().length() +" concept(s)" );
				for (int j=0; j<concepts.content().length(); j++) {
					String name = concepts.content().ta()[ j ].name();
					if (name.equals( "concept" )) {
						String	op = concepts.content().ta()[ j ].attribute( "op" ),
								id = concepts.content().ta()[ j ].attribute( "id" );
						audit.audit( "id="+ id +", op="+ op );
						if (op.equals( "load" ))
							load( id ); // using itself!!
						else if (op.equals( "unload" ))
							unload( id ); // using itself!!
						else if (!op.equals( "ignore" ))
							audit.ERROR( "unknown op "+ op +" on reading concept "+ name );
				}	}
			} else
				audit.ERROR( "Concepts tag not found!" );
			audit.decr();
		} else
			audit.ERROR( "Not found "+ fname );
		Audit.resume(); // should be turned off
		//////	
		Audit.turnOff(); // incase it was turned on above!
		//// this sets debugging on at the end of initialisation!
		//Audit.turnOn();
	}

	//
	// operational code
	//
	public Reply innerterpret( String[] utterance ) {
		//audit.traceIn( "innerterpret", Strings.toString( utterance, Strings.CSV));
		
		// preprocess the utterance
		String[] u = Strings.decap( // here: "i am/." becomes "i/am/." 
				Variable.deref( // dereference anything in the environment
						Strings.replace( utterance, Strings.dotDotDot, Strings.ellipsis )
			)	);
		
		// ordering - to reduce runtime startup, check through autop first, as this gets called many times
		// engine commands should go at the end in case they are overridden by user signs?
		Reply r = autop.interpret( u );
		if (Reply.DNU == r.getType()) {
			r = signs.interpret( u );
			if (Reply.DNU == r.getType())
				r = engin.interpret( u );
		}
		//audit.traceOut( r.toString());
		return r;
	}
	
	// transaction bit -- this isn't ACID :(
	private boolean inTxn = false;
	public void startTxn() {
		if (undoIsEnabled()) {
			inTxn = true;
			o.createOverlay();
	}	}
	public void finishTxn() {
		if (undoIsEnabled()) {
			inTxn = false;
			o.combineUnderlays();
	}	}
	public void reStartTxn() {
		if (inTxn) {
			o.destroyTop(); // remove this overlay
			o.destroyTop(); // remove previous -- this is the undo bit
			o.createOverlay(); // restart a new txn
	}	}
	
	@Override
	public String interpret( String[] utterance ) {
		//audit.traceIn( "interpret", Strings.toString( utterance, Strings.CSV ));
		
		// obtain a reply
		if (lastReplyWasUnderstood()) startTxn(); // all work in this new overlay
		Reply r = innerterpret( user().apply( Reply.both().disapply( utterance )) );
		lastReplyWasUnderstood( Reply.DNU != r.getType() );
		if (lastReplyWasUnderstood()) finishTxn(); // Accept the previous overlay.
		
		// convert that reply into text
		String reply = r.toString();
		if ( lastReplyWasUnderstood() ) {
			if (!r.repeated()) lastThingSaid( reply );
		} else {
			aloudIs( true ); // sets aloud for whole session if reading from fp
			// put in "dnu, '...'" here? -- if aloud()?
			r.handleDNU( utterance );
			reply = r.toString();
		}
		 
		//audit.traceOut( reply ); 
		return reply;
	}
		
	public static void main( String[] args ) {
		interpreter = new Enguage( null );
		interpreter.loadConfig();
		System.out.println( interpreter.copyright() );
		System.out.println( "Enguage main(): overlay is: "+ Overlay.Get().toString());
		interpreter.interpret( System.in );
}	}
