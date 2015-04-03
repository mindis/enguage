package com.yagadi.enguage.sofa.tier2;

import com.yagadi.enguage.concept.Tag;
import com.yagadi.enguage.concept.Tags;
import com.yagadi.enguage.expression.Plural;
import com.yagadi.enguage.sofa.Attribute;
import com.yagadi.enguage.sofa.Attributes;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class Item {
	/* 
	 * Item.class replaces Tuple.class
	 */
	static Audit audit = new Audit("Item");
	static public final String NAME = "item";
	//static private boolean debug = true;
	
	static private Strings format = new Strings(); // e.g. "cake slice", "2 cake slices" or "2 slices of cake"
	static public  void    format( String csv ) { format = new Strings( csv, ',' ); }
	static public  Strings format() { return format; }

	public Item() { tag.name( "item" ); }
	public Item( Tag t ) { this(); tag( t ); }
	public Item( Strings ss ) { // [ "black", "coffee", "quantity='1'", "unit='cup'" ]
		this();
		Attributes a = new Attributes();
		Strings content = new Strings();
		
		for (String s : ss)
			if (Attribute.isAttribute( s ))
				a.add( new Attribute( s ));
			else
				content.add( s );

		tag.content( new Tag().prefix( content.toString( Strings.SPACED )))
		   .attributes( a );
	}
	public Item( String s ) { this( new Strings( s ).contract( "=" )); } // "black coffee quantity='1' unit='cup'
	public Item( Item item ) { // copy c'tor
		this( item.content().size()>0 ? item.content().get( 0 ).prefix() : "" );
		tag.attributes( new Attributes( item.attributes()) );
	}
	
	// contains one tag -- can't extend as it is recursively defined.
	private Tag        tag = new Tag();
	public  void       tag( Tag t ) { tag = t; }
	public  Tag        tag() {        return tag; }
	public  Tags       content()    { return tag.content(); }
	public  Attributes attributes() { return tag.attributes(); }
	
	private void attributesReplace( String name, String value ) {
		tag.attributes().remove( name );
		tag.attributes().add( new Attribute( name, value ));
	}
	public Item update( Strings alterations ) {
		// e.g. update( ["quantity+=5"] )
		for (String alt : alterations) {
			if (alt.contains("+=")) {
				Strings s = new Strings( alt, '+' );
				if (s.size() == 2) {
					try {
						int val = Integer.parseInt( tag.attribute( s.get( 0 )));
						try {
							int delta = Integer.parseInt( s.get( 1 ).substring( 1 ));
							attributesReplace( s.get( 0 ), Integer.toString( val + delta ));
						} catch (Exception e) {
							audit.ERROR( "bad update attribute format" );
						}
					} catch (Exception e) {
						audit.ERROR( "bad original attribute format" );
					}
				} else
					audit.ERROR( "bad update attribute format" );
				
			} else if (alt.contains("-=")) {
				Strings s = new Strings( alt, '-' );
				if (s.size() == 2) {
					try {
						int val = Integer.parseInt( tag.attribute( s.get( 0 )));
						try {
							int delta = Integer.parseInt( s.get( 1 ).substring( 1 ));
							attributesReplace( s.get( 0 ), Integer.toString( val - delta ));
						} catch (Exception e) {
							audit.ERROR( "bad update attribute format" );
						}
					} catch (Exception e) {
						audit.ERROR( "bad original attribute format" );
					}
				} else
					audit.ERROR( "bad update attribute format" );
					
				
			} else if (alt.contains("=")) {
				Strings s = new Strings( alt, '=' );
				if (s.size() == 2)
					attributesReplace( s.get( 0 ), s.get( 1 ));
				else
					audit.ERROR( "bad update attribute format" );
		}	}
		return this;
	}
	public String counted( boolean pluralise, int num, String val ) {
		if (val.equals("1")) return "a";
		return pluralise ? Plural.ise( num, val ) : val;
	}
	public String toXml() { return tag.toString(); }
	public String toString() {
		Strings rc = new Strings();
		Strings formatting = format();
		if (formatting == null || formatting.size() == 0) {
			if (tag.content().size()>0)
				rc.add( tag.content().get(0).prefix());
		} else {
			boolean pl = false; // are we pluralising words found?
			int prevNum = 1;    // is so what are we pluralising to?
			for (String format : formatting)
				if (format.equals("")) { // main item: "black coffee"
					if (tag.content().size()>0) {
						String val = tag.content().get(0).prefix();
						rc.add( pl ? Plural.ise( prevNum, val ) : val );
						////rc.add( val );
					}
				} else { // formatted attributes: "UNIT of" + unit='cup' => "cups of"
					Strings subrc = new Strings();
					boolean found = true;
					for (String component : new Strings( format ))
						if ( Strings.isUpperCase( component )) // UNIT
							if (tag.attributes().hasIgnoreCase( component )) {
								String val = tag.attributes().getIgnoreCase( component );
								subrc.add( counted( pl,  prevNum, val ));  // UNIT='cup(S)'
								////subrc.add( tag.attributes().getIgnoreCase( component ));  // UNIT='cup(S)'
								try {
									prevNum=Integer.valueOf(val);
									pl = true;
								} catch(Exception bnfr) {
									pl=false;
								}
							} else
								found = false;
						else
							subrc.add( component ); // ...of...
					if (found) rc.addAll( subrc );
		}		}
		return rc.toString( Strings.SPACED );
	}
	static public String interpret( Strings cmd ) {
		String rc = Shell.FAIL;
		if (cmd.size() == 3
				&& cmd.get( 0 ).equals( "set" )
				&& cmd.get( 1 ).equals( "format" ))
		{
			Item.format( Strings.stripQuotes( cmd.get( 2 )));
			rc = Shell.SUCCESS;
		}
		return rc;
	}
	public static void main( String args[] ) {
		Audit.turnOn();
		Item.format( "QUANTITY,UNIT of,,from FROM" );
		Item t1 = new Item( new Strings( "black coffee quantity=1 unit='cup'" ).contract( "=" ));
		audit.debug( ">>t1 is "+ t1.toXml() );
		audit.debug( ">>t1 is "+ t1.toString() );
		t1 = new Item( "black coffee quantity='1' unit='cup'" );
		audit.debug( ">>t1 is "+ t1.toXml() );
		audit.debug( ">>t1 is "+ t1.toString() );
		System.exit( 0 );
		Item t2 = new Item( t1 );
		audit.debug( ">>t2 is "+ t2.toString() );
		audit.debug( ">>t1 and t2 are "+ (t1.tag().matches( t2.tag() ) ? "": "NOT ") + "equal, and should be.");
		String[] delta = { "quantity+=5" };
		t1.update( new Strings( delta ));
		audit.debug( ">>t1 is now "+ t1.toString() );
		audit.debug( ">>t2 is now "+ t2.toString() );
		audit.debug( ">>They are "+ (t1.tag.matches( t2.tag ) ? "still": "no longer") + " equal, and should not be.");
		Item t3 = new Item( "black coffee,1,cup" );
		audit.debug( ">>t3 is "+ t3.toString());
		Strings alterations = new Strings( "quantity-=3/unit=pint", '/' );
		t1.update( alterations );
		audit.debug( ">>t1 is "+ t1.toString() );
}	}
