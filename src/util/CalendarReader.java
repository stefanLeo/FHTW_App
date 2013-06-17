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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import fhtw.lvplan.data.LvPlanEntries;

import android.content.Context;
import android.util.Log;

public class CalendarReader extends Thread {
	private static ArrayList<LvPlanEntries> readEntries = null;
	private static ArrayList<LvPlanEntries> readCalEntries = null;
	private static boolean THREAD_RUNNING = false;
	
	private static class Parameter {
		private static final String JUMP_MARKER		= "UID";
		private static final String SUMMARY			= "SUMMARY";
		private static final String LOCATION		= "LOCATION";
		private static final String START			= "DTSTART";
		private static final String END				= "DTEND";
		private static final String DESCRIPTION		= "DESCRIPTION";
		private static final Integer CRLF			= 10;
		private static final Integer FILE_END		= -1;
		private static final Integer LOAD_WEEKS		= 3;
	}
	
	private Context ctx;
	private boolean timeBoxed = true;
	
	public CalendarReader (Context ctx) {
		this.ctx = ctx;
		this.setDaemon(true);
	}
	
	public void run(){
		THREAD_RUNNING = true;
		readCalendar();
		THREAD_RUNNING = false;
	}
	
	public static List<LvPlanEntries> getLvPlanEntries() {
		if(!THREAD_RUNNING) {
			return readEntries;
		}
		return null;
	}
	
	public static List<LvPlanEntries> getLvPlanCalendarEntries() {
		if(!THREAD_RUNNING) {
			return readCalEntries;
		}
		return null;
	}
	
	public static void cleanUp(){
		try{
			if(readCalEntries != null) {
				readCalEntries.clear();
				readCalEntries = null;
			}
			if(readEntries != null) {
				readEntries.clear();
				readEntries = null;
			}
		} catch(Exception ex){
			Log.d("CR", "Error Clean Up", ex);
		}
	}
	
	public void setTimeBoxed(boolean timeBoxed) {
		this.timeBoxed = timeBoxed;
	}
	
	/**
	 * Reads Calendar from FILE 
	 * @return
	 */
    private void readCalendar() {		
    	if(timeBoxed){
    		readEntries = new ArrayList<LvPlanEntries>();
    	} else {
    		readCalEntries = new ArrayList<LvPlanEntries>();
    	}
    	
    	InputStreamReader in 	= null;
    	BufferedReader br 	 	= null;
    	FileInputStream fstream = null;
    	
    	//READ FILE AND PARSE
    	try { 
				GregorianCalendar now = new GregorianCalendar(Locale.GERMANY);
				GregorianCalendar last = new GregorianCalendar(Locale.GERMANY);
				last.add(Calendar.WEEK_OF_YEAR, Parameter.LOAD_WEEKS);
				
				fstream = ctx.openFileInput(DownloadManager.CAL_FILENAME);
				byte[] buffer = new byte[1024];
				byte[] jumpbuffer = new byte[21*16];

				fstream.read(jumpbuffer);
				jumpbuffer = new byte[200];
				
				LvPlanEntries entry = new LvPlanEntries();
			    int readEntriesCount = 0;

			    in = new InputStreamReader(fstream, "utf8");
			    br = new BufferedReader(in, buffer.length);
			    
			    String str;
			    while((str = br.readLine()) != null ){
					if(str.contains(Parameter.SUMMARY)) {
						entry.setSummary( str.substring(str.indexOf(":")+1, str.indexOf(" ")) );
						continue;
					}
					
					if(str.contains(Parameter.DESCRIPTION)){
						String prof = str.substring(str.indexOf("\\n")+2);
						prof = prof.substring(0, prof.indexOf("\\n"));
						Log.d("PROF", prof);
						entry.setProfessor(prof);
						continue;
					}
					
					if(str.contains(Parameter.LOCATION)) {
						entry.setLocation( str.substring(str.indexOf(":")+1, str.length()) );
						continue;
					}
					
					if(str.contains(Parameter.START)) {
						String fromRead = str.substring(str.indexOf(":")+1, str.length());
						entry.setFromDate( getCalenderFromString(fromRead) );
						continue;
					}
					
					if(str.contains(Parameter.END)) {    						
						String toRead = str.substring(str.indexOf(":")+1, str.length());	
						//Datumspruefung > Heute
						entry.setToDate( getCalenderFromString(toRead) );
						
						//Nur 3 Wochen nach vorne laden!! 
						if(timeBoxed && entry.getToDate().getTimeInMillis() > last.getTimeInMillis()) {
							if(readEntriesCount < 15) {
                                readEntries.add(entry);
                                readEntriesCount++;
                                entry = new LvPlanEntries();
                                continue;
                            } else {
                            	break;
                            }
						}
					
						if(entry.getToDate().getTimeInMillis() > now.getTimeInMillis()) {
							if(timeBoxed){           //Timeboxed == Read for InMap Usage
								readEntries.add(entry);
								readEntriesCount++;
					    	} else {                     //Read 4 Calendar
					    		readCalEntries.add(entry);
					    	}
						}
						entry = new LvPlanEntries();
					}
				}
			} catch(Exception ex) {
				Log.d("ERROR READ", "Calendar Reader", ex);
			} finally {
				if(br != null){
					try{
						br.close();
					} catch(Exception ignore){}
				}
				if(in != null){
					try{
						in.close();
					} catch(Exception ignore){}
				}
				if(fstream != null){
					try{
						fstream.close();
					}catch(Exception ignore){}
				}
			}
	}
    
    /**
     * Returns a gregorian calendar from a String
     * @param date
     * @return
     */
    private GregorianCalendar getCalenderFromString(String date) {
    	return new GregorianCalendar (Integer.parseInt(date.substring(0, 4)), 
    			Integer.parseInt(date.substring(4, 6)) - 1, 
    			Integer.parseInt(date.substring(6, 8)), 
    			Integer.parseInt(date.substring(9, 11)), 
    			Integer.parseInt(date.substring(11, 13)));
    }
			
}
