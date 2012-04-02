package com.netsol.scraper.downloader;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import com.netsol.scraper.util.Configuration;

public class IterativeDownloaderNIE extends BaseDownloaderNIE
{
	// from Base: public static final String SEP = CacheUtilsNIE.SEP;

	/**
	 * Main logic:
	 * 1: Get initial page
	 * 2: Parse then fetch main Super Categories
	 * 3: Parse then fetch Sub Categories
	 * 4: Parse then fetch Products
	 */
	@Override
	public void start( Configuration config, Logger logger )
	{
		try
		{

			ScraperConfiguration scraperConfig = new ScraperConfiguration(config.baseConfig);
			Scraper scraper = new Scraper( scraperConfig, config.temp );
			// initURLs now writes its files to the cache dir
			// It doesn't actually read from cache, becasue we need actual connections,
			// but it saves it's work.
			String pageSource = navigateToInitialUrls( scraper, config, logger );

			if(pageSource != null)
			{
				
				// Parse the main Categories from the first page
				// NO ACTUAL DOWNLOADING
				// This particular method doesn't actually fetch/download any content
				// content of main page -> parse main Category URLs -> unique list of main Cat URLs
				List<String> superCategoryUrls = parseSuperCategoryUrls( pageSource, config, logger );
				if ( null==superCategoryUrls || superCategoryUrls.isEmpty() ) {
					logger.warn( "No superCategoryUrls" );
					// TODO: Could skip next step, but don't want to change any logic yet
				}
				else {
					logger.info( "Got " + superCategoryUrls.size() + " superCategoryUrls" );
				}

				List<String> subCategoryUrls    = fetchLevenNUrlsAndParseForLevelNPlusOneUrls( scraper, 1, superCategoryUrls, config, logger, -1 );				
				List<String> subSubCategoryUrls = fetchLevenNUrlsAndParseForLevelNPlusOneUrls( scraper, 2, subCategoryUrls, config, logger, -1 );
				List<String> level3Urls         = fetchLevenNUrlsAndParseForLevelNPlusOneUrls( scraper, 3, subSubCategoryUrls, config, logger, -1 );
				List<String> level4Urls         = fetchLevenNUrlsAndParseForLevelNPlusOneUrls( scraper, 4, level3Urls, config, logger, -1 );
				List<String> level5Urls         = fetchLevenNUrlsAndParseForLevelNPlusOneUrls( scraper, 5, level4Urls, config, logger, -1 );
				List<String> level6Urls         = fetchLevenNUrlsAndParseForLevelNPlusOneUrls( scraper, 6, level5Urls, config, logger, -1 );
				List<String> level7Urls         = fetchLevenNUrlsAndParseForLevelNPlusOneUrls( scraper, 7, level6Urls, config, logger, -1 );
				List<String> level8Urls         = fetchLevenNUrlsAndParseForLevelNPlusOneUrls( scraper, 8, level7Urls, config, logger, -1 );

				List<String> skuUrls = fetchLevenNUrlsAndParseForLevelNPlusOneUrls( scraper, 9, level8Urls, config, logger, -1 ); // 10*1000 );

				logger.info( "SKU URLs list items: " + skuUrls.size() );
				
				ParallelDownloaderNIE productFetcher = new ParallelDownloaderNIE( skuUrls, config );
				productFetcher.waitForCompletion();

				/***
				// Parse the Sub Categories
				// list of Super Category URLs -> download -> parse Subcat URLs -> unique list of Subcat URLs
                // List<String> subCategoryUrls = fetchSubCategoryUrls( scraper, superCategoryUrls, config, logger );
                List<String> subCategoryUrls = fetchSuperCategoryUrlsAndParseSubcategoryUrls( scraper, superCategoryUrls, config, logger );
                if ( null==subCategoryUrls || subCategoryUrls.isEmpty() ) {
                	logger.warn( "No subCategoryUrls" );
                }
				else {
					logger.info( "Got " + subCategoryUrls.size() + " subCategoryUrls" );
				}

                // Now the actual products!
                // list of Subcategory URLs -> download -> parse product/sku URLs -> download them??? (and no unique list)
                // handles intermediate next-page links to?
				// fetchProducts( scraper, subCategoryUrls, config, logger );
				fetchSubCategoryUrlsAndParseProductUrls( scraper, subCategoryUrls, config, logger );
                // throw new IllegalStateException( "Fetch Products temp disabled during dev" );
                ***/
			}
			else {
				logger.error( "No pageSource" );
			}
		}
		catch(Exception ex)
		{
			logger.error( "Error" , ex );
		}
	}

	@Override
    int getMaxOrdinalCategoryLevel() {
    	return 3;
    }
	@Override
    String getRegexForOrdinalCategoryLevel( int levelNumber, Configuration config, Logger logger ) {
    	if ( 1==levelNumber ) {
    		return config.regexForCategory;
    	}
    	else if ( 2==levelNumber ) {
    		return config.regexForSubcategory;    		
    	}
    	else if ( 3==levelNumber ) {
    		return config.regexForSkuUrlItem;    		
    	}
    	else {
    		//throw new IllegalArgumentException( "IterativeDownloader sites have 3 category levls, was expecting 1, 2 or 3 but got "+levelNumber );
    		return config.regexForSkuUrlItem; 
    	}
    }

