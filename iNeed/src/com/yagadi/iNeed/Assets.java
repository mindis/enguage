package com.yagadi.iNeed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import android.content.res.AssetManager;

import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Filesystem;

public class Assets {
	static public Audit audit = new Audit( "Assets" );
	
	// this is Android specific!
	static public String stringFromFileOrAsset( AssetManager am, String fname ) {
		String helpText;
		String fullName = 
				Filesystem.root + File.separator
				+ "yagadi.com"  + File.separator
				+ fname;
		//audit.audit( "config file:"+ (null == ctx ? fname : fullName ));
		//prefer user data over assets
		if (new File( fullName ).exists())
			helpText = Filesystem.stringFromFile( fullName );
		else {
			//AssetManager am = this.getAssets();
			try {
				InputStream is = am.open( fname );
				helpText = Filesystem.stringFromStream( is );
				is.close();
			} catch (IOException e) {
				audit.ERROR( "no help text found in asset "+ fname );
				helpText = ""; //"no help text found in asset "+ fname;
		}	}
		return helpText;
}	}
