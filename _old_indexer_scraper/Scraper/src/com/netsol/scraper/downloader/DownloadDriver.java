package com.netsol.scraper.downloader;

import org.apache.log4j.Logger;

import com.netsol.scraper.util.Configuration;

public class DownloadDriver 
{
	private Logger logger  = Logger.getLogger(DownloadDriver.class);
	
	public void startDownloading(Configuration config)
	{
		try
		{
			
			ClassLoader classLoader = DownloadDriver.class.getClassLoader();
			System.out.print(config.defaultDownloader);
			Class  clazz = classLoader.loadClass(config.defaultDownloader) ;
			BaseDownloader downloader = (BaseDownloader) clazz.newInstance();
			downloader.start(config , logger);
		}
		catch(Exception exception)
		{
			logger.error("error", exception);
		}
	}
	
}
