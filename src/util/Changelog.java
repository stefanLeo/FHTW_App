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

import fhtw.lvplan.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

public class Changelog {
	
	private static String KEY_CHANGELOG_VERSION_VIEWED = "changelogVersion";
	private static String PREFS_NAME = "fhtwPreferences";
	
	public static boolean verifyVersionChanged(final Context ctx){
        //evaluate if we will show changelog
	    try {
	        //current version
	        PackageInfo packageInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
	        int versionCode = packageInfo.versionCode; 
	
	        //version where changelog has been viewed
	        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
	        int viewedChangelogVersion = settings.getInt(KEY_CHANGELOG_VERSION_VIEWED, 0);
	
	        if(viewedChangelogVersion<versionCode) {
	            Editor editor=settings.edit();
	            editor.putInt(KEY_CHANGELOG_VERSION_VIEWED, versionCode);
	            editor.commit();
	            return true;
	        }
	    } catch (NameNotFoundException e) {
	        Log.w("Unable to get version code. Will not show changelog", e);
	    }
	    return false;
	}
	
	public static void viewChangelog(final Activity ctx){
		LayoutInflater li = LayoutInflater.from(ctx);
	    View view = li.inflate(R.layout.changelog_view, null);

	    new AlertDialog.Builder(ctx)
	    .setTitle("Changelog")
	    .setIcon(android.R.drawable.ic_menu_info_details)
	    .setView(view)
	    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
	      public void onClick(DialogInterface dialog, int whichButton) {
	          //Close the dialog
	    	  dialog.cancel();
	      }
	    }).show();
	}
	
	
}
