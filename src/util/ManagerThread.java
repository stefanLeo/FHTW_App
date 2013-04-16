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

import android.os.Handler;
import android.os.Message;

public class ManagerThread extends Thread {
	
	private Handler handler;
	
	public ManagerThread(final Handler handler){
		this.handler = handler;
	}
	
	/**
	 * Sends a message to LV_Plan_Activity handler
	 * @param what
	 */
	protected void sendMessage(int what) {
		Message msg = new Message();
		msg.what=what;
		handler.sendMessage(msg);
	}
}
