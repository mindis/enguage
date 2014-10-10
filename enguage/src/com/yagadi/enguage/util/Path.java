package com.yagadi.enguage.util;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import com.yagadi.enguage.util.Strings;

class PathShell extends Shell {
	PathShell( String[] args ) { super( "Path", args );}
	public String interpret( String[] utterance ) {
		String rc = Shell.SUCCESS;
		int sz = utterance.length;
		if (utterance[ 0 ].equals( "cd" )) {
			String[] pathnames = Strings.copyAfter( utterance, 0 );
			if (!Path.testPath.cd( Strings.toString( pathnames, Strings.CONCAT )))
				rc = utterance[ 1 ] +": not found" ;
		} else if ((sz == 1) && (0 == utterance[ 0 ].compareTo( "up" ))) {
			if (!Path.testPath.cd( Path.PARENT ))
				rc = Path.PARENT +": not found" ;
		} else if ((sz == 1) && (0 == utterance[ 0 ].compareTo( "pwd" )))
			rc = Path.testPath.pwd();
		else if (sz > 0)
			rc = "Unknown command: " + Strings.toString( utterance, Strings.SPACED );
		
		return rc;
}	}
// =======================
public class Path {
	static private Audit audit = new Audit( "Path" );
	
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

	private   String[] names; // [ "", "home", "martin", "invoices", "invoice.0" ] or [ "..", "ruth" ]
	protected String[] names() { return names;}
	private   void     names( String s ){ names = Strings.fromString( s, PATH_SEP_CHAR ); }
	private   void     names( String[] ss ){ names = ss; }

	public Path() { this( System.getProperty( PWD_PROPERTY )); }
	public Path( String pwd ) {
		pents = new ArrayList<Pent>();
		names( pwd == null ? System.getProperty( PWD_PROPERTY ) : pwd );
		audit.debug( "Path set to "+ Strings.toString( names, Strings.CSV ));
	}

	public String  toString() { return pwd(); }
	public String       pwd() {
		return names.length == 0 ? "/" : Strings.toString( names, Strings.PATH );
	}
	
	public void    pop() { if (0<names.length) names = Strings.removeAt( names, names.length-1 ); } // don't pop leading ""
	public void    push( String val ) { names = Strings.append( names, val ); }
	public void    up() { pop();}
	public boolean isEmpty() { return names == null || 0 == names.length; } // need arrayEmpty()
	public boolean isAbsolute() { return 0<names.length && names[ 0 ].equals( "" ); }
	public boolean isRelative() { return 0<names.length && !names[ 0 ].equals( "" ); }

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
		String[] dests = Strings.fromString( dest, PATH_SEP_CHAR );
		if (dests.length > 0 && dests[ 0 ].equals( "" )) // absolute
			candidate.names( dests );
		else
			for (int i = 0; i<dests.length; ++i )
				if (dests[ i ].equals( PARENT ))
					candidate.pop();
				else if (!dests[ i ].equals( "." ))
					candidate.push( dests[ i ]);
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
		if (null != s)
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
		PathShell ps = new PathShell( args );
		ps.run();
}	}