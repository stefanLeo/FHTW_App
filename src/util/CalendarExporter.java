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

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import fhtw.lvplan.CalendarPickerDialog;
import fhtw.lvplan.data.LvPlanEntries;

public class CalendarExporter extends Thread {
	public static final int CAL_UPDATE		= 12;
	public static final int CAL_OK 			= 10;
	private static final String MARKER = "Created by LVPlan FHTW (C)";
	
	private String URI_CALENDAR ="content://";
	private Context appContext = null;
	private List<LvPlanEntries> lvPlanEntries = null;
	private Handler handler;
	public static int COUNT = 0;
	private boolean ics = false;
	private int calendarID = 0;
	private ContentResolver contentResolver = null;
	private FragmentManager fragmentManager = null;
	
	public CalendarExporter(final Context curContext, final Handler handler, final FragmentManager fragmentManger){
		COUNT = 0;
		appContext = curContext;
		this.handler = handler;

		calendarID = 1;
		this.fragmentManager = fragmentManger;
		this.contentResolver = curContext.getContentResolver();
		
		if(Build.VERSION.SDK_INT >= 14) {
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
				if(ics){
					this.requireCalendarID();
				} else {
					doUpdate();
				}
			}
		} catch(Exception ex) {
			Log.d("Error while syncing Google Calendar", "", ex);
		}
	}
	
	private void doUpdate(){
		//Delete already created Entries:
		deleteAddedCalEntries();
		for(LvPlanEntries entry : lvPlanEntries) {
            this.pushAppointmentsToCalender(entry);
			//Log.d("EventID:"," " + eventId);
			COUNT++;
			sendMessage(CAL_UPDATE);
		}
		sendMessage(CAL_OK);
	}
	
	/**
	 * Set the Entries to be synced
	 * @param lvPlanEntries
	 */
	public void setLvPlanEntries(List<LvPlanEntries> lvPlanEntries){
		this.lvPlanEntries = lvPlanEntries;
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private long pushAppointmentsToCalender(final LvPlanEntries entry) {
		try{
			if(ics){
				ContentValues eventValues = new ContentValues();
				 //The _ID of the calendar the event belongs to --> Primary == 1
				 eventValues.put("calendar_id", calendarID); 
				 eventValues.put(CalendarContract.Events.TITLE, entry.getSummary());
				 eventValues.put(CalendarContract.Events.DESCRIPTION, MARKER); //used to find again !?!?
				 eventValues.put(CalendarContract.Events.EVENT_LOCATION, entry.getLocation());
				 eventValues.put(CalendarContract.Events.DTSTART, entry.getFromDate().getTimeInMillis() ); //The time the event starts in UTC millis since epoch
				 eventValues.put(CalendarContract.Events.DTEND, entry.getToDate().getTimeInMillis() );
				 //eventValues.put("eventStatus", status);
				 //eventValues.put(CalendarContract.Events.VISIBLE, 0); // visibility to default (0),
				                                        // confidential (1), private
				                                        // (2), or public (3):
				 //eventValues.put("transparency", 0); // You can control whether
				                                        // an event consumes time
				                                        // opaque (0) or transparent
				                                        // (1).
				 eventValues.put(CalendarContract.Events.HAS_ALARM, 0); // 0 for false, 1 for true
				 TimeZone timeZone = TimeZone.getDefault();
				 eventValues.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
				 
				 Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues);
				 return Long.parseLong(uri.getLastPathSegment());
			} else {
				 ContentValues eventValues = new ContentValues();
				 //The _ID of the calendar the event belongs to --> Primary == 1
				 eventValues.put("calendar_id", calendarID); 
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
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public boolean deleteAddedCalEntries(){
		try{
			//It is currently deleting calendar entries in every Calendar
			if(ics){
				contentResolver.delete(CalendarContract.Events.CONTENT_URI, "description=?", new String[]{ MARKER});
			} else {	
				//contentResolver.delete(Uri.parse(URI_CALENDAR), "calendar_id=? and description=?", new String[]{String.valueOf(1), MARKER});
				contentResolver.delete(Uri.parse(URI_CALENDAR), "description=?", new String[]{MARKER});
			}
		}catch(Exception ex){
			Log.d("Delete Event", "Error", ex);
			return false;
		}
		return true;
	}
	
	/**
	 * Asks user for Calendar to be exported to ONLY if there are more than one calendar instances available
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void requireCalendarID(){
		if(fragmentManager == null){
			return;
		}
		Cursor calendarCursor = null;
		try{
			String[] projection = new String[] {
			       CalendarContract.Calendars._ID,
			       CalendarContract.Calendars.ACCOUNT_NAME,
			       CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
			       CalendarContract.Calendars.NAME,
			       CalendarContract.Calendars.CALENDAR_COLOR
			};
	
			calendarCursor = contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null);
			calendarCursor.moveToFirst();
			if(calendarCursor.isFirst() && calendarCursor.isLast()){
				//Only one calendar available... nothing to be selected...
				Log.d("CalendarCursor", "ONLY ONE calendar instance available");
				return;				
			} else {
				final List<Integer> ids			= new ArrayList<Integer>();
				final List<StringBuilder> sb 	= new ArrayList<StringBuilder>();
				final StringBuilder[] sbArray 	= new StringBuilder [1];
				
				while(!calendarCursor.isLast()){
					//only add calendars with different ids
					if(!ids.contains(calendarCursor.getInt(0))){
						sb.add(new StringBuilder(calendarCursor.getString(3)));
						ids.add(Integer.parseInt(calendarCursor.getString(0)));
					}
					calendarCursor.moveToNext();
				}
				
				final CalendarPickerDialog cpd = CalendarPickerDialog.newInstance(sb.toArray(sbArray), new CalendarChosenListener(){
					public void selected(int id) {
						calendarID = ids.get(id);
						Thread t = new Thread(){
							public void run(){
								doUpdate();
							}
						};
						t.start();
					}

					public void canceled() {
						return; 
					}
				});
				
				cpd.show(fragmentManager, "chooser");
			}
		} catch(Exception ex){
			Log.d("Require Calendar ID", "Error", ex);
			if(calendarCursor != null){
				calendarCursor.close();
			}
		}	
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
