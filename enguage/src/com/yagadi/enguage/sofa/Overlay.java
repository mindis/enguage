package com.yagadi.enguage.sofa;

import java.io.File;
import java.io.IOException;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;
import com.yagadi.enguage.util.Path;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

class OverlayShell extends Shell {
	OverlayShell( Strings args ) { super( "Overlay", args ); }
	public String interpret( Strings argv ) { return Overlay.interpret( argv ); }
}

/* The analysis of this file is all off following the port to Android
 * OverlaySpace contains several series, each series contains several overlays.
 * TODO: sort this out! v1.1?
 */
class OverlaySpace {
	public static final String root = Filesystem.root + File.separator + "yagadi.com"; //+"sofa";
	public static String charsAndInt( String s, int n ) { return s +"."+ n; }
	
	//static Series s = null;
	//attach
}
class Series { // relates to hypothetical attachment of series of overlays to fs 
	static private Audit audit = new Audit( "Series" );
	private static boolean debug = false ; //Enguage.runtimeDebugging || Enguage.startupDebugging;

	static public  final String DETACHED="";
	static private final String basePointer = File.separator + "reuse";
	static public  final String DEFAULT = "iNeed"; //"sofa";
	
	static private String baseName( String nm ){ return name( nm, 0 ) + basePointer; }
	
	static public String base( String nm ) {
		if (nm==null || nm.equals( DETACHED )) return DETACHED;
		//audit.debug("Series.base(): Looking for:"+baseName( nm ));
		return Filesystem.stringFromLink( baseName( nm ));
	}
	static public boolean existing( String nm ) {
		return null != nm && !nm.equals( DETACHED ) && Filesystem.exists( baseName( nm ));
	}
	static public boolean create( String name, String whr ) {
		//audit.traceIn( "create", "name="+name+", whence="+whr );
		boolean rc = true;
		try {
			Filesystem.stringToLink( baseName( name ), new File( whr ).getCanonicalPath());
		} catch (IOException e) {
			audit.ERROR( "Series.create(): error in canonical path!" );
			rc = false;
		}
		//return audit.traceOut( rc );
		return rc;
	}
	static private String name = DETACHED;
	static public  void   name( String nm ) { name = nm; }
	static public  String name() { return name; }
	static public  String name( int vn ) { return OverlaySpace.charsAndInt( OverlaySpace.root + File.separator + name, vn ); }
	static public  String name( String nm, int vn ) { return OverlaySpace.charsAndInt( OverlaySpace.root + File.separator + nm, vn ); }
	
	static private int  number = 0; // series.0, series.1, ..., series.n => 1+n
	static public  int  number() { return number; } // initialised to 1, for 0th overlay
	
	static private int  highest = -1; // series.0, series.1, ..., series.n => 1+n
	static public  int  highest() { return highest; } // initialised to 1, for 0th overlay
	
