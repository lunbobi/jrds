package jrds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * An ugly class suposed to manage properties
 * should be reworked
 * @author Fabrice Bacchella
 */
public class PropertiesManager {
	static private PropertiesManager instance;
	static final private Logger logger = Logger.getLogger(PropertiesManager.class);

	private Properties properties = new Properties();
	
	
	private void printError(String s, Throwable throwable) {
		logger.warn("Invalid value for " + s + " (" + throwable.getLocalizedMessage() + ").");
	}
	
	private String getParameter(String key, String defaultValue,
			boolean doTrim) {
		String returnValue = properties.getProperty(key);
		if (doTrim && returnValue != null) {
			returnValue = returnValue.trim();
		}
		if (returnValue == null) {
			returnValue = defaultValue;
			properties.setProperty(key, defaultValue);
		}
		return returnValue;
	}
	
	private String getParameter(String s, String s1) {
		return getParameter(s, s1, true);
	}
	
	private int parseInteger(String s) throws NumberFormatException {
		Integer integer = null;
		if (s != null) {
			if (s.startsWith("#")) {
				integer = Integer.valueOf(s.substring(1), 16);
			}
			else
				if (s.startsWith("0x")) {
					integer = Integer.valueOf(s.substring(2), 16);
				}
				else
					if (s.startsWith("0") && s.length() > 1) {
						integer = Integer.valueOf(s.substring(1), 8);
					}
					else {
						integer = Integer.valueOf(s);
					}
		}
		return integer.intValue();
	}
	
	private URL gh(URL url, String s) throws MalformedURLException {
		URL url1 = null;
		if (s != null && s.length() > 0) {
			if (s.indexOf(58) != -1) {
				url1 = new URL(s);
			}
			else
				if (s.startsWith("/")) {
					url1 = new URL(url.getProtocol(), url.getHost(), url.getPort(), s);
				}
				else {
					url1 = new URL(url, s);
				}
		}
		return url1;
	}
	
	private URL yc(URL url, String s, String s1, String s2) {
		URL url1 = null;
		try {
			url1 = gh(url, s2);
			if (s1 != null && s1.length() > 0) {
				url1 = gh(url, s1);
			}
		}
		catch (MalformedURLException malformedurlexception) {
			printError(s, malformedurlexception);
		}
		return url1;
	}
	
	public void join(URL url) {
		try {
			InputStream inputstream = url.openStream();
			properties.load(inputstream);
			inputstream.close();
		}
		catch (IOException ex) {
			logger.warn("Invalid URL: " + ex.getLocalizedMessage());
		}
	}
	
	public void join(Properties moreProperties) {
		properties.putAll(moreProperties);
	}
	
	public void join(File propFile)
	{
		logger.debug("Using propertie file " + propFile.getAbsolutePath());
		try {
			InputStream inputstream = new FileInputStream(propFile);
			properties.load(inputstream);
			inputstream.close();
		} catch (IOException ex) {
			logger.warn("Invalid properties file " + propFile.getAbsolutePath() + ": " + ex.getLocalizedMessage());
		}
	}
	
	public void join(InputStream propStream)
	{
		try {
			properties.load(propStream);
			propStream.close();
		} catch (IOException ex) {
			logger.warn("Invalid properties stream " + propStream + ": " + ex.getLocalizedMessage());
		}
	}

	public Properties getProperties()
	{
		return properties;
	}
	
	private PropertiesManager()
	{
		join(System.getProperties());
		update();
	}
	
	public void update()
	{
		urlperfpath = getParameter("urlperfpath","./HTTPTest");
		configfilepath = getParameter("configfilepath", "config.xml");
		rrddir = getParameter("rrddir", "probe");
		fileSeparator = getParameter("file.separator", "/");
		logfile = getParameter("logfile", "jrds.log");
		loglevel = Level.toLevel(getParameter("loglevel", "DEBUG"));
		resolution = parseInteger(getParameter("resolution", "300"));
		collectorThreads = parseInteger(getParameter("collectorThreads", "1"));
		dbPoolSize = parseInteger(getParameter("dbPoolSize", "10"));
		syncPeriod = parseInteger(getParameter("syncPeriod", "-1"));
		
	}
	
	
	static public PropertiesManager getInstance()
	{
		if(instance == null)
			instance = new PropertiesManager();
		return instance;
	}
	public String urlperfpath;
	public String configfilepath;
	public String urlpngroot;
	public String rrddir;
	public String fileSeparator;
	public String logfile;
	public Level loglevel;
	public int resolution;
	public int collectorThreads;
	public int dbPoolSize;
	public int syncPeriod;
	
}
