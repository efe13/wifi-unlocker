package com.anontech.wifiunlock;

import com.anontech.wifiunlock.R;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;

public class NativeThomson extends KeygenThread{

	public NativeThomson(Handler h, Resources res) {
		super(h, res);
	}


	static {
		System.loadLibrary("thomson");
    }
  /** 
   * Native processing without a dictionary.
   */
	public native String[] thomson( byte [] essid );
  
  
	public void run(){
		if ( router == null)
			return;
		if ( router.getSSIDsubpart().length() != 6 ) 
		{
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_shortessid6)));
			return;
		}
		byte [] routerESSID = new byte[3];

		for (int i = 0; i < 6; i += 2)
			routerESSID[i / 2] = (byte) ((Character.digit(router.getSSIDsubpart().charAt(i), 16) << 4)
					+ Character.digit(router.getSSIDsubpart().charAt(i + 1), 16));
		String [] results;
		try{
			results = this.thomson(routerESSID);
		}catch (Exception e) {
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_err_native)));
			return;
		}
		if ( stopRequested )
			return;
		for (int i = 0 ; i < results.length ; ++i  )
			pwList.add(results[i]);
		
		if(pwList.toArray().length == 0)
		{
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_errnomatches)));
			return;
		}
		handler.sendEmptyMessage(RESULTS_READY);
		return;
	}

}
