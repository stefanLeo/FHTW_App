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
package fhtw.lvplan;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import fhtw.lvplan.data.LvPlanEntries;
import fhtw.lvplan.data.Settings;

import util.CalendarExporter;
import util.Changelog;
import util.ExportCalendarManager;
import util.CalendarReader;
import util.DownloadManager;
import util.ReadCalendarManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import util.SettingsManager;

public class LV_PlanActivity extends FragmentActivity {
	public static final int PROGRESS_LOAD_DIALOG 	= 0;
	public static final int PROGRESS_CAL_DIALOG 	= 1;
	public static final int PROGRESS_READ_DIALOG 	= 2;
	private static int readLvPlanEntries = 0;
	public 	static boolean DOWNLOADED = true;
	
	private static int CountDownloadAttempts = 0;
	
	private List<String> groups = new ArrayList<String>();
	private List<String[]> children = new ArrayList<String[]>();
	private ExpandableListView epView;
	private ExpandableListAdapter mAdapter;
	private DialogFragment newDF = null;
	
	private DownloadManager download;
	private ReadCalendarManager calendarReader;
	private ExportCalendarManager calendarWriter;
	
	private Handler handler = new DownloadMsgHandler(this); 
	
	// handler for the background updating
	static class DownloadMsgHandler extends Handler {
		private LV_PlanActivity main_activity;
		
		DownloadMsgHandler(LV_PlanActivity main_activity) {
			this.main_activity = main_activity;
		}
		
