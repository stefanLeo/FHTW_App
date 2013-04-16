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
package fhtw.lvplan.data;

import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class LvPlanEntries {

	DecimalFormat df = new DecimalFormat("00");
	
	private String date;

	private String summary;
	
	private String location;
	
	private GregorianCalendar fromTime;
	
	private GregorianCalendar toTime;
	
	public LvPlanEntries(){}
	
	private String[] weekdays = new DateFormatSymbols().getWeekdays(); 
	
	public LvPlanEntries(String date, String summary, String location, GregorianCalendar fromDate, GregorianCalendar toDate) {
		this.date = date;
		this.summary = summary;
		this.location = location;
		this.fromTime = fromDate;
		this.toTime = toDate;		
	}
	
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public GregorianCalendar getFromDate() {
		return fromTime;
	}

	public void setFromDate(GregorianCalendar fromDate) {
		this.date = "     " + weekdays[fromDate.get(Calendar.DAY_OF_WEEK)] + "  - " + 
								df.format(fromDate.get(Calendar.DAY_OF_MONTH)) + "." + 
								df.format((fromDate.get(Calendar.MONTH)+1)) + "." + 
								fromDate.get(Calendar.YEAR);
		this.fromTime = fromDate;
	}

	public GregorianCalendar getToDate() {
		return toTime;
	}

	public void setToDate(GregorianCalendar toDate) {
		this.toTime = toDate;
	}
	
	public String getFormattedToDate(){
		return getFormattedDate(toTime);
	}
	
	public String getFormattedFromDate(){
		return getFormattedDate(fromTime);
	}
	
	private String getFormattedDate(GregorianCalendar calendar) {
		try{
			return df.format(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + df.format(calendar.get(Calendar.MINUTE));
		} catch (NullPointerException ex) {
			return "empty";
		}
	}
	
	@Override
	public String toString() {
		return this.getSummary() +" - " + this.getLocation() + "\nFrom: " + this.getFormattedFromDate() + "  To: " + this.getFormattedToDate();
	}
}
