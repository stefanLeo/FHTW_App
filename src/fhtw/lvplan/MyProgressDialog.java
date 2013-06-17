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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;



public class MyProgressDialog extends DialogFragment {
	private int id = 0;
	private ProgressDialog progressDialog;
	
	public static MyProgressDialog newInstance(final int id){
		MyProgressDialog myPD = new MyProgressDialog();
		myPD.setId(id);
		return myPD;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		switch(id) {
	        case LV_PlanActivity.PROGRESS_LOAD_DIALOG:
	            progressDialog.setMessage("Loading...");
	            return progressDialog;
	        case LV_PlanActivity.PROGRESS_CAL_DIALOG:
	            progressDialog.setMessage("Writing entries to Calendar: 0 / " + LV_PlanActivity.getReadLvPlanEntries());
	            return progressDialog;
	        case LV_PlanActivity.PROGRESS_READ_DIALOG:
	            progressDialog.setMessage("Reading Calendar-File...");
	            return progressDialog;    
	        default:
	            return null;
		}
	}
	
	public void dismiss(){
		getDialog().dismiss();
	}
	
	public void setId(final int id){
		this.id = id;
	}
	
	/**
     * Updates progress of google sync
     */
    public void updateProgress(final int current, final int total){
    	progressDialog.setMessage("Writing entries to Calendar: " + current +" / " + total);
    }
}
