from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException, ElementNotVisibleException
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
import os, time, re, csv, traceback, htmlentitydefs, unicodedata, threading, sys, subprocess, urllib
from pprint import pprint

ids_dir = "config\\"

waitSeconds = 30
baseURL = "http://emarket.airgas.com"

# old header

# header = ["#ItemNumber", "NEW_ITEM-DESCRIPTION", "NEW_ITEM-UNIT", "NEW_ITEM-PRICE", "NEW_ITEM-CURRENCY", "NEW_ITEM-VENDORMAT", "NEW_ITEM-MANUFACTMAT", 
#    "NEW_ITEM-LONGTEXT", "NEW_ITEM-URL", "NEW_ITEM-IMAGE", "ITEM-MIN_ORDER_QTY", "ITEM-BREADCRUMB", "NEW_ITEM-MATGROUP"]
#
# row = [i, descr, unit, price, "USD", id, id, longtext, url, image, minquant, breadcrumb, mapping.get(id, '')]
row = [i, "", descr, "", mapping.get(id, ''), unit, price, "", "USD", "", "", "", "", "", "", "", "", url, id, "", id, longtext, image, "", "", "", "", "",
	"", "", "", "", "", "", "", "", "", "", "", "", "", "", minquant, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""]

header = ["#ItemNumber", "NEW_ITEM-VENDOR", "NEW_ITEM-DESCRIPTION", "NEW_ITEM-MATNR", "NEW_ITEM-MATGROUP", "NEW_ITEM-UNIT", "NEW_ITEM-PRICE", 
	"NEW_ITEM-PRICEUNIT", "NEW_ITEM-CURRENCY", "ITEM-TIERED_PRICING", "NEW_ITEM-LEADTIME", "NEW_ITEM-EXT_SCHEMA_TYPE", "NEW_ITEM-EXT_CATEGORY_ID", 
	"NEW_ITEM-EXT_CATEGORY", "NEW_ITEM-SLD_SYS_NAME", "NEW_ITEM-ATTACHMENT_PURPOSE", "NEW_ITEM-EXT_PRODUCT_ID", "NEW_ITEM-QUANTITY", "ITEM-URL",
	"NEW_ITEM-VENDORMAT", "NEW_ITEM-MANUFACTCODE", "NEW_ITEM-MANUFACTMAT", "NEW_ITEM-LONGTEXT", "NEW_ITEM-IMAGE", "NEW_ITEM-SERVICE", "NEW_ITEM-CONTRACT", 
	"NEW_ITEM-CONTRACT_ITEM", "NEW_ITEM-EXT_QUOTE_ID", "NEW_ITEM-EXT_QUOTE_ITEM", "ITEM-QUOTE_QUANTITY", "NEW_ITEM-ATTACHMENT", "NEW_ITEM-ATTACHMENT_TITLE",
	"NEW_ITEM-CUST_FIELD1", "NEW_ITEM-CUST_FIELD2", "NEW_ITEM-CUST_FIELD3", "NEW_ITEM-CUST_FIELD4", "NEW_ITEM-CUST_FIELD5", "ITEM-GREEN_ITEM", "ITEM-BUNDLE_NO",
	"ITEM-BUNDLE_QUANTITY", "ITEM-INSTOCK", "ITEM-QTY_ONHAND", "ITEM-MANUFACTURER_NAME", "ITEM-MIN_ORDER_QTY", "ITEM-MODELNO", "ITEM-UPC", "ITEM-CASE_UPC", 
	"ITEM-CONFIG", "ITEM-BRAND_NAME", "ITEM-LIST_PRICE", "ITEM-COLOR", "ITEM-SIZE", "ITEM-GENDER", "ITEM-RAM", "ITEM-CPU", "ITEM-HDD", "ITEM-SCREEN_SZ",
	"ITEM-SCREEN_REZ", "ITEM-ENERGY_STAR", "ITEM-COMMENTS,DELETE"]

