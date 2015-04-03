package com.yagadi.enguage.expression;

import java.util.ArrayList;

import com.yagadi.enguage.concept.Autoload;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.sofa.Numeric;
import com.yagadi.enguage.sofa.Preferences;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Reply { // a reply is basically a formatted answer
	
	static private Audit audit = new Audit( "Reply" );

	private final static String  verbose     = "verbose"; // this is also defined in iNeed/MainActivity.java 
	private final static boolean initVerbose = false;
	private boolean verbose() {
		Preferences p = Preferences.getPreferences();
		if (p==null) return true; // default to a verbose state
		return p.get( verbose, initVerbose );
	}
	
	static public final int   NO = 0; // FALSE -- -ve
	static public final int  YES = 1; // TRUE  -- +ve
	static public final int  DNU = 2; // DO NOT UNDERSTAND
	static public final int   NK = 3; // NOT KNOWN -- init
	static public final int   IK = 4; // I know, silly!
	static public final int  CHS = 5; // use stored expression
	
	static private boolean verbatim;
	static public  boolean isVerbatim() { return verbatim; }
	static public  void    verbatimIs( boolean val ) { verbatim = val; }
	
	static private String strangeThought = "DNU";
	static public  void   strangeThought( String thought ) { strangeThought = thought; }
	static public  String strangeThought(){ return strangeThought; }

	static private String dnu = "DNU";
	static public  void   dnu( String s ) { dnu = s; }
	static public  String dnu(){ return dnu; }

	// TODO: these need to be Strings
	static private String dnk = "DNK";
	static public  void   dnk( String s ) { dnk = s; }
	static public  String dnk() { return dnk; }

	static private String ik = "IK";
	static public  void   ik( String s ) { ik = s; }
	static public  String ik() { return ik; }

	static private String no = "no";
	static public  void   no(  String s ) { no = s; }
	static public  String no() { return no; }
	
	static private String yes = "yes";
	static public  void   yes( String s ) { yes = s; }
	static public  String yes() { return yes; }

	static private String failure = Shell.FAIL;
	static public  void   failure(  String s ) { failure = s; }
	static public  String failure() { return failure; }
	
	static private String success = Shell.SUCCESS;
	static public  void   success( String s ) { success = s; }
	static public  String success() { return success; }

	static private String repeatFormat = "I said, ... .";
	static public  void   repeatFormat( String s ) { repeatFormat = s; }
	static public  String repeatFormat() { return repeatFormat; }

	static private String helpPrefix = "you can say, ";
	static public  void   helpPrefix( String s ) { helpPrefix = s; }
	static public  String helpPrefix() { return helpPrefix; }

	static private Strings conjunctions = new Strings( ", and" );
	static public  void    conjunctions( Strings sa ) { conjunctions = sa; }
	static public  Strings conjunctions() { return conjunctions; }

	static private Strings referencers = new Strings( "the" );
	static public  void   referencers( Strings sa ) { referencers = sa; }
	static public  Strings referencers() { return referencers; }

	static private String listSep = "/";
	static public  void   listSep( String s ) { listSep = s; }
	static public  String listSep() { return listSep; }

	static private Strings andListFormat = new Strings( ", /, and ", '/' );
	static public  void     andListFormat( String s ) { andListFormat = new Strings( s, listSep().charAt( 0 )); }
	static public  Strings andListFormat() { return andListFormat; }

	static private Strings orListFormat = new Strings( ", /, or ", '/' );
	static public  void    orListFormat( String s ) { orListFormat = new Strings( s, listSep().charAt( 0 )); }
	static public  Strings orListFormat() { return orListFormat; }

	static private ArrayList<Attributes> contexts = new ArrayList<Attributes>();
	static public void  pushContext( Attributes ctx ){ contexts.add( 0, ctx ); }
	static public void  popContext() { contexts.remove( 0 );}
	static public Attributes context() {
		if (contexts.size() == 0) pushContext( new Attributes() );
		return contexts.get( 0 );
	}

	private int type = DNU;
	private int calculateType() {
		if (answer().equals( "" ) && format.size() == 0) {
			return DNU;
		} else if (answer().equals( no ) && format.equals( new Strings( ik ))) {
			return CHS;
		} else if (answer().equals( "" ) && format.contain( "..." )) {
			return NK;
		} else if (answer().equals( "" ) && !format.contain( "..." )) {
				 if (format.equals( new Strings(   yes ))) return YES;
			else if (format.equals( new Strings(success))) return YES;
			else if (format.equals( new Strings(    no ))) return NO;
			else if (format.equals( new Strings(failure))) return NO;
			else if (format.equals( new Strings(    ik ))) return IK;
			else if (format.equals( new Strings(   dnk ))) return NK;
			else if (format.equals( new Strings(   dnu ))) return DNU;
			else return CHS;
		} else {
			     if (answer().equalsIgnoreCase(   yes )) return YES;
			else if (answer().equalsIgnoreCase(success)) return YES;
			else if (answer().equalsIgnoreCase(    no )) return NO;
			else if (answer().equalsIgnoreCase(failure)) return NO;
			else if (answer().equalsIgnoreCase(    ik )) return IK;
			else if (answer().equalsIgnoreCase(   dnk )) return NK;
			else if (answer().equalsIgnoreCase(   dnu )) return DNU;
			else return CHS;
	}	}
	public int      getType() { return type; }
	public boolean positive() {return YES == type || CHS == type; } // != !negative() !!!!!
	public boolean negative() {return  NO == type ||  NK == type; } // != !positive() !!!!!

	private Answer  answer = new Answer();
	private String  ansCache = ""; // effectively null!
	public  String  answer() {
		if (!ansCache.equals("")) return ansCache;
		return ansCache = answer.toString();
	}
	public  Reply   answer( String ans ) {
		answer.values( ans == null ? "" : ans );
		// type is dependent on answer
		ansCache = "";
		type = calculateType();
		return this;
	}
	public  boolean forward() { return answer.forward(); }

	private Strings format = new Strings(); // format is empty!
	public  Reply   format( String s ) { format( new Strings( s )); return this; }
	public  Reply   format( Strings sa ) {
		format = sa;
		if (!format.contain( "..." )) answer( "" );
		type = calculateType(); // type is dependent on format -- should it be???
		return this;
	}
	public Strings format() {
		//audit.traceIn("format", format.toString(0));
		if (!verbose()) {
			if (format.size() > 1 && format.get( 1 ).equals( "," ))
				if (format.get( 0 ).equalsIgnoreCase( yes ) || // yes is set to "OK", when yes is "yes", this fails...
					format.get( 0 ).equalsIgnoreCase(  no ) ||
					format.get( 0 ).equalsIgnoreCase( success )) {
					//audit.traceOut("returning only 1st");
					return new Strings( say() + " " +format.get( 0 )); // return only first
				} else if (format.get( 0 ).equalsIgnoreCase( failure )) {
					//audit.traceOut("returning rest");
					return new Strings( say()).append( format.copyAfter( 1 ).filter()); // forget 1st & 2nd
				}
			//audit.traceOut("returning filtered format");
			return new Strings( say()).append( format.filter( ));
		}
		//audit.traceOut("returning full format");
		return new Strings( say()).append( format );
	}
	
	private boolean repeated = false;
	public  void    repeated( boolean s ) { repeated = s; }
	public  boolean repeated() { return repeated; }

	private boolean done = false;
	public  void    doneIs( boolean b ) { done = b; }
	public  boolean isDone() { return done; }

	public  Strings say = new Strings();
	public  String  say() { return say.toString( Strings.SPACED ); }
	public  void    say( Strings sa ) { say.addAll( Shell.addTerminator( sa )); }
	
	// ---
	public String asString() {
		String format = format().toString( Strings.SPACED );
		if (format.equals( dnu() )) format = "-";
		return format +"/"+ answer() +"/"+ type;
	}

	public  String toString() { // TODO cache this!!
		audit.traceIn( "toString", format().toString() );
		verbatimIs( false );
		Strings utterance = format();
		if (0 == utterance.size())
			utterance = answer.none() ? new Strings( dnu()) : answer.values();

		// ... then post-process:
		// if not terminated, add first terminator -- see Tag.c::newTagsFromDescription()
		if (utterance.size() > 0 && !Shell.isTerminator( utterance.get( utterance.size() -1)) &&
			!((utterance.size() > 1) && Shell.isTerminator( utterance.get( utterance.size() -2)) && Language.isQuote( utterance.get( utterance.size() -1))))
			utterance.add( Shell.terminators().get( 0 ));

		// ...finally, if required put in answer (verbatim!)
		if (utterance == null || utterance.size() == 0)
			if ( answer().equals( "" ))
				utterance = new Strings( dnk() );
			else
				utterance = new Strings( answer.toString()); // use the raw answer???
		else if (utterance.contain( Strings.ELLIPSIS )) {
//			audit.ERROR( "replyToString() used to look for ellipsis - now done in Intention" );
			if ( answer().equals( "" ))
				utterance = new Strings( dnk() ); // need an answer, but we don't have one
			else
				// ok replace "..." with answer -- where reply maybe "martin/mother/computer"
				utterance.replace( Strings.ellipsis, new Strings( answer.toString()));
		}
		
		// outbound and general colloquials
		if (!isVerbatim())
			utterance = Colloquial.applyOutgoing( utterance );
			
		// ...deref any context...
		utterance = context().deref( utterance );
		
		// English-dependent processing...
		utterance = Language.indefiniteArticleVowelSwap(
						Language.sentenceCapitalisation( 
							Language.pronunciation( 
								Plural.ise( utterance ))));
		
		String rc = Language.asString( Numeric.deref( /*Variable.deref(*/ utterance /*)*/));
		audit.traceOut( rc );
		// ...deref any envvars...  ...any numerics...
		return rc;
	}
	
	// -- helpers
	public static void setContext( Attribute p ) {
		//audit.traceIn( "SetContext", p.toString());
		// terms need to be spaced out e.g. ". ? !"
		if (p.name().equals("LISTFORMATSEP")) Reply.listSep(       p.value()); else
		if (p.name().equals("CONJUNCTIONS" )) Reply.conjunctions(  new Strings( p.value() )); else
		if (p.name().equals("REFERENCERS"  )) Reply.referencers(   new Strings( p.value() )); else
		if (p.name().equals("ANDLISTFORMAT")) Reply.andListFormat( p.value()); else
		if (p.name().equals("ORLISTFORMAT" )) Reply.orListFormat(  p.value()); else
		if (p.name().equals("REPEATFORMAT" )) Reply.repeatFormat(  p.value()); else
		if (p.name().equals(    "LOCATION" )) Filesystem.location(  p.value()); else
		if (p.name().equals("HPREFIX")) Reply.helpPrefix( p.value()); else
		if (p.name().equals("SUCCESS")) Reply.success( p.value()); else
		if (p.name().equals("FAILURE")) Reply.failure( p.value()); else
		if (p.name().equals("TERMS")) Shell.terminators( new Strings( p.value() )); else
		if (p.name().equals( "TTL" )) Autoload.ttl( p.value()); else
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
	public void handleDNU( Strings utterance ) {
		audit.traceIn( "handleDNU", utterance.toString( Strings.CSV ));
		if (Shell.terminators().get( 0 ).equals( Shell.terminatorIs( utterance )))
			utterance = Shell.stripTerminator( utterance );
		
		// Construct the DNU format
		Strings fmt = new Strings( Reply.dnu() );
		fmt.add( "," );
		//if (!Language.apostrophesReq()) fmt = Strings.append( fmt, "'" );
		fmt.add( "..." );
		//if (!Language.apostrophesReq()) fmt = Strings.append( fmt, "'" );
		/*
		 * take this out for the moment... ...needs more thought 
		if (!strangeThought.equals( "" ))
			fmt.add( " when thinking about "+ strangeThought());
		 */
		
		format( fmt );
		answer( utterance.toString( Strings.SPACED ));
		verbatimIs( true );
		audit.traceOut();
	}
	public static String spell( String a ) { return spell( a, false ); }
	public static String spell( String a, boolean slowly ) {
		String b = "";
		for (int i=0; i<a.length(); i++)
			b += ( " "+ ( slowly && i>0 ? ", ":"" )+ a.charAt( i ));
		return b;
	}
	public static void main( String args[] ) {
		Audit.turnOn();
		
		Reply.dnu( "Pardon?" );
		Reply.dnk( "Dunno" );
		Reply.no(  "No" );
		Reply.yes( "Yes" );
		
		Reply r = new Reply();
		r.answer( "42" );
		audit.audit( "Initially: "+ r.toString());
		r.answer.valuesAdd( "53" );
		audit.audit( "W/no format:"+ r.toString());
		
		r.format( "The answer to X is ..." );
		
		Attributes attrs = new Attributes();
		attrs.add( new Attribute( "x", "life the universe and everything" ));
		Reply.pushContext( attrs );
		audit.audit( "Context is "+ Reply.context().toString());
		
		audit.audit( "Finally:"+ r.toString());
}	}