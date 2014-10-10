package com.yagadi.enguage.sofa;

import java.io.File;
import java.io.IOException;

import com.yagadi.enguage.sofa.Entity;
import com.yagadi.enguage.sofa.LinkShell;
import com.yagadi.enguage.sofa.Value;
import com.yagadi.enguage.util.*;

class LinkShell extends Shell {
	LinkShell( String[] args ) { super("LinkShell", args ); }
	public String interpret( String[] a ) { return Link.interpret( a ); }
}

// we may have had: my car's colour is red. ==> martin/car/colour -> ../../red
// We have:
//   create(  "user", "martin", 0 ); --> user -> martin // simle link
//   create("martin", "holding", [ "ruth", "hand" ]) --> ./martin/holding -> ../ruth/hand
// we also want (where ./"martin/car/" exists!):
//   create("martin", "car", [ "f209klg" ]) --> ./martin/car -> ../f209klg
// being done at wrong level(?) as we aslo assert:     ./f209klg/isa -> ../car
// we aslo want to support components:
// create( "martin/keys", "location", [ "mach", "kitchen" ]);
// => martin/keys/location -> ../../mach/kitchen
// Sooooo..... the number of parents "../" is dependent on the number of "levels/" in entity string!!!
// this has so far only been 1! e.g. entity="martin", attribute="attr" => link="../attr"
public class Link {
	//static private Audit audit = new Audit( "Link" );
	
	public static String get( String entity, String attribute ) {
		//audit.traceIn( "get", "entity='"+ entity +"', attribute='"+ attribute +"'" );
		if ( entity.charAt( 0 ) == '$' ) entity = "_"+ entity.substring( 1 ); // not relevant here?
		String linkName = null != attribute ?
				Value.name( entity, Filesystem.linkName( attribute ), Overlay.MODE_READ ) :
				Entity.name(    Filesystem.linkName( entity ), Overlay.MODE_READ ) ;
		String str = Filesystem.stringFromLink( linkName );
		//audit.debug( linkName +" => '"+ str +"'" );
		if ((null != str) && (str.length() > 3) /*&& attribute != null*/)
			str = str.substring(null != attribute ? 3 : 0) ; // attr value will start "../"
		return str; //audit.traceOut( str );
	}
	static public boolean create( String entity, String attribute, String[] values ) { return set( entity, attribute, values );}
	static public boolean set( String entity, String attribute, String[] values ) {
		//audit.traceIn( "set", "entity='"+ entity +"', attribute='"+ attribute +"', values=["+ Strings.toString( values, Strings.CSV ) +"]" );
		boolean status = false;
		if (entity!=null && attribute != null) {
			String linkName, content;
			if ( values != null && 0 < values.length ) { // link martin mother janet -- mother is attribute!
				content = "../"; // differnt to C version as Strings.PATH, is not equiv to toPath in Link.c
				// component support... prefix "../"'s to match entity depth...
				for (int i=0, sz=entity.length(); i<sz; i++) {
					if ('/' == entity.charAt( i )) { // for every '/'...
						content += "../"; // ...use an extra "../"
						while (i<sz && '/' == entity.charAt( 1+i )) i++; // remove instances of "////"
				}	}
				// ...component support.
				content += Strings.toString( values, Strings.PATH ); // ../ruth/hand
				linkName = Value.name( entity, attribute, Overlay.MODE_WRITE ); // martin/holding
				Entity.create( entity );                                        // martin
			} else { // link NAME value -- simple representation of (versionable!) shell vars as symlinks
				linkName = Entity.name( entity, Overlay.MODE_WRITE );
				content = attribute;
			}
			//audit.debug( "deleting: "+ linkName );
			new File( linkName ).delete();
			//audit.debug( "creating: "+ linkName +", content:"+ content );
			Filesystem.stringToLink( linkName, content);
			status = true;
		}
		//audit.traceOut( status ? "TRUE" : "FALSE" );
		return status;
	}

