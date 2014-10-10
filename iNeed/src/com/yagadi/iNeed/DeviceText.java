package com.yagadi.iNeed;

import android.app.Activity;
import android.text.Html;
import android.widget.TextView;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.concept.Engine;
import com.yagadi.enguage.sofa.Numeric;
//import com.yagadi.enguage.util.Audit;

public class DeviceText {
	public static final String help = "help.html";
	//private static Audit audit = new Audit ( "DeviceText" );
	
	public static Float getSize() {
		
		final Float mixTextSize = 8f;
		final Float maxTextSize = 36f; // -- or 72?
		final Float defTextSize = 22f;
		
		Numeric   textSizeNumeric = new Numeric( "device", "textSize" );
		Float textSize = textSizeNumeric.get( defTextSize );
		if (textSize < mixTextSize) {
			textSize = mixTextSize;
			textSizeNumeric.set( textSize );
		} else if (textSize > maxTextSize) {
			textSize = maxTextSize;
			textSizeNumeric.set( textSize );
		}
		return textSize;
	}
	public static TextView get( Activity ctx, String name ) {
		String s = "";
		if (name.equals( help )) {
			String rep = Engine.repertoireHelpToHtml();
			String app = Assets.stringFromFileOrAsset( ctx.getAssets(), name );
			String def = Enguage.interpreter == null || !Enguage.interpreter.defaultRepIsLoaded() ? "" :
				"To add multiple items use <b><i>X and Y and ...</i></b><br/><br/>"
				+ "<b>Hint</b>: use the same phrase/spelling used for added items on removing them!<br/>"
				+ "<b>Note</b>: explicitly remove 'I need to X', with 'I don't need to X'<br/> ";
			s = "Say <b><i>help</i></b> to get aural help.<br/><br/>"
					+ (rep.equals( "" ) ? " - repertoire not yet loaded -<br/>" : rep) + "<br/>" 
					+ (app.equals( "" ) ? "" : app + "<br/>") 
					+ (def.equals( "" ) ? "" : def + "<br/>") 
					+ "For further details, "
					+ "including device control and how to program this language engine, "
					+ "see <a href=\"www.yagadi.com\">www.yagadi.com</a>";
		} else
			s = Assets.stringFromFileOrAsset( ctx.getAssets(), name );
		
		TextView text = new TextView( ctx );
		text.setText( Html.fromHtml( s ));
		text.setTextSize( getSize() );
		text.setId( R.string.phContent );
		return text;
}	}
