package com.netsol.scraper;

import org.apache.log4j.Logger;

import com.netsol.scraper.reader.ReaderDriver;
import com.netsol.scraper.util.Configuration;
import com.netsol.scraper.util.ConfigurationManager;



public class Engine
{
	private static Logger logger  = Logger.getLogger(Engine.class);
	public static void main(String argu[])
	{
		try
		{
			logger.info("Loading Configuration.....");
			Configuration config = (Configuration) ConfigurationManager.loadConfiguration(Configuration.class, "resources/configuration.xml");
			logger.info("Configuration Loaded !");


			logger.info("Intializing Downloader....");
			//DownloadDriver downloadDriver = new DownloadDriver();
			//downloadDriver.startDownloading(config);
			logger.info("Downloading Finished !");

			logger.info("Starting Reading....");
			ReaderDriver readerDriver = new ReaderDriver();
                        readerDriver.startReading(config);
			logger.info("Reading Finished !");

			//DAOManager daoManager = new DAOManager();
			//daoManager.updateCatalog(catalogs);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
