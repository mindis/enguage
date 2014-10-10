package com.yagadi.enguage.util;

import java.util.Locale;

import java.util.ArrayList;

import com.yagadi.enguage.expression.Answer;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.util.Attribute;

public class Attributes extends ArrayList<Attribute> {
	static private Audit audit = new Audit( "Attributes" );
	static final long serialVersionUID = 0;
	
	public Attributes() { super(); }
	public Attributes( Attributes orig ) { super( orig ); }
	public Attributes( String s ) {
		super();
		int i=0, sz=s.length();
		while (i<sz && Character.isWhitespace( s.charAt( i ) )) i++; // read over spaces
		while (i<sz && Character.isLetter( s.charAt( i ) )) {
			String name = "", value = "";
			
			while (i<sz && Character.isLetterOrDigit( s.charAt( i ) )) name += Character.toString( s.charAt( i++ ));
			while (i<sz && Character.isWhitespace( s.charAt( i ) )) i++ ; // read over spaces
			
			if (i<sz && '=' == s.charAt( i )) { // look for a value
				i++; // read over '='
				while (i<sz && Character.isWhitespace( s.charAt( i ) )) i++; // read over spaces
				if (i<sz && ('\'' == s.charAt( i ) || '"' == s.charAt( i ))) {
					Character quoteMark = '\0';
					do {
						if (i<sz) quoteMark = s.charAt( i++ ); // save and read over '"' or "'"
						while ( i<sz && quoteMark != s.charAt( i ))
							value += Character.toString( s.charAt( i++ ));
						i++; //read over end quote
						while (i<sz && Character.isWhitespace( s.charAt( i ) )) i++; // read over spaces
						if (i<sz && quoteMark == s.charAt( i )) value += "\n"; // split multi-line values
					} while (i<sz && quoteMark == s.charAt( i )); // inline const str found e.g. .."   -->"<--...
				}
				add( new Attribute( name, value )); // was append( NAME, value );
			}
			while (i<sz && Character.isWhitespace( s.charAt( i ) )) i++; // read over spaces
	}	}

	public boolean has( String name ) { return indexOf( name ) != -1; }
	public String get( String name ) {
		for (int i=0, sz=size(); i<sz; i++ ) {
			Attribute a = get( i );
			if (a.name().equals( name )) {
				return a.value();
		}	}
		return "";
	}
	public String getIgnoreCase( String name ) {
		int rc = -1;
		for (int i=0, sz=size(); i<sz; i++ )
			if (get( i ).name().equalsIgnoreCase( name )) {
				rc = i;
				break;
			}
		return -1 == rc ? "" : get( rc ).value();
	}
	
	public String toString( String indent ) {
		if (null == indent) indent = "";
		String s = "";
		for (int i=0, sz=size(); i<sz; ++i ) s += ((i==0?"":indent) + get( i ).toString());
		return s;
	}
	public String toString() { return toString( " " ); }

	
	
