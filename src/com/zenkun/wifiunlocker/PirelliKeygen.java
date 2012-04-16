package com.zenkun.wifiunlocker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.zenkun.wifiunlocker.R;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;


public class PirelliKeygen extends KeygenThread{
	
	public PirelliKeygen(Handler h, Resources res) {
		super(h, res);
	}
	
	final byte[] saltMD5 = {
			0x22, 0x33, 0x11, 0x34, 0x02,
		    (byte) 0x81, (byte) 0xFA, 0x22, 0x11, 0x41,
			0x68, 0x11,	0x12, 0x01, 0x05,
			0x22, 0x71, 0x42, 0x10, 0x66 };
	
	public void run(){

		if ( router == null)
			return;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e1) {
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_nomd5)));
			return;
		}
		if ( router.getSSIDsubpart().length() != 12 ) 
		{
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_errpirelli)));
			return;
		}
		
		byte [] routerESSID = new byte[6];
		for (int i = 0; i < 12; i += 2)
			routerESSID[i / 2] = (byte) ((Character.digit(router.getSSIDsubpart().charAt(i), 16) << 4)
					+ Character.digit(router.getSSIDsubpart().charAt(i + 1), 16));

		md.reset();
		md.update(routerESSID);
		md.update(saltMD5);
		byte [] hash = md.digest();
		short [] key = new short[5];
		/*Grouping in five groups fo five bits*/
		key[0] = (short)( (hash[0] & 0xF8) >> 3 );
		key[1] = (short)(( (hash[0] & 0x07) << 2) | ( (hash[1] & 0xC0) >>6 ));
		key[2] = (short)((hash[1] & 0x3E) >> 1 );
		key[3] = (short)(( (hash[1] & 0x01) << 4) |  ((hash[2] & 0xF0) >> 4));
		key[4] = (short)(( (hash[2] & 0x0F) << 1) |  ((hash[3] & 0x80) >> 7) );
		for ( int i = 0 ; i < 5 ; ++i )
			if ( key[i] >= 0x0A )
				key[i] += 0x57;
		try {
			pwList.add(StringUtils.getHexString(key));
		} catch (UnsupportedEncodingException e) {}
		handler.sendEmptyMessage(RESULTS_READY);
		return;
	}

}
