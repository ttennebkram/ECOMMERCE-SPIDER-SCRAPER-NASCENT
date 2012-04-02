package com.netsol.scraper;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator; 
import com.netsol.scraper.downloader.DownloadDriver;
import com.netsol.scraper.reader.ReaderDriver;
import com.netsol.scraper.util.Configuration;
import com.netsol.scraper.util.ConfigurationManager;



public class Engine
{
	private static Logger logger  = Logger.getLogger(Engine.class);
	public static void main(String argu[])
	{
		//BasicConfigurator.configure();
		try
		{
			logger.info("Loading Configuration.....");
			Configuration config = (Configuration) ConfigurationManager.loadConfiguration(Configuration.class, "resources/configuration.xml");
			logger.info("Configuration Loaded !");


			logger.info("Intializing Downloader....");
			DownloadDriver downloadDriver = new DownloadDriver();
			downloadDriver.startDownloading(config);
			logger.info("Downloading Finished !");

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
