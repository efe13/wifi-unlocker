package com.zenkun.wifiunlocker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.zenkun.wifiunlocker.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.widget.Toast;

class WiFiScanReceiver extends BroadcastReceiver {
	  RouterKeygen solver;

	  public WiFiScanReceiver( RouterKeygen wifiDemo) {
	    super();
	    this.solver = wifiDemo;
	  }

	  public void onReceive(Context c, Intent intent) {
		  
		if ( solver == null )
			return;
		if ( solver.wifi == null )
			return;
	    List<ScanResult> results = solver.wifi.getScanResults();
	    ArrayList<WifiNetwork> list = new ArrayList<WifiNetwork>();
	    Set<WifiNetwork> set = new TreeSet<WifiNetwork>();
	    if ( results == null )/*He have had reports of this returning null instead of empty*/
	    	return;
	    for (int i = 0; i < results.size() - 1; ++i)
	    	for (int j = i+1; j < results.size(); ++j)
		    	if(results.get(i).SSID.equals(results.get(j).SSID))
		    		results.remove(j--);
	    
	    for (ScanResult result : results) {
	    	  set.add(new WifiNetwork(result.SSID, result.BSSID, result.level , result.capabilities , solver));
	    }
	    Iterator<WifiNetwork> it = set.iterator();
	    while( it.hasNext())
	    	list.add(it.next());
	    solver.vulnerable = list;
	    if (  list.isEmpty() )
	    {
			Toast.makeText( solver , solver.getResources().getString(R.string.msg_nowifidetected) ,
					Toast.LENGTH_SHORT).show();
	    }
	    solver.scanResuls.setAdapter(new WifiListAdapter(list , solver)); 
	    try{
		solver.unregisterReceiver(this);   
	    }catch(Exception e ){}
	    
	 }

}
