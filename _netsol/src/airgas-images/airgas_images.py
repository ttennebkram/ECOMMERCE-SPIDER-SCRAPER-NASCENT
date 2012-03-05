from selenium import webdriver
from selenium.webdriver.support.ui import WebDriverWait

import os, sys, time, csv, traceback

def makePageDict(file_name):
	file = open(file_name, "rb")
	reader = csv.reader(file)
	
	page_dict = {}
	for row in reader:
		if len(row) > 0:
			id = row[0]
			page_dict[id] = ""
			
	return page_dict
	
def generatePageUrls(page_dict):
	page_urls = []
	
	for id in page_dict:
		page_urls.append("https://emarket.airgas.com/product/inquiry/" + id)
		
	return page_urls

def login(login_url, cache_dir):
	full_cache_dir = os.path.abspath(cache_dir)
	
	fp = webdriver.FirefoxProfile()
	fp.set_preference("browser.cache.memory.enable", False)
	fp.set_preference("browser.cache.disk.capacity", 5000000)
	fp.set_preference("browser.cache.disk.parent_directory", full_cache_dir)
	fp.set_preference("browser.cache.disk_cache_ssl", True)
	
	browser = webdriver.Firefox(firefox_profile=fp)
	browser.get(login_url)
	
	return browser

def relogin(login_url, cache_dir, browser):
	browser.close()
	time.sleep(10)
	
	return login(login_url, cache_dir)
	
def getImages(login_url, cache_dir, page_dict, page_urls):
	browser = login(login_url, cache_dir)

	i = 0
	t = 0
	image_url = ""	
	for url in page_urls:
		try:
			if t > 50:
				print "\n*** logging in again ***\n"
				browser = relogin(login_url, cache_dir, browser)
				t = 0
				
			browser.get(url)
			if "Server Error" in browser.page_source or "punchout attempt" in browser.page_source:
				browser = relogin(login_url, cache_dir, browser)
				browser.get(url)
		
			elem = WebDriverWait(browser, 30).until(lambda driver : driver.find_element_by_xpath("//img[contains(@id,'productImageCtl_productImage')]"))
			image_url = elem.get_attribute("src").strip()
			if (len(image_url) > 0):
			      	browser.get(image_url)
			      	print "[" + str(i) + "] fetched " + image_url

			i = i + 1
			t = t + 1
		except:
	      		traceback.print_exc()
	      		
		page_id = url.split('/')[-1]
		page_dict[page_id] = image_url
	
	time.sleep(10)
	browser.close()

if __name__ == "__main__":
	
	site = sys.argv[1]
	pnum = sys.argv[2]
	ids_dir = sys.argv[3]
	print "*** airgas_images.py - " + site + pnum + " ***\n"
	
	id_file = ids_dir + "\\" + site + "_image_ids" + pnum + ".csv"
	cache_dir = site + "_image_cache" + pnum + "\\"
	results_file = site + "_image_dict" + pnum + ".csv"

	page_dict = makePageDict(id_file)
	page_urls = generatePageUrls(page_dict)

	login_url = ""
	if site == 'airgas':
		login_url = "https://emarket.airgas.com/eMarketPunchin/OCI4_Punchin.aspx?USERNAME=NORNGC3_NA2&PASSWORD=330man_NA2&HOOK_URL=http://www.smartoci.com"
	elif site == 'airgasnonmed':
		login_url  = "https://emarket.airgas.com/eMarketPunchin/OCI4_Punchin.aspx?USERNAME=NORNor4_NA3&PASSWORD=330man_NA3&HOOK_URL=http://www.smartoci.com"
	
	getImages(login_url, cache_dir, page_dict, page_urls)
	
	writer = csv.writer(open(results_file, "wb"))
	for page_id in page_dict:
		image_url = page_dict[page_id]
		writer.writerow([ page_id, image_url ])
