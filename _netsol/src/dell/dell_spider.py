from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException, ElementNotVisibleException
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
import os, time, re, csv, traceback
from pprint import pprint

waitSeconds = 20

ids_dir = "config\\"

idPattern = re.compile('ProductName\s+href=\"ProductDetails.aspx\?Hlk=PE&amp;tbn=sh&amp;PID=(.*?)\">', re.IGNORECASE)
pricePattern = re.compile('PRICE\s+\$\s+(.*?)</span>', re.IGNORECASE)
descrPattern = re.compile('lblProdDisplayName>(.*?)</span>', re.IGNORECASE|re.MULTILINE|re.DOTALL)
longtextPattern = re.compile('lblTab1Data\"?>(.*?)</span>', re.IGNORECASE|re.MULTILINE|re.DOTALL)
imagePattern = re.compile('thumbnail.aspx\?mkt=US\&amp;mfgpn=(.*?)\&', re.IGNORECASE|re.MULTILINE|re.DOTALL)

manufNamePattern = re.compile('lblManuDisplayName"?>(.*?)</SPAN>', re.IGNORECASE|re.MULTILINE|re.DOTALL)
manufMatPattern = re.compile('lblManuItemNo"?>(.*?)</SPAN>', re.IGNORECASE|re.MULTILINE|re.DOTALL)
matGroupPattern = re.compile('lblUNSPSC"?>(.*?)</SPAN>', re.IGNORECASE|re.MULTILINE|re.DOTALL)
currencyPattern = re.compile('lblCurrencyCode"?.*?>(.*?)</SPAN>', re.IGNORECASE|re.MULTILINE|re.DOTALL)
versionPattern = re.compile('Version</TD>\s+<TD.*?>(.*?)</TD>', re.IGNORECASE|re.MULTILINE|re.DOTALL)
licenseQtyPattern = re.compile('License Qty</TD>\s+<TD.*?>(.*?)</TD>', re.IGNORECASE|re.MULTILINE|re.DOTALL)

nextLinkPattern = re.compile('<a id=\"(.*?)\".*?>Next</a>', re.IGNORECASE)
    
    
def print_timing(func):
    def wrapper(*arg):
        t1 = time.time()
        res = func(*arg)
        t2 = time.time()
        print '%s took %0.3f s' % (func.func_name, (t2-t1))
        return res
    return wrapper


def readIdsFile():
    try:   
        idsFile = open(ids_dir + "ids.txt")
    except:
        idsFile = open(ids_dir + "ids.txt", "w")
        idsFile.close()
        idsFile = open(ids_dir + "ids.txt")
    for line in idsFile.readlines():
        line = line.strip()
        if line:
            idDict[line] = 1
    idsFile.close()
    print "starting with", len(idDict), "ids"
   
   
@print_timing
def getIds(category):
    try:
        pageNum = 1
        numIds = 0
        lastIds = None
        #while pageNum < 2: # just do the first page
        while True: # do all pages until done (exception thrown) 
            ids = idPattern.findall(browser.page_source)
            print ids
            if ids == lastIds:
                raise Exception("In an infinite loop, break out")
            lastIds = ids
            for id in ids:
                if not idDict.has_key(id):
                    idDict[id] = 1
                    idsFile.write(id + "\n")
                    idsFile.flush()
                    numIds = numIds + 1

            # next button
            """
            # this is a more general version, but not working yet
            nextLink = nextLinkPattern.findall(browser.page_source)[0]
            elem = browser.find_element_by_id(nextLink)
            elem.send_keys(Keys.RETURN)
            """

            elem = browser.find_element_by_id("ctl00_ASAPHolderMain_dlPageTop_ctl10_lnkPage")        
            elem.send_keys(Keys.RETURN)
            time.sleep(waitSeconds)
            pageNum = pageNum + 1
    except:
        traceback.print_exc()
        #print "writing problem page"
        #file = open(category + ".html", 'w')
        #file.write(browser.page_source.encode("utf_8"))
        #file.close() 
    print "found", numIds, "new ids on", pageNum, "pages"


