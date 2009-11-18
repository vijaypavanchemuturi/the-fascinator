from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.common import JsonConfigHelper
from java.io import ByteArrayInputStream, ByteArrayOutputStream

class AutocompleteData:
    def __init__(self):
        self.result = JsonConfigHelper()
        self.__search()
    
    def __search(self):
        query = formData.get("q")
        if query is None or query == "":
            query = "*:*"
        else:
            query = "dc_title:%s*" % query
        
        req = SearchRequest(query)
        req.setParam("fl", "dc_title")
        limit = formData.get("limit")
        if limit is not None:
            limit = "10"
        req.setParam("rows", limit)
        
        out = ByteArrayOutputStream()
        indexer = Services.getIndexer()
        indexer.search(req, out)
        self.result = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
    
    def getResult(self):
        return self.result

scriptObject = AutocompleteData()
