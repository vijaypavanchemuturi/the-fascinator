from java.net import URLDecoder
from java.io import StringWriter
from org.dom4j.io import OutputFormat, XMLWriter, SAXReader

from org.apache.commons.io import IOUtils;
from au.edu.usq.fascinator.model import DCRdf 

class DetailData:
    def __init__(self):
        self.__storage = Services.storage
        uri = request.getAttribute("RequestURI")
        basePath = portalId + "/" + pageName
        self.__oid = URLDecoder.decode(uri[len(basePath)+1:])
        self.__dcRdf = None
        
    def isHidden(self, pid):
        if pid.find("_files%2F")>-1:
            return 1
        return 0
        
    def getObject(self):
        print " *** getting %s" % self.__oid 
        return self.__storage.getObject(self.__oid)
    
    def getDcRdf(self):
        print " *** payload list: %s *** " % self.__storage.getObject(self.__oid).payloadList
        dcrdfPayload = self.__storage.getPayload(self.__oid, "dc-rdf")
        if dcrdfPayload is not None:
            self.__dcRdf = DCRdf(dcrdfPayload.getInputStream())
        return self.__dcRdf
    
    def getPayloadContent(self):
        print " *** payload content, format: %s *** " % self.__dcRdf.format
        contentStr = ""
        if self.__dcRdf.format.startswith("text"):
            contentStr = "<pre>"
            payload = self.__storage.getPayload(self.__oid, self.__dcRdf.label)
            
            str = StringWriter() 
            IOUtils.copy(payload.getInputStream(), str)
            contentStr += str.toString()
            contentStr += "</pre>"
        elif self.__dcRdf.format.find("vnd.ms-")>-1 or self.__dcRdf.format.find("vnd.oasis.opendocument.")>-1:
            #get the html version if exist....
            htmlPayloadName = self.__dcRdf.label
            htmlPayloadName = htmlPayloadName[:htmlPayloadName.find(".")] + ".htm"
            payload = self.__storage.getPayload(self.__oid, htmlPayloadName)
            
            saxReader = SAXReader()
            document = saxReader.read(payload.getInputStream())
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

scriptObject = DetailData()
