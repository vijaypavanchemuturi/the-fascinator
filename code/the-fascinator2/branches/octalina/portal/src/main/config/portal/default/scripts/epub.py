from au.edu.usq.fascinator.api.storage import Payload
from org.dom4j import DocumentHelper #, XmlWriter
from java.lang import System
from java.io import File
from java.io import FileInputStream
from java.io import ByteArrayInputStream

class Epub:
    def __init__(self):
        portalManager = Services.getPortalManager()
        print "Generating epub for portal: %s" % portalId
        self.__tempDirStr = System.getProperty("java.io.tmpdir") + "epub"
        
        self.__orderedList = []
        self.__itemWithPayload = {}
        self.__tempDir = File(System.getProperty("java.io.tmpdir"), "epub")
        if self.__tempDir.exists():
            self.__tempDir.delete()
        self.__tempDir.mkdirs()
        self.__contentFolder = File(self.__tempDir.getAbsolutePath(), "OEBPS")
        self.__contentFolder.mkdir()
        
        print self.__tempDir.getAbsolutePath()
        print self.__contentFolder.getAbsolutePath()
        self.__generateEpub()
        
    def __generateEpub(self):
        manifest = self.__getOrganiserManifest()
        if manifest:
            self.__getHtmlSource(manifest)
            self.__generateNCXToc()
        
    def __getOrganiserManifest(self):
        return self.__getPortal().getJsonMap("manifest")
    
    def __getPortal(self):
        return Services.portalManager.get(portalId)
    
    def __getPortalDescription(self):
        return self.__getPortal().description
    
    def __generateNCXToc(self):
        #toc.ncx
        tocFileName = "toc.ncx"
#        tocWriter = new XMLWriter(
#            new FileWriter( "output.xml" )
#        );
        document = DocumentHelper.createDocument()
        rootElement = document.addElement("ncx")
        rootElement.addNamespace("ncx", "http://www.daisy.org/z3986/2005/ncx/")
        rootElement.addAttribute("version", "2005-1")
        
        rootElement = self.__generateHeadMeta(rootElement)
        navMap = rootElement.addElement("navMap")
        
        #content.opf
        packageDocument = DocumentHelper.createDocument()
        packageRootElement = packageDocument.addElement("package")
        packageRootElement.addNamespace("opf", "http://www.idpf.org/2007/opf")
        packageRootElement.addAttribute("version", "2.0")
        packageRootElement.addAttribute("unique-identifier", "BookId")
        
        metadata = packageRootElement.addElement("metadata")
        metadata.addNamespace("dc", "http://purl.org/dc/elements/1.1/")
        metadata.addNamespace("opf", "http://www.idpf.org/2007/opf")
        dcMeta = {"dc:title" : self.__getPortalDescription(),
                  "dc:language" : "en",
                  "dc:creator" : "The Fascinator Desktop"
                  }
        for meta in dcMeta:
            metadata.addElement(meta).setText(dcMeta[meta])
        manifest = packageRootElement.addElement("manifest")
        
        spine = packageRootElement.addElement("spine")
        spine.addAttribute("toc", tocFileName)
        
        count = 1
        for itemHash in self.__orderedList:
            id, title, htmlFileName, payloadList = self.__itemWithPayload[itemHash]
            count = 1
                
            navPoint = navMap.addElement("navPoint")
            navPoint.addAttribute("id", itemHash)
            navPoint.addAttribute("playOrder", str(count))
            navText = navPoint.addElement("navLabel").addElement("text")
            navText.addText(title)
            navPoint.addElement("content").addAttribute("src", htmlFileName)         
            
            for payloadId in payloadList:
                #need to save the payload to the temp file...
                payload = payloadList[payloadId]
                print type(payload)
                contentType = "application/xhtml+xml"
                if isinstance(payload, Payload):
                    contentType = payload.contentType
                    #need to save the payload stream
                else:
                    #for the image... need to save the html string
                    pass
                item = manifest.addElement("item")
                item.addAttribute("id", payloadId)
                item.addAttribute("href", payloadId)
                item.addAttribute("media-type", contentType)
                
            itemRef = spine.addElement("itemRef")
            itemRef.addAttribute("idref", htmlFileName)
                
            
            count+=1
            
        ### Need to save the content.opf 
        print packageDocument.getRootElement().asXML()
        
        
        print    
        ### Need to save the toc.ncx     
        print document.getRootElement().asXML()
    
    
    
    
    
    
    
    
    
    

    def __getHtmlSource(self, manifest):
        for itemHash in manifest.keySet():
            self.__orderedList.append(itemHash)
            payloadDict = {}
            item = manifest[itemHash]
            id = item.get("id")
            title = item.get("title")
            pid = id[id.rfind("/")+1:]
            htmlFileName = pid[:pid.rfind(".")] + ".htm"

            sourcePayload = Services.storage.getPayload(id, pid)
            htmlPayload = Services.storage.getPayload(id, htmlFileName)
            if htmlPayload:
                payloadDict[htmlFileName] = htmlPayload
                #get related _files
                payloadList = Services.storage.getObject(id).getPayloadList()
                for payload in payloadList:
                    if payload.id.find("_files") > -1:
                        payloadDict[payload.id] = payload
            elif sourcePayload:
                if sourcePayload.contentType.startswith("image"):
                    htmlString = "<html><head><title>%s</title></head><body><img src='%s' alt='%s'/></body></html>"
                    htmlString = htmlString % (pid, pid, pid)
                    payloadDict[pid] = sourcePayload
                    payloadDict[htmlFileName] = htmlString
