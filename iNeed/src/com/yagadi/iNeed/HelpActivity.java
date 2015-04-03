/*
 * iNeed - HelpActivity.
 * 
 * This code is released under the Lesser GPL
 * - do with it what you will, just keep the below
 *   copyright for the swathes of code you leave in!
 * 
 * (c) yagadi ltd, 2013-4.
 */
package com.yagadi.iNeed;

import com.yagadi.enguage.concept.Repertoire;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;

public class HelpActivity extends Activity {
	//static private Audit audit = new Audit( "HelpActivity" );
	
	//needs to be static to get formatting to work...
	static private ScrollView helpScrollArea = null;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.help_layout );
		setTitle( Repertoire.lastLoaded() +": Help");
		
		//refillHelpScrollArea();
		helpScrollArea = (ScrollView)findViewById( R.id.helpScrollView );
		helpScrollArea.removeAllViews();
		helpScrollArea.addView( DeviceText.get( this, DeviceText.help ));
}	}
