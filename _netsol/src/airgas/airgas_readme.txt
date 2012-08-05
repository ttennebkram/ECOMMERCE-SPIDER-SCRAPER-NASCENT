install python
	http://www.python.org/download/
	http://www.python.org/ftp/python/2.7.2/python-2.7.2.msi
	"C:\Python27;C:\Python27\Scripts" on path

	install setuptools
		http://pypi.python.org/packages/2.7/s/setuptools/setuptools-0.6c11.win32-py2.7.exe#md5=57e1e64f6b7c7f1d2eddfc9746bbaf20

	install pip
		http://pypi.python.org/pypi/pip#downloads
		setup.py install

	pip install selenium

download selenium server
http://selenium.googlecode.com/files/selenium-server-standalone-2.9.0.jar

in IE, en/disable Protected Mode for all zones.
	Tools/Internet Options/Security/Internet/Enable Protected Mode
	
run selenium server
java -jar selenium-server-standalone-2.9.0.jar

expects 2 files to exist
NORNOR4_NA3.csv
NORNGC3_NA2.csv

and two subfolders
airgas_pages
airgasnonmed_pages

run spider in separate console windows
airgas_spider.py airgas|airgasnonmed 0
airgas_spider.py airgas|airgasnonmed 1
airgas_spider.py airgas|airgasnonmed 2
airgas_spider.py airgas|airgasnonmed 3
airgas_spider.py airgas|airgasnonmed 4
airgas_spider.py airgas|airgasnonmed 5

when they've finished run
airgas_spider.py airgas|airgasnonmed missing

