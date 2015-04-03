package com.yagadi.enguage.concept;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Repertoire extends Signs {
	final   static long serialVersionUID = 0l;
	private static Audit           audit = new Audit( "Repertoire" );
	//private static boolean         debug = Audit.isOn();
	
	private static final String        PREFIX = Reply.helpPrefix();
	public  static final String PRONUNCIATION = "repper-to-are";  // better than  ~wah
	public  static final String PLURALISATION = "repper-to-wahs"; // better than ~ares
	public  static final String          NAME = "repertoire";
	
	public Repertoire( String name ) { super( name );}
	public Repertoire( String name, Sign[] signs ) {
		this( name );
		for (Sign sign: signs )
			insert( sign );
	}

	private Strings helpItems( String name, boolean html ) {
		Strings items = new Strings();
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		//for (Sign s : this ) {
		while (i.hasNext()) {
			Map.Entry<Integer,Sign> me = (Map.Entry<Integer,Sign>)i.next();
			Sign s = me.getValue();
		
			if (null != s.help() &&
				(null == s.concept() || s.concept().equalsIgnoreCase( name )))
			{	String helpDesc = s.help();
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
	public String helpToString() { return helpToString( prime ); }
	public String helpToString( String name ) {
		String output = helpToString( name, PREFIX, ".", false );
		return output != "" ? output :
				name.equals( prime ) ? 
					"sorry, aural help is not yet configured" :
					"sorry, there appears to be no aural help for "+name ;
	}
	
	/* this is a general how to get help function returning some string on
	 * whether the user will be able to see the screen, and if app is in 
	 * preview mode. This is dependent on the operation of the app!
	 * TODO: put this into config.xml
	 */
	static public String help = "to get help, just say help";
	static public String help() { return help; }
	static public void   help( String msg ) { help = msg; }

	static private String prime = "";
	static public  void prime( String name ) { prime = name; }
	static public  String prime() { return prime; }
	
	public static void main( String[] args ) {
		Repertoire r = new Repertoire( "test" );
		r.insert(
			new Sign().content( new Tag(  "describe ", "x", "."))
				.attribute( "id", "test" )
				.attribute( "engine", "describe X" )
				.help( "where x is a repertoire" )
		);
		r.insert(
			new Sign().content( new Tag(  "list repertoires ", "", "."))
				.attribute( "id", "test" )
				.help( "" )
		);
		audit.audit( r.helpToString( "test" ));
		audit.audit( r.helpToHtml(   "test" ));
}	}

