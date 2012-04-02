package com.netsol.scraper.reader;

import org.apache.log4j.Logger;

import com.netsol.scraper.downloader.DownloadDriver;
import com.netsol.scraper.util.Configuration;
import com.netsol.scraper.util.ConfigurationManager;
import com.netsol.scraper.util.ReaderConfiguration;

public class ReaderDriver 
{
	private Logger logger = Logger.getLogger(ReaderDriver.class);
	public void startReading(Configuration config)
	{
		try
		{
			ReaderConfiguration readerConfig = (ReaderConfiguration) ConfigurationManager.loadConfiguration(ReaderConfiguration.class, config.readerConfig);
			ClassLoader classLoader = DownloadDriver.class.getClassLoader();
			System.out.print(config.defaultReader);
			Class  clazz = classLoader.loadClass(config.defaultReader) ;
			BaseReader reader = (BaseReader) clazz.newInstance();
			reader.start(config ,readerConfig, logger);
		}
		catch(Exception exception)
		{
			logger.error("error", exception);
		}
	}

}
