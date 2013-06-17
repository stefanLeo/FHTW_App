/**
 * FHTW App – CIS Plattform of the UAS Technikum Vienna 4 Android devices
 * Copyright (C) 2013  Stefan Leonhartsberger
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * License can be found in project root: LICENSE
 */
package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import security.StringEncrypter;
import android.content.Context;
import android.util.Log;
import fhtw.lvplan.data.Settings;

public class SettingsManager {
	private static final String SETTINGS_FILE = "Settings.set";
	private static final String SETTINGS_KEY = "y8Z80XFdFU8q2sQZnP033hMAkcu3KtP83DQea66SFbvY4heb24JPPwI3a8GNPTG";
	
	private Context ctx;
	
	public SettingsManager(Context ctx){
		this.ctx = ctx;
	}
	
	/**
	 * Singleton
	 */
	private static Settings settings = null; 
	private static SettingsManager settingsManager = null;
	
	public static SettingsManager getInstance(Context ctx){
		if(settingsManager == null){
			settingsManager = new SettingsManager(ctx);
		}
		return settingsManager;
	}
	
	public Settings getSettingsInstance(){
        if(settings == null) {
        	if(!readSettings()) {
        		settings = null;
        	}
        }
        return settings;
    }		
	
	/**
     * Reads credentials from Settings file and writes them to local settings object
     * @return
     */
    private boolean readSettings()
    {
    	FileInputStream fstream = null;
    	try{
    		File settingsFile = new File(SETTINGS_FILE);
    		
    		if(!settingsFile.exists()){
	    		fstream = ctx.openFileInput(SETTINGS_FILE);
	    		StringEncrypter cipher = new StringEncrypter(SETTINGS_KEY);
	    		byte[] buffer = new byte[200];
	    		byte[] totalBuffer = null;
	    		int count = 0;
	    		
	    		while((count = fstream.read(buffer)) != -1) {
	    			if(totalBuffer != null){
	    				byte[] tmpArray = new byte[totalBuffer.length + count];
	    				System.arraycopy(totalBuffer, 	0, tmpArray, 0, 					totalBuffer.length);
	    				System.arraycopy(buffer, 		0, tmpArray, totalBuffer.length, 	count);
	    				totalBuffer = null;
	    				totalBuffer = tmpArray.clone();
	    				tmpArray 	= null;
	    			} else {
	    				totalBuffer = buffer.clone();
	    			}			
	    		}  
	    		String read = cipher.decrypt(totalBuffer);
				settings = Settings.fromString(read);
    		} else {
    			Log.d("Verify Settings file exists", "Not existing");
    			return false;
    		}
    	} catch(Exception ex){
    		Log.d("Error", "Read Settings", ex);
    		return false;
    	} finally{
    		if(fstream != null){
    			try{
    				fstream.close();
    			} catch(Exception ignore){}
    		}
    	}
    	
    	return true;
    }
    
    /**
     * SAVE Settings to FILE
     * @param set
     * @return
     */
    public boolean saveSettings(Settings set) {		
    	FileOutputStream f = null;
    	try {
			 f = ctx.openFileOutput(SETTINGS_FILE, Context.MODE_PRIVATE);
            
			//Encrypted Output
			StringEncrypter cipher = new StringEncrypter(SETTINGS_KEY);
			
			//byte[] buffer = cipher.encrypt((set.getUsername() + "@" + set.getPassword()));
			byte[] buffer = cipher.encrypt(set.toString());
			f.write(buffer, 0, buffer.length);

			settings = set;
		} catch ( IOException e ) { 
			Log.d("Error", "Saving Settings", e); 
			return false;
		} finally{
			try{
				if(f != null){
					f.close();
				}
			} catch(Exception ignore){}
		}
		return true;
	}
    
    public boolean saveSettings(){
    	return saveSettings(this.getSettingsInstance());
	}
    
}
