package com.netsol.scraper.downloader;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.runtime.web.HttpInfo;
import org.webharvest.runtime.variables.Variable;
import org.apache.log4j.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import com.netsol.scraper.util.Configuration;



/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Apr 11, 2011
 * Time: 1:44:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class EwayDownloader extends BaseDownloader{

   private String cookie = "";
    protected String navigateToInitialUrls(Scraper scraper,
                                           Configuration config, Logger logger)
    {
        String pageSource = null;
        Variable var = null;
        String url = null;

        try
        {

            scraper.addVariableToContext("url", config.url);
            System.out.println("url : "+config.url);
            scraper.execute();
            var = getVariable(scraper, "pageContent", logger);
            pageSource = var.toString();

            System.out.println("page source: " + pageSource);
            for (int i = 0; i < config.listOfInitialUrls.size(); i++)
            {
                if (config.isApplyRegixToIntUrl)
                {
                    url = extractValueUsingRegex(pageSource,
                                                 config.listOfInitialUrls.get(i));
                    System.out.println("url: " + url);
                    url = url.substring(24, url.length()-1);
                    url = url + "&dept_prompt=&zipcode=94501";
                    System.out.println("url: " + url);
                   // url = url.substring(23, url.length());
                   // System.out.println("url: " + url);
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
                    scraper.addVariableToContext("url", url);
                    System.out.println("URL : " + url);
                    if(!config.enableScripting)
                        scraper.removeRunningFunction();
                    scraper.execute();
                    var = getVariable(scraper, "pageContent",
                                      logger);
                    pageSource = var.toString();

                    // Extract parameters for authentication
                    System.out.println("page source 2: " + pageSource);
                    String page = extractPageSource(pageSource);
                    scraper.addVariableToContext("url", page);
                     scraper.execute();
                    var = getVariable(scraper, "pageContent",
                                      logger);
                    pageSource = var.toString();

                    //Redirect to eway.com
                    System.out.println("page source 3: " + pageSource);
                    String Home_url = "https://www.eway.com/ce/eway/bulletin.b_view";
                    //String Home_url = "https://www.eway.com/ce/eway/ui";//+"?"+"jsessionid="+cookie;
                    HttpClientManager httpClientManager = scraper.getHttpClientManager();
                   HttpClient httpClient = httpClientManager.getHttpClient();
                    HttpState state = httpClient.getState();
                    //IS2_TestServers_5806=no_testservers_defined;
//IS2_Rules_5806=(no rules defined);
//IS2_StoredValues=PageCountSinceLastShown`1,PrevOfferCount`0,PrevOfferTime`0,LastRuleIDTriggered`0,RuleTriggered`false,TimeSinceLastCheck`25,CurrentPageMatches`;
//IS2_MatchHistory=

                    Cookie sess = new Cookie ("www.eway.com/ce/eway/ui","session", cookie);
                    //Cookie IS2_TestServers_5806 = new Cookie ("www.eway.com/ce/eway/ui","IS2_TestServers_5806", "no_testservers_defined");
                    //Cookie IS2_Rules_5806 = new Cookie ("www.eway.com/ce/eway/ui","IS2_Rules_5806", "(no rules defined)");
                    //Cookie IS2_StoredValues = new Cookie ("www.eway.com/ce/eway/ui","IS2_StoredValues", "PageCountSinceLastShown`1,PrevOfferCount`0,PrevOfferTime`0,LastRuleIDTriggered`0,RuleTriggered`false");
                    //Cookie IS2_MatchHistory = new Cookie ("www.eway.com/ce/eway/ui","IS2_MatchHistory", " ");
                    //state.addCookie(sess);
                    //state.addCookie(IS2_TestServers_5806);
                    //state.addCookie(IS2_Rules_5806);
                    //state.addCookie(IS2_StoredValues);
                    //state.addCookie(IS2_MatchHistory);
                       //  HttpClientParams params = httpClient.getParams();


                    //params.setParameter("Cookie", "jSessionId:"+cookie);
                      // httpClient.

                     scraper.addVariableToContext("url", Home_url);

                     scraper.execute();
                    var = getVariable(scraper, "pageContent",
                                      logger);
                    pageSource = var.toString();

                    //Redirect to eway.com
                    System.out.println("page source 4: " + pageSource);
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

    private String extractPageSource(String pageSource)
    {
        String user_id="";
        String jb2b_profile_id = "";
        String jSessionID = "";
        String joperation = "";
        String jwebmethods_cart_url = "";
        String jexit_url = "";
        String jdept_prompt = "";
        String jzipcode = "94501";
        String url = "";
        String browser = "Mozilla";
        String browser_version = "5.0";
        try{

            user_id = extractValueUsingRegex(pageSource,  "juser_id[\\s]*=escape\\(\"(.*)\"\\)");
            System.out.println("user_id: " + user_id);
            user_id = user_id.substring(user_id.indexOf('\"')+1,user_id.lastIndexOf('\"'));
             System.out.println("user_id: " + user_id);

            jb2b_profile_id = extractValueUsingRegex(pageSource,  "jb2b_profile_id[\\s]*=escape\\(\"(.*)\"\\)");
            System.out.println("jb2b_profile_id: " + jb2b_profile_id);
            jb2b_profile_id = jb2b_profile_id.substring(jb2b_profile_id.indexOf('\"')+1,jb2b_profile_id.lastIndexOf('\"'));
             System.out.println("jb2b_profile_id: " + jb2b_profile_id);

            jSessionID = extractValueUsingRegex(pageSource,  "jSessionID[\\s]*=escape\\(\"(.*)\"\\)");
            System.out.println("jSessionID: " + jSessionID);
            jSessionID = jSessionID.substring(jSessionID.indexOf('\"')+1,jSessionID.lastIndexOf('\"'));
             System.out.println("jSessionID: " + jSessionID);
            cookie = jSessionID;


            joperation = extractValueUsingRegex(pageSource,  "joperation[\\s]*=escape\\(\"(.*)\"\\)");
            System.out.println("joperation: " + joperation);
            joperation = joperation.substring(joperation.indexOf('\"')+1,joperation.lastIndexOf('\"'));
             System.out.println("joperation: " + joperation);

            jwebmethods_cart_url = extractValueUsingRegex(pageSource,  "jwebmethods_cart_url[\\s]*=escape\\(\"(.*)\"\\)");
            System.out.println("jwebmethods_cart_url: " + jwebmethods_cart_url);
            jwebmethods_cart_url = jwebmethods_cart_url.substring(jwebmethods_cart_url.indexOf('\"')+1,jwebmethods_cart_url.lastIndexOf('\"'));
             System.out.println("jwebmethods_cart_url: " + jwebmethods_cart_url);

            jexit_url = extractValueUsingRegex(pageSource,  "jexit_url[\\s]*=escape\\(\"(.*)\"\\)");
            System.out.println("jexit_url: " + jexit_url);
            jexit_url = jexit_url.substring(jexit_url.indexOf('\"')+1,jexit_url.lastIndexOf('\"'));
             System.out.println("jexit_url: " + jexit_url);

            url = "https://www.eway.com/eway/celogin.p_start_b2b_punchout_session?user_id="+user_id+"&b2b_profile_id="+jb2b_profile_id+"&changing=F&browser=" + browser + "&version=" + browser_version + "&os=UNKNOWN" + "&sessionID=" + jSessionID + "&operation=" + joperation + "&webmethods_cart_url="+jwebmethods_cart_url+"&exit_url="+jexit_url+"&dept_prompt="+jdept_prompt+"&zipcode="+jzipcode;
                 // https://www.eway.com/eway/celogin.p_ce_eway_b2b_punchout
             System.out.println("URL: " + url);

         }
         catch(Exception e)
         {
             e.printStackTrace();
         }

        return url;

    }

}
