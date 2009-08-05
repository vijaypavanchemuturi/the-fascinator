from java.net import URLDecoder

class DetailData:
    def __init__(self):
        self.__storage = Services.storage
        uri = request.getAttribute("RequestURI")
        basePath = portalId + "/" + pageName
        self.__oid = URLDecoder.decode(uri[len(basePath)+1:])

    def getObject(self):
        print " *** getting %s" % self.__oid
        return self.__storage.getObject(self.__oid)

scriptObject = DetailData()
