package com.yagadi.enguage.concept;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import android.app.Activity;
import android.content.res.AssetManager;

import com.yagadi.enguage.expression.Plural;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Strings;

public class Tag {
	static Audit audit = new Audit( "Tag" );
	
	public static final int NULL   = 0;
	public static final int ATOMIC = 1;
	public static final int START  = 2;
	public static final int END    = 3;

	private int  type = NULL;
	private int  type() { return type; }
	private void type( int t ) { if (t>=NULL && t<=END) type = t; }

	
	public static final String quoted = "quoted";
	public static final String quotedPrefix = quoted.toUpperCase( Locale.ENGLISH ) + "-";
	public static final String plural = Plural.NAME; // "plural";
	public static final String pluralPrefix = plural.toUpperCase(Locale.ENGLISH) + "-";
	public static final String numeric = "numeric";
	public static final String numericPrefix = numeric.toUpperCase( Locale.ENGLISH ) + "-";
	public static final String singular = "singular";
	public static final String singularPrefix = singular.toUpperCase( Locale.ENGLISH ) + "-";
	public static final String phrase = "phrase";
	public static final String abstr  = "abstract";
	
	
	public boolean quoted() { return attribute( quoted ).equals( quoted );}
	public boolean phrased() { return attribute( phrase ).equals( phrase );}
	public boolean pluraled() { return attribute( plural ).equals( plural );}
	public boolean xnumeric() { return attribute( numeric ).equals( numeric );}
	
	public boolean validPlural( boolean isPlural ) { return pluraled() ? isPlural : true; }
	public boolean validQuote(  boolean isQuoted ) { return   quoted() ? isQuoted : true; }
	//public  boolean xvalidNumeric(boolean isNum    ) { return  xnumeric() ? isNum    : true; }
	

	public Strings prefixAsStrings = new Strings();
	public Strings prefixAsStrings() { return prefixAsStrings; }
	public String prefix = "";
	public String prefix(  ) { return prefix; }
	public Tag    prefix( String str ) {
		// set this shortcut..
		prefixAsStrings = new Strings( str );
		
		// replace any leading whitespace with a single space
		boolean whitespaceNeeded = false;
		if (str.length() > 0 && Character.isWhitespace( str.charAt( 0 ))) {
			whitespaceNeeded = true;
			while ( str.length() > 0 && Character.isWhitespace( str.charAt( 0 ))) 
				str = str.substring( 1 );
		}
		prefix = (whitespaceNeeded ? " " : "") + str;
		return this;
	}

	public Strings postfixAsStrings;
	public Strings postfixAsStrings() { return new Strings( postfix ); }
	public String postfix = "";
	public String postfix(  ) { return postfix; }
	public Tag    postfix( String str ) { postfix = str; return this; }

	private String name = "";
	public  String name() { return name; }
	public  Tag    name( String nm ) { if (null != nm) name = nm; return this; }

	Attributes attrs = new Attributes();
	public  Attributes attributes() { return attrs; }
	public  Tag        attributes( Attributes as ) { attrs = as; return this; }
	public  Tag        attributes( Attribute a ) { attrs.add( a ); return this; }
	public  String     attribute( String name ) { return attrs.get( name ); }
	public  Tag        attribute( String name, String value ) {attrs.add( new Attribute( name, value )); return this; }
	// ordering of attributes relevant to Autopoiesis
	public  Tag        append( String name, String value ) { attrs.add( new Attribute( name, value )); return this; }
	public  Tag        prepend( String name, String value ) {attrs.add( 0, new Attribute( name, value )); return this; } // 0 == add at 0th index!
	public  Tag        remove( int nth ) { attrs.remove( nth ); return this; }
	public  Tag        remove( String name ) { attrs.remove( name ); return this; }
	public  Tag        replace( String name, String value ) {
		attrs.remove( name );
		attrs.add( new Attribute( name, value ));
		return this;
	}
	
	private boolean nullTag() { return name.equals("") && prefix.equals(""); }
	
