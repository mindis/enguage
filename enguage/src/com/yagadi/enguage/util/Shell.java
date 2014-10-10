package com.yagadi.enguage.util;

import java.io.*;

import com.yagadi.enguage.util.Strings;

abstract public class Shell {

	public static final String SUCCESS = "TRUE";	
	public static final String FAIL    = "FALSE";
		
	abstract public String interpret( String[] argv ) ;
	
	//static protected String[] args = null;
	//static private   void     args( String[] a ) { args = a; }
	//static public   String[]  args() { return args; 	}
	
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
		// will inherit args -- need to check if this is used first, no args?
		name( name ).prompt( "> " ).copyright( "Martin Wheatman", "2001-4, 2011-14" );
		//System.err.println( copyright() );
	}
	public Shell( String name, String args[] ) {
		this( name );
		//args( args ); // these are common to all spawned shells
	}
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
					String input[] = Strings.fromString( line ); 
					if (input.length > 0) {
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
	public void run( String[] args ) { interpret( args ); }
}