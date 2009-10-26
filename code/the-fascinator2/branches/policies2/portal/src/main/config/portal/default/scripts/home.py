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
	self.__total = 0
        self.__latest = JsonConfigHelper()
        self.__search()
    
    def __search(self):
        indexer = Services.getIndexer()

        req = SearchRequest("d_usq_document_effective_date:[NOW-1MONTH TO *]")
        req.setParam("fq", 'item_type:"object"')
        req.setParam("rows", "10")
	req.setParam("sort", "d_usq_document_effective_date asc, title_sort asc");
        out = ByteArrayOutputStream()
        indexer.search(req, out)
        self.__latest = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))

	req = SearchRequest("*:*")
        req.setParam("fq", 'item_type:"object"')
        req.setParam("rows", "0")
        out = ByteArrayOutputStream()
        indexer.search(req, out)
        self.__total = JsonConfigHelper(ByteArrayInputStream(out.toByteArray())).get("response/numFound")
    
    def getLatest(self):
        return self.__latest.getList("response/docs")

    def getTotal(self):
        return self.__total

scriptObject = HomeData()
