from scrapy.contrib.spiders import CrawlSpider, Rule
from scrapy.contrib.linkextractors.sgml import SgmlLinkExtractor
import os, unicodedata

"""
http://static.cdwg.com/shop/contracts/contractexplorer.aspx?ContractCode=13822&wclss=B
http://static.cdwg.com/shop/search/results.aspx?wclss=B2&ContractCode=13822
http://static.cdwg.com/shop/products/Belkin-FastCAT-crossover-cable-25-ft/2290925.aspx
"""

visitedIds = {}


class MySpider(CrawlSpider):
    name = 'static.cdwg.com'
    allowed_domains = ['static.cdwg.com']
    start_urls = ['http://static.cdwg.com/Eprocurement/xCBL/RoundTripHandoff.aspx?USERNAME=Northrop&PASSWORD=0308f9ded3&HOOK_URL=http://www.smartoci.com']

    rules = (
        # Extract links matching allow and parse them with the spider's method parse_item
        Rule(SgmlLinkExtractor(deny=('email.aspx', 'printable=1'), allow=('/shop/contracts/contractexplorer.aspx', '/shop/search/results.aspx', '/shop/products/')), callback='parse_item', follow=True),
    )

    def getFilename(self, url):
        replaceChars = ":/?.="
        for char in replaceChars:
            url = url.replace(char, "_")
        return url + ".html"        

    def getId(self, url):
        try: 
            return url.split('/')[-1].split('.')[0]
        except:
            return url
    
    def getFolders(self, product_id):
        #md5.new(url).digest()
        
        if not os.path.exists('products'):
            os.mkdir('products')
            
        folder1 = 'products/' + product_id[:2]
        if not os.path.exists(folder1):
            os.mkdir(folder1)
        
        folder2 = folder1 + "/" + product_id[2:4]
        if not os.path.exists(folder2):
            os.mkdir(folder2)  
            
        return folder2
        
    def parse_item(self, response):    
        if '/shop/products/' in response.url:
            id = self.getId(response.url)
            if not id in visitedIds:
                visitedIds[id] = 1
                filename = self.getFilename(response.url)
                folder = self.getFolders(id)
	        #self.log(filename)
	        
	        # this is supposed to strip weird characters, getting errors at the moment
	        #normalized = unicodedata.normalize('NFKD', response.body.encode("windows-1252"))
	        #body = normalized.encode('ascii', 'ignore')
	        
	        body = response.body
                file = open(folder + '/' + filename, 'wb')
                file.write('<url>' + response.url + '</url>\n' + body) 
                file.close()


