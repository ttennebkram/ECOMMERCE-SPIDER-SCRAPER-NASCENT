package com.netsol.scraper.downloader;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import com.netsol.scraper.util.Configuration;

public class BaseDownloader
{
    protected List<String> vistedPages = new LinkedList<String>();
    protected List<String> vistedSkues = new LinkedList<String>();
    public void start(Configuration config, Logger logger)
    {
        try
        {      	
            ScraperConfiguration scraperConfig = new ScraperConfiguration(config.baseConfig);
            Scraper scraper = new Scraper(scraperConfig, config.temp);
            String pageSource = navigateToInitialUrls(scraper, config, logger);
            if (pageSource != null)
            {
                List<String>categoryUrl = fetchCategoryUrls(pageSource, config, logger);
                fetchProducts(scraper, categoryUrl, config, logger);
            }
        }
        catch (Exception ex)
        {
            logger.error("Error", ex);
        }
    }
    
    public void relogin(Scraper scraper, Configuration config, Logger logger) throws InterruptedException {
    	System.out.println("relogin");
        scraper.stopExecution();
        scraper.dispose();
        scraper = null;
        Thread.sleep(4000);
        scraper = initializeScraper(config);
        navigateToInitialUrls(scraper, config, logger);    
    }

    public String getPage(Scraper scraper, Configuration config, Logger logger, String url) throws InterruptedException {
    	boolean success = false;
    	String pageSource = null;
    	while (!success) {
    		try {
		        scraper.addVariableToContext("url", url);
		        if(!config.enableScripting)
		            scraper.removeRunningFunction();
		        scraper.execute();
		        Variable var = getVariable(scraper, "pageContent", logger);
		        pageSource = var.toString();  
		        
		        if (pageSource.length() < 100) {
		        	System.out.println("page too small: " + pageSource);
		        	relogin(scraper, config, logger);        	
		        }
		        else if (pageSource.contains("ACCOUNT LOG ON")) {
		        	System.out.println("got logged off");
		        	relogin(scraper, config, logger);
		        }  	  
		        else {
		        	success = true;
		        }
    		}
            catch (Exception exp)
            {
                logger.error("Error", exp);
                relogin(scraper, config, logger);
            }
    	}
        return pageSource;
    }
    
