package za.co.knowbase.versioncheck;

import java.io.File;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ProgramDetailsActivity extends Activity
	{	
	@Override
	public void onCreate(Bundle savedInstanceState)
		{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.details);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
			{
			String value = extras.getString("software");
			SoftwareVersion sv = new SoftwareVersion(value);
			
			ImageButton image = (ImageButton) findViewById(R.id.screen_top_info);
			if (sv._name.startsWith("AllyCAD"))
				image.setImageResource(R.drawable.allycad48);
			else
				image.setImageResource(R.drawable.civdes48);
			
			TextView text1 = (TextView) findViewById(R.id.details_top);
			text1.setText(sv._name + " - " + sv.versionString());
			
			Button updateButton1 = (Button) findViewById(R.id.x86button);
			Button updateButton2 = (Button) findViewById(R.id.x64button);
			
			boolean online = true;
			try 
				{
				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo ni = cm.getActiveNetworkInfo();
				online = (null == ni ? false : ni.isConnectedOrConnecting());
				}
			catch (Exception e) 
				{
				}

			boolean enabled = (0 != sv._versionNum && online);
			updateButton1.setEnabled(enabled);
			updateButton2.setEnabled(enabled);
			
			if (sv._oldType)
				{
				updateButton1.setText("Update");
				updateButton2.setVisibility(View.INVISIBLE);
				}
			}
		}


	public void onClick1(View v) 
		{
		downloadFile(1);
		}


	public void onClick2(View v)
		{
		downloadFile(2);
		}


	private void downloadFile(int option)
		{
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
			{
			String value = extras.getString("software");
			SoftwareVersion sv = new SoftwareVersion(value);

			final String url = sv.makeSourceUrl(option, sv._versionNum);
			final String filename = sv.makeDestinationFilename(option, sv._versionNum);

			if (!fileAlreadyExists(filename))
				{
				final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
				if (sdkVersion > Build.VERSION_CODES.FROYO)
					{
					// Gingerbread and later can use the Android DownloadManager to do the hard work
					DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
					Request request = new Request(Uri.parse(url));
					request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
					dm.enqueue(request);
					
					Intent i = new Intent();
					i.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
					startActivity(i);
					}
				else
					{
					// Froyo and before need a background task and progress bar
					DownloadFileTask task = new DownloadFileTask(this);
					task.execute(url, filename);
					}
				}
			}
		}


	private boolean fileAlreadyExists(String filename)
		{
		final File f = new File(filename);
		if (f.exists())
			{
			String msg = String.format(getString(R.string.toastAlreadyExists), filename);			
			Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
			toast.show();
			return true;
			}
		return false;
		}
	}