def extractValue(source, pattern, varName='', mappingVars={}):    
    matches = pattern.findall(source)
    value = ""
    if len(matches) > 0 and matches[0]:
        value = matches[0]
    else:
        if mappingVars and mappingVars[varName]:
           value = mappingVars[varName]
    return value
    

def getFilename(url):
       replaceChars = ":/?.="
       for char in replaceChars:
           url = url.replace(char, "_")
       return url + ".html"
       

def relogin(url, browser):
    if True: #while "please login" in browser.page_source:
        browser.close()
        time.sleep(waitSeconds)
        browser = webdriver.Ie()
        print 'getting login url'
        browser.get(login_url)   
        time.sleep(1)
        print 'getting', url
        browser.get(url) 
    return browser
    
    
@print_timing
def getProductDetails(mapping, browser):

    print "found", len(idDict.keys()), "products"
    csvWriter = csv.writer(open('dell.csv', 'wb'))
    csvWriter.writerow(["#ItemNumber", "NEW_ITEM-DESCRIPTION","NEW_ITEM-UNIT", "NEW_ITEM-PRICE", "NEW_ITEM-CURRENCY", "NEW_ITEM-VENDORMAT", "NEW_ITEM-LONGTEXT", "NEW_ITEM-URL", "NEW_ITEM-IMAGE", \
        "NEW_ITEM-CUSTFIELD4", "NEW_ITEM-MANUFACTMAT", "NEW_ITEM-MATGROUP", "NEW_ITEM-CUSTFIELD1", "NEW_ITEM-CUSTFIELD5"])
    
    i = 1
    for id in idDict.keys(): 
        try:
            url = "https://shop.asap.com/Search/ProductDetails.aspx?Hlk=PE&tbn=sh&PID=" + id
            browser.get(url)
            if "please login" in browser.page_source:
                browser = relogin(url, browser)
                
            time.sleep(1)
            source = browser.page_source.encode("utf_8")

            file = open("pages/" + getFilename(url), 'w')
            file.write(source)
            file.close() 
            
            if mapping.has_key(id):
                mappingVars = mapping[id]
            else:
                mappingVars = {}
        
            price      = extractValue(source, pricePattern)
            descr      = extractValue(source, descrPattern)
            longtext   = extractValue(source, longtextPattern)
 
            manufName  = extractValue(source, manufNamePattern, 'manufName', mappingVars)
            manufMat   = extractValue(source, manufMatPattern)
            matGroup   = extractValue(source, matGroupPattern, 'unspsc', mappingVars)
            version    = extractValue(source, versionPattern, 'version', mappingVars)
            licenseQty = extractValue(source, licenseQtyPattern, 'licenseQty', mappingVars)

            matches = currencyPattern.findall(source)
            if len(matches) > 0:
                currency = matches[0].encode("utf_8")
            else:       
                currency = "USD"
                
            images = imagePattern.findall(source)
            if len(images) > 0:
                image = "https://solutions.asap.com/RichProducts/Products/thumbnail.aspx?mkt=US&mfgpn=" + images[0].encode("utf_8") + "&imgType=1&width=80"
            else:       
                image = "" 

            row = [i, descr, "EA", price, currency, id, longtext, url, image, manufName, manufMat, matGroup, version, licenseQty]
            print row
            if descr:
                csvWriter.writerow(row)
                i = i + 1
            else:
                print 'blank description, skipping'
        except:
            traceback.print_exc()
    
   
def readMappingFile():
    mapping = {}
    reader = csv.reader(open(ids_dir + "Northrop Product UNSPSC Mapping_dell.csv", 'rb'))
    for row in reader:
        productId = row[1]
        unspsc = row[2]
        version = row[4].strip()
        if version == "NONE":
            version = ""
        manufName = row[7].strip()
        licenseQty = row[8]
        mapping[productId] = {"unspsc": unspsc, "version": version, "manufName": manufName, "licenseQty": licenseQty}
        #print "productId:", productId, "unspsc:", unspsc, "version:", version, "manufName:", manufName, "licenseQty:", licenseQty
    return mapping
 
 
