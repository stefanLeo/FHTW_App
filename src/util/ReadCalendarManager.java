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

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class ReadCalendarManager extends ManagerThread {
	private static final long TIMEOUT_READ = 60 * 1000; //60 secs
	private CalendarReader reader;

	public static final int READ_OK 			= 20;
	public static final int READ_OK_CAL			= 21;
	public static final int READ_ERROR			= 22;
	private boolean read4Cal = false;
	
	public ReadCalendarManager(final Context curContext, final  Handler handler){
		super(handler);
		this.reader = new CalendarReader(curContext);
		this.setDaemon(true);
	}
	
	public void run(){
		try{
			reader.start();
			reader.join(TIMEOUT_READ);
			if(reader.isAlive()){
				sendMessage(READ_ERROR);
			}else if(read4Cal){
				sendMessage(READ_OK_CAL);	
			} else {
				sendMessage(READ_OK);	
			}
		} catch(Exception ex) {
			Log.d("Error while reading Calendar from FileSys", "", ex);
			sendMessage(READ_ERROR);
		}
	}
	
	public void setRead4Cal(boolean read4Cal){
		this.read4Cal = read4Cal;
		reader.setTimeBoxed(!read4Cal); //TIMEBOXED == FALSE IF READCALENDAR IS TRUE..
	}
	
	public boolean getRead4Cal(){
		return read4Cal;
	}
}