	static public boolean count() {
		//audit.traceIn( "count", "" );
		highest = -1;
		number = 0;
		String overlays[] = new File( OverlaySpace.root ).list();
		if (overlays != null) for (String file : overlays) {
			//audit.debug( "counting:"+ file );
			if (   file.length() > name.length()+1
			    && (name + ".").equals( file.substring( 0, name.length()+1 )) )
			{
				number++;
				int n = Integer.parseInt( file.substring( name.length() + 1));
				if ( n>highest ) highest = n;
				//audit.debug( "   counted: "+ number +" overlay(s), highest="+ highest );
		}	}
		//return audit.traceOut( highest > -1 );
		return highest > -1;
	}
	static public boolean attached() { return !name.equals( DETACHED ); }
	static public boolean attach( String series ) {
		name( series );
		count(); // we are attached - it may not exist!
		return Series.attached();
	}
	static public void detach() {
		name( DETACHED );
		highest = -1;
		number = 0;
	}
	static public void append() {
		//audit.traceIn( "append", "" );
		highest++;
		File overlay = new File( name( highest )); 
		if (!overlay.mkdir()) {
			highest--;
			audit.ERROR( "unable to create "+ overlay.toString());
		} else {
			number++;
			//audit.debug( "created, overlay "+ highest +", count ="+ number );
		}
		//audit.traceOut();
	}
	static public boolean compact( /* int targetNumber */) {
		if (debug) audit.traceIn( "compact", "combining "+ (number()-1) +" underlays" );
		/*
		 * For expediency's sake, this function combines all underlaid  (i.e. protected) overlays
		 */
		boolean rc = false;
		//audit.debug( "top overlay is "+ highest );
		if (attached() && highest > 1) {
			File src, dst;
			for (int overlay=highest()-1; overlay>0; overlay--) {
				//audit.debug( "COMBINING "+ overlay +" with "+ (overlay-1) );
				src = new File( name( overlay ));
				if (src.exists()) {
					dst = new File( name( overlay-1 ));
					if (dst.exists()) {
						// move all new files into old overlay
						//audit.debug( "dst exist: moving overlay "+ src.toString() +" to "+ dst.toString());
		    			Filesystem.moveFile( src, dst );
		    			if (src.exists()) audit.ERROR( src.toString() +" still exists - after MOVE ;(" );
					} else {
						//audit.debug( "dst not existing: renaming overlay "+ src.toString() +" to "+ dst.toString());
						src.renameTo( dst );
		    			if (!dst.exists()) audit.ERROR( src.toString() +" still doesn't exists - after RENAME ;(" );
					}
					count();
	    			//audit.debug( "number of overlays is now: "+ number() );
				} //else
					//audit.debug( "doing nothing: overlay "+ src.toString() +" does not exists" );
			}
			// then _rename_ top overlay - will now be ".1"
			//audit.debug("Now renaming top overlay "+ highest() +" to be 1" );
			src = new File( name( highest() ));
			dst = new File( name( 1 ));
			if (dst.exists()) dst.delete();
			if (!src.renameTo( dst )) audit.ERROR( "RENAME "+ src.toString()+" to "+ dst.toString() +" FAILED" );
			//if (src.exists()) audit.debug( src.toString() +" still exists - after RENAME ;(" );
			// adjust count()
			count();
			//audit.debug( "count really is: "+ number() );
			rc = true;
		}
		if (debug) audit.traceOut( "combine "+ (rc?"done":"failed") +", count="+ number() +", highest="+ highest() );
		return rc;
}	}
/* --- Q's:
 * could overlays be sparse (e.g. persons.0 persons.5? -- reuse link is in persons.0 could it be persons.<min>?
 */
//-- BEGIN Overlay
public class Overlay {
	static private Audit audit = new Audit( "Overlay" );
	private static boolean debug = false; //Enguage.runtimeDebugging || Enguage.startupDebugging;
	
	public static final String DEFAULT = "default";
	
	final static String MODE_READ   = "r";
	final static String MODE_WRITE  = "w";
	final static String MODE_APPEND = "a";
	final static String MODE_DELETE = "d";
	final static String MODE_RENAME = "m";
	final static String RENAME_CH   = "^";
	final static String DELETE_CH   = "!";
	final static String OPT_X       ="-x";
	
	private Path p;
	private String  path() { return p.toString(); }
	private boolean path( String dest ) {
		boolean rc = true;
		if (null != dest) {
			String src = p.pwd();    // remember where we are
			p.cd( dest );            // do the cd
			// check destination - might only exist in object space
			rc = Filesystem.existsEntity( Overlay.fsname( p.pwd(), "r" ));
			if (!rc) p = new Path( src ); // return to where we were
		}
		return rc;
	}

	private static String error = "";
	public  static String error() { return error; }
	 
	public Overlay() {
		Series.detach(); // start detach()ed;
		p = new Path( System.getProperty( "user.dir" ));
		//audit.debug( "initial path is "+ System.getProperty( "user.dir" ) +" (p="+ p.toString() +")" );
		new File( OverlaySpace.root ).mkdir(); // JIC -- ignore result
		/*
		 * For VERSION 2 - was plain Series.DEFAULT...
		 * If we put in the following it should result in /yagadi.com being overlaid
		 * with base overlay being /yagadi.com/yagadi.com.0  
		 * This will make it incompatible with version 1 ( as its series is iNeed ) 
		Series.create( new File( System.getProperty("user.dir")).getName(), p.toString() );
		 */
		Series.create( Series.DEFAULT, p.toString() );
	}