	Tags content = new Tags();
	public  Tags content() {return content;}
	public  Tag  content( Tags ta ) { content = ta; return this; }
	public  Tag  removeContent( int n ) { return content.remove( n ); }
	public  Tag  content( int n, Tag t ) { content.add( n, t ); return this; }
	public  Tag  content( Tag child ) {
		if (null != child && !child.nullTag()) {
			type = START;
			content.add( child );
		}
		return this;
	}
	// --
/*	private boolean equalsIgnoringAttribute( Tag pattern, String attribute ) {
		Tag t = new Tag( pattern );
		t.attributes().remove( attribute );
		audit.traceIn( "matchesIgnoringAttribute", "this="+ toString() +", pattern="+ t.toString() +", attribute="+ attribute );
		return audit.traceOut( equals( t ));
	}
/*	private String updateAttribute( Tag pattern, String attribute ) {
		audit.traceIn( "updateAttribute", pattern.toString() +"/"+ attribute );
		String rc = Shell.FAIL;
		Iterator<Tag> i = content().iterator();
		String newVal = pattern.attribute( attribute );
	
		while (i.hasNext()) {
			Tag t = i.next();
			if (t.equalsIgnoringAttribute( pattern, attribute )) {
				// see if this is an alteration
				Strings ss = new Strings( newVal );
				if (ss.size() == 2 && (ss.get( 0 ).equals( "+=" ) || ss.get( 0 ).equals( "-=" ))) {
					int oldInt = 0, newInt = 0;
					try {
						oldInt = Integer.valueOf( t.attributes().remove( attribute ));
						newInt = Integer.valueOf( ss.get( 1 ));
						newVal = Integer.toString( ss.get( 0 ).equals( "+=" ) ? oldInt + newInt :  oldInt - newInt );
					} catch (Exception e) {} // fail silently
				}
				audit.debug( "updating with:"+ newVal );
				t.attributes().remove( attribute );
				t.attribute( attribute, newVal );
				rc = new Item( t ).toString();
		}	}
		audit.traceOut( rc );
		return rc;
	}*/
	public void updateAttributes( Tag pattern ) {
		audit.traceIn( "updateAttributes", pattern.toString() );
		for (Attribute pa : pattern.attributes()) {
			String value = pa.value(),
					name = pa.name();
			if (name.equals( "quantity" )) {
				Strings vs = new Strings( value );
				if (vs.size() == 2) {
					String vfirst = vs.get( 0 ), vsecond = vs.get( 1 );
					if (vfirst.equals( "+=" ) || vfirst.equals( "-=" )) {
						int oldInt = 0, newInt = 0;
						try {
							oldInt = Integer.valueOf( attribute( name ));
						} catch (Exception e) {} // fail silently, oldInt = 0
						try {
							newInt = Integer.valueOf( vsecond );
							value = Integer.toString( vfirst.equals( "+=" ) ? oldInt + newInt :  oldInt - newInt );
						} catch (Exception e) {} // fail silently, newInt = 0;
			}	}	}
			//audit.debug( "updating with:"+ value );
			//remove( pa.name());
			//append( pa.name(), value );
			replace( pa.name(), value );
		}
		audit.traceOut();
	}
	public int remove( Tag pattern ) {
		int rc = 0;
		Iterator<Tag> i = content().iterator();
		while (i.hasNext()) {
			Tag t = i.next();
			if (t.equals( pattern )) {
				i.remove();
				rc++;
		}	}
		return rc;
	}
	public int removeMatches( Tag pattern ) {
		int rc = 0;
		Iterator<Tag> i = content().iterator();
		while (i.hasNext()) {
			Tag t = i.next();
			if (t.matches( pattern )) {
				i.remove();
				rc++;
		}	}
		return rc;
	}
	public boolean equals( Tag pattern ) {
		audit.traceIn( "equals", "this="+ toString() +", with="+ pattern.toString());
		boolean rc;
		if (attributes().size() < pattern.attributes().size()) { // not exact!!!
			//audit.audit( " T:Size issue:"+ attributes().size() +":with:"+ pattern.attributes().size() +":");
			rc = false;
		} else if ((content().size()==0 && pattern.content().size()!=0) 
				|| (content().size()!=0 && pattern.content().size()==0)) {
			//audit.audit(" T:one content empty -> false");
			rc =  false;
		} else {
			//audit.audit( " T:Checking also attrs:"+ attributes().toString() +":with:"+ pattern.attributes().toString() +":" );
			//audit.audit( " T:Checking also prefx:"+ prefix() +":with:"+ pattern.prefix() +":" );
			rc =   Plural.singular( prefix()).equals( Plural.singular( pattern.prefix()))
					&& Plural.singular( postfix()).equals( Plural.singular( pattern.postfix()))
					&& attributes().matches( pattern.attributes()) // not exact!!!
					&& content().equals( pattern.content());
			//audit.audit( " T:full check returns: "+ rc );
		}
		audit.traceOut( rc );
		return rc;
	}
	/* <fred attr1="a" attr2="b">fred<content/> bill<content/></fred>.(
	//		<fred attr2="b"/> -> true
	// )
	 * 
	 */
	public boolean matches( Tag pattern ) {
		audit.traceIn( "matches", "this="+ toString() +", pattern="+ pattern.toString() );
		boolean rc;
		if (attributes().size() < pattern.attributes().size()) {
			//audit.debug( "Too many attrs -> false" );
			rc = false;
		} else if ((content().size()==0 && pattern.content().size()!=0) 
				|| (content().size()!=0 && pattern.content().size()==0)) {
			//audit.audit("one content empty -> false");
			rc =  false;
		} else {
			//audit.debug( "Checking also attrs:"+ attributes().toString() +":with:"+ pattern.attributes().toString() );
			rc =   Plural.singular( prefix()).contains( Plural.singular( pattern.prefix()))
					&& postfix().equals( pattern.postfix())
					&& attributes().matches( pattern.attributes())
					&& content().matches( pattern.content());
			//audit.debug("full check returns: "+ rc );
		}
		audit.traceOut( rc );
		return rc;
	}
	public boolean matchesContent( Tag pattern ) {
		audit.traceIn( "matches", "this="+ toString() +", pattern="+ pattern.toString() );
		boolean rc;
		if (   (content().size()==0 && pattern.content().size()!=0) 
			|| (content().size()!=0 && pattern.content().size()==0)) {
			//audit.audit("one content empty -> false");
			rc =  false;
		} else {
			//audit.debug( "Checking also attrs:"+ attributes().toString() +":with:"+ pattern.attributes().toString() );
			rc =   Plural.singular( prefix()).contains( Plural.singular( pattern.prefix()))
					&& postfix().equals( pattern.postfix())
					&& content().matches( pattern.content());
			//audit.debug("full check returns: "+ rc );
		}
		audit.traceOut( rc );
		return rc;
	}

