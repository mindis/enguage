/*
 * iNeed - DeviceText
 * 
 * This code is released under the Lesser GPL
 * - do with it what you will, just keep the below
 *   copyright for the swathes of code you leave in!
 * 
 * (c) yagadi ltd, 2013-4.
 */
package com.yagadi.iNeed;

import android.app.Activity;
import android.text.Html;
import android.widget.TextView;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.concept.Repertoire;
import com.yagadi.enguage.sofa.Numeric;
import com.yagadi.enguage.util.Audit;

public class DeviceText {
	public static final String help = "help.html";
	private static Audit audit = new Audit ( "DeviceText" );
	
	public static Float getSize() {
		final float minTextSize = 8f;
		final float maxTextSize = 36f; // -- or 72?
		final float defTextSize = 22f;
		
		Numeric   textSizeNumeric = new Numeric( "device", "textSize" );
		Float textSize = textSizeNumeric.get( defTextSize );
		if (Float.isNaN( textSize )) {
			audit.ERROR( "DeviceText.get(): textSize("+ textSize +") is Float.NaN" );
			textSize = defTextSize;
			textSizeNumeric.set( textSize );
		} else if (textSize < minTextSize) {
			audit.ERROR( "DeviceText.get(): textSize("+ textSize +") < minTextSize("+ minTextSize +")" );
			textSize = minTextSize;
			textSizeNumeric.set( textSize );
		} else if (textSize > maxTextSize) {
			audit.ERROR( "DeviceText.get(): textSize("+ textSize +") > maxTextSize("+ maxTextSize +")" );
			textSize = maxTextSize;
			textSizeNumeric.set( textSize );
		}
		return textSize;
	}
	public static TextView get( Activity ctx, String name ) {
		String s = "";
		if (name.equals( help )) {
			String rep = (Enguage.e == null || Enguage.signs == null) ?  "" : Enguage.signs.helpToHtml( Repertoire.def() );
			String app = Assets.stringFromFileOrAsset( ctx.getAssets(), name );
			String def = Enguage.e == null || !Repertoire.defaultRepIsLoaded() ? "" :
				ctx.getString( R.string.visualHelp1 );
			s = ctx.getString( R.string.visualHelp2 )
					+ (rep.equals( "" ) ? ctx.getString( R.string.visualHelp4repNotLoaded ) + "<br/>" : "Basic repertoire:<br/>" + rep) + "<br/>" 
					+ (app.equals( "" ) ? "" : app + "<br/>") 
					+ (def.equals( "" ) ? "" : def + "<br/>") 
					+ ctx.getString( R.string.visualHelp3 );
		} else
			s = Assets.stringFromFileOrAsset( ctx.getAssets(), name );
		
		TextView text = new TextView( ctx );
		text.setText( Html.fromHtml( s ));
		text.setTextSize( getSize() );
		text.setId( R.string.phContent );
		return text;
}	}
