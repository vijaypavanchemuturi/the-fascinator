import os
from jarray import array
from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.common import JsonConfigHelper
from java.io import ByteArrayInputStream, ByteArrayOutputStream
from java.lang import String
from java.util import ArrayList, HashMap

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

        # setup facets
        action = formData.get("action")
        currentfq = sessionState.getObject("fq")
        if currentfq is not None:
            req.setParam("fq", currentfq)
        if action == "add_fq":
            fq = formData.get("fq")
            print " ***  adding facet query %s" % fq
            req.addParam("fq", fq)
        elif action == "del_fq":
            pass
        elif action == "clear_fq":
            req.setParam("fq", "")
        sessionState.setObject("fq", req.getParam("fq"))
        req.addParam("fq", 'item_type:"object"')

        req.setParam("facet", ["true"])
        req.setParam("rows", [str(self.__portal.recordsPerPage)])
        req.setParam("facet.field", array(self.__portal.facetFieldList, String))
        out = ByteArrayOutputStream()
        indexer = Services.getIndexer()
        indexer.search(req, out)
        self.__result = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))

    def getResult(self):
        return self.__result

    def getFacetName(self, key):
        return self.__portal.facetFields.get(key)

    def getFacetCounts(self, key):
        values = HashMap()
        valueList = self.__result.getList("facet_counts/facet_fields/%s" % key)
        for i in range(0,len(valueList),2):
            name = valueList[i]
            count = valueList[i+1]
            if count > 0:
                values.put(name, count)
        return values

    def getFileName(self, path):
        return os.path.split(path)[1]

scriptObject = SearchData()