		@Override
		public void handleMessage(Message msg) {
			try{
				 switch(msg.what)
				 {
					 case DownloadManager.IS_ALIVE:  											//Thread stuerzt ab--> falsche Credentials
						 	//main_activity.dismissDialog(PROGRESS_LOAD_DIALOG);	
						 	main_activity.dismissProgressDialog();
						 	DOWNLOADED = false;
						 	Toast.makeText(main_activity.getBaseContext(), 
	        	                    "Error, Please enter valid Credentials (Username & PW)", 
	        	                    Toast.LENGTH_LONG).show();
						 	main_activity.startCredDialog();
					 		break;
					 case DownloadManager.CONN_ERROR:											// Connection error
						 	main_activity.dismissProgressDialog();
							Toast.makeText(main_activity.getBaseContext(), "Error, No Connection!!",Toast.LENGTH_LONG).show();
							DOWNLOADED = false;
						 	break;
					 case DownloadManager.NOT_DOWNLOADED:										//NO Download --> Restart
					 	 	DOWNLOADED = false;
						 	CountDownloadAttempts++;
		    				if(CountDownloadAttempts > 3) {	
		    					Toast.makeText(main_activity.getBaseContext(), 
		        	                    "Error, FHTW Server not reachable.\nPlease try later!!", 
		        	                    Toast.LENGTH_LONG).show();
		    					CountDownloadAttempts = 0;
		    					main_activity.dismissProgressDialog();
		    					break;
		    				}
		    				main_activity.DownloadFromUrl();
		    				break;
					 case DownloadManager.OK_MSG: 												//OK -- REFRESH UI 
						 DOWNLOADED = true;
						 main_activity.dismissProgressDialog();
						 CalendarReader.cleanUp();
						 main_activity.ReadCalender();
						 main_activity.UpdateUI();	
						 Toast.makeText(main_activity.getBaseContext(), "Refreshed Successfully!", Toast.LENGTH_LONG).show();
						 //Write current date to settings file...
						 SettingsManager.getInstance(null).getSettingsInstance().setLastUpdate(new GregorianCalendar());
						 SettingsManager.getInstance(null).saveSettings();
						 break;
					 case CalendarExporter.CAL_OK:										//CAL SYNC SUCCESSFULL
						 main_activity.dismissProgressDialog();
						 CalendarExporter.COUNT = 0;
						 //main_activity.updateProgress();
						 break;
					 case ExportCalendarManager.CAL_ERROR:									//CAL SYNC TIMEOUT OR ERROR
						 main_activity.dismissProgressDialog();
						 CalendarExporter.COUNT = 0;
						 Toast.makeText(main_activity.getBaseContext(), "ERROR while syncronising Calendar", Toast.LENGTH_LONG).show();
						 //main_activity.updateProgress();
						 break;
					 case CalendarExporter.CAL_UPDATE:										//UPDATE PROGRESS DIALOG ENTRIES
						 main_activity.updateProgress();
						 break;
					 case ReadCalendarManager.READ_OK:											//UPDATE UI WITH CALENDAR
						 main_activity.dismissProgressDialog();
						 main_activity.UpateCalendarUI(CalendarReader.getLvPlanEntries());
						 break;
					 case ReadCalendarManager.READ_OK_CAL:										//UPDATE GOOGLE CAL
						 main_activity.dismissProgressDialog();
						 main_activity.syncGoogleCal(CalendarReader.getLvPlanCalendarEntries());
						 break;
					 case ReadCalendarManager.READ_ERROR:										//UPDATE READ ERROR
						 main_activity.dismissProgressDialog();
						 Toast.makeText(main_activity.getBaseContext(), "ERROR while syncronising Calendar", Toast.LENGTH_LONG).show();
						 break;
					default:
						 Log.d("Handle Msgs", "Unkown Message");
						 main_activity.dismissProgressDialog();
						 break;
				 }
			 } catch(Exception ex) { //DUE TO REPORTED BUG....
				 Log.d("Handle Msg", "ERROR", ex);
				 main_activity.dismissProgressDialog();
			 }
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	/*View title = getWindow().findViewById(android.R.id.title);
    	View titleBar = (View) title.getParent();
    	titleBar.setBackgroundColor(Color.parseColor("#0087c7"));*/
    	
    	try{
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.main);
	        epView = (ExpandableListView) findViewById(R.id.expandableListView1);
	        
        
	        Settings set = SettingsManager.getInstance(this.getBaseContext()).getSettingsInstance();
	        //Read Credentials if existing, if not --> Open Credentials Dialog to create settings
	        if(set == null) {
	        	CredDialog.showChangelog = true;
	        	startCredDialog();
		    } else {
		        	//Check if Calendar already downloaded... 
		        	//Downloaded is true per default
		        	if(DOWNLOADED) {
		        		if(set.getLastUpdate() != null) {
		        			GregorianCalendar now = new GregorianCalendar();
			        		//Validate Update Interval settings.
			        		switch(set.getUpdateInterval()){
				        		case STARTUP:
				        			DownloadFromUrl();
				        			break;
				        		case DAILY:
				        			long delta = now.getTimeInMillis() - set.getLastUpdate().getTimeInMillis();
				        			if(delta >= 24*60*60*100) {
				        				DownloadFromUrl();
				        			} else if(delta > 0) {
				        				if(now.get(Calendar.DAY_OF_YEAR) > set.getLastUpdate().get(Calendar.DAY_OF_YEAR)) {
				        					DownloadFromUrl();
				        				}
				        			}
				        			break;
				        		case MONTHLY:
				        			if(now.get(Calendar.YEAR) > set.getLastUpdate().get(Calendar.YEAR)) {
				        				DownloadFromUrl();
				        			} else if(now.get(Calendar.MONTH) > set.getLastUpdate().get(Calendar.MONTH)) {
				        				DownloadFromUrl();
				        			}
	
				        			break;
				        		default: // MANUAL do nothing
				        			break;
			        		}
		        		}
		        		
		        		List<LvPlanEntries> entries = CalendarReader.getLvPlanEntries();
			    		if( entries == null ) {
			    			ReadCalender(); //Reread if there aren't any
			    		} else {
			    			this.UpateCalendarUI(entries);
			    		}
		        	} else {
		        		DownloadFromUrl();
		        	}
			    //Verify if newer version installed
		        if(Changelog.verifyVersionChanged(this)){
		        	Changelog.viewChangelog(this);
		        }
		    }
	        	        
	        //Init List
	        mAdapter = new MyExpandableListAdapter(this, groups, children);
	        epView.setAdapter(mAdapter);
	        	        
    	} catch(Exception ex) {
    		Log.d("OnCreate", "Exception: ", ex);
    	}
    }
    
