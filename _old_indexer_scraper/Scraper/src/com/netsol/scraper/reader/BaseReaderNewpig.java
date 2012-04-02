package com.netsol.scraper.reader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.netsol.scraper.util.Configuration;
import com.netsol.scraper.util.ReaderConfiguration;
import com.netsol.scraper.util.TagInformation;
import org.apache.commons.lang.StringEscapeUtils;
import com.netsol.scraper.util.Utils;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.*;
import java.sql.*;

import com.netsol.scraper.util.SolrManager;
import com.netsol.scraper.DBConnection;
import org.apache.solr.client.solrj.*;
import au.com.bytecode.opencsv.CSVWriter;

import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.variables.Variable;

import javax.transaction.SystemException;


public class BaseReaderNewpig
{

    private static int[] invalid = {65533, 194, 226, 128, 157, 162};
    private static List<Integer> invalidCharacters = new ArrayList<Integer>();

    static
    {
        for (int i : invalid)
        {
            invalidCharacters.add(i);
        }
    }

    public void getUNSPSCmap(HashMap UNSPSCmap, HashMap idMap, HashMap variesMap) {
    	//System.out.println("starting UNSPSC load");

        try {
    		File file = new File("Northrop Product UNSPSC Map.csv");

    		BufferedReader bufRdr  = new BufferedReader(new FileReader(file));
    		String line = bufRdr.readLine();
    		while((line = bufRdr.readLine()) != null)
    		{
    			//System.out.println("line: " + line);
    			int col = 0;
    			String itemNo = "";
    			String UNSPSC = "";
    			StringTokenizer st = new StringTokenizer(line,",");
    			while (st.hasMoreTokens())
    			{
					//System.out.println("col: " + col);
    				if (col == 0) {
    					itemNo = st.nextToken();
    					//System.out.println("itemNo: " + itemNo);
    				}
    				else if (col == 1) {
    					UNSPSC = st.nextToken().trim();
    					//System.out.println("UNSPSC: " + UNSPSC);
    					UNSPSCmap.put(itemNo, UNSPSC);
    				}
    				else if (col == 2) {
    					String id = st.nextToken().trim();
    					//System.out.println("id: " + id);
    					idMap.put(itemNo, id);
    				}
    				else if (col == 3) {
    					String varies = st.nextToken().trim();
    					//System.out.println("varies: " + varies);
    					if (varies.equals("1")) {
    						variesMap.put(itemNo, "X");
    					}
    					break;
    				}
					col++;
    			}
    		}
    		bufRdr.close();
    		//System.out.println(UNSPSCmap);
    		//System.out.println(idMap);
    		//System.out.println(variesMap);
        }
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	//System.out.println("finished UNSPSC load");
    }

    public void writeCSVline(CSVWriter out, ItemValuesBean itemBean, HashMap UNSPSCmap, String url, String varies, int itemNo) {
        try {
            String UNSPSC = (String) UNSPSCmap.get(itemBean.getItemNo());
            if (UNSPSC == null) {
            	System.out.println(itemBean.getItemNo());
            	String[] entries = {Integer.toString(itemNo), itemBean.getDescription(), "", "EA", itemBean.getPrice(), "USD", itemBean.getItemNo(), itemBean.getItemLongText1(), itemBean.getImage(), url, varies};
            	out.writeNext(entries);
            }
            else {
            	String[] entries = {Integer.toString(itemNo), itemBean.getDescription(), UNSPSC, "EA", itemBean.getPrice(), "USD", itemBean.getItemNo(), itemBean.getItemLongText1(), itemBean.getImage(), url, varies};
            	out.writeNext(entries);
            }
        }
        catch (Exception e){
    			e.printStackTrace();
    	}
    }

