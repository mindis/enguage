package com.yagadi.enguage.concept;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.expression.Language;
import com.yagadi.enguage.util.Strings;

public class Repertoire extends Signs {
	static final long serialVersionUID = 0l;
	
	public Repertoire( String name ) { super( name );}
	
	public String helpToHtml() {
		String output = "";
		// this may be called on startup
		for (Sign s : this )
			if (s.attributes().get("help") != ""
			 && s.attributes().get("id").equalsIgnoreCase( Enguage.defaultRepertoire ))
				output += ( "<b><i>"+ Language.stripTerminator( s.content().toText()) +"</i></b>, to " + s.attribute( "help" ) +"<br/>");
		return output;
	}
	public String helpToString() { return helpToString( Enguage.defaultRepertoire ); }
	public String helpToString( String name ) {
		String output = "";
		for (Sign s : this )
			if (s.attributes().get("help") != "" && s.attributes().get("id").equalsIgnoreCase( name ))
				output += ("you can say, "+ Language.stripTerminator( s.content().toText()) +", to " + s.attribute("help") +"."); // TODO: parameterise "You can say, ..."
		return output;
	}
	public String[] toIdList() {
		String[] output = new String[ 0 ];
		for (Sign s : this ) {
			String name = s.attributes().get("id");
			if (!Strings.contain( output, name ))
				output = Strings.append( output, name );
		}
		return output;
}	}
