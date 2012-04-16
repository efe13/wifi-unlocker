package com.zenkun.wifiunlocker;

import android.content.res.Resources;
import android.os.Handler;

public class DiscusKeygen extends KeygenThread {

	public DiscusKeygen(Handler h, Resources res) {
		super(h, res);
	}

	static final int essidConst = 0xD0EC31;

	public void run(){
		int routerEssid = Integer.parseInt( router.getSSIDsubpart() , 16);
		int result  = ( routerEssid - essidConst )>>2;
		pwList.add("YW0" + Integer.toString(result));
		handler.sendEmptyMessage(RESULTS_READY);
		return;
		
	}

}
