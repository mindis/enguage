package com.yagadi.enguage.concept;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.*;
import com.yagadi.enguage.util.*;

public class Autopoiesis extends Attribute {
	static private Audit audit = new Audit( "Autopoiesis" );

	public static final String NEW     = "add";
	public static final String APPEND  = "app";
	public static final String PREPEND = "prep";
	
	public static final Sign[] autopoiesis = {
		// PATTERN PRE-CHECK cases (4)
		// 1: A implies B.
		new Sign().attribute( PREPEND, "think $a $b")
//			.attribute("id", "1" )
			.content( new Tag( "", "a"      ).attribute( "quoted", "quoted" ))
			.content( new Tag( " implies ", "b", "." ).attribute( "quoted", "quoted" )),
		// 2: A implies B, if not, say C.
		new Sign().attribute( PREPEND, "elseReply $a $c").attribute( PREPEND, "think $a $b")
//			.attribute("id", "2" )
			.content( new Tag(          "", "a" ).attribute( "quoted", "quoted" ))
			.content( new Tag( " implies ", "b" ).attribute( "quoted", "quoted" ))
			.content( new Tag( " ; if not, reply ", "c", "." ).attribute( "quoted", "quoted" )),
			
			
		// PATTERN CREATION cases (7).
		// a1: On X, think Y.
		new Sign().attribute( NEW, "think $X $Y")
//			.attribute("id", "a1" )
			.content( new Tag(     "On ", "X"      ).attribute( "quoted", "quoted" ))
			.content( new Tag( ", think ", "Y", "." ).attribute( "quoted", "quoted" )),
			
		// a2: On X, reply Y.
		new Sign().attribute( NEW, "reply $X $Y")
//			.attribute("id", "a2" )
			.content( new Tag(     "On ", "X" ).attribute( "quoted", "quoted" ))
			.content( new Tag( ", reply ", "Y", "." ).attribute( "quoted", "quoted" )),
			
		// a3: On X, perform Y.
		new Sign().attribute( NEW, "perform $X $Y")
//			.attribute("id", "a3" )
			.content( new Tag(       "On ", "X" ).attribute( "quoted", "quoted" ))
			.content( new Tag( ", perform ", "Y", "." ).attribute( "quoted", "quoted" )),
			
		// b1: Then on X think Y.
		new Sign().attribute( APPEND, "think $X $Y")
//			.attribute("id", "b1" )
			.content( new Tag( "Then on ", "X" ).attribute( "quoted", "quoted" ))
			.content( new Tag( ", think  ", "Y", "." ).attribute( "quoted", "quoted" )),
			
		// b2: Then on X reply Y.
		new Sign().attribute( APPEND, "reply $X $Y")
//			.attribute("id", "b2" )
			.content( new Tag( "Then  on ", "X" ).attribute( "quoted", "quoted" ))
			.content( new Tag( ", reply   ", "Y", "." ).attribute( "quoted", "quoted" )),
			
		// b3: Then on X perform Y.
		new Sign().attribute( APPEND, "perform $X $Y")
//			.attribute("id", "b3" )
			.content( new Tag( "Then  on ", "X" ).attribute( "quoted", "quoted" ))
			.content( new Tag( ", perform ", "Y", "." ).attribute( "quoted", "quoted" )),
		
		// At some point this could be improved to say "On X, perform Y; if not, reply Z." -- ??? think!
		// !b1: Else on X think Y.
		new Sign().attribute( APPEND, "elseThink $X $Y")
//			.attribute("id", "!b1" )
			.content( new Tag( "Then on ", "X"      ).attribute( "quoted", "quoted" ))
			.content( new Tag( ", if not, think  ", "Y", "." ).attribute( "quoted", "quoted" )),
			
		// !b2: Else on X reply Y.
		new Sign().attribute( APPEND, "elseReply $X $Y")
//			.attribute("id", "!b2" )
			.content( new Tag(  "Then on ", "X"      ).attribute( "quoted", "quoted" ))
			.content( new Tag( ", if not, reply   ", "Y", "." ).attribute( "quoted", "quoted" )),
			
		// !b3: Else on X perform Y.
		new Sign().attribute( APPEND, "elsePerform $X $Y")
//			.attribute("id", "!b3" )
			.content( new Tag( "Then  on ", "X"      ).attribute( "quoted", "quoted" ))
			.content( new Tag( ", if not, perform ", "Y", "." ).attribute( "quoted", "quoted" )),
			
		// c1: Finally on X perform Y. -- dont need think or reply?
		new Sign().attribute( APPEND, "finally $X $Y")
//			.attribute("id", "c1" )
			.content( new Tag( " Finally on ", "X"      ).attribute( "quoted", "quoted" ))
			.content( new Tag( ",   perform ", "Y", "." ).attribute( "quoted", "quoted" ))
	};
		
