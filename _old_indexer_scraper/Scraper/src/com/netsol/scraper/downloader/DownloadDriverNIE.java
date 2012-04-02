package com.netsol.scraper.downloader;

import org.apache.log4j.Logger;

import com.netsol.scraper.util.Configuration;

public class DownloadDriverNIE 
{
	private Logger logger  = Logger.getLogger(DownloadDriverNIE.class);
	
	public void startDownloading(Configuration config)
	{
		try
		{
			
			ClassLoader classLoader = DownloadDriverNIE.class.getClassLoader();
			System.out.print(config.defaultDownloader);
			Class  clazz = classLoader.loadClass(config.defaultDownloader) ;
			BaseDownloaderNIE downloader = (BaseDownloaderNIE) clazz.newInstance();
			downloader.start(config , logger);
		}
		catch(Exception exception)
		{
			logger.error("error", exception);
		}
	}
	
}
