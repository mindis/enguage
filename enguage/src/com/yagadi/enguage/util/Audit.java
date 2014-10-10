package com.yagadi.enguage.util;

import com.yagadi.enguage.util.Strings;

public class Audit {
	private String name = "";
	private String[] stack = null;

	private static boolean OUTPUTING = false; 
	public  static void suspend() { if (ON) OUTPUTING = false; }
	public  static void resume() {  if (ON) OUTPUTING = true;  }
	public  static boolean isOn() { return OUTPUTING; }
	
	private static boolean ON = false; 
	public  static void turnOff() { ON = OUTPUTING = false; }
	public  static void turnOn() {  ON = OUTPUTING = true;  }
	
	private static final String indent = "|  ";
	private static int level = 0;
	public  void incr() { level++; }
	public  void decr() { level--; }

	public Audit( String nm ) { name = nm; }
	
	/* private boolean on() {
	 *	 String dbg = System.getenv( "DEBUG" ); // e.g. "Enguage:Tags";
	 *	 String[] dbgs = Strings.fromString( dbg, ':' );
	 *	 for (int i=0; i<dbgs.length; i++)
	 *	 		if (NAME.equals( dbgs[ i ])) return true;
	 *	 return false;
	 * }
	 */
	public  void   ERROR( String info ) {
		System.err.println( "ERROR:" + name +": "+ info );
	}
	public  void   audit( String info ) {
		for (int i = 0 ; i < level; i++)
			System.err.print( indent );
		System.err.println( info );
	}
	public  void   debug( String info ) { if (OUTPUTING) audit( info ); }
	public  void traceIn( String fn, String info ) {
		if (OUTPUTING) {
			stack = Strings.prepend( stack, fn );
			audit( "IN  "+ name +"."+ stack[ 0 ] +"("+ (info==null?"":" "+ info +" ") +")" );
			incr();
	}	}
	public String traceOut( String result ) {
		if (OUTPUTING) {
			decr();
			audit( "OUT "+ name +"."+(null==stack || 0==stack.length? "traceOutNullStack" : stack[ 0 ]) + (result==null?"":" => "+ result ));
			stack = Strings.removeAt( stack, 0 );
		}
		return result;	
	}
	public void     traceOut() { traceOut( (String)null ); }
	public String[] traceOut( String[] sa ) { traceOut( "["+Strings.toString( sa, Strings.CSV )+"]"); return sa; }
	public boolean  traceOut( boolean bool ) { traceOut( bool ? "true":"false"); return bool; }
	public int      traceOut( int n ) { traceOut( Integer.toString( n )); return n;}
	
	public static void main( String[] agrs ) {
		Audit audit = new Audit( "Audit" ); // <= needs setting as $DEBUG to test
		audit.traceIn( "main", "this='is', a='test'" );
		audit.traceIn( "inner", "this='is', again='aTest'" );
		audit.debug( "Hello, martin" );
		audit.traceOut();
		audit.traceOut("passed");
}	}