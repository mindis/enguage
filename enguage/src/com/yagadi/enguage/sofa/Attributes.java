package com.yagadi.enguage.sofa;

import java.util.ArrayList;
import java.util.Locale;

import com.yagadi.enguage.concept.Tag;
import com.yagadi.enguage.expression.Answer;
import com.yagadi.enguage.expression.Plural;
import com.yagadi.enguage.util.Strings;

public class Attributes extends ArrayList<Attribute> {
	//static private Audit audit = new Audit( "Attributes" );
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

	public boolean has( String name, String value ) { return indexOf( new Attribute( name, name )) != -1; }
	public boolean has( String name ) { return has( name, name ); }
	
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
	
	public String toString( String sep ) {
		if (null == sep) sep = "";
		String s = "";
		for (int i=0, sz=size(); i<sz; ++i ) s += ((i==0?"":sep) + get( i ).toString());
		return s;
	}
	public String toString() { return " "+ toString( " " ); }

	public static boolean isUpperCase( String s ) {return s.equals( s.toUpperCase( Locale.getDefault()));}
	// BEVERAGE -> coffee + [ NAME="martins", beverage="tea" ].deref( "SINGULAR-NAME needs a $BEVERAGE" );
	// => martin needs a coffee.
	private String derefName( String name ) { // hopefully non-blank string
		//audit.traceIn( "derefName", name );
		String value = null;
		if (null != name && name.length() > 0 ) {
			String orig = name;
			// if we have QUOTED-X, retrieve X and leave answer QUOTED
			// [ x="martin" ].derefChs( "QUOTED-X" ) => '"martin"'
			boolean prefixed = '$'==name.charAt( 0 ) && !name.equals( "$" ),
					quoted = name.contains( Tag.quotedPrefix ),
					plural = name.contains( Tag.pluralPrefix ),
					singular = name.contains( Tag.singularPrefix );
			
			// remove all prefixes...
			name = name.substring( name.lastIndexOf( "-" )+1 );
			// just in case user has used $X notation in think intention
			if (prefixed) name = name.substring( 1 );
			// do the dereferencing...
			if ( isUpperCase( name )) {
				value = get( name.toLowerCase( Locale.getDefault() ));
				//audit.debug( "Found: "+name +"='"+ value +"'");
			}
			if (value == null || value.equals( "" ))
				value = orig;
			else {
				if (plural)   value = Plural.plural( value );
				if (singular) value = Plural.singular( value );
				if (quoted)   value = "'"+ value +"'";
		}	}
		//return audit.traceOut( value );
		return value;
	}
	
	public String[] deref( String[] ans ) {
		//audit.traceIn("deref", Strings.toString( ans, Strings.DQCSV ));
		//audit.debug( "attributes are: "+ toString() );
		if (null != ans)
			for (int i=0; i<ans.length; i++)
				ans[ i ] = derefName( ans[ i ]);
		//audit.traceOut(Strings.toString( ans, Strings.DQCSV ));
		return ans;
	}
	public String deref( String value ) {
		// called on REPLYing
		return Strings.toString(
			deref(
				Strings.fromString( value )
			),
			Strings.SPACED
		);
	}
	public void delistify() { // "beer+crisps" => "beer and crisps"
		for (int i=0, sz=size(); i<sz; i++)
			get( i ).value( // set value to...
					new Answer().value( // ...an answer, with the value of...
							get( i ).value() // ... this value
				).toString() // ... as a string
			);
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
			System.out.println( "\tderef martin is "+  a.derefName( "$martin" ));
			
			//System.out.println( "\temily is "+  b.get( "emily" ));
			a.remove( new Attribute( "martin" ));
			System.out.println( "\ta now (removing martin) is:"+ a.toString());
			System.out.println( "\tafter deletion a is: "+ a.toString());
			//System.out.println( "\tb is: %s"+ b.toString( null ));
			b = new Attributes();
			System.out.println( "\tb is: >"+ b.toString() +"<" );
		} else if (1 == argc) {
			Attributes a = new Attributes( "   NAME=\"one\" name2='two'  " );
			System.out.println( "File contains: "+ a.toString());
		} else
			System.err.println( "Usage: Attributes [<filename>]" );
}	}