package com.netsol.scraper.reader;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.netsol.scraper.util.Configuration;
import com.netsol.scraper.util.ConfigurationManager;
import com.netsol.scraper.util.ReaderConfiguration;

public class ReaderDriverNIE
{
	static final String DEFAULT_RESOURCE_DIR = "resources/";
	static final String DEFAULT_BASE_NAME = "configuration";
	static final String DEFAULT_EXTENSION = ".xml";
	static final String DEFAULT_FULL_NAME = DEFAULT_RESOURCE_DIR + DEFAULT_BASE_NAME + DEFAULT_EXTENSION;
	static final String USE_DEFAULT_MARKER = "--default";

	private static void syntaxAndExit() {
		System.out.println( "Data parser for a site's downloaded/cached pages." );
		System.out.println();
		System.out.println( "Takes 1 argument, any of:" );
		System.out.println( "\tfull/path/to/some-configuration.xml (if we see a file extension or slash in the string)" );
		System.out.println( "\tCompanyName: look for " + DEFAULT_BASE_NAME + "-CompanyName.xml in directory " + DEFAULT_RESOURCE_DIR );
		System.out.println( "\t--default: use default config of " + DEFAULT_FULL_NAME );
		System.exit(1);
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


	private static Logger logger = Logger.getLogger(ReaderDriverNIE.class);
	public void startReading(Configuration config)
	{
		try
		{
			ReaderConfiguration readerConfig = (ReaderConfiguration) ConfigurationManager.loadConfiguration(ReaderConfiguration.class, config.readerConfig);
			ClearConfiguration clearConfiguration = (ClearConfiguration) ConfigurationManager.loadConfiguration(ClearConfiguration.class, config.clearConfig);
                        UnspscConfiguration unspscConfiguration = (UnspscConfiguration) ConfigurationManager.loadConfiguration(UnspscConfiguration.class, config.unspscConfig);
			ClassLoader classLoader = BaseReader.class.getClassLoader();
			Class  clazz = classLoader.loadClass(config.defaultReader) ;
			BaseReader reader = (BaseReader) clazz.newInstance();
			reader.start(config,readerConfig,clearConfiguration,unspscConfiguration,logger);

		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public static void main( String args[] ) throws IOException, SecurityException, NoSuchMethodException {
		if (args.length != 1) {
			syntaxAndExit();
		}
		// Which configuration to run
		String configName = optionToConfigNameOrNull( args[0] );
		if ( null==configName ) {
			syntaxAndExit();
		}
		logger.info("Loading Base Configuration '" + configName + "' ...");
		Configuration config = (Configuration) ConfigurationManager.loadConfiguration( Configuration.class, configName );
		if ( null==config ) {
			throw new IllegalStateException( "Got back Null Base Configuration from requested file \""+configName+'"');
		}

		String readerConfigName = config.readerConfig;
		if ( null==readerConfigName ) {
			throw new IllegalStateException( "No Reader Configuration Name (reader-config) in main config file \""+configName+'"');
		}
		logger.info("Loading Reader Configuration '" + readerConfigName + "' ...");
		ReaderConfiguration readerConfig = (ReaderConfiguration) ConfigurationManager.loadConfiguration(ReaderConfiguration.class, readerConfigName );
		if ( null==readerConfig ) {
			throw new IllegalStateException( "Got back Null Reader Configuration from requested file \""+readerConfigName+'"');
		}

		String clearConfigName = config.clearConfig;
		if ( null==clearConfigName ) {
			throw new IllegalStateException( "No Clear Configuration Name (clear-config) in main config file \""+configName+'"');
		}
		logger.info("Loading Clear Configuration '" + clearConfigName + "' ...");
		ClearConfiguration clearConfiguration = (ClearConfiguration) ConfigurationManager.loadConfiguration(ClearConfiguration.class, clearConfigName );
		if ( null==clearConfiguration ) {
			throw new IllegalStateException( "Got back Null Clear Configuration from requested file \""+clearConfigName+'"');
		}

		String unspscConfigName = config.unspscConfig;
		if ( null==unspscConfigName ) {
			throw new IllegalStateException( "No UNSPSC Configuration Name (unspsc-config) in main config file \""+configName+'"');
		}
		logger.info("Loading UNSPSC Configuration '" + unspscConfigName + "' ...");
		UnspscConfiguration unspscConfiguration = (UnspscConfiguration) ConfigurationManager.loadConfiguration(UnspscConfiguration.class, unspscConfigName );
		if ( null==unspscConfiguration ) {
			throw new IllegalStateException( "Got back Null UNSPSC Configuration from requested file \""+unspscConfigName+'"');
		}

		logger.info("All configurations Loaded !");

		BaseReaderNIE br = new BaseReaderNIE();
		// Logger log = Logger.getLogger(ReaderDriverNIE.class);
		Logger log = Logger.getLogger( BaseReaderNIE.class );
		br.start( config, readerConfig, clearConfiguration, unspscConfiguration, log );

	}

	public static void _main(String args[]) throws IOException, SecurityException, NoSuchMethodException{
		Configuration config = (Configuration) ConfigurationManager.loadConfiguration(Configuration.class, "D:\\Projects\\Index\\resources/configuration.xml");//to be removed
		ReaderConfiguration readerConfig = (ReaderConfiguration) ConfigurationManager.loadConfiguration(ReaderConfiguration.class, config.readerConfig);//to be removed
		ClearConfiguration clearConfiguration = (ClearConfiguration) ConfigurationManager.loadConfiguration(ClearConfiguration.class, config.clearConfig);
                UnspscConfiguration unspscConfiguration = (UnspscConfiguration) ConfigurationManager.loadConfiguration(UnspscConfiguration.class, config.unspscConfig);
		BaseReader br  = new BaseReader();
		Logger log = Logger.getLogger(ReaderDriverNIE.class);
		br.start(config,readerConfig,clearConfiguration,unspscConfiguration,log);
	}

}
