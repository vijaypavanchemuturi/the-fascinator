import os

from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.api.storage import PayloadType
from au.edu.usq.fascinator.common import JsonConfigHelper

from java.awt import Desktop
from java.io import ByteArrayInputStream, ByteArrayOutputStream, File, StringWriter
from java.net import URLDecoder
from java.lang import Boolean

from org.apache.commons.io import IOUtils
from org.dom4j.io import OutputFormat, XMLWriter, SAXReader

import traceback

import traceback

class SolrDoc:
    def __init__(self, json):
        self.json = json
    
    def getField(self, name):
        field = self.json.getList("response/docs/%s" % name)
        if field.isEmpty():
            return None
        return field.get(0)
    
    def getFieldText(self, name):
        return self.json.get("response/docs/%s" % name)
    
    def getFieldList(self, name):
        return self.json.getList("response/docs/%s" % name)
    
    def getDublinCore(self):
        dc = self.json.getList("response/docs").get(0)
        remove = ["dc_title", "dc_description"]
        for entry in dc:
            if not entry.startswith("dc_"):
                remove.append(entry)
        for key in remove:
            dc.remove(key)
        return JsonConfigHelper(dc).getMap("/")
    
    def toString(self):
        return self.json.toString()

class DetailData:
    def __init__(self):
        if formData.get("verb") == "open":
            self.__openFile()
        else:
            self.__storage = Services.storage
            uri = URLDecoder.decode(request.getAttribute("RequestURI"))
            basePath = portalId + "/" + pageName
            self.__oid = uri[len(basePath)+1:]
            self.__metadata = JsonConfigHelper()
            self.__search()
            self.__sid = self.__json.getList("response/docs").get(0).get("storage_id")
            slash = self.__sid.rfind("/")
            self.__pid = self.__sid[slash+1:]
            print """ * detail.py: uri='%s'
              oid='%s'
              sid='%s'
              pid='%s'""" % (uri, self.__oid, self.__sid, self.__pid)
            payload = self.__storage.getPayload(self.__sid, self.__pid)
    
    def __search(self):
        req = SearchRequest('id:"%s"' % self.__oid)
        out = ByteArrayOutputStream()
        Services.indexer.search(req, out)
        self.__json = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
        self.__metadata = SolrDoc(self.__json)
    
    def getSolrResponse(self):
        return self.__json
    
    def getMetadata(self):
        return self.__metadata
    
    def getObject(self):
        return self.__storage.getObject(self.__oid)
    
    def getPdfUrl(self):
        pid = os.path.splitext(self.__pid)[0] + ".pdf"
        return "%s/%s" % (self.__oid, pid)
    
    def getPayloadContent(self):
        pid = os.path.splitext(self.__pid)[0] + ".htm"
        payload = self.__storage.getPayload(self.__sid, pid)
        saxReader = SAXReader(False)
        document = saxReader.read(payload.getInputStream())
        bodyNode = document.selectSingleNode("//*[local-name()='body']")
        format = OutputFormat.createPrettyPrint()
        format.setSuppressDeclaration(True)
        out = ByteArrayOutputStream()
        writer = XMLWriter(out, format)
        writer.write(bodyNode)
        writer.close()
        return out.toString("UTF-8")
    
    def __openFile(self):
        value = formData.get("value")
        print " * detail.py: opening file %s..." % value
        Desktop.getDesktop().open(File(value))

scriptObject = DetailData()
