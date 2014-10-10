package com.yagadi.enguage.concept;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Language;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.util.Attribute;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Engine extends Attribute {

	static private Audit audit = new Audit( "Engine" );

	public static final String NAME = "engine";
	public static final Sign commands[] = {
		//new Sign().attribute( NAME,   "load $NAME" ).content( new Tag(  "load ", "NAME", ".")),
		//new Sign().attribute( NAME, "unload $NAME" ).content( new Tag("unload ", "NAME", ".")),
		//new Sign().attribute( NAME, "show $x" ).content( new Tag( "show ", "x", "." )),
		//new Sign().attribute( NAME, "show"    ).content( new Tag( "show",   "", "." )),
		//new Sign().attribute( NAME, "save"    ).content( new Tag( "save", "", "" ) ),
		//new Sign().attribute( NAME, "saveas $NAME" ).content( new Tag("saveas ", "NAME", ".")),
		//new Sign().attribute( NAME, "reload $NAME" ).content( new Tag("reload ", "NAME", ".")),
		new Sign().content( new Tag(    "enable undo.", "" )).attribute( NAME, "undo enable"  ),
		new Sign().content( new Tag(   "disable undo.", "" )).attribute( NAME, "undo disable" ),
		new Sign().content( new Tag(           "undo.", "" )).attribute( NAME, "undo"         ),
		new Sign().content( new Tag(      "say again.", "" )).attribute( NAME, "repeat"       ),
		//w Sign().content( new Tag(          "hello.", "" )).attribute( NAME, "help"         ),
		new Sign().content( new Tag(           "help.", "" )).attribute( NAME, "help"         ),
		new Sign().content( new Tag( "what can i say.", "" )).attribute( NAME, "repertoire"   ),
		new Sign().content( new Tag(      "debug ", "x", ".")).attribute( NAME, "debug $x"     ),
		new Sign().content( new Tag( "No ", "x", ".").attribute( "phrase", "phrase" )).attribute( NAME, "undo" ).attribute( "think", "X" )
	 };
	
	public Engine( String name, String value ) { super( name, value ); }
	
	// this supports the command="" attribute loaded in the creation of command data structure
	// needs "command //delete "...". -- to remove a tag, to support '"X" is meaningless.'
	private Reply unknownCommand( Reply r, String[] cmd ) {
		audit.ERROR( "Unknown command "+ Strings.toString( cmd, Strings.CSV ));
		return r.format( Reply.dnu() );
	}
	private static String repertoireHelpToString() {
		String output = "";
		for (Sign s : Enguage.interpreter.signs.list() )
			if (s.attributes().get("help") != "")
				output += ("you can say, "+ Language.stripTerminator( s.content().toText()) +", to " + s.attribute("help") +".");
		return output;
	}
	public static String repertoireHelpToHtml() {
		String output = "";
		// this may be called on startup
		if (Enguage.interpreter != null && Enguage.interpreter.signs != null)
			for (Sign s : Enguage.interpreter.signs.list() )
				if (s.attributes().get("help") != "")
					output += ( "<b><i>"+ Language.stripTerminator( s.content().toText()) +"</i></b>, to " + s.attribute( "help" ) +"<br/>");
		return output;
	}
	public Reply mediate( Reply r ) {
		r.answer( Reply.yes()); // just to stop debug output look worrying
		
		String[] cmd = Reply.context().deref(
				Reply.context().getCommand(
						Strings.fromString( value )));
		
		if (cmd[ 0 ].equals( "load" )) {
			String[] files = Strings.copyAfter( cmd, 0 );
			for(int i=0; i<files.length; i++)
				Enguage.interpreter.load( files[ i ]);
			 
		//} else if (interpreter[ 0 ].equals( "unload" )) {
		//	String[] files = Strings.copyAfter( interpreter, 0 );
		//	for(int i=0; i<files.length; i++) Concepts.Unload( files[ i ]);

		//} else if (interpreter[ 0 ].equals( "reload" )) {
		//	String[] files = Strings.copyAfter( interpreter, 0 );
		//	for(int i=0; i<files.length; i++) Concepts.Unload( files[ i ]);
		//	for(int i=0; i<files.length; i++) Concepts.Load( files[ i ]);
/*
		} else if (interpreter[ 0 ].equals( "save" ) || interpreter[ 0 ].equalsIgnoreCase( "saveAs" )) {
			if (interpreter[ 0 ].equalsIgnoreCase( "saveAs" ) && ( interpreter.length != 2))
				System.err.println( interpreter[ 0 ] +": NAME required." );
			else {
				if (interpreter[ 0 ].equalsIgnoreCase( "saveAs" )) {
					//(re)NAME concept
					System.out.println( "renaming concept" );
				}
				//save concept
				System.out.println( "Saving concept" );
			}
*/
		} else if (cmd[ 0 ].equals( "debug" )) {
		//	; //setenv( "//DEBUG", !interpreter[ 1 ] || interpreter[ 1 ].equals( "on") ? "Enguage.c:Pattern.c":"", 1 );
			if (cmd.length <= 1) ; // do nothing -- prob won't get here 'cos it won't have matched.
			else if (cmd[ 1 ].equals( "on")) {
				Audit.turnOn();
				r.format( "ok" );
			} else if (cmd[ 1 ].equals( "off")) {
				Audit.turnOff();			
				r.format( "ok" );
			} else if (cmd[ 1 ].equals( "show" )) {
				Enguage.interpreter.signs.show();
				r.format( "ok" );
			} else
				r = unknownCommand( r, cmd );

		} else if ( value.equals( "repeat" )) {
			if (Enguage.interpreter.lastThingSaid() == null)
				r.format( Reply.dnu());
			else {
				r.repeated( true );
				r.format( Reply.repeatFormat());
				r.answer( Enguage.interpreter.lastThingSaid());
			}
//		} else if ( cmd[ 0 ].equals( "help" )) {
//			r.format( "you can say, what can i say" );
//			
//		} else if ( cmd[ 0 ].equals( "repertoire" )) {
			
		} else if ( cmd[ 0 ].equals( "help" ) || cmd[ 0 ].equals( "repertoire" )) { // until help is more than "what can i say?"
			// "you can say, "+ pattern.toString() + ", to "+ help.value() +"."
			String output = repertoireHelpToString();
			r.format( (String) (output=="" ? "sorry, aural help is not yet configured" : output) );
			
		} else if ( cmd[ 0 ].equals( "undo" )) {
			r.format( "ok" );
			if (cmd.length == 2)
				if (cmd[ 1 ].equals( "enable" )) 
					Enguage.interpreter.undoEnabledIs( true );
				else if (cmd[ 1 ].equals( "disable" )) 
					Enguage.interpreter.undoEnabledIs( false );
				else 
					r = unknownCommand( r, cmd );
			else if (cmd.length == 1 && Enguage.interpreter.undoIsEnabled()) 
				Enguage.interpreter.reStartTxn();
			else if (!Enguage.interpreter.undoIsEnabled())
				r.format( Reply.dnu() );
			else
				r = unknownCommand( r, cmd );
				
		} else
			r = unknownCommand( r, cmd );
		return r;
}	}