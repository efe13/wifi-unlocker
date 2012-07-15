
package com.anontech.wifiunlock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.anontech.wifiunlock.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import java.lang.Runnable;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.Menu;
import com.anontech.wifiunlock.WifiNetwork.TYPE;

@SuppressWarnings("deprecation")
public class Main extends SherlockActivity {
	WifiManager wifi;
	boolean wifi_state;
	ListView scanResuls;
	KeygenThread calculator;
	ArrayList<String> list_key = null;
	BroadcastReceiver scanFinished;
	BroadcastReceiver stateChanged;
	ArrayList<WifiNetwork> vulnerable;
	WifiNetwork router;
	long begin;
	static final String TAG = "WifiUnlocker";
	static final String welcomeScreenShownPref = "welcomeScreenShown";

	/** Called when the activity is first created. */
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		wifi = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
		wifi_state = wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED ||  
		wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLING;
	
		SharedPreferences mPrefs = getSharedPreferences("misPref",MODE_PRIVATE);
		SharedPreferences.Editor editor = mPrefs.edit();
		int contador= mPrefs.getInt("contador", 0);
		if(contador>6 & !mPrefs.getBoolean("TOS", false))
		{
			this.showDialog(RATE);
		}else
		{
			editor.putInt("contador",contador+1);
			editor.commit();
		}
		scanResuls = (ListView) findViewById(R.id.ListWifi);
		scanResuls.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				router = vulnerable.get(position);
				if (router.newThomson)
				{
					Toast.makeText( Main.this , getString(R.string.msg_newthomson) ,
							Toast.LENGTH_SHORT).show();
					return;
				}
				calcKeys(router);
			}
		});
		stateChanged = new WifiStateReceiver(wifi);
		scanFinished = new WiFiScanReceiver(this);
		if ( savedInstanceState == null  )
			return;
		Boolean warning = (Boolean)savedInstanceState.getSerializable("warning");
		if ( warning != null )
		{
			removeDialog(DIALOG_NATIVE_CALC);
			if ( calculator != null )
				calculator.stopRequested = true;
		}
		ArrayList<WifiNetwork> list_networks =(ArrayList<WifiNetwork>) savedInstanceState.getSerializable("networks");
		if ( list_networks != null )
		{
			vulnerable = list_networks;
			scanResuls.setAdapter(new WifiListAdapter(vulnerable, this));
		}
		WifiNetwork r = (WifiNetwork) savedInstanceState.getSerializable("router");
		if ( r != null )
		{
			router = r;
		}
		else
			router = new WifiNetwork("","",0,"",this);
		ArrayList<String> list_k =  (ArrayList<String>) savedInstanceState.getSerializable("keys");
		if ( list_k != null )
		{
			list_key = list_k;
		}
	}
	
	protected void onSaveInstanceState (Bundle outState){	
		try {
			if ( calculator instanceof NativeThomson )
			{
				outState.putSerializable("warning", true);
			}
			outState.putSerializable("router", router);
			outState.putSerializable("keys", list_key );
			outState.putSerializable("networks", vulnerable );
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}


	public void onStart() {
		try{ 
			super.onStart();
			getPrefs();
			if ( wifiOn )
			{
				if ( !wifi.setWifiEnabled(true))
					Toast.makeText( Main.this , getString(R.string.msg_wifibroken),
							Toast.LENGTH_SHORT).show();
				else
					wifi_state = true;
			}
			scan();	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onStop() {
		try{ 
			super.onStop();
			unregisterReceiver(scanFinished);
			unregisterReceiver(stateChanged);
			removeDialog(DIALOG_KEY_LIST);
			removeDialog(DIALOG_MANUAL_CALC); 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	ProgressDialog progressDialog;
	private static final int DIALOG_THOMSON3G = 0; 
	private static final int DIALOG_KEY_LIST = 1;
	private static final int DIALOG_MANUAL_CALC = 2;
	private static final int DIALOG_NATIVE_CALC = 3;
	private static final int DIALOG_AUTO_CONNECT = 4;
	private static final int RATE = 5;

	
	protected Dialog onCreateDialog(int id ) {
		switch (id) {
		case RATE:
			LayoutInflater factory = getLayoutInflater();
	        final View rateApp = factory.inflate(R.layout.rate, null);
	       // Button donateMarketButton = (Button) donateView.findViewById(R.id.DonateMarketButton);
	        return new AlertDialog.Builder(this)
	        .setIcon(R.drawable.ic_launcher)
	        .setTitle("Please Read")
	        .setView(rateApp)
	        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					SharedPreferences prefs =
					     getSharedPreferences("misPref",Context.MODE_PRIVATE);
					 
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean("TOS", true);
					editor.commit();
					try
					{
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("market://details?id=com.anontech.wifiunlock"));
					startActivity(intent);
					}catch (Exception e) {
						//no market
						// TODO: handle exception
					}
					
				}
			})
			.setNegativeButton(getResources().getString(R.string.dialog_remindme), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
				}
			})
	        .create();
	        
			case DIALOG_THOMSON3G: {
				progressDialog = new ProgressDialog(Main.this);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setTitle(getString(R.string.dialog_thomson3g));
				progressDialog.setMessage(getString(R.string.dialog_thomson3g_msg));
				progressDialog.setCancelable(false);
				progressDialog.setProgress(0);
				progressDialog.setButton(getString(R.string.bt_manual_cancel),
						new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						if ( Main.this.calculator != null )
							Main.this.calculator.stopRequested = true;
						removeDialog(DIALOG_THOMSON3G);
					}
				});
				progressDialog.setIndeterminate(false);
				return progressDialog;
			}
			case DIALOG_KEY_LIST: {
				AlertDialog.Builder builder = new Builder(this);
				builder.setTitle(router.ssid);
			    LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
			    View layout = inflater.inflate(R.layout.results,
			                                   (ViewGroup) findViewById(R.id.layout_root));
			    ListView list = (ListView) layout.findViewById(R.id.list_keys);
				list.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						String key = ((TextView)view).getText().toString();
						Toast.makeText(getApplicationContext(), key + " " 							
								+ getString(R.string.msg_copied),
								Toast.LENGTH_SHORT).show();
						ClipboardManager clipboard = 
							(ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
	
						clipboard.setText(key);
						startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
					}
				});
				
				list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list_key)); 
				/*
				 * TODO: Auto connect
				 * Still not working as wished though it works +-.
				 */
			/*	builder.setPositiveButton(Main.this.getResources().getString(R.string.bt_connect),
						new OnClickListener() {	
							public void onClick(DialogInterface dialog, int which) {
								
								//String _wep = list_key.get(0);
								//saveWepConfig(router.ssid,"");
								WifiConfiguration conf = new WifiConfiguration();
								
								List<WifiConfiguration> item = wifi.getConfiguredNetworks();
								for(WifiConfiguration x : item)
								{
									if(router.ssid.equals(x.SSID))
									{
										wifi.removeNetwork(x.networkId);
									}
								}
								wifi.disconnect();
								conf.SSID = "\"" + router.ssid + "\"";   
								
								 if(router.encryption.contains("WEP"))
								    {
										conf.wepKeys[0] = "\"" + list_key.get(0) + "\""; 
										conf.wepTxKeyIndex = 0;
										conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
										conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40); 
								    }else
								    {
								    	conf.preSharedKey = "\""+ list_key.get(0) +"\"";
								    }
								 WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
								 wifiManager.setWifiEnabled(true);
								int res= wifiManager.addNetwork(conf);
								   boolean es = wifi.saveConfiguration();
								    Log.v("WifiPreference", "saveConfiguration returned " + es );
								    boolean b = wifi.enableNetwork(res, true);   
								    Log.v("WifiPreference", "enableNetwork returned " + b );
								    if (!b)
								    {
								    	 Log.v("WifiPreference", "Falso ");
								    }
								    else
								    {
								    	Log.v("WifiPreference", "enableNetwork returned " + b);
								    }
							}
				});*/
				
				builder.setNeutralButton(Main.this.getResources().getString(R.string.bt_share),
							new OnClickListener() {	
								public void onClick(DialogInterface dialog, int which) {
									try
									{
										Intent i = new Intent(Intent.ACTION_SEND);
										i.setType("text/plain");
										i.putExtra(Intent.EXTRA_SUBJECT, router.ssid + getString(R.string.share_msg_begin));
										Iterator<String> it = list_key.iterator();
										String message = router.ssid + getString(R.string.share_msg_begin) + ":\n";
										while ( it.hasNext() )
											message += it.next() + "\n";
										
										i.putExtra(Intent.EXTRA_TEXT, message);
										message = getString(R.string.share_title);
										startActivity(Intent.createChooser(i, message));
									}
									catch(Exception e)
									{
										Toast.makeText( Main.this , getString(R.string.msg_err_sendto) , 
												Toast.LENGTH_SHORT).show();
										return;
									}
								}
							});
				builder.setNegativeButton(getString(R.string.bt_save_sd), new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_MANUAL_CALC);
						if ( !Environment.getExternalStorageState().equals("mounted")  && 
							     !Environment.getExternalStorageState().equals("mounted_ro")	)
						{
							Toast.makeText( Main.this , getString(R.string.msg_nosdcard),
								Toast.LENGTH_SHORT).show();
							return ;
						}
						try {
							BufferedWriter out = new BufferedWriter(
									new FileWriter(folderSelect + File.separator + router.ssid + ".txt"));
							out.write(router.ssid + " KEYS");
							out.newLine();
							for ( String s : list_key )
							{
								out.write(s);
								out.newLine();
							}
							out.close();
						}
						catch (IOException e)
						{
							Toast.makeText( Main.this , getString(R.string.msg_err_saving_key_file),
									Toast.LENGTH_SHORT).show();
							return ;
						}
						Toast.makeText( Main.this , router.ssid + ".txt " + getString(R.string.msg_saved_key_file),
								Toast.LENGTH_SHORT).show();
					}
				});
				
				builder.setView(layout);
				return builder.create();
			}
			case DIALOG_MANUAL_CALC: {
				AlertDialog.Builder builder = new Builder(this); 
				final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
				final View layout = inflater.inflate(R.layout.manual_input,
                        (ViewGroup) findViewById(R.id.manual_root));
				builder.setTitle(getString(R.string.menu_manual));
				/*Need to do this to renew the dialog to show the MAC input*/
				builder.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						removeDialog(DIALOG_MANUAL_CALC);
					}
				});
				String[] routers = getResources().getStringArray(R.array.supported_routers);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
										android.R.layout.simple_dropdown_item_1line, routers);
				final AutoCompleteTextView edit = (AutoCompleteTextView) layout.findViewById(R.id.manual_autotext);
				edit.setAdapter(adapter);
				edit.setThreshold(1);
				InputFilter filterMAC = new InputFilter() { 
			        public CharSequence filter(CharSequence source, int start, int end, 
			        		Spanned dest, int dstart, int dend) { 
			        		                for (int i = start; i < end; i++) { 
			        		                        if (!Character.isLetterOrDigit(source.charAt(i)) &&
			        		                        		source.charAt(i) != '-' && source.charAt(i) != '_' && source.charAt(i) != ' ') { 
			        		                                return ""; 
			        		                        } 
			        		                } 
			        		                return null; 
			        		        }
	     		};
			    edit.setFilters(new InputFilter[]{ filterMAC});
			    if ( manualMac )
			    {
			    	layout.findViewById(R.id.manual_mac_root).setVisibility(View.VISIBLE);
			    	edit.setImeOptions(EditorInfo.IME_ACTION_NEXT);
			    	final EditText macs[] = new EditText[6];
			    	macs[0] = (EditText) layout.findViewById(R.id.input_mac_pair1);
			    	macs[1] = (EditText) layout.findViewById(R.id.input_mac_pair2);
			    	macs[2] = (EditText) layout.findViewById(R.id.input_mac_pair3);
			    	macs[3] = (EditText) layout.findViewById(R.id.input_mac_pair4);
			    	macs[4] = (EditText) layout.findViewById(R.id.input_mac_pair5);
			    	macs[5] = (EditText) layout.findViewById(R.id.input_mac_pair6);
		     		final InputFilter maxSize = new InputFilter.LengthFilter(2);
	        		InputFilter filterMac = new InputFilter() { 
				        public CharSequence filter(CharSequence source, int start, int end, 
				        		Spanned dest, int dstart, int dend) { 
				        		                try{/*TODO:Lazy mode programming, improve in the future*/
				        		                	Integer.parseInt((String) source , 16);
				        		                }
				        		                catch( Exception e){
				        		                	return "";
				        		                }
				        		                return null; 
				        		        }
				        		};
				    for(final EditText mac : macs)
				    {
				    	mac.setFilters(new InputFilter[]{filterMac , maxSize});
					    mac.addTextChangedListener(new TextWatcher() {
							public void onTextChanged(CharSequence s, int start, int before, int count) {}
							
							public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
							
							public void afterTextChanged(Editable e) {
						    	if(e.length() != 2)
						    		return;
						    	
							    for(int i = 0; i < 6; ++i)
							    {
							    	if(macs[i].getText().length() != 0)
							    		continue;
							    	
						    		macs[i].requestFocus();
						    		return;
							    }
							}
						});
				    }
			    }
				builder.setNeutralButton(getString(R.string.bt_manual_calc), new OnClickListener() {			
					public void onClick(DialogInterface dialog, int which) {
						String ssid = edit.getText().toString().trim();
						String mac = "";
						if ( manualMac )
						{
						    EditText mac1 = (EditText) layout.findViewById(R.id.input_mac_pair1);
						    EditText mac2 = (EditText) layout.findViewById(R.id.input_mac_pair2);
						    EditText mac3 = (EditText) layout.findViewById(R.id.input_mac_pair3);
						    EditText mac4 = (EditText) layout.findViewById(R.id.input_mac_pair4);
						    EditText mac5 = (EditText) layout.findViewById(R.id.input_mac_pair5);
						    EditText mac6 = (EditText) layout.findViewById(R.id.input_mac_pair6);
						    mac= mac1.getText().toString()+':'+mac2.getText().toString()+':'+
						    	 mac3.getText().toString()+':'+mac4.getText().toString()+':'+
						    	 mac5.getText().toString()+':'+mac6.getText().toString();
						    if ( mac.length() < 17 )
						    	mac = "";
					    }
						if ( ssid.equals("") )
							return;
						begin =  System.currentTimeMillis();
						router = new WifiNetwork(ssid, mac , 0 ,"" , Main.this);
						calcKeys(router);
						InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);						
					}
				});
				builder.setNegativeButton(getString(R.string.bt_manual_cancel), new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_MANUAL_CALC);
					}
				});
				
				builder.setView(layout);
				return builder.create();
			}
			case DIALOG_NATIVE_CALC: {
				progressDialog = new ProgressDialog(Main.this);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setTitle(Main.this.getResources().getString(R.string.dialog_nativecalc));
				progressDialog.setMessage(Main.this.getResources().getString(R.string.dialog_nativecalc_msg));
				progressDialog.setCancelable(false);
				progressDialog.setProgress(0);
				progressDialog.setButton(Main.this.getResources().getString(R.string.bt_manual_cancel),
						new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						if ( Main.this.calculator != null )
							Main.this.calculator.stopRequested = true;
						removeDialog(DIALOG_THOMSON3G);
					}
				});
				progressDialog.setIndeterminate(false);
				return progressDialog;
			}
			case DIALOG_AUTO_CONNECT:
			{
				progressDialog = new ProgressDialog(Main.this);
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.setMessage("Connecting");
				progressDialog.setMax(list_key.size() + 1);
				progressDialog.setTitle(R.string.msg_dl_dlingdic);
				progressDialog.setCancelable(false);
				progressDialog.setButton(getString(R.string.bt_close), new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						removeDialog(DIALOG_AUTO_CONNECT);
					}
				});
				return progressDialog;
			}
		}
		return null;
	}
	void saveWepConfig(String ssid,String WEP)
	{
		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> item = wifi.getConfiguredNetworks();
		for(WifiConfiguration x : item)
		{
			if(ssid.equals(x.SSID))
			{
				wifi.removeNetwork(x.networkId);
			}
		}
	    WifiConfiguration wc = new WifiConfiguration(); 
	    wc.SSID = "\""+ ssid +"\""; //IMP! This should be in Quotes!!
	    wc.hiddenSSID = false;
	    wc.status = WifiConfiguration.Status.DISABLED;     
	    wc.priority = 40;
	    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	    wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); 
	    wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	    wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	    wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

	    wc.wepKeys[0] = "\""+WEP+"\""; //This is the WEP Password
	    wc.wepTxKeyIndex = 0;
	    WifiManager  wifiManag = (WifiManager) this.getSystemService(WIFI_SERVICE);
	    boolean res1 = wifiManag.setWifiEnabled(true);
	    int res = wifi.addNetwork(wc);
	    Log.d("WifiPreference", "add Network returned " + res );
	    boolean es = wifi.saveConfiguration();
	    Log.d("WifiPreference", "saveConfiguration returned " + es );
	    boolean b = wifi.enableNetwork(res, true);   
	    Log.d("WifiPreference", "enableNetwork returned " + b );  

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater infla = getSupportMenuInflater();
		infla.inflate(R.menu.wifi, menu);
		return true;
	}

	public void scan(){
		registerReceiver(scanFinished, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		if ( !wifi_state && !wifiOn )
		{
			Toast.makeText( Main.this , 
					Main.this.getResources().getString(R.string.msg_nowifi),
					Toast.LENGTH_SHORT).show();
			return;
		}
		if ( wifi.getWifiState() == WifiManager.WIFI_STATE_ENABLING )
		{
			registerReceiver(stateChanged, new IntentFilter(
					WifiManager.WIFI_STATE_CHANGED_ACTION));
			Toast.makeText( Main.this ,
					Main.this.getResources().getString(R.string.msg_wifienabling),
					Toast.LENGTH_SHORT).show();
		}
		else{
			Thread run = new Thread (new Runnable() {
				
				public void run() {
					if ( wifi.startScan() )
					{
						handler.sendMessage(Message.obtain(handler, KeygenThread.ERROR_MSG , 
								getResources().getString(R.string.msg_scanstarted)));
					}
					else{
						handler.sendMessage(Message.obtain(handler, KeygenThread.ERROR_MSG , 
								getResources().getString(R.string.msg_scanfailed)));
					}
					
				}
				 
			});
			run.start();
		}
	}


	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.wifi_scan:
			scan();
			return true;
		//case R.id.manual_input:
			//showDialog(DIALOG_MANUAL_CALC);
		//	return true;
		case R.id.pref:
			startActivity( new Intent(this , Preferences.class ));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void calcKeys(WifiNetwork wifi){
		if(wifi.isOpen)
		{
			Toast.makeText(Main.this, "Open Network, no need Password", Toast.LENGTH_SHORT).show();
		}
		if ( !wifi.supported )
		{
			Toast.makeText( Main.this , 
					Main.this.getResources().getString(R.string.msg_unspported),
					Toast.LENGTH_SHORT).show();
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			return;
		}
		
		try {
			switch( wifi.type )
			{
				case THOMSON: Main.this.calculator = 
							new ThomsonKeygen(handler,getResources(), folderSelect , thomson3g);
							break;
				case DISCUS: Main.this.calculator = 
							new DiscusKeygen(handler,getResources());
							break;
				case EIRCOM: Main.this.calculator = 
							new EircomKeygen(handler,getResources());
							break;
				case DLINK: Main.this.calculator = 
							new DlinkKeygen(handler,getResources());
							break;
				case VERIZON: Main.this.calculator = 
							new VerizonKeygen(handler,getResources());
							break;
				case PIRELLI: Main.this.calculator = 
							new PirelliKeygen(handler,getResources());
							break;
				case TELSEY: Main.this.calculator = 
							new TelseyKeygen(handler,getResources());
							break;
				case ALICE:	 Main.this.calculator = 
							new AliceKeygen(handler,getResources());
							break;
				case WLAN4:	 Main.this.calculator = 
							new Wlan4Keygen(handler,getResources());
							break;
				case HUAWEI: Main.this.calculator = 
							new HuaweiKeygen(handler,getResources());
							break;
				case WLAN2:	 Main.this.calculator = 
							new Wlan2Keygen(handler,getResources());
							break;
				case ONO_WEP: Main.this.calculator = 
							new OnoKeygen(handler,getResources());
							break;
				case SKY_V1: Main.this.calculator = 
							new SkyV1Keygen(handler,getResources());
							break;	
				case WLAN6: Main.this.calculator = 
							new Wlan6Keygen(handler,getResources());
							break;
				case TECOM: Main.this.calculator = 
							new TecomKeygen(handler,getResources());
							break;
				case INFOSTRADA: Main.this.calculator = 
							new InfostradaKeygen(handler,getResources());
							break;		
				case EASYBOX: Main.this.calculator = new EasyBoxKeygen(handler, getResources());
				break;
			}
		}catch(LinkageError e){
			e.printStackTrace();
			Toast.makeText( Main.this ,
					Main.this.getResources().getString(R.string.err_misbuilt_apk), 
					Toast.LENGTH_SHORT).show();
			return;
		}

		Main.this.calculator.router = wifi;
		Main.this.calculator.setPriority(Thread.MAX_PRIORITY);
		begin =  System.currentTimeMillis();//debugging
		Main.this.calculator.start();
		removeDialog(DIALOG_KEY_LIST);
		removeDialog(DIALOG_MANUAL_CALC);
		if (  wifi.type == TYPE.THOMSON && thomson3g )
			showDialog(DIALOG_THOMSON3G);
		removeDialog(DIALOG_KEY_LIST);
	}

	boolean wifiOn;
	boolean thomson3g;
	boolean nativeCalc;
	boolean manualMac;
	String folderSelect;
	final String folderSelectPref = "folderSelect";
	final String wifiOnPref = "wifion";
	final String thomson3gPref = "thomson3g";
	final String nativeCalcPref = "nativethomson";
	final String manualMacPref = "manual_mac";

	private void getPrefs() {
		SharedPreferences prefs = PreferenceManager
		.getDefaultSharedPreferences(getBaseContext());
		wifiOn = prefs.getBoolean(wifiOnPref , true);
		thomson3g = prefs.getBoolean(thomson3gPref, false);
		nativeCalc = prefs.getBoolean(nativeCalcPref, true);
		manualMac = prefs.getBoolean(manualMacPref, false);
		folderSelect = prefs.getString(folderSelectPref, 
				Environment.getExternalStorageDirectory().getAbsolutePath());
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if ( thomson3g)
				removeDialog(DIALOG_THOMSON3G);
			if ( nativeCalc )
				removeDialog(DIALOG_NATIVE_CALC);
			if ( msg.what == KeygenThread.RESULTS_READY ) /*Got Keys*/
			{
				begin = System.currentTimeMillis()-begin;
				list_key = Main.this.calculator.getResults();
				Log.d(TAG, "Time to solve:" + begin);
				if (!isFinishing())
					showDialog(DIALOG_KEY_LIST);
				return;
			}
			if ( msg.what == KeygenThread.ERROR_MSG ) 
			{
				if ( nativeCalc && ( calculator instanceof ThomsonKeygen ) )
				{
					if ( ((ThomsonKeygen)calculator).errorDict )
					{
						Toast.makeText( Main.this , getString(R.string.msg_startingnativecalc) , 
								Toast.LENGTH_SHORT).show();
						
						WifiNetwork tmp = Main.this.calculator.router;
						try{
							Main.this.calculator = new NativeThomson(this ,Main.this.getResources() );
						}catch(LinkageError e){
							Toast.makeText( Main.this ,getString(R.string.err_misbuilt_apk), 
									Toast.LENGTH_SHORT).show();
							return;
						}
						if (isFinishing())
							return;
						Main.this.calculator.router = tmp;
						Main.this.calculator.setPriority(Thread.MAX_PRIORITY);
						Main.this.calculator.start();
						showDialog(DIALOG_NATIVE_CALC);
						return;
					}

				}
				if (!isFinishing())
					Toast.makeText( Main.this , msg.obj.toString() , Toast.LENGTH_SHORT).show();
				return;
			}
			if ( msg.what == 2 )
			{
				progressDialog.setProgress(progressDialog.getProgress() +1);
				return;
			}
			if ( msg.what == 3 )
			{
				removeDialog(DIALOG_AUTO_CONNECT);
				if (!isFinishing())
					Toast.makeText( Main.this ,msg.obj.toString() , Toast.LENGTH_SHORT).show();
				return;
			}
		}
		
	};

}