if __name__ == "__main__":

    # 50 results per page
    #elem = browser.find_element_by_id("ctl00_ASAPHolderMain_ucResultsPerPageTop_lnkTop50")
    #elem.send_keys(Keys.RETURN)
    #time.sleep(2)

    if not os.path.exists("pages"):
        os.makedirs("pages")

    idDict = {}
    readIdsFile()
    idsFile = open(ids_dir + "ids.txt", "a")

    browser = webdriver.Ie()
    browser.get("https://b2b.asap.com:443/invoke/AsapOCI.PunchOut:asapOCIPunchOut?USERNAME=OC00000145378&PASSWORD=OciAsap2004&HOOK_URL=http://www.smartoci.com")  
    
    """
    print "all results search, stops showing next link after 700 results"
    elem = browser.find_element_by_id("ctl00_ASAPHolderMain_tdSearch")
    elem.send_keys(Keys.RETURN)
    time.sleep(waitSeconds)
    getIds(None)
    """

    print "cycle through all brands"
    # this is entering an infinite loop, when page doesn't load fast enough?
    for categoryPat, num in [("ctl00_ASAPHolderMain_ucEndecaLeftpane_rpcTopBrands_ctl%s_lnkNavItem", 639)]:
        for i in range(num):
            try:
                browser.get("https://shop.asap.com/Search/AdvancedSearch.aspx?tbn=sh")
                #elem = WebDriverWait(browser, waitSeconds).until(lambda driver : driver.find_element_by_id("ctl00_ASAPHolderMain_tdSearch"))                
                elem = browser.find_element_by_id("ctl00_ASAPHolderMain_tdSearch")
                elem.send_keys(Keys.RETURN)
                time.sleep(waitSeconds) 
   
                # More... link
                #elem = WebDriverWait(browser, waitSeconds).until(lambda driver : driver.find_element_by_id("ctl00_ASAPHolderMain_ucEndecaLeftpane_rpcTopBrands_ctl05_lnkNavItem"))                                
                elem = browser.find_element_by_id("ctl00_ASAPHolderMain_ucEndecaLeftpane_rpcTopBrands_ctl05_lnkNavItem")
                elem.send_keys(Keys.RETURN)
                time.sleep(waitSeconds)     

                if i < 10:
                    i = "0%s" % (i,)
                category = categoryPat % (i,)                
                print category
                
                #elem = WebDriverWait(browser, waitSeconds).until(lambda driver : driver.find_element_by_id(category))
                elem = browser.find_element_by_id(category)
                elem.send_keys(Keys.RETURN)
                time.sleep(waitSeconds)
                getIds(category)
            except:
                traceback.print_exc()

    """
    print "1000 products for this section"
    for categoryPat, num in [("ctl00_ASAPHolderMain_ucEndecaLeftpane_rpcPrice_ctl%s_lnkNavItem", 6),
                             ("ctl00_ASAPHolderMain_ucEndecaLeftpane_rpcTopBrands_ctl%s_lnkNavItem", 6),
                             ("ctl00_ASAPHolderMain_ucEndecaLeftpane_rpcCategories_ctl%s_lnkNavItem", 10),
                             ("ctl00_ASAPHolderMain_ucEndecaLeftpane_rpcAttributes_ctl%s_lnkNavItem", 12)]:
        for i in range(num):
            try:
                browser.get("https://shop.asap.com/Search/AdvancedSearch.aspx?tbn=sh")
                elem = browser.find_element_by_id("ctl00_ASAPHolderMain_tdSearch")
                elem.send_keys(Keys.RETURN)
                time.sleep(waitSeconds) 

                if i < 10:
                    i = "0%s" % (i,)
                category = categoryPat % (i,)                
                print category
                elem = browser.find_element_by_id(category)
                elem.send_keys(Keys.RETURN)
                time.sleep(waitSeconds)
                getIds(category)
            except:
                traceback.print_exc()
    """
    
    mapping = readMappingFile()
    getProductDetails(mapping, browser) 
    idsFile.close()