    public void getPriceInfo(Scraper scraper, ItemValuesBean itemBean, String productId, Logger logger) {
    	try {
    		scraper.addVariableToContext("url", "https://www.newpig.com/webapp/wcs/stores/servlet/RangePriceDisplayView?storeId=11151&catalogId=10151&langId=-1&productId=" + productId);
    		scraper.execute();
    		Variable var = scraper.getContext().getVar("pageContent");
    		String text = var.toString();
    		String price = text.substring(1, text.length());
    		//System.out.println("price: " + price);
    		itemBean.setPrice(price);
    	}
        catch (Exception ex) {
            ex.printStackTrace();
            //logger.error("Error", ex);
        }
    }

    public void start(Configuration config,
                      ReaderConfiguration readerConfig,
                      ClearConfiguration clearConfiguration,
                      UnspscConfiguration unspscConfiguration,
                      Logger logger) throws IOException,
            SecurityException, NoSuchMethodException
    {
        ScraperConfiguration scraperConfig = new ScraperConfiguration("resources/scraper.xml");
        Scraper scraper = new Scraper(scraperConfig, config.temp);
		scraper.addVariableToContext("url", "https://www.newpig.com/webapp/wcs/stores/servlet/OCIPunchOutSetup?USERNAME=ngc001&PASSWORD=OCI1Network&HOOK_URL=http://www.smartoci.com");
		scraper.execute();

        List<File> returnList = new ArrayList<File>();
        List<File>
                files = getFiles(new File(config.filePath), logger, returnList);
        ItemValuesBean itemBean = null;
        //Classify classify = new Classify();

        //SolrManager manager = new SolrManager();

        String vendorId = null; //sendMessageToSolr(Utils.createVendorSolrMessage(config.catalogId), config.serverUrl);
        String vendorName = "";

        if (!isNullOrVoid(vendorId))
        {
            config.vendorId = vendorId.substring(0, vendorId.lastIndexOf("|"));
            vendorName = vendorId.substring(vendorId.lastIndexOf("|") + 1);
        }

        File directory = new File(config.tomcatPath + File.separator + "company-images" + File.separator + config.unitId + File.separator + "external-catalog" + File.separator + config.catalogId);

        if(!directory.exists())
            directory.mkdirs();

    	CSVWriter csvWriter = new CSVWriter(new FileWriter("newpig.csv"), ',');
    	String[] entries = "#ItemNumber,NEW_ITEM-DESCRIPTION,NEW_ITEM-MATGROUP,NEW_ITEM-UNIT,NEW_ITEM-PRICE,NEW_ITEM-CURRENCY,NEW_ITEM-VENDORMAT,NEW_ITEM-LONGTEXT,NEW_ITEM-IMAGE,NEW_ITEM-URL,ITEM-CONFIG".split(",");
    	csvWriter.writeNext(entries);

    	HashMap UNSPSCmap = new HashMap();
    	HashMap idMap = new HashMap();
    	HashMap variesMap = new HashMap();
    	HashSet itemNoSet = new HashSet();
    	getUNSPSCmap(UNSPSCmap, idMap, variesMap);
        int itemNo = 1;

        for (File file : files)
        {
            try
            {
                itemBean = scrapData(readFile(file,
                        logger), readerConfig, logger);

                itemBean.setVendorName(vendorName);
                itemBean.setSupplierName(vendorName);

                clearData(itemBean, clearConfiguration);

                /*if (isNullOrVoid(itemBean.getItemMatGroup()) &&
                        unspscConfiguration != null &&
                        unspscConfiguration.listOfUnspscTagInformation != null)
                {
                    assignUnspsc(file.getParentFile().getName(),
                            unspscConfiguration, itemBean);
                }*/

                if (itemBean.getImage() != null &&
                        !itemBean.getImage().trim().isEmpty() &&
                        !validateUrl(itemBean.getImage()))
                {
                    itemBean.setImage(config.baseUrl + itemBean.getImage());
                }

                /*if (isNullOrVoid(itemBean.getItemMatGroup()))
                {
                    itemBean.setItemMatGroup(classify.classifyItem(config.modelPath, "UTF-8", null, "unknown", "1", "cbayes", "hdfs", itemBean.getDescription() + " " + itemBean.getItemLongText1())); //added by Hasnain
                }*/

                if(!isNullOrVoid(itemBean.getImage()))
                {
                    try
                    {
                        downloadImage(config, itemBean);
                    }
                    catch(Exception exp1)
                    {
                        String itemImage = itemBean.getImage();
                        itemBean.setImage(itemBean.getImage().replaceAll("https", "http"));
                        try
                        {
                            downloadImage(config, itemBean);
                        }
                        catch(Exception exp2)
                        {
                            itemBean.setImage(itemImage);
                        }
                    }
                }

                if (!validation(itemBean))
                {
                    logger.error(file.getAbsolutePath());
                    continue;
                }

                //uploadInSolr(itemBean, config);
                //addItemsInDB(itemBean);
                itemNoSet.add(itemBean.getItemNo());
                //String productId = (String) idMap.get(itemBean.getItemNo());
                String productId = itemBean.getUrl().split("productId=")[1].split("&")[0];
                getPriceInfo(scraper, itemBean, productId, logger);
            	String url = "https://www.newpig.com/pig/ProductDisplay?storeId=11151&catalogId=10151&langId=-1&productId=" + productId;
            	String varies = (String) variesMap.get(itemBean.getItemNo());
            	if (varies == null) {
            		varies = "";
            	}
                writeCSVline(csvWriter, itemBean, UNSPSCmap, url, varies, itemNo);
                itemNo++;

                itemBean = null;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        csvWriter.close();
        Set UNSPSCs = (Set) UNSPSCmap.keySet();
        Iterator it = UNSPSCs.iterator();
        while(it.hasNext()) {
        	String UNSPSC = (String) it.next();
        	if (!itemNoSet.contains(UNSPSC)) {
        		System.out.println(UNSPSC + " not in crawled pages");
        	}
        }


        /*try
        {
            manager.commit(manager.getSolrServer(config.unitId));
            sendMessageToSolr(Utils.createCommitSolrMessage(config.unitId), config.serverUrl);
            if(manager.getCore() != null)
                manager.getCore().shutdown();
        }
        catch (SolrServerException ex1)
        {
            ex1.printStackTrace();
        }
        catch (IOException ex1)
        {
            ex1.printStackTrace();
        }*/
        System.exit(0);
    }

    public static void downloadImage(Configuration config, ItemValuesBean itemBean) throws Exception
    {
        try
        {
            URL url = new URL(itemBean.getImage());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(false);
            connection.setDoInput(true);

            FileOutputStream fos = new FileOutputStream(config.tomcatPath + File.separator + "company-images" + File.separator + config.unitId + File.separator + "external-catalog" + File.separator + config.catalogId + File.separator + itemBean.getItemNo());

            int perNum = 1024; // copy 1024 bytes each time
            byte[] contents = new byte[perNum];
            int readLength = 0;
            try
            {
                while ((readLength = connection.getInputStream().read(contents, 0, perNum)) > 0)
                {
                    fos.write(contents, 0, readLength);
                }
            }
            catch (IOException e)
            {
                fos.close();
                throw e;
            }

            fos.close();

            itemBean.setImage(config.applicationUrl + "/" + "company-images" + "/"+ config.unitId + "/" + "external-catalog" + "/" + config.catalogId + "/" + itemBean.getItemNo());
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    public static String getStringFixed(String str)
    {
        if (isNullOrVoid(str))
        {
            return str;
        }
        StringBuffer aString = new StringBuffer();

        char[] array = str.toCharArray();

        for (char character : array)
        {
            if (invalidCharacters.contains((int) character))
            {
                aString.append(" ");
            }
            else
            {
                aString.append(character);
            }
        }

        return aString.toString().trim();
    }


    public void assignUnspsc(String folderName,
                             UnspscConfiguration unspscConfiguration,
                             ItemValuesBean itemBean)
    {
        for (UNSPSCTagInformation tag :
                unspscConfiguration.listOfUnspscTagInformation)
        {
            if (tag != null &&
                    String.valueOf(tag.catalogName).equals(folderName))
            {
                itemBean.setItemMatGroup(String.valueOf(tag.unspscCodeKey));
                break;
            }
        }
    }

    public static boolean isNullOrVoid(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    public boolean validation(ItemValuesBean bean)
    {

        if (isNullOrVoid(bean.getItemNo()))
        {
        	System.out.println("getItemNo not found");
            return false;
        }
        if (isNullOrVoid(bean.getDescription()))
        {
        	System.out.println("getDescription not found");
            return false;
        }

        if (!isNullOrVoid(bean.getPrice()))
        {
            if (isNullOrVoid(bean.getCurrency()))
            {
            	System.out.println("getCurrency not found");
                return false;
            }
        }
        if (!isNullOrVoid(bean.getPrice()))
        {
            try
            {
                Double.parseDouble(bean.getPrice());
            }
            catch (Exception e)
            {
            	System.out.println("getPrice not found");
                return false;
            }
        }
        else
        {
            bean.setPrice("0");
        }

        if (!isNullOrVoid(bean.getItemNo()) && bean.getItemNo().length() > 64)
        {
        	System.out.println("getItemNo too long");
        	System.out.println("bean.getItemNo(): " + bean.getItemNo());
            return false;
        }
        if (!isNullOrVoid(bean.getDescription()) &&
                bean.getDescription().length() > 256)
        {
            bean.setDescription(bean.getDescription().substring(0, 256));
        }
        if (!isNullOrVoid(bean.getUnit()) && bean.getUnit().length() > 3)
        {
        	System.out.println("getUnit too long");
        	System.out.println("bean.getUnit(): " + bean.getUnit());
            return false;
        }
        if (!isNullOrVoid(bean.getItemPriceUnit()) &&
                bean.getItemPriceUnit().length() > 5)
        {
        	System.out.println("getItemPriceUnit too long");
            return false;
        }
        if (!isNullOrVoid(bean.getCurrency()) &&
                bean.getCurrency().length() > 5)
        {
        	System.out.println("getCurrency too long");
            return false;
        }
        if (!isNullOrVoid(bean.getItemVendor()) &&
                bean.getItemVendor().length() > 10)
        {
        	System.out.println("getItemVendor too long");
            return false;
        }
        if (!isNullOrVoid(bean.getItemVendorMat()) &&
                bean.getItemVendorMat().length() > 40)
        {
        	System.out.println("getItemVendorMat too long");
            return false;
        }
        if (!isNullOrVoid(bean.getItemManufactCode()) &&
                bean.getItemManufactCode().length() > 10)
        {
        	System.out.println("getItemManufactCode too long");
            return false;
        }
        if (!isNullOrVoid(bean.getItemManufactMat()) &&
                bean.getItemManufactMat().length() > 40)
        {
        	System.out.println("getItemManufactMat too long");
            return false;
        }
        if (!isNullOrVoid(bean.getItemLongText1()) &&
                bean.getItemLongText1().length() > 10000)
        {
            bean.setItemLongText1(bean.getItemLongText1().substring(0, 10000));
        }
        if (!isNullOrVoid(bean.getUrl()) && bean.getUrl().length() > 1000)
        {
        	System.out.println("getUrl too long");
            return false;
        }
        if (!isNullOrVoid(bean.getImage()) && bean.getImage().length() > 1000)
        {
        	System.out.println("getImage too long");
            return false;
        }

        return true;
    }


    public void uploadInSolr(ItemValuesBean bean, Configuration config) throws IOException,
            SecurityException, NoSuchMethodException
    {

        if (bean.getItemVendorMat() == null || bean.getItemVendorMat().trim().isEmpty())
        {
            bean.setItemVendorMat(bean.getItemNo());
        }

        bean.setItemNo(config.catalogId + "|" + bean.getItemNo());
        bean.setCatalogId(config.catalogId);
        bean.setItemVendor(config.vendorId);
        bean.setUserId(config.userId);
        bean.setDescription(getStringFixed(bean.getDescription()));
        bean.setItemLongText1(getStringFixed(bean.getItemLongText1()));
        bean.setCurrency(getStringFixed(bean.getCurrency()));
        bean.setPrice(getStringFixed(bean.getPrice()));
        bean.setCatalogType("1");

        if (bean.getCurrency() == null || bean.getCurrency().trim().isEmpty())
        {
            bean.setCurrency("USD");
        }
        if (bean.getUnit() == null || bean.getUnit().trim().isEmpty())
        {
            bean.setUnit("EA");
        }

        /*SolrManager manager = new SolrManager();
        try
        {
            manager.addDocument(manager.getSolrServer(config.unitId), bean);
        }
        catch (SolrServerException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }*/

    }

    public static String sendMessageToSolr(String dataPost, String strUrl)
    {
        try
        {
            URL url = new URL(strUrl);
            URLConnection connection = url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Method", "POST");

            OutputStreamWriter wr = new OutputStreamWriter(connection
                    .getOutputStream());
            wr.write(dataPost);
            wr.flush();
            wr.close();

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line = null;
            String responseString = "";

            while ((line = rd.readLine()) != null)
            {
                responseString = responseString.concat(line);
            }

            rd.close();

            return responseString;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "";
    }


    public void clearData(ItemValuesBean itemToExamine,
                          ClearConfiguration clearConfig) throws
            SecurityException,
            NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException,
            InvocationTargetException
    {
        ItemValuesBean obj = itemToExamine;
        Class cl = ItemValuesBean.class;
        Object iClass = obj;
        for (ClearTagInformation tags : clearConfig.listOfTagInformation)
        {
            Method m1 = cl.getMethod("get" + tags.propertyName);
            String[] sp = new String[]{};
            String str = (String) m1.invoke(iClass, sp);

            if (str != null)
            {
                String[] par = new String[]{str.replaceAll(tags.targetKey,
                        tags.resultKey).trim()};
                Method m2 = cl.getMethod("set" +
                        tags.propertyName,
                        String.class);
                m2.invoke(iClass, par);
            }
        }
    }

    private List<File> getFiles(File directory, Logger logger, List<File>
            returnList)
    {

        try
        {
            //populate the list of files in the directory.

            File[] listOfFiles = directory.listFiles();

            for (File file : listOfFiles)
            {
                if (file.isFile())
                {
                    returnList.add(file);
                }
                else
                {
                    getFiles(file, logger, returnList);
                }
            }

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return returnList;
    }

    private String readFile(File filePath, Logger logger) throws IOException
    {
        StringBuffer stBuff = new StringBuffer();
        FileInputStream fis = null;
        BufferedReader input = null;
        InputStreamReader reader = null;
        try
        {
            //get the contents of the filePath and pass it to scrapData
            fis = new FileInputStream(filePath);
            reader = new InputStreamReader(fis);
            input = new BufferedReader(reader);
            String sr = input.readLine();
            while (sr != null)
            {
                stBuff.append(sr);
                sr = input.readLine();
            }
        }
        catch (Exception ex)
        {
            logger.error("error", ex);
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
            if (input != null)
            {
                input.close();
            }
            if (reader != null)
            {
                reader.close();
            }
        }
        return stBuff.toString().replaceAll("™","&trade;");
    }

    private ItemValuesBean scrapData(String fileContent,
                                     ReaderConfiguration readerConfig,
                                     Logger logger) throws
            ClassNotFoundException,
            SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException,
            InstantiationException
    {
        ItemValuesBean obj = new ItemValuesBean();
        Class cl = ItemValuesBean.class;
        Object iClass = obj;
        //logger.info("\n");
        ArrayList<TagInformation> tagInfo = readerConfig.listOfTagInformation;
        for (int i = 0; i < tagInfo.size(); i++)
        {
            try
            {
                TagInformation selectedTag = tagInfo.get(i);
                Pattern patt = Pattern.compile(selectedTag.regEx1, Pattern.CASE_INSENSITIVE);
                Matcher mach = patt.matcher(fileContent);
                boolean found = mach.find();
                //System.out.println("found: " + found);
                if (found)
                {
                    String[] par = new String[]{StringEscapeUtils.unescapeHtml(
                            mach.group(1).replaceAll("\\s[\\s]*", " ").trim())};
                    Method m1 = cl.getMethod("set" + selectedTag.propertyName,
                            String.class);
                    m1.invoke(iClass, par);
                }
                else if (!isNullOrVoid(selectedTag.regEx2))
                {
                    patt = Pattern.compile(selectedTag.regEx2);
                    mach = patt.matcher(fileContent);
                    if (mach.find())
                    {
                        String[] par = new String[]{StringEscapeUtils.
                                unescapeHtml(
                                mach.group(1).replaceAll(
                                        "\\s[\\s]*", " ").trim())};
                        Method m1 = cl.getMethod("set" +
                                selectedTag.propertyName,
                                String.class);

                        m1.invoke(iClass, par);
                    }
                    else if (!isNullOrVoid(selectedTag.regEx3))
                    {
                        patt = Pattern.compile(selectedTag.regEx3);
                        mach = patt.matcher(fileContent);
                        if (mach.find())
                        {
                            String[] par = new String[]{StringEscapeUtils.
                                    unescapeHtml(
                                    mach.group(1).replaceAll(
                                            "\\s[\\s]*", " ").trim())};
                            Method m1 = cl.getMethod("set" +
                                    selectedTag.propertyName,
                                    String.class);

                            m1.invoke(iClass, par);
                        }
                    }

                }

            }
            catch (Exception exp)
            {
            }
        }
        return (ItemValuesBean) iClass;
    }

    public static boolean validateUrl(String url)
    {
        Pattern pattern = Pattern.compile("((https?|ftp|gopher|telnet|file|notes|ms-help):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\.&]*)");
        Matcher m = pattern.matcher(url);
        return m.matches();
    }

    public void addItemsInDB(ItemValuesBean itemBean)
    {
        DBConnection dbConnect = new DBConnection();
        Connection con = null;
        Statement st = null;
        int count = 0;
        try
        {
            try
            {
                con = dbConnect.getConnection();
            }
            catch (SystemException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            st = con.createStatement();
        }
        catch (SQLException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        ResultSet rs = null;
        PreparedStatement pstmt = null;


        String deleteQuery = "select count(*) from item_inventory where item_id = " + "\"" + itemBean.getItemNo() + "\"" + " and deleted=0";


        try
        {
            rs = st.executeQuery(deleteQuery);
            rs.next();
            count = rs.getInt(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        if (count >= 1)
        {
            String updateQuery = "update item_inventory " +
                    " set " +
                    " NEW_ITEM_DESCRIPTION=" + "\"" + itemBean.getDescription() + "\"" + "," +
                    " NEW_ITEM_UNIT=" + "\"" + itemBean.getUnit() + "\"" + "," +
                    " NEW_ITEM_PRICEUNIT=" + "\"" + itemBean.getItemPriceUnit() + "\"" + "," +
                    " NEW_ITEM_CURRENCY=" + "\"" + itemBean.getCurrency() + "\"" + "," +
                    " NEW_ITEM_VENDOR=" + "\"" + itemBean.getItemVendor() + "\"" + "," +
                    " NEW_ITEM_VENDORMAT=" + "\"" + itemBean.getItemVendorMat() + "\"" + "," +
                    " NEW_ITEM_MANUFACTCODE=" + "\"" + itemBean.getItemManufactCode() + "\"" + "," +
                    " NEW_ITEM_MANUFACTMAT=" + "\"" + itemBean.getItemManufactMat() + "\"" + "," +
                    " NEW_ITEM_MATGROUP=" + "\"" + itemBean.getItemMatGroup() + "\"" + "," +
                    " NEW_ITEM_LONGTEXT_1=" + "\"" + itemBean.getItemLongText1() + "\"" + "," +
                    " item_image=" + "\"" + itemBean.getImage() + "\"" + "," +
                    " item_url=" + "\"" + itemBean.getUrl() + "\"" + "," +
                    " price=" + "\"" + itemBean.getPrice() + "\"" +
                    " where item_id=" + "\"" + itemBean.getItemNo() + "\"" + " and " + "deleted = 0";


			System.out.println(updateQuery);

            try
            {
                st.executeUpdate(updateQuery);
            }
            catch (SQLException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if (count == 0)
        {
            String query = "INSERT into item_inventory(item_id,catalog_id,NEW_ITEM_DESCRIPTION," +
                    "NEW_ITEM_QUANTITY,NEW_ITEM_UNIT,NEW_ITEM_PRICEUNIT," +
                    "NEW_ITEM_CURRENCY,NEW_ITEM_VENDOR,NEW_ITEM_VENDORMAT," +
                    "NEW_ITEM_MANUFACTCODE,NEW_ITEM_MANUFACTMAT,NEW_ITEM_MATGROUP," +
                    "NEW_ITEM_LONGTEXT_1," +
                    "item_image,item_url,approved_id," +
                    "price,deleted,NEW_ITEM_MATNR, " +
                    "NEW_ITEM_LEADTIME, " +
                    "NEW_ITEM_CONTRACT, " +
                    "NEW_ITEM_CONTRACT_ITEM, " +
                    "NEW_ITEM_SERVICE, " +
                    "NEW_ITEM_EXT_QUOTE_ID, " +
                    "NEW_ITEM_EXT_QUOTE_ITEM, " +
                    "NEW_ITEM_EXT_PRODUCT_ID, " +
                    "NEW_ITEM_CUST_FIELD1, " +
                    "NEW_ITEM_CUST_FIELD2, " +
                    "NEW_ITEM_CUST_FIELD3, " +
                    "NEW_ITEM_CUST_FIELD4, " +
                    "NEW_ITEM_CUST_FIELD5, " +
                    "NEW_ITEM_PARENT_ID, " +
                    "NEW_ITEM_ITEM_TYPE, " +
                    "new_item_attachment, " +
                    "new_item_attachment_title, " +
                    "new_item_attachment_purpose, " +
                    "new_item_ext_schema_type, " +
                    "new_item_ext_catagory_id, " +
                    "new_item_ext_catagory, " +
                    "new_item_sld_SYS_name, " +
                    "quote_quantity,green_item)" +
                    "VALUES" +
                    "(\"" +
                    itemBean.getItemNo() + "\"," +
                    "\"" + itemBean.getCatalogId() + "\"," +
                    "\"" + itemBean.getDescription() + "\"" + "," +
                    "\"" + 1000 + "\"" + "," +
                    "\"" + itemBean.getUnit() + "\"," +
                    "\"" + itemBean.getItemPriceUnit() + "\"," +
                    "\"" + itemBean.getCurrency() + "\"," +
                    "\"" + itemBean.getItemVendor() + "\"," +
                    "\"" + itemBean.getItemVendorMat() + "\"," +
                    "\"" + itemBean.getItemManufactCode() + "\"," +
                    "\"" + itemBean.getItemManufactMat() + "\"," +
                    "\"" + itemBean.getItemMatGroup() + "\"," +
                    "\"" + itemBean.getItemLongText1() + "\"," +
                    "\"" + itemBean.getImage() + "\"," +
                    "\"" + itemBean.getUrl() + "\"," +
                    "\"" + 1 + "\"" + "," +
                    "\"" + itemBean.getPrice() + "\"," +
                    "\"" + 0 + "\""
                    + ",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"0\")";


            try
            {
                pstmt = con.prepareStatement(query);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            try
            {
                System.out.println("query:" + query);
                pstmt.execute();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            if(rs != null)
                rs.close();
            if (st != null)
                st.close();
            if (pstmt != null)
                pstmt.close();
            if (con != null)
                con.close();

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


}
