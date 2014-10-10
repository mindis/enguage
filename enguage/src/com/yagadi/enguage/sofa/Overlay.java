package com.yagadi.enguage.sofa;

import java.io.IOException;
import java.io.File;

import com.yagadi.enguage.util.*;

class OverlayShell extends Shell {
	OverlayShell( String[] args ) { super( "Overlay", args ); }
	public String interpret( String[] argv ) { return Overlay.interpret( argv ); }
}

/*
 * The analysis of this file is all off following the port to Android
 * Ospace contains several series, each series contains several overlays.
 * TODO: sort this out! v1.1?
 */
class Series { // relates to hypothetical attachment of series of overlays to fs 
	//static private Audit audit = new Audit( "Series" );

	static private final String basePointer = File.separator + "reuse";
	static public  final String DEFAULT = "iNeed"; //"sofa";
	
	static private String charsAndInt( String name, int vn ) { return name +"."+ vn; }
	static private String baseName( String nm ){ return charsAndInt( Overlay.root + File.separator + nm, 0 ) + basePointer; }
	
	static public String base( String nm ) {
		if (nm==null || nm.equals( Overlay.DETACHED )) return Overlay.DETACHED;
		String rc = Filesystem.stringFromLink( baseName( nm ));
		return rc == null ? "" : rc;
	}
	static public boolean existing( String nm ) {
		return null != nm && !nm.equals( Overlay.DETACHED ) && null != Filesystem.stringFromLink( baseName( nm ));
	}
	static public boolean create( String name, String whr ) {
		try {
			Filesystem.stringToLink( baseName( name ), new File( whr ).getCanonicalPath());
		} catch (IOException e) {
			System.err.println( "Series.create(): error in canonical path!" );
			return false;
		}
		return true;
	}
	static public boolean deleteSeries( String name ) {
		return !name.equals( Overlay.DETACHED ) && Filesystem.destroy( Series.baseName( name )); // won't work!
}	}
/* --- Q's:
 * could overlays be sparse (e.g. persons.0 persons.5? -- reuse link is in persons.0 could it be persons.<min>?
 */
//-- BEGIN Overlay
public class Overlay {
	static private Audit audit = new Audit( "Overlay" );
	
	public static final String DETACHED="";
	public static final String DEFAULT = "default";
	public static final String root = Filesystem.root + File.separator + "yagadi.com"; //+"sofa";
	
	final static String MODE_READ   = "r";
	final static String MODE_WRITE  = "w";
	final static String MODE_APPEND = "a";
	final static String MODE_DELETE = "d";
	final static String MODE_RENAME = "m";
	final static String RENAME_CH   = "^";
	final static String DELETE_CH   = "!";
	final static String OPT_X       ="-x";
	
	private Path p;
	String  path() { return p.toString(); }
	boolean path( String dest ) {
		boolean rc = true;
		if (null != dest) {
			String src = p.pwd();    // remember where we are
			p.cd( dest );              // do the cd
			rc = Filesystem.existsEntity( Overlay.fsname( p.pwd(), "r" )); // check destination
			if (!rc) p = new Path( src ); // return to where we were
		}
		return rc;
	}

	private int  number = 0; // series.0, series.1, ..., series.n => 1+n
	private int  number() { return number; } // initialised to 1, for 0th overlay
	private int  highest = 0; // series.0, series.1, ..., series.n => 1+n
	//private int  highest() { return highest; } // initialised to 1, for 0th overlay
	
	private boolean count( String series ) {
		//audit.traceIn( "count", "'"+series+"'" );
		highest = -1;
		number = 0;
		String overlays[] = new File( root ).list();
		if (overlays != null) for (String file : overlays)
			if (   file.length() > series.length()+1
			    && (series + ".").equals( file.substring( 0, series.length()+1 )) )
			{
				number++;
				int n = Integer.parseInt( file.substring( series.length() + 1));
				if ( n>highest ) highest = n;
			}
		//audit.traceOut( number +" overlay(s), highest="+ highest );
		return highest > -1;
	}
	
	private String name;
	String series() { return name; }
	void   series( String nm ) { name = nm; }
	private static String error = "";
	public  static String error() { return error; }
	 
	private static boolean verbose = false;
	
