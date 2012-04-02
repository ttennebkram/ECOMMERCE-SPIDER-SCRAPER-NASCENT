package com.netsol.scraper.reader;

import java.io.IOException;

import org.apache.log4j.Logger;

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

	public static void main(String args[]) throws IOException, SecurityException, NoSuchMethodException{
		Configuration config = (Configuration) ConfigurationManager.loadConfiguration(Configuration.class, "resources/configuration.xml");//to be removed
		ReaderConfiguration readerConfig = (ReaderConfiguration) ConfigurationManager.loadConfiguration(ReaderConfiguration.class, config.readerConfig);//to be removed
		ClearConfiguration clearConfiguration = (ClearConfiguration) ConfigurationManager.loadConfiguration(ClearConfiguration.class, config.clearConfig);
                UnspscConfiguration unspscConfiguration = (UnspscConfiguration) ConfigurationManager.loadConfiguration(UnspscConfiguration.class, config.unspscConfig);
        Logger log = Logger.getLogger(ReaderDriver.class);                
		if (config.supplierName.equals("newpig")) {
			BaseReaderNewpig br  = new BaseReaderNewpig();
    		br.start(config,readerConfig,clearConfiguration,unspscConfiguration,log);
		}		
		else {
			BaseReader br  = new BaseReader();
    		br.start(config,readerConfig,clearConfiguration,unspscConfiguration,log);
		}
	}

}
