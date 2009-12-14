


#from org.purl.sword.client.Client import *
#import org.apache.commons.httpclient.auth.AuthChallengeProcessor as AuthChallengeProcessor
#import org.apache.commons.httpclient.HttpMethodDirector as HttpMethodDirector
import au.edu.usq.fascinator.portal.SwordSimpleServer as SwordSimpleServer
import java.io.FileOutputStream as FileOutputStream
import org.apache.commons.io.IOUtils as IOUtils


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
            deposit = sword.getDeposit()
            deposit.username = "TestUser"
            deposit.password = deposit.username
            deposit.contentDisposition = "/tmp/zzz.zip"         #????
            deposit.file = inputStream
            if formData.get("test"):
                print "*** Testing noOp=True ***"
                deposit.noOp = True
            else:
                fos = FileOutputStream("/tmp/zzz.zip")
                IOUtils.copy(inputStream, fos)
                fos.close()
            #deposit.slug = "Slug"
            #depositResponse = DummyServer().doDeposit(deposit)
            depositResponse = sword.doDeposit(deposit)
            response.setStatus(201)
            self.__mimeType = "text/xml"
            bindings["pageName"] = "-noTemplate-"
            responseOutput.write(str(depositResponse))
            return
        elif p=="test":
            print "\n--- testing ---"
            print "formData='%s'" % str(formData)
        return "Test"


scriptObject = Sword2()
