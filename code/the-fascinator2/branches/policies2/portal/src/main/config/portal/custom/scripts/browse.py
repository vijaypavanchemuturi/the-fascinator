import array, md5, os

from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.api.storage import Payload, PayloadType
from au.edu.usq.fascinator.common import JsonConfig, JsonConfigHelper
from au.edu.usq.fascinator.common.storage.impl import GenericPayload
from au.edu.usq.fascinator.portal import Pagination, Portal

from java.io import ByteArrayInputStream, ByteArrayOutputStream
from java.net import URLDecoder, URLEncoder
from java.util import ArrayList, LinkedHashMap

class SearchData:
    def __init__(self):
        self.__portal = Services.portalManager.get(portalId)
        self.__result = JsonConfigHelper()
        self.__pageNum = 1
        self.__selected = ArrayList()
        self.__query = ""
        self.__searchType = "full_text"
        self.__search()
    
    def __search(self):
        recordsPerPage = self.__portal.recordsPerPage
        
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
        
        #fq = formData.getValues("fq")
        uri = request.getAttribute("RequestURI")
        self.__pageNum, fq, self.__fqParts = self.__parseUri(uri[len(portalPath):])
        savedfq = sessionState.get("fq")
        limits = []
        if savedfq:
            limits.extend(savedfq)
        if fq:
            limits.extend(fq)
            sessionState.set("fq", limits)
            for q in fq:
                req.addParam("fq", URLDecoder.decode(q, "UTF-8"))
        
        portalQuery = self.__portal.query
        if portalQuery:
            req.addParam("fq", portalQuery)
        
        if req.getParams("fq"):
            self.__selected = ArrayList(req.getParams("fq"))
        
        req.addParam("fq", 'item_type:"object"')
        req.setParam("start", str((self.__pageNum - 1) * recordsPerPage))
        
        print " * search.py:", req.toString(), self.__pageNum
        
        out = ByteArrayOutputStream()
        Services.indexer.search(req, out)
        self.__result = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
        if self.__result is not None:
            self.__paging = Pagination(self.__pageNum,
                                       int(self.__result.get("response/numFound")),
                                       self.__portal.recordsPerPage)
    def getSearchType(self):
        return self.__searchType
    
    def getQuery(self):
        return self.__query
    
    def getQueryTime(self):
        return int(self.__result.get("responseHeader/QTime")) / 1000.0;
    
    def getPaging(self):
        return self.__paging
    
    def getResult(self):
        return self.__result
    
    def getFacetName(self, key):
        return self.__portal.facetFields.get(key)
    
    def getFacetCounts(self, key):
        values = LinkedHashMap()
        valueList = self.__result.getList("facet_counts/facet_fields/%s" % key)
        for i in range(0,len(valueList),2):
            name = valueList[i]
            count = valueList[i+1]
            if count > 0:
                values.put(name, count)
        return values
    
    def hasSelectedFacets(self):
        return self.__selected is not None and self.__selected.size() > 0
    
    def getSelectedFacets(self):
        return self.__selected
    
    def isPortalQueryFacet(self, fq):
        return fq == self.__portal.query
    
    def isSelected(self, fq):
        return fq in self.__selected
    
    def getSelectedFacetIds(self):
        return [md5.new(fq).hexdigest() for fq in self.__selected]
    
    def getFileName(self, path):
        return os.path.split(path)[1]
    
    def getFacetQuery(self, name, value):
        return '%s:"%s"' % (name, value)
    
    def getFacetQueryUri(self, name, value):
        return "%s/%s" % (name, value)
    
    def isImage(self, format):
        return format.startswith("image/")
    
    def getThumbnail(self, oid):
        ext = os.path.splitext(oid)[1]
        url = oid[oid.rfind("/")+1:-len(ext)] + ".thumb.jpg"
        return url
    
    def getPageQuery(self, page):
        prefix = ""
        if self.__fqParts:
            prefix = "/" + "/".join(self.__fqParts)
        suffix = ""
        if page > 1:
            suffix = "/page/%s" % page
        return prefix + suffix
    
    def getLimitQueryWith(self, fq):
        limits = ArrayList(self.__fqParts)
        limits.add("category/" + fq)
        return "/".join(limits)
    
    def getLimitQueryWithout(self, fq):
        limits = ArrayList(self.__fqParts)
        limits.remove("category/" + fq)
        if limits.isEmpty():
            return ""
        return "/".join(limits)
    
    def getFacetValue(self, facetValue):
        return facetValue.split("/")[-1]
    
    def getFacetIndent(self, facetValue):
        return (len(facetValue.split("/")) - 1) * 15

    def __parseUri(self, uri):
        page = 1
        fq = []
        fqParts = []
        if uri != "":
            parts = uri.split("/")
            partType = None
            facetKey = None
            facetValues = None
            for part in parts:
                if partType == "page":
                    facetKey = None
                    facetValue = None
                    page = int(part)
                elif partType == "category":
                    partType = "category-value"
                    facetValues = None
                    facetKey = part
                elif partType == "category-value":
                    if facetValues is None:
                        facetValues = []
                    if part in ["page", "category"]:
                        partType = part
                        facetQuery = '%s:"%s"' % (facetKey, "/".join(facetValues))
                        fq.append(facetQuery)
                        fqParts.append("category/%s/%s" % (facetKey, "/".join(facetValues)))
                        facetKey = None
                        facetValues = None
                    else:
                        facetValues.append(URLDecoder.decode(part))
                else:
                    partType = part
            if partType == "category-value":
                facetQuery = '%s:"%s"' % (facetKey, "/".join(facetValues))
                fq.append(facetQuery)
                fqParts.append("category/%s/%s" % (facetKey, "/".join(facetValues)))
        return page, fq, fqParts

scriptObject = SearchData()
