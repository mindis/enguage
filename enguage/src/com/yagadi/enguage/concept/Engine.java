package com.yagadi.enguage.concept;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

/*
 * TODO: Engine should be split into the NAME/value pair and the generic repertoire. Discuss. 
 *       Start by introducing Repertoire.class => Signs (Signs ArrayList of Sign!)
 */
public class Engine extends Intention {
	private static Audit audit = new Audit( "Engine" );

	public static final String NAME = "engine";
	public static final String HELP = "help";
	public static final Sign commands[] = {
		// because these are raw signs, they MUST be terminated by full stops.
		new Sign().content( new Tag(  "describe ", "x" )).attribute( "id", NAME   )
															 .attribute( NAME, "describe X" )
															 .attribute( HELP, "where x is a repertoire" ),
		new Sign().content( new Tag("list repertoires","" )).attribute( "id", NAME   )
															 .attribute( NAME, "list" )
															 .attribute( HELP, ""     ),
		new Sign().content( new Tag(           "help", "" )).attribute( NAME, "help" ),
		new Sign().content( new Tag(          "hello", "" )).attribute( NAME, "hello"),
		new Sign().content( new Tag(        "welcome", "" )).attribute( NAME, "welcome"),
		new Sign().content( new Tag( "what can i say", "" )).attribute( "id", NAME   )
															 .attribute( NAME, "repertoire"  )
															 .attribute( HELP, ""            ),
		new Sign().content( new Tag(   "load ", "NAME" )).attribute( NAME,   "load NAME" ),
		new Sign().content( new Tag( "unload ", "NAME" )).attribute( NAME, "unload NAME" ),
		new Sign().content( new Tag( "reload ", "NAME" )).attribute( NAME, "reload NAME" ),
		//new Sign().attribute( NAME, "save"    ).content( new Tag( "save", "", "" ) ),
		//new Sign().attribute( NAME, "saveas $NAME" ).content( new Tag("saveas ", "NAME", ".")),
		new Sign().content( new Tag(    "enable undo", "" )).attribute( NAME, "undo enable"  ),
		new Sign().content( new Tag(   "disable undo", "" )).attribute( NAME, "undo disable" ),
		new Sign().content( new Tag(           "undo", "" )).attribute( NAME, "undo"         ),
		new Sign().content( new Tag(      "say again", "" )).attribute( NAME, "repeat"       ),
		new Sign().content( new Tag(     "debug ", "x" )).attribute( NAME, "debug X"      ),
		new Sign().content( new Tag(     "spell ", "x" )).attribute( NAME, "spell X"      ),
		/* 
		 * it is possible to arrive at the following construct:   think="reply 'I know'"
		 * e.g. "if X, Y", if the instance is "if already exists, reply 'I know'"
		 * here reply is thought. Should be rewritten:
		 * representamen: "if X, reply Y", then Y is just the quoted string.
		 * However, the following should deal with this situation.
		 */
		new Sign().content( new Tag( REPLY+" ", "x" ).attribute( Tag.quoted, Tag.quoted ))
				.attribute( REPLY, "X" ),
		/* 
		 * REDO: undo and do again, or disambiguate
		 */
		new Sign().content( new Tag( "No ", "x" ).attribute( "phrase", "phrase" ))
					.attribute( NAME, "undo" )
					.attribute( "elseReply", "undo is not available" )
					/* On thinking the below, if X is the same as what was said before,
					 * need to search for the appropriate sign from where we left off
					 * Dealing with ambiguity: "X", "No, /X/"
					 */
					.attribute(  NAME,  "disamb X" ) // this will set up how the inner thought, below, works
					.attribute( "think",  "X"    )
	 };
	
	// are we taking the hit of creating / deleting overlays
	//private boolean undoEnabled = false;
	//private boolean undoIsEnabled() { return undoEnabled; }
	//public  Engine undoEnabledIs( boolean enabled ) { undoEnabled = enabled; return this; }
	

	
	public Engine( String name, String value ) { super( name, value ); }
	
	// this supports the command="" attribute loaded in the creation of command data structure
	// needs "command //delete "...". -- to remove a tag, to support '"X" is meaningless.'
	private Reply unknownCommand( Reply r, Strings cmd ) {
		audit.ERROR( "Unknown command "+ cmd.toString( Strings.CSV ));
		return r.format( Reply.dnu() );
	}
	
