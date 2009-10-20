from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.common import JsonConfigHelper
from java.io import ByteArrayInputStream, ByteArrayOutputStream

class HomeData:
    def __init__(self):
        action = formData.get("verb")
        if action == "delete-portal":
            portalName = formData.get("value")
            print " * home.py: delete portal %s" % portalName
            Services.portalManager.remove(portalName)
        self.__latest = JsonConfigHelper()
        self.__search()
    
    def __search(self):
        req = SearchRequest("d_usq_document_effective_date:[NOW-1MONTH TO *]")
        req.setParam("fq", 'item_type:"object"')
        req.setParam("rows", "10")
        
        out = ByteArrayOutputStream()
        indexer = Services.getIndexer()
        indexer.search(req, out)
        self.__latest = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
    
    def getLatest(self):
        return self.__latest.getList("response/docs")

scriptObject = HomeData()
