package com.yagadi.iNeed;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.sofa.Preferences;

//* this code was developed from the background example kindly provided by www.androidhive.info
public class SplashActivity extends Activity {

	Preferences p;
	
	// this value is used in device.txt
	public final static String  hosPref     = "prefStartupHelp";
	public final static boolean initHosPref = true;
	
	// -- widgets
	private Button         ok = null;
	private CheckBox      hos = null; // help on startup
	private ScrollView scroll = null; // placeholder
	private TextView   //help = null,
	                    title = null;
	private ImageView    icon = null;
	
	private void showHosOrIcon( boolean hos ) {
		scroll = (ScrollView) findViewById( R.id.splashPlaceholder );
		scroll.removeAllViews();
		if (hos) {
			scroll.addView( DeviceText.get( this, DeviceText.help ));
		} else {
			icon = new ImageView( this );
			icon.setImageResource( R.drawable.ic_launcher );
			icon.setId( R.string.phContent );
			scroll.addView( icon );
	}	}
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.splash_layout );
		setTitle( "Initialising..." );
		
		// ok, now init this layout...
		title = (TextView)findViewById( R.id.splashTitle );
		title.setText( title.getText() /* +" ("+ this.getString( R.string.version_id ) +")"*/ ); 
		
		// tell the sofa what preferences to use
		p = new Preferences( PreferenceManager.getDefaultSharedPreferences( this ));
		Preferences.setPreferences( p );
		
		// initialise OK button
		ok = (Button) findViewById( R.id.splash_ok );
		ok.setEnabled( false );
		ok.setText( R.string.loading );
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// button is only enabled when ready to move on...
				startActivity( new Intent( SplashActivity.this, MainActivity.class ));
				SplashActivity.this.finish();
			}	});
		
		// setup help on start up option
		boolean showHos = p.get( hosPref, initHosPref );
		hos = (CheckBox) findViewById( R.id.splashHelpCb );
		hos.setChecked( showHos );
		hos.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				p.set( hosPref, ((CheckBox)v).isChecked() );
				showHosOrIcon(  ((CheckBox)v).isChecked() );
		}	});
		showHosOrIcon( showHos );
		
		// now read the config file in the background...
		new ReadConfig( this ).execute();
	}
	// --------
	private class ReadConfig extends AsyncTask<Void, Void, Void> {
		private SplashActivity ctx = null;
		public ReadConfig( SplashActivity a ) {
			super();
			ctx = a;
		}
		@Override
		protected void onPreExecute() { super.onPreExecute(); }
		@Override
		protected Void doInBackground(Void... arg0) {
			Enguage.interpreter = new Enguage( ctx );
			Enguage.interpreter.loadConfig();
			return null;
		}
		@Override
		protected void onPostExecute( Void result ) {
			super.onPostExecute( result );
			if ( ctx.p.get( hosPref, initHosPref )) {
				// just enable OK button, and wait for user confirmation
				if (ok == null) ok = (Button) findViewById( R.id.splash_ok );
				ok.setEnabled( true );
				ok.setText( R.string.okButton );
	   		} else {
	   			// move straight onto next activity
				ctx.startActivity( new Intent( SplashActivity.this, MainActivity.class ));
				finish();
}	}	}	}