package com.anontech.wifiunlock;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.anontech.wifiunlock.R;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;

/*
 * This is the algorithm to generate the WPA passphrase 
 * for the Hitachi (TECOM) AH-4021 and Hitachi (TECOM) AH-4222.
 * The key is the 26 first characters from the SSID SHA1 hash.
 *  Link : http://rafale.org/~mattoufoutu/ebooks/Rafale-Mag/Rafale12/Rafale12.08.HTML
 */
public class TecomKeygen extends KeygenThread {

	public TecomKeygen(Handler h, Resources res) {
		super(h, res);
	}
	
	
	public void run(){
		if ( router == null)
			return;
		try {
			md = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e1) {
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_nosha1)));
			return;
		}
		md.reset();
		md.update(router.ssid.getBytes());
		byte [] hash = md.digest();
		try {
			pwList.add(StringUtils.getHexString(hash).substring(0,26));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		handler.sendEmptyMessage(RESULTS_READY);
		return;
	}
}
