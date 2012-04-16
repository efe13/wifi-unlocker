package com.zenkun.wifiunlocker;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.zenkun.wifiunlocker.R;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;

public class EircomKeygen extends KeygenThread  {

	public EircomKeygen(Handler h, Resources res) {
		super(h, res);
	}

	public void run(){
		String mac=  router.getMacEnd();
		try {
			md = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e1) {
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_nosha1)));
			return;
		}
		byte [] routerMAC = new byte[4];
		routerMAC[0] = 1;
		for (int i = 0; i < 6; i += 2)
			routerMAC[i / 2 + 1] = (byte) ((Character.digit(mac.charAt(i), 16) << 4)
					+ Character.digit(mac.charAt(i + 1), 16));
		int macDec = ( (0xFF & routerMAC[0]) << 24 ) | ( (0xFF & routerMAC[1])  << 16 ) |
					 ( (0xFF & routerMAC[2])  << 8 ) | (0xFF & routerMAC[3]);
		mac = StringUtils.dectoString(macDec) + "Although your world wonders me, ";
		md.reset();
		md.update(mac.getBytes());
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
