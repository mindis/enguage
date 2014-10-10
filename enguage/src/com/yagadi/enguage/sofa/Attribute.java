package com.yagadi.enguage.sofa;

public class Attribute {
	protected String name;
	public    String name() { return name; }
	
	protected String    value;
	public    String    value() { return value; }
	public    Attribute value( String s ) { value = s; return this;}
	
	public Attribute( String n ) { name = n; value = null; }
	public Attribute( String n, String v ) { name = n; value = v; }
	public String toString() {
		String chars = name +"='";
		for (int i=0, sz=value.length(); i<sz; i++)
			if ('\n' == value.charAt( i ))
				chars += "'\n      '";
			else
				chars += Character.toString( value.charAt( i ));
		return chars += "'";
	}
	//public String toString() { return " "+NAME+"='"+value+"'"; }
	public boolean equals( String s ) { return name.equals( s );}
}

