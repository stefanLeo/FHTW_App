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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.GregorianCalendar;

import android.util.Base64;
import android.util.Log;

public class Settings implements Serializable {
	static final long serialVersionUID=12312;
	
	private String userName;
	private String password;
	private UpdateInterval updateInterval = UpdateInterval.DAILY;
	private GregorianCalendar lastUpdate;

	public Settings(String userName, String password) 
	{
		this(userName, password, UpdateInterval.DAILY);
	}
	
	public Settings(String userName, String password, UpdateInterval interval) 
	{
		setUsername(userName);
		setPassword(password);
		setUpdateInterval(interval);
	}
	
	public Settings(String userName, String password, String interval) 
	{
		this(userName, password);
		setUpdateInterval(interval);
	}
	
	public Settings(){}
	
	public void setUsername(String userName){
		this.userName = userName;
	}
	
	public String getUsername(){
		return userName;
	}
	
	public void setPassword(String password){
		this.password = password;
	}
	
	public String getPassword(){
		return password;
	}
	
	public char[] getPasswordAsArray(){
		return password.toCharArray();
	}
	
	public UpdateInterval getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(UpdateInterval updateInterval) {
		this.updateInterval = updateInterval;
	}
	
	public void setUpdateInterval(String updateInterval) {
		this.updateInterval = UpdateInterval.valueOf(updateInterval);
	}
	
	public GregorianCalendar getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(GregorianCalendar lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	/**
	 * Creates Base64 representation of this object
	 */
	public String toString() {
		ObjectOutputStream oos 		= null;
		ByteArrayOutputStream baos 	= null;
		try{
	        baos = new ByteArrayOutputStream();
	        oos  = new ObjectOutputStream( baos );
	        oos.writeObject( this);
	        return Base64.encodeToString( baos.toByteArray(), Base64.DEFAULT);
		} catch(Exception e){
			Log.d("Settings", "toString", e);
		} finally{
			try{
				if(oos != null){
					oos.close();
				}
				if(baos != null){
					baos.close();
				}
			} catch(Exception ignore) {}
		}
		return "";
    }
	
	/** 
	 * Read the object from Base64 string. 
	 * */
    public static Settings fromString( String s ) {
    	ObjectInputStream ois = null;
    	try{
	        byte [] data = Base64.decode(s, Base64.DEFAULT);
	        ois = new ObjectInputStream( new ByteArrayInputStream( data ) );
	        Settings set = (Settings)ois.readObject();
	        return set;
    	} catch(Exception e){
			Log.d("Settings", "fromString", e);
		} finally {
			if(ois != null){
				try{
					ois.close();
				} catch(Exception ignore){}
			}
		}
		return null;
    }
}