	// doesn't yet support null attribute
	public static boolean delete( String entity, String attribute ) { 
		Entity.ignore( Filesystem.linkName( Value.name( entity, attribute, Overlay.MODE_WRITE )));
		return true; // lets be optimistic!
	}
	// doesn't yet support null attribute
	public static boolean destroy( String entity, String attribute ) {
		//audit.traceIn( "destroy", "e='"+ entity+"', a='"+attribute+"'" );
		String name = Filesystem.linkName( Value.name( entity, attribute, Overlay.MODE_WRITE ));
		//audit.debug( "deleting NAME='"+ name +"'" );
		//return audit.traceOut( Filesystem.destroy( name ));
		return Filesystem.destroy( name );
	}
	private static boolean arrayCharsEqual( String[] a, String[] b ) {
		boolean rc = null == a && null == b; // both empty -- equal
		if (null != a && null != b) { // both not empty -- inspect array contents!
			int ai = 0, bi = 0, az=a.length, bz=b.length;
			// skip any parent dirs, so "../../martin" is equal to "martin"
			while (ai<az && a[ ai ].equals("..")) ai++;
			while (bi<bz && b[ bi ].equals("..")) bi++;
			//ai--; bi--; // step back on both!
			while (az>ai && bz>bi && b[ bi ].equals( a[ ai ])) {
				ai++; bi++;
			}
			rc = az == ai && bz == bi;
		}
		return rc;
	}
	// martin/car/colour/silver, martin/car -> ../pj55ozw + pj55ozw/colour -> silver => found
	private static boolean linkAndValue(String name, String[] value) {
		//audit.traceIn( "linkAndValue", "NAME='"+ name +"', value=["+ Strings.toString( value, Strings.CSV ) +"]");
		boolean found = false;
		String candidate = Overlay.fsname( Filesystem.linkName( name ), Overlay.MODE_READ );
		if (null != candidate) {
			String buffer= Filesystem.stringFromLink( candidate );
			found = null != buffer;
			//audit.debug( candidate + ": found -> "+ (found ? buffer : "FALSE"));
			// now see if we've found a link... is it the right one?
			if (found && value != null && 0 != value.length) { // found AND we have a required value...
				String[] b = Strings.fromArrayList( Strings.ListFromSeparatedString( buffer, '/' ));
				//audit.debug( "Checking value '"+ Strings.toString( b, Strings.CSV ) +"' <==> '"+ Strings.toString( value, Strings.CSV ) +"'");
				found = arrayCharsEqual( b, value );
				//audit.debug( "Found is "+ (found?"TRUE":"FALSE"));
		}	}
		return found ; //audit.traceOut( found );
	}

	// Currently this supports:
	// linkExists( "martin", "holding", [ "ruth", "hand" ]) => martin/holding -> ../ruth/hand
	// We also want:
	// linkExists( "martin", "car", [ "colour", "red" ]) => martin/car/colour -> ../red
	// possibly "../../red", possibly "red"?
	// need to deal with martin/car -> ../pj55ozw/bodyShell -> ../1234567890/colour -> red
	// so first link found might not be the only one!
	// we may even have user -> martin ???
	// 2013/10/01 - some of this doesn't work: link exists person isa isa (should get thru to class)
	//   TODO:    - should this call transExists()?
	public static boolean exists( String[] a ) {
		//audit.traceIn( "exists", Strings.toString( a, Strings.CSV ));
		boolean found = false;
		if ( null != a && (a.length >= 2)) {
			String candidate = new String( a[ 0 ]);
			int ai = 0, az = a.length;
			while (ai<az && !found) { // keep going until we've found it, or run out of array
				a = Strings.copyAfter( a, 0 );
				//audit.debug( "candidate='"+ candidate +"', "+ Strings.toString( a, Strings.CSV ));
				if (!(found = linkAndValue( candidate, a ))) { // ...not found
					String buffer = Filesystem.stringFromLink( Filesystem.linkName( candidate ));
					//audit.debug( "found intermediate link => "+ buffer );
					if (null != buffer) {
						if ('/' == buffer.charAt( 0 ))
							candidate = Overlay.fsname( buffer, Overlay.MODE_READ );
						else {
							try {
								candidate = new File( candidate + "/../" + buffer ).getCanonicalPath();
							} catch(IOException e) {
								found = false;
					}	}	}
				if (a == null) break;
					// swap next array value onto candidate - whether or not we've found a link...
					candidate += ( "/" + a[ 0 ]);
		}	}	}
		return found; //audit.traceOut( found );
	}
/*	// doesn't yet support null values
	private boolean linkExistsOld(String entity, String link, String[] values ) {
		String NAME = Filesystem.sym( Attribute.name( entity, link, Overlay.MODE_READ ));
		return (null != NAME && NAME.length() > 3) // not found OR buffer too small
			&& (null != values || 0 != values.length || 0 == NAME.substring(3).equals( Strings.toString( values, Strings.PATH ) )); // no value to compare of value compares ok
	}*/