	public int count() { return Series.number(); }
	public String toString() { return "[ "+ OverlaySpace.root +", "+ Series.name() +"("+ Series.number() +") ]"; }
	
	// called from Enguage...
	public boolean attached() { return Series.attached(); }
	//static public boolean attach( String series ) { return Series.attach( series ); }
	//static public void detach() { Series.detach(); }
	
	public void create() {
		if (debug) audit.traceIn( "createOverlay", "" );
		if (Series.attached())
			Series.append();
		else
			audit.debug( "not created -- not attached" );
		if (debug) audit.traceOut();
	}
	public boolean destroy() {
		// removes top overlay of current series
		if (debug) audit.traceIn( "destroy", "" );
		boolean rc = false;
		int topOverlay = Series.highest();
		if (topOverlay > 0 && !Series.name().equals( "" )) {
			String nm = Series.name( topOverlay );
			//audit.debug( "Destroying "+ nm );
			rc = Filesystem.destroy( nm );
			if (rc) Series.count();
		}
		if (debug) audit.traceOut( rc );
		return rc;
	}
	private String nthCandidate( String nm, int vn ) {
		//audit.traceIn( "nthCandidate", "nm='"+ nm +"', vn="+ vn);
		// nthCandidate( "/home/martin/src/myfile.c", 27 ) => "/var/overlays/series.27/myfile.c"
		if (vn > Series.highest()) { vn = Series.highest(); audit.ERROR("nthCandidate called with too high a value"); }
		return	//audit.traceOut(
				(0 > vn) ? nm : 
				(Series.name( vn ) + File.separator
						+ nm.substring( Series.base( Series.name() ).length() /*+1*/ ))
				//)
						; // +1 remove leading "/"
	}
	private String topCandidate( String name ) {
		return nthCandidate( name, Series.number()-1);
	}
	private String delCandidate( String name, int n ) {
		File f = new File( nthCandidate( name, n ));
		return f.getParent() +"/!"+ f.getName(); 
	}
	private String find( String vfname ) {
		//audit.traceIn( "find", "find: "+ vfname );
		String fsname = null;
		int vn = Series.number(); // number=3 ==> series.0,1,2, so initially decr vn
		boolean done = false;
		while (!done && --vn >= 0) {
			fsname = nthCandidate( vfname, vn );
			done = Filesystem.exists( fsname ); // first time around this will be topCandidate()
			if (!done) { // look for a delete marker
				if (done = Filesystem.exists( delCandidate( vfname, vn ) )) {
					fsname = topCandidate( vfname ); // look no further - return top (non-existing) file
				} else { // look for rename marker -- is this the right order: file - delete - rename?
//					fsname = o.renameCandidate( vfname, vn );
//					done = new File( fsname ).isFile() || new File(  fsname  ).isDirectory();
		}	}	}
//		if (!done) // orig file or non-existant file!
//			fsname = Filesystem.exists( vfname ) ? vfname : topCandidate( vfname );
		//return audit.traceOut( fsname );
		return fsname;
	}
	// return true for path=/home/martin/persons/fred/waz/ere
	// where series = persons
	//   and /home/martin/sofa/persons.0/reuse -> /home/martin/persons
	private boolean isOverlaid( String vfname ) {
		return null != Series.base( Series.name() )
		&& vfname != null // sanity
		&& 0 != Series.base( Series.name() ).length() && 0 != vfname.length() // so base == "x*", NAME == "x*"
		&& Series.base( Series.name() ).length() < vfname.length()
		&& vfname.substring( 0, Series.base( Series.name() ).length()).equals( Series.base( Series.name() )); // NAME="xyz/abc" base="xyz" => true
	}
	// maps a virtual filename onto a "real" filesystem NAME.
	// if write - top overlay NAME is returned, simple.
	// if read  - overlay space is searched for an existing file.
	//          - if not found, or if the file has been deleted, the (non-existing) write filename is returned
	//          - if rename found (e.g. old^new), change return the old NAME. 
	public static String fsname( String vfname, String modeChs ) {
		//audit.traceIn("fsname","vfname="+vfname+", mode="+modeChs );
		String fsname = vfname; // pass through!
		Overlay o = Get();
		if (o != null && vfname != null) {
			//-- String vfname = rationalisePath( absolutePath( o.p.toString(), new String( fsname ) ));
			vfname = Path.absolute( o.path(), vfname );
			//audit.debug("abs is:"+vfname);
			if (o.isOverlaid( vfname )) {
				if (modeChs.equals( MODE_READ )) {
					fsname = o.find( vfname );
					
				} else if (modeChs.equals( MODE_WRITE ) || modeChs.equals( MODE_APPEND )) {
					fsname = o.topCandidate( vfname );
					
				} else if (modeChs.equals( MODE_DELETE )) {
					fsname = o.delCandidate( vfname, Series.highest()); // .. was number()
				}
			} //else
				//audit.debug("not overlaid by base="+ Series.base(Series.name()) +", name="+ Series.name());
		}
		//return audit.traceOut( fsname );
		return fsname;
	}
	static public String autoAttach() {
		String error = "";
		String candidate = new File( System.getProperty("user.dir")).getName();
		audit.debug( "autoAttach(): candidate is "+ candidate );
		if (candidate.equals( "" )) candidate = /*File.separator +*/ Series.DEFAULT;
		if (!Series.attach( candidate )) {
			error = "Unable to attach to "+ candidate;
			candidate = Overlay.DEFAULT;
			if (!Series.existing( candidate ))
				Series.create( candidate, new File( System.getProperty("user.dir")).getPath() );
			if (!Series.attach( candidate ))
				error = "Unable to attach to "+ candidate;
			else
				error = "";
		}
		if (error.equals( "" )) audit.debug( "Attached to "+ candidate );
		return error;
	}
	
