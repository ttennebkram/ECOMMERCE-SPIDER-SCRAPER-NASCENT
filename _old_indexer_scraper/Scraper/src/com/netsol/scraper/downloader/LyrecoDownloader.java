package com.netsol.scraper.downloader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import com.netsol.scraper.util.Configuration;

public class LyrecoDownloader extends BaseDownloader
{
	private String regexForCategoryJS = "function\\smore\\(prodh\\).*\\s.*\\s.*\\s.*\\s.*\\s.*\\s.*\\s.*\\s.*";
	private String categoryJSReplaceTarget = "parent.principal.location.href=";
	private String categoryJSReplaceValue  = "return ";

	private String regexForSubCategoryReDirection = "http://www.lyreco.com/OnLineOrderingWeb/P[0-9]+/M[0-9]+/catalogueMain.do;.*&includeMenu=true";
	private String regexForSearchReDirection  ="http://www.lyreco.com/OnLineOrderingWeb/P[0-9]+/M[0-9]+/search.do.*nul";
	private String regexForSearchResultReDirection  ="http://www.lyreco.com/OnLineOrderingWeb/P[0-9]+/M[0-9]+/searchResultList.do.*olonocachekey=.[0-9]+";
	private String regexForSubCategoryJs = "function\\smore\\(prodh,endLevel\\).*\\s.*\\s.*\\s.*\\s.*\\s.*\\s.*\\s.*\\s.*\\s.*\\s.*\\s.*\\s.*";
	private String subCategoryJSReplaceTarget = "this.location.href=";
	private String subCategoryJSReplaceValue  = "return ";
	private List<String> visitedCategories = new ArrayList<String>();
	@Override
	public void start(Configuration config, Logger logger)
	{
		try
		{
			ScraperConfiguration scraperConfig =    new ScraperConfiguration(config.baseConfig);
			Scraper scraper = new Scraper(scraperConfig, config.temp);
			String pageSource = navigateToInitialUrls(scraper , config , logger);
			if(pageSource != null)
			{
				String function = super.extractValueUsingRegex(pageSource, regexForCategoryJS);
				function = function.replaceAll(categoryJSReplaceTarget, categoryJSReplaceValue);

				List<String>categoryUrl =  fetchCategoryUrls(pageSource, config, logger);

				List<String>superCategoryUrls = executeJsToGetCategoryUrl(categoryUrl , function);
				for(int i=0 ; i < superCategoryUrls.size() ; i ++)
				{
					System.out.println(superCategoryUrls.get(i));
				}
				List<String>subCategoryUrls = fetchSubCategoryUrls(scraper , superCategoryUrls , config , logger);
				for(int i=0 ; i < subCategoryUrls.size() ; i ++)
				{
					System.out.println(subCategoryUrls.get(i));
				}
				super.fetchProducts(scraper, subCategoryUrls, config, logger);

			}
		}
		catch(Exception ex)
		{
			logger.error("error" , ex);
		}
	}
	private List<String> fetchSubCategoryUrls(Scraper scraper , List<String> superCategoryUrls  , Configuration config , Logger logger)
	{
		try
		{
			String function = null;
			String pageSource = null;
			Variable var = null;
			List<String> subCategoryUrls = new LinkedList<String>();

			for(int i = 0 ; i < superCategoryUrls.size() ; i ++)
			{
				logger.info(superCategoryUrls.get(i));
				iterateSubCategoryRecursivly(scraper, config,superCategoryUrls.get(i), subCategoryUrls , logger );
			}

			return subCategoryUrls;
		}
		catch(Exception ex)
		{
			logger.error("error" , ex);
		}
		return null;
	}

	private void iterateSubCategoryRecursivly(Scraper scraper, Configuration config, String targetURL, List<String> skuURLs, Logger logger )throws Exception
	{

		scraper.addVariableToContext("url", targetURL);
		scraper.execute();
		Variable var = getVariable(scraper, "pageContent", logger);
		String	pageSource = var.toString();


		// Extract Redirect url for redirecting.
		String redirectUrl =  super.extractValueUsingRegex(pageSource, regexForSubCategoryReDirection);
		//Redirect according to redirection url
		if(redirectUrl != null)
		{
			scraper.addVariableToContext("url", redirectUrl);
			scraper.execute();
			var = getVariable(scraper, "pageContent", logger);

			String function = extractValueUsingRegex(var.toString(), regexForSubCategoryJs);
			function = function.replaceAll(subCategoryJSReplaceTarget, subCategoryJSReplaceValue);
			List<String> subCategory = extractNextSubCategory(var.toString(), config.regexForSubcategory);
			for(int i =0 ; i< subCategory.size() ; i++)
			{
				String url = executeJavaScript(function, subCategory.get(i) );
				iterateSubCategoryRecursivly(scraper, config,url, skuURLs , logger );
			}
		}
		else
		{
			redirectUrl =  super.extractValueUsingRegex(pageSource, regexForSearchReDirection);
			scraper.addVariableToContext("url", redirectUrl);
			scraper.execute();
			var = getVariable(scraper, "pageContent", logger);

			redirectUrl = extractValueUsingRegex(var.toString(), regexForSearchResultReDirection);
			scraper.addVariableToContext("url", redirectUrl);
			scraper.execute();
			skuURLs.add(redirectUrl);
			return ;
		}



	}
	private String executeJavaScript(String function , String methodCall)
	{
		ScriptEngineManager manager = new ScriptEngineManager ();
		ScriptEngine engine = manager.getEngineByName ("js");
		try
		{
			if (engine instanceof Compilable)
			{
				Compilable compEngine = (Compilable) engine;
				CompiledScript script = compEngine.compile(function+methodCall);
				return script.eval().toString();
			}
	    }
	    catch (ScriptException e)
	    {

	        e.printStackTrace();
	    }
	    return null;
	}

	private List<String> executeJsToGetCategoryUrl(List<String> category , String function)
	{
		List<String> extractedUrls = new LinkedList<String>();
		ScriptEngineManager manager = new ScriptEngineManager ();
		ScriptEngine engine = manager.getEngineByName ("js");

		try
		{
			if (engine instanceof Compilable)
			{
				for(int i = 0 ; i < category.size() ; i++)
				{
					Compilable compEngine = (Compilable) engine;
					CompiledScript script = compEngine.compile(function+category.get(i));
					extractedUrls.add(script.eval().toString());
					visitedCategories.add(script.eval().toString());

				}
			}

	    }
	    catch (ScriptException e)
	    {

	        e.printStackTrace();
	    }
	    return extractedUrls;

	}

	protected List<String> extractNextSubCategory(String pageSource , String regex) throws Exception
	{
		List<String> list = new LinkedList<String>();
		String value = null;
		Pattern pattern=Pattern.compile(regex);
		Matcher matchList=pattern.matcher(pageSource);
		String tmpmatch="";

		while(matchList.find())
		{
			tmpmatch=matchList.group();
			if(!tmpmatch.equals("") && visitedCategories.contains(tmpmatch) == false )
			{
				value = tmpmatch;
				list.add(value);
				visitedCategories.add(value);

			}
		}
		return list;
	}


}