    /**
     * Downloads the calendar
     */
    private void DownloadFromUrl() {  //this is the downloader method  
    	/*if(progressDialog != null) {
    	   if(!progressDialog.isShowing()) {
    			showProgressDialog(PROGRESS_LOAD_DIALOG);
    	   }
    	} else {*/
    		showProgressDialog(PROGRESS_LOAD_DIALOG);
    	//}
    	Settings tmpSettings = SettingsManager.getInstance(this.getBaseContext()).getSettingsInstance();
    	if(tmpSettings != null){
	    	//Thread starten
	    	download = new DownloadManager(tmpSettings, handler, this.getApplicationContext());
	    	download.start();
    	} else {
    		Toast.makeText(getBaseContext(), 
					"No Settings found!\nPlease enter Username & Passord!", 
    	            Toast.LENGTH_LONG).show();
    		startCredDialog();
    	}
    }

    /**
     * Read Calendar without Cal Option
     */
    public void ReadCalender() { 
    	ReadCalender(false);
    }
    
    /**
     * Reads & Parses Calendar File
     */
    private void ReadCalender(boolean read4Cal) { 
    	/*if(progressDialog != null) {
	    	   if(!progressDialog.isShowing()) {
	    		   showProgressDialog(PROGRESS_READ_DIALOG);
	    			//showDialog(PROGRESS_READ_DIALOG);
	    	   }
		} else {*/
			showProgressDialog(PROGRESS_READ_DIALOG);
	    		//showDialog(PROGRESS_READ_DIALOG);	
		//}
        calendarReader = new ReadCalendarManager(this.getBaseContext(), handler);
        calendarReader.setRead4Cal(read4Cal);
    	calendarReader.start();
	}
    
    /**
     * Updates UI with Read LvPlan Entries
     * @param lvPlan
     */
    public void UpateCalendarUI(List<LvPlanEntries> lvPlan){
    	groups = new ArrayList<String>();
    	children = new ArrayList<String[]>();
    	if(lvPlan != null) {
	    	if(lvPlan.size() > 0) {
	    		for(LvPlanEntries entry : lvPlan) {
	    			if(groups.size() == 0) {
	    				groups.add(entry.getDate());
		    			String [] tmp = { entry.toString() };
		    			children.add(tmp);
	    			}else if(groups.contains(entry.getDate())) { //Verify if last entry is already in list...
		    			String [] tmp1 = children.get(children.size()-1);
		    			String [] added = new String[tmp1.length + 1];
	
		    			System.arraycopy (tmp1, 0, added, 0, tmp1.length);
		    			added[tmp1.length] = entry.toString();			    							
	
		    			children.remove((children.size()-1));
		    			children.add(added);
	    			} else {
		    			groups.add(entry.getDate());
		    			String [] tmp = { entry.toString() };
		    			children.add(tmp);
	    			}
	    		}
	    		UpdateUI();
		        return;
	    	} 
    	} else {
    		//TODO: ADD HOLIDAY PICTURE TO BACKGROUND??
			Toast.makeText(getBaseContext(), 
						"No LV's found!\nSMELLS LIKE VACATION :-)", 
	    	            Toast.LENGTH_LONG).show();
    	}
		this.startCredDialog();
    }
    