categoryPattern = re.compile('<li class="categoryListSub"><a href="(.*?)">', re.IGNORECASE|re.MULTILINE|re.DOTALL)
subcategoryPattern = re.compile('<SPAN class="categoryList">\s+<A href="(.*?)">', re.IGNORECASE|re.MULTILINE|re.DOTALL)
idPattern = re.compile("<a\s+href='/product/inquiry/(.*?)'>", re.IGNORECASE|re.MULTILINE|re.DOTALL)
pricePattern = re.compile('productDetailOverviewControl__unitPrice\">(<b>)?\$(.*?)(</b>)?<br', re.IGNORECASE|re.MULTILINE|re.DOTALL)
descrPattern = re.compile('<span\s+id=\"ctl00_ContentPlaceHolder1__productInformationControl_shortDescriptionLabel\">(.*?)</span>', re.IGNORECASE|re.MULTILINE|re.DOTALL)
longtextPattern = re.compile('<span\s+id=\"ctl00_ContentPlaceHolder1__productInformationControl_longDescriptionLabel\"><p>(.*?)</p></span>', re.IGNORECASE|re.MULTILINE|re.DOTALL)
imagePattern = re.compile('<img\s+id=\"ctl00_ContentPlaceHolder1__productInformationControl__productImageCtl_productImage\s+src=\"(.*?)\"', re.IGNORECASE|re.MULTILINE|re.DOTALL)
unitPattern = re.compile("Min Purchase Qty<br />[0-9]+ (.*?)</span></td>", re.IGNORECASE|re.MULTILINE|re.DOTALL)
minquantPattern = re.compile("Min Purchase Qty<br />([0-9]+).*?</span></td>", re.IGNORECASE|re.MULTILINE|re.DOTALL)
breadcrumbPattern = re.compile('<span class="breadcrumb">(.*?)</span>', re.IGNORECASE|re.MULTILINE|re.DOTALL)
overviewPattern = re.compile("", re.IGNORECASE|re.MULTILINE|re.DOTALL)


def unescape(text):
    def fixup(m):
        text = m.group(0)
        if text[:2] == "&#":
            # character reference 
            try:
                if text[:3] == "&#x":
                    return unichr(int(text[3:-1], 16))
                else:
                    return unichr(int(text[2:-1]))
            except ValueError:
                pass
        else:
            # named entity
            try:
                text = unichr(htmlentitydefs.name2codepoint[text[1:-1]])
            except KeyError:
                pass
        return text # leave as is
    return re.sub("&#?\w+;", fixup, text)
    

def print_timing(func):
    def wrapper(*arg):
        t1 = time.time()
        res = func(*arg)
        t2 = time.time()
        print '%s took %0.3f s' % (func.func_name, (t2-t1)) 
        return res
    return wrapper


def readIdsFile(idDict, filename):
    print 'reading', filename
    try:   
        idsFile = open(filename)
    except:
        idsFile = open(filename, "w")
        idsFile.close()
        idsFile = open(filename)
    for line in idsFile.readlines():
        line = line.strip()
        if line:
            idDict[line] = 1
    idsFile.close()
    print "starting with", len(idDict), "ids"
   
   
@print_timing
def getIds(category, idDict, browser, idsFile):
    try:
        print 'getIds', browser.current_url
        catID = browser.current_url.split("category=")[1]
        pageNum = 2
        numIds = 0
        while True: # do all pages until done (exception thrown) 
            ids = []
            #elems = browser.find_elements_by_xpath("//a[contains(@href,'/product/inquiry/')]")            
            elems = WebDriverWait(browser, waitSeconds).until(lambda driver : driver.find_elements_by_xpath("//a[contains(@href,'/product/inquiry/')]"))
            for elem in elems:
                url = elem.get_attribute("href")
                id = url.split('/')[-1]
                ids.append(id)

            print ids
            for id in ids:
                if not idDict.has_key(id):
                    idDict[id] = 1
                    idsFile.write(id + "\n")
                    idsFile.flush()
                    numIds = numIds + 1                   

            url = "https://emarket.airgas.com/browse/productlist.aspx?catID=%s&page=%s" % (catID, pageNum)
            browser.get(url)
            if "Server Error" in browser.page_source or "punchout attempt" in browser.page_source:            
                browser = relogin(url, browser)          
            pageNum = pageNum + 1
        print "found", numIds, "new ids on", pageNum, "pages"
    except:
        traceback.print_exc()
        #print "writing problem page"
        #file = open(category + ".html", 'w')
        #file.write(browser.page_source.encode("utf_8"))
        #file.close() 


