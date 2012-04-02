package com.netsol.scraper;

import org.apache.log4j.Logger;

import com.netsol.scraper.downloader.DownloadDriver;
import com.netsol.scraper.downloader.DownloadDriverNIE;
import com.netsol.scraper.reader.ReaderDriver;
import com.netsol.scraper.util.Configuration;
import com.netsol.scraper.util.ConfigurationManager;

public class EngineNIE
{
	static final String DEFAULT_RESOURCE_DIR = "resources/";
	static final String DEFAULT_BASE_NAME = "configuration";
	static final String DEFAULT_EXTENSION = ".xml";
	static final String DEFAULT_FULL_NAME = DEFAULT_RESOURCE_DIR + DEFAULT_BASE_NAME + DEFAULT_EXTENSION;
	static final String USE_DEFAULT_MARKER = "--default";

	private static void syntaxAndExit() {
		System.out.println( "Spider a site by downloading pages and scraping some links." );
		System.out.println();
		System.out.println( "Takes 1 argument, any of:" );
		System.out.println( "\tfull/path/to/some-configuration.xml (if we see a file extension or slash in the string)" );
		System.out.println( "\tCompanyName: look for " + DEFAULT_BASE_NAME + "-CompanyName.xml in directory " + DEFAULT_RESOURCE_DIR );
		System.out.println( "\t--default: use default config of " + DEFAULT_FULL_NAME );
	}
	private static String optionToConfigNameOrNull( String option ) {
		if ( null==option || option.trim().startsWith("-") ) {
			return null;
		}
		if ( option.trim().equalsIgnoreCase(USE_DEFAULT_MARKER) ) {
			return DEFAULT_FULL_NAME;
		}
		// Look for any mangled attempts at -help or -default or something
		// A hyphen anywhere else is fine
		if ( option.trim().startsWith("-") ) {
			return null;
		}
		// Full path, they know what they're doing
		if ( option.indexOf('.')>=0 || option.indexOf('.')>=0 ) {
			return option;
		}
		// Should be a company name then
		return DEFAULT_RESOURCE_DIR + DEFAULT_BASE_NAME + '-' + option.trim() + DEFAULT_EXTENSION;
	}

	private static Logger logger  = Logger.getLogger(EngineNIE.class);
	public static void main(String args[])
	{
		if (args.length != 1) {
			syntaxAndExit();
		}
		// Which configuration to run
		String configName = optionToConfigNameOrNull( args[0] );
		if ( null==configName ) {
			syntaxAndExit();
		}
		try
		{
			System.out.println("maxMemory: " + Runtime.getRuntime().maxMemory());
			logger.info("Loading Configuration '" + configName + "' ...");
			Configuration config = (Configuration) ConfigurationManager.loadConfiguration( Configuration.class, configName );
			logger.info("Configuration Loaded !");


			logger.info("Intializing Downloader....");
			DownloadDriverNIE downloadDriver = new DownloadDriverNIE();
			downloadDriver.startDownloading(config);
			logger.info("Downloading Finished !");

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
}