#                    payloadDict[htmlFileName] = ByteArrayInputStream(htmlString.getBytes())
                    #print ByteArrayInputStream(htmlString.getBytes("UTF-8"))
                
            self.__itemWithPayload[itemHash] = id, title, htmlFileName, payloadDict
            print self.__itemWithPayload
#            if digitalObject:
#                payloadList = digitalObject.getPayloadList()
                
            
#            payloadList = digitalObject.getPayloadList()
#            foundHtml = False
#            relatedPayload = {}
#            for payload in payloadList:
#                if payload.id.endswith(".htm"):
#                    foundHtml = True
#                    relatedPayload[payload.id] = payload.getInputStream()
#                elif payload.id.find("_files") > -1:
#                    relatedPayload[payload.id] = payload.getInputStream()
#            
#            if relatedPayload == {}:
#                oid = digitalObject.getId()
#                slash = oid.rfind("/")
#                pid = oid[slash+1:]
#                payload = Services.storage.getPayload(oid, pid)
#                if payload.contentType.startswith("image"):
#                    #generate html preview
#                    htmlStr = "<html><head><title>%s</title></head><body><img src='%s' alt='%s'/></body></html>"
#                    htmlStr = htmlStr % (pid, pid, pid)
#                    relatedPayload["%s.htm" % pid] = htmlStr
#                    relatedPayload[pid] = payload.getInputStream()
#        
#        print "---- relatedPayload: ", relatedPayload
#                
#        return relatedPayload
                

    
    def __generateHeadMeta(self, rootElement):   
        metaDict = { "dtb:uid": self.__getPortal().name,
                     "dtb:depth": "1",
                     "dtb:totalPageCount": "0",
                     "dtb:maxPageNumber": "0" }   
        headElem = rootElement.addElement("head")
        for meta in metaDict:
            metaElem = headElem.addElement("meta")
            metaElem.addAttribute("name", meta)
            metaElem.addAttribute("content", metaDict[meta])
            
        docTitle = rootElement.addElement("docTitle")
        docTitle.addElement("text").setText(self.__getPortalDescription())
        return rootElement
                
scriptObject = Epub()



#<?xml version="1.0" encoding="UTF-8"?>
#<ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1" xml:lang="en">
#   <head xmlns:ncx="http://www.daisy.org/z3986/2005/ncx/">
#      <meta name="dtb:uid" content="http://www.snee.com/epub/pg23598"/>
#      <meta name="dtb:depth" content="1"/>
#      <meta name="dtb:totalPageCount" content="0"/>
#      <meta name="dtb:maxPageNumber" content="0"/>
#   </head>
#   <docTitle xmlns:ncx="http://www.daisy.org/z3986/2005/ncx/">
#      <text>Little Bo-Peep: A Nursery Rhyme Picture Book</text>
#
#   </docTitle>
#   <navMap xmlns:ncx="http://www.daisy.org/z3986/2005/ncx/">
#      <navPoint id="navpoint-1" playOrder="1">
#         <navLabel>
#            <text>Little Bo-Peep: A Nursery Rhyme Picture Book</text>
#         </navLabel>
#         <content src="23598-h.htm"/>
#      </navPoint>
#   </navMap>
#</ncx>