def relogin(url, browser):
    if True: #while "Server Error" in browser.page_source:
        browser.close()
        time.sleep(waitSeconds)
        browser = webdriver.Firefox()
        print 'getting login url'
        browser.get(login_url)   
        time.sleep(1)
        print 'getting', url
        browser.get(url) 
    return browser


def readMappingFile(filename):
    mapping = {}
    reader = csv.reader(open(filename, 'rb'))
    for row in reader:
        productId = row[0]
        unspsc = row[3]
        mapping[productId] = unspsc
    return mapping
    
    
def extractValue(source, pattern):    
    matches = pattern.findall(source)
    value = ""
    if len(matches) > 0 and matches[0]:
        value = unicodedata.normalize('NFKD', matches[0]).encode('ascii', 'ignore').strip()
        #print value
    return value
    
    
def getFilename(url):
       replaceChars = ":/?.="
       for char in replaceChars:
           url = url.replace(char, "_")
       return url + ".html"
 
 
def getImageFilename(image, id):
       replaceChars = ":/?.="
       for char in replaceChars:
           id = id.replace(char, "_")
       return id + "." + image.split(".")[-1]
           
       
@print_timing
def getProductDetails(i, idDict, browser, dir = ""):

    if len(dir) > 0:
    	dir = dir + "\\"

    print "found", len(idDict.keys()), "products"
    csvWriter = csv.writer(open('%s%s%s.csv' % (dir, site, i), 'wb'))
    csvWriter.writerow(header)
    
    imageIdFile = open('%s%s_image_ids%s.csv' % (dir, site, i), 'wb')
    
    i = 1
    for id in idDict.keys():
        try:
            url = "https://emarket.airgas.com/product/inquiry/" + id
            browser.get(url)
            if "Server Error" in browser.page_source or "punchout attempt" in browser.page_source:
                browser = relogin(url, browser)
                
            try:
                elem = WebDriverWait(browser, 1).until(lambda driver : driver.find_element_by_id("ctl00_ContentPlaceHolder1__productInformationControl__productImageCtl_productImage"))
                image = elem.get_attribute("src").strip()
                
                imageIdFile.write(id + "\n")
                
                #print 'image:', image 
            except:
                image = ""
            source = browser.page_source

            file = open(dir + site + "_pages\\" + getFilename(url), 'w')
            file.write(source.encode("utf_8"))
            file.close()            
        
            prices = pricePattern.findall(source)
            if len(prices) > 0:
                price = prices[0][1].encode("utf_8").replace(',', '')
                price = round(float(price), 2)
            else:
                price = ""
            #print 'price:', price
        
            descr      = extractValue(source, descrPattern)
            longtext   = extractValue(source, longtextPattern)                              
            overview   = extractValue(source, overviewPattern)                              
            minquant   = extractValue(source, minquantPattern)
            unit       = extractValue(source, unitPattern) 
            
            breadcrumbs = breadcrumbPattern.findall(source)
            breadcrumb = ''
            for b in breadcrumbs[3:]:
                breadcrumb += unescape(b)                   

            row = [i, descr, unit, price, "USD", id, id, longtext, url, image, minquant, breadcrumb, mapping.get(id, '')]
            #print row
            csvWriter.writerow(row)
            
            # get the enlarged image, not working right now
            
            #elem = WebDriverWait(browser, waitSeconds).until(lambda driver : driver.find_element_by_xpath("//a[contains(@href,'javascript:ImagePopup()')]"))
            #elem.click()
            
            #pprint(dir(browser))
            
            #browser.openWindow("https://emarket.airgas.com/browse/popup/enlargedImage.aspx", "enlargedImage");
	    #browser.waitForPopUp("enlargedImage", "30000");
	    #browser.selectWindow("enlargedImage");

            #elem = WebDriverWait(browser, waitSeconds).until(lambda driver : driver.find_element_by_xpath("//img[contains(@src,'CachedImages')]"))
            #image = elem.get_attribute("src").strip()
            #browser.selectWindow('null')
            
            # big image url   /CachedImages/0000004/t047_r00301_v6.jpg
            # small           /CachedImages/0000005/t047_r00301_v-2.jpg
            #imageData = urllib.urlopen(image).read()
            #file = open(site  +"_pages/" + getImageFilename(image, id), 'wb')
            #file.write(imageData)
            #file.close()
            
            
            i = i + 1
        #except TimeoutException:
        #    #sometimes there's no image
        #    print "timeout for", url
        except:
            traceback.print_exc()
    print "wrote", i-1, "products"
    browser.close()
            