	// --- Autopoeisis: constructing new concept tags
	private static String newCharsSingleUpperChar( String str ) { // "if X does $USER Y" -> "if $x does martin $y"
		String cmdChs = "";
		String[] words = Strings.fromString( str ); // correct? ...Chars? -> [.."$", "USER",..] ...NonWS?
		words = Strings.replace( words, Strings.dotDotDot, Strings.ellipsis );
		for (int i = 0; i<words.length; ++i ) {
			if (i>0 && !words[ i-1 ].equals("$")) cmdChs += " ";
			cmdChs += words[ i ];
		}
		return cmdChs;
	}
	
	public Autopoiesis( String name, String value ) { super( name, value ); }	
	
	static private Sign s = null;
	// this supports the meta="" autopoeisis rule
	// needs "concepts //delete "...". -- to remove a tag, to support '"X" is meaningless.'
	public Reply mediate( Reply r ) {
		if (Audit.isOn()) audit.traceIn( "autoInterp", "NAME="+ name +", value="+ value +", "+ Reply.context().toString());

		// NAME="add", VALUE="help "add item to list""
		String[] sa = Strings.fromString( value ); // stripped from getCommand
		sa = Reply.context().getCommand( sa );
		sa = Reply.context().deref( sa ); // stripped from getCommand
		
		// sa[ 0 ]='reply', sa[ 1 ]='"help"', sa[ 2 ]='"aural help is not yet implemented"'
		
		if (3 != sa.length)
			audit.ERROR( name +": wrong number ("+ sa.length +") of params ["+ Strings.toString( sa, Strings.CSV ) +"]");
		else {
			String val = newCharsSingleUpperChar( Strings.trim( sa[ 2 ], '"' ));
			if (name.equals( "app" ) || name.equals( "prep" )) {
				if (null == s)
					// this should return DNU...
					audit.ERROR( "adding to non existent concept: ["+ Strings.toString( sa, Strings.CSV )+"]");
				else {
					if (name.equals( "app" )) {
						if (Audit.isOn()) audit.debug( "Appending  to EXISTING rule: ["+ Strings.toString( sa, Strings.CSV )+"]");
						s.append(  sa[ 0 ], val );
					} else {
						if (Audit.isOn()) audit.debug( "Prepending to EXISTING rule: ["+ Strings.toString( sa, Strings.CSV )+"]");
						s.remove( 0 );                          // remove id=""
						s.prepend( sa[ 0 ], val );              // prepend name="val"
						s.prepend( "id", Enguage.nowLoading()); // re-prepend id=""
				}	}
			} else if (name.equals( "add" )) { // autopoeisis?
				/* TODO: need to differentiate between
				 * "X is X." and "X is Y." -- same shape, different usage.
				 * At least need to avoid this (spot when "X is X" happens)
				 */
				if ( sa[ 1 ].equals( "\"help\"" )) {
					if (Audit.isOn()) audit.debug( "Adding HELP: ["+ Strings.toString( sa, Strings.CSV )+"]");
					s.attribute( "help", val ); // add: help="text" to cached sign
				} else { // create a new cached sign
					if (Audit.isOn()) audit.debug( "Adding NEW rule: ["+ Strings.toString( sa, Strings.CSV )+"]");
					s = new Sign()
							.content( new Tags( Strings.trim( sa[ 1 ], '"' )) )
							.attribute( "id", Enguage.nowLoading() )
							.attribute( sa[ 0 ], val );
					// then add it into the list of signs
					Enguage.interpreter.signs.insert( s );
		}	}	}

		r.answer( Reply.yes());

		if (Audit.isOn()) audit.traceOut( "result="+ r.toString() );
		return r;
}	}
