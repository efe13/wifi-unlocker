package com.anontech.wifiunlock;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

public class Downloader extends Thread{
	Handler messHand;
	String urlDownload;	
	boolean stopRequested = false;
	boolean deleteTemp = false;
	public void run() {
		File myDicFile;
		URLConnection con;
		DataInputStream dis;
		FileOutputStream fos;
		int myProgress = 0;
		int fileLen, byteRead;
		byte[] buf;
		try {
			con = new URL(urlDownload).openConnection();
			myDicFile = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "DicTemp.dic");
			// Append mode on
			fos = new FileOutputStream(myDicFile, true);
			
			// Resuming if possible
			myProgress = byteRead = (int) myDicFile.length();
			if(byteRead > 0)
				con.setRequestProperty("Range", "bytes=" + byteRead + "-");

			dis = new DataInputStream(con.getInputStream());
			fileLen = myProgress + con.getContentLength();
			messHand.sendMessage(Message.obtain(messHand, 2, myProgress, fileLen));
			// Checking if external storage has enough memory ...
			android.os.StatFs stat = new android.os.StatFs(Environment.getExternalStorageDirectory().getPath());
			if((long)stat.getBlockSize() * (long)stat.getAvailableBlocks() < fileLen)
				messHand.sendEmptyMessage(1);
			buf = new byte[65536];
			while (myProgress < fileLen) {
				try{

					if ((byteRead = dis.read(buf)) != -1)
					{
						fos.write(buf, 0, byteRead);
						myProgress += byteRead;
					}
					else
					{
						dis.close();
						fos.close();
						myProgress = fileLen;
					}
				}
				catch(Exception e){}
				messHand.sendMessage(Message.obtain(messHand, 4, myProgress, fileLen));
				if ( stopRequested )
				{
					if ( deleteTemp )
						myDicFile.delete();
					dis.close();
					fos.close();
					return;
				}
			}
			messHand.sendEmptyMessage(3);
		}
		catch (FileNotFoundException e)
		{
			messHand.sendEmptyMessage(0);
		}
		catch(Exception e)
		{
			messHand.sendEmptyMessage(-1);
		}
	}

	public Downloader(Handler messHand, String urlDownload) {
		this.messHand = messHand;
		this.urlDownload = urlDownload;
	}

}
