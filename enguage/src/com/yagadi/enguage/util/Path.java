package com.yagadi.enguage.util;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;


class Pent {
	String name, value; // value is managed in the application code
	boolean type;

	Pent( String nm, String val, boolean typ ) {
		name = nm;
		value = val; //newChars( value );
		type = typ;
	}
	public String  name()  { return name; }
	public boolean type()  { return type; }
	public String  value() { return value; }
	// ------
	static int pentCmp( Pent p1, Pent p2 ) { return p1.name().compareTo( p2.name()); }
	static int pentMax( Pent[] p ) {
		int max=0, tmp;
		for (int i=0, sz=p.length; i<sz; i++){ if ( max < (tmp = p[ i ].name().length())) max = tmp; }
		return max;
}	}

class PathShell extends Shell {
	PathShell( Strings args ) { super( "Path", args );}
	public String interpret( Strings utterance ) {
		String rc = Shell.SUCCESS;
		int sz = utterance.size();
		if (utterance.get( 0 ).equals( "cd" )) {
			Strings pathnames = utterance.copyAfter( 0 );
			if (!Path.testPath.cd( pathnames.toString( Strings.CONCAT )))
				rc = utterance.get( 1 ) +": not found" ;
		} else if ((sz == 1) && (0 == utterance.get( 0 ).compareTo( "up" ))) {
			if (!Path.testPath.cd( Path.PARENT ))
				rc = Path.PARENT +": not found" ;
		} else if ((sz == 1) && (0 == utterance.get( 0 ).compareTo( "pwd" )))
			rc = Path.testPath.pwd();
		else if (sz > 0)
			rc = "Unknown command: " + utterance.toString( Strings.SPACED );
		
		return rc;
}	}
// =======================
public class Path {
	//static private Audit audit = new Audit( "Path" );
	
	final static char   PATH_SEP_CHAR = File.separatorChar; 
	final static String PWD_PROPERTY = "user.dir"; 
	final static String PARENT = ".."; 
	final static String OPT_A = "-a"; 
	final static String OPT_X = "-x";
	
	public static String absolute( String pwd, String vfname ) {
		String savedName = vfname;
		if (!new File( vfname ).isAbsolute()) vfname = pwd + File.separator + vfname;
		try {
			vfname = new File( vfname ).getCanonicalPath();
		} catch( IOException e) {
			System.err.println( "getCanonicalPath("+savedName +" => "+ vfname +") failed:"+ e );
		}
		return vfname;
	}

	private   Strings names; // [ "", "home", "martin", "invoices", "invoice.0" ] or [ "..", "ruth" ]
	protected Strings names() { return names;}
	private   void     names( String s ){ names = new Strings( s, PATH_SEP_CHAR ); }
	private   void     names( Strings ss ){ names = ss; }

	public Path() { this( System.getProperty( PWD_PROPERTY )); }
	public Path( String pwd ) {
		pents = new ArrayList<Pent>();
		names( pwd == null ? System.getProperty( PWD_PROPERTY ) : pwd );
		//audit.debug( "Path set to "+ names.toString( Strings.CSV ));
	}

	public String  toString() { return pwd(); }
	public String       pwd() {
		return names.size() == 0 ? "/" : names.toString( Strings.PATH );
	}
	
	public void    pop() { if (0<names.size()) names.remove( names.size()-1 ); } // don't pop leading ""
	public void    push( String val ) { names.add( val ); }
	public void    up() { pop();}
	public boolean isEmpty() { return names == null || 0 == names.size(); } // need arrayEmpty()
	public boolean isAbsolute() { return 0<names.size() && names.get( 0 ).equals( "" ); }
	public boolean isRelative() { return 0<names.size() && !names.get( 0 ).equals( "" ); }

	//TODO:
	// cd -/fred => cd ../fred, cd ../../fred, cd ../../../fred etc,
	// cd +/fred => cd fred, cd */fred, cd */*/fred, cd */*/*/fred etc.
	//  so cd ../-/fred   => cd ../../fred ../../fred etc (but not ../fred)
	// and cd marv/+/fred => cd marv/*/fred marv/*/*/fred,  but NOT cd ruth/fred
	// TODO:
	// cd martin@ change to the target of that link, so if /home/martin@ -> /cygwin/p cd ~ => /cygwin/p
	// TODO:
	// cd ~ => cd `getenv( "HOME" )`
	public boolean cd( String dest ) {
		boolean rc = false;
		// add the dest onto a proposed pwd
		Path candidate = new Path( pwd() );
		Strings dests = new Strings( dest, PATH_SEP_CHAR );
		if (dests.size() > 0 && dests.get( 0 ).equals( "" )) // absolute
			candidate.names( dests );
		else
			for (int i = 0; i<dests.size(); ++i )
				if (dests.get( i ).equals( PARENT ))
					candidate.pop();
				else if (!dests.get( i ).equals( "." ))
					candidate.push( dests.get( i ));
		// see if this proposal exists
		File f = new File( candidate.pwd() );
		if (f.isFile())
			System.err.println( "cd: '"+ dest +"' not a directory!" );
		else if (!f.isDirectory())
			System.err.println( "cd: '"+ dest +"' not found!" );
		else // success
			try {
				names( f.getCanonicalPath() );
				rc = true;
			} catch( Exception e ) {
				System.err.println( "getCanonicalPath failed:"+ e.toString());
			}

		return rc;
	}
	// =======================
	private ArrayList<Pent> pents; // [ "0.address", "1.client", "3.id", "4.week.0", "4.week.1", ... ]
	private String filter( String s ) {
		String str = "";
		for (int i=0, sz=s.length();
				i<20
				&& i<sz
				&& (Character.isLetterOrDigit( s.charAt( i )) || ' ' == s.charAt( i ));
				i++)
			str += s.charAt( i );
		return str;
	}
	public void insert( String dname, String opts ) { // return list of Pents!!!
		File dirp = new File( dname );
		if (dirp.isDirectory()) {
			File[] flist = dirp.listFiles();
			for (int dp=0; dp<flist.length; dp++) if( 0 == opts.compareTo( OPT_A ) || '.' != flist[dp].getName().charAt( 0 )) {
				String value = "";
				if (0 == opts.compareTo( OPT_X )) {
					if (flist[dp].isFile()) // TTD: move this to String - newCharsFromFilePreview()
						value = filter( Filesystem.stringFromFile( flist[dp].getPath()));
					else if (Filesystem.isLink( flist[dp].getName() ))
						value = filter( Filesystem.stringFromLink( flist[dp].getName()));
					else if (flist[dp].isDirectory())
						value = "<"+ filter(flist[dp].getName()) +"/>";
				}
				Pent p = new Pent( flist[dp].getName(), value, flist[dp].isDirectory() );
				if (-1 == pents.indexOf( p ))
					pents.add( p ); // pents.arrayInsert( p, pentCmp );
	}	}	}
	public ArrayList<Pent> list() { return pents; }

	// =======================

	public static Path testPath = new Path();
	public static void main (String args []) {
	/* System.out.println( "path sep is "+ PATH_SEP_CHAR );
		Path p = new Path();
		System.out.println( "cwd is "+ p.toString());
		p.up();
		System.out.println( "cwd is "+ p.toString());
		*/
//		PathShell ps = new PathShell( args );
//		ps.run();
}	}