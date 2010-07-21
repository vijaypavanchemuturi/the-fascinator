from __main__ import Services, formData

from au.edu.usq.fascinator.common import JsonConfigHelper

from org.apache.commons.lang import StringEscapeUtils
from java.lang import Exception as JavaException, Boolean, String
from java.util import ArrayList, HashMap
from java.io import (InputStreamReader, ByteArrayInputStream,
    ByteArrayOutputStream, File, StringWriter)
from org.apache.commons.io import IOUtils
from au.edu.usq.fascinator.api.indexer import SearchRequest

from json2 import read as jsonReader, write as jsonWriter
import re


class Test(object):
    def __init__(self):
        self.__oid = formData.get("oid")
        self.__tfpackage = None
        self.__manifest = None
        self.__object = None
        func = formData.get("func", "")
        id = formData.get("id")
        result = None

        print "**** test.py"
        #print "formData: %s" % formData
        try:
            if self.__oid:
                # get the package manifest
                self.__object = Services.getStorage().getObject(self.__oid)
                sourceId = self.__object.getSourceId()
                payload = self.__object.getPayload(sourceId)
                writer = StringWriter()
                IOUtils.copy(payload.open(), writer)
                self.__tfpackage = jsonReader(writer.toString())
                self.__manifest = self.__tfpackage.get("manifest")
                if not self.__manifest:
                    self.__manifest = {}
                    self.__tfpackage["manifest"]=self.__manifest
                payload.close()
                #print self.__oid
                #print self.__tfpackage
            if func=="getItem" and id:
                doc = self._getPackagedItem(id)
                result = jsonWriter({"doc":doc, "id":id})
            elif func=="addItem" and id and self.__tfpackage:
                title = formData.get("title")
                if self._addItem(id, title):
                    result = jsonWriter({"ok":"addedItem", "id":id})
                else:
                    result = jsonWriter({"id":id,
                        "error":"Failed to addItem id='%s', title='%s'" % (id, title)})
            elif func=="removeItem" and id and self.__tfpackage:
                if self._removeItem(id):
                    result = jsonWriter({"ok":"removedItem", "id":id})
                else:
                    result = jsonWriter({"id":id,
                        "error":"Failed to remove item"})
        except Exception, e:
            log.error("Failed to load manifest", e);
            result = jsonWriter({"status":"error", "message":"%s" % str(e)})
        if self.__object:
            self.__object.close()
            self.__object=None
        if result is not None:
            writer = response.getPrintWriter("text/plain; charset=UTF-8")
            #writer = response.getPrintWriter("application/json; charset=UTF-8")
            writer.println(result)
            writer.close()

    def getJsonMetadata(self):              # used
        return jsonWriter(self.__tfpackage)

    def getAjaxRequestUrl(self):
        return "../workflows/test.ajax"

    def getJsonTest(self):                  # used (testing)
        return jsonWriter("""Testing a string with ' and " and \n\rnewlines""")

    def getJsonPackagedItems(self):
        return jsonWriter(self._getPackagedItems())

    def _addItem(self, id, title):
        # "node-eeceaa96721e8f5682b5bde81d0a6536" : {
        #      "id" : "eeceaa96721e8f5682b5bde81d0a6536",
        #      "title" : "icons.gif"    }
        try:
            nodeId = "node-%s" % id
            if self.__manifest.has_key(nodeId):
                print "Error the manifest already contains nodeId '%s'" % nodeId
                return False
            self.__manifest[nodeId] = {"id":id, "title":title}
            self._savePackage()
            return True
        except Exception, e:
            print "Error adding an item to the manifest - '%s'" % str(e)
            return False

    def _removeItem(self, id):
        nodeId = "node-%s" % id
        item = self.__manifest.pop(nodeId)
        if item:
            self._savePackage()
            return True
        else:
            return False

    def _getPackagedItems(self):
        ids = []
        docs = []
        try:
            manifest = self.__tfpackage.get("manifest", {})     #### []
            for v in manifest.itervalues():
                id = v.get("id")
                ids.append(id)
            docs = self._search(ids)
        except Exception, e:
            pass
        return docs

    def _getPackagedItem(self, id):
        ids = [id]
        doc = None
        docs = self._search(ids)
        if len(docs):
            doc = docs[0]
        return doc


    def _search(self, ids):
        # id = "0a815e0473f54d98a8a74c36345fcceb"
        docs = []
        if len(ids)==0:
            return docs
        try:
            query = " or ".join(["id:%s" % id for id in ids])
            print "_search query='%s'" % query
            req = SearchRequest(query)
            req.setParam("sort", "f_dc_title asc")
            req.setParam("sort", "dateCreated asc")
            out = ByteArrayOutputStream()
            Services.indexer.search(req, out)
            writer = StringWriter()
            IOUtils.copy(ByteArrayInputStream(out.toByteArray()), writer)
            json = jsonReader(writer.toString())
            #print "json='%s'" % json
            response = json.get("response")
            if response:
                docs = response.get("docs", [])
        except Exception, e:
            print "********* ERROR: %s" % str(e)
        docs = [{
                "title":d.get("dc_title", [""])[0],
                "description":d.get("dc_description", [""])[0],
                "thumbnail":d.get("thumbnail"),
                "id":d.get("storage_id")
                } for d in docs]
        return docs

    def getFormData(self, field):
        return StringEscapeUtils.escapeHtml(formData.get(field, ""))

    def _savePackage(self):
        json = jsonWriter(self.__tfpackage)
        self.__object.updatePayload(self.__object.getSourceId(),
                        ByteArrayInputStream(String(json).getBytes("UTF-8")))

#    ##
#    def getManifest(self):
#        return self.__tfpackage.get("manifest", {})
#
#    def getMeta(self, metaName):
#        return StringEscapeUtils.escapeHtml(formData.get(metaName, self.__tfpackage.get(metaName)))
#
#    def getManifestViewId(self):
#        searchPortal = self.__tfpackage.get("viewId", defaultPortal)
#        if Services.portalManager.exists(searchPortal):
#            return searchPortal
#        else:
#            return defaultPortal
#
#    def __getRvtManifest(self, manifest):
#        rvtMap = HashMap()
#        rvtMap.put("title", self.__tfpackage.get("title"))
#        rvtMap.put("toc", self.__getRvtNodes(manifest))
#        rvtManifest = JsonConfigHelper(rvtMap)
#        return rvtManifest.toString()
#
#    def __getRvtNodes(self, manifest):
#        rvtNodes = ArrayList()
#        #print "manifest=%s" % manifest
#        for key in manifest.keySet():
#            node = manifest.get(key)
#            rvtNode = HashMap()
#            if node.get("hidden") != "True":
#                relPath = node.get("id")
#                if not relPath:
#                    relPath = "blank"
#                rvtNode.put("visible", True)
#                rvtNode.put("relPath", relPath)
#                rvtNode.put("title", node.get("title"))
#                rvtNode.put("children", self.__getRvtNodes(node.getJsonMap("children")))
#                rvtNodes.add(rvtNode)
#        return rvtNodes

scriptObject = Test()

