package fhtw.lvplan;

import util.CalendarChosenListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class CalendarPickerDialog extends DialogFragment {
	
	private CharSequence[] dialogList;
	private int CalendarID = -1;
	private CalendarChosenListener calendarListener;
	
	public static CalendarPickerDialog newInstance(StringBuilder[] dialogList, CalendarChosenListener calendarListener ){
		CalendarPickerDialog  cpd = new CalendarPickerDialog();
		cpd.setCalendars(dialogList);
		cpd.calendarListener = calendarListener;
		return cpd;
	}
	
	public void setCalendars(StringBuilder[] dialogList){
		this.dialogList = dialogList;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle(R.string.CalendarChooser)
	           .setItems(dialogList, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int which) {
	            	   // The 'which' argument contains the index position of the selected item
	            	   CalendarID = which;
	            	   dismiss();
	           }
	    });
	    return builder.create();
	}
	
	@Override
	public void dismiss(){
		getDialog().dismiss();
		if(CalendarID != -1){
			calendarListener.selected(CalendarID);
		} else {
			calendarListener.canceled();
		}
	}

	public int getCalendarID(){
		return CalendarID;
	}

}
