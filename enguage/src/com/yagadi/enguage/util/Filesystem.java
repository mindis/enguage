package com.yagadi.enguage.util;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

//-- this is not in the non-android version
import android.os.Environment;

public class Filesystem {
	static Audit audit = new Audit("Filesystem");
	
	static private String location = "";
	static public  void   location( String sa ) { location = sa; }
	static public  String location() { return location; }

	static public final String root = 
		null != System.getenv( "HOME" ) ?
			System.getenv( "HOME" ) :
			Environment.getExternalStorageDirectory().getPath();
//					"./"; // -- this is in the non-android version.
			
	// Composite specific
	static public boolean createEntity( String name ) { return new File( name ).mkdirs(); }
	static public boolean renameEntity( String from, String to ) { return new File( from ).renameTo( new File( to )); }
	static public boolean existsEntity( String name ) { return new File( name ).isDirectory(); }
	static public boolean destroyEntity( String name ) { return new File( name ).delete(); }
	// General
	static public boolean create( String name ) { if (name == null) name = "."; return new File( name ).mkdirs(); }
	static public boolean rename( String from, String to ) { return new File( from ).renameTo( new File( to )); }
	static public boolean exists( String name ) { return new File( name ).isDirectory() || new File( name ).isFile(); }
	static public boolean destroy( String name ) {
		File dir = new File( name );
		String[] list = dir.list();
		if (list != null) for (int i=0; i<list.length; i++)
			destroy( name+ File.separator +list[ i ]);
		return dir.delete();
	}
	static public void stringToFile( String fname, String value ) {
		create( new File( fname ).getParent()); // just in case?
		try {
			PrintWriter pw = new PrintWriter( fname );
			pw.println( value );
			pw.close();
		} catch (FileNotFoundException e ) {
			System.err.println( "Filesystem::stringToFile(): File "+ fname +" not found: "+ e );	
	}	}
	static public String stringFromFile( String fname ) {
		String value = "";
		try {
			FileInputStream fis = new FileInputStream( fname );
			byte buf[] = new byte[ 1024 ];
			while (-1 != fis.read(buf)) value += new String( buf );
			value = value.trim(); // remove trailing blanks?
			fis.close();
		} catch (IOException e) {
			// just ignore non-existant files...
			//System.out.println( "Filesystem::stringFromFile(): IO exception: "+ e +", on reading: "+ fname );
			value = null;
		}
		return value;
	}
	static public String stringFromStream( InputStream is ) {
		String value = "";
		try {
			byte buf[] = new byte[ 1024 ];
			while (-1 != is.read(buf)) value += new String( buf );
			value = value.trim(); // remove trailing blanks?
		} catch (IOException e) {
			// just ignore non-existent files...
			System.out.println( "Filesystem::stringFromInputStream(): IO exception: "+ e );
			value = null;
		}
		return value;
	}
	// java fs model is s...  symlink-less
	static private final String symLinkExtension = ".symlink" ;
	static public boolean isLink( String s ) {
		return	s.length() > symLinkExtension.length()
				&& s.substring( s.length() - symLinkExtension.length()).equals( symLinkExtension );
	}
	static public String linkName( String name ) { return isLink( name ) ? name : name + symLinkExtension;}
	static public void   stringToLink( String fname, String value ) {
		if (null != fname) {
			if (isLink( fname ))
				stringToFile( fname, value );
			else
				stringToFile( fname+symLinkExtension, value );
	}	}
	static public String stringFromLink( String fname ){
		return isLink( fname ) ?
				Filesystem.stringFromFile( fname ) : 
				Filesystem.stringFromFile( fname+symLinkExtension );
}	}