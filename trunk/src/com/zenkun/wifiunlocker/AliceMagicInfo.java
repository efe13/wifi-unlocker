package com.zenkun.wifiunlocker;

import java.io.Serializable;

public class AliceMagicInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1640975633984337261L;
	String alice;
	int [] magic;
	String serial;
	String mac;
	public AliceMagicInfo(String alice,  int[] magic,
			String serial, String mac) {
		this.alice = alice;
		this.magic = magic;
		this.serial = serial;
		this.mac = mac;
	}
}
