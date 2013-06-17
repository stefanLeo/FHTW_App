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
package security;

import java.security.MessageDigest;

import javax.crypto.Cipher;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

public class StringEncrypter {

	Cipher cipher;
	SecretKey key;
	
	public StringEncrypter(String password) {
		try{
			key = generateKey(password, "AES");
			cipher= Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch(Exception ex) {
			Log.d("Error", ex.getMessage());
		}
	}	
	
	/**
	 * Generates Key with correct length for AES encryption
	 * @param password
	 * @param algorithm
	 * @return
	 */
	private SecretKeySpec generateKey(final String password, final String algorithm){
        try{
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.update(password.getBytes("UTF-8"));
            byte[] key = digester.digest();
            return new SecretKeySpec(key, algorithm);
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
	
	/**
	 * Takes a single String as an argument and returns an Encrypted version
	 * of that String.
	 * @param str String to be encrypted
	 * @return <code>String</code> Encrypted version of the provided String
	 */
	public byte[] encrypt(String str) {
		try{
			cipher.init(Cipher.ENCRYPT_MODE, key);
			//byte[] utf8Bytes = str.getBytes("UTF8");
			return cipher.doFinal(Base64.encode(str.getBytes(), Base64.DEFAULT));
		} catch(Exception ex) {
			Log.d("Error Encrypt", "Exception", ex);
		}
		return null;
	}


	/**
	 * Takes a encrypted String as an argument, decrypts and returns the decrypted String.
	 * @param str Encrypted String to be decrypted
	 * @return <code>String</code> Decrypted version of the provided String
	 */
	public String decrypt(byte[] dec) {
		try {
			//final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
			cipher.init(Cipher.DECRYPT_MODE, key);
			return new String(Base64.decode(cipher.doFinal(dec), Base64.DEFAULT));
		} catch(Exception ex) {
			Log.d("Error Decrypt", "", ex);
		}
		return null;
	}
}