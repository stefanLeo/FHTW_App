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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import security.EasyX509TrustManager;

import fhtw.lvplan.data.Settings;

import android.content.Context;
import android.util.Log;

/**
 * Download Thread -> Downloads ical File from Server
 * @author Stefan Leonhartsberger
 *
 */
public class DownloadThread extends Thread{	
	private Settings set = new Settings();
	private Context ctx;
	public boolean ConnectionError = false;
	public boolean NoDownload = false;

	
	public DownloadThread(Settings set, Context ctx) {
		this.set = set;
		this.ctx = ctx;
		ConnectionError=false;
		this.setDaemon(true);
	}
	
	public void run(){
		FileOutputStream f 	= null;	
		InputStream in 		= null;
		HttpURLConnection c = null;
		
		try { 
				long start = System.currentTimeMillis() / 1000L;
        		long end = start + 31216961; // 1 year --> doesn't matter parsing stops 3 weeks after start
				
        		String fileURL = "https://cis.technikum-wien.at/cis/private/lvplan/stpl_kalender.php?type=student&pers_uid=" + set.getUsername() +"&ort_kurzbz=&stg_kz=&sem=&ver=&grp=&gruppe_kurzbz=&begin=" + start + "&ende=" + end + "&format=ical&version=2&target=ical";

		       	URL u = new URL(fileURL);
		        
		        
		        if(u.getProtocol().toLowerCase(Locale.getDefault()).equals("https")){
		        	trustAllHosts();
		        	HttpsURLConnection https = (HttpsURLConnection) u.openConnection();
		        	https.setHostnameVerifier(DO_NOT_VERIFY);
		        	c=https;
		        } else {
		        	c = (HttpURLConnection) u.openConnection();
		        }
		        c.setConnectTimeout(22000);
		        //c.setReadTimeout(1000);
		        
		        Authenticator.setDefault(new Authenticator(){
		        	 protected PasswordAuthentication getPasswordAuthentication() {
		        	        return new PasswordAuthentication (set.getUsername(), set.getPasswordAsArray());
		        	    }
		        });
		        
		        c.setRequestMethod("GET");
		        c.setDoOutput(true);
		        c.connect();
		        
		        //Log.d("LOAD", "Connected");
		        f = ctx.openFileOutput(DownloadManager.CAL_FILENAME, Context.MODE_PRIVATE);
		        
		        in = c.getInputStream();
		        
		        //Log.d("LOAD", "Writing");
		        byte[] buffer = new byte[1024];
		        int len1 = 0;
		        int downloadBytes=0;
		        
		        while ((len1 = in.read(buffer)) > 0) {
		            f.write(buffer, 0, len1);
		            downloadBytes+=len1;
		        }
		        
		        if(downloadBytes < 100) {
		        	NoDownload = true;
		        }                
		} catch(SocketTimeoutException e) {
			Log.d("ERROR LOAD", "Error: ", e);
		} catch (IOException e) {
		        Log.d("ERROR LOAD", "Error: " + e.getLocalizedMessage(), e);
		        ConnectionError = true;
		} catch(Exception e) {
			Log.d("ERROR LOAD", "Error: interrupt ", e);
			 ConnectionError = true;
		} finally{
			try{
				//CLEANUP
				if(in != null){
					in.close();
				}
				if(f != null){
					f.close();		
				}
				if(c != null){
					c.disconnect(); 
				}
			} catch(Exception ignore){}
		}
	}
		
   private static void trustAllHosts() {
            // Install the all-trusting trust manager
            try {
                    SSLContext sc = SSLContext.getInstance("TLS");
                    //sc.init(null, trustAllCerts, new java.security.SecureRandom());
                    sc.init(null, new TrustManager[] { new EasyX509TrustManager(null) }, new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                    e.printStackTrace();
            }
    }
        
   /** Called when the activity is first created. */
   private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                    return true;
            }
   };
}
