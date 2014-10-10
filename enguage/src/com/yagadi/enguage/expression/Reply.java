package com.yagadi.enguage.expression;

import java.util.ArrayList;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.sofa.List;
import com.yagadi.enguage.util.*;

public class Reply { // a reply is basically a formatted answer
	
	static private Audit audit = new Audit( "Reply" );
	
	static public final int   NO = 0; // FALSE -- -ve
	static public final int  YES = 1; // TRUE  -- +ve
	static public final int  DNU = 2; // DO NOT UNDERSTAND
	static public final int   NK = 3; // NOT KNOWN -- init
	static public final int   IK = 4; // I know, silly!
	static public final int  CHS = 5; // use stored expression
	
	static private Colloquials host = new Colloquials();
	static public  Colloquials host() {return host;}
	static public  void host(Colloquials c) {host = c;}
	
	static private Colloquials both = new Colloquials();
	static public  Colloquials both() {return both;}
	static public  void both( Colloquials c ) {both = c;}
	
	static public String[] applyOutgoingColloquials( String[] list) {
		for (int i=0; i<list.length; i++) {
			String[] sublist = Strings.fromString( list[ i ]);
			sublist = Reply.both().apply( Reply.host().apply( sublist ));
			list[ i ] = Strings.toString( sublist, Strings.SPACED );
		}
		return list;
	}
	
	static private boolean verbatim;
	static public  boolean isVerbatim() { return verbatim; }
	static public  void    verbatimIs( boolean val ) { verbatim = val; }
	
	// these need to be in a Map structure...?
	static private String dnu = "DNU";
	static public  void   dnu( String s ) { dnu = s; }
	static public  String dnu(){ return dnu;}

	static private String dnk = "DNK";
	static public  void   dnk( String s ) { dnk = s; }
	static public  String dnk() { return dnk;}

	static private String ik = "IK";
	static public  void   ik( String s ) { ik = s;}
	static public  String ik() { return ik;}

	static private String no = "NO";
	static public  void   no(  String s ) { no = s; }
	static public  String no() { return no; }
	
	static private String yes = "YES";
	static public  void   yes( String s ) { yes = s; }
	static public  String yes() { return yes; }

	static private String repeatFormat = "I said, ... .";
	static public  void   repeatFormat( String s ) { repeatFormat = s; }
	static public  String repeatFormat() { return repeatFormat; }

	static private String[] conjunctions = { ",", "and" };
	static public  void   conjunctions( String[] sa ) { conjunctions = sa; }
	static public  String[] conjunctions() { return conjunctions; }

	static private String[] referencers = { "the" };
	static public  void   referencers( String[] sa ) { referencers = sa; }
	static public  String[] referencers() { return referencers; }

	static private String listSep = "/";
	static public  void   listSep( String s ) { listSep = s; }
	static public  String listSep() { return listSep; }

	static private String[] andListFormat = { ", ", ", and " };
	static public  void     andListFormat( String s ) { andListFormat = Strings.fromString( s, listSep()); }
	static public  String[] andListFormat() { return andListFormat; }

	static private String[] orListFormat = { ", ", ", or " };
	static public  void     orListFormat( String s ) { orListFormat = Strings.fromString( s, listSep()); }
	static public  String[] orListFormat() { return orListFormat; }

	static private ArrayList<Attributes> contexts = new ArrayList<Attributes>();
	static public void  pushContext( Attributes ctx ){ contexts.add( 0, ctx ); }
	static public void  popContext() { contexts.remove( 0 );}
	static public Attributes context() {
		if (contexts.size() == 0) pushContext( new Attributes() );
		return contexts.get( 0 );
	}

	private Answer  answer = new Answer();
	public  String  answer() { return answer.toString(); }
	public  Reply   answer( String ans ) {
		answer.value( ans == null ? "" : ans );
		// type is dependent on answer
		type = calculateType();
		return this;
	}
	public  boolean forward() { return answer.forward(); }

	private String[] format = new String[ 0 ]; // format is empty!
	public  Reply    format( String s ) { format( Strings.fromString( s )); return this; }
	public  Reply    format( String[] sa ) {
		format = sa;
		type = calculateType(); // type is dependent on format -- should it be???
		return this;
	}
	public  String[] format() { return format; }

	private boolean repeated = false;
	public  void    repeated( boolean s ) { repeated = s; }
	public  boolean repeated() { return repeated; }

	private boolean done = false;
	public  void    doneIs( boolean b ) { done = b; }
	public  boolean isDone() { return done; }

	private int type = DNU;
	private int calculateType() { // TODO: set type once on setting answer/format
		if (answer().equals( "" ) && format.length == 0) {
			return DNU;
		} else if (answer().equals( no ) && Strings.equals( format, Strings.fromString(  ik ))) {
			return CHS;
		} else if (answer().equals( "" ) && Strings.contain( format, "..." )) {
			return NK;
		} else if (answer().equals( "" ) && !Strings.contain( format, "..." )) {
			     if (Strings.equals( format, Strings.fromString( yes ))) return YES;
			else if (Strings.equals( format, Strings.fromString(  no ))) return NO;
			else if (Strings.equals( format, Strings.fromString(  ik ))) return IK;
			else if (Strings.equals( format, Strings.fromString( dnk ))) return NK;
			else if (Strings.equals( format, Strings.fromString( dnu ))) return DNU;
			else return CHS;
		} else {
			     if (answer().equalsIgnoreCase( yes )) return YES;
			else if (answer().equalsIgnoreCase(  no )) return NO;
			else if (answer().equalsIgnoreCase(  ik )) return IK;
			else if (answer().equalsIgnoreCase( dnk )) return NK;
			else if (answer().equalsIgnoreCase( dnu )) return DNU;
			else return CHS;
	}	}
	public int      getType() { return type; }
	public boolean positive() {return YES == type || CHS == type; } // != !negative() !!!!!
	public boolean negative() {return  NO == type ||  NK == type/*|| DNU == type()*/;} // != !positive() !!!!!

