package com.zenkun.wifiunlocker;

import com.zenkun.wifiunlocker.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class WifiStateReceiver extends BroadcastReceiver {

	WifiManager wifi;
	public WifiStateReceiver(WifiManager wifi ){
		this.wifi = wifi;
	}
	@Override
	public void onReceive(Context context, Intent arg1) {
		if ( wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED  )
		{
			wifi.startScan();
			try{
    		context.unregisterReceiver(this);
			}catch(Exception e ){}
    		Toast.makeText( context ,
					context.getResources().getString(R.string.msg_scanstarted),
					Toast.LENGTH_SHORT).show();
    		return;
		}
		if ( wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING )
		{
			Toast.makeText( context , 
					  context.getResources().getString(R.string.msg_nowifi),
					  Toast.LENGTH_SHORT).show();
			return;
		}
	}

}