def getSubcategories(subcategoryList, idDict, browser, idsFile):
    for subcat in subcategoryList:
        subcategory = unescape(subcat)
        print 'subcategory', subcategory
        url = baseURL + subcategory
        browser.get(url)            
        time.sleep(1)
                
        sscategoryList = subcategoryPattern.findall(browser.page_source)
        print 'sscategoryList', sscategoryList

        getIds(subcategory, idDict, browser, idsFile) 
                
        if len(sscategoryList) > 0:
            getSubcategories(sscategoryList, idDict, browser, idsFile)
                     
                    
def getCategories(i, url, categoryList, skipSpideringForIds=False):
    idDict = {}
    idsFilename = "%s%s_ids%s.txt" % (ids_dir, site, i)
    readIdsFile(idDict, idsFilename)
    idsFile = open(idsFilename, "a")

    browser = webdriver.Firefox()
    browser.get(url)    

    if not skipSpideringForIds:
        for cat in categoryList:
            category = unescape(cat)
            print 'category', category

            url = baseURL + category
            browser.get(url)                            
            
            subcategoryList = subcategoryPattern.findall(browser.page_source)
            getSubcategories(subcategoryList, idDict, browser, idsFile)
 
    getProductDetails(i, idDict, browser)
    idsFile.close()


class Spider(threading.Thread):
    def __init__(self, i, categoryList):
        threading.Thread.__init__(self)
        self.i = i
        self.categoryList = categoryList
        
    def run(self):
        getCategories(self.i, login_url, self.categoryList)
           

def spliceCSVfiles(dir):
    csvWriter = csv.writer(open(site + '.csv', 'wb'))
    csvWriter.writerow(header)
        
    itemNo = 1
    for i in range(7):
    
        filename = dir + "\\" + site + '%s.csv' %i
        csvReader = csv.reader(open(filename, 'rb'))
        
        rowNum = 0
        for row in csvReader:
            if rowNum > 0:
                row[0] = itemNo
                csvWriter.writerow(row)
                itemNo = itemNo + 1
            rowNum = rowNum + 1
            

def getMissing(dir):
    spider_skus = {}
    idDict = {}

    for i in range(6):
        reader = csv.reader(open(dir + "\\" + site + str(i) + '.csv', 'rb'))
        for row in reader:
            id = row[5] 
            if not id in['', "NEW_ITEM-VENDORMAT"]:
                spider_skus[id] = row
        
    reader = csv.reader(open(dir + "\\" + ids_dir + username + '.csv', 'rb'))
    for row in reader:
        id = row[0] 
        if not id == "TIMS ID" and not spider_skus.has_key(id):       
            idDict[id] = 1      
    browser = webdriver.Firefox()
    browser.get(login_url)
    getProductDetails(6, idDict, browser, dir)
    
    