	public Overlay() {
		name = DETACHED; // start detach()ed;
		p = new Path( System.getProperty( "user.dir" ));
		audit.debug( "initial path is "+ System.getProperty( "user.dir" ) +" (p="+ p.toString() +")" );
		new File( Overlay.root ).mkdir(); // JIC -- ignore result
		Series.create( Series.DEFAULT, p.toString() );
	}
	public Overlay( boolean v ) {
		this();
		verbose = v;
	}

	private static String charsAndInt( String s, int n ) { return s +"."+ n; }
	
	public String version( int vn ) { return charsAndInt( series(), vn ); }
	public String toString() { return "[ "+ root +", "+ series() +"("+ number() +") ]"; }
	
	public boolean attached() { return !series().equals( DETACHED ); }
	public boolean attach( String series ) {
		series( series );
		count( series ); // we are attached - it may not exist!
		return !series().equals( DETACHED );
	}
	public void detach() { series( DETACHED ); }
	
	public void createOverlay() {
		//audit.traceIn( "createOverlay", "" );
		if (!series().equals( DETACHED )) {
			new File( root ).mkdir(); // JIC!
			highest++;
			File overlay = new File( charsAndInt( root + File.separator + series(), highest )); 
			if (!overlay.mkdir()) {
				highest--;
				//audit.traceOut( "unable to create "+ overlay.toString());
			} else {
				number++;
				//audit.traceOut( "created, overlay "+ highest +", count ="+ number );
			}
		} //else
			//audit.traceOut( "not created -- not attached" );
	}
	private static void moveFile( File src, File dest ) {
		//audit.traceIn( "moveFile", "moving folder: "+ src.toString() +" to "+ dest.toString());
		/* only called from reducto()
		 * so we're not propagating !files
		 */
    	if (src.isDirectory()) {
    		/////audit.audit( "moving folder: "+ src.toString() +" to "+ dest.toString());
    		if (!dest.exists()) dest.mkdir();
    		//list all the directory contents
    		String files[] = src.list();
    		//audit.debug( "moving src of number:"+ src.listFiles().length );
    		for (String file : files) {
	    		//construct the src and dest file structure
	    		File srcFile = new File(src, file);
	    		File destFile = new File(dest, file);
	    		//recursive copy
	    		moveFile( srcFile, destFile );
    		}
    		//audit.debug( "deleting "+src.toString()+" of number:"+ src.listFiles().length );
    		if (!src.delete()) audit.ERROR( "folder move DELETE failed" );
    	} else {
    		/* was...
    		 *   if (dest.exists()) dest.delete();
             *	 src.renameTo( dest );
             */
    		// Of source files, if...
    		if (!Entity.isDeleteName( src.toString()) //  ...we have filename...
    				&& new File( Entity.deleteName( src.toString() )).exists()) // ...and !filename exists
    		{	//// remove !filename
        		//audit.debug( "a-delete !filename "+ Entity.deleteName( src.toString() ));
    			new File( Entity.deleteName( src.toString() )).delete();
    			//// move file across as before
        		//audit.debug( "a-move file across "+ src.toString() );
        		if (dest.exists()) dest.delete();
        		src.renameTo( dest );
    		} else if ( Entity.isDeleteName( src.toString()) // ...we have !filename
    				&& new File( Entity.nonDeleteName( src.toString() )).exists())  // ...and filename exists
    		{	//// just remove !filename (filename will be dealt with when we get there!)
        		src.delete();
    			if (src.exists()) audit.ERROR( "b) deleting !filename, with filename ("+ src.toString() +") FAILED" );
    		} else if (!Entity.isDeleteName( src.toString()) // we have filename..
    				&& !new File( Entity.deleteName( src.toString() )).exists()) // but not !filename
    		{	//// move file across - as before
        		//audit.debug( "c-move file across: "+ src.toString() +" to "+ dest.toString());
        		if (dest.exists()) if (!dest.delete()) audit.ERROR( "c) DELETE failed: "+ dest.toString() );
        		if (!src.renameTo( dest ))  audit.ERROR( "c) RENAME of "+ src.toString() +" to "+ dest.toString() +" failed" );
    		} else if (Entity.isDeleteName( src.toString()) // we have !filename 
    				&& new File( Entity.nonDeleteName( src.toString() )).exists()) // but not filename
    		{	//// remove !filename...
        		//audit.debug( "d-remove !filename... "+ src.toString() );
    			if (!src.delete()) audit.ERROR( "deleting !fname ("+ src.toString() +") failed");
    			//// and delete underlying file
        		//audit.debug( "d-...and delete underlying file "+ dest.toString());
    			new File( Entity.nonDeleteName( dest.toString() )).delete();
    	}	}
    	//audit.traceOut();
   	}
	public boolean combineUnderlays() {
		//audit.traceIn( "combineUnderlays", "combining "+ (number()-1) +" underlays" );
		/*
		 * For expediency's sake, this function combines all underlaid  (i.e. protected) overlays
		 */
		boolean rc = false;
		int topOverlay = highest;
		if (!series().equals( DETACHED ) && topOverlay > 1) {
			new File( root ).mkdir(); // JIC!
			File src, dst;
			for (int overlay=topOverlay-1; overlay>0; overlay--) {
				src = new File( charsAndInt( root + File.separator + series(), overlay ));
				if (src.exists()) {
					dst = new File( charsAndInt( root + File.separator + series(), overlay-1 ));
					if (dst.exists()) {
						// move all new files into old overlay
						//audit.debug( "moving overlay "+ src.toString() +" to "+ dst.toString());
		    			moveFile( src, dst );
		    			if (src.exists()) audit.ERROR( src.toString() +" still exists - after MOVE ;(" );
					} else {
						//audit.debug( "renaming overlay "+ src.toString() +" to "+ dst.toString());
						src.renameTo( dst );
		    			if (!dst.exists()) audit.ERROR( src.toString() +" still doesn't exists - after RENAME ;(" );
					}
	    			count( series() );
	    			//audit.debug( "number of overlays is now: "+ number );
			}	}
			// then _rename_ top overlay - will now be ".1"
			//audit.debug("Now rename top overlay "+ topOverlay +" to be 1" );
			src = new File( charsAndInt( root + File.separator + series(), topOverlay ));
			dst = new File( charsAndInt( root + File.separator + series(), 1 ));
			if (dst.exists()) dst.delete();
			if (!src.renameTo( dst )) audit.ERROR( "RENAME "+ src.toString()+" to "+ dst.toString() +" FAILED" );
			if (src.exists()) audit.debug( src.toString() +" still exists - after RENAME ;(" );
			// adjust count()
			count( series() );
			//audit.debug( "count really is: "+ number );
			rc = true;
		}
		//audit.traceOut( "combine "+ (rc?"done":"failed") +", count="+ number +", highest="+ highest );
		return rc;
	}
	public boolean destroyTop() {
		audit.traceIn( "destroyOverlay", "" );
		boolean rc = false;
		int topOverlay = highest;
		if (topOverlay > 0 && !series().equals( "" )) {
			new File( root ).mkdir();
			String nm = charsAndInt( root + File.separator + series(), topOverlay );
			/////audit.debug( "Destroying "+ nm );
			rc = Filesystem.destroy( nm );
			if (rc) count( series() );
		}
		audit.traceOut( rc );
		return rc;
	}
// --
// --
//	public static String rename( String NAME ) { 
//		File f = new File( NAME );
//		return f.getParent() +"/"+ f.getName() +"^";
//	}
	private String nthCandidate( String nm, int vn ) {
		return (0 > vn) ? nm : 
				(root + File.separator + version( vn ) +"/"+ nm.substring( Series.base( series() ).length() /*+1*/ )); // +1 remove leading "/"
	}
	private String topCandidate( String name ) { return nthCandidate( name, number()-1); }
	private String delCandidate( String name, int n ) {
		File f = new File( nthCandidate( name, n ));
		return f.getParent() +"/!"+ f.getName(); 
	}
	private String find( String vfname ) {
		//audit.traceIn( "find", "find: "+ vfname );
		String fsname = null;
		int vn = number(); // number=3 ==> series.0,1,2, so initially decr vn
		boolean done = false;
		while (!done && --vn >= 0) {
			fsname = nthCandidate( vfname, vn );
			done = Filesystem.exists( fsname ); // first time around this will be topCandidate()
			if (!done) { // look for a delete marker
				if (done = Filesystem.exists( delCandidate( vfname, vn ) )) {
					fsname = topCandidate( vfname ); // look no further - return top (non-existing) file
				} else { // look for rename marker -- is this the right order: file - delete - rename?
//					fsname = o.renameCandidate( vfname, vn );
//					done = new File(  fsname  ).isFile() || new File(  fsname  ).isDirectory();
		}	}	}
//		if (!done) // orig file or non-existant file!
//			fsname = Filesystem.exists( vfname ) ? vfname : topCandidate( vfname );
		//return audit.traceOut( fsname );
		return fsname;
	}
	// return true for path=/home/martin/persons/fred/waz/ere
	// where series = persons
	//   and /home/martin/sofa/persons.0/reuse -> /home/martin/persons
	boolean isOverlaid( String vfname ) {
		return null != Series.base( series() )
		&& vfname != null // sanity
		&& 0 != Series.base( series() ).length() && 0 != vfname.length() // so base == "x*", NAME == "x*"
		&& Series.base( series() ).length() < vfname.length()
		&& vfname.substring( 0, Series.base( series() ).length()).equals( Series.base( series() )); // NAME="xyz/abc" base="xyz" => true
	}
	// maps a virtual filename onto a "real" filesystem NAME.
	// if write - top overlay NAME is returned, simple.
	// if read  - overlay space is searched for an existing file.
	//          - if not found, or if the file has been deleted, the (unexisting) write filename is returned
	//          - if rename found (e.g. old^new), change return the old NAME. 
	public static String fsname( String vfname, String modeChs ) {
		String fsname = vfname; // pass through!
		Overlay o = Get();
		if (o != null && vfname != null) {
			//-- String vfname = rationalisePath( absolutePath( o.p.toString(), new String( fsname ) ));
			vfname = Path.absolute( o.path(), vfname );
			if (o.isOverlaid( vfname )) {
				if (modeChs.equals( MODE_READ )) {
					fsname = o.find( vfname );
					
				} else if (modeChs.equals( MODE_WRITE ) || modeChs.equals( MODE_APPEND )) {
					fsname = o.topCandidate( vfname );
					
				} else if (modeChs.equals( MODE_DELETE )) {
					fsname = o.delCandidate( vfname, o.number());
		}	}	}
		return fsname;
	}

// list/Read/Close() se also  pents in Path.c
// similar to opendir, readdir and closedir:
// should return ???:
// /home/martin/sofa/series.1/one/two/one.1
// /home/martin/sofa/series.0/one/two/one.1
// /home/martin/sofa/series.0/one/two/one.0
// or just...
// /home/martin/sofa/series.1/one/two/one.1
// /home/martin/sofa/series.0/one/two/one.0
// or even (b):
// /home/martin/one/two/one.0
// /home/martin/one/two/one.1
// or (a)
// one.0
// one.1
// needs to support n.name where n = [0..n] and my be on lower overlay (though generally not) and NAME is unknown.
// last will suffice, but would be useful if others could be specified...
// a) list( "." ); b) list( "/home/martin/one/two" ); --> pass to Overlay.fsname() for real filename?
// currently just needed for "ls" and attribute ordering so one.1 sufficient

/* this code is interesting but isn't used, yet, by entity model -- multi-valued attributes!!! a shopping list?!?
 * **************************************************************************************** *
 * **************************************************************************************** *
 * **************************************************************************************** *
   List should add files until a delete file found, then remove the file and add the delete.
   If file is refound, re add the file and start again
   The list will then contain files and delete files - just don't list the dlete files!
   So add from bottom up!
    
	private String newSubDir( String dname ) {
		String rc = null;
		String b = Series.base( series()); // =/home/martin/persons/ -- trailing '/'
		if (null != b) {
			int len = b.length();
			if (len > 0 && len < dname.length() && 0 == dname.substring( 0, len-1 ).equals( b )) {
				rc = dname.substring( len );
System.out.println( "newSubDir(): rc="+ rc );
		}	}
		return rc;
	}
	ArrayList<Pent> list( String dname ) {
		if (0 == dname.equals( "." ))
			dname = path();
//System.out.println("list(): "+ dname );
		p.pathListDelete();
		//String absName = rationalisePath( absolutePath( pathAsChars( p ), strdup( dname ? dname : "." ) ));
		 //-- String absName = rationalisePath( absolutePath( o.p.toString(), new String( uname ) ));
			String absName = dname;
			
//System.out.println("list(): inserting "+ absName );
		p.pathListInsert( absName, OPT_X );
		String subDir = newSubDir( absName );
		if (null != subDir) {
//System.out.println( "list: also subDir="+ subDir );
			int n = count();
			// remove root + series + n from dname...
//System.out.println( "list: sz="+ n );
			String prefix = root + "/" + series() + ".";
//System.out.println( "list: " );
			while (0 != n--) {
//System.out.println( "list: instering"+ prefix + n );
				p.pathListInsert( prefix + n, OPT_X );
		}	}
		return p.list();
	}
	//pent overlayRead( Overlay o ) { return pathListRead( ((overlay)o)->p ); }
	//void overlayClose( Overlay o ) { pathListDelete( ((overlay)o)->p ); }
 ***************************************************************************************** *
 ***************************************************************************************** *
 ***************************************************************************************** */
	// -- Overlay END
	
