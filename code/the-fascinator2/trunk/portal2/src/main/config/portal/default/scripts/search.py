import os
from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.common import JsonConfigHelper
from java.io import ByteArrayInputStream, ByteArrayOutputStream
from java.util import HashMap

class SearchData:
    def __init__(self):
        self.__result = JsonConfigHelper()
        self.__portal = Services.getPortalManager().get(portalId)
        self.__search()

    def __search(self):
        query = formData.get("query")
        if query is None or query == "":
            query = "*:*"
        req = SearchRequest(query)
        req.setParam("facet", "true")
        req.setParam("rows", str(self.__portal.recordsPerPage))
        req.setParam("facet.field", self.__portal.facetFieldList)

        # setup facets
        action = formData.get("action")
        fq = sessionState.get("fq")
        if fq is not None:
            req.setParam("fq", fq)
        if action == "add_fq":
            name = formData.get("name")
            value = formData.get("value")
            req.addParam("fq", value)
        elif action == "remove_fq":
            value = formData.get("value")
            req.removeParam("fq", value)
        elif action == "clear_fq":
            req.removeParam("fq")
        req.addParam("fq", 'item_type:"object"')
        self.__selected = req.getParams("fq")
        sessionState.set("fq", self.__selected)
        print " * search.py:", req.toString()
        
        out = ByteArrayOutputStream()
        Services.indexer.search(req, out)
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

    def hasSelectedFacets(self):
        return self.__selected.size() > 1

    def isSelected(self, fq):
        return fq in self.__selected

    def getFileName(self, path):
        return os.path.split(path)[1]

    def getFacetQuery(self, name, value):
        return '%s:"%s"' % (name, value)

scriptObject = SearchData()
