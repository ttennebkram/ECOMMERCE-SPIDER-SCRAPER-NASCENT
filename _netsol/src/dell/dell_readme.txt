install python
	http://www.python.org/download/
	http://www.python.org/ftp/python/2.7.2/python-2.7.2.msi
	"C:\Python27;C:\Python27\Scripts" on path

install setuptools
	http://pypi.python.org/packages/2.7/s/setuptools/setuptools-0.6c11.win32-py2.7.exe#md5=57e1e64f6b7c7f1d2eddfc9746bbaf20

install pip
	http://pypi.python.org/pypi/pip#downloads
	setup.py install

install selenium
	pip install selenium

download selenium server
	http://selenium.googlecode.com/files/selenium-server-standalone-2.9.0.jar

in IE, en/disable Protected Mode for all zones.
	Tools/Internet Options/Security/Internet/Enable Protected Mode
	
run selenium server
	java -jar selenium-server-standalone-2.9.0.jar

run spider
	dell_spider.py

There are two phases to the spidering, collecting product ids, and then scraping the data out of them.  
There were problems in the first part (infinite loops, pages not loading completely before the spider tried
to get the product ids, various errors like "unable to find element", or "element is not displayed".  
So I wrote the ids found in a file (ids.txt) and read them in on startup.  This way the problematic parts
can be done piecemeal, and the spider keeps track of everything found without having to get them
in one run.  The second scraping phase is faster and less error prone, and can be done just on the ids.txt file.