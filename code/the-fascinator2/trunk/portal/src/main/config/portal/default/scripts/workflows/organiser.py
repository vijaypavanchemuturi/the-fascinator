from __main__ import Services, formData

import md5

from au.edu.usq.fascinator.common import JsonConfigHelper

from java.lang import Exception
from java.util import ArrayList, HashMap

class OrganiserData:
    def __init__(self):
        print "formData: %s" % formData
        self.__oid = formData.get("oid")
        result = None
        try:
            # get the package manifest
            object = Services.getStorage().getObject(self.__oid)
            sourceId = object.getSourceId()
            payload = object.getPayload(sourceId)
            self.__manifest = JsonConfigHelper(payload.open())
            payload.close()
            object.close()
            # check if we need to do processing
            func = formData.get("func")
            if func == "get-item-props":
                result = self.__getItemProps()
            elif func == "get-rvt-manifest":
                result = self.__getRvtManifest(self.getManifest())
        except Exception, e:
            log.error("Failed to load manifest", e);
            result = '{ status: "error", message: "%s" }' % str(e)
        if result is not None:
            writer = response.getPrintWriter("application/json; charset=UTF-8")
            writer.println(result)
            writer.close()
    
    def getManifest(self):
        return self.__manifest.getJsonMap("manifest")
    
    def getFormData(self, field):
        return formData.get(field, "")
    
    def __getItemProps(self, itemId):
        itemId = formData.get("itemId")
        props = JsonConfigHelper()
        props.set("title", "")
        return props.toString()
    
    def __getRvtManifest(self, manifest):
        rvtMap = HashMap()
        rvtMap.put("toc", self.__getRvtNodes(manifest))
        rvtManifest = JsonConfigHelper(rvtMap)
        return rvtManifest.toString()
    
    def __getRvtNodes(self, manifest):
        rvtNodes = ArrayList()
        #print "manifest=%s" % manifest
        for key in manifest.keySet():
            node = manifest.get(key)
            rvtNode = HashMap()
            rvtNode.put("visible", "true")
            rvtNode.put("relPath", node.get("id"))
            rvtNode.put("title", node.get("title"))
            rvtNode.put("children", self.__getRvtNodes(node.getJsonMap("children")))
            rvtNodes.add(rvtNode)
        return rvtNodes

if __name__ == "__main__":
    scriptObject = OrganiserData()
