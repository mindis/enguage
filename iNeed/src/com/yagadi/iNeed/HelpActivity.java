package com.yagadi.iNeed;

import com.yagadi.enguage.Enguage;

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
		setTitle( Enguage.lastLoaded() +": Help");
		
		//refillHelpScrollArea();
		helpScrollArea = (ScrollView)findViewById( R.id.helpScrollView );
		helpScrollArea.removeAllViews();
		helpScrollArea.addView( DeviceText.get( this, DeviceText.help ));
}	}
