


#from org.purl.sword.client.Client import *
#import org.apache.commons.httpclient.auth.AuthChallengeProcessor as AuthChallengeProcessor
#import org.apache.commons.httpclient.HttpMethodDirector as HttpMethodDirector
import au.edu.usq.fascinator.portal.SwordSimpleServer as SwordSimpleServer
import java.io.FileOutputStream as FileOutputStream
import org.apache.commons.io.IOUtils as IOUtils
from au.edu.usq.fascinator.api import PluginManager
import au.edu.usq.fascinator.common.JsonConfig as JsonConfig
import au.edu.usq.fascinator.common.JsonConfigHelper as JsonConfigHelper
import au.edu.usq.fascinator.common.storage.impl.GenericDigitalObject as GenericDigitalObject
import au.edu.usq.fascinator.common.storage.impl.FilePayload as FilePayload
import au.edu.usq.fascinator.transformer.ims.ImsDigitalObject as ImsDigitalObject
import java.io.File as File;
import org.apache.commons.io.FileUtils as FileUtils
import au.edu.usq.fascinator.HarvestClient as HarvestClient
import au.edu.usq.fascinator.QueueStorage as QueueStorage
import java.io.FileWriter as FileWriter


class Sword2(object):
    def __init__(self):
        self.__mimeType = "text/html"       # default mimeType
        self.__html = self.__getTest()
        request.setAttribute("Content-Type", self.__mimeType)


    def getMimeType(self):
        """ the results content mimeType """
        return self.__mimeType

    def getHtml(self):
        """ Called from the template to get the HTML content """
        return self.__html

    def __getTest(self):
        """ """
        # uses responseOutput.write() to output non HTML content
        depositUrl = "http://localhost:9997/portal/default/sword/deposit.post"
        sword = SwordSimpleServer(depositUrl)
        c = sword.getClient()
        c.clearProxy()
        c.clearCredentials()
        postMsg = sword.getPostMessage();
        postMsg.filetype = "application/zip"
        postMsg.filepath = "/home/ward/Desktop/Test.zip"

        try:
            p =  request.path.split(portalId+"/"+pageName+"/")[1]  # portalPath
        except:
            p = ""
        if p=="testpost":
            print "\n--- testpost ---"
            url = "http://localhost:9997/portal/default/sword/servicedocument"
            url = "http://localhost:8080/sword/app/servicedocument"
            username = "fedoraAdmin"
            password = username
            if True:
                pd = sword.getPostDestination()
                pd.setUrl(url)
                pd.setUsername(username)
                pd.setPassword(password)
                #pd.setUrl(i.location)
                #print pd
            c.setServer("localhost", 8080)
            c.setCredentials(username, password)
            sd = c.getServiceDocument(url)
            postMsg.destination = sd.service.workspaces[-1].collections[-1].location
        elif p=="post":
            print "\n--- post ---"
            depositResponse = c.postFile(postMsg)
            return str(depositResponse)
        elif p=="servicedocument":
            #print "\n--- servicedocument ---"
            sdr = sword.getServiceDocumentRequest()
            sdr.username = "TestUser"
            sdr.password = sdr.username
            if formData.get("test"):
                depositUrl += "?test=1"
            sd = sword.doServiceDocument(sdr)  # get a serviceDocument
            print "OK servicedocument"
            for ws in sd.service.workspaces:
                for i in ws.collections:
                    pass
                    #print i.location
            self.__mimeType = "text/xml"
            bindings["pageName"] = "-noTemplate-"
            responseOutput.write(str(sd))
            return sd
        elif p=="deposit.post":
            #print "\n--- deposit ---  formData='%s'" % str(formData)
            inputStream = formData.getInputStream()
            headers = {}
            for x in formData.getHeaders().entrySet():
                headers[x.getKey()] = x.getValue()
            deposit = sword.getDeposit()
            noOp = headers.get("X-No-Op") or "false"
            deposit.noOp = (noOp.lower()=="true") or \
                (formData.get("test") is not None)
            contentDisposition = headers.get("Content-Disposition", "")
            filename = ""
            if contentDisposition!="":
                try:
                    filename = contentDisposition.split("filename=")[1]
                    deposit.filename = filename
                except: pass
            slug = headers.get("Slug")
            if slug is not None and slug!="":
                deposit.slug = slug
            #elif filename!="":
            #    deposit.slug = filename

            deposit.username = "SwordUser"
            deposit.password = deposit.username
            try:
                file = File.createTempFile("tmptf", ".zip")
                file.deleteOnExit()
                fos = FileOutputStream(file.getAbsolutePath())
                IOUtils.copy(inputStream, fos)
                fos.close()
                print "copied posted data to '%s'" % file.getAbsolutePath()
            except Exception, e:
                print "--- Exception - '%s'" % str(e)
            deposit.contentDisposition = file.getAbsolutePath()         #????
            deposit.file = inputStream
            depositResponse = sword.doDeposit(deposit)
            id = str(depositResponse.getEntry().id)
            try:
                print
                #imsPlugin = PluginManager.getTransformer("ims")
                jsonConfig = JsonConfig()
                #imsPlugin.init(jsonConfig.getSystemFile())
                #harvestClient = HarvestClient(jsonConfig.getSystemFile());
                storagePlugin = PluginManager.getStorage(jsonConfig.get("storage/type"))
                #storagePlugin.init(jsonConfig.getSystemFile())

                setConfigUri = self.__getPortal().getClass().getResource("/swordRule.json").toURI()
                configFile = File(setConfigUri)
                harvestConfig = JsonConfigHelper(configFile);
                tFile = File.createTempFile("harvest", ".json")
                tFile.deleteOnExit()
                harvestConfig.set("configDir", configFile.getParent())
                harvestConfig.set("sourceFile", file.getAbsolutePath())
                harvestConfig.store(FileWriter(tFile))

                zipObject = GenericDigitalObject(id)
                zipObject.addPayload(FilePayload(file))
                #digitalObject = imsPlugin.transform(zipObject, file)
                qStorage = QueueStorage(storagePlugin, tFile)
                qStorage.init(jsonConfig.getSystemFile())
                qStorage.addObject(zipObject)
                if deposit.noOp:
                    print "-- Testing noOp='true' --"
                else:
                    # deposit the content
                    pass
            except Exception, e:
                print "---"
                print " -- Exception - '%s'" % str(e)
                print "---"
            response.setStatus(201)
            self.__mimeType = "text/xml"
            bindings["pageName"] = "-noTemplate-"
            responseOutput.write(str(depositResponse))
            return
        elif p=="test":
            print "\n--- testing ---"
            print "formData='%s'" % str(formData)
        return "Test"
    
    def __getPortal(self):
        return Services.portalManager.get(portalId)


scriptObject = Sword2()