	/* 
	 * getCommand() --- doesn't really belong in SOFA - to Attributes? deref
	 */
	// "interpreter $val" + val='fred' -> [ "interpreter, "fred" ]
	private static char varPrefix = '$';
	public String[] getCommand( String[] a ) {
		//audit.traceIn( "getCommand", "chs='"+chs+"', varPrefix='"+varPrefix+"', attributes=["+ values.toString( " " )+"]" );
		String[] cmd = new String[ 0 ];
		for (int i=0, sz=a.length; i<sz; i++) {
			String param = a[ i ];
			if (param.charAt( 0 ) == varPrefix ) { // starts with prefix
				if (!param.equals( varPrefix )) { // it isn't just prefix
					String tmp = param.substring( 1 );
					String atval = get( tmp );
					if (null != atval && 0 != atval.compareTo( "" ))
						param = atval;
					else {
						String enval = Variable.get( tmp );
						if (enval != null)
							param = enval;
						else
							audit.ERROR( "sofa: Undefined "+ varPrefix + tmp +" ("+ Strings.toString( a, Strings.CSV ) +") in Env or ["+ toString() +"]" );
			}	}	}
			cmd = Strings.append( cmd, param );
		}
		return cmd; //audit.traceOut( cmd ); // Language.indefiniteArticleFlatten( interpreter );
	}


	
	public static boolean isUpperCase( String s ) {return s.equals( s.toUpperCase( Locale.getDefault()));}
	// TODO: there is also a deref in Variable.java -- 
	// TODO: Split this into Variable.deref() and Attribute deref() -- like getCommand() above 
	// BEVERAGE -> coffee + [ NAME="martin", beverage="tea" ].deref( "NAME needs a $BEVERAGE" );
	// => martin needs a coffee.
	public String derefChs( String nbChs ) { // hopefully non-blank string
		//audit.traceIn( "derefChs", nbChs );
		String quotedPrefix = "QUOTED-";
		boolean quoted = false;
		if (null != nbChs && nbChs.length() > 0 ) {
			String newval = null;
			// check is were working with 'QUOTED-X'
			if (nbChs.length() > quotedPrefix.length()
			 && nbChs.substring( 0, quotedPrefix.length() ).equals( quotedPrefix )) {
				quoted = true;
				nbChs = nbChs.substring( quotedPrefix.length() );
			}
			
			if ('$'==nbChs.charAt( 0 ) && !nbChs.equals( "$" )) {
				// just in case user has used $X notation in think intention
				newval = get( nbChs.substring( 1 ));
				if (null != newval && !newval.equals( "" ))
					nbChs = newval;
			} else if ( isUpperCase( nbChs )) { // isUpperCase()!
				// if we have QUOTED-X, retrieve X and leave answer QUOTED
				// [ x="martin" ].derefChs( "QUOTED-X" ) => '"martin"'
				newval = get( nbChs.toLowerCase( Locale.getDefault())); // hint needs to be obtained from config
				
				if (null != newval && !newval.equals("")) { // doesn't match a given value...
					String tmp = "_" + nbChs;
					if (Variable.isSet( tmp )) // but is set in environment...
						nbChs = tmp; // don't deref envvars here?
					else
						nbChs = newval;
		}	}	}
		//return audit.traceOut( quoted ? "'"+nbChs+"'" : nbChs );
		return quoted ? "'"+nbChs+"'" : nbChs;
	}
	
	public String[] deref( String[] ans ) {
		if (null != ans)
			for (int i=0; i<ans.length; i++)
				ans[ i ] = derefChs( ans[ i ]);
		return ans;
	}
	public String deref( String value ) {
		return Strings.toString(
			deref(
				Strings.fromString( value )
			),
			Strings.SPACED
		);
	}
	public void delistify() {
		for (int i=0, sz=size(); i<sz; i++)
			get( i ).value( new Answer().value( get( i ).value() ).toString());
	}

	public static void main( String argv[]) {
		int argc = argv.length;
		if (0 == argc) {
			Attributes b = null, a = new Attributes();
			a.add( new Attribute( "martin", "hero" ));
			a.add( new Attribute( "ruth", "fab" ));
			System.out.println( "Initial test: "+ a.toString());
			System.out.println( "\tmartin is "+  a.get( "martin" ) +"ic");
			System.out.println( "\truth is "+   a.get( "ruth" ));
			System.out.println( "\tjames is "+  a.get( "james" ));
			System.out.println( "\tderef martin is "+  a.derefChs( "$martin" ));
			
			//System.out.println( "\temily is "+  b.get( "emily" ));
			a.remove( new Attribute( "martin" ));
			System.out.println( "\ta now (removing martin) is:"+ a.toString());
			System.out.println( "\tafter deletion a is: "+ a.toString());
			//System.out.println( "\tb is: %s"+ b.toString( null ));
			b = new Attributes();
			System.out.println( "\tb is: >"+ b.toString() +"<");
		} else if (1 == argc) {
			Attributes a = new Attributes( "   NAME=\"one\" name2='two'  " );
			System.out.println( "File contains: "+ a.toString());
		} else
			System.err.println( "Usage: Attributes [<filename>]" );
}	}