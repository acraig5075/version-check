package za.co.knowbase.versioncheck;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class QuickPrefsActivity extends PreferenceActivity
	{
//	private boolean _initial = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
		{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		}


	@Override
	protected void onStart() 
		{
		super.onStart();

//		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//	    _initial = sharedPrefs.getBoolean("background", false);
		}


	@Override
	protected void onStop() 
		{
		super.onStop();
		
//		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
//	    boolean subseq = sharedPrefs.getBoolean("background", false);
//	    boolean start = subseq;
//	    
//	    if (subseq != _initial)
//		    {
//	    	if (start)
//	    		startService(new Intent(this, BackgroundService.class));
//	    	else
//	    		stopService(new Intent(this, BackgroundService.class));
//		    }
		}
	}
