 package com.netsol.scraper;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class TestJavaScript 
{
	public static void main (String argu[])
	{
		ScriptEngineManager manager = new ScriptEngineManager ();
        ScriptEngine engine = manager.getEngineByName ("js");
        String function = "function more(prodh){var countrycode = 'CA';	return 'http://www.lyreco.com/OnLineOrderingWeb/P02/M01/catalogueMainWait.do;jsessionid=0000BwmltkFqXphz8wyIwHM8lyr:14c4q742o?searchedCatCode='+prodh+'&refresh=true&countrycode='+countrycode+prodh+'&olonocachekey=-7066122693994617341';}";
        try 
        {
        	
    	    if (engine instanceof Compilable) 
    	    {
    	      Compilable compEngine = (Compilable) engine;
    	      CompiledScript script = compEngine.compile(function+"more(45);");
    	      System.out.println(script.eval());
    	        	     
    	    }
    	    else 
    	    {
    	      System.err.println("Engine can't compile code");
    	    }
        }
        catch (ScriptException e) 
        {
        	
            e.printStackTrace();
        }
	}
}
