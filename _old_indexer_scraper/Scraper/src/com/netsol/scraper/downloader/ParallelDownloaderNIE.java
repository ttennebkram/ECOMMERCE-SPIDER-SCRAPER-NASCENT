package com.netsol.scraper.downloader;

// import groovyjarjarantlr.collections.List;
import java.util.List;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import com.netsol.scraper.util.Configuration;

public class ParallelDownloaderNIE {
	private static final int NUM_THREADS = 30;

	static final char SEP = CacheUtilsNIE.SEP;
    // Mimick original Config behavior
    static final boolean DEFAULT_ENABLE_SCRIPTING = BaseDownloaderNIE.DEFAULT_ENABLE_SCRIPTING;

	Configuration config;
	Logger logger;
	private Queue<String> commonQueue = new LinkedList<String>();
	// Scraper scraper;
	// ScraperConfiguration scraperConfig;
	
    private Collection<Thread> allMyThreads;

	public ParallelDownloaderNIE( List<String> urls, Configuration config /*, Logger logger*/ )
			throws FileNotFoundException
	{
		this.config = config;
		// this.logger = logger;
		this.logger = Logger.getLogger( ParallelDownloaderNIE.class );
		// this.commonQueue = new LinkedList<String>();
		this.commonQueue = new ConcurrentLinkedQueue<String>();
		this.commonQueue.addAll( urls );
		initThreads();
	}
	private void initThreads() throws FileNotFoundException {
	    allMyThreads = new ArrayList<Thread>();
	    logger.info( "Creating " + NUM_THREADS + " threads" );
	    // Loop, create threads
	    for ( int i=0; i<NUM_THREADS; i++ ) {
		    String tName = "FetchPage_T"+i;
			Logger tLogger = Logger.getLogger( tName );
		    Runnable fetcher = new FetchPage( commonQueue, config, tLogger );
		    Thread t = new Thread( fetcher );
		    t.setName( tName );
		    allMyThreads.add( t );
		    t.start();
	    }
	}
	void waitForCompletion() {
		int numRunning = 0;
		while( (numRunning = checkRunningThreads()) > 0 ) {
			logger.info( "THREADS = " + numRunning );
			try {
				Thread.sleep( 1000 );
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				break;
			}
		}
	}
	int checkRunningThreads() {
	    int runCount = 0;
	    for ( Thread t : allMyThreads ) {
	    	if ( t.isAlive() ) {
	    		runCount++;
	        }
	    }
	    return runCount;
	}	
	
	class FetchPage implements Runnable
	// class FetchPage extends Thread
	{
		// ScraperConfiguration scraperConfig;
		Configuration config;
		Logger logger;
		Queue commonQueue;
		Scraper scraper;

		public FetchPage( Queue commonQueue, Configuration config, Logger logger ) throws FileNotFoundException {
			this.config = config;
			this.logger = logger;
			this.commonQueue = commonQueue;
			init();
		}
		void init() throws FileNotFoundException {
			ScraperConfiguration scraperConfig = new ScraperConfiguration(config.baseConfig);
			scraper = new Scraper( scraperConfig, config.temp );
			// initURLs now writes its files to the cache dir
			// It doesn't actually read from cache, becasue we need actual connections,
			// but it saves it's work.
			String pageSource = navigateToInitialUrls( scraper, config, logger );
		}

		// No @Override for Java 1.5
        public void run() {
            // do stuff
        	String url = null;
        	while ( null != (url=(String)commonQueue.poll() ) ) {
	        	String dirName = config.filePath + SEP + "products";
				String pageSource = CacheUtilsNIE.fetchUrlWithCaching( url, dirName, scraper, DEFAULT_ENABLE_SCRIPTING, logger );
        	}
        }

	    protected String navigateToInitialUrls(Scraper scraper,
                Configuration config, Logger logger)
		{
			String pageSource = null;
			Variable var = null;
			String url = null;
			try
			{
				String startUrl = config.url;
				scraper.addVariableToContext("url", startUrl);
				scraper.execute();
				var = getVariable(scraper, "pageContent", logger);
				pageSource = var.toString();
			
				// We don't cache these pages, but we'd like to see them
				// String dirName = config.filePath + SEP + "init";
				// String fullName = CacheUtilsNIE.calcUrlCacheFileNameForURL( dirName, startUrl );
				// CacheUtilsNIE.writeToFile( pageSource, fullName, logger );
			
				// For all other URLs
				for (int i = 0; i < config.listOfInitialUrls.size(); i++)
				{
					if (config.isApplyRegixToIntUrl)
					{
						url = extractValueUsingRegex(pageSource,
					                      config.listOfInitialUrls.get(i));
						/***
						if (config.baseUrl != null && !validateUrl(url))
						{
						url = config.baseUrl + url;
						}
						***/
						url = CacheUtilsNIE.normalizeUrl( url, config.baseUrl );
					}
					else
					{
						url = config.listOfInitialUrls.get(i);
				
						/***
						if (config.baseUrl != null && !validateUrl(url))
						{
							url = config.baseUrl + url;
						}
						***/
						url = CacheUtilsNIE.normalizeUrl( url, config.baseUrl );
				
						config.isApplyRegixToIntUrl = true;
					}
					try
					{
						// TODO: Not using our Util class yet for these special fetches
						scraper.addVariableToContext("url", url);
						if(!config.enableScripting) {
							scraper.removeRunningFunction();
						}
						scraper.execute();
						var = getVariable(scraper, "pageContent",
				           logger);
						pageSource = var.toString();
						// We don't cache these pages, but we'd like to see them
						// fullName = CacheUtilsNIE.calcUrlCacheFileNameForURL( dirName, url );
						// CacheUtilsNIE.writeToFile( pageSource, fullName, logger );
			
					}
					catch (Exception exp)
					{
						logger.error("error", exp);
					}
				}
			}
			catch (Exception ex)
			{
				logger.error("error", ex);
			}
			return pageSource;
		}
	    protected Variable getVariable(Scraper scraper, String varName,
                Logger logger)
		{
			try
			{
				return scraper.getContext().getVar(varName);
			}
			catch (Exception ex)
			{
				logger.error("error", ex);
				return null;
			}
		}
	    protected String extractValueUsingRegex(String pageSource, String regex)
	    	throws Exception
		{
		    String value = null;
		    Pattern pattern = Pattern.compile(regex);
		    Matcher matchList = pattern.matcher(pageSource);
		    String tmpmatch = "";
		    if (matchList.find())
		    {
		        if (!regex.contains("(.*?)"))
		        {
		            tmpmatch = matchList.group();
		        }
		        else
		        {
		            tmpmatch = matchList.group(1);
		        }
		
		        if (!tmpmatch.equals(""))
		        {
		            value = tmpmatch.replaceAll(" ", "%20").trim();
		        }
		    }
		    return value;
		}

    }

}
