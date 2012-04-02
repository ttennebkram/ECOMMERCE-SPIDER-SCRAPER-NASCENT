package com.netsol.scraper.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import com.netsol.scraper.util.Configuration;

public class CacheUtilsNIE {
	// Intermediate directories to prevent too many files in single dir
	private static final int SLICE_WIDTH = 2;  // 2 hex digits = 256
	private static final int NUM_SLICES = 2;   // 2 levels deep = 256^2
	// Example: 1 million docs
	// divided by 65k
	// average: 15 docs per dir
	
	public static final char SEP = File.separatorChar; //NOT File.pathSeparator

	public static String fetchUrlWithCaching( String url, String parentCacheDir, Scraper scraper, boolean enableScripting, Logger logger ) {
		if ( null==url || url.trim().isEmpty() ) {
			throw new IllegalArgumentException( "Null/empty URL passed in." );
		}

		// Step 1: Calc cache name
		String fullPath = calcUrlCacheFileNameForURL( parentCacheDir, url, false );

		// Step 2: Attempt to READ from Cache
		String content = readFromFileOrNull( fullPath, logger, false );
	    if ( null!=content && ! content.trim().isEmpty() ) {
    		logger.info( "CACHE HIT: read "+content.length()+" chars from file \""+fullPath + "\", for URL \""+url+'"' );
	    	// if ( logger.isDebugEnabled() ) {
	    		// logger.debug( "CACHE HIT: read "+content.length()+" chars from: "+fullPath );
	    	// }
	    	return content;
	    }

	    // Step 3: Fetch the content
		scraper.addVariableToContext( "url", url );
		if ( ! enableScripting ) {
            scraper.removeRunningFunction();
		}
		logger.info( "DOWNLOADING: "+url );
		scraper.execute();
		Variable var = scraper.getContext().getVar( "pageContent" );
		if( null != var ) {
			content = var.toString();
		}
		content = "<url>" + url + "</url>" + content;

		// Step 4: Write to cache
		try {
    		logger.debug( "CACHE SAVE: writing "+content.length()+" chars to file \""+fullPath+"\", for URL \""+url+'"' );
			writeToFile( content, fullPath, logger );
		} catch (IOException e) {
			logger.warn( "Unable to WRITE to cache for file \""+fullPath+"\", exception: ", e );
		}
				
		return content;
	}

	public static void writeToFile(String pageSource, String directory,
            String fileName, Logger logger
	) {
		FileWriter writer = null;
		try {
			File fileFolder = new File(directory);
			if (!fileFolder.exists()) {
				fileFolder.mkdirs();
			}
			String fullName = directory + fileName + ".htm";
			logger.info( "WRITING FILE '" + fullName + "'" );
			writer = new FileWriter( fullName, false );
			byte[] bytes = pageSource.getBytes("UTF-8");
			writer.write(new String(bytes));
			writer.close();
		}
		catch (Exception ex) {
			logger.error("error", ex);
		}
	}

    public static void writeToFile( String pageSource,
    		String fullName, Logger logger
	) throws IOException {
		FileWriter writer = null;
		File fullFile = new File( fullName );
		// Verify directory
		String dirName = fullFile.getParent();
		if ( null!=dirName ) {
			File dirPath = new File( dirName );
			if (!dirPath.exists()) {
				if ( ! dirPath.mkdirs() ) {
					// Double check, maybe another thread just created it?
					try {
						// If the system a chance to catch up, this should be very rare
						 // 100ms, 1/10th of a second
						Thread.sleep(100);
					} catch (InterruptedException e) {
						logger.warn( "Sleep interrupted before rechecking dir \""+ dirName + "\"");
					}
					if (!dirPath.exists()) {
						throw new IOException( "Unable to create dir \""+ dirName + "\"");
					}
				}
			}
			else if ( !dirPath.isDirectory() ) {
				throw new IOException( "Not a directory? - \""+ dirName + "\"");					
			}
		}
		
		logger.info( "WRITING FILE '" + fullName + "'" );
		writer = new FileWriter( fullName, false );
		// byte[] bytes = pageSource.getBytes("UTF-8");
		// writer.write(new String(bytes));
		writer.write( pageSource );
		writer.close();
	}
	
