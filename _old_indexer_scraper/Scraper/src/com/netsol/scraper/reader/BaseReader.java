package com.netsol.scraper.reader;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.netsol.scraper.util.Configuration;
import com.netsol.scraper.util.ReaderConfiguration;

public class BaseReader 
{
	public void start(Configuration config, ReaderConfiguration readerConfig, Logger logger)
	{
		try
		{
			List<String> files = getFiles(config.filePath , logger);
			for(int i = 0 ;  i < files.size() ; i ++)
			{
				String fileContent = readFile(files.get(i) , logger);
			}
		}
		catch(Exception ex)
		{
			logger.error("error", ex);
		}
	}
	
	private List<String> getFiles(String directory , Logger logger)
	{
		List<String> files = new LinkedList<String>();
		try
		{
			
		}
		catch(Exception ex)
		{
			logger.error("error" , ex);
		}
		return files;
	}
	
	private String readFile(String filePath , Logger logger)
	{
		String fileContent = null;
		try
		{
			
		}
		catch (Exception ex)
		{
			logger.error("error" , ex);
		}
		return fileContent;
	}
	
	private Map<String , String> scrapData(String fileContent,ReaderConfiguration readerConfig )
	{
		Map map = new HashMap();
		
		return map;
	}

}
