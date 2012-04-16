package com.zenkun.wifiunlocker;

import java.security.MessageDigest;
import java.util.ArrayList;

import android.content.res.Resources;
import android.os.Handler;


public class KeygenThread extends Thread {
	
	MessageDigest md;
	WifiNetwork router;
	boolean stopRequested = false;
	ArrayList<String> pwList;
	static final int RESULTS_READY = 1000;
	static final int ERROR_MSG = 1001;
	Handler handler;
	Resources resources;
	

	public KeygenThread( Handler h , Resources res)
	{
		this.handler = h;
		this.resources = res;
		this.pwList = new ArrayList<String>();

	}


	public ArrayList<String> getResults() {
		return pwList;
	}

	
}
