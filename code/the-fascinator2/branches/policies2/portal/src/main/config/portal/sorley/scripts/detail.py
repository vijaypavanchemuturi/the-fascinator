import os

from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.api.storage import PayloadType
from au.edu.usq.fascinator.common import JsonConfigHelper

from java.io import ByteArrayInputStream, ByteArrayOutputStream, File, StringWriter
from java.net import URLDecoder

from org.apache.commons.io import IOUtils
from org.dom4j.io import OutputFormat, XMLWriter, SAXReader

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
    
    def getFieldValues(self, name):
        return ", ".join(self.getFieldList(name))
    
    def toString(self):
        return self.json.toString()

class DetailData:
    def __init__(self):
        self.__storage = Services.storage
        uri = URLDecoder.decode(request.getAttribute("RequestURI"))
        basePath = portalId + "/" + pageName
        self.__oid = uri[len(basePath)+1:]
        self.__metadata = JsonConfigHelper()
        self.__search()
        docList = self.__json.getList("response/docs")
        if docList:
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
    
    def getPayloadContent(self):
        pid = os.path.splitext(self.__pid)[0] + ".htm"
        payload = self.__storage.getPayload(self.__sid, pid)
        saxReader = SAXReader(False)
        document = saxReader.read(payload.getInputStream())
        bodyNode = document.selectSingleNode("//*[local-name()='body']")
        self.__fixLinks(bodyNode, "img", "src")
        self.__fixLinks(bodyNode, "a", "href")
        format = OutputFormat.createPrettyPrint()
        format.setSuppressDeclaration(True)
        out = ByteArrayOutputStream()
        writer = XMLWriter(out, format)
        writer.write(bodyNode)
        writer.close()
        return out.toString("UTF-8")
    
    def __fixLinks(self, root, elemName, attrName):
        nodes = root.selectNodes("//*[local-name()='%s']" % elemName)
        for node in nodes:
           attrValue = node.attributeValue(attrName)
           if attrValue and not (attrValue.startswith("#") or attrValue.find("://") != -1):
               newAttrValue = "%s/download/%s/%s" % (portalPath, self.__oid, attrValue)
               node.addAttribute(attrName, newAttrValue)

scriptObject = DetailData()