	/**
	 * Parse a list of main Category URLs from source code of the starting page.
	 * This doesn't do any actual downloading.
	 * @param pageSource
	 * @param config
	 * @param logger
	 * @return
	 */
	// private List<String> fetchSuperCategoryUrls( String pageSource, Configuration config, Logger logger )
	private List<String> parseSuperCategoryUrls( String pageSource, Configuration config, Logger logger )
	{
		try
		{
			List<String> superCategoryUrls = super.extractAllValueUsingRegex(pageSource, config.regexForCategory, config.baseUrl);
			return superCategoryUrls;
		}
		catch(Exception ex)
		{
			logger.error("error" , ex);
		}
		return null;
	}

	/**
	 * Download Main Category pages and parse out the Subcategory links
	 * @param scraper
	 * @param superCategoryUrls links to the MAIN category pages
	 * @param config
	 * @param logger
	 * @return
	 */
	private List<String> _fetchSubCategoryUrls( Scraper scraper,
			List<String> superCategoryUrls, Configuration config , Logger logger
	) {
		try
		{
			String pageSource = null;

			// Changing to more effecient data structure that preserves the order
			// List<String> subCategoryUrls = new LinkedList<String>();
	        Set<String> subCategoryUrls = new LinkedHashSet<String>();

			// Foreach Super Category URL
	        // TODO: Although we could use iterators, at least some loops use i for filenames, not sure if we have it here
			for(int i = 0 ; i < superCategoryUrls.size() ; i ++)
			{
				logger.info(superCategoryUrls.get(i));
				try
				{
					/***
					scraper.addVariableToContext("url", superCategoryUrls.get(i));
					scraper.execute();
					Variable var = getVariable(scraper, "pageContent", logger);
					if(var != null)
					{
						pageSource = var.toString();
					}
					***/
					
					String url = superCategoryUrls.get(i);
		        	String dirName = config.filePath + SEP + "supercats";
					pageSource = CacheUtilsNIE.fetchUrlWithCaching( url, dirName, scraper, DEFAULT_ENABLE_SCRIPTING, logger );

				}
				catch(Exception ex)
				{
					logger.error("unable to fetch data from :" + superCategoryUrls.get(i));
				}

				// Parse URLs from the page we just downloaded
				List<String> urls = super.extractAllValueUsingRegex(pageSource, config.regexForSubcategory, null);

				// Keep any New / Unique URLs
                for(int j = 0; j < urls.size(); j++)
                {
                    String tempUrl = urls.get(j);

                    if(!superCategoryUrls.contains(tempUrl))
                    {
                        subCategoryUrls.add(tempUrl);
                    }
                }

			} // End Foreach Super Category URL

			// return subCategoryUrls;
	        // Convert back to list
	        List<String> outList = new LinkedList<String>();
	        outList.addAll( subCategoryUrls );
	        return outList;
		
		}
		catch(Exception ex)
		{
			logger.error("error" , ex);
		}
		return null;
	}
	/**
	 * Download Main Category pages and parse out the Subcategory links
	 * @param scraper
	 * @param superCategoryUrls links to the MAIN category pages
	 * @param config
	 * @param logger
	 * @return
	 */
	// List<String> fetchSubCategoryUrls( )
	private List<String> fetchSuperCategoryUrlsAndParseSubcategoryUrls( Scraper scraper,
			List<String> superCategoryUrls, Configuration config , Logger logger
	) {
		try
		{
			String pageSource = null;

			// Changing to more effecient data structure that preserves the order
			// List<String> subCategoryUrls = new LinkedList<String>();
	        Set<String> subCategoryUrls = new LinkedHashSet<String>();

			// Foreach Super Category URL
	        // TODO: Although we could use iterators, at least some loops use i for filenames, not sure if we have it here
			for(int i = 0 ; i < superCategoryUrls.size() ; i ++)
			{
				logger.info(superCategoryUrls.get(i));
				try
				{
					/***
					scraper.addVariableToContext("url", superCategoryUrls.get(i));
					scraper.execute();
					Variable var = getVariable(scraper, "pageContent", logger);
					if(var != null)
					{
						pageSource = var.toString();
					}
					***/
					
					String url = superCategoryUrls.get(i);
		        	String dirName = config.filePath + SEP + "supercats";
					pageSource = CacheUtilsNIE.fetchUrlWithCaching( url, dirName, scraper, DEFAULT_ENABLE_SCRIPTING, logger );

				}
				catch(Exception ex)
				{
					logger.error("unable to fetch data from :" + superCategoryUrls.get(i));
				}

				// Parse URLs from the page we just downloaded
				// List<String> urls = super.extractAllValueUsingRegex(pageSource, config.regexForSubcategory, null);
				parseSubcategoryUrls( subCategoryUrls, pageSource, config, logger );

				/*** (we already do this merging)
			    // Keep any New / Unique URLs
                for(int j = 0; j < urls.size(); j++)
                {
                    String tempUrl = urls.get(j);

                    if(!superCategoryUrls.contains(tempUrl))
                    {
                        subCategoryUrls.add(tempUrl);
                    }
                }
                ***/

			} // End Foreach Super Category URL

			// return subCategoryUrls;
	        // Convert back to list
	        List<String> outList = new LinkedList<String>();
	        outList.addAll( subCategoryUrls );
	        return outList;
		
		}
		catch(Exception ex)
		{
			logger.error("error" , ex);
		}
		return null;
	}

	private Set<String> parseSubcategoryUrls(
			Set<String> subCategoryUrls, String pageSource, Configuration config , Logger logger
	) {
		// Parse URLs from the page we just downloaded
		return extractAllValuesUsingRegexWithDedupe( subCategoryUrls, pageSource, config.regexForSubcategory, null, logger );
	}


}
