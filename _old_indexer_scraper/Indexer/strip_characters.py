import codecs, unicodedata, sys

filename = sys.argv[1]

Reader = codecs.getreader("windows-1252") #for scrapy, for airgas use utf-8
file = open(filename)
data = Reader(file).read()
data = data.encode('ascii', 'ignore')

file = open(filename +  ".stripped", 'w')
file.write(data)
file.close()
