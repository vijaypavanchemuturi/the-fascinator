import os, re

from download import DownloadData

from java.io import ByteArrayInputStream, ByteArrayOutputStream
from java.lang import Boolean
from java.net import URLDecoder
from java.util import TreeMap

from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.api.storage import StorageException
from au.edu.usq.fascinator.common import JsonConfigHelper

class DetailData:
    def __init__(self):
        pass
    
    def __activate__(self, context):
        self.services = context["Services"]
        self.request = context["request"]
        self.response = context["response"]
        self.contextPath = context["contextPath"]
        self.formData = context["formData"]
        self.page = context["page"]
        
        uri = URLDecoder.decode(self.request.getAttribute("RequestURI"))
        matches = re.match("^(.*?)/(.*?)/(?:(.*?)/)?(.*)$", uri)
        if matches and matches.group(3):
            oid = matches.group(3)
            pid = matches.group(4)
            # If we have a PID
            if pid:
                # Download the payload instead... supports relative links
                download = DownloadData()
                download.__activate__(context)

            # Otherwise, render the detail screen
            else:
                self.__oid = oid
                self.__loadSolrData(oid)
                if self.isIndexed():
                    self.__object = self.__getObject(oid)
                    self.__metadata = self.__solrData.getJsonList("response/docs").get(0)
                    self.__json = JsonConfigHelper(self.__solrData.getList("response/docs").get(0))
                    self.__metadataMap = TreeMap(self.__json.getMap("/"))
        else:
            # require trailing slash for relative paths
            self.response.sendRedirect("%s/%s/" % (self.contextPath, uri))
    
    def hasLocalFile(self):
        # get original file.path from object properties
        filePath = self.getObject().getMetadata().getProperty("file.path")
        return filePath and os.path.exists(filePath)
    
    def getOid(self):
        return self.__oid
    
    def isIndexed(self):
        return self.__getNumFound() == 1
    
    def getMetadata(self):
        return self.__metadata
    
    def getMetadataMap(self):
        return self.__metadataMap
    
    def getObject(self):
        return self.__object
    
    def isPending(self):
        meta = self.getObject().getMetadata()
        status = meta.get("render-pending")
        return Boolean.parseBoolean(status)
    
    def getFriendlyName(self, name):
        if name.startswith("dc_"):
            name = name[3:]
        if name.startswith("meta_"):
            name = name[5:]
        return name.replace("_", " ").capitalize()
    
    def isDetail(self):
        preview = Boolean.parseBoolean(self.formData.get("preview", "false"))
        return not (self.request.isXHR() or preview)
    
    def __loadSolrData(self, oid):
        portal = self.page.getPortal()
        query = 'id:"%s"' % oid
        if self.isDetail() and portal.getSearchQuery():
            query += " AND " + portal.getSearchQuery()
        req = SearchRequest(query)
        req.addParam("fq", 'item_type:"object"')
        if self.isDetail():
            req.addParam("fq", portal.getQuery())
        out = ByteArrayOutputStream()
        self.services.getIndexer().search(req, out)
        self.__solrData = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
    
    def __getNumFound(self):
        return int(self.__solrData.get("response/numFound"))
    
    def __getObject(self, oid):
        try:
            storage = self.services.getStorage()
            try:
                obj = storage.getObject(oid)
            except StorageException:
                sid = self.__getStorageId(oid)
                obj = storage.getObject(sid)
                print "Object not found: oid='%s', trying sid='%s'" % (oid, sid)
        except StorageException:
            print "Object not found: oid='%s'" % oid
        return obj
    
    def __getStorageId(self, oid):
        return self.__metadata.get("storage_id")
    