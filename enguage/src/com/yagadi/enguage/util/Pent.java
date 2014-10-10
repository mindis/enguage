package com.yagadi.enguage.util;

public class Pent {
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