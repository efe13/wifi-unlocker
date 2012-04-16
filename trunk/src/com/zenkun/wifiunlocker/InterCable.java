package com.zenkun.wifiunlocker;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;

public class InterCable extends KeygenThread {

	public InterCable(Handler h, Resources res) {
		super(h, res);
		// TODO Auto-generated constructor stub
	}
	//basado en http://www.cablevision.com.mx/contenido/portal/descargables/pdf/arris.pdf
	public void run(){
		if ( router == null)
			return;
		
		if ( router.getMac().length() != 12 ) 
		{
			
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_errpirelli)));
			return;
		}
		//get the mac , the last number seemsits the last Hex number minus 1
		String pwd ="m"+ router.getMac().substring(0, router.getMac().length()-1).replace(":", "");
		char hex = router.getMac().charAt(router.getMac().length());
		//need confirm again if its its one less HEX
		
		pwList.add(pwd);
		handler.sendEmptyMessage(RESULTS_READY);
		return;
	}

}
