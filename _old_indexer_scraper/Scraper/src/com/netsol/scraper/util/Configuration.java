package com.netsol.scraper.util;


import java.util.ArrayList;

public class Configuration
{

	public String baseConfig;
	public String readerConfig;
	public String temp;

	public String url;
	public String baseUrl;
	public String filePath;
	public String fileName;

	public String defaultDownloader;
	public String defaultReader;
	public String defaultTransformer;

	public String regexForCategory;
	public String regexForSubcategory;

	public String regexForSkuUrl;
	public String regexForAdditionalUrl;
	public String regexForSkuUrlItem;
	public String regexForNextSku;

	public boolean enableScripting = true;
	public boolean isApplyRegixToIntUrl = true;
	public boolean isApplyRegixToCatUrl = true;
	public ArrayList<String> listOfInitialUrls = new ArrayList<String>();
	public ArrayList<String> listOfVisitedUrls = new ArrayList<String>();
}
