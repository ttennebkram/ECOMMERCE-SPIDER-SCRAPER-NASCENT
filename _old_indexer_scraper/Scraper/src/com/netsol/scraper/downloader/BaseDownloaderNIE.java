package com.netsol.scraper.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import com.netsol.scraper.util.Configuration;

public class BaseDownloaderNIE
{
	public static final char SEP = CacheUtilsNIE.SEP;

	// protected List<String> vistedPages = new LinkedList<String>();
    // protected List<String> vistedSkues = new LinkedList<String>();
	protected Set<String> foundUrls = new LinkedHashSet<String>();
    protected Set<String> foundSkueUrls = new LinkedHashSet<String>();
    
    // Mimick original Config behavior
    public static final boolean DEFAULT_ENABLE_SCRIPTING = true;
    
    
  
    int getMaxOrdinalCategoryLevel() {
    	return 2;
    }
    String getRegexForOrdinalCategoryLevel( int levelNumber, Configuration config, Logger logger ) {
    	if ( 1==levelNumber ) {
    		return config.regexForCategory;
    	}
    	else if ( 2==levelNumber ) {
    		return config.regexForSkuUrlItem;    		
    	}
    	else {
    		throw new IllegalArgumentException( "BaseDownloader sites only have 2 category levles, was expecting 1 or 2 but got "+levelNumber );
    	}
    }

    // TODO: Probably needs more refactoring
    // TODO: Doesn't boostrap for level 0
    // TODO: Doesn't handle "next page" for product listings, OK for now to just get samples
    List<String> fetchLevenNUrlsAndParseForLevelNPlusOneUrls( Scraper scraper,
			int levelNumber, List<String> urlsForLevelN, Configuration config , Logger logger,
			int optLimitSampleSizeGoal
	) {
    	logger.info( "================== Level " + levelNumber + " =========================");

    	try
		{
			String pageSource = null;

			// Changing to more effecient data structure that preserves the order
			// List<String> subCategoryUrls = new LinkedList<String>();
	        Set<String> urlsForLevelNPlusOne = new LinkedHashSet<String>();

			// Foreach Super Category URL
	        // TODO: Although we could use iterators, at least some loops use i for filenames, not sure if we have it here
			for(int i = 0 ; i < urlsForLevelN.size() ; i ++)
			{
				String url = urlsForLevelN.get(i);
				logger.info( "Processing: level N="+levelNumber + " URL=\""+url+'"');
				try
				{
		        	String dirName = config.filePath + SEP + "level-"+levelNumber;
					pageSource = CacheUtilsNIE.fetchUrlWithCaching( url, dirName, scraper, DEFAULT_ENABLE_SCRIPTING, logger );

				}
				catch(Exception ex)
				{
					logger.error( "Unable to fetch URL \"" + url + "\", Exception: " + ex );
				}

				// Parse URLs from the page we just downloaded
			    String regexLevelPlusOne = null;
			    if ( levelNumber < getMaxOrdinalCategoryLevel() ) {
			    	regexLevelPlusOne = getRegexForOrdinalCategoryLevel( levelNumber+1, config, logger );
			    }
			    else {
			    	regexLevelPlusOne = config.regexForSkuUrl;
			    	// regexLevelPlusOne = config.regexForNextSku;
			    }
			    logger.info( "PARSE: Level N="+levelNumber + ", but parsing with regex for level N+1 of \""+regexLevelPlusOne+"\" from text of URL \""+url+'"');
				urlsForLevelNPlusOne = extractAllValuesUsingRegexWithDedupe( urlsForLevelNPlusOne, pageSource, regexLevelPlusOne, config.baseUrl, logger );

				// Are we limiting our sample size?
				if ( optLimitSampleSizeGoal > 0 ) {
					if ( urlsForLevelNPlusOne.size() >= optLimitSampleSizeGoal ) {
						logger.info( "INTENTIONAL PREMATURE LEVEL STOP at level N="+levelNumber + ": Asked to only get approx "+optLimitSampleSizeGoal+" and now have "+urlsForLevelNPlusOne.size() );
						break;
					}
				}
			} // End Foreach Super Category URL

			// return subCategoryUrls;
	        // Convert back to list
	        List<String> outList = new LinkedList<String>();
	        outList.addAll( urlsForLevelNPlusOne );
	        return outList;
		
		}
		catch(Exception ex)
		{
			logger.error("error" , ex);
		}
		return null;
	}