	// -- constructors...
	public Tag() {}
	
	// -- tag from string ctor
	private Tag doPreamble() {
		int i = 0;
		String preamble = "";
		while (i < postfix().length() && '<' != postfix().charAt( i )) preamble += postfix().charAt( i++ );
		prefix( preamble );
		if (i < postfix().length()) {
			i++; // read over terminator
			postfix( postfix().substring( i )); // ...save rest for later!
		} else
			postfix( "" );
		return this;
	}
	private Tag doName() {
		int i = 0;
		while(i < postfix().length() && Character.isWhitespace( postfix().charAt( i ))) i++; //read over space following '<'
		if (i < postfix().length() && '/' == postfix().charAt( i )) {
			i++;
			type( END );
		}
		if (i < postfix().length()) {
			String name = "";
			while (i < postfix().length() && Character.isLetterOrDigit( postfix().charAt( i ))) name += postfix().charAt( i++ );
			name( name );
			
			if (i < postfix().length()) postfix( postfix().substring( i )); // ...save rest for later!
		} else
			name( "" ).postfix( "" );
		return this;
	}
	private Tag doAttrs() {
		int i = 0;
		while (i < postfix().length() && -1 == "/>".indexOf( postfix().charAt( i ))) i++;
		attributes( new Attributes( postfix().substring( 0, i ) ));
		if (i < postfix().length()) {
			type( '/' == postfix().charAt( i ) ? ATOMIC : START );
			while (i < postfix().length() && '>' != postfix().charAt( i )) i++; // read to tag end
			if (i < postfix().length()) i++; // should be at '>' -- read over it
			postfix( postfix().substring( i )); // save rest for later
		}
		return this;
	}
	private Tag doChildren() {
		if ( null != postfix() && !postfix().equals("") ) {
			Tag child = new Tag( postfix());
			while( NULL != child.type()) {
				// move child remained to tag...
				postfix( child.postfix());
				// ...add child, sans remainder, to tag content
				content( child.postfix( "" ));
				child = new Tag( postfix());
			}
			postfix( child.postfix() ).content( child.postfix( "" ));
		}
		return this;
	}
	private Tag doEnd() {
		name( "" ).type( NULL );
		int i = 0;
		while (i < postfix().length() && '>' != postfix().charAt( i )) i++; // read over tag end
		postfix( i == postfix().length() ? "" : postfix().substring( ++i ));
		return this;
	}
	public Tag( String cpp ) { // tagFromString()
		this();
		postfix( cpp );
		doPreamble().doName();
		if (type() == END) 
			doEnd();
		else {
			doAttrs();
			if (type() == START) doChildren();
	}	}
	// -- tag from string: DONE
	public Tag( String pre, String nm ) {
		this();
		prefix( pre ).name( nm );
	}
	public Tag( String pre, String nm, String post ) {
		this( pre, nm );
		postfix( post );
	}
	public Tag( Tag orig ) {
		this( orig.prefix(), orig.name(), orig.postfix());
		attributes( new Attributes( orig.attributes()));
		content( orig.content());
	}

