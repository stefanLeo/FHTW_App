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

import fhtw.lvplan.data.Settings;
import android.content.Context;
import android.os.Handler;

/**
 * DownloadManager handles DownloadThread errors/success
 * @author Stefan Leonhartsberger
 */
public class DownloadManager extends ManagerThread{
	public static final String CAL_FILENAME = "MyCalendar.ics";
	
	public static final int IS_ALIVE 		= 0;
	public static final int CONN_ERROR 		= 1;
	public static final int NOT_DOWNLOADED	= 2;
	public static final int OK_MSG 			= 3;
	
	private Settings set;
	private Context ctx;
	
	public DownloadManager(final Settings set, final Handler handler, final Context ctx){
		super(handler);
		this.set = set;
		this.ctx = ctx;
		this.setDaemon(true);
	}
	
	public void run(){
			DownloadThread download = new DownloadThread(set, ctx);
	    	download.start();
	    	
	    	//Aktuellen Thread sleepen
	    	try{
	    		download.join(25000);
	    	} catch(Exception ex){}
	
	    	if(download.isAlive()) { //WRONG CREDENTIALS --> LET IT DYE...
	    		download.interrupt();
	    		sendMessage(IS_ALIVE);
	    	} else if(download.ConnectionError) {
	    		sendMessage(CONN_ERROR);
	    	} else if(download.NoDownload) {
	    		sendMessage(NOT_DOWNLOADED);
	    	} else {
	    		sendMessage(OK_MSG);
	    	}
	    	
	    	download.interrupt();
	    	download=null;
		
	}
}