    String registerNewUrlOrNull( String inUrl, Logger logger ) {
    	if ( null==inUrl ) {
    		logger.warn( "Null URL passed in." );
    		return null;
    	}
    	inUrl = inUrl.trim();
    	if ( inUrl.isEmpty() ) {
    		logger.warn( "Empty URL passed in." );
    		return null;
    	}
    	boolean isNew = true;
    	// Defer logging to outside of sync block
    	synchronized (foundUrls) {
			if ( ! foundUrls.contains(inUrl) ) {
				foundUrls.add(inUrl);
			}
			else {
				isNew = false;
			}
		}
    	if ( isNew ) {
    		if ( logger.isDebugEnabled() ) {
    			logger.debug( "NEW URL: \""+inUrl+'"');
    		}
    		return inUrl;
    	}
    	else {
    		if ( logger.isDebugEnabled() ) {
    			logger.debug( "DUPLICATE URL: \""+inUrl+'"');
    		}
    		return null;
    	}
    }

    String registerNewSkuUrlOrNull( String inUrl, Logger logger ) {
    	if ( null==inUrl ) {
    		logger.warn( "Null Sku URL passed in." );
    	}
    	inUrl = inUrl.trim();
    	if ( inUrl.isEmpty() ) {
    		logger.warn( "Empty Sku URL passed in." );
    	}
    	boolean isNew = true;
    	// Defer logging to outside of sync block
    	synchronized (foundSkueUrls) {
			if ( ! foundSkueUrls.contains(inUrl) ) {
				foundSkueUrls.add(inUrl);
			}
			else {
				isNew = false;
			}
		}
    	if ( isNew ) {
    		if ( logger.isDebugEnabled() ) {
    			logger.debug( "NEW SKU URL: \""+inUrl+'"');
    		}
    		return inUrl;
    	}
    	else {
    		if ( logger.isDebugEnabled() ) {
    			logger.debug( "DUPLICATE SKU URL: \""+inUrl+'"');
    		}
    		return null;
    	}
    }

    // Overrode in IterativeDownloader
    public void start(Configuration config, Logger logger)
    {
        try
        {

            ScraperConfiguration scraperConfig = new ScraperConfiguration(
                    config.baseConfig);
            Scraper scraper = new Scraper(scraperConfig, config.temp);
            String pageSource = navigateToInitialUrls(scraper, config, logger);
            if (pageSource != null) {
                List<String> categoryUrls = fetchCategoryUrls(pageSource, config,
                        logger);
                if ( null==categoryUrls || categoryUrls.isEmpty() ) {
                	logger.warn( "No categoryUrls" );
                }
				else {
					logger.info( "Got " + categoryUrls.size() + " categoryUrls" );
				}

                // fetchProducts(scraper, categoryUrls, config, logger);
                throw new IllegalStateException( "Fetch Products temp disabled during dev" );
            }
        }
        catch (Exception ex)
        {
            logger.error("Error", ex);
        }
    }