	static public String autoAttach() {
		String error = "";
		Overlay o = Get();
		String candidate = new File( System.getProperty("user.dir")).getName();
		if (verbose) System.out.println( "autoAttach(): candidate is "+ candidate );
		if (candidate.equals( "" )) candidate = /*File.separator +*/ Series.DEFAULT;
		if (!o.attach( candidate )) {
			error = "Unable to attach to "+ candidate;
			candidate = Overlay.DEFAULT;
			if (!Series.existing( candidate ))
				Series.create( candidate, new File( System.getProperty("user.dir")).getPath() );
			if (!o.attach( candidate ))
				error = "Unable to attach to "+ candidate;
			else
				error = "";
		}
		if (verbose && error.equals( "" )) System.out.println( "Attached to "+ candidate );
		return error;
	}

	static private Overlay singletonO = null;
	static public Overlay Get() {
		if (null==singletonO) singletonO = new Overlay();
		return singletonO ;
	}
	static public void Set( Overlay o ) { singletonO = o; }

	static public String interpret( String[] argv ) {
		String rc = Shell.FAIL;
		int argc = argv.length;
		Overlay o = Overlay.Get();
		
		if (argv[ 0 ].equals("attach") && (2 >= argc)) {
			if (2 == argc) {
				if (o.attach( argv[ 1 ]))
					System.err.println( "No such series "+ argv[ 1 ]);
				else
					rc = Shell.SUCCESS;
			} else if ( o.attached())
				System.err.println( "Not attached" );
			else
				rc = Shell.SUCCESS;
				
		} else if (argv[ 0 ].equals("detach") && (1 == argc)) {
			o.detach();
			rc = Shell.SUCCESS;
			
		} else if ((argv[ 0 ].equals( "save" ) || argv[ 0 ].equals( "create" )) && (1 == argc)) {
			//System.out.println( "Creating "+ o.series());
			rc = Shell.SUCCESS;
			o.createOverlay();
			
		//} else if (0 == argv[ 0 ].equals( "exists" ) && (2 == argc)) {
		//	rc =  o.existingSeries( argv[ 1 ]) ? "Yes":"No";
			
		} else if (argv[ 0 ].equals( "create" ) && ((2 == argc) || (3 == argc)) ) {
			if (!Series.create( argv[ 1 ], argc == 3 ? argv[ 2 ]:System.getProperty("user.dir") ))
				System.err.println( argv[ 1 ] + " already exists" );
			else
				rc = Shell.SUCCESS;
				
		} else if (argv[ 0 ].equals( "delete" ) && (2 == argc) ) {
			if (Series.deleteSeries( argv[ 1 ]))
				rc = Shell.SUCCESS;
			else
				System.err.println( argv[ 1 ] + " doesn't exists" );
			
		} else if (argv[ 0 ].equals(  "destroy"  ) && (1 == argc) ) {
			rc = o.destroyTop() ? Shell.SUCCESS : Shell.FAIL;
			
		} else if ((   argv[ 0 ].equals(    "bond"  )
				    || argv[ 0 ].equals( "combine"  ))
		           && (1 == argc) ) {
			rc = o.combineUnderlays() ? Shell.SUCCESS : Shell.FAIL;
			
		//} else if (0 == argv[ 0 ].equals( "rm" ) && (2 == argc) ) {
		//	if (!entityIgnore( argv[ 1 ])) rc =  argv[ 1 ] +" doesn't exists" );
		
		} else if ((1 == argc) && argv[ 0 ].equals( "up" )) {
			if ( o.path( ".." ))
				rc = Shell.SUCCESS;
			else
				System.err.println( argv[ 0 ] +": Cannot cd to .." );
				
		} else if ((2 == argc) && argv[ 0 ].equals( "cd" )) {
			if ( o.path( argv[ 1 ]))
				rc = Shell.SUCCESS;
			else
				System.err.println( argv[ 0 ] +": Cannot cd to "+ argv[ 1 ]);
		
		} else if (argv[ 0 ].equals( "pwd" ) && (1 == argc)) {
			rc = Shell.SUCCESS;
			System.err.println( o.path());
			
		} else if (argv[ 0 ].equals( "write"  ) && (2 == argc)) {
			rc = Shell.SUCCESS;
			System.out.println( "New file would be '"+ Overlay.fsname( argv[ 1 ], "w" ) +"'" ); // last param ignored
			
		} else if (argv[ 0 ].equals( "mkdir" ) && (2 == argc)) {
			rc = Shell.SUCCESS;
			System.out.println( ">>>mkdirs("+Overlay.fsname( argv[ 1 ], "w" )+") => "+ (new File( Overlay.fsname( argv[ 1 ], "w" )).mkdirs()?"Ok":"Error"));
			
		} else if (argv[ 0 ].equals( "read" ) && (2 == argc)) {
			rc = Shell.SUCCESS;
			System.out.println( "File found? is '"+ Overlay.fsname( argv[ 1 ], "r" )+"'" );
		/*
		} else if (0 == argv[ 0 ].equals( "ls" )) {
			ArrayList<Pent> pents = o.list( argv.length > 1 && null != argv[ 1 ]?argv[ 1 ]:"." );
			rc = "";
			for (int i=0, sz=pents.size(); i<sz; i++)
				rc += ((i>0 ? "\n": "") + pents.get( i ).name());
				
		} else if (0 == argv[ 0 ].equals( "lx" )) {
			ArrayList<Pent> pents = o.list( argv.length > 1 && null != argv[ 1 ]?argv[ 1 ]:"." );
			for (int i=0, sz = pents.size(); i<sz; ++i)
				if (pents.get( i ).type())
					rc += ((i>0 ? "\n": "") +"<"+ pents.get( i ).name()+"/>");
				else
					rc += ( (i>0 ? "\n": "") + pents.get( i ).name() +"='"+ pents.get( i ).value() +"'");
		*/
		} else
			System.err.println( "Usage: attach <series>\n"
			                 +"     : detach\n"
			                 +"     : save\n"
		//	                 +"     : show [<n>] <pathname>\n"
			                 +"     : write <pathname>\n"
			                 +"     : read [<n>] <pathname>\n"
			                 +"     : pwd <pathname>\n"
			                 +"     : cd <pathname>\n"
		//	                 +"     : ls <pathname>\n"
			                 +"given: "+ Strings.toString( argv, Strings.CSV ));
		return rc;
	}


	
	public static void main (String args []) {
		Overlay o = Get();
		Overlay.Set( o );
		String rc = autoAttach();
		if (!rc.equals( "" ))
			System.out.println( "Ouch!" + rc );
		else {
			System.out.println( "NAME=, n=" + o.number() );
			OverlayShell os = new OverlayShell( args );
			os.run();
}	}	}

// ================================


/*class renamer implements FileFilter {
	static String newname;
	renamer( String s ) { newname = s; }
	public boolean accept( File f ) {
		int i = f.getName().indexOf( Overlay.RENAME_CH );
		if (i == -1)
			return false;
		else if (0 == f.getName().substring( i+1 ).equals(newname))  // f.getName() => "old^new"  == newname => "new" ???
			return true;
		else
			return false;
}	}
*/