	//---
	// doesn't yet support null value
	// assumes all links are "../X"
	public static boolean transExists(String entity, String trans, String value ) {
		boolean rc = false;
		String buffer = Filesystem.stringFromLink( Value.name( entity, trans, Overlay.MODE_READ ) );
		if ((null == buffer) || buffer.length() <= 3) // not found OR buffer too small
			rc = false;
		else if (null == value || buffer.substring(3).equals( value ))
			rc = true;
		else if (!buffer.substring(3).equals("class"))
			rc = transExists( buffer.substring(3), trans, value );
		return rc;
	}

	// doesn't yet support null value
	// assumes all links are "../X"
	public static boolean transAttrExists(String entity, String trans, String value ) {
		boolean rc = false;
		if (new File( Value.name( entity, value, Overlay.MODE_READ )).isFile()) 
			rc = true;
		else {
			String buffer = Filesystem.stringFromLink( Value.name( entity, trans, Overlay.MODE_WRITE ));
			if (null == buffer || buffer.length() <= 3) // not found OR buffer too small
				rc = false;
			else if (null == value || buffer.substring(3).equals( value ))
				rc = true;
			else if (!buffer.substring(3).equals("class"))
				rc = transAttrExists( buffer.substring(3), trans, value );
		}
		return rc;
	}
	//---
	static public String interpret( String[] a ) {
		//audit.traceIn( "interpret", "["+ Strings.toString( a, Strings.CSV ) +"]" );
		String rc = Shell.SUCCESS;
		int argc = a.length;
		String[] args = Strings.copyAfter( a, 2 );
		String[] b = Strings.copyAfter( a, 0 );
		
		if(( argc >= 3 ) &&  a[ 0 ].equals("set")) {
			rc = create( a[ 1 ], a[ 2 ], args ) ? Shell.SUCCESS : Shell.FAIL;
		} else if(( argc == 3 ) && a[ 0 ].equals("get")) {
			rc = get( a[ 1 ], a[ 2 ]);
		} else if(( argc == 2 ) && a[ 0 ].equals("get")) {
			rc = get( a[ 1 ], null );
		} else if(( argc >= 2 ) && a[ 0 ].equals("exists")) {
			rc = exists( b ) ? Shell.SUCCESS : Shell.FAIL;
		} else if(( argc == 3 ) && a[ 0 ].equals("delete")) {
			rc = delete( a[ 1 ], a[ 2 ]) ? Shell.SUCCESS : Shell.FAIL;
		} else if(( argc >= 3 ) && a[ 0 ].equals("destroy")) {
			if (exists( b )) rc = destroy( a[ 1 ], a[ 2 ]) ? Shell.SUCCESS : Shell.FAIL;
		} else if(( argc == 4 ) && a[ 0 ].equals("delete")) {
			if (exists( b )) rc = delete( a[ 1 ], a[ 2 ]) ? Shell.SUCCESS : Shell.FAIL;
		} else if (( argc == 4 ) && a[ 0 ].equals("transExists")) {
			rc = transExists( a[ 1 ], a[ 2 ], a[ 3 ]) ? Shell.SUCCESS : Shell.FAIL;
		} else if(( argc == 4 ) && a[ 0 ].equals("transAttrExists")) {
			rc = transAttrExists( a[ 1 ], a[ 2 ], a[ 3 ]) ? Shell.SUCCESS : Shell.FAIL;
		} else {
			rc = Shell.FAIL;
			System.out.println(
					"Usage: link: [set|get|exists|transExists|transAttrExists|delete] <ent> <link> [<value>]\n"+
					"given: "+ Strings.toString( a, Strings.SPACED ));
		}
		return rc; // audit.traceOut( rc );	
	}
	public static void main( String[] args ) {
		Overlay.Set( Overlay.Get());
		String rc = Overlay.autoAttach();
		if (!rc.equals( "" ))
			System.out.println( "Ouch! "+ rc );
		else
			new LinkShell( args ).run();
}	}