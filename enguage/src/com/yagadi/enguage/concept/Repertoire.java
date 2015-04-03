package com.yagadi.enguage.concept;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Repertoire extends Signs {
	static final long serialVersionUID = 0l;
	//static private Audit audit = new Audit( "Repertoire" );

	static private final String        PREFIX = Reply.helpPrefix();
	static  public final String PRONUNCIATION = "repper-to-are";  // better than  ~wah
	static  public final String PLURALISATION = "repper-to-wahs"; // better than ~ares
	static  public final String          NAME = "repertoire";
	static  public final String       DEFAULT = "iNeed";
	
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
						+ Shell.stripTerminator( s.content().toText())
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
	static private void    namesAdd(    String name ) {
		if (names.contains( name ))
			audit.ERROR("Repertoire.load(): already loaded: "+ name );
		else
			names.add( name );
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
		Enguage.e.signs.remove( concept );
		namesRemove( lastLoaded() );
	}
	static public boolean load( String name ) {
		audit.traceIn("load", "'"+ name +"'");
		boolean loaded = false;
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
			if (Enguage.e.ctx != null) {
				String aname = name+".txt";
				try {
					AssetManager am = Enguage.e.ctx.getAssets();
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
		
		lastLoaded( nowLoading() );
		nowLoading( "" );
		namesAdd( lastLoaded() );
		audit.traceOut();
		return loaded;
	}
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

