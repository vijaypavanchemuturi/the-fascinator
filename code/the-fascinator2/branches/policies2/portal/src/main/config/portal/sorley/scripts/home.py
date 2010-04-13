import array, md5, os

from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.api.storage import Payload, PayloadType
from au.edu.usq.fascinator.common import JsonConfig, JsonConfigHelper
from au.edu.usq.fascinator.common.storage.impl import GenericPayload
from au.edu.usq.fascinator.portal import Pagination, Portal

from java.io import ByteArrayInputStream, ByteArrayOutputStream
from java.net import URLDecoder, URLEncoder
from java.util import LinkedHashMap


class SearchData:
    def __init__(self):
        self.__portal = Services.portalManager.get(portalId)
        self.__result = JsonConfigHelper()
        self.__query = ""
        self.__searchType = "full_text"
        self.__search()
    
    def __search(self):
        recordsPerPage = self.__portal.recordsPerPage
        
        uri = URLDecoder.decode(request.getAttribute("RequestURI"))
        searchType = formData.get("searchType")
        if searchType != "" or searchType is not None:
            self.__searchType = searchType
        else:
            self.__searchType = "full_text"
        
        if uri != portalPath:
            query = uri[len(portalPath):]
        if query is None or query == "":
            query = formData.get("query")
        if query is None or query == "":
            query = "*:*"
             
        if query == "*:*":
            self.__query = ""
        else:
            self.__query = query
            query = "%s:%s" % (searchType,query)
        
        req = SearchRequest(query)
        req.setParam("facet", "true")
        req.setParam("rows", str(recordsPerPage))
        req.setParam("facet.field", self.__portal.facetFieldList)
        req.setParam("facet.sort", str(self.__portal.facetSort).lower())
        req.setParam("facet.limit", str(self.__portal.facetCount))
        req.setParam("sort", "title_sort asc")
        req.setParam("fq", 'item_type:"object"')
        
        portalQuery = self.__portal.query
        if portalQuery:
            req.addParam("fq", portalQuery)
        
        print " * search.py:", req.toString()
        
        out = ByteArrayOutputStream()
        Services.indexer.search(req, out)
        self.__result = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
    
    def getResult(self):
        return self.__result
    
scriptObject = SearchData()
