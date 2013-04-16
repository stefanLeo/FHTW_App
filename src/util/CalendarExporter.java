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

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.util.Log;
import fhtw.lvplan.data.LvPlanEntries;

public class CalendarExporter extends Thread {
	public static final int CAL_UPDATE		= 12;
	private static final String MARKER = "Created by LVPlan FHTW (C)";
	
	private String URI_CALENDAR ="content://";
	private Context appContext = null;
	private List<LvPlanEntries> lvPlanEntries = null;
	private Handler handler;
	public static int COUNT = 0;
	private boolean ics = false;
	private int calendarID = 0;
	
	
	public CalendarExporter(final Context curContext, final Handler handler){
		COUNT = 0;
		appContext = curContext;
		this.handler = handler;

        //TODO: ASK USER TO CHOOSE CALENDAR
		calendarID = 0;

		if(Build.VERSION.SDK_INT >= 15) {
			//Android 4
			ics = true;
		} else if( Build.VERSION.SDK_INT >= 8 ) {
		    URI_CALENDAR += "com.android.calendar/events";
		} else {
		    URI_CALENDAR += "calendar/events";
		}
		this.setDaemon(true);
	}
	
	public void run(){
		COUNT = 0;
		try {
			if(lvPlanEntries != null) {
				//Delete already created Entries:
				deleteAddedCalEntries();
				for(LvPlanEntries entry : lvPlanEntries) {
                    this.pushAppointmentsToCalender(entry);
        			//Log.d("EventID:"," " + eventId);
        			COUNT++;
        			sendMessage(CAL_UPDATE);
        			//if(COUNT > 5) break;
				}
			}
		} catch(Exception ex) {
			Log.d("Error while syncing Google Calendar", "", ex);
		}
	}
	
	/**
	 * Set the Entries to be synced
	 * @param lvPlanEntries
	 */
	public void setLvPlanEntries(List<LvPlanEntries> lvPlanEntries){
		this.lvPlanEntries = lvPlanEntries;
	}
	
	private long pushAppointmentsToCalender(final LvPlanEntries entry) {
		try{
			if(ics){
				
				return 0;
			} else {
				 ContentValues eventValues = new ContentValues();
				 //The _ID of the calendar the event belongs to --> Primary == 1
				 eventValues.put("calendar_id", 1); 
				 eventValues.put("title", entry.getSummary());
				 eventValues.put("description", MARKER); //used to find again !?!?
				 eventValues.put("eventLocation", entry.getLocation());
				 eventValues.put("dtstart", entry.getFromDate().getTimeInMillis() ); //The time the event starts in UTC millis since epoch
				 eventValues.put("dtend", entry.getToDate().getTimeInMillis() );
				 //eventValues.put("eventStatus", status);
				 eventValues.put("visibility", 0); // visibility to default (0),
				                                        // confidential (1), private
				                                        // (2), or public (3):
				 //eventValues.put("transparency", 0); // You can control whether
				                                        // an event consumes time
				                                        // opaque (0) or transparent
				                                        // (1).
				 eventValues.put("hasAlarm", 0); // 0 for false, 1 for true
				 Uri eventUri = appContext.getContentResolver().insert(Uri.parse(URI_CALENDAR), eventValues);
				 long eventID = Long.parseLong(eventUri.getLastPathSegment());
		
				 return eventID;
			}
		 } catch(Exception ex){
				Log.d("Add Event", "Error", ex);
				return -1;
		 }
	}
	
	/**
	 * DELETES ALL FHTW RELATED ENTRIES
	 */
	public boolean deleteAddedCalEntries(){
		try{
			if(ics){
				//TODO: use calendar ID
				//appContext.getContentResolver().delete(CalendarContract.Events.CONTENT_URI, "calendar_id=? and description=?", new String[]{String.valueOf(1), MARKER});
			} else {			
				appContext.getContentResolver().delete(Uri.parse(URI_CALENDAR), "calendar_id=? and description=?", new String[]{String.valueOf(1), MARKER});
			}
		}catch(Exception ex){
			Log.d("Delete Event", "Error", ex);
			return false;
		}
		return true;
	}
	
	/**
	 * Sends a message to LV_Plan_Activity handler
	 * @param what
	 */
	private void sendMessage(int what) {
		Message msg = new Message();
		msg.what=what;
		handler.sendMessage(msg);
	}
}