    public static String readFromFile( String directory,
            String fileName, Logger logger, boolean reportAsErrors )
	{
    	return readFromFileOrNull( directory, fileName, logger, true );
	}
    public static String readFromFileOrNull( String directory,
            String fileName, Logger logger, boolean reportAsErrors )
	{
		BufferedReader reader = null;
		try {
			String fullName = directory + fileName + ".htm";
			logger.debug( "Reading file '" + fullName + "'" );
			reader = new BufferedReader( new FileReader(fullName) );
			StringBuffer buff = new StringBuffer();
		    String line = null;
		    while( null != (line = reader.readLine()) ) {
		        buff.append( line );
		    }
		    reader.close();
		    return new String( buff );
		}
		catch (Exception ex) {
			/*if ( reportAsErrors ) {
				logger.error( "Error reading file", ex );
			}
			else {
				logger.debug( "Unable to read file", ex );
			}*/
			return null;
		}	
	}
    public static String readFromFileOrNull( String fullName,
    		Logger logger, boolean reportAsErrors )
	{
		BufferedReader reader = null;
		try {
			logger.debug( "Reading file '" + fullName + "'" );
			reader = new BufferedReader( new FileReader(fullName) );
			StringBuffer buff = new StringBuffer();
		    String line = null;
		    while( null != (line = reader.readLine()) ) {
		        buff.append( line );
		    }
		    reader.close();
		    return new String( buff );
		}
		catch (Exception ex) {
			/*if ( reportAsErrors ) {
				logger.error( "Error reading file", ex );
			}
			else {
				logger.debug( "Unable to read file", ex );
			}*/
			return null;
		}	
	}

    // TODO: Do we need exception handling?
    protected Variable _getVariable(Scraper scraper, String varName,
            Logger logger)
	{
		// try {
			return scraper.getContext().getVar(varName);
		// }
		// catch (Exception ex) {
		//	logger.error("error", ex);
		//	return null;
		//}
	}

	/**
	 * Create a consistent and safe cache file name for a URL
	 * Uses MD5 for uniqueness
	 * ALSO creates an intermediate directory structure based on MD5
	 * Adds a cleaned version of the URL itself
	 * Adds ".html" extension
	 *
	 * INPUT:
	 * url = http://google.com/cgi-bin/foo?q=hello+world#marker
	 * dir = cache/dir
	 * OUTPUT:
	 * final dir = cache/dir/2d/ad/
	 *  filename = cache/dir/2d/ad/2dad12aa8982927f24fa80f0912264a6_http___google_com_cgi-bin_foo_q_hello_world_marker.html
	 *  features   ^dir     ^span-dirs   ^md5                          ^cleaned-url                                   ^ext
	 * 
	 * @param cacheDir
	 * @param url
	 * @param createDirIfMissing
	 * @return
	 */
	public static String calcUrlCacheFileNameForURL( String cacheDir, String url ) {
		return calcUrlCacheFileNameForURL( cacheDir, url, false );
	}
	static String calcUrlCacheFileNameForURL( String cacheDir, String url, boolean createDirIfMissing ) {
		if ( null==cacheDir || null==url ) {
			return null;
		}
		// Start with base Dir
		StringBuffer outBuff = new StringBuffer( cacheDir );
		if ( ! cacheDir.endsWith("/") ) {
			outBuff.append('/');
		}
		// Directory tree and filename based on MD5
		String md5Str = md5( url );
		String spanDirs = chopper( md5Str );
		outBuff.append( spanDirs );
		outBuff.append( SEP );
		// Check the Dir
		if ( createDirIfMissing ) {
			// Grab an intermediate snapshot
			String dirName = new String( outBuff );
			File checkDir = new File( dirName );
			if ( ! checkDir.exists() ) {
				// Try to create it
				if ( ! checkDir.mkdirs() ) {
					throw new IllegalArgumentException( "Failed to create directory: \""+checkDir.toString()+'"');					
				}
				/***
				else {
					System.out.println( "Created directory: \""+checkDir.toString()+'"');
					// System.out.println( "\tfull path: \""+checkDir.getCanonicalPath().toString()+'"');
					System.out.println( "\tfull path: \""+checkDir.getAbsolutePath()+'"');
				}
				***/
			}
			// Exists, do some other checking
			else {
				if ( ! checkDir.isDirectory() ) {
					throw new IllegalArgumentException( "Not a directory: \""+checkDir.toString()+'"');
				}
			}
		}
		
		// Continue building full path
		// by adding the filename portion
		outBuff.append(md5Str);
		// Add on the clean URL
		String cleanUrlStr = cleanURL( url );
		outBuff.append('_');
		outBuff.append(cleanUrlStr);
		outBuff.append(".html");
		
		return new String( outBuff );
	}