	// ---
	public String asString() {
		String format = Strings.toString( format(), Strings.SPACED );
		if (format.equals( "I do not understand" )) format = "-";
		return format +"/"+ answer() +"/"+ type;
	}

	public String toString() {
//		audit.traceIn( "toString", null );
		verbatimIs( false );
		// TODO: if format is blank but answer is not empty then use reply!!!
		String[] utterance = format();
		if (0 == utterance.length)
			utterance = Strings.fromString( answer.value().equals("") ? dnu() : answer.value());
		
		// ... then post-process:
		// if not terminated, add first terminator -- see Tag.c::newTagsFromDescription()
		if (utterance.length > 0 && !Language.isTerminator( utterance[ utterance.length -1]) &&
			!((utterance.length > 1) && Language.isTerminator( utterance[ utterance.length -2]) && Language.isQuote( utterance[ utterance.length -1])))
			utterance = Strings.append( utterance, Language.terminators()[ 0 ]);

		// ...deref any context...
		utterance = context().deref( utterance );
		// English-dependent processing...
		utterance = Language.indefiniteArticleVowelSwap(
						Language.sentenceCapitalisation( utterance ));
		
		// ...finally, if required put in answer (verbatim!)
		if (utterance == null || utterance.length == 0)
			if ( answer().equals( "" ))
				utterance = Strings.fromString( dnk());
			else
				utterance = Strings.fromString( answer.toString()); // use the raw answer???
		else if (Strings.contain( utterance, Strings.ELLIPSIS ))
			if ( answer().equals( "" ))
				utterance = Strings.fromString( dnk()); // need an answer, but we don't have one
			else
				// ok replace "..." with answer -- where reply maybe "martin/mother/computer"
				utterance = Strings.replace( utterance, Strings.ellipsis, Strings.fromString( answer.toString()));
		
		// outbound and general colloquials
		if (!isVerbatim())
			utterance = both.apply( host.apply( utterance ));

		//return audit.traceOut( Language.asProse( Variable.deref( utterance )));
		// ...deref any envvars...
		return Language.asString( Variable.deref( utterance ));
	}
	
	// -- helpers
	public static void setContext( Attribute p ) {
		//audit.traceIn( "SetContext", p.toString());
		// terms need to be spaced out e.g. ". ? !"
		if (p.name().equals("LISTFORMATSEP")) Reply.listSep(       p.value()); else
		if (p.name().equals("CONJUNCTIONS" )) Reply.conjunctions(  Strings.fromString( p.value() )); else
		if (p.name().equals("REFERENCERS"  )) Reply.referencers(   Strings.fromString( p.value() )); else
		if (p.name().equals("ANDLISTFORMAT")) Reply.andListFormat( p.value()); else
		if (p.name().equals("ORLISTFORMAT" )) Reply.orListFormat(  p.value()); else
		if (p.name().equals("LISTSEP" ))     List.sep(  p.value()); else
		if (p.name().equals("REPEATFORMAT" )) Reply.repeatFormat(  p.value()); else
		if (p.name().equals("LOCATION" )) Filesystem.location(  p.value()); else
		if (p.name().equals("TERMS")) Language.terminators( Strings.fromString(p.value())); else
		if (p.name().equals( "DNU" )) Reply.dnu( p.value()); else
		if (p.name().equals( "DNK" )) Reply.dnk( p.value()); else
		if (p.name().equals( "YES" )) Reply.yes( p.value()); else
		if (p.name().equals(  "NO" )) Reply.no(  p.value()); else
		if (p.name().equals(  "IK" )) Reply.ik(  p.value());
		//audit.traceOut();
	}
	public static void setContext( ArrayList<Attribute> p ) {
		//audit.traceIn( "SetContext", "" );
		if (null != p) for (int i=0; i<p.size(); i++) Reply.setContext( p.get( i ));
		//audit.traceOut();
	}
	public void handleDNU( String[] utterance ) {
		audit.traceIn( "handleDNU", Strings.toString( utterance, Strings.CSV ));
		if (Language.terminators()[ 0 ].equals( Language.terminatorIs( utterance )))
			utterance = Language.stripTerminator( utterance );
		
		// Construct the DNU format
		String[] fmt = Strings.fromString( Reply.dnu() );
		fmt = Strings.append( fmt, "," );
		//if (!Language.apostrophesReq()) fmt = Strings.append( fmt, "'" );
		fmt = Strings.append( fmt, "..." );
		//if (!Language.apostrophesReq()) fmt = Strings.append( fmt, "'" );
		
		format( fmt );
		answer( Strings.toString( utterance, Strings.SPACED ));
		verbatimIs( true );
		audit.traceOut();
	}
	public static void main( String[] args ) {
		Reply.dnu( "Pardon?" );
		Reply.dnk( "Dunno" );
		Reply.no(  "No" );
		Reply.yes( "Yes" );
		
		Reply r = new Reply();
		System.out.println( "Initially: "+ r.toString());
		r.format( "The answer to X is ..." );
		
		Attributes attrs = new Attributes();
		attrs.add( new Attribute( "x", "life the universe and everything" ));
		Reply.pushContext( attrs );
		System.out.println( "Context is "+ Reply.context().toString());
		
		System.out.println( "W/no answer:"+ r.toString());
		r.answer( "42" );
		System.out.println( "Finally:"+ r.toString());
}	}