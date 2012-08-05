install python
	http://www.python.org/download/
	http://www.python.org/ftp/python/2.7.2/python-2.7.2.msi
	"C:\Python27;C:\Python27\Scripts" on path

install setuptools
	http://pypi.python.org/packages/2.7/s/setuptools/setuptools-0.6c11.win32-py2.7.exe#md5=57e1e64f6b7c7f1d2eddfc9746bbaf20

install pip
	http://pypi.python.org/pypi/pip#downloads
	setup.py install

install scrapy		
	pip install scrapy		
	
install twisted
	http://twistedmatrix.com/Releases/Twisted/11.0/Twisted-11.0.0.winxp32-py2.7.exe
	
install zope.interface
	pip install zope.interface
	
install pyopenssl
	http://launchpad.net/pyopenssl/main/0.11/+download/pyOpenSSL-0.11.winxp32-py2.7.exe
	
run spider
	scrapy runspider --logfile=cdw.log cdw_spider.py
	
after running the netsol indexing code, use strip_characters.py cdwg.csv to get 
rid of wierd characters