    protected Scraper resetConnection( Scraper scraper, Configuration config, Logger logger ) {
        scraper.stopExecution();
        scraper.dispose();
        scraper = null;
        scraper = initializeScraper(config);
        
        // Login Again!
        navigateToInitialUrls(scraper, config, logger);
        
        return scraper;
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
        	System.out.println("startUrl: " + startUrl);
        	scraper.addVariableToContext("url", startUrl);
            scraper.execute();
            var = getVariable(scraper, "pageContent", logger);
            pageSource = var.toString();

            // We don't cache these pages, but we'd like to see them
        	String dirName = config.filePath + SEP + "init";
        	String fullName = CacheUtilsNIE.calcUrlCacheFileNameForURL( dirName, startUrl );
        	CacheUtilsNIE.writeToFile( pageSource, fullName, logger );
            
            // For all other URLs
            for (int i = 0; i < config.listOfInitialUrls.size(); i++)
            {
                if (config.isApplyRegixToIntUrl)
                {
                    url = extractValueUsingRegex(pageSource,
                                                 config.listOfInitialUrls.get(i));
                    //System.out.println("isApplyRegixToIntUrl: " + url);
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
                    //System.out.println("listOfInitialUrls.get(i): " + url);
                    /***
                    if (config.baseUrl != null && !validateUrl(url))
                    {
                        url = config.baseUrl + url;
                    }
                    ***/
                    url = CacheUtilsNIE.normalizeUrl( url, config.baseUrl );

                    config.isApplyRegixToIntUrl = true;
                }
                System.out.println("normalizedUrl: " + url);                
                try
                {
                	// TODO: Not using our Util class yet for these special fetches
                	//System.out.println("initialUrl: " + url);
                    scraper.addVariableToContext("url", url);
                    if(!config.enableScripting) {
                        scraper.removeRunningFunction();
                    }
                    scraper.execute();
                    var = getVariable(scraper, "pageContent",
                                      logger);
                    pageSource = var.toString();
                    // We don't cache these pages, but we'd like to see them
                	fullName = CacheUtilsNIE.calcUrlCacheFileNameForURL( dirName, url );
                	CacheUtilsNIE.writeToFile( pageSource, fullName, logger );
                    
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


    
    protected List<String> fetchCategoryUrls(String pageSource,
                                             Configuration config,
                                             Logger logger)
    {
        // List<String> productsUrl = new LinkedList<String>();
        Set<String> productsUrl = new LinkedHashSet<String>();

        if(!config.isApplyRegixToCatUrl)
        {
            productsUrl.add(config.regexForCategory);
            // return productsUrl;
            // Convert back to list
            List<String> outList = new LinkedList<String>();
            outList.addAll( productsUrl );
            return outList;
        }

        try
        {
            Pattern regexForHref = Pattern.compile(config.regexForCategory);
            Matcher matchList = regexForHref.matcher(pageSource);
            String tmpmatch = "";
            while (matchList.find())
            {
                if (!config.regexForCategory.contains("(.*?)"))
                {
                    tmpmatch = matchList.group();
                }
                else
                {
                    tmpmatch = matchList.group(1);
                }

                if (!tmpmatch.equals(""))
                {

                    if (productsUrl.contains(tmpmatch) == false)
                    {
                        productsUrl.add(tmpmatch.replaceAll(" ", "%20").trim());
                        logger.info(tmpmatch);                        
                    }
                }
            }
        }
        catch (Exception ex)
        {
            logger.error("error", ex);
        }

        // return productsUrl;
        // Convert back to list
        List<String> outList = new LinkedList<String>();
        outList.addAll( productsUrl );
        return outList;

    }

    // protected String getNextPageUrl( )
    protected String parseNextPageUrl(String pageSource, Configuration config,
                                    Logger logger)
    {

		// Parse all next page URLs from the page we just downloaded
		Set<String> pageUrls = extractAllValuesUsingRegexWithDedupe( null, pageSource, config.regexForNextSku, null, logger );
		
		// Now find the first one we haven't seen before
		String nextPageUrl = null;
		for ( String url : pageUrls ) {
			// Non-Null means this is a NEW url
			nextPageUrl = registerNewUrlOrNull( url, logger );
			if ( null!=nextPageUrl ) {
				break;
			}
		}
		// We either found one or we didn't
		return nextPageUrl;
    	
		/***
    	String nextPageUrl = null;
        try
        {
            Pattern regexForHref = Pattern.compile(config.regexForNextSku);
            Matcher matchList = regexForHref.matcher(pageSource);
            String tmpmatch = "";
            while (matchList.find())
            {
                if (!config.regexForNextSku.contains("(.*?)"))
                {
                    tmpmatch = matchList.group();
                }
                else
                {
                    tmpmatch = matchList.group(1);
                }

                if (!tmpmatch.equals(""))
                {

                    if (!vistedPages.contains(tmpmatch))
                    {
                        vistedPages.add(tmpmatch.replaceAll(" ", "%20").trim());
                        logger.info(tmpmatch);
                        nextPageUrl = tmpmatch.replaceAll(" ", "%20").trim();
                        break;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            logger.error("error", ex);
        }
        return nextPageUrl;
        ***/
    }


    protected String extractValueUsingRegex(String pageSource, String regex) throws
            Exception
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

    // Master list is actually optional, we'll create if you don't pass it
    Set<String> extractAllValuesUsingRegexWithDedupe( Set<String> masterList, String pageSource, String regex, String optUrlPrefix, Logger logger ) {
    	if( null==masterList ) {
    		masterList = new LinkedHashSet<String>();
    	}
    	// Sanity
        if (null==pageSource || pageSource.trim().isEmpty() ) {
        	logger.warn( "Null/empty source string passed in for regex \""+regex+"\"" );
        	return masterList;
        }
        
        
        
        
    	int numChecked = 0;
    	int numAdded = 0;
        Pattern regexForHref = Pattern.compile(regex);
        String candidate = "";
        Matcher matchList = regexForHref.matcher(pageSource);
        // Foreach match
        while (matchList.find()) {
        	numChecked++;
        	// If not using special Non-greedy Grouper then grab the entire match
            if (!regex.contains("(.*?)")) {
            	candidate = matchList.group();
            }
            // Else get the first group match
            else {
            	candidate = matchList.group(1);
            }
            // Normalize, which also does sanity check
            candidate = CacheUtilsNIE.normalizeUrl( candidate, optUrlPrefix );
            // Tabulate
            // Nice to check vs. blindly add, but we don't really need to
            if ( ! masterList.contains(candidate) ) {
            	numAdded++;
            	masterList.add( candidate );
            }
        }
        if (0==numChecked) {
        	logger.warn( "No matches for regex \""+regex+"\", from text with "+pageSource.length()+" chars." );
        }
        else {
        	logger.info( "Checked = "+numChecked + ", added = "+numAdded + ", full list now has = " + masterList.size() );
        }
        return masterList;
    }

    // TODO: Eventually change to Set or Collection but don't want to break other stuff
    protected List<String> extractAllValueUsingRegex(String pageSource,
            String regex, String optUrlPrefix) // throws Exception
    {
        // List<String> list = new LinkedList<String>();
    	// Converting to data structure that removes duplicates
    	// LinkedHashSet preserves insertion order
        Set<String> set = new LinkedHashSet<String>();
        
        Pattern regexForHref = Pattern.compile(regex);
        Matcher matchList = regexForHref.matcher(pageSource);
        String tmpmatch = "";
        while (matchList.find())
        {
        	// If no Group set then grab the entire match
            if (!regex.contains("(.*?)"))
            {
                tmpmatch = matchList.group();
            }
            // Else get the first group match
            else
            {
                tmpmatch = matchList.group(1);
            }


            if (!tmpmatch.equals(""))
            {
                /***
                if (optUrlPrefix != null && !validateUrl(tmpmatch))
                {
                    tmpmatch = optUrlPrefix + tmpmatch;
                }
                ***/
            	tmpmatch = CacheUtilsNIE.normalizeUrl( tmpmatch, optUrlPrefix );
                // list.add(tmpmatch);
                set.add(tmpmatch);

            }
        }
        // return list;
        // Convert back to list
        List<String> outList = new LinkedList<String>();
        outList.addAll( set );
        return outList;
    }

    // protected void fetchProducts(Scraper scraper, List<String> categoryUrls,
    protected List<String> fetchSubCategoryUrlsAndParseProductUrls(Scraper scraper, List<String> categoryUrls,
            Configuration config, Logger logger)
    {
        String pageSource = null;
        String _skuPageSource = null;
        Variable var = null;

        // Output data structure
        Set<String> skuUrls = new LinkedHashSet<String>();

        // Might need seed from config
        if (categoryUrls.isEmpty()) {
            categoryUrls.add(config.url);
        }

        // Foreach Category URL
        for (int i = 0; i < categoryUrls.size(); i++)
        {
        	// Main Try / Catch
        	// NOT responsible for Retry
            try {
                int currentProduct = 1;
                int currentPage = 1;
                boolean run = true;
                String url = categoryUrls.get(i);
            	System.out.println("categoryUrl: " + url);

                // While run is True
                do
                {
                    // Cleanup, base URL is optional
                    url = CacheUtilsNIE.normalizeUrl( url, config.baseUrl );

                    // We check after normalizing
                    // This assumes it'll work
                    if(!foundUrls.contains(url)) {
                        foundUrls.add(url);
                    }

                    //  (moved to normalize url)
                    // url = url.replaceAll("&amp;", "&");
                    // if (config.baseUrl != null && !validateUrl(url)) {
                    //    url = config.baseUrl + (!config.baseUrl.endsWith("/") && !url.startsWith("/") ? "/" : "") + url;
                    // }
                    
                    // logger.info(url);
                    // scraper.addVariableToContext("url", url);
                    // if(!config.enableScripting)
                    //    scraper.removeRunningFunction();
                    // scraper.execute();
                    // var = getVariable(scraper, "pageContent", logger);
                    // pageSource = var.toString();
                    
		        	String dirName = config.filePath + SEP + "subcats";
					pageSource = CacheUtilsNIE.fetchUrlWithCaching( url, dirName, scraper, config.enableScripting, logger );

                    // logger.info(url);
                    // ArrayList<String> skuesPerPage = fetchSkuesFromPage(var.toString(), config, logger);
                    // List<String> skuesPerPage = fetchSkuesFromPage( pageSource, config, logger);
                    /*List<String> skuesPerPage =*/ parseSkueUrlsFromPage( skuUrls, pageSource, config, logger );

                    /***
                    // Foreach SKUs Found on Page / skuesPerPage
                    for (int j = 0; j < skuesPerPage.size(); j++)
                    {
                        skuPageSource = "";

                        try
                        {
                            // scraper.
                            //        addVariableToContext(
                            //                "url",
                            //                skuesPerPage.get(j));
                            // if(!config.enableScripting)
                            //    scraper.removeRunningFunction();
                            // scraper.execute();
                            // var = getVariable(scraper,
                            //                  "pageContent",
                            //                  logger);
                            // skuPageSource = var.
                            //                toString();

                            String skuDirName = config.filePath + SEP + "fetchProds2";
                            String skuUrl = skuesPerPage.get(j);
                            skuPageSource = CacheUtilsNIE.fetchUrlWithCaching( skuUrl, skuDirName, scraper, config.enableScripting, logger );

                        }
                        catch (Exception exp)
                        {
                            exp.printStackTrace();
                            j--;

                            vistedSkues.remove(
                                            skuesPerPage.
                                            get(j + 1));

                            scraper.stopExecution();
                            scraper.dispose();
                            scraper = null;
                            scraper = initializeScraper(config);
                            
                            // Login Again!
                            navigateToInitialUrls(scraper, config, logger);

                            continue;
                        }

                        // Two possibilities
                        // Do we have a regex for SKU URL Item ?
                        if (config.regexForSkuUrlItem != null &&
                            !config.regexForSkuUrlItem.trim().isEmpty())
                        {
                            ArrayList<String>
                                    itemsPerPage = fetchItemsFromPage(var.
                                    toString(), config, logger);

                            // Foreach Items Found on Page / itemsPerPage
                            for (int k = 0; k < itemsPerPage.size(); k++)
                            {
                                if (vistedSkues.contains(
                                        itemsPerPage.get(k)))
                                {
                                    continue;
                                }
                                skuPageSource = "";
                                try
                                {
                                    scraper.
                                            addVariableToContext(
                                            "url",
                                            itemsPerPage.get(k));
                                    if(!config.enableScripting)
                                        scraper.removeRunningFunction();
                                    scraper.execute();
                                    var = getVariable(scraper,
                                            "pageContent",
                                            logger);
                                    skuPageSource = var.
                                            toString();

                                    skuPageSource = "<url>" +
                                            itemsPerPage.get(k) +
                                            "</url>" +
                                            skuPageSource;

                                    //skuName = skuesPerPage.get(j).replaceFirst(config.baseUrl, "");
                                    //skuName = skuName.replaceAll("/", "-");
                                    writeToFile(skuPageSource,
                                                config.filePath + "/catalog" + (i + 1),
                                                "/" + (i + 1) + config.fileName + currentPage + "-" + currentProduct,
                                                logger);

                                    vistedSkues.add(
                                            itemsPerPage.
                                            get(k));
                                    currentProduct++;
                                }
                                catch (Exception exp)
                                {
                                    logger.error("Error", exp);
                                    k--;

                                    vistedSkues.remove(
                                            itemsPerPage.
                                            get(k + 1));

                                    scraper.stopExecution();
                                    scraper.dispose();
                                    scraper = null;
                                    scraper = initializeScraper(config);
                                    navigateToInitialUrls(scraper, config, logger);

                                    continue;
                                }

                            } // End Foreach Items Found on Page / itemsPerPage


                            if(itemsPerPage != null)
                                itemsPerPage.clear();

                            itemsPerPage = null;
                        }
                        // Else no regex for SKU URL Item ?
                        else
                        {
                            if (vistedSkues.contains(
                                    skuesPerPage.get(j)))
                            {
                                continue;
                            }
                            try
                            {
                                skuPageSource = "<url>" +
                                                skuesPerPage.get(j) +
                                                "</url>" +
                                                skuPageSource;

                                //skuName = skuesPerPage.get(j).replaceFirst(config.baseUrl, "");
                                //skuName = skuName.replaceAll("/", "-");
                                writeToFile(skuPageSource,
                                            config.filePath +
                                            "/catalog" + (i + 1),
                                            "/" + (i + 1) +
                                            config.fileName +
                                            currentPage + "-" +
                                            currentProduct,
                                            logger);

                                vistedSkues.add(
                                        skuesPerPage.
                                        get(j));
                                currentProduct++;
                            }
                            catch (Exception exp)
                            {
                                logger.error("Error", exp);
                            }
                        }
                    } // End Foreach skuesPerPage / SKUs found on Page

                    if(skuesPerPage != null)
                        skuesPerPage.clear();

                    skuesPerPage = null;
                    ***/
                    
                    url = parseNextPageUrl(pageSource, config, logger);
                    if ( null==url ) {
                        run = false;
                    }
                    currentPage++;

                // End While run is True
                } while (run);

            } // End of main Try/Catch
            catch (Exception ex)
            {
                logger.error("Error", ex);
            }

        }  // End Foreach Category URL

        List<String> outList = new LinkedList<String>();
        outList.addAll( skuUrls );
        return outList;

    }

    // protected void fetchProducts(Scraper scraper, List<String> categoryUrls,
    // protected void fetchSubCategoryUrlsAndParseProductUrls(Scraper scraper, List<String> categoryUrls,
    protected void _fetchProducts(Scraper scraper, List<String> skuUrls,
            Configuration config, Logger logger)
    {
    	/***
        String pageSource = null;
        String skuPageSource = null;
        Variable var = null;

        // Might need seed from config
        if (categoryUrls.isEmpty()) {
            categoryUrls.add(config.url);
        }

        // Foreach Category URL
        for (int i = 0; i < categoryUrls.size(); i++)
        {
        	// Main Try / Catch
        	// NOT responsible for Retry
            try {
                int currentProduct = 1;
                int currentPage = 1;
                boolean run = true;
                String url = categoryUrls.get(i);

                // While run is True
                do
                {
                    // Cleanup, base URL is optional
                    url = CacheUtilsNIE.normalizeUrl( url, config.baseUrl );

                    // We check after normalizing
                    // This assumes it'll work
                    if(!vistedPages.contains(url)) {
                        vistedPages.add(url);
                    }

                    // (moved to normalize url)
                    // url = url.replaceAll("&amp;", "&");
                    // if (config.baseUrl != null && !validateUrl(url)) {
                    //    url = config.baseUrl + (!config.baseUrl.endsWith("/") && !url.startsWith("/") ? "/" : "") + url;
                    //}
                    
                    // logger.info(url);
                    // scraper.addVariableToContext("url", url);
                    // if(!config.enableScripting)
                    //     scraper.removeRunningFunction();
                    // scraper.execute();

                    // var = getVariable(scraper, "pageContent", logger);
                    // pageSource = var.toString();
                    
		        	String dirName = config.filePath + SEP + "subcats";
					pageSource = CacheUtilsNIE.fetchUrlWithCaching( url, dirName, scraper, config.enableScripting, logger );

                    // logger.info(url);
                    // ArrayList<String> skuesPerPage = fetchSkuesFromPage(var.toString(), config, logger);
                    List<String> skuesPerPage = fetchSkuesFromPage( pageSource, config, logger);

                    // Foreach SKUs Found on Page / skuesPerPage
                    for (int j = 0; j < skuesPerPage.size(); j++)
                    {
                        skuPageSource = "";

                        try
                        {
                        	/***
                            // scraper.
                            //        addVariableToContext(
                            //                "url",
                            //                skuesPerPage.get(j));
                            // if(!config.enableScripting)
                            //    scraper.removeRunningFunction();
                            // scraper.execute();
                            // var = getVariable(scraper,
                            //                  "pageContent",
                            //                  logger);
                            // skuPageSource = var.
                            //                toString();

                            String skuDirName = config.filePath + SEP + "fetchProds2";
                            String skuUrl = skuesPerPage.get(j);
                            skuPageSource = CacheUtilsNIE.fetchUrlWithCaching( skuUrl, skuDirName, scraper, config.enableScripting, logger );

                        }
                        catch (Exception exp)
                        {
                            exp.printStackTrace();
                            j--;

                            vistedSkues.remove(
                                            skuesPerPage.
                                            get(j + 1));

                            scraper.stopExecution();
                            scraper.dispose();
                            scraper = null;
                            scraper = initializeScraper(config);
                            
                            // Login Again!
                            navigateToInitialUrls(scraper, config, logger);

                            continue;
                        }

                        // Two possibilities
                        // Do we have a regex for SKU URL Item ?
                        if (config.regexForSkuUrlItem != null &&
                            !config.regexForSkuUrlItem.trim().isEmpty())
                        {
                            ArrayList<String>
                                    itemsPerPage = fetchItemsFromPage(var.
                                    toString(), config, logger);

                            // Foreach Items Found on Page / itemsPerPage
                            for (int k = 0; k < itemsPerPage.size(); k++)
                            {
                                if (vistedSkues.contains(
                                        itemsPerPage.get(k)))
                                {
                                    continue;
                                }
                                skuPageSource = "";
                                try
                                {
                                    scraper.
                                            addVariableToContext(
                                            "url",
                                            itemsPerPage.get(k));
                                    if(!config.enableScripting)
                                        scraper.removeRunningFunction();
                                    scraper.execute();
                                    var = getVariable(scraper,
                                            "pageContent",
                                            logger);
                                    skuPageSource = var.
                                            toString();

                                    skuPageSource = "<url>" +
                                            itemsPerPage.get(k) +
                                            "</url>" +
                                            skuPageSource;

                                    //skuName = skuesPerPage.get(j).replaceFirst(config.baseUrl, "");
                                    //skuName = skuName.replaceAll("/", "-");
                                    writeToFile(skuPageSource,
                                                config.filePath + "/catalog" + (i + 1),
                                                "/" + (i + 1) + config.fileName + currentPage + "-" + currentProduct,
                                                logger);

                                    vistedSkues.add(
                                            itemsPerPage.
                                            get(k));
                                    currentProduct++;
                                }
                                catch (Exception exp)
                                {
                                    logger.error("Error", exp);
                                    k--;

                                    vistedSkues.remove(
                                            itemsPerPage.
                                            get(k + 1));

                                    scraper.stopExecution();
                                    scraper.dispose();
                                    scraper = null;
                                    scraper = initializeScraper(config);
                                    navigateToInitialUrls(scraper, config, logger);

                                    continue;
                                }

                            } // End Foreach Items Found on Page / itemsPerPage


                            if(itemsPerPage != null)
                                itemsPerPage.clear();

                            itemsPerPage = null;
                        }
                        // Else no regex for SKU URL Item ?
                        else
                        {
                            if (vistedSkues.contains(
                                    skuesPerPage.get(j)))
                            {
                                continue;
                            }
                            try
                            {
                                skuPageSource = "<url>" +
                                                skuesPerPage.get(j) +
                                                "</url>" +
                                                skuPageSource;

                                //skuName = skuesPerPage.get(j).replaceFirst(config.baseUrl, "");
                                //skuName = skuName.replaceAll("/", "-");
                                writeToFile(skuPageSource,
                                            config.filePath +
                                            "/catalog" + (i + 1),
                                            "/" + (i + 1) +
                                            config.fileName +
                                            currentPage + "-" +
                                            currentProduct,
                                            logger);

                                vistedSkues.add(
                                        skuesPerPage.
                                        get(j));
                                currentProduct++;
                            }
                            catch (Exception exp)
                            {
                                logger.error("Error", exp);
                            }
                        }
                    } // End Foreach skuesPerPage / SKUs found on Page

                    if(skuesPerPage != null)
                        skuesPerPage.clear();

                    skuesPerPage = null;

                    url = getNextPageUrl(pageSource, config, logger);
                    currentPage++;

                    if (url == null)
                    {
                        run = false;
                    }

                // End While run is True
                } while (run);

            } // End of main Try/Catch
            catch (Exception ex)
            {
                logger.error("Error", ex);
            }

        }  // End Foreach Category URL
        ***/
    }

    protected int getTotalPages(String pageSource, Configuration config)
    {
        //return extractValueFromPageSource(pageSource , config.masterRegexForTotalPages , config.regexForTotalPages);
        return -1;
    }

    protected int getTotalProducts(String pageSource, Configuration config)
    {
        //return extractValueFromPageSource(pageSource , config.masterRegexForTotalProducts , config.regexForTotalProducts);
        return -1;
    }

    protected int extractValueFromPageSource(String src, String masterRegexStr,
                                             String regexStr)
    {
        int value = -1;
        Pattern masterRegex = Pattern.compile(masterRegexStr);
        Pattern regex = Pattern.compile(regexStr);
        Matcher matchList = masterRegex.matcher(src);
        String tmpmatch = "";
        String actualMatch = "";
        if (matchList.find())
        {
            if (!regexStr.contains("(.*?)"))
            {
                tmpmatch = matchList.group();
            }
            else
            {
                tmpmatch = matchList.group(1);
            }

            if (!tmpmatch.equals(""))
            {
                matchList = regex.matcher(tmpmatch);
                if (matchList.find())
                {
                    actualMatch = matchList.group();
                    value = Integer.parseInt(actualMatch.replaceAll(" ", "%20").trim());
                }
            }
        }
        return value;
    }

    // TODO: convert data structure
    // protected ArrayList<String> fetchSkuesFromPage( )
    // ??? regexForSkuUrl vs. regexForSkuUrlItem
    protected Set<String> parseSkueUrlsFromPage( Set<String>skuUrls, String pageSource,
            Configuration config, Logger logger)
    {
		// Parse URLs from the page we just downloaded
		return extractAllValuesUsingRegexWithDedupe( skuUrls, pageSource, config.regexForSkuUrl, null, logger );

    	/***
    	ArrayList<String> skues = new ArrayList<String>();
        Pattern regexForHref = Pattern.compile(config.regexForSkuUrl);
        Matcher matchList = regexForHref.matcher(pageSource);
        String tmpmatch = "";
        while (matchList.find())
        {
            if (!config.regexForSkuUrl.contains("(.*?)"))
            {
                tmpmatch = matchList.group();
            }
            else
            {
                tmpmatch = matchList.group(1);
            }

            if (!tmpmatch.equals(""))
            {
            	tmpmatch = CacheUtilsNIE.normalizeUrl( tmpmatch, config.baseUrl );

                // if (config.baseUrl != null && !validateUrl(tmpmatch)) {
                //    tmpmatch = config.baseUrl + tmpmatch;
                //}
                if (skues.contains(tmpmatch) == false)
                {
                    skues.add(tmpmatch.replaceAll(" ", "%20").trim());
                }
            }
        }
        return skues;
        ***/
    }

    // TODO: convert data structure
    // ??? regexForSkuUrl vs. regexForSkuUrlItem
    protected Set<String> fetchItemsFromPage(String pageSource,
            Configuration config, Logger logger)
    {
		return extractAllValuesUsingRegexWithDedupe( null, pageSource, config.regexForSkuUrlItem, null, logger );

    	/***
    	ArrayList<String> skues = new ArrayList<String>();
        Pattern regexForHref = Pattern.compile(config.regexForSkuUrlItem);
        Matcher matchList = regexForHref.matcher(pageSource);
        String tmpmatch = "";
        while (matchList.find())
        {
            if (!config.regexForSkuUrlItem.contains("(.*?)"))
            {
                tmpmatch = matchList.group();
            }
            else
            {
                tmpmatch = matchList.group(1);
            }

            if (!tmpmatch.equals(""))
            {
                if (config.baseUrl != null && !validateUrl(tmpmatch))
                {
                    tmpmatch = config.baseUrl + tmpmatch;
                }
                if (skues.contains(tmpmatch) == false)
                {
                    skues.add(tmpmatch.replaceAll(" ", "%20").trim());
                }
            }
        }
        return skues;
        ***/
    }


    protected void writeToFile(String pageSource, String directory,
                               String fileName, Logger logger)
    {
        FileWriter writer = null;
        try
        {
            File fileFolder = new File(directory);
            if (!fileFolder.exists())
            {
                fileFolder.mkdirs();
            }
            String fullName = directory + fileName + ".htm";
            logger.info( "WRITING FILE '" + fullName + "'" );
            writer = new FileWriter( fullName, false );
            byte[] bytes = pageSource.getBytes("UTF-8");
            writer.write(new String(bytes));
            writer.close();
        }
        catch (Exception ex)
        {
            logger.error("error", ex);
        }
    }


    protected String getPageSourceAfterPerformingOperation(String configPath,
            String tempPath, Logger logger)
    {
        try
        {

            ScraperConfiguration scraperConfig = new ScraperConfiguration(
                    configPath);
            Scraper scraper = new Scraper(scraperConfig, tempPath);
            scraper.setDebug(false);
            scraper.execute();
            Variable var = getVariable(scraper, "nextOperation", logger);
            if (var != null && var.toString().trim().equals("*") == false)
            {
                getPageSourceAfterPerformingOperation(var.toString(), tempPath,
                        logger);
            }
            else
            {
                var = getVariable(scraper, "pageContent", logger);
                return var.toString();
            }
        }
        catch (Exception ex)
        {
            logger.error("Error", ex);

        }
        return null;
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

    public Scraper initializeScraper(Configuration config)
    {
        try
        {
            ScraperConfiguration scraperConfig = new ScraperConfiguration(
                    config.baseConfig);
            return new Scraper(scraperConfig, config.temp);
        }
        catch (Exception exp)
        {}

        return null;
    }
}
