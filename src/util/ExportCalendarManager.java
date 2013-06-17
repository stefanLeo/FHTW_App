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

import fhtw.lvplan.data.LvPlanEntries;
import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class ExportCalendarManager extends ManagerThread {
	private static final long TIMEOUT_GOOGLE_CAL = 60 * 1000; //1 minute
	public static final int CAL_OK 			= 10;
	public static final int CAL_ERROR		= 11;
	
	private CalendarExporter exporter;
	
	public ExportCalendarManager(final Context curContext, final  Handler handler, final FragmentManager fragmentManager){
		super(handler);
		exporter = new CalendarExporter(curContext, handler, fragmentManager);
		this.setDaemon(true);
	}
	
	public void run(){
		try{
			exporter.start();
			exporter.join(TIMEOUT_GOOGLE_CAL);
		} catch(Exception ex) {
			Log.d("Error while syncing Google Calendar", "", ex);
			sendMessage(CAL_ERROR);
		}
	}
	
	/**
	 * Set the Entries to be synced
	 * @param lvPlanEntries
	 */
	public void setLvPlanEntries(List<LvPlanEntries> lvPlanEntries){
		exporter.setLvPlanEntries(lvPlanEntries);
	}
	
	/**
	 * DELETE CALENDAR ENTRIES
	 */
	public boolean deleteCalEntries(){
		return exporter.deleteAddedCalEntries();
	}
}