    /**
     * Creates options menu 
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.menu, menu);
        return true;
    }
    
    /**
     * When an option from options menu was selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	try{
	    	switch(item.getItemId()){
	    	case R.id.refresh: 
	    		//REFRESH
	    		DownloadFromUrl();
	    		return true;
	    	case R.id.properties:
	    		//open Settings 
	    		Intent intent = new Intent(LV_PlanActivity.this, CredDialog.class);
	            startActivity(intent);
	    		finish();
	    		return true;
	    	case R.id.googlecal:
	    		//Read Calendar
	    		List<LvPlanEntries> entries = CalendarReader.getLvPlanCalendarEntries();
	    		if( entries == null ) {
	    			ReadCalender(true); //Reread if there aren't any --> REREAD ALL!!
	    		} else {
	    			this.syncGoogleCal(entries);	//Take already read ones
	    		}
	    		return true;
	    	case R.id.delgooglecal:
	    		calendarWriter = new ExportCalendarManager(this.getBaseContext(), handler, this.getSupportFragmentManager());
	    		if(calendarWriter.deleteCalEntries()) {
		    		Toast.makeText(getBaseContext(), 
							"Calendar Entries deleted", 
		    	            Toast.LENGTH_LONG).show();
	    		} else {
	    			Toast.makeText(getBaseContext(), 
							"ERROR deleting Calendar entries", 
		    	            Toast.LENGTH_LONG).show();
	    		}
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    	}
    	} catch(Exception ex) {
    		Log.d("Error in menu", "", ex);
    		return false;
    	}
    }
    
    /**
     * Syncs the list to calendar
     * @param lvPlan
     */
    public void syncGoogleCal(List<LvPlanEntries> lvPlan) {
    	if(lvPlan == null){
    		Toast.makeText(getBaseContext(), 
					"No Calendar file found!\nRefresh!", 
    	            Toast.LENGTH_LONG).show();
			this.DownloadFromUrl();
    		return;
    	}else if(lvPlan.size() == 0) {
			Toast.makeText(getBaseContext(), 
					"Calendar File empty!\nRefresh!",
    	            Toast.LENGTH_LONG).show();
			this.DownloadFromUrl();
		} else {
			/*if(progressDialog != null) {
		    	   if(!progressDialog.isShowing()) {
		    		   showProgressDialog(PROGRESS_CAL_DIALOG);
		    	   }
			} else {*/
		    		showProgressDialog(PROGRESS_CAL_DIALOG);	
			//}
			readLvPlanEntries = lvPlan.size();
			//Sync to Google Calendar
			calendarWriter = new ExportCalendarManager(this.getBaseContext(), handler, this.getSupportFragmentManager());
			calendarWriter.setLvPlanEntries(lvPlan);
			calendarWriter.start();
		}
    }
    
    /**
     * Reads Calendar and refreshes GUI afterwards
     */
    private void UpdateUI() {
        mAdapter = new MyExpandableListAdapter(this, groups, children);
        epView.setAdapter(mAdapter);
        this.onContentChanged();
    }
    
    
    /**
     * Dismiss DialogFactory Dialogs
     */
    void dismissProgressDialog() {
    	Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
    	if(prev != null){
    		if(prev instanceof MyProgressDialog){
    			((MyProgressDialog)prev).dismiss();
    		}
    	}
    }
    
    
    /**
     * created Progress Dialog....
     */
    void showProgressDialog(final int id){
    	newDF = MyProgressDialog.newInstance(id);
    	newDF.show(getSupportFragmentManager(), "dialog");
    }
    
    /**
     * Updates progress of google sync
     */
    public void updateProgress(){
    	if(newDF != null &&  newDF instanceof MyProgressDialog){
    		((MyProgressDialog)newDF).updateProgress(CalendarExporter.COUNT, readLvPlanEntries);
    	}
    }
    
    /*
    protected Dialog onCreateDialog(int id) {
        switch(id) {
	        case PROGRESS_LOAD_DIALOG:
	            progressDialog = new ProgressDialog(LV_PlanActivity.this);
	            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            progressDialog.setMessage("Loading...");
	            return progressDialog;
	        case PROGRESS_CAL_DIALOG:
	        	progressDialog = new ProgressDialog(LV_PlanActivity.this);
	            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            progressDialog.setMessage("Writing entries to Calendar: 0 / " + readLvPlanEntries);
	            return progressDialog;
	        case PROGRESS_READ_DIALOG:
	        	progressDialog = new ProgressDialog(LV_PlanActivity.this);
	            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            progressDialog.setMessage("Reading Calendar-File...");
	            return progressDialog;    
	        default:
	            return null;
        }
    }/ 
    
    /**
     * Updates progress of google sync
     *
    public void updateProgress(){
    	progressDialog.setMessage("Writing entries to Calendar: " + CalendarExporter.COUNT +" / " + readLvPlanEntries);
    }/
        
    /**
     * Closes this Activity and starts CredDialog
     */
    public void startCredDialog(){
    	Intent intent = new Intent(LV_PlanActivity.this, CredDialog.class);
        startActivity(intent);
        finish();
    }
    
    public static synchronized int getReadLvPlanEntries(){
    	return readLvPlanEntries;
    }
    
}
    
    

