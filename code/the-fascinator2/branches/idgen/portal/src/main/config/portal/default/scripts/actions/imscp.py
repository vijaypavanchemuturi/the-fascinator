from __main__ import Services, formData, portalId, response

import os.path, urllib

from au.edu.usq.fascinator.common import JsonConfigHelper
from au.edu.usq.fascinator.ims import FileType, ItemType, ManifestType, \
    MetadataType, ObjectFactory, OrganizationType, OrganizationsType, \
    ResourceType, ResourcesType

from java.io import FileOutputStream, StringWriter
from java.util.zip import ZipEntry, ZipOutputStream
from javax.xml.bind import JAXBContext, Marshaller

from org.apache.commons.io import IOUtils

class ImsPackage:
    def __init__(self, outputFile=None):
        oid = formData.get("oid")
        print "Creating IMS content package for: %s" % oid
        try:
            # get the package manifest
            object = Services.getStorage().getObject(oid)
            sourceId = object.getSourceId()
            payload = object.getPayload(sourceId)
            self.__manifest = JsonConfigHelper(payload.open())
            payload.close()
            object.close()
            # create the package
            url = formData.get("url")
            if outputFile is None and url is None:
                self.__createPackage()
            elif url is not None and outputFile is not None:
                self.__createPackage(outputFile)
        except Exception, e:
            log.error("Failed to create IMS content package: %s" % str(e))
    
    def __createPackage(self, outputFile=None):
        title = self.__manifest.get("title")
        manifest = self.__createManifest()
        context = JAXBContext.newInstance("au.edu.usq.fascinator.ims")
        m = context.createMarshaller()
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, True)
        writer = StringWriter()
        jaxbElem = ObjectFactory.createManifest(ObjectFactory(), manifest)
        m.marshal(jaxbElem, writer)
        writer.close()
        
        if outputFile is not None:
            print "writing to %s..." % outputFile
            out = FileOutputStream(outputFile)
        else:
            print "writing to http output stream..."
            response.setHeader("Content-Disposition", "attachment; filename=%s.zip" % urllib.quote(title))
            out = response.getOutputStream("application/zip")
        
        zipOut = ZipOutputStream(out)
        
        zipOut.putNextEntry(ZipEntry("imsmanifest.xml"))
        IOUtils.write(writer.toString(), zipOut)
        zipOut.closeEntry()
        
        jsonManifest = self.__manifest.getJsonMap("manifest")
        for key in jsonManifest.keySet():
            item = jsonManifest.get(key)
            oid = item.get("id")
            obj = Services.getStorage().getObject(oid)
            for pid in obj.getPayloadIdList():
                payload = obj.getPayload(pid)
                if pid != "SOF-META":
                    zipOut.putNextEntry(ZipEntry("%s/%s" % (key[5:], pid)))
                    IOUtils.copy(payload.open(), zipOut)
                    payload.close()
                    zipOut.closeEntry()
            obj.close()
        zipOut.close()
        out.close()
    
    def __createManifest(self):
        manifest = ManifestType()
        meta = MetadataType()
        meta.setSchema("IMS Content")
        meta.setSchemaversion("1.1.4")
        manifest.setMetadata(meta)
        
        jsonManifest = self.__manifest.getJsonMap("manifest")
        
        orgs = OrganizationsType()
        org = OrganizationType()
        org.setIdentifier("default")
        org.setTitle(self.__manifest.get("title"))
        orgs.getOrganization().add(org)
        orgs.setDefault(org)
        manifest.setOrganizations(orgs)
        
        resources = ResourcesType()
        manifest.setResources(resources)
        for key in jsonManifest.keySet():
            jsonRes = jsonManifest.get(key)
            # item
            visible = jsonRes.get("hidden", "false") != "true"
            item = ItemType()
            item.setIdentifier("default-" + key)
            item.setIdentifierref(key)
            item.setIsvisible(visible)
            item.setTitle(jsonRes.get("title"))
            # webcontent
            webRes = ResourceType()
            webRes.setIdentifier(key)
            webRes.setType("webcontent")
            oid = jsonRes.get("id")
            _, filename = os.path.split(oid)
            baseName = os.path.splitext(filename)[0]
            webRes.setHref("%s/%s.htm" % (key[5:], baseName))
            obj = Services.getStorage().getObject(oid)
            for pid in obj.getPayloadIdList():
                if pid != "TF-OBJ-META":  ## hacky
                    file = FileType()
                    file.setHref(pid)
                    webRes.getFile().add(file)
            obj.close()
            org.getItem().add(item)
            resources.getResource().add(webRes)
        
        return manifest
    
if __name__ == "__main__":
    scriptObject = ImsPackage()
