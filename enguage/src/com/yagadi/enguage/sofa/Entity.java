package com.yagadi.enguage.sofa;

import java.io.File;

import com.yagadi.enguage.sofa.Overlay;
import com.yagadi.enguage.util.Strings;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Filesystem;

class EntityShell extends Shell {
	EntityShell( String[] args ) { super( "Entity", args );}
	public String interpret( String[] argv ) { return Entity.interpret( argv ); }
}

public class Entity {
	static private Audit audit = new Audit( "Entity" );
	
	public static String name( String entity, String rw ) { return Overlay.fsname( entity, rw );}
	public static boolean exists( String name ) {
		audit.traceIn( "exists", Overlay.fsname( name, Overlay.MODE_READ ));
		audit.debug( "NAME="+ name( name, Overlay.MODE_READ ));
		return audit.traceOut( Filesystem.exists( name( name, Overlay.MODE_READ )));
	}

	public static String deleteName( String name ) {
		if (isDeleteName( name )) return name;
		File f = new File( name );
		return f.getParent() +"/!"+ f.getName();
	}
	public static String nonDeleteName( String name ) {
		if (!isDeleteName( name )) return name;
		File f = new File( name );
		return f.getParent() +"/"+ f.getName().substring( 1 );
	}
	public static boolean isDeleteName( String name ) {
		File f = new File( name );
		return f.getName().charAt( 0 ) == '!';
	}
	
	public static boolean create( String name ) { return Filesystem.create( name( name, Overlay.MODE_WRITE ) );}
	
	// really should be in a corresponding Component.c module!
	public static boolean createComponent( String[] a ) {
		boolean rc = false;
		String name = "";
		for (int i=0, sz=a.length; i<sz; i++) { // ignore all initial unsuccessful creates
			name += a[ i ];
			rc = Filesystem.create( name );
			name += "/";
		}
		return rc;	
	}
	public static boolean delete( String name ) {
		boolean rc = true;
		String readName  = Overlay.fsname( name, Overlay.MODE_READ );
		if (Filesystem.exists( readName )) {
			String writeName = Overlay.fsname( name, Overlay.MODE_WRITE ),
			       dname = deleteName( writeName );
			if (!Filesystem.destroy( writeName )) {
				// haven't managed to remove top overlay entity -- either not empty or not there
				rc = Filesystem.exists( writeName ) ?
					Filesystem.rename( writeName, dname ) : // ...it is there, so rename it!
					Filesystem.create( dname ); //...not there, so put in a placeholder!
			} else if (Filesystem.exists( readName )) // successfully removed entity but prev version still exists...
				rc = Filesystem.create( dname );
		}
		return rc;
	}
	public static boolean ignore( String name ) {
		boolean status = false;
		String actual = Overlay.fsname( name, Overlay.MODE_READ ),
		       potential = Overlay.fsname( name, Overlay.MODE_WRITE ),
		       ignored = deleteName( potential );
		if (Filesystem.exists( actual ))
			if (Filesystem.exists( potential )) 
				status = Filesystem.rename( potential, ignored );
			else
				status = Filesystem.create( ignored );
		return status;
	}
	
	public static boolean restore( String entity ) {
		boolean status = false;
		String restored = Overlay.fsname( entity, Overlay.MODE_WRITE ),
				ignored = deleteName( restored );
		if (!exists( entity ))
			status = Filesystem.rename( ignored, restored );
		return status;
	}
	
	static public String interpret( String[] argv ) {
		// N.B. argv[ 0 ]="create", argv[ 1 ]="martin wheatman"
		String rc = Shell.FAIL;
		if (argv.length == 2 && argv[ 0 ].equals("create"))
			rc = create( argv[ 1 ])? Shell.SUCCESS : Shell.FAIL;
		else if (argv.length >= 3 && argv[ 0 ].equals("component"))
			rc = createComponent( Strings.copyAfter( argv, 1 ))? Shell.SUCCESS : Shell.FAIL;
		else if (argv.length == 2 && argv[ 0 ].equals("delete"))
			rc = delete( argv[ 1 ])? Shell.SUCCESS : Shell.FAIL;
		else if (argv.length == 2 && argv[ 0 ].equals("exists"))
			rc = exists( argv[ 1 ])? Shell.SUCCESS : Shell.FAIL;
		else if (argv.length == 2 && argv[ 0 ].equals("ignore"))
			rc = ignore( argv[ 1 ])? Shell.SUCCESS : Shell.FAIL;
		else if (argv.length == 2 && argv[ 0 ].equals("restore"))
			rc = restore( argv[ 1 ])? Shell.SUCCESS : Shell.FAIL;
		else
			System.err.println(
					"Usage: entity [create|exists|ignore|delete] <entityName>\n"+
					"Given: "+ Strings.toString( argv, Strings.SPACED ));
		return rc;
	}
	
	public static void main (String args []) {
		Overlay.Set( Overlay.Get());
		String rc = Overlay.autoAttach();
		if (!rc.equals( "" ))
			System.out.println( "Ouch!" );
		else
			new EntityShell( args ).run();
}	}