from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.common import JsonConfigHelper
from java.io import ByteArrayInputStream, ByteArrayOutputStream

class SearchData:
    def __init__(self):
        self.__result = JsonConfigHelper()
        self.__portal = Services.getPortalManager().get(portalId)
        self.__search()

    def __search(self):
        query = formData.get("query")
        if query is None or query == "":
            query = "*:*"
        print "Searching for", query
        req = SearchRequest(query)
        req.setParam("facet", ["true"])
        req.setParam("fq", ['item_type:"object"'])
        req.setParam("rows", [str(self.__portal.recordsPerPage)])
        out = ByteArrayOutputStream()
        indexer = Services.getIndexer()
        indexer.search(req, out)
        self.__result = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))

    def getResult(self):
        return self.__result

scriptObject = SearchData()