	public String toString() {
		return prefix + (name.equals( "" ) ? "" :
			("<"+ name + attrs.toString()+ // attributes has preceding space
			(0 == content().size() ? "/>" : ( ">"+ content.toString() + "</"+ name +">" ))))
			+ postfix;
	}
	public String toText() {
		return prefix + (name.equals( "" ) ? "" :
			( name.toUpperCase( Locale.getDefault() ) +" "+  // attributes has preceding space
			(0 == content().size() ? "" : content.toText() )))
			+ postfix ;
	}
	
	public static Tag fromFile( String fname ) {
		Tag t = null;
		try {
			t = new Tag( Filesystem.stringFromStream( new FileInputStream( fname )) );
		} catch( IOException e ) {
			audit.ERROR( "no tag found in file "+ fname );
		}
		return t;
	}
	public static Tag fromAsset( String fname, Activity ctx ) {
		audit.traceIn( "fromAsset", fname );
		Tag t = null;
		AssetManager am = ctx.getAssets();
		try {
			InputStream is = am.open( fname );
			t = new Tag( Filesystem.stringFromStream( is ));
			is.close();
		} catch (IOException e) {
			audit.ERROR( "no tag found in asset "+ fname );
		}
		audit.traceOut();
		return t;
	}
	public Tag findByName( String nm ) {
		Tag rc = null;
		if (name().equals( nm ))
			rc = this; // found
		else {
			ArrayList<Tag> ta = content();
			for (int i=0; i<ta.size() && null == rc; ++i) // find first child
				rc = ta.get( i ).findByName( nm );
		}
		return rc;
	}

	public static void main( String argv[]) {
		Strings a = new Strings( argv );
		int argc = argv.length;
		Tag orig = new Tag("prefix ", "util", "posstfix").append("sofa", "show").append("attr","one");
		orig.content( new Tag().prefix(" show ").name("sub"));
		orig.content( new Tag().prefix( " fred " ));
		Tag t = new Tag( orig );
		audit.audit( "tag:  "+ orig.toString());
		audit.audit( "copy: "+ t.toString());
		t = new Tag( orig.toString());
		audit.audit( "copy2: "+ t.toString());
		Tag pattern = new Tag("prefix <util attr='one'/>posstfix");
		audit.audit( "patt: "+ pattern.toString());
		audit.audit( "orig "+ (orig.matchesContent( pattern )?"DOES":"does NOT") +" (and should) match pattern" );
		
		if (argc > 0) {
			audit.audit( "Comparing "+ t.toString() +", with ["+ a.toString( Strings.DQCSV ) +"]");
			Attributes attr = t.content().matchValues( a );
			audit.audit( (null == attr ? "NOT " : "("+ attr.toString() +")" ) + "Matched" );
}	}	}
