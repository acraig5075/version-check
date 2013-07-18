package za.co.knowbase.versioncheck;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class BackgroundService extends Service 
	{
	@Override
	public IBinder onBind(Intent arg0) 
		{
		return null;
		}

	@Override
	public void onCreate() 
		{
		super.onCreate();
		Toast.makeText(this, "Background service started ...", Toast.LENGTH_LONG).show();
		}

	@Override
	public void onDestroy() 
		{
		super.onDestroy();
		Toast.makeText(this, "Background service stopped ...", Toast.LENGTH_LONG).show();
		}
	}