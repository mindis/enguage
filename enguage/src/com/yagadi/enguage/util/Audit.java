package com.yagadi.enguage.util;

import java.util.GregorianCalendar;

import com.yagadi.enguage.Enguage;

public class Audit {
	private              String     name = "";
	private              Strings   stack = new Strings();
	private static final boolean timings = Enguage.timingDebugging;

	private static boolean suspended = false; 
	public  static void suspend() { suspended = true; }
	public  static void resume() {  suspended = false;  }
	
	private static boolean on = false; 
	public  static void turnOff() { on = false; }
	public  static void turnOn() {  on = true; }
	public  static boolean isOn() { return on; }
	
	private static final String indent = "|  ";
	private static int level = 0;
	public  void incr() { level++; }
	public  void decr() { if (level>0) level--; }

	static private long then =  new GregorianCalendar().getTimeInMillis();
	static public  long interval() {
		long now = new GregorianCalendar().getTimeInMillis();
		long rc = now - then;
		then = now;
		return rc;
	}
	
	public Audit( String nm ) { name = nm; }
	
	/* private boolean on() {
	 *	 String dbg = System.getenv( "DEBUG" ); // e.g. "Enguage:Tags";
	 *	 String[] dbgs = Strings.fromString( dbg, ':' );
	 *	 for (int i=0; i<dbgs.length; i++)
	 *	 		if (NAME.equals( dbgs[ i ])) return true;
	 *	 return false;
	 * }
	 */
	public  void   ERROR( String info ) { System.err.println( "ERROR:" + name +": "+ info);}
	public  String audit( String info ) {
		if (!suspended) {
			//System.out.print( "("+ level +")" );
			for (int i = 0 ; i < level; i++)
				System.out.print( indent );
			System.out.println( info );
		}
		return info;
	}
	public  void   debug( String info ) { if (on) audit( info ); }
	public  void traceIn( String fn, String info ) {
		if (on) {
			stack.prepend( fn );
			audit( "IN  "+ name +"."+ stack.get( 0 ) +"("+ (info==null?"":" "+ info +" ") +")"
					+ (timings ? " -- "+interval()+"ms" : "")
				);
			incr();
	}	}
	public String traceOut( String result ) {
		if (on) {
			decr();
			audit( "OUT "+ name +"."
					+ (null==stack || 0==stack.size()? "traceOutNullStack" : stack.get( 0 ))
					+ (result==null?"":" => "+ result)
					+ (timings ? " -- "+interval()+"ms" : "")
				);
			if (stack.size() > 0) stack.remove( 0 );
		}
		return result;	
	}
	public void    traceOut() { traceOut( (String)null ); }
	public Strings traceOut( Strings sa ) { traceOut( sa.toString()); return sa; }
	public boolean traceOut( boolean bool ) { traceOut( bool ? "true":"false"); return bool; }
	public int     traceOut( int n ) { traceOut( Integer.toString( n )); return n;}
	public Float   traceOut( Float f ) { traceOut( Float.toString( f )); return f;}
	
	public static void main( String[] agrs ) {
		Audit audit = new Audit( "Audit" ); // <= needs setting as $DEBUG to test
		audit.traceIn( "main", "this='is', a='test'" );
		audit.traceIn( "inner", "this='is', again='aTest'" );
		audit.debug( "Hello, martin" );
		audit.traceOut();
		audit.traceOut("passed");
}	}