package com.yagadi.enguage.concept;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Autopoiesis extends Intention {
	private static Audit   audit = new Audit( "Autopoiesis" );
	private static boolean debug = Enguage.startupDebugging;

	public static final String NAME     = "autopoiesis";
	
	public static final String NEW     = "add";
	public static final String APPEND  = "app";
	public static final String PREPEND = "prep";
	
	public static final Sign[] autopoiesis = {
		// PATTERN PRE-CHECK cases (4)
		// 1: A implies B.
		new Sign().attribute( PREPEND, "think A B")
//			.attribute("id", "1" )
			.content( new Tag( "", "a"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( " implies ", "b", "." ).attribute( Tag.quoted, Tag.quoted )),
		// 2: A implies B, if not, say C.
		new Sign().attribute( PREPEND, "elseReply A C").attribute( PREPEND, "think A B")
//			.attribute("id", "2" )
			.content( new Tag(          "", "a" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( " implies ", "b" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( " ; if not, reply ", "c", "." ).attribute( Tag.quoted, Tag.quoted )),
			
		// PATTERN CREATION cases (7).
		// a1: On X, think Y.
		new Sign().attribute( NEW, "think X Y")
//			.attribute("id", "a1" )
			.content( new Tag(     "On ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", ", "y", "" ).attribute( Tag.phrase, Tag.phrase )),
			
		// a2: On X, reply Y.
		new Sign().attribute( NEW, "reply X Y")
//			.attribute("id", "a2" )
			.content( new Tag(     "On ", "x" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", reply ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
			
		// a3: On X, perform Y.
		new Sign().attribute( NEW, "perform X Y") // <<<< trying this
//			.attribute("id", "a3" )
			.content( new Tag(       "On ", "x" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", perform ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
			
		// b1: Then on X think Y.
		new Sign().attribute( APPEND, "think X Y")
//			.attribute("id", "b1" )
			.content( new Tag( "Then on ", "x" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", ", "y", "" ).attribute( Tag.phrase, Tag.phrase )),
			
		// b2: Then on X reply Y.
		new Sign().attribute( APPEND, "reply X Y")
//			.attribute("id", "b2" )
			.content( new Tag( "Then  on ", "x" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", reply   ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
			
		// b3: Then on X perform Y.
		new Sign().attribute( APPEND, "perform X Y")
//			.attribute("id", "b3" )
			.content( new Tag( "Then  on ", "x" ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", perform ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
		
		// At some point this could be improved to say "On X, perform Y; if not, reply Z." -- ??? think!
		// !b1: Else on X think Y.
		new Sign().attribute( APPEND, "elseThink X Y")
//			.attribute("id", "!b1" )
			.content( new Tag( "Then on ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", if not, ", "y", "" ).attribute( Tag.phrase, Tag.phrase )),
			
		// !b2: Else on X reply Y.
		new Sign().attribute( APPEND, "elseReply X Y")
//			.attribute("id", "!b2" )
			.content( new Tag(  "Then on ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", if not, reply   ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
			
		// !b3: Else on X perform Y.
		new Sign().attribute( APPEND, "elsePerform X Y" )
//			.attribute("id", "!b3" )
			.content( new Tag( "Then  on ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ", if not, perform ", "y", "" ).attribute( Tag.quoted, Tag.quoted )),
			
		// c1: Finally on X perform Y. -- dont need think or reply?
		new Sign().attribute( APPEND, "finally X Y")
//			.attribute("id", "c1" )
			.content( new Tag( " Finally on ", "x"      ).attribute( Tag.quoted, Tag.quoted ))
			.content( new Tag( ",   perform ", "y", "" ).attribute( Tag.quoted, Tag.quoted ))
	};
	/*
	 * For autobuild, need to add above
	 * 	On "X", if [so|not] [think|do|say] "Y" <<< matches On "X", think "Y". ???
	 */
		
	// --- Autopoeisis: constructing new concept tags
	private static String newCharsSingleUpperChar( String str ) { // "if X does $USER Y" -> "if $x does martin $y"
		String cmdChs = "";
		// correct here? ...Chars? -> [.."$", "USER",..] ...NonWS?
		Strings words = new Strings( str );
		for (int i = 0; i<words.size(); ++i ) {
			if (i>0 && !words.get( i-1 ).equals("$")) cmdChs += " ";
			cmdChs += words.get( i );
		}
		return cmdChs;
	}
	
	public Autopoiesis( String name, String value ) { super( name, value ); }	
	
	static private Sign s = null;
	
	// this supports the meta="" autopoeisis rule
	// TODO: needs "concepts //delete "...". -- to remove a tag, to support '"X" is meaningless.'
	// Supports:
	// add "pattern" "action"
	// app "pattern" "action"
	// prep "pattern" "action"
	// TODO: auto "pattern" "action" where auto adds on new pattern and appends thereafter
	public Reply mediate( Reply r ) {
		//Audit.turnOn();
		if (debug) audit.traceIn( "mediate", "NAME="+ NAME +", value="+ value +", "+ Reply.context().toString());

		// NAME="add", VALUE='"help" "add item to list"'
		Strings sa = Reply.context().deref(
			Variable.deref(
				new Strings( value )
		)	);
		
		// sa.get( 0 )='reply', sa.get( 1 )='"help"', sa.get( 2 )='"aural help is not yet implemented"'
		
		if (3 != sa.size())
			audit.ERROR( name +": wrong number ("+ sa.size() +") of params ["+ sa.toString( Strings.CSV ) +"]");
		else {
			String val = newCharsSingleUpperChar( Strings.trim( sa.get( 2 ), '"' ));
			if (name.equals( "app" ) || name.equals( "prep" )) {
				if (null == s)
					// this should return DNU...
					audit.ERROR( "adding to non existent concept: ["+ sa.toString( Strings.CSV )+"]");
				else {
					if (name.equals( "app" )) {
						if (debug) audit.debug( "Appending  to EXISTING rule: ["+ sa.toString( Strings.CSV )+"]");
						s.append(  sa.get( 0 ), val );
					} else {
						if (debug) audit.debug( "Prepending to EXISTING rule: ["+ sa.toString( Strings.CSV )+"]");
						s.remove( 0 );                          // remove id=""
						s.prepend( sa.get( 0 ), val );              // prepend NAME="val"
						s.prepend( "id", Repertoire.nowLoading()); // re-prepend id=""
				}	}
			} else if (name.equals( "add" )) { // autopoeisis?
				/* TODO: need to differentiate between
				 * "X is X" and "X is Y" -- same shape, different usage.
				 * At least need to avoid this (spot when "X is X" happens)
				 */
				if ( sa.get( 1 ).equals( "help" )) {
					if (debug) audit.debug( "Adding HELP: ["+ sa.toString( Strings.CSV )+"]");
					s.attribute( "help", val ); // add: help="text" to cached sign
				} else { // create a new cached sign
					if (debug) audit.debug( "Adding NEW rule: ["+ sa.toString( Strings.CSV )+"]");
					s = new Sign()
							.content( new Tags( Strings.trim( sa.get( 1 ), '"' )) )
							.attribute( "id", Repertoire.nowLoading() )
							.attribute( sa.get( 0 ), val );
					// then add it into the list of signs
					Enguage.signs.insert( s );
		}	//} else if (name.equals( "autobuild" )) {
		}	}

		r.answer( Reply.yes());

		if (debug) audit.traceOut( "result="+ r.toString() );
		//Audit.turnOff();
		return r;
}	}
