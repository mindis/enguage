package com.yagadi.enguage.concept;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.yagadi.enguage.util.*;

import android.app.Activity;
import android.content.res.AssetManager;

public class Tag {
	static Audit audit = new Audit( "Tag" );
	
	public static final int NULL   = 0;
	public static final int ATOMIC = 1;
	public static final int START  = 2;
	public static final int END    = 3;

	        int  type = NULL;
	private int  type() { return type; }
	private void type( int t ) { if (t>=NULL && t<=END) type = t; }
	
	String[] prefixAsStrings = null;
	String[] prefixAsStrings() { return prefixAsStrings; }
	String prefix = "";
	String prefix(  ) { return prefix; }
	Tag    prefix( String str ) {
		// set this shortcut..
		prefixAsStrings = Strings.fromString( str );
		
		// replace any leading whitespace with a single space
		boolean whitespaceNeeded = false;
		if (str.length() > 0 && Character.isWhitespace( str.charAt( 0 ))) {
			whitespaceNeeded = true;
			while ( str.length() > 0 && Character.isWhitespace( str.charAt( 0 ))) 
				str = str.substring( 1 );
		}
		prefix = (whitespaceNeeded ? " " :"") + str;
		return this;
	}

	String[] postfixAsStrings;
	String[] postfixAsStrings() { return Strings.fromString( postfix ); }
	String postfix = "";
	String postfix(  ) { return postfix; }
	Tag    postfix( String str ) {
		postfix = str; return this;
	}

	       String name = "";
	public String name() { return name; }
	       Tag    name( String nm ) { if (null != nm) name = nm; return this; }

	Attributes attrs = new Attributes();
	public  Attributes attributes() { return attrs;}
	public  Tag        attributes( Attributes as ) { attrs = as; return this;}
	public  String     attribute( String name ) { return attrs.get( name );}
	public  Tag        append( String name, String value ) {attrs.add( new Attribute( name, value )); return this; }
	public  Tag        prepend( String name, String value ) {attrs.add( 0, new Attribute( name, value )); return this; } // 0 == add at 0th index!
	public  Tag        remove( int nth ) {attrs.remove( nth ); return this; }
	public  Tag        attribute( String name, String value ) {attrs.add( new Attribute( name, value )); return this; }
	
	Tags content = new Tags();
	public  Tags content() {return content;}
	public  Tag  content( Tags ta ) { content = ta; return this; }
	public  Tag  content( Tag child ) {
		if (null != child) {
			type = START;
			content.append( child );
		}
		return this;
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
		tagInsertTags( orig.content());
	}

	boolean quoted() { return attribute( "quoted" ).equals( "quoted" );}
	boolean phrased() { return attribute( "phrase" ).equals( "phrase" );}
	boolean pluraled() { return attribute( "plural" ).equals( "plural" );}
	
	boolean validPlural( boolean isPlural ) { return pluraled() ? isPlural : true; }
	boolean validQuote(  boolean isQuoted ) { return   quoted() ? isQuoted : true; }
	
	//
	// -- Helpers
	//
	Tag tagInsertTags( Tags tags ) {
		content = tags;
		return this;
	}
	void tagsAppendTag( Tag[] ts, Tag t ) {
		int len = ts.length - 1;
		if ( null != t && len > -1)
			ts[ len ].content( t );
	}
	public String toString() {
		return prefix + (name.equals( "" ) ? "" :
			("<"+ name +" "+ attrs.toString( " " )+ // attributes has preceding space
			(0 == content().length() ? "/>" : ( ">"+ content.toString() + "</"+ name +">" ))))
			+ postfix;
	}
	public String toText() {
		return prefix + (name.equals( "" ) ? "" :
			( name +" "+  // attributes has preceding space
			(0 == content().length() ? "" : content.toText() )))
			+ postfix;
	}
	public static Tag fromFile( String fname ) {
		Tag t = null;
		try {
			String str = Filesystem.stringFromStream( new FileInputStream( fname ));
			if (null == str)
				System.out.println( "No file:"+ fname );
			else
				t = new Tag( str );
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
			Tag[] ta = content().ta();
			for (int i=0; i<ta.length && null == rc; ++i) // find first child
				rc = ta[ i ].findByName( nm );
		}
		return rc;
	}

	public static void main( String argv[]) {
		int argc = argv.length;
		Tag orig = new Tag("pref ", "util", "posstfix").append("sofa", "show");
		orig.content( new Tag().prefix(" show ").name("sub"));
		orig.content( new Tag().prefix( " fred " ));
		Tag t = new Tag( orig );
		System.out.println( "tag:  "+ orig.toString());
		System.out.println( "copy: "+ t.toString());
		t = new Tag( orig.toString());
		System.out.println( "copy2: "+ t.toString());
		
		if (argc > 0) {
			System.out.println( "Comparing "+ t.toString() +", with ["+ Strings.toString( argv, Strings.DQCSV ) +"]");
			Attributes attr = t.content().matchValues( argv );
			System.out.println( (null == attr ? "NOT " : "("+ attr.toString() +")" ) + "Matched" );
}	}	}
