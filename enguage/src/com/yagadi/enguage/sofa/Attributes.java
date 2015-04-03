package com.yagadi.enguage.sofa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;

import com.yagadi.enguage.concept.Tag;
import com.yagadi.enguage.expression.Plural;
import com.yagadi.enguage.expression.Reply;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

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
/*
	public boolean xxxequals( Attributes attrs ) {
		/* 
		 * this is probably a dreadful algorithm: test this
		 * /
		Attribute a;
		Iterator<Attribute> i = iterator();
		while (i.hasNext()) {
			a = i.next();
			if (!a.value().equals( attrs.get( a.name() )))
				return false;
		}
		return true; // for now
	}// */
	public boolean matches( Attributes pattern ) {
		/* Theory is that pattern will typically have less content than target.
		 */
		if (pattern.size() > size())
			return false;
		
		Attribute a;
		Iterator<Attribute> pi = pattern.iterator();
		while (pi.hasNext()) {
			a = pi.next();
			if (!a.value().equals( get( a.name() )))
				return false;
		}
		return true; // for now
	}
	public boolean has( String name, String value ) { return indexOf( new Attribute( name, value )) != -1; }
	public boolean has( String name ) {
		Iterator<Attribute> i = iterator();
		while (i.hasNext())
			if (i.next().name().equals( name ))
				return true;
		return false;
	}
	public boolean hasIgnoreCase( String name ) {
		Iterator<Attribute> i = iterator();
		while (i.hasNext())
			if (i.next().name().equalsIgnoreCase( name ))
				return true;
		return false;
	}
	public String get( String name ) {
		Attribute a;
		Iterator<Attribute> i = iterator();
		while (i.hasNext()) {
			a = i.next();
			if (a.name().equals( name ))
				return a.value();
		}
		return "";
	}
	public String remove( String name ) {
		Attribute a;
		Iterator<Attribute> i = iterator();
		while (i.hasNext()) {
			a = i.next();
			if (a.name().equals( name )) {
				String tmp = a.value();
				i.remove();
				return tmp;
		}	}
		return "";
	}
	public String getIgnoreCase( String name ) {
		Attribute a;
		Iterator<Attribute> i = iterator();
		while (i.hasNext()) {
			a = i.next();
			if (a.name().equalsIgnoreCase( name )) return a.value();
		}
		return "";
	}
	
	public String toString( String sep ) {
		if (null == sep) sep = "";
		String s = "";
		for (int i=0, sz=size(); i<sz; ++i ) s += (sep + get( i ).toString());
		return s;
	}
	public String toString() { return toString( " " ); }

	public static boolean isUpperCase( String s ) {return s.equals( s.toUpperCase( Locale.getDefault()));}
	public static boolean isAlphabetic( String s ) {
		for ( int i=0; i< s.length(); i++ ) {
			int type = Character.getType( s.charAt( i ));
			if (type == Character.UPPERCASE_LETTER ||
				type == Character.LOWERCASE_LETTER   )
				return true;
		}
		return false;
	}
	// BEVERAGE -> coffee + [ NAME="martins", beverage="tea" ].deref( "SINGULAR-NAME needs a $BEVERAGE" );
	// => martin needs a coffee.
	private String derefName( String name, boolean expand ) { // hopefully non-blank string
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
			if ( isAlphabetic( name ) && isUpperCase( name )) {
				value = get( name.toLowerCase( Locale.getDefault() ));
				if (expand && !value.equals( "" ))
					value = name.toLowerCase( Locale.getDefault() ) +"='"+ value +"'";
				//audit.debug( "Found: "+name +" => '"+ value +"'");
			}
			if (value == null || value.equals( "" ))
				value = orig;
			else {
				if (plural)   value = Plural.plural( value );
				if (singular) value = Plural.singular( value );
				if (quoted)   value = "'"+ value +"'";
		}	}
		//audit.traceOut( value );
		return value;
	}
	public Strings deref( Strings ans ) {
		return deref( ans, false ); // backward compatible
	}
	public Strings deref( Strings ans, boolean expand ) {
		//audit.traceIn("deref", ans.toString( Strings.DQCSV ));
		//audit.debug( "attributes are: "+ toString() );
		if (null != ans) {
			ListIterator<String> i = ans.listIterator();
			while ( i.hasNext())
				i.set( derefName( i.next(), expand ));
		}
		//audit.traceOut( ans.toString( Strings.DQCSV ));
		return ans;
	}
	public String deref( String value ) { return deref( value, false ); }
	public String deref( String value, boolean expand ) {
		// called on REPLYing
		return deref( new Strings( value ), expand ).toString( Strings.SPACED );
	}
	public void delistify() { // "beer+crisps" => "beer and crisps"
		for (int i=0, sz=size(); i<sz; i++) {
			Attribute a = get( i );
			Strings sa = new Strings( a.value(), Attribute.VALUE_SEP.charAt( 0 ));
			set( i, new Attribute( a.name(), sa.toString( Reply.andListFormat() )));
	}	}
	public static Strings stripValues( Strings sa ) {
		for (int i=0; i< sa.size(); i++)
			sa.set( i, Attribute.expandValues( sa.get( i )).toString( Strings.SPACED ));
		return sa;
	}

	public static void main( String argv[]) {
		Audit.turnOn();
		Attributes b = null, a = new Attributes();
		a.add( new Attribute( "martin", "hero" ));
		a.add( new Attribute( "ruth", "fab" ));
		audit.audit( "Initial test: "+ a.toString());
		audit.audit( "\tmartin is "+  a.get( "martin" ) +"ic");
		audit.audit( "\truth is "+   a.get( "ruth" ));
		audit.audit( "\tjames is "+  a.get( "james" ));
		audit.audit( "\tderef martin is "+  a.deref( "what is MARTIN" ));
		
		//audit.audit( "\temily is "+  b.get( "emily" ));
		a.remove( new Attribute( "martin" ));
		audit.audit( "\ta now (removing martin) is:"+ a.toString());
		audit.audit( "\tafter deletion a is: "+ a.toString());
		//audit.audit( "\tb is: %s"+ b.toString( null ));
		b = new Attributes();
		audit.audit( "\tb is: >"+ b.toString() +"<" );
}	}