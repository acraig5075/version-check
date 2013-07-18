package za.co.knowbase.versioncheck;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class DownloadFileTask extends AsyncTask<String, Integer, Void> 
	{
	private Context _context; 
	private ProgressDialog _progressDialog;     
	private static final int MAX_BUFFER_SIZE = 1024; //1kb
	private boolean _cancelled = false;
	private String _filename = "";
		
	public  DownloadFileTask(Context c)
		{
		_context = c;
		}
	
	@Override
	protected Void doInBackground(String... files) 
		{
		try 
			{
			URL url = new URL(files[0]);
			
			//File dest = new File(_context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), files[1]);
			File dest = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), files[1]);
			dest.mkdirs();
			_filename = dest.getAbsolutePath();
			FileOutputStream fos = new FileOutputStream(dest);

			URLConnection ucon = url.openConnection();
			double fileSize = ucon.getContentLength();
			double downloaded = 0.0;
			
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, MAX_BUFFER_SIZE);
			
			byte[] baf = new byte[MAX_BUFFER_SIZE];
			int actual = 0;
			while (actual != -1) 
				{
				if (_cancelled)
					break;
				fos.write(baf, 0, actual);
				actual = bis.read(baf, 0, 1024);
				if (-1 == actual)
					{
					publishProgress(100);
					break;
					}
				downloaded += actual;
				publishProgress((int) ((downloaded / fileSize) * 100));
				}
			fos.close();
			bis.close();
			} 
		catch (Exception e) 
			{
			System.out.println("Exception: " + e);
			}// end of catch
		return null;
		}

	@Override
	protected void onProgressUpdate(Integer... changed) 
		{
		_progressDialog.setProgress(changed[0]);
		}


	@Override
	protected void onPreExecute()
		{
		_progressDialog = new ProgressDialog(_context);
		_progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		_progressDialog.setMessage(_context.getString(R.string.progressDownloading));
		_progressDialog.setButton(_context.getString(R.string.progressCancel), new DialogInterface.OnClickListener() 
			{
			public void onClick(DialogInterface dialog, int which) 
				{
				_cancelled = true;
				// Use either finish() or return() to either close the activity or just the dialog
				return;
				}
			});
		_progressDialog.show();
		}


	@Override
	protected void onPostExecute(Void result)
		{
		File target = new File(_filename);
		if (_cancelled)
			{
			// cleanup partially downloaded file
			if (target.exists())
				target.delete();
			}
		else
			{
			_progressDialog.dismiss();
			
			// friendly message that download was successfull
			String msg = _context.getString(R.string.toastSuccessful);
			Toast toast = Toast.makeText(_context, msg, Toast.LENGTH_SHORT);
			toast.show();
			}
		}
	}