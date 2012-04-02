package com.netsol.scraper.downloader;

import com.netsol.scraper.util.Configuration;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Apr 13, 2011
 * Time: 2:19:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class MooreWallaceDownloader extends BaseDownloader{


      protected List<String> fetchCategoryUrls(String pageSource,
                                             Configuration config,
                                             Logger logger)
    {
        //System.out.println("pageSource: "+pageSource);
        List<String> productsUrl = new LinkedList<String>();

        if(!config.isApplyRegixToCatUrl)
        {
            productsUrl.add(config.regexForCategory);
            return productsUrl;
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
        return productsUrl;
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

                       //System.out.println("URL: " + url + "...End");

                       logger.info(url);
                       scraper.addVariableToContext("url", url);
                       if(!config.enableScripting)
                           scraper.removeRunningFunction();
                       scraper.execute();

                       var = getVariable(scraper, "pageContent", logger);
                       pageSource = var.toString();

                       logger.info(url);
                       ArrayList<String>
                               skuesPerPage = fetchSkuesFromPage(var.toString(),
                               config, logger);

                       for (int j = 0; j < skuesPerPage.size(); j++)
                       {
                           skuPageSource = "";

                           try
                           {

                               System.out.println("i = " + i + "j = " + j + "itemUrl = " + skuesPerPage.get(j));
                              //skuesPerPage.set(j,"https://www.insight.com/search/ppp.web?fromSearch=true&fromIB=&materialId=CE459A\\%23ABA");


                               //System.out.println("sku page source : " + skuPageSource);
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
                                   skuPageSource = getItemStringForFile(skuesPerPage.get(j));
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
                                   exp.printStackTrace();
                                   logger.error("Error", exp);
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


       protected ArrayList<String> fetchSkuesFromPage(String pageSource,
            Configuration config, Logger logger)
    {
        System.out.println("Skew Page source: " + pageSource);
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
                /*if (config.baseUrl != null && !validateUrl(tmpmatch))
                {
                    tmpmatch = config.baseUrl + tmpmatch;
                }*/
                if (skues.contains(tmpmatch) == false)
                {
                    //tmpmatch=tmpmatch.replaceAll("%23", "#");
                    skues.add(tmpmatch.trim());
                }
            }
        }
        //System.out.println("Skews on page: " + skues);
        return skues;
    }

    private String getItemStringForFile(String input)
    {
        //openQuickView(1,"JOHN HAMMERGREN BOOK","MCKSITGBK","<B>Skin in the Game by John Hammergren.  <FONT COLOR=RED>IMPORTANT:  Your cost center will be charged $14.00/book.</b>","","-1","P","N","","","","","","","/xs2/images/icons/png/Icon_Cart.png","doAddToCart(\"JOHN HAMMERGREN BOOK\",\"MCKSITGBK\",\"false\",\"0\",\"0\",\"<B>Skin in the Game by John Hammergren.  <FONT COLOR=RED>IMPORTANT:  Your cost center will be charged $14.00/book.</b>\",\"\",\"\",\"\",\"\",\"-1\",\"P\",\"N\",\"0\")","Add To Cart","","","Add To Cart","","","","","",false,"");

        System.out.println("Input: " + input);
        int indexStart = input.indexOf("openQuickView");
        indexStart = indexStart+14;
        input = input.substring(indexStart,input.length()-2);
        System.out.println("Input: " + input);

        int CartBtnIndex = input.indexOf("doAddToCart");
        input = input.substring(0,CartBtnIndex-2);
        System.out.println("Input: " + input);

        String[] tokens = input.split(",");

        String ItemDescription = tokens[1];

        String ItemNumber = tokens[2];

        if(ItemNumber.equals("544809"))
        {
            System.out.println("Got here!");
        }

        String ItemLongText1 = tokens[3];

        String ItemImage = tokens[4];

        System.out.println("item number: " + ItemNumber);

        

        String fileString ="<HTML><BODY><br/>"+ "ItemNumber: " + ItemNumber + "\n" + "ItemDescription: " + ItemDescription + "\n" + "ItemLongText1: " + ItemLongText1+"\n"+"Item Image: " + ItemImage +"<br /></BODY></HTML>";
        /*String fileString ="<HTML><BODY><br/>"+ "ItemNumber: " + ItemNumber + "\n" + "ItemDescription: " + ItemDescription + "\n" + "ItemLongText1: " + ItemLongText1+"<br /></BODY></HTML>";*/



        return fileString;
    }
    


}
