package com.yagadi.enguage.concept;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Conv extends Tag {
	/*
	 * conversion is needed to CONVERT any existing iNeed lists into a list tag structure:
	 *  friends
	 *  cookie,2
	 *  coffee,2,cups
	 * INTO
	 * 	<list>
	 * 		<item>friends</item>
	 * 		<item quantity="2">cookie</item>
	 * 		<item quantity="2" unit="cup">coffee</item>
	 * 	<list>
	 */
	static Audit audit = new Audit("Conv");
	
	public Conv() {super();}
	public Conv( String s ) {super( s );}
	
	public Conv name( String name ) {super.name(name); return this;}
	
	private void add( String s ) {content( new Tag().name( "item" ).content( new Tag().prefix( s )));}
	private Conv addAll( Strings ss ) {for(String s : ss) add( s ); return this;}
	private void convert() {
		name( "list" );
		addAll( new Strings( prefix(), '\n' ));
		prefix( "" );
	}
/*	private String convertToLine( Tag t ) {
		String line = "\""+ content().get(0).prefix() +"\"";
		return line;
	}
/*	private Strings convertToLines() {
		Strings ss = new Strings();
		for( Tag t: content())
			ss.add( convertToLine( t ));
		return ss;
	}
	*/
	private boolean isText() {
		return name().equals("");
	}
	private static void test( String s ) {
		Conv t = new Conv( s );
		if (t.isText()) t.convert();
		audit.audit("tag:"+t.toString());
	}
	public static void main( String[] argv ) {
		test( "this is a test\nthis is another test" );
		test( "<list><item>this is a test</item><item>this is another tast</item></list>" );
}	}
