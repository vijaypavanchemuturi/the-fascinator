from au.edu.usq.fascinator.api.storage import Payload
from java.lang import String, Boolean, System
from java.io import ByteArrayInputStream, File, FileInputStream, FileOutputStream, ByteArrayOutputStream, OutputStream
from java.util.zip import ZipOutputStream, ZipEntry
from org.apache.commons.io import IOUtils

from org.dom4j import DocumentHelper, QName
from org.dom4j.io import XMLWriter, OutputFormat, SAXReader

import traceback

class Epub:
    def __init__(self):
        print "generating epub for: ", self.__getPortal().description
        self.__exportEpub()
        
    def __exportEpub(self):
        self.__epubFolder = File(System.getProperty("user.home"), ".fascinator/epub")
        self.__epubFolder.mkdirs()

        manifest = self.__getPortalManifest()
        self.__epubMimetypeStream = None
        self.__epubContainerStream = None
        self.__epubcss = None
        self.__orderedItem = []
        self.__itemRefDict = {}
        if manifest:
            self.__getEpubMimeTypeFiles()
            self.__getDigitalItems(manifest)
            self.__generateEpub()
            
    def __getEpubMimeTypeFiles(self):
        try:
            mimeTypeFile = File(self.__getPortal().getClass().getResource("/epub/mimetype").toURI())
            self.__epubMimetypeStream = FileInputStream(mimeTypeFile)
            
            containerFile = File(self.__getPortal().getClass().getResource("/epub/container.xml").toURI())
            self.__epubContainerStream = FileInputStream(containerFile)
            
            cssFile = File(self.__getPortal().getClass().getResource("/epub/epub.css").toURI())
            self.__epubcss = FileInputStream(cssFile)
        except:
            print "Mimetype file/container.xml file for epub are not found or in wrong format"
        
    def __generateEpub(self):
        zipFileName = self.__epubFolder.getAbsolutePath() + "/%s.epub" % self.__getPortal().name
        zipOutputStream = ZipOutputStream(FileOutputStream(zipFileName))
        
        #save mimetype... and the rest of standard files in epub
        zipOutputStream.putNextEntry(ZipEntry("mimetype"))
        IOUtils.copy(self.__epubMimetypeStream, zipOutputStream)
        zipOutputStream.closeEntry()
        
        zipOutputStream.putNextEntry(ZipEntry("META-INF/container.xml"))
        IOUtils.copy(self.__epubContainerStream, zipOutputStream)
        zipOutputStream.closeEntry()

        zipOutputStream.putNextEntry(ZipEntry("OEBPS/epub.css"))
        IOUtils.copy(self.__epubcss, zipOutputStream)
        zipOutputStream.closeEntry()
        
        #toc.ncx
        document = DocumentHelper.createDocument()
        tocElement = document.addElement("ncx:ncx")
        tocElement.addNamespace("ncx", "http://www.daisy.org/z3986/2005/ncx/")
        tocElement.addAttribute("version", "2005-1")
        
        tocMetadata = { "dtb:uid" : "0",
                        "dtb:depth": "1", 
                        "dtb:totalPageCount": "0",
                        "dtb:maxPageNumber": "0"
                      }
        headElement = tocElement.addElement("ncx:head")
        for meta in tocMetadata:
            headElement.addElement("ncx:meta").addAttribute("name", meta).addAttribute("content", tocMetadata[meta])
            
        tocElement.addElement("ncx:docTitle").addElement("ncx:text").addText(self.__getPortal().description)
        
        #content.opf
        document1 = DocumentHelper.createDocument()
        packageElement = document1.addElement("pac:package")
        packageElement = document1.getRootElement()
        packageElement.addNamespace("pac", "http://www.idpf.org/2007/opf")
        packageElement.addAttribute("version", "2.0")
        packageElement.addAttribute("unique-identifier", portalId)
        
        metaElement = packageElement.addElement("pac:metadata")
        metaElement.addNamespace("dc", "http://purl.org/dc/elements/1.1/")
        metaElement.addNamespace("opf", "http://www.idpf.org/2007/opf")
        
        metaElement.addElement("dc:title").addText(self.__getPortal().description)
        metaElement.addElement("dc:language").addText("en-AU")
        metaElement.addElement("dc:creator").addAttribute("opf:role", "aut").addText("The Fascinator Desktop")
        metaElement.addElement("dc:publisher").addText("USQ")
        metaElement.addElement("dc:identifier").addAttribute("id", portalId).addText(self.__getPortal().description)
        
        #Assigning items to toc & content
        navMap = tocElement.addElement("ncx:navMap")
        
        manifest = packageElement.addElement("pac:manifest")
        manifest.addElement("pac:item").addAttribute("id", "ncx").addAttribute("href", "toc.ncx").addAttribute("media-type", "text/xml")
        manifest.addElement("pac:item").addAttribute("id", "style").addAttribute("href", "epub.css").addAttribute("media-type", "text/css")
        spine = packageElement.addElement("pac:spine").addAttribute("toc", "ncx")
        
        count = 1
        for itemHash in self.__orderedItem:
            id, title, htmlFileName, payloadDict = self.__itemRefDict[itemHash]
            
            #toc
            navPoint = navMap.addElement("ncx:navPoint").addAttribute("id", itemHash).addAttribute("playOrder", str(count))
            navPoint.addElement("ncx:navLabel").addElement("ncx:text").addText(title)
            navPoint.addElement("ncx:content").addAttribute("src", htmlFileName)  
            
            for payloadId in payloadDict:
                #need to save the payload to the temp file...
                payload, payloadType = payloadDict[payloadId]
                if isinstance(payload, Payload):
                    payloadId = payloadId.lower()
                    zipOutputStream.putNextEntry(ZipEntry("OEBPS/%s" % payloadId))
                    
                    if payloadType == "application/xhtml+xml":
                        ##process the html....
                        saxReader = SAXReader(Boolean.parseBoolean("false"))
                        try:
                            saxDoc = saxReader.read(payload.getInputStream())
                            ## remove class or style nodes
                            classOrStyleNodes = saxDoc.selectNodes("//@class | //@style ")
                            for classOrStyleNode in classOrStyleNodes:
                                node = classOrStyleNode
                                if classOrStyleNode.getParent():
                                    node = classOrStyleNode.getParent()
                                if node.getQualifiedName() == "img":
                                    attr = node.attribute(QName("class"))
                                    if attr:
                                        node.remove(attr)
                                    attr = node.attribute(QName("style"))
                                    if attr:
                                        node.remove(attr)
                                    
                            ## remove name in a tags
                            ahrefs = saxDoc.selectNodes("//*[local-name()='a' and @name!='']")
                            for a in ahrefs:
                                attr = a.attribute(QName("name"))
                                if attr:
                                    a.remove(attr)
                            
                            bodyNode = saxDoc.selectSingleNode("//*[local-name()='div' and @class='body']")
                            bodyNode.setName("div")
                            
                            out = ByteArrayOutputStream()
                            format = OutputFormat.createPrettyPrint()
                            format.setSuppressDeclaration(True)
                            writer = XMLWriter(out, format)
                            writer.write(bodyNode)
                            writer.flush()
                            contentStr = out.toString("UTF-8")
                            
                            htmlString = """<html xmlns="http://www.w3.org/1999/xhtml"><head><title>%s</title>
                                            <link rel="stylesheet" href="epub.css"/>
                                            </head><body>%s</body></html>"""
                            htmlString = htmlString % (title, contentStr)
                            
                            IOUtils.copy(ByteArrayInputStream(String(htmlString).getBytes("UTF-8")), zipOutputStream)
                            
                        except:
                            traceback.print_exc()
                    else:
                        #... other than image
                        IOUtils.copy(payload.getInputStream(), zipOutputStream)
                    zipOutputStream.closeEntry()
                else:
                    zipOutputStream.putNextEntry(ZipEntry("OEBPS/%s" % payloadId))
                    IOUtils.copy(payload, zipOutputStream)
                    zipOutputStream.closeEntry()
                
                item = manifest.addElement("pac:item")
                #item.addAttribute("id", payloadId.replace("/", "_"))
                if payloadId == htmlFileName:
                    item.addAttribute("id", itemHash)
                else:
                    item.addAttribute("id", self.__getProperId(payloadId))
                
                item.addAttribute("href", payloadId)
                item.addAttribute("media-type", payloadType)
                
            itemRef = spine.addElement("pac:itemref")
            itemRef.addAttribute("idref", self.__getProperId(itemHash))
            
            count +=1
        
        #saving content.opf...
        zipOutputStream.putNextEntry(ZipEntry("OEBPS/content.opf"))
        format = OutputFormat.createPrettyPrint()
        writer = XMLWriter(zipOutputStream, format)
        writer.write(document1)
        writer.flush()
        zipOutputStream.closeEntry()

        #saving toc.ncx
        zipOutputStream.putNextEntry(ZipEntry("OEBPS/toc.ncx"))
        writer2 = XMLWriter(zipOutputStream, format)
        writer2.write(document)
        writer2.flush()
        zipOutputStream.closeEntry()
        
        zipOutputStream.close()
    
    def __getProperId(self, id):
        #not start with numeric and must not have slash
        if id[:1].isdigit():
            id = "_%s" % id
        return id.replace("/", "_")
        
    
    def __getDigitalItems(self, manifest):
        for itemHash in manifest.keySet():
            self.__orderedItem.append(itemHash)
            payloadDict = {}
            item = manifest[itemHash]
            id = item.get("id")
            title = item.get("title")
            pid = id[id.rfind("/")+1:]
            htmlFileName = pid[:pid.rfind(".")] + ".htm"
            
            sourcePayload = Services.storage.getPayload(id, pid)
            payloadType = sourcePayload.contentType
            htmlPayload = Services.storage.getPayload(id, htmlFileName)
            if htmlPayload:
                #gather all the related payload
                payloadDict[htmlFileName] = htmlPayload, "application/xhtml+xml"
                payloadList = Services.storage.getObject(id).getPayloadList()
                for payload in payloadList:
                    if payload.id.find("_files") > -1:
                        payloadDict[payload.id] = payload, payload.contentType
            elif sourcePayload:
                #for now only works for images
                if payloadType.startswith("image"):
                    htmlString = """<html xmlns="http://www.w3.org/1999/xhtml"><head><title>%s</title>
                                    <link rel="stylesheet" href="epub.css"/>
                                    </head><body><div><span style="display: block"><img src="%s" alt="%s"/></span></div></body></html>"""
                    htmlString = htmlString % (pid, pid.lower(), pid)
                    payloadDict[pid] = sourcePayload, payloadType
                    payloadDict[htmlFileName] = ByteArrayInputStream(String(htmlString).getBytes("UTF-8")), "application/xhtml+xml"
        
            self.__itemRefDict[itemHash] = id, title, htmlFileName, payloadDict
        return self.__itemRefDict, self.__orderedItem
    
    def __getPortalManifest(self):
        return self.__getPortal().getJsonMap("manifest")
    
    def __getPortal(self):
        return Services.portalManager.get(portalId)
                
scriptObject = Epub()