if __name__ == "__main__":

    #browser = webdriver.Firefox()
    #browser.get(url)    
    #categoryList = categoryPattern.findall(browser.page_source)
    
    categoryLists = [
        [u'/Safety%20Products/Clothing',
         u'/Safety%20Products/Environmental',
         u'/Safety%20Products/Ergonomics%20%26%20Fall%20Protection',
         u'/Safety%20Products/Head,%20Eye%20%26%20Face%20Protection',],
        
        [u'/Safety%20Products/Fire%20Equipment',
         u'/Safety%20Products/First%20Aid',
         u'/Safety%20Products/Footwear',
         u'/Safety%20Products/Gloves',],
        
        [u'/Safety%20Products/Hearing%20Protection',
         u'/Safety%20Products/Monitors%20%26%20Calibration%20Equipment',
         u'/Safety%20Products/Respiratory%20Protection',
         u'/Safety%20Products/Area%20Protection',],
        
        [u'/Janitorial/Janitorial%20Equipment',
         u'/Other%20Products/Incentive%20Apparel',
         u'/Gas%20Equipment/Specialty%20Gas%20Equipment',        
         u'/Gas%20Equipment/Welding%20Gas%20Equipment',],
        
        [u'/Gas%20Equipment/Gas%20Equipment%20Accessories',
         u'/Welding%20Products/Filler%20Metal',
         u'/Welding%20Products/Misc.%20Welding%20Equipment',],
        
        [u'/Welding%20Products/Welders%20%26%20Accessories',
         u'/Welding%20Products/Welding%20Support%20Equipment',
         u'/Tools%20%26%20Hardware/MRO%20%26%20Plant%20Maintenance']
    ]
   
    
    if len(sys.argv) > 2:
    
        site = sys.argv[1]

        if site == 'airgas':
            login_url = "https://emarket.airgas.com/eMarketPunchin/OCI4_Punchin.aspx?USERNAME=NORNGC3_NA2&PASSWORD=330man_NA2&HOOK_URL=http://www.smartoci.com"
            username = 'NORNGC3_NA2'
        elif site == 'airgasnonmed':
            login_url  = "https://emarket.airgas.com/eMarketPunchin/OCI4_Punchin.aspx?USERNAME=NORNor4_NA3&PASSWORD=330man_NA3&HOOK_URL=http://www.smartoci.com"
            username = 'NORNor4_NA3'            
        else:
            print 'first argument should be "airgas" or "airgasnonmed"'

        mapping = readMappingFile(ids_dir + username + ".csv") 
        processNum = sys.argv[2]
        if len(sys.argv) > 3:
        	print "*** airgas_spider.py - " + site + " complete ***\n"
        	if processNum == "complete":
            		getMissing(sys.argv[3])
            		spliceCSVfiles(sys.argv[3])
        else:

            print "*** airgas_spider.py - " + site + processNum + " ***\n"        
            if not os.path.exists(site + "_pages"):
        	os.makedirs(site + "_pages")

            processNum = int(processNum)
            getCategories(processNum, login_url, categoryLists[processNum], skipSpideringForIds=False)

    else:
        print 'need a site and processNum'
        #i = 0
        #for categoryList in categoryLists:   
        #    spider = Spider(i, categoryList)
        #    spider.start()
        #    time.sleep(30)
        #    i = i + 1
        
    exit()
 
    """
    auditing which did we not get, get them next time
    packaging
    overview tab in longtext
    
    start selenium server, wait a few seconds
    spawn 6 threads, one for each console window.
    wait for them to finish.
    email csv
    
    Our data repository gets recycled every night around 8PM-11PM.  
    Also the third weekend of the month we do updates, etc from Sat 10PM - 9AM the following morning.

    """  