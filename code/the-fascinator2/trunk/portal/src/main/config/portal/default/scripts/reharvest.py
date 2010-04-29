from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.common import JsonConfigHelper

from java.io import ByteArrayInputStream, ByteArrayOutputStream
from java.util import HashSet

class Reharvest:
    def __init__(self):
        print "formData=%s" % formData
        func = formData.get("func")
        result = "{}"
        resultType = "text/plain; charset=UTF-8"
        oid = formData.get("oid")
        portalId = formData.get("portalId")
        portalManager = Services.getPortalManager()
        if oid:
            print "Reharvesting single object: %s" % oid
            portalManager.reharvest(oid)
            result = '{ status: "ok" }'
        elif portalId:
            portal = portalManager.get(portalId)
            print " Reharvesting portal: %s" % portal.getName()
            indexer = Services.getIndexer()
            # TODO security filter
            # TODO this should loop through the whole portal,
            #      not just the first page of results
            if portal.getQuery() == "":
                searchRequest = SearchRequest("item_type:object")
            else:
                searchRequest = SearchRequest(portal.getQuery())
            result = ByteArrayOutputStream();
            Services.getIndexer().search(searchRequest, result)
            json = JsonConfigHelper(ByteArrayInputStream(result.toByteArray()))
            objectIds = HashSet()
            for doc in json.getJsonList("response/docs"):
                objectIds.add(doc.get("id"))
            if not objectIds.isEmpty():
                portalManager.reharvest(objectIds)
            result = '{ status: "ok" }'
        else:
            result = '{ status: "failed" }'
        writer = response.getPrintWriter(resultType)
        writer.println(result)
        writer.close()

scriptObject = Reharvest()
