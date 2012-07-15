package com.anontech.wifiunlock;

import com.anontech.wifiunlock.R;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;

/*
 * This is not actual an algorithm
 * as the key is calculated from the MAC 
 * address adding a '2' as the first character
 */
public class InfostradaKeygen extends KeygenThread {

	public InfostradaKeygen(Handler h, Resources res) {
		super(h, res);
	}
	
	public void run(){
		if ( router == null)
			return;
		if ( router.getMac().length() != 12 ) 
		{
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_errpirelli)));
			return;
		}
		pwList.add("2"+router.getMac().toUpperCase());
		handler.sendEmptyMessage(RESULTS_READY);
		return;
	}
	
}