    protected String navigateToInitialUrls(Scraper scraper,
                                           Configuration config, Logger logger)
    {
        String pageSource = null;
        Variable var = null;
        String url = null;
        try
        {            
            /*scraper.addVariableToContext("url", config.url);
            scraper.execute();
            var = getVariable(scraper, "pageContent", logger);
            pageSource = var.toString();*/
        	pageSource = getPage(scraper, config, logger, config.url);
            for (int i = 0; i < config.listOfInitialUrls.size(); i++)
            {
                if (config.isApplyRegixToIntUrl)
                {
                    url = extractValueUsingRegex(pageSource,
                                                 config.listOfInitialUrls.get(i));
                    if (config.baseUrl != null && !validateUrl(url))
                    {
                        url = config.baseUrl + url;
                    }
                }
                else
                {
                    url = config.listOfInitialUrls.get(i);

                    if (config.baseUrl != null && !validateUrl(url))
                    {
                        url = config.baseUrl + url;
                    }
                    config.isApplyRegixToIntUrl = true;
                }
                try
                {                    
                    /*scraper.addVariableToContext("url", url);
                    if(!config.enableScripting)
                        scraper.removeRunningFunction();
                    scraper.execute();
                    var = getVariable(scraper, "pageContent",
                                      logger);
                    pageSource = var.toString();*/
                	pageSource = getPage(scraper, config, logger, url);
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
    	//logger.info("fetchCategoryUrls");
        List<String> productsUrl = new LinkedList<String>();

        if(!config.isApplyRegixToCatUrl)
        {
            productsUrl.add(config.regexForCategory);
        	logger.info("!isApplyRegixToCatUrl");
            return productsUrl;
        }

        try
        {
            Pattern regexForHref = Pattern.compile(config.regexForCategory);
        	logger.info("regexForHref: " + regexForHref);
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
                	//logger.info("tmpmatch: " + tmpmatch);
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
        return productsUrl;
    }

    protected String getNextPageUrl(String pageSource, Configuration config,
                                    Logger logger)
    {
        String nextPageUrl = null;
        try
        {
            Pattern regexForHref = Pattern.compile(config.regexForNextSku);
            System.out.println("config.regexForNextSku: " + config.regexForNextSku);
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
                //System.out.println("tmpmatch: " + tmpmatch);
                if (!tmpmatch.equals(""))
                {

                    if (!vistedPages.contains(tmpmatch))
                    {
                        vistedPages.add(tmpmatch.replaceAll(" ", "%20").trim());
                        logger.info(tmpmatch);
                        nextPageUrl = tmpmatch.replaceAll(" ", "%20").trim();
                        System.out.println("nextPageUrlbreak: " + nextPageUrl);
                        break;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            logger.error("error", ex);
        }
        System.out.println("nextPageUrl: " + nextPageUrl);
        return nextPageUrl;
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

    protected List<String> extractAllValueUsingRegex(String pageSource,
            String regex, String appender) throws Exception
    {
        List<String> list = new LinkedList<String>();
        System.out.println("regex: " + regex);
        Pattern regexForHref = Pattern.compile(regex);
        Matcher matchList = regexForHref.matcher(pageSource);
        String tmpmatch = "";
        while (matchList.find())
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
                if (appender != null && !validateUrl(tmpmatch))
                {
                    tmpmatch = appender + tmpmatch;
                }
                list.add(tmpmatch);
            }
        }
        System.out.println(list.size());
        System.out.println(list);
        return list;
    }

    public static boolean validateUrl(String url)
    {
        Pattern pattern = Pattern.compile("((https?|ftp|gopher|telnet|file|notes|ms-help):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\.&]*)");
        Matcher m = pattern.matcher(url);
        return m.matches();
    }

    protected void fetchProducts(Scraper scraper, List<String> categoryUrls,
            Configuration config, Logger logger)
    {
        String pageSource = null;
        String skuPageSource = null;
        Variable var = null;

        if (categoryUrls.isEmpty())
        {
            categoryUrls.add(config.url);
        }
        System.out.println("categoryUrls(): " + categoryUrls.size());
        for (int i = 0; i < categoryUrls.size(); i++)
        {       	
            try
            {
                int currentProduct = 1;
                int currentPage = 1;
                boolean run = true;
                String url = categoryUrls.get(i); 
                do
                {
                    if(!vistedPages.contains(url))
                        vistedPages.add(url);

                    url = url.replaceAll("&amp;", "&");
                    //logger.info("beforeURL:" + url);

                    if (config.baseUrl != null && !validateUrl(url))
                    {
                        url = config.baseUrl + (!config.baseUrl.endsWith("/") && !url.startsWith("/") ? "/" : "") + url;
                    }

                    logger.info(url);
                    
                    /*scraper.addVariableToContext("url", url);
                    if(!config.enableScripting)
                        scraper.removeRunningFunction();
                    scraper.execute();
                    var = getVariable(scraper, "pageContent", logger);
                    pageSource = var.toString();*/
                    pageSource = getPage(scraper, config, logger, url);
                    
                    ArrayList<String>
                            skuesPerPage = fetchSkuesFromPage(pageSource,
                            config, logger, url);

                    System.out.println("skuesPerPage.size(): " + skuesPerPage.size());                    
                    for (int j = 0; j < skuesPerPage.size(); j++)
                    {
                        skuPageSource = "";

                        try
                        {
                            scraper.
                                    addVariableToContext(
                                            "url",
                                            skuesPerPage.get(j));
                            if(!config.enableScripting)
                                scraper.removeRunningFunction();
                            scraper.execute();
                            var = getVariable(scraper,
                                              "pageContent",
                                              logger);
                            skuPageSource = var.toString();
                            if (skuPageSource.length() < 100) {
                            	Thread.sleep(4000);
                            	throw new Exception("page too small");
                            }
                            if (skuPageSource.contains("ACCOUNT LOG ON")) {
                            	Thread.sleep(4000);
                            	throw new Exception("got logged off");
                            }                            
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
                            navigateToInitialUrls(scraper, config, logger);

                            continue;
                        }
                        if (config.regexForSkuUrlItem != null &&
                            !config.regexForSkuUrlItem.trim().isEmpty())
                        {
                            ArrayList<String>
                                    itemsPerPage = fetchItemsFromPage(var.
                                    toString(), config, logger);
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
                                    skuPageSource = var.toString();
                                    if (skuPageSource.length() < 100) {
                                    	Thread.sleep(4000);
                                    	throw new Exception("page too small");
                                    }
                                    if (skuPageSource.contains("ACCOUNT LOG ON")) {
                                    	Thread.sleep(4000);
                                    	throw new Exception("got logged off");
                                    }                                       
                                    skuPageSource = "<url>" +
                                            itemsPerPage.get(k) +
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
                            }
                            if(itemsPerPage != null)
                                itemsPerPage.clear();

                            itemsPerPage = null;
                        }
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
                    }

                    if(skuesPerPage != null)
                        skuesPerPage.clear();

                    skuesPerPage = null;

                    url = getNextPageUrl(pageSource, config, logger);
                    currentPage++;

                    if (url == null)
                    {
                        run = false;
                    }
                } while (run);
            }
            catch (Exception ex)
            {
                logger.error("Error", ex);
            }
        }
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


    protected ArrayList<String> fetchSkuesFromPage(String pageSource,
            Configuration config, Logger logger, String url)
    {
        ArrayList<String> skues = new ArrayList<String>();
        System.out.println("config.regexForSkuUrl: " + config.regexForSkuUrl);

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
            System.out.println("tmpmatch2: " + tmpmatch);

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
        /*if (skues.size() == 0) {
            System.out.println("skues.size() == 0 pageSource: " + pageSource);
        }*/        
        //System.out.println("skues: " + skues);
        return skues;
    }

    protected ArrayList<String> fetchItemsFromPage(String pageSource,
            Configuration config, Logger logger)
    {
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
    }


    protected void writeToFile(String pageSource, String directory,
                               String fileName, Logger logger)
    {
        FileWriter writer = null;
        try
        {
            logger.info("directory: " + directory);
            logger.info("fileName: " + fileName);

            File fileFolder = new File(directory);
            if (!fileFolder.exists())
            {
                fileFolder.mkdirs();
            }
            writer = new FileWriter(directory + fileName + ".htm", false);
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