	// determines the behaviour of the app over prompting for help...
	static private boolean helpRun = false;
	static public  void    helpRun( boolean run ) { helpRun = run; }
	static public  boolean helpRun() { return helpRun; }
	// record whether the user has figured it out...
	// run in conjunction with the main intepreter and the app...
	static private boolean spoken = false;
	static public  void    spoken( boolean run ) { spoken = run; }
	static public  boolean spoken() { return spoken; }
	//
	//
	// redo ----------------------------------------------------
	private static boolean disambFound = false;
	public static void    disambFound( boolean b ) {
		audit.debug( (b ? "":"RE") + "SETTING DisambFound" );
		disambFound = b;
	}
	public static boolean disambFound() { return disambFound; }
	
	/* disambFound() called from Engine
	 * redoOff() called from Enguage
	 */
	static private void disambOn( Strings cmd ) {
		//simply turn disambiguation on if this thought is same as last...
		audit.debug( "Engine:disambFound():REDOING:"+(disambFound()?"ON":"OFF")+":"+ Enguage.lastInput() +" =? "+ cmd +")" );
		if (	(/*!redoIsOn() &&*/ Enguage.lastInput()               .equals( cmd  )) //    X == (redo) X     -- case 1
		    ||	(/* redoIsOn() &&*/	Enguage.lastInput().copyAfter( 0 ).equals( cmd  )  // no X == (redo) X...  -- case 2
		    				     &&	Enguage.lastInput().get(    0    ).equals( "no" )  // ..&& last[ 0 ] = "no"
		)	)	{
			if (Enguage.signs.lastFoundAt() != -1) { // just in case!
				Enguage.signs.ignore( Enguage.signs.lastFoundAt() );
				audit.debug("Engine:disambOn():REDOING: Signs to avoid now: "+ Enguage.signs.ignore().toString() );
				disambFound( true );
	}	}	}
	//static private boolean subsequent = false;
	/* now, we have disamb found (ignore list has increased) so we are still adjusting the list
	 * AND the list itself! This is called at the end of an utterance
	 */
	static public void disambOff() {
		//audit.traceIn( "disambOff", "avoids="+ Enguage.e.signs.ignore().toString());
		if (Engine.disambFound()) { //still adjusting the list!
			//audit.debug( "Engine:disambOff():COOKED!" );
			Engine.disambFound( false );
			Enguage.signs.reorder();
		} else {
			//audit.debug( "Engine:disambOff():RAW, forget ignores: "+ Enguage.e.signs.ignore().toString());
			Enguage.signs.ignoreNone();
		}
		//audit.traceOut();
	}
	// redo ----------------------------------------------------
	//
	//

