package za.co.knowbase.versioncheck;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class SoftwareVersion
	{
	public static final String ERRORVERSION = "<unknown>";
	
	public String _name;
	public String _domain;
	public String _versionFile;
	public boolean _oldType;
	public String _updateFile1;
	public String _updateFile2;
	public int _versionNum;


	public SoftwareVersion(String tokens)
		{
		fromStringEx(tokens);
		}


	private void fromStringEx(String tokens)
		{
		String [] s = tokens.split(":");
		if (s.length == 7)
			{
			_name = s[0];
			_domain = s[1];
			_versionFile = s[2];
			_oldType = Boolean.parseBoolean(s[3]);
			_updateFile1 = s[4];
			_updateFile2 = s[5];
			_versionNum = Integer.parseInt(s[6]);
			}		
		}


	public String toStringEx()
		{
		String format = "%s:%s:%s:%s:%s:%s:%d";
		String tokens = String.format(format, _name, _domain, _versionFile, (_oldType ? "true" : "false"), _updateFile1, _updateFile2, _versionNum);
		return tokens;
		}


	public String versionString()
		{
		if (0 == _versionNum)
			return ERRORVERSION;
		
		return "Release " + String.valueOf(_versionNum);
		}


	public String makeDestinationFilename(int option, int version)
		{
		return String.format(2 == option ? _updateFile2 : _updateFile1, version);
		}


	public String makeSourceUrl(int option, int version)
		{
		String filename = makeDestinationFilename(option, version);
		return "http://" + _domain + "Downloads/" + filename;
		}


	public void queryVersion()
		{
		try
			{
			URL url = new URL("http://" + _domain + _versionFile);
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStreamReader content = new InputStreamReader(conn.getInputStream());
			
			char [] buffer = new char[128];
			int length = content.read(buffer);
			if (length > 0)
				{
				String serverString = new String(buffer).trim();

				_versionNum = versionFromServerString(!_oldType, serverString);
				}
			} 
		catch (MalformedURLException e)
			{
			_versionNum = 0;
			return;
			} 
		catch (IOException e)
			{
			_versionNum = 0;
			return;
			}
		}


	// Extract version number from server string
	private int versionFromServerString(boolean newFormat, String version)
		{
		if (newFormat) // server string having format "major,minor,release,build"
			{
			String[] tokens = version.split(",");
			if (4 == tokens.length)
				{
				int rel = Integer.parseInt(tokens[2].trim());
				if (rel >= 100)
					rel -= 100;
				return rel;
				}
			}
		else // server string having format "Version x (Build y)"
			{
			if (version.endsWith(")"))
				{
				String[] tokens = version.replace(")", "").split(" ");
				if (tokens.length > 0)
					return Integer.parseInt(tokens[tokens.length - 1].trim());
				}
			}
		return 0;
		}
	}
