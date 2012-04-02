package com.netsol.scraper.downloader;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import com.netsol.scraper.util.Configuration;

public class IterativeDownloader extends BaseDownloader
{

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
			String pageSource = navigateToInitialUrls( scraper, config, logger );

			if(pageSource != null)
			{

                // content, dir, file, logger
				writeToFile( pageSource,
                         config.filePath + "/main",
                         "/start-page",
                         logger);
				
				// Parse the main Categories from the first page
				// This particular method doesn't actually fetch/download any content
				// content of main page -> parse main Category URLs -> unique list of main Cat URLs
				List<String> superCategoryUrls = fetchSuperCategoryUrls( pageSource, config, logger );
				if ( null==superCategoryUrls || superCategoryUrls.isEmpty() ) {
					logger.warn( "No superCategoryUrls" );
					// TODO: Could skip next step, but don't want to change any logic yet
				}
				else {
					logger.info( "Got " + superCategoryUrls.size() + " superCategoryUrls" );
				}

				// Parse the Sub Categories
				// list of Super Category URLs -> download -> parse Subcat URLs -> unique list of Subcat URLs
                List<String> subCategoryUrls = fetchSubCategoryUrls( scraper, superCategoryUrls, config, logger );
                if ( null==subCategoryUrls || subCategoryUrls.isEmpty() ) {
                	logger.warn( "No subCategoryUrls" );
                }
				else {
					logger.info( "Got " + subCategoryUrls.size() + " subCategoryUrls" );
				}

                // Now the actual products!
                // list of Subcategory URLs -> download -> parse product/sku URLs -> download them??? (and no unique list)
                // handles intermediate next-page links to?
				fetchProducts( scraper, subCategoryUrls, config, logger );
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

	/**
	 * Parse a list of main Category URLs from source code of the starting page.
	 * This doesn't do any actual downloading.
	 * @param pageSource
	 * @param config
	 * @param logger
	 * @return
	 */
	private List<String> fetchSuperCategoryUrls( String pageSource, Configuration config, Logger logger )
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
	private List<String> fetchSubCategoryUrls( Scraper scraper,
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
			//for(int i = 0 ; i < 1 ; i ++)
			{
				logger.info(superCategoryUrls.get(i));
				System.out.println("superCategoryUrl: " + superCategoryUrls.get(i));
				try
				{
					scraper.addVariableToContext("url", superCategoryUrls.get(i));
					scraper.execute();
					Variable var = getVariable(scraper, "pageContent", logger);
					if(var != null)
					{
						pageSource = var.toString();
					}

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


}
