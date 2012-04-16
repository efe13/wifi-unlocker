package com.zenkun.wifiunlocker;

import com.zenkun.wifiunlocker.R;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;

public class DlinkKeygen extends KeygenThread {

	public DlinkKeygen(Handler h, Resources res) {
		super(h, res);
	}

	public void run(){
		 char hash[] =  { 'X', 'r', 'q', 'a', 'H', 'N',
				 			'p', 'd', 'S', 'Y', 'w', 
				 			'8', '6', '2', '1', '5'};
		 
		if ( router.mac.equals("") ) 
		{
			handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
					resources.getString(R.string.msg_nomac)));
			return;
		}
		char[] key = new char[20];
		String mac = router.getMac();
		key[0]=mac.charAt(11);
		key[1]=mac.charAt(0);
		 
		key[2]=mac.charAt(10);
		key[3]=mac.charAt(1);
		
		key[4]=mac.charAt(9);
		key[5]=mac.charAt(2);
		 
		key[6]=mac.charAt(8);
		key[7]=mac.charAt(3);
		
		key[8]=mac.charAt(7);
		key[9]=mac.charAt(4);
		
		key[10]=mac.charAt(6);
		key[11]=mac.charAt(5);
		
		key[12]=mac.charAt(1);
		key[13]=mac.charAt(6);
		
		key[14]=mac.charAt(8);
		key[15]=mac.charAt(9);
		
		key[16]=mac.charAt(11);
		key[17]=mac.charAt(2);
		
		key[18]=mac.charAt(4);
		key[19]=mac.charAt(10);
		char [] newkey = new char[20];
		char t;
		int index = 0;
		for (int i=0; i < 20 ; i++)
		{
			t=key[i];
			if ((t >= '0') && (t <= '9'))
				index = t-'0';
			else
			{
				t=Character.toUpperCase(t);
				if ((t >= 'A') && (t <= 'F'))
					index = t-'A'+10;
				else
				{
					handler.sendMessage(Message.obtain(handler, ERROR_MSG , 
							resources.getString(R.string.msg_dlinkerror)));
					return;
				}
			}
			newkey[i]=hash[index];
		}
		pwList.add(String.valueOf(newkey, 0, 20));
		handler.sendEmptyMessage(RESULTS_READY);
		return;
	}
}
