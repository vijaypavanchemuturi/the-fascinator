from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.common import JsonConfigHelper
from java.io import ByteArrayInputStream, ByteArrayOutputStream
from java.net import URLEncoder
from java.util import LinkedHashMap

class HomeData:
    def __init__(self):
        sessionState.remove("fq")
        action = formData.get("verb")
        if action == "delete-portal":
            portalName = formData.get("value")
            print " * home.py: delete portal %s" % portalName
            Services.portalManager.remove(portalName)
        self.__portal = Services.portalManager.get(portalId)
        self.__result = JsonConfigHelper()
        self.__search()
    
    def __search(self):
        indexer = Services.getIndexer()
        
        req = SearchRequest("*:*")
        req.setParam("fq", 'item_type:"object"')
        req.setParam("rows", "0")
        req.setParam("facet", "true")
        req.setParam("facet.sort", "false")
        req.setParam("facet.field", ["f_title_group", "f_usq_document_policy_type"])
        out = ByteArrayOutputStream()
        indexer.search(req, out)
        self.__result = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
    
    def getTotal(self):
        return self.__result.get("response/numFound")
    
    def getFacetName(self, key):
        return self.__portal.facetFields.get(key)
    
    def getFacetCounts(self, key):
        values = LinkedHashMap()
        valueList = self.__result.getList("facet_counts/facet_fields/%s" % key)
        for i in range(0,len(valueList),2):
            name = valueList[i]
            count = valueList[i+1]
            if count > 0:
                values[name] = count
        return values
    
    def getFacetQuery(self, key, value):
        return '%s:"%s"' % (key, URLEncoder.encode(value, "UTF-8"))
    
    def getFacetValue(self, facetValue):
        return facetValue.split("/")[-1]
    
    def getFacetIndent(self, facetValue):
        return (len(facetValue.split("/")) - 1) * 15;

scriptObject = HomeData()
