/*
 * iNeed - MainActivity.
 * 
 * This code is released under the Lesser GPL
 * - do with it what you will, just keep the below
 *   copyright for the swayths of code you leave in!
 * 
 * (c) yagadi ltd, 2013-4.
 */

package com.yagadi.iNeed;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yagadi.enguage.Enguage;
import com.yagadi.enguage.concept.Engine;
import com.yagadi.enguage.concept.Repertoire;
import com.yagadi.enguage.expression.Colloquial;
import com.yagadi.enguage.expression.Language;
import com.yagadi.enguage.sofa.Preferences;
import com.yagadi.enguage.sofa.Variable;
import com.yagadi.enguage.sofa.tier2.Item;
import com.yagadi.enguage.sofa.tier2.List;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Shell;
import com.yagadi.enguage.util.Strings;

public class MainActivity extends ActionBarActivity implements TextToSpeech.OnInitListener {

	//static private Audit audit = new Audit( "MainActivity" );

	// these values are defined in resources.
	static public final String      previewMode = "preview";
	static public final boolean initPreviewMode = false;
	static public final String       visualMode = "visual";
	static public final boolean  initVisualMode = true;
	static public final String          verbose = "verbose";
	static public final boolean     initVerbose = true;
	
	// behaviour - other values from Engine
	static private      boolean     firstRun = true;
	// widgets
	       private ImageButton        cancel = null;
	       private ImageButton      okButton = null;
	       //private ImageButton       cycle = null; // skip forward -- not yet used
	       private EditText         editText = null;
	       private ImageButton      speakNow = null;
	
	static public boolean verboseMode() { return Preferences.getPreferences().get( verbose, initVerbose ); }
	static public boolean previewMode() { return visualMode() && Preferences.getPreferences().get( previewMode, initPreviewMode ); }
	static public boolean  visualMode() { return Preferences.getPreferences().get( visualMode, initVisualMode ); }
	private void initHelp( boolean spoken ) {
		if (spoken)
			Repertoire.help( this.getString( R.string.help0 ) + " , " +
				(visualMode() ?
					this.getString( R.string.help1 )  + " , " + (previewMode()?this.getString( R.string.help2 ):"")
				:	this.getString( R.string.help3 )  ));
		else
			Repertoire.help( this.getString( R.string.toSpeak0 ) + " , " +
				(visualMode() ?
					this.getString( R.string.toSpeak1 )  + " , " + (previewMode()?this.getString( R.string.toSpeak2 ):"")
				:	this.getString( R.string.toSpeak3 )) + " " + this.getString( R.string.helpHint ));
	}
	public String helpPrompt() {
		return !Engine.helpRun() ?
				Repertoire.help() /* just say help */
				: Enguage.e.signs.helpToString(); // what you can say
	}

