package com.netsol.scraper.downloader;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import com.netsol.scraper.util.Configuration;

public class IntersafeDownloader extends BaseDownloader
{
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

                    if (config.baseUrl != null && !validateUrl(url))
                    {
                        url = config.baseUrl + (!config.baseUrl.endsWith("/") && !url.startsWith("/") ? "/" : "") + url;
                    }

                    logger.info(url);
                    scraper.addVariableToContext("url", url);
                    if(!config.enableScripting)
                        scraper.removeRunningFunction();
                    scraper.execute();

                    var = getVariable(scraper, "pageContent", logger);
                    pageSource = var.toString();

                    logger.info(url);
                    ArrayList<String>
                            skuesPerPage = null; //fetchSkuesFromPage(var.toString(), config, logger);

                    for (int j = 0; j < skuesPerPage.size(); j++)
                    {
                        skuPageSource = "";

                        try
                        {
                            scraper.
                                    addVariableToContext(
                                            "url",
                                            skuesPerPage.get(j));
                            scraper.execute();
                            var = getVariable(scraper,
                                              "pageContent",
                                              logger);
                            skuPageSource = var.
                                            toString();
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

                            String additionalUrl = extractValueUsingRegex(skuPageSource, config.regexForAdditionalUrl);

                            if (config.baseUrl != null && !validateUrl(additionalUrl))
                            {
                                additionalUrl = config.baseUrl + (!config.baseUrl.endsWith("/") && !additionalUrl.startsWith("/") ? "/" : "") + additionalUrl;
                            }

                            logger.info(additionalUrl);
                            scraper.addVariableToContext("url", additionalUrl);
                            if (!config.enableScripting)
                                scraper.removeRunningFunction();
                            scraper.execute();

                            var = getVariable(scraper, "pageContent", logger);

                            skuPageSource += var.toString();

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

                    if(skuesPerPage != null)
                        skuesPerPage.clear();

                    skuesPerPage = null;

                    String parameter = getNextPageUrl(pageSource, config, logger);

                    url = (url.contains("?") ? url.substring(0, url.lastIndexOf("?")) : url) + "?@start@comp.Items=" + parameter;

                    currentPage++;

                    if (parameter == null)
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
}
