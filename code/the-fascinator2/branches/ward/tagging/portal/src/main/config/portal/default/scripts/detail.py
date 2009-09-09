from au.edu.usq.fascinator.api.indexer import SearchRequest
from au.edu.usq.fascinator.api.storage import PayloadType
from au.edu.usq.fascinator.common import JsonConfigHelper
from java.awt import Desktop
from java.io import ByteArrayInputStream, ByteArrayOutputStream, File, StringWriter
from java.net import URLDecoder
from org.dom4j.io import OutputFormat, XMLWriter, SAXReader

from org.apache.commons.io import IOUtils;
from au.edu.usq.fascinator.model import DCRdf 

import traceback

class SolrDoc:
    def __init__(self, json):
        self.json = json
    
    def getField(self, name):
        return self.json.getList("response/docs/%s" % name).get(0)
    
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
        print formData
        self.__storage = Services.storage
        uri = request.getAttribute("RequestURI")
        print " **************", uri
        basePath = portalId + "/" + pageName
        self.__oid = URLDecoder.decode(uri[len(basePath)+1:])
        self.__dcRdf = None
        self.__metadata = JsonConfigHelper()
        self.__search()
        if formData.get("verb") == "open":
            self.__openFile()
    
    def __search(self):
        req = SearchRequest('id:"%s"' % self.__oid)
        out = ByteArrayOutputStream()
        Services.indexer.search(req, out)
        self.__json = JsonConfigHelper(ByteArrayInputStream(out.toByteArray()))
        self.__metadata = SolrDoc(self.__json)
    
    def getSolrResponse(self):
        return self.__json
    
    def formatName(self, name):
        return name[3:4].upper() + name[4:]
    
    def formatValue(self, value):
        return value[1:-1]
    
    def isHidden(self, pid):
        if pid.find("_files%2F")>-1:
            return True
        return False
    
    def getMetadata(self):
        return self.__metadata
    
    def getObject(self):
        print " *** getting %s" % self.__oid 
        return self.__storage.getObject(self.__oid)
    
    def getDcRdf(self):
        print " *** payload list: %s *** " % self.__storage.getObject(self.__oid).payloadList
        dcrdfPayload = self.__storage.getPayload(self.__oid, "dc-rdf")
        if dcrdfPayload is not None:
            self.__dcRdf = DCRdf(dcrdfPayload.getInputStream())
        return self.__dcRdf
    
    def getStorageId(self):
        obj = self.getObject()
        if hasattr(obj, "getPath"):
            return obj.path.absolutePath
        return obj.id
    
    def hasSlideShow(self):
        slash = self.__oid.rfind("/")
        pid = self.__oid[slash+1:]
        pid = pid[:pid.find(".")] + ".slide.htm"
        payload = self.__storage.getPayload(self.__oid, pid)
        if payload.getInputStream() is None:
            return ""
        return pid
    
    def getPayloadContent(self):
        format = self.__metadata.getField("dc_format")
        slash = self.__oid.rfind("/")
        pid = self.__oid[slash+1:]
        print " *** payload content, format: %s, pid: %s *** " % (format, pid)
        contentStr = ""
        if format.startswith("text"):
            contentStr = "<pre>"
            payload = self.__storage.getPayload(self.__oid, pid)
            str = StringWriter() 
            IOUtils.copy(payload.getInputStream(), str)
            contentStr += str.toString()
            contentStr += "</pre>"
        elif format.find("vnd.ms-")>-1 or format.find("vnd.oasis.opendocument.")>-1 or format.find("application/pdf")>-1:
            #get the html version if exist....
            pid = pid[:pid.find(".")] + ".htm"
            payload = self.__storage.getPayload(self.__oid, pid)
            saxReader = SAXReader()
            try:
                document = saxReader.read(payload.getInputStream())
            except:
                traceback.print_exc()
            slideNode = document.selectSingleNode("//div[@class='body']")
            #linkNodes = slideNode.selectNodes("//img")
            #contentStr = slideNode.asXML();
            # encode character entities correctly
            out = ByteArrayOutputStream()
            format = OutputFormat.createPrettyPrint()
            format.setSuppressDeclaration(True)
            writer = XMLWriter(out, format)
            writer.write(slideNode)
            writer.close()
            contentStr = out.toString("UTF-8")
        return contentStr
    
    def __openFile(self):
        print " ********", self.__oid
        Desktop.getDesktop().open(File(self.__oid))

scriptObject = DetailData()
