package com.yagadi.enguage.concept;

import java.util.ArrayList;

import com.yagadi.enguage.expression.*;
import com.yagadi.enguage.util.*;

public class Sign extends Tag {
	static Audit audit = new Audit("Sign");

	// methods need to return correct class of this
	public Sign attribute( String name, String value ) { attrs.add( new Attribute( name, value )); return this; }
	public Sign content( Tag t ) {
		audit.traceIn( "content", "NAME='"+ t.name() +"', id='"+ t.attribute("id") +"'" );
		content.append( t );
		audit.traceOut();
		return this;
	}
	@Override
	public Sign content( Tags ta ) { content = ta; return this; }
	public Sign() {
		super();
		name( "pattern" );
	}
	public Sign( String a, String b, String c ) {
		super( a, b, c );
		name( "pattern" );
	}
	public Reply interpret() {
		//audit.traceIn( "interpret", null );
		ArrayList<Attribute> a = attributes();
		int i = -1;
		Reply r = new Reply();
		while (!r.isDone() && a.size() > ++i) {
			String name  = a.get( i ).name(),
			       value = a.get( i ).value();
			r = name.equals( Engine.NAME ) ?
					new Engine(      name, value ).mediate( r )
				: name.equals( Autopoiesis.APPEND )  ||
				  name.equals( Autopoiesis.PREPEND ) ||
				  name.equals( Autopoiesis.NEW ) ?
					new Autopoiesis( name, value ).mediate( r )
				: // finally, perform, think, say...
					new Intention(   name, value ).mediate( r );
		}
		//audit.traceOut( r.toString() );
		return r;
	}
	private final static String indent = "    ";
	public String toString() {
		return prefix + (name.equals( "" ) ? "" :
			(indent +"<"+ name +" "+ attrs.toString( "\n      " )+ // attributes has no preceding space
			(null == content() ? "/>" : ( ">\n"+ indent + indent + content().toString() + "</"+ name +">" ))))
			+ postfix + "\n";
	}
	/*public static void main( String argv[]) {
		Sign p = new Sign();
		p.attribute("reply", "hello world");
		p.content( new Tag().prefix( "hello" ));
		Reply r = new Reply();
		Intention intent = new Intention( "say", "hello world" );
		r = intent.mediate( r );
}*/	}
