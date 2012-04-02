package com.netsol.scraper.downloader;

import com.netsol.scraper.util.Configuration;
import org.apache.log4j.Logger;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Apr 9, 2011
 * Time: 8:14:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class InsightDownloader extends BaseDownloader{
    protected String getNextPageUrl(String pageSource, Configuration config,
                                    Logger logger)
    {
        String nextPageUrl = null;
        //System.out.println("Page source: " + pageSource);
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

               // System.out.println("next page regex matched this: " + tmpmatch);
                int indexStart = tmpmatch.indexOf("search/");

                indexStart = indexStart + 7;
                //System.out.println("start: "+indexStart);
                int indexLast = tmpmatch.lastIndexOf("next");
               // System.out.println("last: "+indexLast);
                tmpmatch = tmpmatch.substring(indexStart, indexLast-3);

               // System.out.println("next page url: " + tmpmatch);
                tmpmatch=tmpmatch.replaceAll("#038;","");

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
    }
}
