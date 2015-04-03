package com.yagadi.enguage.expression;

import java.util.ArrayList;
import java.util.ListIterator;

import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Colloquial {
	static private Audit audit = new Audit( "Colloquial" );
	
	private ArrayList<Strings> froms;
	private ArrayList<Strings> tos;

	public String add( String fs, String ts ) {
		add( new Strings( fs ), new Strings( ts ));
		return "ok";
	}
	private void add( Strings fs, Strings ts ) {
		//audit.audit( "Colloqua: adding "+ fs.toString() +", "+ ts.toString());
		froms.add( Variable.deref( fs ));
		  tos.add( Variable.deref( ts ));
	}
	public Colloquial() {
		froms = new ArrayList<Strings>();
		tos = new ArrayList<Strings>();
	}
	// ---
	// this doesn't spot overlapping colloquia...
	// wandered lonely --> moved aimlessly
	// aimlessly as a cloud --> duff analogy
	// lonely as a cloud --> very lonely
	// I wandered lonely as a cloud --> I moved duff analogy (not as required: I moved aimlessly very lonely.)
	public Strings externalise( Strings a ) {
		for (int i=0; i<froms.size() && i<tos.size(); ++i)
			a.replace( froms.get( i ), tos.get( i )); // "I have" to "I've"
		return a;
	}
	public Strings internalise( Strings a ) {
		for (int i=0; i<froms.size() && i<tos.size(); ++i)	
			a.replace( tos.get( i ), froms.get( i ));
		return a;
	}
	public String toString() {
		String str = "";
		for (int i=0; i<froms.size(); i++)
			str += ("=>"+ froms.get( i ).toString() +" <=> "+ tos.get( i ).toString() +"\n");
		return str;
	}
	
	static private Colloquial user = new Colloquial();
	static public  Colloquial user() { return user; }
	static public  void       user( Colloquial c ) {user = c;}

	static private Colloquial host = new Colloquial();
	static public  Colloquial host() {return host;}
	//static public  void      host( Colloquial c ) {host = c;}
	
	static private Colloquial symmetric = new Colloquial();
	static public  Colloquial symmetric() {return symmetric;}
	//static public  void      symmetric( Colloquial c ) {symmetric = c;}
	
	static public String applyOutgoingColloquia( String s ) {
		return symmetric().externalise(             // 3. [ "You", "do", "not", "know" ] -> [ "You", "don't", "know" ]
				    host().externalise(             // 2. [ "_user", "does", "not", "know" ] -> [ "You", "do", "not", "know" ]
						new Strings( s ) ))         // 1. "_user does not know" -> [ "_user", "does", "not", "know" ]
						.toString( Strings.SPACED );// 4. [ "I", "don't", "know" ] => "I don't know"
	}
	static public Strings applyOutgoingColloquia( Strings list ) {
		ListIterator<String> li = list.listIterator();
		while (li.hasNext())
			li.set( applyOutgoingColloquia( li.next()) );
		return list;
	}
	
	static public String interpret( Strings a ) {
		if (null == a) return Shell.FAIL;
		//audit.traceIn( "interpret", a.toString( Strings.CSV ));
		if (a.get( 0 ).equals("add") && a.size() >= 3) {
			Strings first = new Strings(),
					second = new Strings();
			if (a.size() == 3) {
				first = new Strings( Strings.trim( a.get( 1 ), '"' ));
				second= new Strings( Strings.trim( a.get( 2 ), '"' ));
			} else {
				boolean doingFirst = true;
				for (int i=1; i<a.size(); i++) {
					String item = a.get( i );
					if ( item.equals( "=" ))
						doingFirst = false;
					else if (doingFirst)
						first.add( item );
					else
						second.add( item );
			}	}
			//if (Audit.isOn()) audit.audit("COLLOQUIALS: adding: "+ first +" = "+ second );
			symmetric().add( first, second );
		} else
			audit.ERROR( "colloquialAdd(): unknown command: "+ a.toString( Strings.CSV ));
		//return audit.traceOut( Shell.SUCCESS );
		return Shell.SUCCESS;
	}
	public static void main( String args[] ) {
		Audit.turnOn();
		Strings a = new Strings( "This is a failure" );
		Colloquial c = new Colloquial();
		c.add( "This", "Hello" );
		c.add( "is a failure", "world" );
		a = c.externalise( a );
		audit.audit( a.toString( Strings.SPACED ));
		a = c.internalise( a );
		audit.audit( a.toString( Strings.SPACED ));
}	}
