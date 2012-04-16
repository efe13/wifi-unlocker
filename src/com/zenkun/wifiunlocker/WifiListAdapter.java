package com.zenkun.wifiunlocker;

import java.util.ArrayList;
import java.util.List;

import com.zenkun.wifiunlocker.R;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WifiListAdapter extends BaseAdapter {
	private List<WifiNetwork> listNetworks; 
	private Context context; 
	public WifiListAdapter(List<WifiNetwork> list, Context context) {
		if ( list != null )
			this.listNetworks = list;
		else
			this.listNetworks = new ArrayList<WifiNetwork>();
        this.context = context;
    }
	
	public int getCount() {
		return listNetworks.size();
	}

	public Object getItem(int position) {
		return listNetworks.get(position);
	}

	public long getItemId(int position) {
		return listNetworks.get(position).hashCode();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		 RelativeLayout itemLayout;
		 WifiNetwork wifi = listNetworks.get(position);
		 int strenght = listNetworks.get(position).getLevel();
	     itemLayout= (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.item_list_wifi, parent, false);
	 
	     TextView ssid = (TextView) itemLayout.findViewById(R.id.wifiName);
	     ssid.setText(wifi.ssid);
	     
	     TextView bssid = (TextView) itemLayout.findViewById(R.id.wifiMAC);
	     bssid.setText(wifi.mac);
	     
	     ImageView icon = (ImageView)itemLayout.findViewById(R.id.icon);
	     if ( wifi.supported && !wifi.newThomson)
	     {
	    	 icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_possible));
	    	 ssid.setTextColor(Color.parseColor("#99CC00"));
	    	 bssid.setTextColor(Color.parseColor("#99CC00"));
	     }
	     ImageView networkS = (ImageView)itemLayout.findViewById(R.id.strenght);
	     int pic = WifiManager.calculateSignalLevel(strenght, 4);
	     switch (pic){
	     	case 0:
	     				networkS.setImageDrawable(context.getResources().
		    		 		getDrawable(( wifi.supported && !wifi.newThomson)? R.drawable.ic_wifi_weak_possible: R.drawable.ic_wifi_weak));
		     		break;
	     	case 1: networkS.setImageDrawable(context.getResources().
	 						getDrawable(( wifi.supported && !wifi.newThomson)?R.drawable.ic_wifi_medium_possible: R.drawable.ic_wifi_medium));
	     			break;
	     	case 2: networkS.setImageDrawable(context.getResources().
						getDrawable(( wifi.supported && !wifi.newThomson)?R.drawable.ic_wifi_strong_possible: R.drawable.ic_wifi_strong));
	     			break;
	     	case 3: networkS.setImageDrawable(context.getResources().
					getDrawable(( wifi.supported && !wifi.newThomson)?R.drawable.ic_wifi_verystrong_possible: R.drawable.ic_wifi_verystrong));
     				break;
	     }
		return  itemLayout;
	}

}