	public TextToSpeech tts = null;
	private boolean ttsInitialised = false;
	private boolean vocalised() {
		AudioManager am = (AudioManager)this.getSystemService( Activity.AUDIO_SERVICE );
		return ttsInitialised && AudioManager.RINGER_MODE_NORMAL == am.getRingerMode();
	}

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );

		// tell the sofa what preferences to use
		Preferences.setPreferences( new Preferences( PreferenceManager.getDefaultSharedPreferences( this )) );
		setTitle( Repertoire.lastLoaded().equals( Repertoire.def() ) ?
				Repertoire.def() :
				Enguage.name +": "+ Repertoire.lastLoaded()
		);
		
		/* Text to speech initialisation */
		Intent checkIntent = new Intent();
		checkIntent.setAction( TextToSpeech.Engine.ACTION_CHECK_TTS_DATA );
		startActivityForResult( checkIntent, REQUEST_LANGUAGE );
		/* Text to speech initialisation */
		
		//p = new Preferences( PreferenceManager.getDefaultSharedPreferences( this ));
		//Preferences.setPreferences( p );
	}
    @Override
	protected void onDestroy() {
		if (null != tts) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}
	@Override
	public void onPause() { super.onPause(); }
	@Override
	public void onResume() {
		super.onResume();
		
		// TODO: make o an Overlay static?
		if (null == Enguage.o || !Enguage.o.attached() || null == Enguage.e ) {
			Enguage.e = new Enguage( this );
			Enguage.e.loadConfig( Enguage.configFilename );
		}
		drawScreen();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu );
		return super.onCreateOptionsMenu( menu );
	}
	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		switch (item.getItemId()) {
		case R.id.appSettings :
			startActivity( new Intent( this, SettingsActivity.class ));
			return true;
		case R.id.repertoireHelp :
			startActivity( new Intent( this, HelpActivity.class ));
			return true;
		default:
			return super.onOptionsItemSelected(item);
	}	}
	public static final int REQUEST_SPEECH   =  1;
	public static final int REQUEST_LANGUAGE = 99;
	private ArrayList<String> newTextArr = null;
	private int newTextIdx = 0;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_LANGUAGE:
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				tts = new TextToSpeech(this, this);
				tts.setLanguage( Locale.UK );
			} else {
				// missing data? install it!
				Intent installIntent = new Intent();
				installIntent.setAction( TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA );
				startActivity( installIntent );
			}
			break;
		case REQUEST_SPEECH:
			if (resultCode == RESULT_OK && null != data) {
				newTextArr = data.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );
				newTextIdx = 0;
				if (!previewMode()) {
					interpret( newTextArr.get( newTextIdx ));
				} else {
					// all this bother is to get new text inserted at 
					// cursor and cursor placed at end of inserted text.
					/* this will need to change for  a cycle button
					 * TODO: create a function to work on editText with a given new text index
					 * need to remember the existing text, incase were cycling through a
					 * part utterance: a bog dog -> a bag dog -> a bug dog -> beg dog -> a big dog(!)
					 */
					String oldText = editText.getText().toString();
					int posnStart  = editText.getSelectionStart(), // typically...
						posnEnd    = editText.getSelectionEnd();   // ...these two will be the same
					String header  = oldText.substring( 0, posnStart ),
					       tailer  = oldText.substring( posnEnd );
					if (tailer.length()>0 && tailer.charAt(                 0 ) != ' ' ) tailer = " " + tailer;
					if (header.length()>0 && header.charAt( header.length()-1 ) != ' ' ) header = header + " ";
					editText.setText( header + newTextArr.get( newTextIdx ) + tailer );
					editText.setSelection( header.length() + newTextArr.get( newTextIdx ).length() );
	}	}	}	}
	public void onInit(int code) {
		initHelp( false );
		if (TextToSpeech.SUCCESS == code) ttsInitialised = true;
		// initiate the welcome message...
		if (firstRun || !visualMode()) {
			firstRun = false;
			tts.speak( this.getString( R.string.welcome ), TextToSpeech.QUEUE_ADD, null );
		}
		if (verboseMode())
			tts.speak( helpPrompt(), TextToSpeech.QUEUE_ADD, null );
	}

	public void interpret( String message ) {
		if (!message.equals("")) {
			
			message = Enguage.e.interpret(
							Shell.addTerminator( new Strings( message ))
					  );

			// Scrub user input if understood and if preview mode
			if (editText != null && Enguage.e.lastReplyWasUnderstood())
				editText.setText( "" );
			
			if (vocalised()) { // Say it
				tts.speak( message, TextToSpeech.QUEUE_ADD, null );
				/// this needs to be put into engine!!!
				if (verboseMode() && // we're showing help, and...
					!Enguage.e.lastReplyWasUnderstood() // ...we need help
				){	initHelp( true );
					tts.speak( helpPrompt(), TextToSpeech.QUEUE_ADD, null );
			}	}

			// screen may have changed due to commands issued...
			drawScreen();
			
			/* TODO:
			 * Add in here, if it was an ask (and not a reply)
			 * to initiate text-to-speech. presumably the reply 
			 * repertoire has been set up in engine.
			 */
	}	}
	private void drawScreen() {
		// remove edit text box - saving content
		LinearLayout ll = null;
		String editTextContent = "";
		if (editText != null) {
			editTextContent = editText.getText().toString();
			ll= (LinearLayout)findViewById( R.id.mainLayout );
			ll.removeViewAt( 1 );
			editText = null;
		}
		
		// remove all buttons
		ll = (LinearLayout)findViewById( R.id.llayout );
		ll.removeAllViews();
		
		LinearLayout.LayoutParams params;
		if (!visualMode() || (!previewMode() && !Repertoire.defaultRepIsLoaded())) {
			// one button in centre of screen
			params = new LinearLayout.LayoutParams( 150, 150, 1 );
			params.setMargins(50,100,50,100);
			params.gravity = Gravity.CENTER;
		} else {
			// button(s) at top of screen
			params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, //-- req API 8 */
	            LinearLayout.LayoutParams.WRAP_CONTENT,
	            1 ); // 1 is the weight -- will fill row with 1 or 3 buttons
		}
		

		// now rebuild screen
		// speak now buttons needed in both operating modes
		speakNow = new ImageButton( this );
		speakNow.setImageResource( android.R.drawable.ic_btn_speak_now );
		speakNow.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// pray silence...
				tts.speak( "", TextToSpeech.QUEUE_FLUSH, null );
				// start speak now dialogue...
				// TODO: localise this! Add this to settings
				Intent intent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH );
				intent.putExtra( RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-GB" );
				try {
					startActivityForResult( intent, REQUEST_SPEECH );
				} catch (ActivityNotFoundException a) {
					// force the device into preview mode...
					Preferences.getPreferences().set( previewMode, true );
					Toast.makeText(
						getApplicationContext(),
						"Sorry, your device doesn't support speech-to-text",
						Toast.LENGTH_SHORT
					).show();
		}	}	});
		ll.addView( speakNow, params );
		
		if (previewMode()) {
			cancel = new ImageButton( this );
			cancel.setImageResource( android.R.drawable.ic_delete );
			cancel.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					editText.setText( "" );
				}	});
			cancel.setLayoutParams( params );
			ll.addView( cancel, 0 ); // 1st
			
			okButton = new ImageButton( this );
			okButton.setImageResource( android.R.drawable.ic_media_play );
			okButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					interpret( editText.getText().toString() );
				}	});
			okButton.setLayoutParams( params );
			ll.addView( okButton ); // 3rd
			
			/* the idea behind this is that the speech-to-text software returns an array.
			 * so this button allows us to cycle through this array to find the best match.
			cycle = new ImageButton( this );
			cycle.setImageResource( android.R.drawable.ic_media_ffwd );
			cycle.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					interpret( editText.getText().toString() );
				}	});
			cycle.setLayoutParams( params );
			ll.addView( cycle ); // 3rd // */
			
			ll = (LinearLayout)findViewById( R.id.mainLayout );
			editText = new EditText( this );
			editText.setHint( R.string.edit_message );
			editText.setText( editTextContent ); // put it back
			editText.setTextSize( DeviceText.getSize() ); // size it
			ll.addView( editText, 1 ); // 1 => 2nd line
		}
		
		if (Repertoire.defaultRepIsLoaded())
			populateList();
	}
	private void populateList() {
		String text = "list text to go here";
		
		/*
		 * Enguage code...
		 */
		boolean wasOn = false;
		if (Audit.isOn()) { // nothing worth logging here, move along!
			wasOn = true;
			Audit.turnOff();
		}
		
		Item.format( "QUANTITY,UNIT of,,from FROM" );
		
		// subject variable is set in iNeed.txt 
		String subject = Variable.get( "subject", "_user" );
		
		Strings lines = new List( subject, "needs" ).get();
		lines.add( 0, subject + (lines.size() == 0 ? " does not need anything." : " needs:") );
		lines = Colloquial.applyOutgoingColloquia( lines ); // "_user does not..." => "You do not..."
		lines.set( 0, Language.capitalise( lines.get( 0 )));
		text = lines.toString( Strings.LINES );
		
		if (wasOn) Audit.turnOn();
		/*
		 * 
		 */
		
		TextView tv = ((TextView)findViewById( R.id.mainText ));
		tv.setText( text );
		tv.setTextSize( DeviceText.getSize() ); // was 24 -- a comfortable size on phone
		
}	}
