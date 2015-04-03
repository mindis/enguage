package com.yagadi.enguage.util;

import java.io.*;

import com.yagadi.enguage.util.Strings;

abstract public class Shell {

	public static final String SUCCESS = "TRUE";	
	public static final String FAIL    = "FALSE";
		
	abstract public String interpret( Strings argv ) ;
	
	
	static public Strings terminators = new Strings( ". ? !" );
	static public void    terminators( Strings a ){ terminators = a; }
	static public Strings terminators() { return terminators; }
	static public boolean isTerminator( String s ) { return Strings.contain( terminators(), s ); }
	static public String  terminatorIs( Strings a ){ return (null != a) && a.size()>0 ? a.get( a.size() -1) : ""; }
	static public boolean isTerminated( Strings a ) {
		boolean rc = false;
		if (null != a) {
			int last = a.size() - 1;
			if (last > -1) rc = isTerminator( a.get( last ));
		}
		return rc; 
	}
	static public Strings stripTerminator( Strings a ) {
		if (isTerminated( a ))
			a.remove( a.size() - 1 );
		return a;
	}
	static public String stripTerminator( String a ) {
		return stripTerminator( new Strings( a )).toString( Strings.SPACED );
	}
	static public Strings addTerminator( Strings a, String term ) {
		if (!isTerminated( a ) && null != term)
			a.add( term );
		return a;
	}
	static public Strings addTerminator( Strings a ) { return addTerminator( a, terminators().get( 0 )); }

	
	
	
	private String  prompt;
	public  String  prompt() { return prompt; }
	public  Shell   prompt( String p ) { prompt = p; return this; }
	
	private boolean aloud = true;
	public  boolean isAloud() { return aloud; }
	public  Shell   aloudIs( boolean is ) { aloud = is; return this; }
	
	private String prog;
	public  String name() { return prog; }
	public  Shell  name( String nm ) { prog = nm; return this; }
	
	private String who, dates;
	public  String copyright() { return prog +" (c) "+ who +", "+ dates; }
	public  Shell  copyright( String wh, String dts ) { who = wh; dates = dts; return this; }

	public Shell( String name ) {
		name( name ).prompt( "> " ).copyright( "Martin Wheatman", "2001-4, 2011-14" );
	}
	public Shell( String name, Strings args ) { this( name ); }
	public void interpret( InputStream fp ) { // reads file stream and "interpret()"s it
		if (fp==System.in) System.err.print( name() + prompt());
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader( new InputStreamReader( fp ));
			boolean was = aloud; // so we can reset volume between utterances.
			while ((line = br.readLine()) != null) {
				if (!line.equals("\n")) {
					// truncate comment -- only in real files
					int i = line.indexOf( '#' );
					if (-1 != i) line = line.substring( 0, i );
					// will return "cd .." as ["cd", ".", "."], not ["cd" ".."] -- "cd.." is meaningless!
					// need new stage of non-sentence sign processing
					Strings input = new Strings( line ); 
					if (input.size() > 0) {
						String rc = interpret( input );
						if (aloud)
							System.out.println( rc );
					}
					if (fp==System.in) System.err.print( name() + prompt());
				}
				aloud = was;	
			}
		} catch (java.io.IOException e ) {
			System.err.println( "IO error in Shell::interpret(stdin);" );
		} finally {
			try {
				br.close();
			} catch (java.io.IOException e ) { //ignore?
	}	}	}
	public void run() { interpret( System.in ); }
	//public void run( String[] args ) { interpret( args ); }
}