    public static String normalizeUrl( String url, String optBaseUrl ) {
    	//System.out.println("normalizeUrl: " + url + " optBaseUrl: " + optBaseUrl);
    	if ( null==url || url.trim().isEmpty() ) {
    		throw new IllegalArgumentException( "URL was null" );
    	}
        url = url.replaceAll("&amp;", "&");
        url = url.replaceAll(  " ", "%20");
        if ( null!=optBaseUrl && !validateUrl(url) ) {
            url = optBaseUrl + (!optBaseUrl.endsWith("/") && !url.startsWith("/") ? "/" : "") + url;
        }
    	//System.out.println("url: " + url);        
        return url;
    }
    public static boolean validateUrl(String url)
    {
        Pattern pattern = Pattern.compile("((https?|ftp|gopher|telnet|file|notes|ms-help):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\.&]*)");
        Matcher m = pattern.matcher(url);
        return m.matches();
    }

	public static String cleanURL( String inURL ) {
		if ( null==inURL ) {
			return null;
		}
		// Java caches compiled Regex's, so no need to buffer a pattern
	    return inURL.replaceAll( "[^-_A-Za-z0-9]", "_" );
	}
	public static String md5( String inStr ) {
		if ( null==inStr ) {
			return null;
		}
		byte[] myBytes;
		MessageDigest md5Proc;
		try {
			myBytes = inStr.getBytes("UTF-8");
			md5Proc = MessageDigest.getInstance("MD5");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		byte[] theDigest = md5Proc.digest( myBytes );
		return byteArrayToString( theDigest );
	}
	public static String byteArrayToString( byte[] inBytes ) {
	    if ( null==inBytes || 0==inBytes.length ) {
	        return null;
	    }
	    StringBuffer outBuff = new StringBuffer();
	    for (int i=0; i<inBytes.length; i++ ) {
	        outBuff.append( Integer.toHexString( 0xFF & inBytes[i] ) );
	    }
	    return new String( outBuff );
	}
	
	public static String chopper( String inStr ) {
		if ( null==inStr || inStr.length()<NUM_SLICES*SLICE_WIDTH) {
			return null;
		}
		StringBuffer outBuff = new StringBuffer();
		for ( int i=0; i<NUM_SLICES; i++ ) {
			if ( i>0 ) {
				outBuff.append( SEP );
			}
			String fragment = inStr.substring( i*SLICE_WIDTH, (i+1)*SLICE_WIDTH );
			outBuff.append( fragment );
		}
		return new String( outBuff );
	}

	public static void main( String[] args ) {
		String myStr1 = "http://google.com/cgi-bin/foo?q=hello+world#marker";
		String myStr2 = cleanURL( myStr1 );
		String myStr3 = md5( myStr1 );
		System.out.println( "myStr1 = " + myStr1 );
		System.out.println( "myStr2 = " + myStr2 );
		System.out.println( "myStr3 = " + myStr3 );
		String cacheDir = "cache/dir";
		String myStr4 = calcUrlCacheFileNameForURL( cacheDir, myStr1, true );
		System.out.println( "myStr4 = " + myStr4 );
	}

}
