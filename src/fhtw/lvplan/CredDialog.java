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

import util.SettingsManager;
import fhtw.lvplan.data.Settings;
import fhtw.lvplan.data.UpdateInterval;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class CredDialog extends Activity{	
	@Override
    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        	        
	        //SET LAYOUT
	        setContentView(R.layout.dialoglayout);
	        
	        //SET FOCUS
	        findViewById(R.id.txtUN).requestFocus();
    		
	        //BUTTONS
		    Button btnSave = (Button) findViewById(R.id.btn_Save);
	        Button btnClose = (Button) findViewById(R.id.btn_Close);
		    
	        /**
	         * SAVE BUTTON LISTENER
	         */
	        btnSave.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					EditText uid = (EditText) findViewById(R.id.txtUN);
				    EditText pw = (EditText) findViewById(R.id.txtPW);
	        		String updateInterval = ((Spinner) findViewById(R.id.spinner1)).getSelectedItem().toString();
				    Settings set = new Settings(uid.getText().toString(), pw.getText().toString(), updateInterval.toUpperCase());
				    
				    if(!SettingsManager.getInstance(null).saveSettings(set)) {
				    	Log.d("ERROR", "WRITE SETTINGS");
				    }
				    
				    Intent intent = new Intent(CredDialog.this, LV_PlanActivity.class);

				    LV_PlanActivity.DOWNLOADED = false;
				    startActivity(intent);
				    finish();
				}
				
			});
	        
	        /**
	         * CLOSE BUTTON LISTENER
	         */
	        btnClose.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					Intent intent = new Intent(CredDialog.this, LV_PlanActivity.class);
	                startActivity(intent);
	                finish();
				}
			});
	        
	        /**
	         * ComboBox Selection
	         */
	        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
	        // Create an ArrayAdapter using the string array and a default spinner layout
	        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.UpdateIntervals, android.R.layout.simple_spinner_item);
	        // Specify the layout to use when the list of choices appears
	        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        // Apply the adapter to the spinner
	        spinner.setAdapter(adapter);
	        //SEt default selection...
	        spinner.setSelection(UpdateInterval.DAILY.getId());
	        
	        /**
	         * Read settings and show them to the user
	         */
	        try{
	        	Settings settings = SettingsManager.getInstance(this.getBaseContext()).getSettingsInstance();
	        	if(settings != null) {
	        		((EditText) findViewById(R.id.txtUN)).setText(settings.getUsername());
	        		((EditText) findViewById(R.id.txtPW)).setText(settings.getPassword());
	        		spinner.setSelection(settings.getUpdateInterval().getId());
	        	}
	        }catch(Exception e){
	        	Log.d("CRED DIALOG", "SETINGS", e);
	        }
	}	
}