	public boolean combineUnderlays() { return Series.compact(); }
	
	static private Overlay singletonO = null;
	static public Overlay Get() {
		if (null==singletonO) singletonO = new Overlay();
		return singletonO ;
	}
	static public void Set( Overlay o ) { singletonO = o; }

	static public String interpret( Strings argv ) {
		String rc = Shell.FAIL;
		int argc = argv.size();
		Overlay o = Overlay.Get();
		
		Strings values = argv.copyAfter( 0 );
		String  value  = values.toString( Strings.PATH );
		
		
		if (argv.get( 0 ).equals("attach") && (2 >= argc)) {
			if (2 == argc) {
				if (Series.attach( argv.get( 1 )))
					audit.debug( "No such series "+ argv.get( 1 ));
				else
					rc = Shell.SUCCESS;
			} else if ( Series.attached())
				audit.debug( "Not attached" );
			else
				rc = Shell.SUCCESS;
				
		} else if (argv.get( 0 ).equals("detach") && (1 == argc)) {
			Series.detach();
			rc = Shell.SUCCESS;
			
		} else if ((argv.get( 0 ).equals( "save" ) || argv.get( 0 ).equals( "create" )) && (1 == argc)) {
			//audit.audit( "Creating "+ o.series());
			rc = Shell.SUCCESS;
			o.create();
			
		//} else if (0 == argv.get( 0 ).equals( "exists" ) && (2 == argc)) {
		//	rc =  o.existingSeries( argv.get( 1 )) ? "Yes":"No";
			
		} else if (argv.get( 0 ).equals( "create" ) && ((2 == argc) || (3 == argc)) ) {
			if (!Series.create( argv.get( 1 ), argc == 3 ? argv.get( 2 ):System.getProperty("user.dir") ))
				audit.debug( argv.get( 1 ) + " already exists" );
			else
				rc = Shell.SUCCESS;
				
		//} else if (argv.get( 0 ).equals( "delete" ) && (2 == argc) ) {
		//	if (Series.deleteSeries( argv.get( 1 )))
		//		rc = Shell.SUCCESS;
		//	else
		//		audit.debug( argv.get( 1 ) + " doesn't exists" );
			
		} else if (argv.get( 0 ).equals(  "destroy"  ) && (1 == argc) ) {
			rc = o.destroy() ? Shell.SUCCESS : Shell.FAIL;
			
		} else if ((   argv.get( 0 ).equals(    "bond"  )
				    || argv.get( 0 ).equals( "combine"  ))
		           && (1 == argc) ) {
			rc = o.combineUnderlays() ? Shell.SUCCESS : Shell.FAIL;
			
		//} else if (0 == argv.get( 0 ).equals( "rm" ) && (2 == argc) ) {
		//	if (!entityIgnore( argv.get( 1 ))) rc =  argv.get( 1 ) +" doesn't exists" );
		
		} else if ((1 == argc) && argv.get( 0 ).equals( "up" )) {
			if ( o.path( ".." ))
				rc = Shell.SUCCESS;
			else
				audit.debug( argv.get( 0 ) +": Cannot cd to .." );
				
		} else if ((2 == argc) && argv.get( 0 ).equals( "cd" )) {
			if ( o.path( argv.get( 1 )))
				rc = Shell.SUCCESS;
			else
				audit.audit( argv.get( 0 ) +": Cannot cd to "+ argv.get( 1 ));
		
		} else if (argv.get( 0 ).equals( "pwd" ) && (1 == argc)) {
			rc = Shell.SUCCESS;
			audit.audit( o.path());

		} else if (argv.get( 0 ).equals( "write" )) {
			rc = Shell.SUCCESS;
			audit.audit( "New file would be '"+ Overlay.fsname( value, "w" ) +"'" ); // last param ignored
			
		} else if (argv.get( 0 ).equals( "mkdir" ) && (2 == argc)) {
			rc = Shell.SUCCESS;
			audit.audit( ">>>mkdirs("+Overlay.fsname( argv.get( 1 ), "w" )+") => "+ (new File( Overlay.fsname( argv.get( 1 ), "w" )).mkdirs()?"Ok":"Error"));
			
		} else if (argv.get( 0 ).equals( "read" ) && (2 == argc)) {
			rc = Shell.SUCCESS;
			audit.audit( "File found? is '"+ Overlay.fsname( argv.get( 1 ), "r" )+"'" );
		} else
			audit.debug( "Usage: attach <series>\n"
			                 +"     : detach\n"
			                 +"     : save\n"
		//	                 +"     : show [<n>] <pathname>\n"
			                 +"     : write <pathname>\n"
			                 +"     : read  <pathname>\n"
			                 +"     : delete <pathname>\n"
			                 +"     : pwd <pathname>\n"
			                 +"     : cd <pathname>\n"
		//	                 +"     : ls <pathname>\n"
			                 +"given: "+ argv.toString( Strings.CSV ));
		return rc;
	}
	
	/*
	 * Some helpers from enguage.....
	 * implements the transaction bit -- this isn't ACID :(
	 */
	private boolean inTxn = false;
	public void startTxn( boolean undoIsEnabled ) {
		if (undoIsEnabled) {
			inTxn = true;
			create();
	}	}
	public void finishTxn( boolean undoIsEnabled ) {
		if (undoIsEnabled) {
			inTxn = false;
			combineUnderlays();
	}	}
	public void reStartTxn() {
		if (inTxn) {
			destroy(); // remove this overlay
			destroy(); // remove previous -- this is the undo bit
			create();  // restart a new txn
	}	}
	

	
	public static void main (String args []) {
		Audit.turnOn();
		Overlay o = Get();
		Overlay.Set( o );
		String rc = autoAttach();
		if (!rc.equals( "" ))
			audit.audit( "Ouch!" + rc );
		else {
			audit.audit( "osroot="+ OverlaySpace.root );
			audit.audit( "base="+ Series.name()+", n=" + Series.number() );
			OverlayShell os = new OverlayShell( new Strings( args ));
			os.run();
}	}	}