	public Reply mediate( Reply r ) {
		r.answer( Reply.yes()); // just to stop debug output look worrying
		
		Strings cmds =
				Reply.context().deref(
					Variable.deref(
						new Strings( value )
					)
				);
		cmds = cmds.normalise();
		String cmd = cmds.get( 0 );

/*		audit.debug( "in Engine.mediate, ctx="+ Reply.context().toString());
		audit.debug( "in Engine.mediate, val=[ "+ value +" ]");
		audit.debug( "in Engine.mediate, cmd=[ "+ Strings.toString( cmd, Strings.CSV ) +" ]");
		audit.debug( "in Engine.mediate, NAME='"+ NAME +"', value='"+ value +"'");
// */
		if ( cmd.equals( "undo" )) {
			r.format( "ok" );
			if (cmds.size() == 2 && cmds.get( 1 ).equals( "enable" )) 
				Enguage.undoEnabledIs( true );
			else if (cmds.size() == 2 && cmds.get( 1 ).equals( "disable" )) 
				Enguage.undoEnabledIs( false );
			else if (cmds.size() == 1 && Enguage.undoIsEnabled()) {
				if (Enguage.o.count() < 2) { // if there isn't an overlay to be removed
					audit.debug( "overlay count( "+ Enguage.o.count() +" ) < 2" );
					r.answer( Reply.no() );
				} else
					Enguage.o.reStartTxn();
			} else if (!Enguage.undoIsEnabled())
				r.format( Reply.dnu() );
			else
				r = unknownCommand( r, cmds );
			
		} else if (cmd.equals( "disamb" )) {
			disambOn( cmds.copyAfter( 0 ));

		} else if (cmd.equals( "load" )) {
			Strings files = cmds.copyAfter( 0 );
			audit.debug( "loading "+ files.toString( Strings.CSV ));
			for(int i=0; i<files.size(); i++)
				Repertoire.load( files.get( i ));
			 
		} else if (cmd.equals( "unload" )) {
			Strings files = cmds.copyAfter( 0 );
			for(int i=0; i<files.size(); i++)
				Repertoire.unload( files.get( i ));

		} else if (cmd.equals( "reload" )) {
			Strings files = cmds.copyAfter( 0 );
			for(int i=0; i<files.size(); i++) Repertoire.unload( files.get( i ));
			for(int i=0; i<files.size(); i++) Repertoire.load( files.get( i ));
/*
		} else if (e.get( 0 ).equals( "save" ) || e.get( 0 ).equalsIgnoreCase( "saveAs" )) {
			if (e.get( 0 ).equalsIgnoreCase( "saveAs" ) && ( e.size() != 2))
				System.err.println( e.get( 0 ) +": NAME required." );
			else {
				if (e.get( 0 ).equalsIgnoreCase( "saveAs" )) {
					//(re)NAME concept
					System.out.println( "renaming concept" );
				}
				//save concept
				System.out.println( "Saving concept" );
			}
*/
		} else if (cmd.equals( "spell" )) {
			r.format( Reply.spell( cmds.get( 1 ), true ));
			
		} else if (cmd.equals( "debug" )) {
		//	; //setenv( "//DEBUG", !e.get( 1 ) || e.get( 1 ).equals( "on") ? "Enguage.c:Pattern.c":"", 1 );
			if (cmds.size() <= 1) ; // do nothing -- prob won't get here 'cos it won't have matched.
			else if (cmds.get( 1 ).equals( "on")) {
				Audit.turnOn();
				r.format( "ok" );
			} else if (cmds.get( 1 ).equals( "off")) {
				Audit.turnOff();			
				r.format( "ok" );
			} else if (cmds.get( 1 ).equals( "show" )) {
				//Enguage.e.autop.show();
				Enguage.signs.show();
				//Enguage.e.engin.show();
				r.format( "ok" );
			} else
				r = unknownCommand( r, cmds );

		} else if ( value.equals( "repeat" )) {
			if (Enguage.lastOutput() == null) {
				audit.audit("Engine:repeating dnu");
				r.format( Reply.dnu());
			} else {
				audit.audit("Engine:repeating: "+ Enguage.lastOutput());
				r.repeated( true );
				r.format( Reply.repeatFormat());
				r.answer( Enguage.lastOutput());
			}
			
		} else if (   cmd.equals( "help"    )) {
			helpRun( true );
			r.format( Enguage.engin.helpToString( NAME ));

		} else if (cmd.equals( "hello"   ) ||
				   cmd.equals( "welcome" )    ) {
				helpRun( true );
				if (cmd.equals(  "hello"  )) r.say( new Strings( "hello" ));
				if (cmd.equals( "welcome" )) r.say( new Strings( "welcome" ));
				r.format( Repertoire.help());

		} else if ( cmd.equals( "list" )) {
			//Strings reps = Enguage.e.signs.toIdList();
			r.format( "loaded repertoires include "+ Repertoire.names().toString( Reply.andListFormat() ));
			
		} else if ( cmd.equals( "describe" ) && cmds.size() >= 2) {
			String name = cmds.copyAfter( 0 ).toString( Strings.CONCAT );
			r.format( Enguage.signs.helpToString( name ));
			
		} else if ( cmd.equals( "repertoire" )) {
			r.format( Enguage.signs.helpToString());
			
		} else
			r = unknownCommand( r, cmds );
		return r;
	}
	public static void main( String args[]) {
		Audit.turnOn(); //main()
		// NB. This test program needs more work.
		//Enguage eng = new Enguage( null );
		Reply r = new Reply();
		Repertoire.load( "iNeed" );
		Engine e = new Engine( "engine", "list" );
		e.mediate( r );
}	}