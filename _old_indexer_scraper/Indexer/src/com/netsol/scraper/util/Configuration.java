package com.netsol.scraper.util;


import java.util.ArrayList;

public class Configuration
{
	public String baseConfig;
	public String readerConfig;
	public String configurationPath;
	public String temp;
	public String baseUrl;
	public String filePath;
	public String fileName;
	public String clearConfig;
	public String unspscConfig;
	public String defaultDownloader  = "com.netsol.scraper.downloader.BaseDownloader" ;
	public String defaultReader 	 = "com.netsol.scraper.reader.BaseReader";
	public String defaultTransformer = "com.netsol.scraper.transformer.BaseTransformer";
	public String destinationFolder;
	public String dateFormatForTimestamps = "dd-MM-yyyy";
	public String catalogId;
	public String vendorId ;
	public String userId ;
    public String unitId;
	public String supplierName;
    public String serverUrl;
    public String tomcatPath;
    public String applicationUrl;
    public String modelPath = "/home/smartoci/mahout/Model";

	public String regexForSkuUrl;
	public String masterRegexForTotalProducts;
	public String regexForTotalProducts;
	public String masterRegexForTotalPages;
	public String regexForTotalPages;

	public ArrayList<String> listOfInitialUrls = new ArrayList<String>() ;
	public ArrayList<String> listOfProductUrls = new ArrayList<String>() ;


}
