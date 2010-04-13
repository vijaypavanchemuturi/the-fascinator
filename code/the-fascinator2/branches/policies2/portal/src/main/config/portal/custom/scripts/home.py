from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.common import JsonConfigHelper
from java.io import ByteArrayInputStream, ByteArrayOutputStream
from java.util import LinkedHashMap

class Facet:
    def __init__(self, key, value, count):
        self.__name = value[value.rfind("/") + 1:]
        fq = '%s:"%s"' % (key, value)
        self.__facetQuery = URLEncoder.encode(fq, "UTF-8")
        self.__id = md5.new(fq).hexdigest()
        self.__count = count
        self.__subFacets = ArrayList()

    def getId(self):
        return self.__id

    def getName(self):
        return self.__name

    def getCount(self):
        return self.__count

    def getFacetQuery(self):
        return self.__facetQuery

    def addSubFacet(self, facet):
        self.__subFacets.add(facet)

    def getSubFacets(self):
        return self.__subFacets

class FacetList:
    def __init__(self, name, json):
        self.__facetMap = LinkedHashMap()
        self.__facetList = ArrayList()
        entries = json.getList("facet_counts/facet_fields/" + name)
        for i in range(0, len(entries), 2):
            value = entries[i]
            count = entries[i+1]
            if count > 0:
                facet = Facet(name, value, count)
                self.__facetMap.put(value, facet)
                slash = value.rfind("/")
                if slash == -1:
                    self.__facetList.add(facet)
                else:
                    parent = self.getFacet(value[:slash])
                    if parent is not None:
                        parent.addSubFacet(facet)

    def getFacets(self):
        return self.__facetList

    def getFacet(self, name):
        return self.__facetMap.get(name)

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
    
    def getFacetList(self, key):
        return FacetList(name, self.__result)

scriptObject = HomeData()
