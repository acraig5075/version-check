package za.co.knowbase.versioncheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;



public class VersionListingActivity extends ListActivity 
	{
	// preference names
	private static final String [] _keys = {"cd1pref", "ac1pref", "cd2pref", "ac2pref", "cd3pref", "ac3pref"};

	// number of possible software packages in list (whether shown or not)
	private int _count;
	
	// storage for list items, primary and secondary text for each item
	ArrayList<HashMap<String, String>> _listContents;

	// storage for details of the various software versions
	ArrayList<SoftwareVersion> _software;

	// download thread handler
	Handler _handler;

	// whether data connection is available or not
	boolean _online = true;



	@Override
	public void onCreate(Bundle savedInstanceState)
		{
		//reload any saved activity instance state
		super.onCreate(savedInstanceState);

		// set the activity to use the main.xml layout resource
		setContentView(R.layout.main);

		try
			{
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			setTitle("Knowledge Base Software: Version Check " + pInfo.versionName);
			}
		catch (Exception e)
			{
			setTitle("Knowledge Base Software: Version Check");
			}
		
		_count = _keys.length;
		_listContents = new ArrayList<HashMap<String, String>>(_count);
		_software = new ArrayList<SoftwareVersion>(_count);
		
		final String t1 = "Civil Designer 2011:www.civildesigner.com/:CD2011_Ver.txt:false:CD2011_%d_x86_Update.exe:CD2011_%d_x64_Update.exe:0";
		final String t2 = "AllyCAD 2011:www.allycad.com/:AC2011_Ver.txt:false:AC2011_%d_x86_Update.exe:AC2011_%d_x64_Update.exe:0";
		final String t3 = "Civil Designer 6.5:www.civildesigner.com/:CivDes65Ver.txt:true:CD65R%d_Patch.exe::0";
		final String t4 = "AllyCAD 3.6:www.allycad.com/:AllyCAD36Ver.txt:true:AC36R%d_Patch.exe::0";
		final String t5 = "Civil Designer 2013:www.civildesigner.com/:CD2013_Ver.txt:false:CD2013_%d_x86_Update.exe:CD2013_%d_x64_Update.exe:0";
		final String t6 = "AllyCAD 2013:www.allycad.com/:AC2013_Ver.txt:false:AC2013_%d_x86_Update.exe:AC2013_%d_x64_Update.exe:0";
		
		SoftwareVersion s0 = new SoftwareVersion(t1);
		SoftwareVersion s1 = new SoftwareVersion(t2);
		SoftwareVersion s2 = new SoftwareVersion(t3);
		SoftwareVersion s3 = new SoftwareVersion(t4);
		SoftwareVersion s4 = new SoftwareVersion(t5);
		SoftwareVersion s5 = new SoftwareVersion(t6);
	
		int [] lastVersions = retrieveLastKnownVersions();
		
		s0._versionNum = lastVersions[0];
		s1._versionNum = lastVersions[1];
		s2._versionNum = lastVersions[2];
		s3._versionNum = lastVersions[3];
		s4._versionNum = lastVersions[4];
		s5._versionNum = lastVersions[5];
		
		_software.add(s0);
		_software.add(s1);
		_software.add(s2);
		_software.add(s3);
		_software.add(s4);
		_software.add(s5);

		// gather the data to be used by the array
		collectListData();
		}


	@Override
	public void onConfigurationChanged(Configuration newConfig)
		{
		// Don't let orientation change restart the activity
		super.onConfigurationChanged(newConfig);
		}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
		{
		MenuItem prefItem = menu.add(Menu.NONE, 0, 0, R.string.preferences);
		prefItem.setIcon(android.R.drawable.ic_menu_preferences);
		return super.onCreateOptionsMenu(menu);
		}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
		{
		if (0 == item.getItemId())
			startActivityForResult(new Intent(this, QuickPrefsActivity.class), 1);

		return super.onOptionsItemSelected(item);
		}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
		{
		if (1 == requestCode)
			{
			_listContents.clear();
			collectListData();
			onContentChanged();
			}
		super.onActivityResult(requestCode, resultCode, data);
		}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
		{	
		SoftwareVersion sv = _software.get(position);
		Intent i = new Intent(this, ProgramDetailsActivity.class);
		i.putExtra("software", sv.toStringEx());
		
		startActivity(i);
		}
	
	
	// Bind data to the list with adapter
	private void setupListAdapter()
		{
		// the from array specifies which keys from the map
		// we want to view in our ListView
		String[] from = { "line1", "line2" };
		
		// the to array specifies the TextViews from the xml layout
		// on which we want to display the values defined in the from array
		int[] to = { android.R.id.text1, android.R.id.text2 };
		
		// create the adapter and assign it to the listview
		SimpleAdapter adapter = new SimpleAdapter(this, _listContents, android.R.layout.simple_list_item_2, from, to);
		
		setListAdapter(adapter);
		}


	// Prepare a thread with progress dialog to do the potentially long-running http connection
	private void collectListData()
		{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		int total = 0;
		for (String key : _keys)
			{
			if (sharedPrefs.getBoolean(key, false))
				total++;
			}
		
		try 
			{
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo ni = cm.getActiveNetworkInfo();
			_online = (null == ni ? false : ni.isConnectedOrConnecting());
	
			if (!_online)
				{
				Toast toast = Toast.makeText(this, R.string.toastNoNetwork, Toast.LENGTH_SHORT);
				toast.show();
				}
			} 
		catch (Exception e) 
			{
			}
	    
		final ProgressDialog dlg = new ProgressDialog(this);
		dlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dlg.setMessage(getString(R.string.progressChecking));
		dlg.setMax(total);
		dlg.show();
		
		_handler = new Handler() 
			{
			public void handleMessage(Message msg) 
				{
				if (0 == msg.what)
					{
					dlg.dismiss();
					setupListAdapter();
					}
				else if (msg.what > 0 && msg.what <= dlg.getMax())
					{
					dlg.setProgress(msg.what);
					}
				}
			};
			
		new Thread() 
			{
			public void run() 
				{
				collectListData2();
				_handler.sendEmptyMessage(0);
				}
			}.start();
		}


	// Do http query, also reading and afterwards writing the version number to disk for subsequent use   
	private void collectListData2()
		{
		int [] show = new int[_count];
		
		try
			{
			// read from local file the versions cached during last run of the app
			FileInputStream fis = openFileInput("version.txt");
			byte buffer[] = new byte[256];
			int actual = fis.read(buffer, 0, 256);
			fis.close();
			
			if (actual > 0)
				{
				String sin = new String(buffer);
				if (sin != null)
					{
					String [] cache = sin.split(",");				
					for (int i = 0; i < Math.min(show.length, cache.length); i++)
						show[i] = Integer.parseInt(cache[i].trim());
					}
				}
			}
		catch (FileNotFoundException e)
			{
			} 
		catch (IOException e)
			{
			}
		
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		String sout = "";
		
		// if set to display, then query from web the version number, or fall back to last known number
		for (int i = 0; i < show.length; i++)
			{
			if (sharedPrefs.getBoolean(_keys[i], true))
				show[i] = queryVersion(i, show[i]);
			
			sout += String.valueOf(show[i]) + ',';
			}
		
		try
			{
			// write back out to local file the new version numbers
			FileOutputStream fos = openFileOutput("version.txt", MODE_WORLD_READABLE);
			sout = sout.replaceFirst(",$", "\n");
			fos.write(sout.getBytes());
			fos.close();
			}
	    catch (FileNotFoundException e)
	    	{
	    	} 
	    catch (IOException e)
			{
			}
		}


	// Populate the list structure that is content bound to our list view 
	private int queryVersion(int index, int cachedVersionNum)
		{
		SoftwareVersion sw = _software.get(index);
		sw._versionNum = cachedVersionNum;
		if (_online)
			sw.queryVersion();
		String version = sw.versionString();
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("line1", sw._name);
		map.put("line2", version);

		_listContents.add(map);
		_handler.sendEmptyMessage(index + 1);
		return sw._versionNum;
		}
	
	private int[] retrieveLastKnownVersions()
		{
		int [] lastVersions = new int[_count];
		try
			{
			File vtfile = new File("version.txt");
			if (vtfile.exists())
				{
				FileInputStream fis;
				fis = openFileInput("version.txt"); 
				byte[] buf = new byte[1024];
				int actual = fis.read(buf, 0, 1023);
				fis.close();
				
				String ver = new String(buf, 0, actual);
				String [] v = ver.split(",");
				
				for (int i = 0; i < Math.min(lastVersions.length, v.length); i++)
					lastVersions[i] = Integer.parseInt(v[i]);
				}
			}
		catch (FileNotFoundException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		catch (IOException e)
			{
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		return lastVersions;
		}
	}
