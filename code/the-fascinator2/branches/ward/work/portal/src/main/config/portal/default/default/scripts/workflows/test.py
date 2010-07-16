from __main__ import Services, formData

from au.edu.usq.fascinator.common import JsonConfigHelper

from org.apache.commons.lang import StringEscapeUtils
from java.lang import Exception as JavaException, Boolean, String
from java.util import ArrayList, HashMap
from java.io import InputStreamReader, ByteArrayInputStream, StringWriter
from org.apache.commons.io import IOUtils

from json import read as jsonReader, write as jsonWriter


class Test(object):
    def __init__(self):
        print "formData: %s" % formData
        self.__oid = formData.get("oid")
        result = None
        try:
            # get the package manifest
            obj = Services.getStorage().getObject(self.__oid)
            sourceId = obj.getSourceId()
            payload = obj.getPayload(sourceId)
            writer = StringWriter()
            IOUtils.copy(payload.open(), writer)
            self.__tfpackage = jsonReader(writer.toString())
            payload.close()
            obj.close()

            print "**** test.py"
            print self.__oid
            print self.__tfpackage
            
            # check if we need to do processing
            func = formData.get("func")
            if func == "get-rvt-manifest":
                result = self.__getRvtManifest(self.getManifest())
        except Exception, e:
            log.error("Failed to load manifest", e);
            result = '{ status: "error", message: "%s" }' % str(e)
        if result is not None:
            writer = response.getPrintWriter("application/json; charset=UTF-8")
            writer.println(result)
            writer.close()

    def getManifest(self):
        return self.__tfpackage.get("manifest", {})

    def getFormData(self, field):
        return StringEscapeUtils.escapeHtml(formData.get(field, ""))

    def getMeta(self, metaName):
        return StringEscapeUtils.escapeHtml(formData.get(metaName, self.__tfpackage.get(metaName)))

    def getJsonMetadata(self):
        return jsonWriter(self.__tfpackage)

    def getJsonTest(self):
        return jsonWriter("""Testing a string with ' and " and \n\rnewlines""")

    def getManifestViewId(self):
        searchPortal = self.__tfpackage.get("viewId", defaultPortal)
        if Services.portalManager.exists(searchPortal):
            return searchPortal
        else:
            return defaultPortal

    def __getRvtManifest(self, manifest):
        rvtMap = HashMap()
        rvtMap.put("title", self.__tfpackage.get("title"))
        rvtMap.put("toc", self.__getRvtNodes(manifest))
        rvtManifest = JsonConfigHelper(rvtMap)
        return rvtManifest.toString()

    def __getRvtNodes(self, manifest):
        rvtNodes = ArrayList()
        #print "manifest=%s" % manifest
        for key in manifest.keySet():
            node = manifest.get(key)
            rvtNode = HashMap()
            if node.get("hidden") != "True":
                relPath = node.get("id")
                if not relPath:
                    relPath = "blank"
                rvtNode.put("visible", True)
                rvtNode.put("relPath", relPath)
                rvtNode.put("title", node.get("title"))
                rvtNode.put("children", self.__getRvtNodes(node.getJsonMap("children")))
                rvtNodes.add(rvtNode)
        return rvtNodes

scriptObject = Test()

