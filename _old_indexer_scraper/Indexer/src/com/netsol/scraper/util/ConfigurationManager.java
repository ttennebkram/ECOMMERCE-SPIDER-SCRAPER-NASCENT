package com.netsol.scraper.util;

import java.io.File;
import java.io.FileReader;

import org.exolab.castor.xml.Unmarshaller;

public class ConfigurationManager
{
	public static Object loadConfiguration(Class classType , String fileName)
	{
		try
        {   
            File file = new File(fileName);
            FileReader fr=new FileReader(file);
            return Unmarshaller.unmarshal(classType, fr);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
}
