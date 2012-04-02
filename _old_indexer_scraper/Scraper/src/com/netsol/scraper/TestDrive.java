package com.netsol.scraper;

import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

public class TestDrive 
{
	
	public static String getUrl(String pageSource , String regex) throws Exception
	{
		String url = null;
		 Pattern regexForHref=Pattern.compile(regex);
		    Matcher matchList=regexForHref.matcher(pageSource);
		    String tmpmatch="";
		    while(matchList.find())
		    {
		    	tmpmatch=matchList.group();
		    	if(!tmpmatch.equals(""))
		    	{  		
		    		url = tmpmatch;
		        }
		   }
		return url;
	}
	public static void main(String argu[]) 
	{
		try 
		{
			String url = null;
			Variable var = null;
		    ScraperConfiguration config =    new ScraperConfiguration("c:/config.xml");
		    Scraper scraper = new Scraper(config, "c:/temp/");	    	    
		   /*
		    scraper.addVariableToContext("fileName", "login");
		    scraper.addVariableToContext("url", "https://chi2.ebiz.grainger.com/invoke/BVProcess.SAPOCI/processCatalogRequest?USERNAME=northgrum811357656tp&PASSWORD=pHav7349&HOOK_URL=http://www.smartoci.com");		    
		    scraper.execute();
		    
		    scraper.addVariableToContext("fileName", "home");
		    scraper.addVariableToContext("url", "https://niles4.ebiz.grainger.com/Grainger/wwg/start.shtml");		    
		    scraper.execute();
		    
		    scraper.addVariableToContext("fileName", "search");
		    scraper.addVariableToContext("url", "https://niles4.ebiz.grainger.com/Grainger/abrasives/ecatalog/N-bi1?op=search");
		    scraper.execute();
		    
		    scraper.addVariableToContext("fileName", "product");
		    scraper.addVariableToContext("url","https://niles4.ebiz.grainger.com/Grainger/items/4ZR10?Pid=search");
		    scraper.execute();
		   
		    */
		    
		    //scraper.addVariableToContext("url", "https://bci.stapleslink.com:3079/invoke/StaplesCommerceOne/receive?USERNAME=PITNEY&PASSWORD=AX9013_PY1&HOOK_URL=http://www.smartoci.com");
		    scraper.addVariableToContext("url", "https://mercury.rev.net/apd/cgi-bin/ociConnect4?USERNAME=ngc12410&PASSWORD=fR46PEsw&HOOK_URL=http://www.smartoci.com");
		    
		    scraper.execute();
		    
		    
		    //scraper.addVariableToContext("url", "http://www.google.com.pk");
		    scraper.addVariableToContext("isInclude", "y");		    
		    scraper.execute();
		    
		    
		    /*
		    scraper.addVariableToContext("url", "https://biz.officedepot.com/catalog/brandSearch.do;jsessionid=0000Gr0Fp8WMGD_RRQg4Pp9zHd6:14bs17k02");
		    scraper.execute();
		    */
		    
		    /*url = getUrl(var.toString() ,"/catalog/brandSearch.do[-a-zA-Z0-9@:;%_\\+.~#?&//=]+");
		    url = "https://biz.officedepot.com"+url;
		    scraper.addVariableToContext("url", url);		    
		    scraper.execute();
		    var = scraper.getContext().getVar("pageContent");
		    System.out.println(var);*/
		    
//		    scraper.addVariableToContext("url", "http://www.stapleslink.com/webapp/wcs/stores/servlet/home?ts=1274528226818");
//		    scraper.execute();
		    System.out.println("finished");
		}
		catch (FileNotFoundException e) 
		{
		    System.out.println(e.getMessage());
		}
		catch (Exception ex)
		{
			
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	/**
	 *
	 * Lyreco javascript
	 * 
	 function more(prodh) {
	//refresh de la frame main
	var countrycode = 'CA';
	parent.principal.location.href='http://www.lyreco.com/OnLineOrderingWeb/P02/M01/catalogueMainWait.do;jsessionid=0000BwmltkFqXphz8wyIwHM8lyr:14c4q742o?searchedCatCode='+prodh+'&refresh=true&countrycode='+countrycode+prodh+'&olonocachekey=-7066122693994617341';
	}
	 */

}
