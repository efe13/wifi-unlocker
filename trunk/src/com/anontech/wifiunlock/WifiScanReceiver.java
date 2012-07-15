package com.anontech.wifiunlock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.anontech.wifiunlock.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.widget.Toast;

class WiFiScanReceiver extends BroadcastReceiver {
	  Main solver;

	  public WiFiScanReceiver( Main wifiDemo) {
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
	    
	    int size = results.size(); // for performance
	    
	    for (int i = 0; i < size - 1; ++i)
	    	for (int j = i+1; j < size; ++j)
		    	if(results.get(i).SSID.equals(results.get(j).SSID))
		    		results.remove(j--);
	    
	    for (ScanResult result : results) {
	    	WifiNetwork tmp = new WifiNetwork(result.SSID, result.BSSID, result.level , result.capabilities , solver);
	    	if((tmp.supported && !tmp.newThomson)|| tmp.isOpen) //we only add supported devices
	    	{
	    		set.add(tmp);
	    		list.add(tmp);
	    	}
	    	  //set.add(new WifiNetwork(result.SSID, result.BSSID, result.level , result.capabilities , solver));
	    }
	    /*Iterator<WifiNetwork> it = set.iterator();
	    while( it.hasNext())
	    	list.add(it.next());*/
	    solver.vulnerable = list; //solo mostraremos las SSID válidas, no importa el orden.
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
